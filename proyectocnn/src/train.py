import os
import cv2
import numpy as np
import pickle
import shutil
from mtcnn.mtcnn import MTCNN
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.applications import MobileNetV2
from tensorflow.keras.layers import GlobalAveragePooling2D, Dropout, Flatten, Dense, Input
from tensorflow.keras.models import Model
from tensorflow.keras.optimizers import Adam

# --- CONFIGURACIÓN DE DIRECTORIOS ---
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
ORIGINAL_DATASET_DIR = os.path.abspath(os.path.join(BASE_DIR, '..', 'dataset'))
PROCESSED_DATASET_DIR = os.path.abspath(os.path.join(BASE_DIR, '..', 'dataset_faces_clean'))
MODEL_DIR = os.path.abspath(os.path.join(BASE_DIR, '..', 'models'))

os.makedirs(MODEL_DIR, exist_ok=True)

# --- HIPERPARÁMETROS ---
INIT_LR = 1e-4
EPOCHS_HEAD = 36    # Épocas iniciales
EPOCHS_FINE = 15    # Épocas de ajuste fino (Subí un poco esto)
BS = 32
IMAGE_SIZE = (224, 224)

def create_face_dataset_safe(input_dir, output_dir):
    print("[INFO] Cargando detector MTCNN...")
    detector = MTCNN()
    
    total_images = 0
    faces_saved = 0
    errors = 0

    print(f"[INFO] Procesando (o reanudando) imágenes desde: {input_dir}")

    for root, dirs, files in os.walk(input_dir):
        for file in files:
            if file.lower().endswith(('.png', '.jpg', '.jpeg')):
                total_images += 1
                
                # Definir rutas
                image_path = os.path.join(root, file)
                rel_path = os.path.relpath(root, input_dir)
                output_subdir = os.path.join(output_dir, rel_path)
                output_path = os.path.join(output_subdir, file)
                
                # --- LÓGICA DE REANUDACIÓN ---
                # Si la imagen ya existe, saltar (ahorra tiempo de lo que ya hiciste ayer)
                if os.path.exists(output_path):
                    faces_saved += 1
                    if total_images % 1000 == 0:
                        print(f"[REANUDANDO] Revisadas {total_images} imágenes...")
                    continue

                os.makedirs(output_subdir, exist_ok=True)
                
                try:
                    # Leer imagen
                    img = cv2.imread(image_path)
                    if img is None: continue
                    
                    # --- PROTECCIÓN DE MEMORIA (FIX PARA TU ERROR) ---
                    # Si la imagen es gigante (>1200px), la redimensionamos antes de detectar
                    h, w = img.shape[:2]
                    max_dim = 1200
                    if h > max_dim or w > max_dim:
                        scale = max_dim / max(h, w)
                        img = cv2.resize(img, None, fx=scale, fy=scale)
                    
                    # Convertir a RGB
                    rgb_img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
                    
                    # Detectar caras
                    results = detector.detect_faces(rgb_img)
                    
                    if results:
                        best_face = None
                        max_area = 0

                        for res in results:
                            # Filtro de confianza > 95%
                            if res['confidence'] > 0.95:
                                x, y, w_box, h_box = res['box']
                                if w_box * h_box > max_area:
                                    max_area = w_box * h_box
                                    best_face = (x, y, w_box, h_box)
                        
                        if best_face:
                            (x, y, w_box, h_box) = best_face
                            # Padding y recortes seguros
                            x, y = max(0, x), max(0, y)
                            p = 20 
                            x = max(0, x - p)
                            y = max(0, y - p)
                            w_box = min(img.shape[1] - x, w_box + 2*p)
                            h_box = min(img.shape[0] - y, h_box + 2*p)
                            
                            face_crop = img[y:y+h_box, x:x+w_box]
                            
                            if face_crop.shape[0] > 50 and face_crop.shape[1] > 50:
                                cv2.imwrite(output_path, face_crop)
                                faces_saved += 1
                                print(f"[OK] Procesada nueva: {rel_path}")

                except Exception as e:
                    print(f"[ERROR] Falló la imagen {rel_path}. Saltando...")
                    # print(e) # Descomentar si quieres ver el error específico
                    errors += 1
                    continue

    print(f"\n[FIN PROCESAMIENTO]")
    print(f"Total imágenes revisadas: {total_images}")
    print(f"Total caras válidas en disco: {faces_saved}")
    print(f"Errores saltados: {errors}")

create_face_dataset_safe(ORIGINAL_DATASET_DIR, PROCESSED_DATASET_DIR)

print(f"[INFO] Cargando generadores de datos...")

trainAug = ImageDataGenerator(
    rescale=1./255,
    rotation_range=20,
    zoom_range=0.15,
    width_shift_range=0.2,
    height_shift_range=0.2,
    shear_range=0.15,
    horizontal_flip=True,
    fill_mode="nearest",
    validation_split=0.2
)

trainGen = trainAug.flow_from_directory(
    PROCESSED_DATASET_DIR,
    target_size=IMAGE_SIZE,
    batch_size=BS,
    class_mode='categorical',
    subset='training'
)

valGen = trainAug.flow_from_directory(
    PROCESSED_DATASET_DIR,
    target_size=IMAGE_SIZE,
    batch_size=BS,
    class_mode='categorical',
    subset='validation'
)

if trainGen.samples == 0:
    print("[ERROR] No hay imágenes. Algo falló en la etapa de MTCNN.")
    exit()

print("[INFO] Construyendo MobileNetV2 Mejorado...")
baseModel = MobileNetV2(weights="imagenet", include_top=False,
                        input_tensor=Input(shape=(IMAGE_SIZE[0], IMAGE_SIZE[1], 3)))

headModel = baseModel.output
headModel = GlobalAveragePooling2D()(headModel)
headModel = Dense(1024, activation="relu")(headModel)
headModel = Dropout(0.5)(headModel)
headModel = Dense(trainGen.num_classes, activation="softmax")(headModel)

model = Model(inputs=baseModel.input, outputs=headModel)

# --- FASE 1: ENTRENAMIENTO DE LA CABEZA ---
for layer in baseModel.layers:
    layer.trainable = False

print("[INFO] Entrenando cabeza (Fase 1)...")
model.compile(loss="categorical_crossentropy", optimizer=Adam(learning_rate=INIT_LR),
              metrics=["accuracy"])

model.fit(
    trainGen,
    steps_per_epoch=len(trainGen),
    validation_data=valGen,
    validation_steps=len(valGen),
    epochs=EPOCHS_HEAD
)

# --- FASE 2: FINE-TUNING ---
print("[INFO] Descongelando capas para Fine-Tuning (Fase 2)...")
# Descongelamos un poco más (40 capas) para dar más libertad al modelo
for layer in baseModel.layers[-40:]:
    layer.trainable = True

print("[INFO] Re-compilando con Learning Rate bajo...")
model.compile(loss="categorical_crossentropy", optimizer=Adam(learning_rate=INIT_LR/10),
              metrics=["accuracy"])

model.fit(
    trainGen,
    steps_per_epoch=len(trainGen),
    validation_data=valGen,
    validation_steps=len(valGen),
    epochs=EPOCHS_FINE
)

model_path = os.path.join(MODEL_DIR, 'face_classifier.h5')
model.save(model_path)
print(f'[INFO] Modelo guardado en {model_path}')

label_map = trainGen.class_indices
with open(os.path.join(MODEL_DIR, 'labels.pkl'), 'wb') as f:
    pickle.dump(label_map, f)
print('[INFO] Label encoder guardado.')