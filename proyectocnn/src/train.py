from pathlib import Path
import os
import json
import tensorflow as tf
from tensorflow.keras import layers, models, optimizers, callbacks
from tensorflow.keras.layers import Input
from tensorflow.keras.applications import MobileNetV2
from tensorflow.keras.preprocessing.image import ImageDataGenerator

root_dir = Path(__file__).resolve().parent.parent

CROPPED_DATASET_DIR = os.path.join(root_dir, "cropped_dataset")
MODEL_PATH = os.path.join(root_dir, "model", "face_classifier.h5")
TAGS_PATH = os.path.join(root_dir, "model", "tags.json")

# Hiper parametros
LEARNING_RATE = 1e-4
EPOCHS_HEAD = 28
EPOCHS_FINE = 12
BATCH_SIZE = 32
IMAGE_SIZE = (224, 224)

os.makedirs(Path(MODEL_PATH).parent.resolve(), exist_ok=True)
os.makedirs(Path(TAGS_PATH).parent.resolve(), exist_ok=True)

tags = os.listdir(CROPPED_DATASET_DIR)
with open(TAGS_PATH, "w") as f:
    json.dump(tags, f)

train_aug = ImageDataGenerator(
    rescale=1.0 / 255,
    rotation_range=20,
    zoom_range=0.15,
    width_shift_range=0.2,
    height_shift_range=0.2,
    shear_range=0.15,
    horizontal_flip=True,
    fill_mode="nearest",
    validation_split=0.2,
)

train_gen = train_aug.flow_from_directory(
    CROPPED_DATASET_DIR,
    target_size=IMAGE_SIZE,
    batch_size=BATCH_SIZE,
    class_mode="categorical",
    subset="training",
    shuffle=True,
)

val_gen = train_aug.flow_from_directory(
    CROPPED_DATASET_DIR,
    target_size=IMAGE_SIZE,
    batch_size=BATCH_SIZE,
    class_mode="categorical",
    subset="validation",
)

# Base model
base_model = MobileNetV2(
    input_tensor=Input(shape=(IMAGE_SIZE[0], IMAGE_SIZE[1], 3)),
    include_top=False,
    weights="imagenet",
)

x = base_model.output
x = layers.GlobalAveragePooling2D()(x)
x = layers.BatchNormalization()(x)
x = layers.Dropout(0.5)(x)
x = layers.Dense(512, activation="relu")(x)
x = layers.Dropout(0.4)(x)
x = layers.Dense(256)(x)
x = layers.BatchNormalization()(x)
x = layers.Dropout(0.3)(x)
x = layers.Dense(128, activation="relu")(x)
outputs = layers.Dense(train_gen.num_classes, activation="softmax")(x)

model = models.Model(base_model.input, outputs)

model.summary()

print("----------------Head Training---------------")

base_model.trainable = False

model.compile(
    optimizer=optimizers.Adam(learning_rate=LEARNING_RATE),
    loss="categorical_crossentropy",
    metrics=["accuracy"],
)

model.fit(
    train_gen,
    validation_data=val_gen,
    steps_per_epoch=len(train_gen),
    validation_steps=len(val_gen),
    epochs=EPOCHS_HEAD,
)

print("----------------Fine tuning---------------")
for layer in base_model.layers[-50:]:
    layer.trainable = True

model.compile(
    optimizer=optimizers.Adam(learning_rate=LEARNING_RATE / 10),
    loss="categorical_crossentropy",
    metrics=["accuracy"],
)

model.fit(
    train_gen, 
    steps_per_epoch=len(train_gen),
    validation_steps=len(val_gen),
    validation_data=val_gen, 
    epochs=EPOCHS_FINE
)

model.save(MODEL_PATH)
