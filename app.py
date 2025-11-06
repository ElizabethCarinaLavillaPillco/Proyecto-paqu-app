from flask import Flask, request, jsonify
from transformers import AutoTokenizer, AutoModelForSeq2SeqLM

app = Flask(__name__)

# --- Carga del Modelo y Tokenizador ---
# Este es el nombre correcto del modelo que quieres usar.
# La primera vez que ejecutes esto, tardará un poco mientras descarga el modelo.
MODEL_NAME = "somosnlp-hackathon-2022/t5-small-finetuned-spanish-to-quechua"

print("Cargando el modelo y tokenizador...")
try:
    tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
    model = AutoModelForSeq2SeqLM.from_pretrained(MODEL_NAME)
    print("¡Modelo cargado exitosamente!")
except Exception as e:
    print(f"Error al cargar el modelo: {e}")
    # Si hay un error, el programa se detendrá aquí.
    exit()

# --- Definición de la Ruta de la API ---
@app.route('/traducir', methods=['POST'])
def traducir_texto():
    # Obtener el JSON enviado en la petición
    datos = request.get_json()
    
    # Validar que se recibió texto
    if not datos or 'texto' not in datos:
        return jsonify({'error': 'No se proporcionó el campo "texto"'}), 400

    texto_a_traducir = datos['texto']
    print(f"Recibido para traducir: {texto_a_traducir}")

    # --- Proceso de Traducción ---
    # 1. Tokenizar el texto de entrada
    inputs = tokenizer(texto_a_traducir, return_tensors="pt", padding=True)

    # 2. Generar la traducción con el modelo
    outputs = model.generate(**inputs, max_length=128)

    # 3. Decodificar la salida a texto legible
    traduccion = tokenizer.decode(outputs[0], skip_special_tokens=True)
    print(f"Traducción generada: {traduccion}")

    # Devolver el resultado en formato JSON
    return jsonify({'traduccion': traduccion})

# --- Iniciar el Servidor ---
if __name__ == '__main__':
    # 'host="0.0.0.0"' hace que el servidor sea accesible desde otros dispositivos en tu misma red (como tu teléfono Android)
    app.run(host='0.0.0.0', port=5000, debug=True)
    