import bz2
import csv
import os
import sys
import unicodedata

sys.stdout.reconfigure(encoding='utf-8')

raw_dir = 'training/data/v2/raw/tatoeba'
inter_dir = 'training/data/v2/intermediate'
os.makedirs(inter_dir, exist_ok=True)

sat_bz2 = os.path.join(raw_dir, 'sat_sentences.tsv.bz2')
hin_bz2 = os.path.join(raw_dir, 'hin_sentences.tsv.bz2')
sat_hin_bz2 = os.path.join(raw_dir, 'sat-hin_links.tsv.bz2')
sat_eng_bz2 = os.path.join(raw_dir, 'sat-eng_links.tsv.bz2')
hin_eng_bz2 = os.path.join(raw_dir, 'hin-eng_links.tsv.bz2')

# 1. Load Santali sentences
sat_sentences = {}
with bz2.open(sat_bz2, 'rt', encoding='utf-8') as f:
    for line in f:
        parts = line.strip().split('\t')
        if len(parts) >= 3:
            sat_sentences[parts[0]] = parts[2].strip()

# 2. Load Hindi sentences
hin_sentences = {}
with bz2.open(hin_bz2, 'rt', encoding='utf-8') as f:
    for line in f:
        parts = line.strip().split('\t')
        if len(parts) >= 3:
            hin_sentences[parts[0]] = parts[2].strip()

print(f"Loaded {len(sat_sentences)} Santali sentences and {len(hin_sentences)} Hindi sentences.")

all_tatoeba_pairs = {}  # key: (hin_text, sat_text)

# 3. Direct links
with bz2.open(sat_hin_bz2, 'rt', encoding='utf-8') as f:
    for line in f:
        parts = line.strip().split('\t')
        if len(parts) == 2:
            sid, hid = parts
            if sid in sat_sentences and hid in hin_sentences:
                st = unicodedata.normalize('NFC', sat_sentences[sid])
                ht = unicodedata.normalize('NFC', hin_sentences[hid])
                all_tatoeba_pairs[(ht, st)] = {
                    'hin_id': hid,
                    'sat_id': sid,
                    'hin_text': ht,
                    'sat_text': st,
                    'source_subset': 'tatoeba_direct_links',
                    'via_eng_id': ''
                }

print(f"Direct Tatoeba links: {len(all_tatoeba_pairs)}")

# 4. English-mediated graph links
eng_to_sat = {}
with bz2.open(sat_eng_bz2, 'rt', encoding='utf-8') as f:
    for line in f:
        parts = line.strip().split('\t')
        if len(parts) == 2:
            sid, eid = parts
            eng_to_sat.setdefault(eid, set()).add(sid)

eng_to_hin = {}
with bz2.open(hin_eng_bz2, 'rt', encoding='utf-8') as f:
    for line in f:
        parts = line.strip().split('\t')
        if len(parts) == 2:
            hid, eid = parts
            eng_to_hin.setdefault(eid, set()).add(hid)

common_eng = set(eng_to_sat.keys()).intersection(set(eng_to_hin.keys()))

added_graph = 0
for eid in sorted(common_eng):
    for sid in sorted(eng_to_sat[eid]):
        for hid in sorted(eng_to_hin[eid]):
            if sid in sat_sentences and hid in hin_sentences:
                st = unicodedata.normalize('NFC', sat_sentences[sid])
                ht = unicodedata.normalize('NFC', hin_sentences[hid])
                if (ht, st) not in all_tatoeba_pairs:
                    all_tatoeba_pairs[(ht, st)] = {
                        'hin_id': hid,
                        'sat_id': sid,
                        'hin_text': ht,
                        'sat_text': st,
                        'source_subset': 'tatoeba_eng_graph_pivot',
                        'via_eng_id': eid
                    }
                    added_graph += 1

print(f"Added from English-mediated graph links: {added_graph}")
print(f"Total unique Tatoeba parallel pairs: {len(all_tatoeba_pairs)}")

out_rows = []
for idx, ((ht, st), d) in enumerate(all_tatoeba_pairs.items(), 1):
    source_id = "hin:" + str(d['hin_id']) + "|sat:" + str(d['sat_id'])
    if d['via_eng_id']:
        source_id += "|via_eng:" + str(d['via_eng_id'])

    if '-' in st:
        qs = 'NEEDS_REVIEW'
        notes = 'Contains ASCII hyphen in Santali text; review for Ohod'
    else:
        qs = 'VALID'
        notes = "Tatoeba provable link (" + d['source_subset'] + ")"

    out_rows.append({
        'id': f"TT-V2-TAT-{idx:05d}",
        'source': 'TATOEBA',
        'source_subset': d['source_subset'],
        'src_lang': 'hin_Deva',
        'tgt_lang': 'sat_Olck',
        'src_text': ht,
        'tgt_text': st,
        'source_id': source_id,
        'license': 'CC-BY-2.0-FR',
        'quality_status': qs,
        'verification_status': 'PIPELINE_VERIFIED',
        'notes': notes
    })

out_csv = os.path.join(inter_dir, 'tatoeba_hi_sat.csv')
fieldnames = ['id', 'source', 'source_subset', 'src_lang', 'tgt_lang', 'src_text', 'tgt_text', 'source_id', 'license', 'quality_status', 'verification_status', 'notes']
with open(out_csv, 'w', encoding='utf-8', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=fieldnames)
    writer.writeheader()
    writer.writerows(out_rows)

print(f"Wrote {len(out_rows)} Tatoeba pairs to {out_csv}.")
