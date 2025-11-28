from flask import Flask, request, jsonify
from flask_cors import CORS
import os
import numpy as np
from transformers import Wav2Vec2Processor, Wav2Vec2ForCTC
import torch
import soundfile as sf
import Levenshtein

app = Flask(__name__)
CORS(app)

# Cargar modelo de reconocimiento de voz
# Usamos Wav2Vec2 multilingüe que soporta español
MODEL_NAME = "facebook/wav2vec2-large-xlsr-53-spanish"
processor = Wav2Vec2Processor.from_pretrained(MODEL_NAME)
model = Wav2Vec2ForCTC.from_pretrained(MODEL_NAME)

# Diccionario de palabras Quechua (expandible)
PALABRAS_QUECHUA = {
    "allinllachu": "allinllachu",
    "añay": "añay",
    "inti": "inti",
    "mama quilla": "mama quilla",
    "sumaq kawsay": "sumaq kawsay",
    "ñuqanchik": "ñuqanchik",
    "qhapaq": "qhapaq",
    "tukuy sunqu": "tukuy sunqu"
}

@app.route('/pronunciacion', methods=['POST'])
def evaluar_pronunciacion():
    try:
        # Recibir archivo de audio
        if 'audio' not in request.files:
            return jsonify({'error': 'No se recibió archivo de audio'}), 400
        
        audio_file = request.files['audio']
        palabra_esperada = request.form.get('palabra', '').lower()
        
        # Guardar archivo temporalmente
        temp_path = os.path.join('temp', audio_file.filename)
        os.makedirs('temp', exist_ok=True)
        audio_file.save(temp_path)
        
        # Procesar audio
        audio_input, sample_rate = sf.read(temp_path)
        
        # Asegurar sample rate de 16000 Hz
        if sample_rate != 16000:
            import librosa
            audio_input = librosa.resample(audio_input, orig_sr=sample_rate, target_sr=16000)
        
        # Convertir a tensor
        inputs = processor(audio_input, sampling_rate=16000, return_tensors="pt", padding=True)
        
        # Hacer predicción
        with torch.no_grad():
            logits = model(inputs.input_values).logits
        
        # Decodificar
        predicted_ids = torch.argmax(logits, dim=-1)
        transcripcion = processor.batch_decode(predicted_ids)[0].lower()
        
        # Calcular similitud
        similitud = calcular_similitud(transcripcion, palabra_esperada)
        puntaje = int(similitud * 100)
        
        # Generar feedback
        feedback = generar_feedback(puntaje)
        
        # Limpiar archivo temporal
        os.remove(temp_path)
        
        return jsonify({
            'puntaje': puntaje,
            'transcripcion': transcripcion,
            'esperado': palabra_esperada,
            'feedback': feedback,
            'similitud': similitud
        })
    
    except Exception as e:
        print(f"Error: {str(e)}")
        return jsonify({'error': str(e)}), 500

def calcular_similitud(texto1, texto2):
    """Calcula similitud usando distancia de Levenshtein"""
    texto1 = texto1.strip().lower()
    texto2 = texto2.strip().lower()
    
    if not texto1 or not texto2:
        return 0.0
    
    # Distancia de Levenshtein
    distancia = Levenshtein.distance(texto1, texto2)
    max_len = max(len(texto1), len(texto2))
    
    if max_len == 0:
        return 1.0
    
    similitud = 1 - (distancia / max_len)
    return max(0.0, min(1.0, similitud))

def generar_feedback(puntaje):
    """Genera feedback basado en el puntaje"""
    if puntaje >= 90:
        return "¡Excelente pronunciación! 🌟"
    elif puntaje >= 75:
        return "¡Muy bien! Sigue así 👍"
    elif puntaje >= 60:
        return "Bien, pero puedes mejorar 💪"
    else:
        return "Intenta nuevamente, ¡tú puedes! 🔄"

@app.route('/traducir', methods=['POST'])
def traducir():
    """Endpoint para traducción Quechua-Español"""
    try:
        data = request.json
        texto = data.get('texto', '').lower().strip()
        idioma_origen = data.get('de', 'qu')
        idioma_destino = data.get('a', 'es')
        
        # Diccionario simple (expandir con más palabras)
        diccionario_qu_es = {
            "allinllachu": "¿Cómo estás?",
            "añay": "¡Qué lindo!",
            "inti": "sol",
            "mama quilla": "madre luna",
            "sumaq kawsay": "buen vivir",
            "ñuqanchik": "nosotros/as",
            "qhapaq": "rico/poderoso",
            "tukuy sunqu": "con todo el corazón",
            "kay": "esto",
            "chay": "eso",
            "runa": "persona",
            "warmi": "mujer",
            "qhari": "hombre",
            "wawa": "bebé/niño",
            "yachay": "aprender/saber",
            "munay": "querer/amar"
        }
        
        # Invertir diccionario para español-quechua
        diccionario_es_qu = {v: k for k, v in diccionario_qu_es.items()}
        
        if idioma_origen == 'qu' and idioma_destino == 'es':
            traduccion = diccionario_qu_es.get(texto, f"Traducción no disponible para '{texto}'")
        elif idioma_origen == 'es' and idioma_destino == 'qu':
            traduccion = diccionario_es_qu.get(texto, f"Traducción no disponible para '{texto}'")
        else:
            traduccion = "Dirección de traducción no soportada"
        
        return jsonify({
            'traduccion': traduccion,
            'texto_original': texto,
            'de': idioma_origen,
            'a': idioma_destino
        })
    
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/health', methods=['GET'])
def health():
    return jsonify({'status': 'OK', 'message': 'API funcionando correctamente'})

if __name__ == '__main__':
    print("🚀 Iniciando API de PAQU...")
    print("📝 Endpoints disponibles:")
    print("   - POST /pronunciacion")
    print("   - POST /traducir")
    print("   - GET  /health")
    app.run(host='0.0.0.0', port=5000, debug=True)