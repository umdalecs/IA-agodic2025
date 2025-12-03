import argparse
import cv2
import os
from pathlib import Path
import random
from datetime import datetime


def recortar_cuadrada(img):
    alto, ancho, _ = img.shape

    nuevo_tamano = min(alto, ancho)

    x_inicio = (ancho - nuevo_tamano) // 2
    y_inicio = (alto - nuevo_tamano) // 2

    img_cuadrada = img[
        y_inicio : y_inicio + nuevo_tamano, x_inicio : x_inicio + nuevo_tamano
    ]

    return img_cuadrada


def main():
    argument_parser = argparse.ArgumentParser()

    argument_parser.add_argument(
        "--name", default=None, type=str, help="name of the dataset output"
    )

    args = argument_parser.parse_args()

    if args.name == None:
        print("No --name arg provided")
        exit()

    root_dir = Path(__file__).resolve().parent.parent

    output_folder_train = os.path.join(root_dir, "dataset", args.name)

    os.makedirs(output_folder_train, exist_ok=True)

    cap = cv2.VideoCapture(0)

    if not cap.isOpened():
        print("Error: Could not open video device")
        exit()

    while True:
        ret, frame = cap.read()

        if not ret:
            print("Error: Failed to capture frame.")
            break

        key = cv2.waitKey(1) & 0xFF

        frame_cuadrada = recortar_cuadrada(frame)

        if key == ord("s"):
            image_filename = os.path.join(
                output_folder_train,
                f"image_{datetime.now().strftime("%Y%m%d%H%M%S%f")}.png",
            )

            cv2.imwrite(image_filename, frame_cuadrada)
            print(f"Image saved: {image_filename}")

        elif key == ord("q"):
            break

        cv2.putText(
            frame_cuadrada,
            "presiona 'q' para salir",
            (30, 30),
            cv2.FONT_HERSHEY_SIMPLEX,
            0.9,
            (0, 255, 0),
            1,
        )

        cv2.putText(
            frame_cuadrada,
            "presiona 's' para tomar fotos",
            (30, 50),
            cv2.FONT_HERSHEY_SIMPLEX,
            0.9,
            (0, 255, 0),
            1,
        )

        cv2.imshow("Preview", frame_cuadrada)

    cap.release()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()
