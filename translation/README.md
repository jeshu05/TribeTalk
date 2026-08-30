# IndicTrans2 Hindi → Santali NMT Evaluation Subsystem

An offline, reproducible evaluation pipeline that evaluates the pretrained official [`ai4bharat/indictrans2-indic-indic-dist-320M`](https://huggingface.co/ai4bharat/indictrans2-indic-indic-dist-320M) model — **without fine-tuning** — for direct translation from Hindi Devanagari (`hin_Deva`) into Santali Ol Chiki (`sat_Olck`).

---

## 📊 Summary of Benchmark Results (IN22-Conv Parallel Benchmark)

* **Dataset Path**: `C:\Users\jesva\Downloads\translation-data\hi_sat_in22.csv` (1,503 sentence pairs)
* **Translation Direction**: `hin_Deva` $\rightarrow$ `sat_Olck`
* **chrF++ Score**: **31.54**
* **BLEU Score**: **4.99**
* **Average Sentence Latency**: **849.93 ms**
* **Median Latency**: **853.36 ms**
* **P95 Latency**: **1,187.00 ms**
* **Peak RAM**: **1,822.95 MB**
* **Throughput**: **1.18 sentences / sec**

---

## 1. Installation
```bash
git clone https://github.com/jeshu05/TribeTalk.git
cd TribeTalk/translation

# Install Python dependencies
pip install -r requirements.txt
```

---

## 2. Model Download
The pipeline automatically downloads the model assets from HF mirror `hari31416/indictrans2-indic-indic-dist-320M-ONNX` into `translation/models/` or can be fetched via:
```bash
python -c "
from huggingface_hub import HfApi, hf_hub_download
import os
os.makedirs('translation/models', exist_ok=True)
api = HfApi()
for f in api.list_repo_files('hari31416/indictrans2-indic-indic-dist-320M-ONNX'):
    if not f.startswith('.') and not f.endswith('.png') and '__pycache__' not in f:
        hf_hub_download('hari31416/indictrans2-indic-indic-dist-320M-ONNX', f, local_dir='translation/models')
"
```

---

## 3. Run Hindi → Santali Translation Pipeline
Translate all 1,503 parallel sentences into Santali Ol Chiki:
```bash
python scripts/translate_hi_sat.py --csv "C:\Users\jesva\Downloads\translation-data\hi_sat_in22.csv" --batch-size 8 --output results/predictions.csv
```

---

## 4. Run Evaluation Metrics (BLEU & chrF++)
Compute BLEU, chrF++, exact match rate, and sentence length statistics:
```bash
python scripts/evaluate_translation.py results/predictions.csv results/metrics.json
```

---

## 5. Run Benchmark (Latency & RAM Measurements)
Benchmark model load time, first inference latency, per-sentence latency distribution, throughput, and RAM footprint:
```bash
python scripts/benchmark_translation.py "C:\Users\jesva\Downloads\translation-data\hi_sat_in22.csv" results/metrics.json
```

---

## 6. Output Artifacts

* [`results/predictions.csv`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/translation/results/predictions.csv): 1,503 ground-truth reference vs predicted Santali Ol Chiki translations.
* [`results/metrics.json`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/translation/results/metrics.json): JSON record of all measured metrics.
* [`results/qualitative_examples.csv`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/translation/results/qualitative_examples.csv): 30 representative qualitative sentence pairs.
* [`results/qualitative_report.md`](file:///c:/Users/jesva/Documents/Documents/rec/notes/sem5/Projects/TribeTalk/translation/results/qualitative_report.md): Human-readable markdown comparison table.

---

## 7. Known Limitations
1. Morphological inflections in Santali (agglutinative grammar) lead to word boundary shifts, making `chrF++` (31.54) a much more accurate evaluation metric than word-level BLEU (4.99).
2. Desktop inference latency (~850 ms per sentence) is measured on CPU; mobile INT8 optimization is scheduled for the next deployment phase.
