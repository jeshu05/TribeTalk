#!/usr/bin/env python3
"""
TribeTalk V2 - Phase 2B Automated Corpus Cleaning & Normalization Pipeline
Location: training/scripts/clean_v2_corpus.py

Implements 15-stage automated quality filtering, benchmark quarantine,
Ol Chiki orthography checks, domain tagging, and deduplication.
"""

import os
import sys
import csv
import re
import json
import unicodedata
import hashlib
from collections import Counter

sys.stdout.reconfigure(encoding='utf-8')

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA_V2_DIR = os.path.join(BASE_DIR, "data", "v2")
INTERMEDIATE_DIR = os.path.join(DATA_V2_DIR, "intermediate")
CLEANED_DIR = os.path.join(DATA_V2_DIR, "cleaned")
VERIFIED_DIR = os.path.join(DATA_V2_DIR, "verified")
METADATA_DIR = os.path.join(DATA_V2_DIR, "metadata")

for d in [CLEANED_DIR, VERIFIED_DIR, METADATA_DIR]:
    os.makedirs(d, exist_ok=True)

# ----------------------------------------------------------------------
# 1. Benchmark Reference Loading (for Contamination Quarantine)
# ----------------------------------------------------------------------
IN22_PREDICTIONS_PATH = os.path.join(BASE_DIR, "..", "translation", "results", "predictions.csv")

benchmark_hindi_hashes = set()
benchmark_santali_hashes = set()
benchmark_hindi_sentences = []
benchmark_hindi_word_sets = []
benchmark_santali_sentences = []

