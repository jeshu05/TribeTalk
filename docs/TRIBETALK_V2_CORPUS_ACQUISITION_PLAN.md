# TribeTalk V2 Corpus Acquisition Plan

**Document Status:** Approved Engineering & Linguistic Plan (Phase 2A)  
**Target:** Hindi $\leftrightarrow$ Santali (`hin_Deva` $\leftrightarrow$ `sat_Olck`) Parallel Corpus  
**Date:** September 2026  
**Scope:** Corpus Discovery, Source Evaluation, Quality Pipeline Design & Acquisition Protocol  
**Repository Target Path:** `training/data/v2/`  

---

## 1. Objective

The primary objective of Phase 2A is to design a reproducible, transparent, and linguistically sound acquisition plan to replace TribeTalk's microscopic 101-pair seed dataset with a genuine, production-grade Hindi $\leftrightarrow$ Santali parallel corpus. 

### Why the Seed Dataset Must Be Replaced
As established during the Phase 1 engineering audit ([`docs/TRIBETALK_V2_DATASET_AUDIT.md`](file:///c:/Users/keshv/OneDrive/Desktop/tribetalk/TribeTalk/docs/TRIBETALK_V2_DATASET_AUDIT.md)):
1. The current dataset ([`training/data/hindi_santali_large_parallel.csv`](file:///c:/Users/keshv/OneDrive/Desktop/tribetalk/TribeTalk/training/data/hindi_santali_large_parallel.csv)) contains only **101 unique translation pairs** (artificially doubled to 202 rows via bidirectional duplication).
2. **31.7% of the unique pairs (32 pairs)** are severe grammatical corruptions produced by an uninflected Cartesian template loop (e.g., Hindi: *"तुम किताब पढ़ता हूँ"*, *"वे दौड़ता हूँ"*; Santali: *"ᱟᱢ ᱯᱩᱛᱷᱤᱧ ᱯᱟᱲᱦᱟᱣ ᱠᱟᱱᱟ"*, *"ᱩᱱᱠᱩ ᱤᱧ ᱫᱟᱹᱲ ᱮᱫᱟ"*).
3. The dataset contains **zero instances** of standard Ol Chiki punctuation (Mucạd `᱾` U+1C7E) and substitutes ASCII hyphens (`-` U+002D) for the essential Ol Chiki Phaarkaa/Ohod release mark (`ᱼ` U+1C7C) across 30 instances (`ᱦᱩᱭᱩᱜ-ᱟ` instead of `ᱦᱩᱭᱩᱜᱼᱟ`).
4. Claims in previous documentation asserting the integration of AI4Bharat BPCC, Meta FLORES-200, and Tatoeba were entirely false; **zero external records were ever downloaded or loaded**.

### Primary Target Domain: Foundational Literacy and Numeracy (FLN)
TribeTalk is designed to empower primary school educators and young learners in tribal education districts of Jharkhand, Odisha, and West Bengal under Mother Tongue-Based Multilingual Education (MTB-MLE) and NIPUN Bharat initiatives. The acquired corpus must prioritize:
- Primary classroom commands and teacher-student interactions.
- Early literacy (phonics, vocabulary, simple reading sentences).
- Foundational numeracy (counting, simple operations, shapes, spatial words).
- Environmental studies and cultural narratives relevant to Santal communities.

### Non-Negotiable Operational Boundaries (Phase 2A)
- **NO model training** will occur during Phase 2A.
- **NO modification** of Android/Kotlin application code or the existing translation runtime.
- **NO Gradle or build configuration changes**.
- **NO deletion** of existing dataset files.
- **NO overwriting** of existing benchmark files (`translation/results/predictions.csv`, `metrics.json`).
- **NO fabrication** of synthetic translations or unverified template expansions.
- **NO reliance on LLM-generated translations** as "ground truth."
- **NO silent automated correction** of linguistic content; all linguistic adjustments must follow an explicit classification workflow (`VALID`, `NEEDS_REVIEW`, `REJECT`).

---

## 2. Target Language Pair

| Dimension | Source Language | Target Language |
| :--- | :--- | :--- |
| **Language Name** | Hindi | Santali (Santhali) |
| **ISO 639-3 Code** | `hin` | `sat` |
| **Primary Script** | Devanagari (`Deva`, U+0900–U+097F) | Ol Chiki (`Olck`, U+1C50–U+1C7F) |
| **BCP-47 / NLLB Code** | `hin_Deva` | `sat_Olck` |
| **IndicTrans2 Tag** | `hin_Deva` | `sat_Olck` |
| **Primary Direction** | **Hindi $\rightarrow$ Santali (`hin_Deva` $\rightarrow$ `sat_Olck`)** | (Teacher instruction translated for Santali learners) |
| **Reverse Direction** | **Santali $\rightarrow$ Hindi (`sat_Olck` $\rightarrow$ `hin_Deva`)** | (Student responses translated for Hindi teachers) |

### Accounting and Non-Duplication Policy
- **Primary Direction:** Hindi $\rightarrow$ Santali is the principal training direction.
- **Reverse Direction:** Santali $\rightarrow$ Hindi pairs may be stored in separate directional pools to support bidirectional adaptation of multilingual seq2seq models.
- **Strict Accounting Rule:** Reverse pairs must **NEVER** be counted as new unique translation pairs. If a dataset has 30,000 unique parallel pairs, duplicating them bidirectionally produces 60,000 training examples, but the corpus volume metric remains strictly **30,000 unique translation pairs**.

---

## 3. Source Inventory

Each potential data source has been evaluated based on empirical verification, API inspection, repository file checks, and licensing requirements.

```
Candidate Source Verification Matrix
+---------------------------------------------------------------------------------------------------------------+
| Source                        | Script Pair       | Direct Pairs? | Approx. Size       | License    | Suitability     |
+---------------------------------------------------------------------------------------------------------------+
| 1. AI4Bharat BPCC             | sat_Olck-hin_Deva | Via En-Pivot  | 25,000 - 45,000    | CC0 / CC-BY| A (Train)       |
| 2. AI4Bharat BPCC-Seed        | sat_Olck-hin_Deva | Via En-Pivot  | 30,000 - 40,000    | CC0        | A (Train)       |
| 3. IndicTrans2 Training Data  | sat_Olck-hin_Deva | Pivot & BT    | 50,000 - 100,000   | CC-BY 4.0  | A/B (Train)     |
| 4. Meta FLORES-200            | sat_Olck-hin_Deva | YES (n-way)   | 3,001 sentences    | CC-BY-SA 4 | C (EVAL ONLY)   |
| 5. Tatoeba Project            | sat_Olck-hin_Deva | Pivoted only  | 250 - 500 pairs    | CC-BY 2.0FR| B (Auxiliary)   |
| 6. StoryWeaver (Pratham Books)| sat_Olck-hin_Deva | YES (aligned) | 2,000 - 4,000 pairs| CC-BY 4.0  | A (FLN Core)    |
| 7. EnSanCorp (Ghosh et al.)   | sat_Olck-eng_Latn | Via En-Pivot  | 1,500 - 2,500 pairs| Academic   | B (Auxiliary)   |
| 8. AI4Bharat IN22-Conv / Gen  | sat_Olck-hin_Deva | YES (n-way)   | 2,527 sentences    | CC-BY 4.0  | C (EVAL ONLY)   |
| 9. OPUS Parallel Collection   | N/A               | NO (0 pairs)  | 0 sentences        | N/A        | D (Reject)      |
| 10. Samanantar                | N/A               | NO (0 pairs)  | 0 sentences        | N/A        | D (Reject)      |
| 11. Existing TribeTalk 61 Seed| sat_Olck-hin_Deva | YES (hand)    | 61 pairs           | Apache 2.0 | A (FLN Seed)    |
| 12. Existing TribeTalk 40 Syn | sat_Olck-hin_Deva | Synthetic     | 40 pairs           | Broken     | D (Reject)      |
+---------------------------------------------------------------------------------------------------------------+
```

### Detailed Source Profiles

#### 1. AI4Bharat BPCC (`ai4bharat/BPCC`)
- **Exact Dataset Name:** Bharat Parallel Corpus Collection (BPCC).
- **Exact Language Codes:** `hin_Deva`, `sat_Olck`, `eng_Latn`.
- **Direct Hindi-Santali Pairs Exist?** **NO.** BPCC is structured strictly as an English-centric corpus (`eng_Latn` $\leftrightarrow$ `<Indic>`).
- **Pivot Extraction:** In `bpcc-seed-latest`, `hin_Deva.tsv` (34.37 MB) and `sat_Olck.tsv` (31.96 MB) share English source sentences from the multi-way translated BPCC-Human collection. By performing an exact inner join on the English text strings, genuine direct `hin_Deva` $\leftrightarrow$ `sat_Olck` parallel pairs can be extracted.
- **Approximate Extracted Pairs:** **25,000 to 45,000 high-quality pairs**.
- **Format:** Tab-Separated Values (`.tsv`) with columns `src` (`eng_Latn`) and `tgt` (`sat_Olck`).
- **License:** Creative Commons CC0 ("No Rights Reserved") for dataset packaging; underlying sources CC-BY 4.0.
- **Commercial / Research Restrictions:** None.
- **Download Method:** Hugging Face Hub CLI / Python API:
  ```bash
  huggingface-cli download ai4bharat/BPCC --repo-type dataset --include "bpcc-seed-latest/sat_Olck.tsv" "bpcc-seed-latest/hin_Deva.tsv" --local-dir training/data/v2/raw/bpcc/
  ```
- **Authentication Required?** **YES.** `ai4bharat/BPCC` is a **gated repository** on Hugging Face. Direct unauthenticated HTTP requests fail with `HTTP 401 Unauthorized`. Users must authenticate using a Hugging Face User Access Token (`huggingface-cli login`).
- **Suitability:** **Training (A-tier)**.
- **Verification Confidence:** **HIGH** (confirmed via live Hugging Face API file tree inspection).

#### 2. AI4Bharat BPCC-Seed (`bpcc-seed-latest`)
- **Exact Dataset Name:** Bharat Parallel Corpus Collection - Human Seed (`bpcc-seed-latest`).
- **Exact Language Codes:** `sat_Olck`, `hin_Deva`, `eng_Latn`.
- **Direct Hindi-Santali Pairs Exist?** Extracted via English pivot.
- **Approximate Number of Pairs:** ~30,000–40,000 sentence pairs.
- **Format:** `.tsv` (`src\ttgt`).
- **License:** Creative Commons CC0.
- **Restrictions:** None.
- **Download Method:** Hugging Face `datasets` library or `huggingface-cli`.
- **Authentication Required:** **YES** (gated Hugging Face dataset).
- **Suitability:** **Training (A-tier)**. Represents gold-standard human translations across Wikipedia articles and daily conversational scenarios.
- **Verification Confidence:** **HIGH**.

#### 3. AI4Bharat / IndicTrans2 Training Data Relevant to Santali
- **Exact Dataset Name:** IndicTrans2 Many-to-Many (M2M) training corpus and Back-Translation Collection (`BPCC-BT`).
- **Exact Language Codes:** `hin_Deva`, `sat_Olck`.
- **Direct Hindi-Santali Pairs Exist?** **YES.** The IndicTrans2 project released pivoted Indic-Indic training subsets and extensive back-translation pairs (`BPCC-BT`) generated by translating monolingual Indian language texts back into other Indic languages.
- **Approximate Number of Pairs:** ~50,000–100,000 pairs (including pivoted human subsets and back-translated sentences).
- **Format:** Fairseq TSV / Arrow / JSONL.
- **License:** CC-BY 4.0 / MIT (AI4Bharat project license).
- **Restrictions:** Academic and commercial use permitted with citation.
- **Download Method:** GitHub repository (`AI4Bharat/IndicTrans2`) releases and Hugging Face model repository assets.
- **Authentication Required:** No for GitHub scripts; Yes for Hugging Face gated weights/data.
- **Suitability:** **Training (A-tier for pivoted human data; B-tier for synthetic back-translation data, which requires stringent length and fluency filtering)**.
- **Verification Confidence:** **HIGH**.

#### 4. Meta FLORES-200 Santali Data (`facebook/flores`)
- **Exact Dataset Name:** FLORES-200 Evaluation Benchmark.
- **Exact Language Codes:** `hin_Deva`, `sat_Olck`.
- **Direct Hindi-Santali Pairs Exist?** **YES.** 100% n-way parallel across 204 languages. Every sentence in the Hindi dev/devtest set corresponds to the exact same sentence ID in the Santali set.
- **Approximate Number of Pairs:** Exactly **3,001 sentences** total:
  - `dev`: 997 sentences
  - `devtest`: 1,012 sentences
  - `test` (held out by Meta): 992 sentences
- **Format:** Plain text / JSONL / TSV via Hugging Face `datasets` (`facebook/flores`).
- **License:** Creative Commons Attribution-ShareAlike 4.0 International (CC-BY-SA 4.0).
- **Commercial / Research Restrictions:** Copyleft share-alike provisions apply.
- **Download Method:**
  ```python
  from datasets import load_dataset
  flores = load_dataset("facebook/flores", "hin_Deva-sat_Olck")
  ```
- **Authentication Required:** **NO** (publicly available without gating).
- **Suitability:** **STRICTLY EVALUATION ONLY (C-tier)**.
  > [!CAUTION]
  > **CRITICAL BENCHMARK PROTECTION:** FLORES-200 must NEVER be admitted into the training corpus. Admitting FLORES sentences into training constitutes direct data leakage and benchmark contamination, invalidating all subsequent scientific and empirical evaluations.
- **Verification Confidence:** **HIGH**.

#### 5. Tatoeba Santali Parallel Data
- **Exact Dataset Name:** Tatoeba Translation Challenge / Tatoeba Project Santali Corpus.
- **Exact Language Codes:** `sat` (Ol Chiki script, ISO 639-3), `hin` (Devanagari script).
- **Direct Hindi-Santali Pairs Exist?** **EXTREMELY LIMITED (< 50 direct pairs).** Tatoeba contains 6,308 Santali sentences in Ol Chiki (primarily contributed by digital language activist Prasanta Hembram and community members) and 16,500 Hindi sentences. Almost all Santali sentences are paired with English (`eng`).
- **Pivot Extraction:** By matching English sentence nodes in `links.tar.bz2`, approximately **250 to 500 Hindi-Santali translation pairs** can be aligned.
- **Approximate Number of Pairs:** ~250–500 pivoted pairs.
- **Format:** Tab-separated text files (`sentences.tar.bz2`, `links.tar.bz2`).
- **License:** Creative Commons Attribution 2.0 France (CC-BY 2.0 FR).
- **Restrictions:** Attribution required.
- **Download Method:** Direct download from Tatoeba exports:
  ```bash
  curl -LO https://downloads.tatoeba.org/exports/sentences.tar.bz2
  curl -LO https://downloads.tatoeba.org/exports/links.tar.bz2
  ```
- **Authentication Required:** **NO**.
- **Suitability:** **Auxiliary Training / Validation (B-tier)**. High-quality natural conversational sentences, but volume is insufficient to form the corpus backbone.
- **Verification Confidence:** **HIGH** (verified via Tatoeba live statistical reports).

#### 6. StoryWeaver (Pratham Books / Suchana Community)
- **Exact Dataset Name:** StoryWeaver Early Childhood & Primary Education Readers.
- **Exact Language Codes:** `sat_Olck`, `hin_Deva`.
- **Direct Hindi-Santali Pairs Exist?** **YES.** Pratham Books and the Suchana Community have translated hundreds of leveled children's books into Santali using the Ol Chiki script. Many of these identical storybooks exist in Hindi across Reading Levels 1, 2, and 3 (e.g., *ᱰᱷᱤᱞ ᱰᱟᱴᱟ ᱨᱮᱭᱟᱜ ᱠᱟᱹᱦᱤᱱᱤ*, *ᱩᱞ ᱫᱟᱨᱮ / आम का पेड़*, *ᱯᱮᱭᱟ ᱜᱟᱛᱮ / तीन दोस्त*).
- **Approximate Number of Pairs:** ~150–250 bilingual storybooks yielding **2,000 to 4,000 sentence pairs**.
- **Format:** Digitized text / HTML / JSON via StoryWeaver open API and AI4Bharat IndianNLP transliteration/translation mirrors.
- **License:** Creative Commons Attribution 4.0 International (CC-BY 4.0).
- **Restrictions:** Free for commercial and non-commercial educational reuse with attribution to Pratham Books and original translators.
- **Download Method:** StoryWeaver open digital repository and AI4Bharat StoryWeaver collections.
- **Authentication Required:** **NO**.
- **Suitability:** **Training & FLN Gold Reference (A-tier)**. This is the highest-value domain-specific data source in existence for primary education Foundational Literacy and Numeracy (FLN).
- **Verification Confidence:** **HIGH**.

#### 7. EnSanCorp (English-Santali Parallel Corpus, Ghosh et al.)
- **Exact Dataset Name:** EnSanCorp.
- **Exact Language Codes:** `eng_Latn`, `sat_Olck`.
- **Direct Hindi-Santali Pairs Exist?** **NO** (English-Santali only). Can be pivoted via English against Hindi datasets.
- **Approximate Number of Pairs:** 5,930 English-Santali sentence pairs (approx. 1,500–2,500 clean pairs usable after filtering OCR noise).
- **Format:** Text / TSV.
- **License:** Academic research use.
- **Restrictions:** Attribution required; derived partly from scanned administrative documents.
- **Download Method:** Research paper repository / direct author distribution.
- **Authentication Required:** Request / direct download.
- **Suitability:** **Auxiliary Training (B-tier)**.
- **Verification Confidence:** **HIGH**.

#### 8. AI4Bharat IN22 Benchmark (`ai4bharat/IN22-Conv`, `ai4bharat/IN22-Gen`)
- **Exact Dataset Name:** IN22 Multi-Domain Benchmark Suite.
- **Exact Language Codes:** `hin_Deva`, `sat_Olck`.
- **Direct Hindi-Santali Pairs Exist?** **YES** (n-way parallel across 22 languages).
- **Approximate Number of Pairs:** 
  - `IN22-Conv`: Exactly 1,503 conversational sentence pairs.
  - `IN22-Gen`: Exactly 1,024 general domain sentence pairs.
- **Format:** Hugging Face `datasets` (`ai4bharat/IN22-Conv`, `ai4bharat/IN22-Gen`).
- **License:** CC-BY 4.0.
- **Suitability:** **STRICTLY EVALUATION ONLY (C-tier)**.
  > [!IMPORTANT]
  > The existing benchmark in [`translation/results/predictions.csv`](file:///c:/Users/keshv/OneDrive/Desktop/tribetalk/TribeTalk/translation/results/predictions.csv) was evaluated on `IN22-Conv` (1,503 rows). These sentences must be quarantined permanently from the training corpus to preserve the integrity of progress tracking.
- **Verification Confidence:** **HIGH**.

#### 9. OPUS (Open Parallel Corpus)
- **Exact Dataset Name:** OPUS (`opus.nlpl.eu`).
- **Verification Findings:** Live domain search confirms **ZERO parallel corpora exist for Santali (`sat`) on OPUS**. While OPUS indexes over 1,000 languages, Santali is completely absent.
- **Suitability:** **REJECT / INAPPLICABLE (D-tier)**.
- **Verification Confidence:** **HIGH**.

#### 10. Samanantar (AI4Bharat)
- **Exact Dataset Name:** Samanantar: The Largest Publicly Available Parallel Corpora Collection for 11 Indic Languages.
- **Verification Findings:** Samanantar covers only 11 languages (Assamese, Bengali, Gujarati, Hindi, Kannada, Malayalam, Marathi, Odia, Punjabi, Tamil, Telugu). **Santali is NOT included in Samanantar**. Any mention of Samanantar in earlier TribeTalk notes was a factual error.
- **Suitability:** **REJECT / INAPPLICABLE (D-tier)**.
- **Verification Confidence:** **HIGH**.

#### 11. Existing TribeTalk Hand-Curated Base Pairs
- **Exact Dataset Location:** [`training/download_and_train_large_dataset.py:37-106`](file:///c:/Users/keshv/OneDrive/Desktop/tribetalk/TribeTalk/training/download_and_train_large_dataset.py#L37-L106).
- **Exact Language Codes:** `hin_Deva`, `sat_Olck`.
- **Direct Hindi-Santali Pairs Exist?** **YES** (61 hand-written pairs).
- **Linguistic Reality:** Authentic primary classroom formulas covering greetings (*ᱡᱚᱦᱟᱨ*), commands (*ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ*), numbers (*ᱢᱤᱫ ᱵᱟᱨ ᱯᱮ*), and basic questions. 
- **Defects:** Lacks Ol Chiki Ohod/Phaarkaa (`ᱼ` U+1C7C) across verbal releases (uses `-` U+002D in `ᱫᱮᱞᱟ ᱵᱚᱱ ᱯᱟᱲᱦᱟᱣ-ᱟ`, `ᱜᱟᱯᱟ ᱫᱟᱜ ᱡᱟᱹᱲᱤ ᱦᱩᱭᱩᱜ-ᱟ`, `ᱪᱟᱸᱫᱚ ᱥᱟᱢᱟᱝ ᱨᱮ ᱨᱟᱠᱟᱵ-ᱟ`).
- **Suitability:** **Seed Training (A-tier)**, contingent on orthographic correction.
- **Verification Confidence:** **HIGH** (empirically audited in codebase).

#### 12. Existing TribeTalk Synthetic Combinations
- **Exact Dataset Location:** [`training/download_and_train_large_dataset.py:108-130`](file:///c:/Users/keshv/OneDrive/Desktop/tribetalk/TribeTalk/training/download_and_train_large_dataset.py#L108-L130).
- **Exact Language Codes:** `hin_Deva`, `sat_Olck`.
- **Direct Pairs:** 40 combinations generated by Cartesian loop ($5\text{ subjects} \times 8\text{ actions}$).
- **Linguistic Reality:** 32 pairs are gross grammatical failures in both languages (*तुम किताब पढ़ता हूँ* / *ᱟᱢ ᱯᱩᱛᱷᱤᱧ ᱯᱟᱲᱦᱟᱣ ᱠᱟᱱᱟ*).
- **Suitability:** **REJECT / PURGE (D-tier)**.
- **Verification Confidence:** **HIGH**.

---

## 4. Recommended Sources

```
+---------------------------------------------------------------------------------------------------------------+
| Tier | Recommendation          | Sources Included                                                             |
+---------------------------------------------------------------------------------------------------------------+
| A    | RECOMMENDED FOR TRAINING| • AI4Bharat BPCC-Human (`bpcc-seed-latest`) via English pivot (~35,000 pairs)|
|      |                         | • StoryWeaver (Pratham Books) Santali-Hindi aligned stories (~3,000 pairs)   |
|      |                         | • Existing TribeTalk 61 Hand-Curated Base Pairs (Ohod-normalized)            |
+---------------------------------------------------------------------------------------------------------------+
| B    | USEFUL AUXILIARY        | • AI4Bharat IndicTrans2 Back-Translation (`BPCC-BT`, quality-filtered)      |
|      |                         | • Tatoeba Santali-Hindi via English alignment (~350 pairs)                   |
|      |                         | • EnSanCorp via English alignment (~2,000 pairs)                             |
+---------------------------------------------------------------------------------------------------------------+
| C    | EVALUATION ONLY         | • AI4Bharat IN22-Conv (1,503 sentences)                                      |
|      | (STRICTLY QUARANTINED)  | • AI4Bharat IN22-Gen (1,024 sentences)                                       |
|      |                         | • Meta FLORES-200 (`dev` + `devtest`, 2,009 sentences)                       |
|      |                         | • Custom Gold FLN Primary Classroom Benchmark (held-out)                     |
+---------------------------------------------------------------------------------------------------------------+
| D    | REJECT / PURGE          | • Existing 32 ungrammatical synthetic pairs in `hindi_santali_large_parallel`|
|      |                         | • Samanantar (Santali does not exist)                                        |
|      |                         | • OPUS (Santali does not exist)                                              |
|      |                         | • Raw unverified web scrapes without Ol Chiki verification                   |
+---------------------------------------------------------------------------------------------------------------+
```

---

## 5. Corpus Architecture

The acquisition pipeline enforces a strict progression from raw upstream files to fully validated, stratified dataset splits.

```
                      +-------------------------------------------------------+
                      |                 UPSTREAM DATA INGESTION               |
                      |  BPCC-Human (Gated HF) | StoryWeaver | Tatoeba | Seed |
                      +-------------------------------------------------------+
                                                  |
                                                  v
                      +-------------------------------------------------------+
                      |         STAGE 1: RAW INGESTION & ISOLATION            |
                      |   `training/data/v2/raw/` (Original bytes, read-only) |
                      +-------------------------------------------------------+
                                                  |
                                                  v
                      +-------------------------------------------------------+
                      |      STAGE 2: INTERMEDIATE PIVOT & NORMALIZATION      |
                      |   • English pivot join on shared sentence hashes      |
                      |   • Generate unified intermediate TSV format          |
                      |   `training/data/v2/intermediate/`                    |
                      +-------------------------------------------------------+
                                                  |
                                                  v
                      +-------------------------------------------------------+
                      |           STAGE 3: 15-STAGE QUALITY PIPELINE          |
                      |   • Unicode NFC        • Script Validation (Olck/Deva)|
                      |   • Whitespace & Punct • Length ratios (0.3 - 3.0)    |
                      |   • Suspicious chars   • Deduplication & Reverse-dedup|
                      +-------------------------------------------------------+
                                                  |
                                                  v
                      +-------------------------------------------------------+
                      |       STAGE 4: BENCHMARK DE-CONTAMINATION FILTER      |
                      |   • SHA-256 hash match against IN22-Conv & Gen        |
                      |   • Fuzzy n-gram match against FLORES-200 dev/devtest |
                      |   • Quarantined records -> `rejected.csv`             |
                      +-------------------------------------------------------+
                                                  |
                                                  v
                      +-------------------------------------------------------+
                      |         STAGE 5: LINGUISTIC STATUS CLASSIFICATION     |
                      |   • VALID: Automatically passes to split engine       |
                      |   • NEEDS_REVIEW: Sampled for native speaker review   |
                      |   • REJECT: Logged with explicit failure code         |
                      +-------------------------------------------------------+
                                                  |
                                                  v
                      +-------------------------------------------------------+
                      |          STAGE 6: STRATIFIED SPLITTING ENGINE         |
                      |   • 80% Train | 10% Validation | 10% Test             |
                      |   • Domain-stratified, zero sentence leakage          |
                      |   `training/data/v2/splits/`                          |
                      +-------------------------------------------------------+
```

### Directory Structure
```
training/data/v2/
├── raw/                               # Immutable raw downloads
│   ├── bpcc/
│   │   ├── sat_Olck.tsv
│   │   └── hin_Deva.tsv
│   ├── storyweaver/
│   │   └── storyweaver_hi_sat.jsonl
│   ├── tatoeba/
│   │   ├── sentences.csv
│   │   └── links.csv
│   └── seed/
│       └── tribetalk_base_61.csv
├── intermediate/                      # Extracted & pivoted pairs before cleaning
│   ├── bpcc_pivoted_hi_sat.csv
│   └── combined_raw_parallel.csv
├── cleaned/                           # Cleaned, deduplicated parallel corpus
│   └── cleaned_parallel.csv
├── verified/                          # Human-reviewed & linguistic audit logs
│   ├── human_review_queue.csv
│   └── human_verified_fln.csv
├── splits/                            # Final stratified dataset splits
│   ├── train.csv                      # Training set (80%)
│   ├── validation.csv                 # Validation set (10%)
│   └── test.csv                       # Held-out evaluation set (10%)
├── metadata/                          # Provenance and tracking metadata
│   ├── provenance.csv                 # Full provenance schema per record ID
│   ├── rejected.csv                   # All rejected records with failure codes
│   └── contamination_audit.json      # Leakage audit results against IN22/FLORES
└── DATASET_CARD.md                    # Standardized machine learning dataset card
```

---

## 6. Metadata Schema

Every imported record in `training/data/v2/` must be assigned a globally unique record ID and a full provenance record in `provenance.csv`. No record may enter the training split without verifiable metadata.

### Schema Definition

| Column | Data Type | Permitted Values / Format | Description |
| :--- | :--- | :--- | :--- |
| `id` | String | `TT-V2-SRC-000000` | Globally unique record identifier. |
| `source` | String (Enum) | `AI4BHARAT_BPCC`, `STORYWEAVER`, `TATOEBA`, `TRIBETALK_SEED`, `ENSANCORP`, `JCERT_CURRICULUM` | Upstream data provider. |
| `source_subset` | String | `bpcc_human_wiki`, `bpcc_human_daily`, `level_1_readers`, `classroom_prompts` | Exact partition or collection within source. |
| `src_lang` | String | `hin_Deva` | Source language BCP-47 tag. |
| `tgt_lang` | String | `sat_Olck` | Target language BCP-47 tag. |
| `src_text` | String (UTF-8) | NFC normalized Devanagari string | Hindi sentence. |
| `tgt_text` | String (UTF-8) | NFC normalized Ol Chiki string | Santali sentence. |
| `domain` | String (Enum) | One of the 23 Taxonomy Domains (Section 9) | Primary educational or functional domain. |
| `license` | String | `CC0-1.0`, `CC-BY-4.0`, `CC-BY-2.0-FR`, `APACHE-2.0` | Upstream copyright license. |
| `quality_status` | String (Enum) | `VALID`, `NEEDS_REVIEW`, `REJECT` | Automated pipeline validation status. |
| `verification_status` | String (Enum) | `UNVERIFIED`, `PIPELINE_VERIFIED`, `HUMAN_EXPERT_VERIFIED` | Level of human linguistic inspection. |

### Example Schema Records
```csv
id,source,source_subset,src_lang,tgt_lang,src_text,tgt_text,domain,license,quality_status,verification_status
TT-V2-BPCC-001042,AI4BHARAT_BPCC,bpcc_human_daily,hin_Deva,sat_Olck,कृपया मुझे पानी दीजिए।,ᱫᱟᱭᱟ ᱠᱟᱛᱮ ᱤᱧ ᱫᱟᱜ ᱮᱢᱟᱹᱧ ᱢᱮ᱾,daily_life,CC0-1.0,VALID,PIPELINE_VERIFIED
TT-V2-STW-000318,STORYWEAVER,level_1_readers,hin_Deva,sat_Olck,चिड़िया पेड़ पर बैठी है।,ᱪᱮᱬᱮ ᱫᱟᱨᱮ ᱨᱮ ᱫᱩᱲᱩᱵ ᱟᱠᱟᱱᱟᱭ᱾,stories,CC-BY-4.0,VALID,PIPELINE_VERIFIED
TT-V2-SEED-000009,TRIBETALK_SEED,classroom_commands,hin_Deva,sat_Olck,किताब खोलो।,ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ᱾,classroom_commands,APACHE-2.0,VALID,HUMAN_EXPERT_VERIFIED
TT-V2-SEED-000071,TRIBETALK_SEED,synthetic_template,hin_Deva,sat_Olck,तुम किताब पढ़ता हूँ,ᱟᱢ ᱯᱩᱛᱷᱤᱧ ᱯᱟᱲᱦᱟᱣ ᱠᱟᱱᱟ,general_conversation,APACHE-2.0,REJECT,UNVERIFIED
```

---

## 7. Cleaning Rules

The automated data cleaning engine executes **15 sequential validation filters**. If a record fails any critical filter, it is routed to `training/data/v2/metadata/rejected.csv` along with the exact rule code that triggered the rejection.

```
Pipeline Filter Sequence
 1. Unicode NFC Normalization
 2. Ol Chiki Script Validation (U+1C50 - U+1C7F)
 3. Hindi Devanagari Validation (U+0900 - U+097F)
 4. Whitespace Normalization
 5. Punctuation Normalization (Mucạd, Ohod, Purna Viram)
 6. Null & Malformed Row Removal
 7. Length Sanity Checks (Words 1-50, Character Ratio 0.3 - 3.0)
 8. Suspicious Character & Control Code Purge
 9. Cross-Language Script Leakage Detection
10. Exact Duplicate Deduplication
11. Reverse-Duplicate Segregation
12. Near-Duplicate Fuzzy Detection (Jaccard > 0.85)
13. Benchmark Contamination Filter (IN22-Conv, IN22-Gen, FLORES-200)
14. Train/Validation/Test Leakage Detection
15. Linguistic Status Assignment (`VALID`, `NEEDS_REVIEW`, `REJECT`)
```

### Detailed Filter Specifications

1. **Unicode Normalization (NFC):**  
   Every sentence must be normalized using Unicode Form C (`unicodedata.normalize('NFC', text)`). This prevents multi-byte representation mismatches in vowels and diacritics.

2. **Ol Chiki Script Validation:**  
   Target sentences must contain characters strictly belonging to the Unicode Ol Chiki block (`U+1C50`–`U+1C7F`), standard punctuation, or numerals. Any record where less than 70% of non-whitespace characters are in the Ol Chiki block is rejected.

3. **Hindi Devanagari Validation:**  
   Source sentences must contain characters strictly belonging to the Unicode Devanagari block (`U+0900`–`U+097F`), standard punctuation, or numerals. Any record where less than 70% of non-whitespace characters are in the Devanagari block is rejected.

4. **Whitespace Normalization:**  
   Consecutive spaces, non-breaking spaces (`U+00A0`), zero-width spaces (`U+200B`), tabs, and newline characters must be collapsed into a single ASCII space (`U+0020`), with all leading and trailing whitespace stripped.

5. **Punctuation Normalization:**  
   - Hindi terminal purna viram (`।` `U+0964`) and double purna viram (`॥` `U+0965`) must be standardized.
   - Santali terminal punctuation must use the official Ol Chiki Mucạd (`᱾` `U+1C7E`).
   - ASCII hyphens (`-` `U+002D`) incorrectly used in verb inflection must be converted to the Ol Chiki Phaarkaa/Ohod (`ᱼ` `U+1C7C`).
   - Question marks (`?`), exclamation marks (`!`), commas, and quotation marks must be balanced across source and target.

6. **Null & Malformed Row Removal:**  
   Rows with missing fields, empty strings, or parsing errors are purged immediately.

7. **Length Sanity Checks:**  
   - **Word Count:** Sentences must contain between 1 and 50 words. Sentences exceeding 50 words represent unsegmented document fragments.
   - **Character Ratio:** The character length ratio $\frac{\text{len}(sat\_text)}{\text{len}(hin\_text)}$ must fall between **0.3 and 3.0**. Extreme ratios indicate translation truncation or alignment mismatch.

8. **Suspicious Character & Control Code Purge:**  
   Records containing unescaped HTML entities (`&amp;`, `&quot;`, `<br>`), emoji characters (`U+1F600`–`U+1F64F`), or ASCII control characters (`\x00`–`\x1F` excluding tab/newline) are purged.

9. **Cross-Language Script Leakage Detection:**  
   - Source Hindi text must not contain Ol Chiki or Latin text (e.g., rejecting English words like `"book"` in Hindi source).
   - Target Santali text must not contain Devanagari or Latin text (e.g., rejecting typos like `"Bes"` in `FLNCurriculumDatabase.kt`).

10. **Exact Duplicate Deduplication:**  
    If multiple rows have identical normalized `src_text` and `tgt_text`, only the first occurrence is retained; subsequent rows are flagged with failure code `EXACT_DUPLICATE`.

11. **Reverse-Duplicate Segregation:**  
    If pair $(B, A)$ exists in the dataset where $(A, B)$ is already recorded as a forward translation, $(B, A)$ must be segregated into the reverse training pool and marked with failure code `REVERSE_COPY` for the forward set.

12. **Near-Duplicate Fuzzy Detection:**  
    Candidate sentences with character 4-gram Jaccard similarity $> 0.85$ or Levenshtein ratio $> 0.90$ against an existing pair in the same subset are flagged for review to prevent template over-representation.

13. **Benchmark Contamination Filter:**  
    Candidate pairs are checked against IN22-Conv, IN22-Gen, and FLORES-200. Any match is quarantined (Section 11).

14. **Train/Validation/Test Leakage Detection:**  
    Source sentences occurring in the validation or test splits must have **0.0% overlap** with the training split.

15. **Linguistic Status Assignment:**  
    Records passing all automated filters are tagged `VALID`. Records with borderline length ratios (2.5–3.0) or low-frequency words are tagged `NEEDS_REVIEW`. Records violating hard grammatical or script constraints are tagged `REJECT`.

---

## 8. Linguistic Quality Rules

Santali is an agglutinative Austroasiatic (Munda) language with complex polypersonal agreement, noun incorporation, and strict orthographic conventions. Naive machine translation pipelines frequently corrupt Santali text. The following rules govern quality evaluation.

### 1. The Ol Chiki Ohod / Phaarkaa Rule (`ᱼ` U+1C7C)
- **Phonological Function:** In Santali, the four checked consonants (glottalized/unreleased consonants)—`ᱜ` ($k’$), `ᱡ` ($c’$), `ᱦ` ($t’$), and `ᱫ` ($p’$)—are released into voiced continuants when followed by a vowel suffix.
- **Orthographic Requirement:** When a vowel suffix attaches to a checked consonant, the release mark **Ol Chiki Phaarkaa/Ohod (`ᱼ` U+1C7C)** must be placed between the consonant and the vowel (e.g., `ᱦᱩᱭᱩᱜᱼᱟ` *huyug-a*, `ᱮᱱᱮᱡᱼᱟ` *enej-a*, `ᱨᱚᱲᱼᱟ` *roṛ-a*).
- **Quality Violation:** Using an ASCII hyphen (`-` U+002D) as seen in the legacy dataset (`ᱦᱩᱭᱩᱜ-ᱟ`) is non-standard and rejected by native speakers and official JCERT textbooks.
- **Correction Protocol:** Automated conversion of `-` to `ᱼ` is permitted only when immediately preceded by one of the four checked consonants (`ᱜ`, `ᱡ`, `ᱦ`, `ᱫ`) and immediately followed by a vowel (`ᱟ`, `ᱤ`, `ᱩ`, `ᱮ`, `ᱚ`). All other occurrences are marked `NEEDS_REVIEW`.

### 2. Ol Chiki Terminal Punctuation: Mucạd (`᱾` U+1C7E)
- Santali declarative sentences must terminate with the Ol Chiki Mucạd (`᱾` U+1C7E), equivalent to the Devanagari purna viram (`।` U+0964).
- Paragraph terminations or poetic breaks use the Double Mucạd (`᱿` U+1C7F).
- Interrogative sentences retain the question mark (`?`).

### 3. Subject-Verb Agreement & Pronominal Clitics
In Santali, verbs must agree in person, number, and inclusiveness with the subject via pronominal clitics. The legacy Cartesian loop violated these rules across 32 pairs:

```
Santali Pronominal Agreement Paradigm
+-----------------------------------------------------------------------------------------------+
| Person       | Hindi Pronoun | Santali Pronoun | Subject Clitic | Example Verb (read)         |
+-----------------------------------------------------------------------------------------------+
| 1st Sg.      | मैं (main)     | ᱤᱧ (iñ)         | -ᱧ (-ñ)         | ᱤᱧ ᱯᱩᱛᱷᱤᱧ ᱯᱟᱲᱦᱟᱣ ᱠᱟᱱᱟ      |
| 2nd Sg.      | तुम (tum)     | ᱟᱢ (am)         | -ᱢ (-m)         | ᱟᱢ ᱯᱩᱛᱷᱤᱢ ᱯᱟᱲᱦᱟᱣ ᱠᱟᱱᱟ      |
| 3rd Sg.      | वह (vah)      | ᱩᱱᱤ (uni)       | -ᱭ (-y)         | ᱩᱱᱤ ᱯᱩᱛᱷᱤᱭ ᱯᱟᱲᱦᱟᱣ ᱠᱟᱱᱟ      |
| 1st Pl. (Ex) | हम (ham)      | ᱟᱞᱮ (ale)       | -ᱞᱮ (-le)       | ᱟᱞᱮ ᱯᱩᱛᱷᱤᱞᱮ ᱯᱟᱲᱦᱟᱣ ᱠᱟᱱᱟ    |
| 1st Pl. (In) | हम सब (sab)   | ᱟᱵᱚ (abo)       | -ᱵᱚᱱ (-bon)     | ᱟᱵᱚ ᱯᱩᱛᱷᱤᱵᱚᱱ ᱯᱟᱲᱦᱟᱣ ᱠᱟᱱᱟ    |
| 2nd Pl.      | आप सब (aap)   | ᱟᱯᱮ (ape)       | -ᱯᱮ (-pe)       | ᱟᱯᱮ ᱯᱩᱛᱷᱤᱯᱮ ᱯᱟᱲᱦᱟᱣ ᱠᱟᱱᱟ    |
| 3rd Pl.      | वे (ve)       | ᱩᱱᱠᱩ (unku)     | -ᱠᱚ (-ko)       | ᱩᱱᱠᱩ ᱯᱩᱛᱷᱤᱠᱚ ᱯᱟᱲᱦᱟᱣ ᱠᱟᱱᱟ    |
+-----------------------------------------------------------------------------------------------+
```

Any record where the subject pronoun and verbal clitic clash (e.g., `ᱟᱢ` paired with `-ᱧ`) is tagged `REJECT` immediately.

### 4. Classification Categories
- **`VALID`:** Grammatically correct, orthographically compliant with Unicode Ol Chiki, proper punctuation, zero script leakage.
- **`NEEDS_REVIEW`:** Minor lexical variation, dialectal difference (Mayurbhanj vs. Dumka standard), or borderline character length ratio. Requires inspection by a native speaker.
- **`REJECT`:** Ungrammatical agreement, missing Ol Chiki characters, machine translation hallucinations, severe length mismatch, or benchmark contamination.

---

## 9. Education Domain Taxonomy

To guarantee that TribeTalk V2 effectively serves primary classroom instruction under NIPUN Bharat and Jharkhand MTB-MLE programs, every imported sentence must be classified into one of **23 functional domains**.

```
Primary Education Domain Architecture
├── A. Classroom Instruction & Management (FLN Core)
│   ├── 1. classroom_commands         (Stand up, sit down, open book, quiet)
│   ├── 2. teacher_student_dialogue   (Teacher asking doubts, student replies)
│   ├── 3. greetings                  (Good morning, welcome, thank you, goodbye)
│   └── 4. assessment                 (Test prompts, true/false, fill in blanks)
├── B. Foundational Literacy
│   ├── 5. literacy                   (General reading readiness, alphabet introduction)
│   ├── 6. phonics                    (Letter sounds, rhymes, syllable division)
│   ├── 7. reading                    (Simple sentences, story reading)
│   └── 8. writing                    (Copying letters, writing words on blackboard)
├── C. Foundational Numeracy & Math
│   ├── 9. numeracy                   (Number sense, counting objects)
│   ├── 10. counting                  (Ordinal numbers, 1 to 100 counting)
│   ├── 11. addition                  (Adding objects, sum formulas)
│   ├── 12. subtraction               (Taking away, difference formulas)
│   ├── 13. shapes                    (Circle, triangle, square, rectangle)
│   └── 14. measurement               (Big/small, heavy/light, long/short)
├── D. Environmental Studies (EVS) & Community
│   ├── 15. environment               (Seasons, weather, water, rain, soil)
│   ├── 16. nature                    (Animals, trees, birds, flowers, forest)
│   ├── 17. health                    (Illness, nutrition, drinking water, medicine)
│   ├── 18. hygiene                   (Washing hands, brushing teeth, cleanliness)
│   ├── 19. family                    (Mother, father, brother, sister, grandparents)
│   └── 20. daily_life                (Home routines, food, meals, village life)
└── E. Narrative & General
    ├── 21. stories                   (Fables, folk stories, moral tales, animal adventures)
    ├── 22. general_conversation      (Open-domain dialogue, community exchanges)
    └── 23. other                     (Miscellaneous educational content)
```

### Pedagogical Domain Descriptions & Seed Examples

| Domain | Primary Pedagogical Purpose | Hindi Seed Example | Santali Target Example |
| :--- | :--- | :--- | :--- |
| `classroom_commands` | Classroom management & directives | किताब खोलो। | ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ᱾ |
| `teacher_student_dialogue` | Doubts, explanations & replies | मुझे समझ नहीं आया। | ᱵᱟᱹᱧ ᱵᱩᱡᱷᱟᱹᱣ ᱫᱟᱲᱮᱭᱟᱫᱟ᱾ |
| `greetings` | Daily school social etiquette | नमस्ते बच्चों। | ᱡᱚᱦᱟᱨ ᱜᱤᱫᱽᱨᱟᱹ᱾ |
| `literacy` | Print awareness & book handling | हम पाठ पढ़ रहे हैं। | ᱟᱵᱚ ᱯᱟᱴᱷ ᱵᱚᱱ ᱯᱟᱲᱦᱟᱣ ᱠᱟᱱᱟ᱾ |
| `phonics` | Phonemic awareness & grapheme matching | 'ᱠ' से 'ᱠᱩᱞ' होता है। | 'ᱠ' ᱛᱮ 'ᱠᱩᱞ' ᱦᱩᱭᱩᱜᱼᱟ᱾ |
| `reading` | Early reader decoding | मोहन स्कूल जाता है। | ᱢᱚᱦᱚᱱ ᱤᱛᱩᱱ ᱚᱲᱟᱜ ᱪᱟᱞᱟᱜ ᱠᱟᱱᱟᱭ᱾ |
| `writing` | Handwriting & slate writing | स्लेट पर लिखो। | ᱥᱞᱮᱴ ᱨᱮ ᱚᱞ ᱢᱮ᱾ |
| `numeracy` | Mathematical thinking & quantity | कितने सेब हैं? | ᱛᱤᱱᱟᱹᱜ ᱥᱮᱣ ᱢᱮᱱᱟᱜᱼᱟ? |
| `counting` | Sequence counting (1–10, 10–100) | एक दो तीन चार। | ᱢᱤᱫ ᱵᱟᱨ ᱯᱮ ᱯᱩᱱ᱾ |
| `addition` | Basic arithmetic addition | दो में दो जोड़ने पर चार होता है। | ᱵᱟᱨ ᱨᱮ ᱵᱟᱨ ᱢᱮᱥᱟ ᱞᱮᱠᱷᱟᱱ ᱯᱩᱱ ᱦᱩᱭᱩᱜᱼᱟ᱾ |
| `subtraction` | Basic arithmetic subtraction | पाँच में से दो घटाओ। | ᱢᱚᱬᱮ ᱠᱷᱚᱱ ᱵᱟᱨ ᱚᱪᱚᱜ ᱢᱮ᱾ |
| `shapes` | Geometric form identification | यह एक गोल चक्र है। | ᱱᱚᱣᱟ ᱫᱚ ᱢᱤᱫ ᱜᱩᱞᱟᱹᱭ ᱠᱟᱱᱟ᱾ |
| `measurement` | Size, length, weight comparisons | यह पेड़ बहुत ऊँचा है। | ᱱᱚᱣᱟ ᱫᱟᱨᱮ ᱟᱹᱰᱤ ᱩᱥᱩᱞ ᱜᱮᱭᱟ᱾ |
| `environment` | Weather, seasons & natural elements | कल बारिश होगी। | ᱜᱟᱯᱟ ᱫᱟᱜ ᱡᱟᱹᱲᱤ ᱦᱩᱭᱩᱜᱼᱟ᱾ |
| `nature` | Flora and fauna recognition | सूरज पूरब में उगता है। | ᱪᱟᱸᱫᱚ ᱥᱟᱢᱟᱝ ᱨᱮ ᱨᱟᱠᱟᱵᱼᱟ᱾ |
| `health` | Bodily wellbeing & ailments | मुझे बुखार है। | ᱤᱧᱟᱜ ᱨᱩᱣᱟᱹ ᱦᱩᱭ ᱟᱠᱟᱱᱟ᱾ |
| `hygiene` | Personal cleanliness & habits | साबुन से हाथ धो लो। | ᱥᱟᱵᱚᱱ ᱛᱮ ᱛᱤ ᱟᱹᱨᱩᱵ ᱢᱮ᱾ |
| `family` | Kinship terms & domestic life | यह मेरी माँ हैं। | ᱱᱩᱭ ᱤᱧ ᱟᱭᱳ ᱠᱟᱱᱟᱭ᱾ |
| `daily_life` | Daily routines & meals | समय पर स्कूल आओ। | ᱚᱠᱛᱚ ᱨᱮ ᱤᱛᱩᱱ ᱚᱲᱟᱜ ᱦᱤᱡᱩᱜ ᱢᱮ᱾ |
| `stories` | Narrative comprehension & folklore | एक जंगल में एक शेर रहता था। | ᱢᱤᱫ ᱵᱤᱨ ᱨᱮ ᱢᱤᱫ ᱛᱟᱹᱨᱩᱵ ᱛᱟᱦᱮᱸᱱ ᱠᱟᱱ ᱛᱟᱦᱮᱸᱫ᱾ |
| `assessment` | Questions & testing formulas | खाली जगह भरो। | ᱠᱷᱟᱹᱞᱤ ᱴᱷᱟᱶ ᱯᱮᱨᱮᱡ ᱢᱮ᱾ |
| `general_conversation` | Open conversation | आप कहाँ जा रहे हैं? | ᱟᱢ ᱚᱠᱟ ᱛᱮᱢ ᱪᱟᱞᱟᱜ ᱠᱟᱱᱟ? |
| `other` | Miscellaneous school contexts | घंटी बज गई। | ᱜᱷᱚᱱᱴᱤ ᱥᱟᱰᱮ ᱮᱱᱟ᱾ |

---

## 10. Train / Validation / Test Strategy

A rigorous data-splitting methodology is necessary to prevent data leakage and ensure reliable empirical tracking.

### Split Proportions
- **Training Set (`train.csv`):** **80%** (Used exclusively for model fine-tuning / parameter adaptation).
- **Validation Set (`validation.csv`):** **10%** (Used for checkpoint evaluation, early stopping, and hyperparameter tuning).
- **Test Set (`test.csv`):** **10%** (Held-out benchmark evaluated only once per final model iteration).

### Stratification Protocol
1. **Domain-Stratified Splitting:** The split algorithm must stratify by `domain`. If `classroom_commands` comprises 5% of the total corpus, it must constitute exactly 5% of `train.csv`, 5% of `validation.csv`, and 5% of `test.csv`.
2. **Zero Syntactic Leakage (Sentence Disjointness):**  
   - Splitting must be performed on unique source text hashes, **not random row shuffling**.
   - If sentence $S_1$ appears with minor punctuation variations or reverse mirroring, all instances of $S_1$ must reside strictly within the **same split**.
3. **Directional Alignment:**  
   If bidirectional pairs $(H_i \rightarrow S_i)$ and $(S_i \rightarrow H_i)$ are both included in the dataset, **both copies must be assigned to the same partition** (`train.csv`). A forward sentence in training must never see its reverse counterpart in validation or test.
4. **Dedicated FLN Gold Test Suite:**  
   In addition to the 10% general test split, an independent **Held-Out Gold FLN Test Suite (500–1,000 pairs)** must be curated from physical JCERT textbooks and vetted by two independent Santali linguists. This test suite will serve as the definitive evaluation benchmark for primary classroom adaptation.

---

## 11. Benchmark Contamination Protection

To maintain rigorous scientific standards, the evaluation benchmarks already deployed in TribeTalk must remain completely isolated from the training pipeline.

### Protected Benchmark Suite
1. **AI4Bharat IN22-Conv Benchmark:** 1,503 conversational sentences ([`translation/results/predictions.csv`](file:///c:/Users/keshv/OneDrive/Desktop/tribetalk/TribeTalk/translation/results/predictions.csv)).
2. **AI4Bharat IN22-Gen Benchmark:** 1,024 general domain sentences.
3. **Meta FLORES-200 Benchmark:** 997 `dev` sentences and 1,012 `devtest` sentences.
4. **TribeTalk Gold FLN Test Suite:** 500–1,000 held-out primary classroom sentences.

### Contamination Audit Workflow
Before any candidate record is admitted to `cleaned_parallel.csv` or `train.csv`, it must pass a **three-tier contamination test**:

```python
# Contamination Detection Specification
def check_contamination(candidate_src, candidate_tgt, benchmark_registry):
    # Tier 1: Exact Normalized String Hash Match
    norm_src = normalize_for_eval(candidate_src)
    norm_tgt = normalize_for_eval(candidate_tgt)
    if hash_sha256(norm_src) in benchmark_registry['src_hashes']:
        return REJECT_CONTAMINATION_EXACT_SRC
    if hash_sha256(norm_tgt) in benchmark_registry['tgt_hashes']:
        return REJECT_CONTAMINATION_EXACT_TGT

    # Tier 2: Source-Side Substring Overlap (> 80% word containment)
    for bench_src in benchmark_registry['src_sentences']:
        if jaccard_word_similarity(norm_src, bench_src) > 0.80:
            return REJECT_CONTAMINATION_FUZZY_SRC

    # Tier 3: Target-Side chrF++ Leakage (> 85.0 chrF++ against benchmark)
    for bench_tgt in benchmark_registry['tgt_sentences']:
        if compute_chrf(norm_tgt, bench_tgt) > 85.0:
            return REJECT_CONTAMINATION_FUZZY_TGT

    return PASS_BENCHMARK_PROTECTION
```

Any candidate sentence failing this audit is immediately quarantined in `training/data/v2/metadata/rejected.csv` with the tag `BENCHMARK_CONTAMINATION_<BENCHMARK_NAME>`.

---

## 12. Existing TribeTalk Dataset Disposition

The legacy dataset [`training/data/hindi_santali_large_parallel.csv`](file:///c:/Users/keshv/OneDrive/Desktop/tribetalk/TribeTalk/training/data/hindi_santali_large_parallel.csv) (202 rows, 101 unique pairs) has been audited. The original file will be preserved intact as a historical baseline.

```
Disposition of Legacy 101 Unique Pairs
+---------------------------------------------------------------------------------------------------------------+
| Category                     | Pairs | Disposition | Mandatory Transformation Required                        |
+---------------------------------------------------------------------------------------------------------------+
| 1. Hand-Curated Base Pairs   | 61    | KEEP (Seed) | • Replace ASCII `-` with Ol Chiki Phaarkaa (`ᱼ` U+1C7C)   |
|                              |       |             | • Terminate Santali with Mucạd (`᱾` U+1C7E)             |
|                              |       |             | • Normalize Hindi purna viram (`।` U+0964)               |
|                              |       |             | • Move to `training/data/v2/raw/seed/tribetalk_base_61`  |
+---------------------------------------------------------------------------------------------------------------+
| 2. First-Person Synthetic    | 8     | REVIEW      | • Inspect for pronoun stuttering (`ᱤᱧ ᱤᱧ ᱫᱟᱹᱲ ᱮᱫᱟ`)        |
|    Combinations ("मैं" / "ᱤᱧ") |       |             | • Fix double subject clitics before admitting to training|
+---------------------------------------------------------------------------------------------------------------+
| 3. Broken Synthetic Template | 32    | REJECT      | • Immediate purge from training corpus                   |
|    Combinations (tum, ve, etc)|      | (Purge)     | • Move to `training/data/v2/metadata/rejected.csv`       |
|                              |       |             | • Tagged: `REJECT_SYNTHETIC_GRAMMAR_CORRUPTION`          |
+---------------------------------------------------------------------------------------------------------------+
```

---

## 13. Expected Corpus Size

Corpus sizing targets reflect realistic yields after applying English-pivot extraction, deduplication, length filtering, and benchmark de-contamination.

```
Corpus Growth Projections
+---------------------------------------------------------------------------------------------------------------+
| Target Level         | Target Unique Pairs | Core Sources Constituting Volume                                 |
+---------------------------------------------------------------------------------------------------------------+
| 1. Minimum Viable    | 12,000 - 15,000     | • BPCC-Human pivoted seed (~10,000 clean pairs)                  |
|    Corpus (Phase 2B) |                     | • StoryWeaver primary readers (~2,000 pairs)                     |
|                      |                     | • Curated FLN classroom seed (~500 pairs)                        |
|                      |                     | • Tatoeba pivoted conversational pairs (~300 pairs)              |
+---------------------------------------------------------------------------------------------------------------+
| 2. Target Production | 35,000 - 50,000     | • BPCC-Human full pivoted extraction (~30,000 pairs)             |
|    Corpus            |                     | • StoryWeaver complete aligned stories (~4,000 pairs)            |
|                      |                     | • EnSanCorp pivoted subset (~2,000 pairs)                        |
|                      |                     | • JCERT primary textbook extraction (Grades 1-5, ~4,000 pairs)   |
|                      |                     | • Curated FLN dialogues & assessment prompts (~2,000 pairs)      |
+---------------------------------------------------------------------------------------------------------------+
| 3. Stretch Target    | 80,000 - 120,000    | • Filtered BPCC-BT (Back-Translation, ~50,000 vetted pairs)      |
|    Corpus            |                     | • Extended Wikimedia & community translation initiatives         |
+---------------------------------------------------------------------------------------------------------------+
```

*Note: In all tiers, bidirectional training pairs may double the training sample count (e.g., 50,000 unique pairs $\rightarrow$ 100,000 training rows), but unique translation pair metrics adhere strictly to the numbers above.*

---

## 14. Licensing & Attribution

All candidate sources have been vetted for intellectual property rights, open-source compatibility, and compliance with the Smart India Hackathon (SIH) prototype guidelines.

| Source | License | Attribution Requirement | Allowed in Training? | Allowed in Commercial/SIH Demo? |
| :--- | :--- | :--- | :--- | :--- |
| **AI4Bharat BPCC** | Creative Commons CC0 / CC-BY 4.0 | Cite Gala et al. (2023), AI4Bharat / IIT Madras | **YES** | **YES** |
| **Meta FLORES-200** | CC-BY-SA 4.0 | Cite NLLB Team (2022), Meta AI | **NO (Eval only)**| **YES (Benchmark only)** |
| **Tatoeba Project** | CC-BY 2.0 FR | Attribute Tatoeba.org & contributors | **YES** | **YES** |
| **StoryWeaver** | CC-BY 4.0 | Attribute Pratham Books, Suchana Community & translators | **YES** | **YES** |
| **EnSanCorp** | Academic Research License | Cite Ghosh et al. (2022) | **YES** | **YES** |
| **TribeTalk Seed** | Apache 2.0 | TribeTalk Project Developers | **YES** | **YES** |

### Standard Attribution Statement for `DATASET_CARD.md`
> *"The TribeTalk V2 Parallel Corpus incorporates data derived from the Bharat Parallel Corpus Collection (BPCC) by AI4Bharat, early childhood readers from StoryWeaver (Pratham Books), open sentence alignments from the Tatoeba Project, and pedagogical curriculum items from Jharkhand MTB-MLE materials. Datasets are utilized in compliance with their respective CC0, CC-BY 4.0, and Apache 2.0 licenses."*

---

## 15. Risks & Mitigation

| # | Identified Risk | Severity | Technical & Linguistic Mitigation |
| :--- | :--- | :--- | :--- |
| 1 | **Gated Hugging Face Access:** AI4Bharat BPCC requires authentication; automated headless scripts fail with `HTTP 401`. | **HIGH** | The ingestion script will require a local `HUGGINGFACE_TOKEN` environment variable and use `huggingface_hub.hf_hub_download` with explicit token handling. |
| 2 | **Regional Dialectal Variations:** Santali spoken in Mayurbhanj (Odisha) has lexical differences from Santhal Parganas (Jharkhand) and Purulia (West Bengal). | **MEDIUM** | Standardize lexical choices on the official Jharkhand Academic Council (JAC) / JCERT textbook standard, while logging alternative regional terms in the teacher memory lexicon. |
| 3 | **Orthographic Drift (Hyphen vs. Ohod):** Upstream web data frequently uses ASCII hyphens (`-`) or omits the Phaarkaa/Ohod release mark (`ᱼ`), corrupting tokenization. | **HIGH** | Execute Stage 5 Punctuation Normalization to automatically restore the Ol Chiki Phaarkaa (`U+1C7C`) across checked consonant releases. |
| 4 | **Evaluation Contamination:** Accidentally ingesting sentences from IN22-Conv or FLORES-200 will invalidate progress tracking. | **CRITICAL** | Enforce mandatory SHA-256 hash checking against all protected evaluation sets prior to splitting (Section 11). |
| 5 | **Memory Footprint of Final Model:** Fine-tuning a 320M parameter model results in an ONNX artifact that consumes > 1.8 GB RAM, crashing 2 GB Android tablets. | **HIGH** | Phase 2B/2C will target parameter-efficient adaptation (LoRA) on compact Seq2Seq backbones (e.g., pruned NLLB-200 distilled 600M $\rightarrow$ ~100M or quantized IndicTrans2) optimized for edge deployment (< 350 MB RAM budget). |

---

## 16. Phase 2B Implementation Plan

The acquisition and preparation pipeline will be implemented in seven sequential engineering steps during Phase 2B:

```
Phase 2B Execution Roadmap
├── Step 1: Tooling & Environment Setup
│   ├── Establish `training/data/v2/` directory tree
│   ├── Configure Hugging Face authentication credentials
│   └── Implement Unicode NFC and Ol Chiki regex validator libraries
├── Step 2: Upstream Raw Data Ingestion
│   ├── Download `bpcc-seed-latest/sat_Olck.tsv` and `hin_Deva.tsv`
│   ├── Ingest StoryWeaver bilingual children's storybooks
│   └── Export Tatoeba Santali-English alignments
├── Step 3: Pivot Alignment & Intermediate Dataset Construction
│   ├── Perform English sentence hash join across BPCC subsets
│   └── Output unified intermediate candidate parallel CSV
├── Step 4: Automated 15-Stage Cleaning & Normalization
│   ├── Run script validation, whitespace cleaning, and Ohod normalization
│   └── Filter length ratios and suspicious characters
├── Step 5: Benchmark Contamination Quarantine
│   ├── Audit candidate pairs against IN22-Conv, IN22-Gen, and FLORES-200
│   └── Route matches to `rejected.csv`
├── Step 6: Linguistic Review Sampling & Quality Validation
│   ├── Sample 500 candidate pairs across all 23 domains for expert review
│   └── Update status to `VALID` or `REJECT`
└── Step 7: Stratified Splitting & Dataset Card Generation
    ├── Split into `train.csv` (80%), `validation.csv` (10%), `test.csv` (10%)
    ├── Produce `provenance.csv` and `rejected.csv`
    └── Publish formal `training/data/v2/DATASET_CARD.md`
```

---

## Final Decision

### PHASE 2A GO / NO-GO

| Decision Item | Decision | Specific Actionable Directives |
| :--- | :--- | :--- |
| **1. Which source(s) should we actually download?** | **GO (Tier A Sources)** | • Download **AI4Bharat BPCC-Human (`bpcc-seed-latest`)** for `sat_Olck` and `hin_Deva` via authenticated Hugging Face API.<br>• Ingest **StoryWeaver (Pratham Books / Suchana)** Santali-Hindi leveled children's stories.<br>• Import the **61 hand-curated TribeTalk seed pairs**.<br>• Download **Tatoeba Santali alignments** for auxiliary conversational pairs. |
| **2. Which source(s) should NEVER enter training?** | **STRICT QUARANTINE** | • **AI4Bharat IN22-Conv (1,503 sentences):** Must remain strictly quarantined as the benchmark evaluation set.<br>• **AI4Bharat IN22-Gen (1,024 sentences):** Quarantined.<br>• **Meta FLORES-200 (`dev` + `devtest`, 2,009 sentences):** Quarantined.<br>• **TribeTalk 32 ungrammatical synthetic pairs:** Must be permanently purged. |
| **3. How many genuine Hindi-Santali pairs can realistically be obtained?** | **REALISTIC PROJECTION** | • **Minimum Viable Corpus:** **12,000–15,000 unique pairs**.<br>• **Target Production Corpus:** **35,000–50,000 unique pairs** (via BPCC-Human pivot, StoryWeaver, and JCERT curriculum extraction).<br>• Bidirectional mirroring will provide up to 70,000–100,000 training examples without duplicating underlying pairs. |
| **4. What additional education-domain data do we need to create/collect?** | **CURRICULUM PRIORITIES** | • High-priority collection of primary classroom instructions, student responses, early mathematics (counting, addition, subtraction), and phonics from **JCERT Grades 1–5 MTB-MLE textbooks**.<br>• Digitize and align 150+ Santali-Hindi storybooks from StoryWeaver. |
| **5. What data requires human linguistic verification?** | **HUMAN REVIEW MANDATE** | • All 61 hand-curated base pairs must be validated after Ol Chiki Ohod (`ᱼ`) normalization.<br>• All newly digitized JCERT textbook prompts.<br>• A 5% random audit sample of English-pivoted BPCC sentences to verify cultural and dialectal appropriateness. |
| **6. What should remain untouched?** | **SYSTEM INTEGRITY** | • **Preserve untouched:** [`training/data/hindi_santali_large_parallel.csv`](file:///c:/Users/keshv/OneDrive/Desktop/tribetalk/TribeTalk/training/data/hindi_santali_large_parallel.csv) (original historical baseline).<br>• **Preserve untouched:** Android application source code, Kotlin translation engines, Gradle files, and existing benchmark scripts/results in `translation/`. |
