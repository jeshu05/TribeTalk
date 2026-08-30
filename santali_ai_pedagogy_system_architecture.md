## Al-Powered Vernacular Pedagogy & Real-Time Translation Architecture: Santali (Ol Chiki)

## 1. Linguistic Foundations: Santali Language & Orthography

## 1.1 Language Profile

- « Linguistic Classification: Kherwarian Munda language belonging to the Austroasiatic language family.

- « Speaker Demographics: Approximately 7.6 million speakers in India (2011 Census), with significant diaspora populations across Bangladesh, Nepal, and Bhutan.

- Constitutional & Regional Status: Recognized as one of India’s 22 Scheduled Languages (Eighth Schedule); holds official language status in the states of Jharkhand and West Bengal.

## 1.2 Linguistics Concept: Script vs. Language

[INOTE] Core Principle: Language is what is spoken (phonology, vocabulary, grammar). Script is the visual writing system (symbols/glyphs) used to encode the spoken language on a physical or digital medium

## Comparative Case Study (Tamil Analogy):

- « Language: Spoken Tamil (grammar, acoustic sounds, vocabulary).

- «Script: Tamil writing system (12 vowel signs + 18 consonant signs; an abugida with inherent /a /).

|   | Linguistic Axiom | Demonstration |   |   |   |   |   |   | Practical Implication |   |   |   |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
|   | One language — |   |   |   | Tamil: Spoken word can be written in Tamil script |   |   |   |   |   | Santali can be written in OI Chiki, Devanagari, Latin, or |   |
|   | Multiple scripts | ( |   |   | ) or Latin script ( Vanakkan ). |   |   |   |   |   | Bengali script while preserving identical spoken semantics. |   |
|   | One script — |   |   |   | Tamil script is used to write Badaga, Toda, and |   |   |   |   |   | Devanagari is used to write Hind, Marathi, Sanskrit, and |   |
|   |   | Multiple languages ~~ Kurumba. |   |   |   |   |   |   |   | phonetically transliterated Santali. |   |   |
|   |   |   |   |   |   |   |   | An NLP |   |   |   | engine can decouple acoustic phonetics from |
|   | Script # Language |   |   |   | Same spoken language, different visual encoding. |   |   |   |   |   |   | via |
|   |   |   |   |   |   |   |   |   |   |   |   | layers. |

One-Liner Rule: Language is what you speak; Script is the set of symbols you use to write it.

## 1.3 Morphology, Syntax & Phonological Nuances

