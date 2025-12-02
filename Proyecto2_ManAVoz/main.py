import easyocr
from PIL import Image
from gtts import gTTS
import pygame
import tkinter as tk
from tkinter import filedialog, messagebox, scrolledtext
import language_tool_python


pygame.mixer.init()
#OCR
reader = easyocr.Reader(['es'])
#Correccion de texto
tool = language_tool_python.LanguageTool('es')

texto_extraido = ""

def cargar_imagen():
    global texto_extraido

    ruta = filedialog.askopenfilename(
        title="Selecciona una imagen",
        filetypes=[("Imágenes", "*.png;*.jpg;*.jpeg;*.bmp")]
    )
    
    if not ruta:
        return

    try:
        #OCR
        resultados = reader.readtext(ruta, detail=0)
        texto_raw = " ".join(resultados)
        #Correccion de contexto y ortografía
        texto_corregido = tool.correct(texto_raw)
        texto_extraido = texto_corregido

        txt_area.delete(1.0, tk.END)
        txt_area.insert(tk.END, texto_corregido)

    except Exception as e:
        messagebox.showerror("Error", str(e))


def actualizar_configuracion(*args):
    """
    Traduce la opción seleccionada a lang + tld
    """
    op = voz_seleccion.get()

    if op == "es_es":
        voz_lang.set("es")
        voz_tld.set("es")

    elif op == "es_mx":
        voz_lang.set("es")
        voz_tld.set("com.mx")

    elif op == "en_us":
        voz_lang.set("en")
        voz_tld.set("com")


def generar_audio():
    global texto_extraido

    if not texto_extraido.strip():
        messagebox.showwarning("Aviso", "Primero extrae texto de una imagen.")
        return

    try:
        try:
            pygame.mixer.music.stop()
            pygame.mixer.music.unload()
        except:
            pass

        tts = gTTS(text=texto_extraido, lang=voz_lang.get(), tld=voz_tld.get())
        tts.save("salida.mp3")

        messagebox.showinfo("Listo", "Audio generado como salida.mp3")

    except Exception as e:
        messagebox.showerror("Error", str(e))


def reproducir_audio():
    try:
        pygame.mixer.music.load("salida.mp3")
        pygame.mixer.music.play()
    except:
        messagebox.showerror("Error", "No existe el archivo salida.mp3")


# ------------------ INTERFAZ ------------------

ventana = tk.Tk()
ventana.title("OCR + Texto a Voz")
ventana.geometry("600x600")


voz_seleccion = tk.StringVar(value="es_es") 
voz_lang = tk.StringVar()
voz_tld = tk.StringVar()

actualizar_configuracion()
voz_seleccion.trace_add("write", actualizar_configuracion)

btn_cargar = tk.Button(ventana, text="Cargar imagen", command=cargar_imagen)
btn_cargar.pack(pady=10)

txt_area = scrolledtext.ScrolledText(ventana, width=70, height=20)
txt_area.pack(pady=10)

# -------- ACENTOS --------
frame_acento = tk.LabelFrame(ventana, text="Selecciona el acento de voz")
frame_acento.pack(pady=10)

tk.Radiobutton(frame_acento, text="España", variable=voz_seleccion, value="es_es").pack(anchor="w")
tk.Radiobutton(frame_acento, text="México", variable=voz_seleccion, value="es_mx").pack(anchor="w")
tk.Radiobutton(frame_acento, text="Estados Unidos (Inglés)", variable=voz_seleccion, value="en_us").pack(anchor="w")

btn_audio = tk.Button(ventana, text="Generar Audio", command=generar_audio)
btn_audio.pack(pady=5)

btn_repro = tk.Button(ventana, text="Reproducir Audio", command=reproducir_audio)
btn_repro.pack(pady=5)

ventana.mainloop()
