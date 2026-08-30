"""
SPRING_F5 Tokenizer Module
Maps text characters (Ol Chiki, Devanagari, Latin) to vocabulary token IDs using checkpoints/vocab.txt.
"""

import os

class SpringF5Tokenizer:
    def __init__(self, vocab_path: str):
        self.vocab_path = vocab_path
        self.char_to_id = {}
        self.id_to_char = {}

        if os.path.exists(vocab_path):
            with open(vocab_path, "r", encoding="utf-8") as f:
                for idx, line in enumerate(f):
                    char = line.rstrip("\n\r")
                    self.char_to_id[char] = idx
                    self.id_to_char[idx] = char

    def encode(self, text: str) -> list[int]:
        token_ids = []
        for ch in text:
            if ch in self.char_to_id:
                token_ids.append(self.char_to_id[ch])
            elif ch.lower() in self.char_to_id:
                token_ids.append(self.char_to_id[ch.lower()])
            else:
                # Fallback to unk (idx 0)
                token_ids.append(0)
        return token_ids

    def decode(self, token_ids: list[int]) -> str:
        return "".join([self.id_to_char.get(i, "") for i in token_ids])