- . reduplication) onto a root stem. nati Words are by stringing discrete morphemes (prefixes, suffixes, infixes, and morphological

- Grammatical Nuances: Nouns are explicitly marked for:

- © Number: Singular, Dual, and Plural.

- © Animacy Class: Animate vs. Inanimate.

- o Case Marking: 7 distinct grammatical cases.

- Syntactic Order: Strict SOV (Subject-Object-Verb) basic word order with a fluid/weak noun-verb categorical distinction.

- Checked Consonants & Glottal Stops: Unique unreleased consonants and glottal phonemes (, d', requiring dedicated phonetic modeling in speech synthesis (TTS)

## 1.4 Writing System: Ol Chiki

- « Origins: Invented in 1925 by Pandit Raghunath Murmu.

- « Typology: A true alphabet (30 distinct | Devanagari). dividual vowels and with glyphs, distinct from abugidas like

- « Official Adoption: Official script for Santali in Jharkhand, West Bengal, and Odisha.


- \* Unicode Standard: Allocated under Unicode range to U+1C7F

## 2. Core Engineering Architecture & Targeted Technical Novelties

## Module 1: Low-Latency Offline Voice Pipeline

## Implementation

- « Speech-to-Text (ASR): Chains Vosk-Hindi (or INT8-quantized Whisper-Tiny) for rapid teacher transcription.

- \* Neural Machine Translation (NMT): Executes a fine-tuned, pruned NLLB-200 micro model exported to ONNX format.

- « Speech Synthesis (TTS): Synthesizes natural Santali speech using Piper-TTS (low-footprint VITS model) running via native C++ Android NDK bindings.

Aludio C+

Teacher Spoken Hindi

- \# Targeted Architectural Novelty: Teacher Assist Heads-Up Display (HUD)

- « Streaming VAD Execution: Voice Activity Detection (VAD) segments audio into sliding 500ms buffers, triggering downstream translation immediately rather than waiting for extended sentence pauses

- Fail-Safe Dual-Screen Output: The interface simultaneously projects OI Chiki on the student-facing display and Phonetic Devanagari with IPA Stress Marks on the teacher HUD.

- « Acoustic Noise Resilience: In high-noise rural classrooms where tablet speaker output may be drowned out or muffled, the non-native di teacher can glance at the HUD and pronounce the Santali sentence accurately and with correct tonality.

Tokens

Ol Chiki

INT8 Vosk / Whisper ASR —————— Pruned NLLB-200 (ONNX)

|

VAD 500ms Window

## Module 2: Dynamic NIPUN Curriculum & Worksheet Generator

## Implementation

- « Modular FLN Schema: Stores structured NIPUN Bharat Foundational Literacy and Numeracy (FLN) competency templates in local JSON schemas.

- « Native Vector Canvas Compiler: Directly compiles printable worksheets u g Android's native android. graphics. pdf.Pdfdocunent API with embedded Noto sans 01 chiki TrueType fonts ( .ttf ), entirely on-device without external rendering engines.

- Targeted Architectural Novelty: Cultural Ontology Mapping & Stroke Trace Guides

- Automated Substitution eplaces urban, non-native Hindi textbook examples with culturally resonant Santali objects (e.g. automatically replacing urban items with local flora, fauna, tools, and traditional Sohrai art motifs in word problems and math puzzles).

- Auto-Generated OI Chiki Trace Paths: Renders parametric stroke-order tracing paths and directional arrows on foundational worksheets, enabling Grade 1-3 to develop native OI Chiki letter mechanics intuitively.

## Module 3: 2 GB RAM Edge Runtime Layer

## Implementation

- « Zero-Copy Memory Mapping ( map ): Reads quantized model weights directly from internal flash storage via POSIX mmap() through the Android NDK, eliminating Java heap allocation overhead.

- « Strict Memory Envelope: Limits total application resident memory to < 500 MB, safeguarding against Android Low Memory Killer (LMK) eviction.


|   | Subsystem Component |   |   |   |   |   |   |   |   | Technical Implementation Strategy |   |   |   |   |   |   | Peak RAM Allocation |   |   |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | ~ 120 MB |   |   |
|   | Speech-to-Text (ASR) |   |   |   |   |   |   |   |   | 8-bit quantized Vosk / Whisper-Tiny native C++ binary |   |   |   |   |   |   |   |   |   |
|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | ~~ 250 MB |   |   |
|   | Translation Engine (NMT) Speech Synthesis (TTS) |   |   |   |   |   |   |   |   |   | Pruned & quantized NLLB-200 micro variant via ONNX Runtime Mobile Piper-TTS (VITS ONNX architecture) with Santali acoustic profile |   |   |   |   |   | ~ 80 MB |   |   |
|   |   | Context Pipeline Overhead |   |   |   |   |   |   |   |   | Sequential execution swapping via OS mnap() file mapping |   |   |   |   |   | ~ 40 MB |   |   |
|   | Total Working Memory |   |   |   |   |   |   |   | Target Constraint: < 500 MB |   |   |   |   |   |   |   | < 490 MB |   |   |

## Targeted Architectural Novelty: Sequential Tensor Swapper & P2P Mesh Sync

- Sequential Tensor Swapper: state-driven Low-end tablets coordinator crash that when ASR, NMT, loads and and TTS unloads models model reside weights in active into RAM a unified concurrently. pre-allocated The system memory slab a memory across the conversational sequence (Audio — ASR — [Swap] — NMT — [Swap] — TTS)

- to cellular data at a block cluster center, it downloads updated curriculum bundles and automatically broadcasts them to nearby tablets in zero-connectivity village schools without requiring internet access or routers. Offline Wi-Fi Direct P2P Sync Mesh: Implements an ad-hoc local mesh using . When a single teacher tablet connects

## 3. Comprehensive Technical Glossary & Conceptual Mastery

## 3.1 Linguistic & Santali Concepts

- OI Chiki Script: The dedicated 30-character alphabetical script designed specifically for Santali phonology ( )

- Agglutination: Linguistic process where complex words are assembled by stringing multiple morphemes to invariant root stems.

- Checked Consonants / Glottal Stops: Acoustic stops (€, g) characterized by an unreleased closure of the vocal tract.

- ph Ph (G2P): Algorithmic mapping rule set converting written Ol Chiki glyphs into phonetic notations for synthesis.

- Dual-Script Transliteration: Systematic conversion of Santali phonetics into Devanagari script for non-native Hindi teachers.

- Low-Resource Language (LRL): Languages with scarce parallel text corpora, minimal annotated speech audio, and missing digital NLP tooling.

## 3.2 NLP & Machine Translation Engine

- Subword izati vocabulary explosion. morphology without iece / BPE): De text into statistically recurrent subword fragments to handle agglutinative

- Pivot constrained. ion: Using an high: e language bridge (Hindi — Pivot — Santali) when direct parallel corpora are

- NLLB-200 (No Language Left Behind): Open-source multilingual translation model supporting Santali under the sat_olck language tag

- LoRA (Low-Rank Adaptation): Parameter-efficient fine-tuning technique freezing base weights and training low-rank decomposition matrices for target domain adaptation.

- chrF / BLEU Evaluation: Metric standards for translation quality; chrF (character n-gram F-score) is the gold standard for agglutinative languages.

## 3.3 Speech Al: Acoustic & Synthesis Systems

- Voice Activity Detection (VAD): Energy- and entropy-based silence detection segmenting audio streams to trigger real-time inference.

- INT8 Quantization: Cy accelerating CPU integer arithmetic. ing 32-bit floating-point weights to 8-bit signed integers, achieving a ~ 75 reduction in model size and

- Acoustic & Language Models (AM / LM): ASR modules where the AM decodes raw spectral waveforms into phonemes and the LM decodes phoneme chains into likely word sequences.

- VITS / Piper-TTS: Fully parallel end-to-end neural text-to-speech processors. capable of real-time audio generation on low-end ARM


## 3.4 Edge Computing & Android Opti

- \+ ONNX Runtime Mobile & TFLite: High-efficiency runtime execution engines optimized for mobile NPU/CPU instructions (ARM NEON),

- mmap() (Memory Mapping): POSIX system call mapping model binaries directly from storage into virtual memory address space, avoiding heap memory duplication.

- Android NDK & JNI: Native Development Kit (C/C++) interfacing via Java Native Interface for bare-metal computation speeds.

- AAudio API: Low-latency, high-performance native audio streaming interface in Android 8.0+.

- Wi-Fi Direct P2P Mesh: Local wireless protocol facilitating decentralized, router-free curriculum synchronization across offline tablets.

## 3.5 Pedagogical Frameworks

- \* MTB-MLE: Mother Tc B il ducati al standard mandated by the Government of Jharkhand.

- FLN: Foundational Literacy and Numeracy—Grade 1-3 targets for basic ion and basic i

- NIPUN Bharat: National Initiative for Proficiency in Reading with Understanding and Numeracy—national curriculum target matrix

## 4. Audio & Voice Dataset Specifications

## 4.1 Automatic Speech Recognition (ASR) Datasets

| Direction Santali — Text |   | Al4Bharat | Dataset Source IndicVoices | split) | Size / Duration Target Model Architecture ~ 120 Hours (Santali INT8 Quantized Whisper-Tiny ~~ Transcribes student spoken Santali / IndicConformer-CTC | Role in Real-Time Pipeline into OI Chiki text streaming chunks. |
| --- | --- | --- | --- | --- | --- | --- |
| Santali — Text |   | Voice > | Mozilla Common |   | ~ 30 + Hours Acoustic model adaptation (Crowdsourced) checkpoint | Captures diverse phonetic accents variations. primary speech and school child |
|   |   | Kathbath |   |   |   |   |
| Hindi — |   |   |   |   | 1,000 + Hours INT8 Vosk-Hindi / Whisper- | Transcribes teacher Hindi instructions |
| Text |   |   | r: |   | Conversational Hindi Micro | with < 500 ms processing latency. |
|   |   | Nirantor |   |   |   |   |

## 4.2 Text-to-Speech (TTS) Datasets

| Direction | Dataset Source |   |   |   |   |   | Format & Audio Specs |   |   |   | Target Model Architecture |   |   | Role in Real-Time Pipeline |   |   |   |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Text — |   |   |   |   |   |   | 52.8 Hours (Studio |   |   |   | Piper-TTS / VITS |   |   |   |   | Synthesizes natural Santali speech output |   |
| Santali Voice |   |   |   |   |   |   |   |   |   | ONNX |   |   |   |   | from translated lesson instructions. |   |   |
| (Santali) |   |   |   |   |   |   | recorded, 48 kHz Mono WAV) |   |   |   |   |   |   |   |   |   |   |
| Text — Hindi Voice (Hindi) | Al4Bharat Rasa |   |   |   |   |   | ~ 50.8 Hours (Studio recorded, 48 kHz Mono WAV) |   |   |   | Sherpa-ONNX / Piper-Hindi |   |   | into Hindi for the teacher. |   | Synthesizes translated student responses |   |


## 5. Verification & Validation Metrics

End-to-End Latency: = 500ms) + (< 600ms) + (< 800ms) = 1.95 < 3.0

- « Translation Quality Threshold: > 55.0 / BLEU > 28.0 on FLN pedagogical test suites

- Edge Stability: Peak Resident Set Size (RSS) < 490 MB under continuous 30-minute streaming translation on Android 9.0 (2 GB RAM tablet)
