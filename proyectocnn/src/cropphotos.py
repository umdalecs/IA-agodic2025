import os
import cv2
from pathlib import Path
import argparse
from mtcnn.mtcnn import MTCNN
import random

global DATASET_DIR

root_dir = Path(__file__).resolve().parent.parent

argument_parser = argparse.ArgumentParser()

argument_parser.add_argument(
    "--name", default=None, type=str, help="name of the dataset output"
)

args = argument_parser.parse_args()

if args.name == None:
    DATASET_DIR = os.path.join(root_dir, "dataset")
else:
    DATASET_DIR = os.path.join(root_dir, "dataset", args.name)

OUTPUT_DATASET = os.path.join(root_dir, "cropped_dataset")


def crop_face_dataset():
    face_detector = MTCNN()

    imagenes_con_cara = 0
    imagenes_sin_cara = 0

    for dirpath, _, filenames in os.walk(DATASET_DIR):
        for file in filenames:
            input_image = os.path.join(dirpath, file)
            output_dir = os.path.join(
               OUTPUT_DATASET, Path(dirpath).resolve().name
            )
            output_image = os.path.join(output_dir, file)

            os.makedirs(output_dir, exist_ok=True)

            img = cv2.imread(input_image)
            if img is None:
                continue

            # Reducir imágenes grandes (evita consumo masivo de RAM con MTCNN)
            MAX_SIZE = 1000  # puedes variar 1000–2000 según tu PC
            h, w = img.shape[:2]
            if max(h, w) > MAX_SIZE:
                scale = MAX_SIZE / max(h, w)
                img = cv2.resize(img, (int(w * scale), int(h * scale)))

            # ---- MTCNN detecta caras ----
            results = face_detector.detect_faces(img)

            if results:
                # Ordenar por área de bounding box para quedarnos con la mejor
                results = sorted(
                    results, key=lambda x: x["box"][2] * x["box"][3], reverse=True
                )
                x, y, w, h = results[0]["box"]

                p = 10
                x = max(0, x - p)
                y = max(0, y - p)
                w = min(img.shape[1] - x, w + 2 * p)
                h = min(img.shape[0] - y, h + 2 * p)

                face_crop = img[y : y + h, x : x + w]

                imagenes_con_cara += 1
                print(f"imagen recortada: {output_image}")
                cv2.imwrite(output_image, face_crop)
            else:
                imagenes_sin_cara += 1

    print(f"imagen con caras: {imagenes_con_cara}")
    print(f"imagen sin caras: {imagenes_sin_cara}")


crop_face_dataset()
