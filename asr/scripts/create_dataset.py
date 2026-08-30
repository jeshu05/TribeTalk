"""
Classroom Hindi Dataset Generator (Phase 3)
Generates 30 authentic 16 kHz mono WAV speech samples covering classroom instructions, FLN numbers, counting, shapes, colors, and math.
Creates asr/tests/metadata.csv.
"""

import os
import sys
import csv
import soundfile as sf
import librosa
from gtts import gTTS

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

CLASSROOM_UTTERANCES = [
    ("001.wav", "बच्चों आज हम गिनती सीखेंगे"),
    ("002.wav", "अपनी किताब खोलिए"),
    ("003.wav", "सब बच्चे ध्यान से सुनिए"),
    ("004.wav", "चित्र को ध्यान से देखिए"),
    ("005.wav", "पहले पांच वस्तुओं को गिनिए"),
    ("006.wav", "अब मेरे साथ बोलिए"),
    ("007.wav", "एक दो तीन चार पांच"),
    ("008.wav", "छह सात आठ नौ दस"),
    ("009.wav", "पांच में दो जोड़िए"),
    ("010.wav", "चार में से एक घटाइए"),
    ("011.wav", "लाल रंग का फल कौन सा है"),
    ("012.wav", "यह गोल आकार है"),
    ("013.wav", "अपनी कॉपी में लिखिए"),
    ("014.wav", "अ से अनार और आ से आम"),
    ("015.wav", "क ख ग घ ङ"),
    ("016.wav", "बहुत अच्छा काम किया"),
    ("017.wav", "दरवाजा बंद कीजिए"),
    ("018.wav", "कलम और पेंसिल उठाइए"),
    ("019.wav", "आज का पाठ समाप्त हुआ"),
    ("020.wav", "नमस्ते गुरुजी"),
    ("021.wav", "क्या आप तैयार हैं"),
    ("022.wav", "पेज नंबर दस निकालिए"),
    ("023.wav", "गणित का सवाल हल कीजिए"),
    ("024.wav", "हरा पत्ता और पीला फूल"),
    ("025.wav", "पानी की बोतल पास रखिए"),
    ("026.wav", "अक्षरों को जोड़कर पढ़िए"),
    ("027.wav", "तीन और तीन छह होते हैं"),
    ("028.wav", "शाबाश बच्चों"),
    ("029.wav", "होमवर्क पूरा कीजिए"),
    ("030.wav", "जय हिंद जय भारत")
]

def main():
    audio_dir = "asr/tests/audio"
    csv_path = "asr/tests/metadata.csv"
    os.makedirs(audio_dir, exist_ok=True)

    print("============================================================")
    print("GENERATING CLASSROOM HINDI EVALUATION DATASET (Phase 3)")
    print("============================================================")

    rows = []
    for filename, ref_text in CLASSROOM_UTTERANCES:
        out_wav = os.path.join(audio_dir, filename)
        temp_mp3 = out_wav.replace(".wav", ".mp3")
        
        print(f"[*] Generating {filename}: '{ref_text}'...")
        tts = gTTS(text=ref_text, lang='hi')
        tts.save(temp_mp3)
        
        # Load and convert to 16 kHz mono WAV
        y, sr = librosa.load(temp_mp3, sr=16000, mono=True)
        sf.write(out_wav, y, 16000)
        
        if os.path.exists(temp_mp3):
            os.remove(temp_mp3)

        rows.append({"file": filename, "reference": ref_text})

    # Write metadata.csv
    with open(csv_path, "w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=["file", "reference"])
        writer.writeheader()
        writer.writerows(rows)

    print("\n" + "=" * 60)
    print(f"[OK] Generated {len(rows)} audio files in {audio_dir}")
    print(f"[OK] Saved metadata to {csv_path}")
    print("=" * 60)

if __name__ == "__main__":
    main()
