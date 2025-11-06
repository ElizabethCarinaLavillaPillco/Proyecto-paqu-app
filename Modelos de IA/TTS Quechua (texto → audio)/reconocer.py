# borrador solo para testear las conexiones 
from flask import Flask, request, jsonify
from transformers import AutoProcessor, AutoModelForCTC
import torch
import io
import soundfile as sf

app = Flask(__name__)

# Cargar modelo de Hugging Face
print("📦 Cargando modelo...")
processor = AutoProcessor.from_pretrained("facebook/mms-1b-all")
model = AutoModelForCTC.from_pretrained("facebook/mms-1b-all")
print("✅ Modelo cargado correctamente")

@app.route('/stt', methods=['POST'])
def stt():
    if 'audio' not in request.files:
        print("❌ No se recibió archivo 'audio'")
        return jsonify({"error": "No se recibió archivo"}), 400

    audio_file = request.files['audio']
    print(f"🎧 Recibido archivo: {audio_file.filename} - tipo: {audio_file.content_type}")
    
    print("🎧 Audio recibido")
    audio_file = request.files['audio']
    audio_bytes = audio_file.read()

    try:
        # Leer cualquier tipo de audio (wav, mp3, 3gp, etc.)
        waveform, sr = sf.read(io.BytesIO(audio_bytes))

        # Convertir a mono si es estéreo
        if len(waveform.shape) > 1:
            waveform = waveform.mean(axis=1)

        # Convertir a tensor de torch
        waveform_tensor = torch.tensor(waveform)

        # Resamplear si no es 16000 Hz
        if sr != 16000:
            import torchaudio
            resampler = torchaudio.transforms.Resample(orig_freq=sr, new_freq=16000)
            waveform_tensor = resampler(waveform_tensor.unsqueeze(0)).squeeze(0)

        # Procesar con el modelo
        inputs = processor(waveform_tensor, sampling_rate=16000, return_tensors="pt")
        with torch.no_grad():
            logits = model(**inputs).logits
        predicted_ids = torch.argmax(logits, dim=-1)
        transcription = processor.batch_decode(predicted_ids)[0]

        print("📄 Texto reconocido:", transcription)
        return jsonify({"text": transcription})

    except Exception as e:
        print("❌ Error:", e)
        return jsonify({"error": str(e)}), 400

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