if os.path.exists(IN22_PREDICTIONS_PATH):
    with open(IN22_PREDICTIONS_PATH, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            hi = unicodedata.normalize("NFC", row.get("hindi", "").strip())
            sat = unicodedata.normalize("NFC", row.get("reference_santhali", "").strip())
            if hi:
                h_hash = hashlib.sha256(hi.encode('utf-8')).hexdigest()
                benchmark_hindi_hashes.add(h_hash)
                benchmark_hindi_sentences.append(hi)
                benchmark_hindi_word_sets.append(set(re.findall(r'\w+', hi.lower())))
            if sat:
                s_hash = hashlib.sha256(sat.encode('utf-8')).hexdigest()
                benchmark_santali_hashes.add(s_hash)
                benchmark_santali_sentences.append(sat)

print(f"[*] Loaded {len(benchmark_hindi_hashes)} protected IN22-Conv benchmark pairs for contamination filtering.")

# Known severely ungrammatical synthetic combinations from Phase 1 audit
KNOWN_SYNTHETIC_CORRUPTIONS = {
    "तुम किताब पढ़ता हूँ", "तुम स्कूल जाता हूँ", "तुम गाना गाता हूँ", "तुम खाना खाता हूँ",
    "तुम पानी पीता हूँ", "तुम काम करता हूँ", "तुम चित्र बनाता हूँ", "तुम दौड़ता हूँ",
    "हम किताब पढ़ता हूँ", "हम स्कूल जाता हूँ", "हम गाना गाता हूँ", "हम खाना खाता हूँ",
    "हम पानी पीता हूँ", "हम काम करता हूँ", "हम चित्र बनाता हूँ", "हम दौड़ता हूँ",
    "वह किताब पढ़ता हूँ", "वह स्कूल जाता हूँ", "वह गाना गाता हूँ", "वह खाना खाता हूँ",
    "वह पानी पीता हूँ", "वह काम करता हूँ", "वह चित्र बनाता हूँ", "वह दौड़ता हूँ",
    "वे किताब पढ़ता हूँ", "वे स्कूल जाता हूँ", "वे गाना गाता हूँ", "वे खाना खाता हूँ",
    "वे पानी पीता हूँ", "वे काम करता हूँ", "वे चित्र बनाता हूँ", "वे दौड़ता हूँ"
}

# ----------------------------------------------------------------------
# 2. Domain Taxonomy & Rule-Based Classifier (Phase 2A Taxonomy)
# ----------------------------------------------------------------------
DOMAINS = [
    "classroom_commands", "teacher_student_dialogue", "greetings", "literacy",
    "phonics", "reading", "writing", "numeracy", "counting", "addition",
    "subtraction", "shapes", "measurement", "environment", "nature", "health",
    "hygiene", "family", "daily_life", "stories", "assessment",
    "general_conversation", "other"
]

DOMAIN_KEYWORDS = {
    "classroom_commands": [
        "खोलो", "बंद करो", "बैठो", "बैठ", "खड़े हो", "खड़े", "शांत", "देखो", "सुनो",
        "आओ", "जाओ", "चलो", "ᱨᱚᱲ", "ᱫᱩᱲᱩᱵ", "ᱛᱤᱸᱜᱩᱱ", "ᱡᱷᱤᱡᱽ", "ᱵᱚᱸᱫᱽ", "ᱟᱧᱡᱚᱢ", "ᱧᱮᱞ", "ᱛᱷᱤᱨ"
    ],
    "teacher_student_dialogue": [
        "समझ नहीं", "समझाइए", "मदद", "पूछ", "बताइए", "सकते हैं", "गुरुजी",
        "ᱢᱟᱪᱮᱛ", "ᱵᱩᱡᱷᱟᱹᱣ", "ᱞᱟᱹᱭ", "ᱜᱚᱲᱚ", "ᱠᱩᱞᱤ"
    ],
    "greetings": [
        "नमस्ते", "धन्यवाद", "अलविदा", "प्रणाम", "स्वागत", "सुप्रभात", "कैसे हैं",
        "ᱡᱚᱦᱟᱨ", "ᱥᱟᱨᱦᱟᱣ", "ᱪᱮᱠᱟ ᱢᱮᱱᱟᱢᱟ", "ᱱᱟᱯᱟᱭ"
    ],
    "numeracy": ["गिनती", "कितने", "संख्या", "अंक", "ಲೆᱠᱷᱟ", "ᱛᱤᱱᱟᱹᱜ", "ᱮᱞ"],
    "counting": [
        "एक", "दो", "तीन", "चार", "पाँच", "पांच", "छह", "सात", "आठ", "नौ", "दस",
        "ᱢᱤᱫ", "ᱵᱟᱨ", "ᱯᱮ", "ᱯᱩᱱ", "ᱢᱚᱬᱮ", "ᱛᱩᱨᱩᱭ", "ᱮᱭᱟᱭ", "ᱤᱨᱟᱹᱞ", "ᱟᱨᱮ", "ᱜᱮᱞ"
    ],
    "addition": ["जोड़ो", "जोड़", "मिलाकर", "होते हैं", "ᱢᱮᱥᱟ", "ᱡᱚᱲᱟᱣ"],
    "subtraction": ["घटाओ", "घटा", "कम", "बाकी", "ᱚᱪᱚᱜ"],
    "shapes": ["गोल", "चौकोर", "त्रिकोण", "आकार", "ᱞᱤᱸᱜᱤᱱ", "ᱪᱚᱣᱠᱟ"],
    "measurement": ["बड़ा", "छोटा", "लंबा", "ऊँचा", "भारी", "हल्का", "ᱢᱟᱨᱟᱝ", "ᱦᱩᱰᱤᱧ", "ᱡᱤᱞᱤᱧ", "ᱩᱥᱩᱞ"],
    "literacy": ["किताब", "पाठ", "वर्णमाला", "अक्षर", "शब्द", "ᱯᱩᱛᱷᱤ", "ᱯᱟᱴᱷ", "ᱚᱠᱷᱚᱨ", "ᱟᱹᱲᱟᱹ"],
    "reading": ["पढ़ो", "पढ़", "पढ़ना", "पढ़ते", "वाचन", "ᱯᱟᱲᱦᱟᱣ"],
    "writing": ["लिखो", "लिख", "लिखना", "स्लेट", "कॉपी", "कलम", "ᱚᱞ", "ᱠᱚᱞᱚᱢ"],
    "phonics": ["ध्वनि", "आवाज", "मात्रा", "उच्चारण", "ᱥᱟᱰᱮ"],
    "environment": ["बारिश", "मौसम", "गर्मी", "ठंड", "धूप", "हवा", "पानी", "ᱫᱟᱜ", "ᱦᱚᱭ", "ᱞᱚᱞᱚ", "ᱨᱮᱭᱟᱲ", "ᱥᱮᱨᱢᱟ"],
    "nature": ["पेड़", "पौधा", "फूल", "पक्षी", "चिड़िया", "नदी", "सूरज", "जंगल", "ᱫᱟᱨᱮ", "ᱵᱟᱦᱟ", "ᱪᱮᱬᱮ", "ᱜᱟᰰ", "ᱪᱟᱸᱫᱚ", "ᱵᱤᱨ", "ᱥᱤᱧᱚᱛ"],
    "health": ["बीमार", "दवा", "दर्द", "बुखार", "भूख", "प्यास", "ᱨᱩᱣᱟᱹ", "ᱨᱟᱱ", "ᱦᱟᱹᱥᱩ", "ᱨᱮᱸᱜᱮᱡ", "ᱛᱮᱛᱟᱝ"],
    "hygiene": ["सफाई", "धो", "हाथ", "दांत", "नहाना", "साफ", "ᱥᱟᱯᱷᱟ", "ᱟᱹᱨᱩᱵ", "ᱛᱤ", "ᱰᱟᱴᱟ"],
    "family": ["माँ", "पिता", "भाई", "बहन", "दोस्त", "परिवार", "दादा", "दादी", "ᱟᱭᱳ", "ᱵᱟᱵᱟ", "ᱵᱚᱭᱦᱟ", "ᱢᱤᱥᱤ", "ᱜᱟᱛᱮ", "ᱜᱷᱟᱨᱚᱸᱡᱽ"],
    "daily_life": ["घर", "स्कूल", "खाना", "रोटी", "दूध", "समय", "काम", "गांव", "ᱚᱲᱟᱜ", "ᱤᱛᱩᱱ", "ᱫᱟᱠᱟ", "ᱨᱩᱴᱤ", "ᱛᱳᱣᱟ", "ᱚᱠᱛᱚ", "ᱠᱟᱹᱢᱤ", "ᱟᱹᱛᱩ"],
    "stories": ["शेर", "बाघ", "खरगोश", "कहानी", "कथा", "राजा", "ᱛᱟᱹᱨᱩᱵ", "ᱠᱩᱞᱟᱹᱭ", "ᱠᱟᱹᱦᱤᱱᱤ", "ᱨᱟᱡᱟ"],
    "assessment": ["प्रश्न", "उत्तर", "परीक्षा", "जांच", "सही", "गलत", "ᱠᱩᱠᱞᱤ", "ᱛᱮᱞᱟ", "ᱵᱤᱰᱟᱹᱣ", "ᱥᱟᱹᱨᱤ", "ᱮᱲᱮ"]
}

def classify_domain(src_text, tgt_text):
    combined_text = f"{src_text} {tgt_text}".lower()
    for domain, keywords in DOMAIN_KEYWORDS.items():
        for kw in keywords:
            if kw.lower() in combined_text:
                return domain
    return "general_conversation"

# ----------------------------------------------------------------------
# 3. Text Validation Functions
# ----------------------------------------------------------------------
def has_devanagari(text):
    return any(0x0900 <= ord(c) <= 0x097F for c in text)

def has_ol_chiki(text):
    return any(0x1C50 <= ord(c) <= 0x1C7F for c in text)

def has_suspicious_latin(text):
    # Detect ASCII/Latin words with length >= 2
    return bool(re.search(r'[A-Za-z]{2,}', text))

def normalize_whitespace(text):
    # Collapse multiple spaces, newlines, tabs, and strip outer spaces
    return re.sub(r'[\s\u00A0\u200B]+', ' ', text).strip()

def strip_control_chars(text):
    return "".join(c for c in text if ord(c) >= 32 or c in '\t\n')

def jaccard_word_sim(s1, s2):
    w1 = set(re.findall(r'\w+', s1.lower()))
    w2 = set(re.findall(r'\w+', s2.lower()))
    if not w1 or not w2:
        return 0.0
    return len(w1.intersection(w2)) / len(w1.union(w2))

def char_4gram_sim(s1, s2):
    def get_4grams(s):
        s = s.replace(" ", "")
        return set(s[i:i+4] for i in range(max(0, len(s)-3)))
    g1 = get_4grams(s1)
    g2 = get_4grams(s2)
    if not g1 or not g2:
        return 0.0
    return len(g1.intersection(g2)) / len(g1.union(g2))

# ----------------------------------------------------------------------
# 4. Main Cleaning Engine
# ----------------------------------------------------------------------
def clean_corpus():
    input_csv = os.path.join(INTERMEDIATE_DIR, "combined_raw_parallel.csv")
    if not os.path.exists(input_csv):
        print(f"[-] Input file not found: {input_csv}")
        return

    with open(input_csv, "r", encoding="utf-8") as f:
        records = list(csv.DictReader(f))

    print(f"[*] Beginning cleaning pipeline on {len(records)} candidate intermediate records...")

    valid_records = []
    review_records = []
    rejected_records = []

    seen_exact_pairs = set()
    seen_underlying_pairs = set() # forward + reverse canonical tuple
    seen_hindi_sources = {}
    contamination_log = []

    stats = Counter()

    for r in records:
        rec_id = r["id"]
        source = r["source"]
        subset = r["source_subset"]
        raw_src = r["src_text"]
        raw_tgt = r["tgt_text"]
        src_id = r.get("source_id", "")
        license_type = r.get("license", "CC-BY-4.0")

        # 1. Unicode NFC Normalization
        src = unicodedata.normalize("NFC", raw_src)
        tgt = unicodedata.normalize("NFC", raw_tgt)

        # 2. Whitespace & Control Characters
        src = normalize_whitespace(strip_control_chars(src))
        tgt = normalize_whitespace(strip_control_chars(tgt))

        # 3. Empty sentence check
        if not src or not tgt:
            stats["REJECT_EMPTY_ROW"] += 1
            rejected_records.append({
                "id": rec_id, "source": source, "src_text": src, "tgt_text": tgt,
                "reason": "EMPTY_OR_WHITESPACE_ONLY", "source_id": src_id
            })
            continue

        # 4. Script Validation
        if not has_devanagari(src):
            stats["REJECT_MISSING_DEVANAGARI"] += 1
            rejected_records.append({
                "id": rec_id, "source": source, "src_text": src, "tgt_text": tgt,
                "reason": "SOURCE_MISSING_DEVANAGARI_SCRIPT", "source_id": src_id
            })
            continue

        if not has_ol_chiki(tgt):
            stats["REJECT_MISSING_OL_CHIKI"] += 1
            rejected_records.append({
                "id": rec_id, "source": source, "src_text": src, "tgt_text": tgt,
                "reason": "TARGET_MISSING_OL_CHIKI_SCRIPT", "source_id": src_id
            })
            continue

        # 5. Suspicious Latin script leakage
        if has_suspicious_latin(tgt):
            stats["REJECT_LATIN_LEAKAGE_TARGET"] += 1
            rejected_records.append({
                "id": rec_id, "source": source, "src_text": src, "tgt_text": tgt,
                "reason": "LATIN_SCRIPT_LEAKAGE_IN_OL_CHIKI", "source_id": src_id
            })
            continue

        if has_suspicious_latin(src):
            stats["REJECT_LATIN_LEAKAGE_SOURCE"] += 1
            rejected_records.append({
                "id": rec_id, "source": source, "src_text": src, "tgt_text": tgt,
                "reason": "LATIN_SCRIPT_LEAKAGE_IN_DEVANAGARI", "source_id": src_id
            })
            continue

        # 6. Known Synthetic Corruption Check
        if src in KNOWN_SYNTHETIC_CORRUPTIONS:
            stats["REJECT_SYNTHETIC_CORRUPTION"] += 1
            rejected_records.append({
                "id": rec_id, "source": source, "src_text": src, "tgt_text": tgt,
                "reason": "PHASE_1_AUDIT_SYNTHETIC_GRAMMATICAL_CORRUPTION", "source_id": src_id
            })
            continue

        # 7. Exact Duplicate Check
        pair_key = (src, tgt)
        if pair_key in seen_exact_pairs:
            stats["REJECT_EXACT_DUPLICATE"] += 1
            rejected_records.append({
                "id": rec_id, "source": source, "src_text": src, "tgt_text": tgt,
                "reason": "EXACT_DUPLICATE_PAIR", "source_id": src_id
            })
            continue
        seen_exact_pairs.add(pair_key)

        # 8. Reverse Duplicate Check (accounting as 1 underlying pair)
        canonical_key = tuple(sorted([src, tgt]))
        if canonical_key in seen_underlying_pairs:
            stats["REJECT_REVERSE_DUPLICATE"] += 1
            rejected_records.append({
                "id": rec_id, "source": source, "src_text": src, "tgt_text": tgt,
                "reason": "REVERSE_COPY_OF_EXISTING_PAIR", "source_id": src_id
            })
            continue
        seen_underlying_pairs.add(canonical_key)

        # 9. Length Ratio Sanity Check
        src_words = len(src.split())
        tgt_words = len(tgt.split())
        char_ratio = len(tgt) / max(1, len(src))

        if src_words > 50 or tgt_words > 50:
            stats["REJECT_EXCESSIVE_LENGTH"] += 1
            rejected_records.append({
                "id": rec_id, "source": source, "src_text": src, "tgt_text": tgt,
                "reason": "WORD_COUNT_EXCEEDS_50", "source_id": src_id
            })
            continue

        if char_ratio < 0.25 or char_ratio > 3.5:
            stats["REJECT_EXTREME_RATIO"] += 1
            rejected_records.append({
                "id": rec_id, "source": source, "src_text": src, "tgt_text": tgt,
                "reason": f"EXTREME_CHARACTER_RATIO_{char_ratio:.2f}", "source_id": src_id
            })
            continue

        # 10. Benchmark Contamination Check
        src_hash = hashlib.sha256(src.encode('utf-8')).hexdigest()
        tgt_hash = hashlib.sha256(tgt.encode('utf-8')).hexdigest()
        is_contaminated = False

        if src_hash in benchmark_hindi_hashes or tgt_hash in benchmark_santali_hashes:
            is_contaminated = True
            contamination_type = "EXACT_BENCHMARK_HASH_MATCH"
        else:
            # Fast fuzzy match check against benchmark Hindi sentences using pre-tokenized word sets
            src_words_set = set(re.findall(r'\w+', src.lower()))
            if src_words_set:
                for b_words in benchmark_hindi_word_sets:
                    if b_words:
                        inter = len(src_words_set.intersection(b_words))
                        union = len(src_words_set.union(b_words))
                        if union > 0 and (inter / union) > 0.85:
                            is_contaminated = True
                            contamination_type = "FUZZY_BENCHMARK_WORD_SIMILARITY_OVER_85"
                            break

        if is_contaminated:
            stats["REJECT_BENCHMARK_CONTAMINATION"] += 1
            rejected_records.append({
                "id": rec_id, "source": source, "src_text": src, "tgt_text": tgt,
                "reason": f"BENCHMARK_CONTAMINATION_{contamination_type}", "source_id": src_id
            })
            contamination_log.append({
                "record_id": rec_id, "src": src, "tgt": tgt, "type": contamination_type
            })
            continue

        # 11. Domain Tagging (Phase 2A Taxonomy)
        assigned_domain = classify_domain(src, tgt)

        # 12. Quality Classification & Ol Chiki Phaarkaa/Ohod Handling
        # Step 10: Flag ASCII hyphen in Santali as NEEDS_REVIEW instead of silently replacing
        has_hyphen_in_tgt = "-" in tgt
        unusual_ratio = char_ratio < 0.35 or char_ratio > 2.8

        if has_hyphen_in_tgt or unusual_ratio:
            status = "NEEDS_REVIEW"
            review_reasons = []
            if has_hyphen_in_tgt:
                review_reasons.append("Contains ASCII hyphen in Santali; potential Ol Chiki Phaarkaa candidate")
            if unusual_ratio:
                review_reasons.append(f"Borderline character length ratio ({char_ratio:.2f})")

            review_records.append({
                "id": rec_id,
                "source": source,
                "source_subset": subset,
                "src_lang": "hin_Deva",
                "tgt_lang": "sat_Olck",
                "src_text": src,
                "tgt_text": tgt,
                "domain": assigned_domain,
                "license": license_type,
                "quality_status": status,
                "verification_status": "AUTO_CLASSIFIED_NEEDS_REVIEW",
                "review_notes": "; ".join(review_reasons),
                "source_id": src_id
            })
            stats["STATUS_NEEDS_REVIEW"] += 1
        else:
            status = "VALID"
            valid_records.append({
                "id": rec_id,
                "source": source,
                "source_subset": subset,
                "src_lang": "hin_Deva",
                "tgt_lang": "sat_Olck",
                "src_text": src,
                "tgt_text": tgt,
                "domain": assigned_domain,
                "license": license_type,
                "quality_status": status,
                "verification_status": "PIPELINE_VERIFIED",
                "review_notes": "Passed all 15 automated validation checks",
                "source_id": src_id
            })
            stats["STATUS_VALID"] += 1

    # ------------------------------------------------------------------
    # 5. Output Artifact Generation
    # ------------------------------------------------------------------
    # A. Cleaned Dataset (VALID only)
    cleaned_csv = os.path.join(CLEANED_DIR, "cleaned_parallel.csv")
    fields = ["id", "source", "source_subset", "src_lang", "tgt_lang", "src_text", "tgt_text", "domain", "license", "quality_status", "verification_status", "source_id"]
    with open(cleaned_csv, "w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fields)
        writer.writeheader()
        for r in valid_records:
            row_dict = {k: r[k] for k in fields if k in r}
            writer.writerow(row_dict)

    # B. Human Review Queue (NEEDS_REVIEW)
    review_csv = os.path.join(VERIFIED_DIR, "human_review_queue.csv")
    review_fields = ["id", "source", "source_subset", "src_lang", "tgt_lang", "src_text", "tgt_text", "domain", "license", "quality_status", "verification_status", "review_notes", "source_id"]
    with open(review_csv, "w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=review_fields)
        writer.writeheader()
        for r in review_records:
            writer.writerow(r)

    # C. Rejected Records
    rejected_csv = os.path.join(METADATA_DIR, "rejected.csv")
    rej_fields = ["id", "source", "src_text", "tgt_text", "reason", "source_id"]
    with open(rejected_csv, "w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=rej_fields)
        writer.writeheader()
        for r in rejected_records:
            writer.writerow(r)

    # D. Full Provenance Record
    provenance_csv = os.path.join(METADATA_DIR, "provenance.csv")
    prov_fields = ["id", "source", "source_subset", "src_lang", "tgt_lang", "src_text", "tgt_text", "domain", "license", "quality_status", "verification_status", "source_id"]
    with open(provenance_csv, "w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=prov_fields)
        writer.writeheader()
        for r in valid_records + review_records:
            row_dict = {k: r[k] for k in prov_fields if k in r}
            writer.writerow(row_dict)

    # E. Benchmark Contamination Audit
    contamination_json = os.path.join(METADATA_DIR, "contamination_audit.json")
    with open(contamination_json, "w", encoding="utf-8") as f:
        json.dump({
            "audit_timestamp": "2026-09-03",
            "protected_benchmarks": ["AI4Bharat IN22-Conv (1,503 sentences)"],
            "total_benchmark_sentences_checked": len(benchmark_hindi_sentences),
            "contaminated_records_quarantined": len(contamination_log),
            "quarantined_details": contamination_log
        }, f, ensure_ascii=False, indent=2)

    # ------------------------------------------------------------------
    # 6. Print Summary Metrics
    # ------------------------------------------------------------------
    total_unique_processed = len(valid_records) + len(review_records)
    print("\n=======================================================")
    print("        TRIBETALK V2 CLEANING PIPELINE RESULTS         ")
    print("=======================================================")
    print(f"Total Input Intermediate Records: {len(records)}")
    print(f"Total Unique Parallel Pairs:     {total_unique_processed}")
    print(f"  [+] VALID Pairs:               {len(valid_records)}")
    print(f"  [?] NEEDS_REVIEW Pairs:        {len(review_records)}")
    print(f"  [-] REJECTED Records:          {len(rejected_records)}")
    print("-------------------------------------------------------")
    print("Source-wise Unique Pairs Breakdown:")
    source_counts = Counter(r["source"] for r in (valid_records + review_records))
    for src_name, count in source_counts.items():
        v_count = sum(1 for r in valid_records if r["source"] == src_name)
        r_count = sum(1 for r in review_records if r["source"] == src_name)
        print(f"  • {src_name:20s}: {count:5d} total ({v_count} VALID, {r_count} NEEDS_REVIEW)")
    print("-------------------------------------------------------")
    print("Domain Distribution (VALID + REVIEW):")
    domain_counts = Counter(r["domain"] for r in (valid_records + review_records))
    for dom, count in domain_counts.most_common():
        print(f"  • {dom:25s}: {count:4d}")
    print("-------------------------------------------------------")
    print("Rejection Breakdown:")
    for k, v in stats.items():
        if k.startswith("REJECT_"):
            print(f"  • {k:30s}: {v}")
    print("=======================================================\n")

if __name__ == "__main__":
    clean_corpus()
