import sqlite3
import hashlib

def sha256(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()

conn = sqlite3.connect('./app/src/main/assets/quran/quran_hafs_uthmani.db')
c = conn.cursor()

c.execute('SELECT COUNT(*) FROM surahs')
surah_count = c.fetchone()[0]

c.execute('SELECT COUNT(*) FROM verses')
verse_count = c.fetchone()[0]

print(f"Surahs: {surah_count}, Verses: {verse_count}")

c.execute('SELECT text FROM verses ORDER BY surah_id, ayah_number')
verses = c.fetchall()
full_text = "".join(v[0] for v in verses)

full_hash = sha256(full_text)

c.execute('SELECT full_mushaf_sha256 FROM integrity_lock WHERE id=1')
expected_hash = c.fetchone()[0]

print(f"Calculated full hash: {full_hash}")
print(f"Expected full hash:   {expected_hash}")
if full_hash == expected_hash:
    print("Hashes MATCH! Integrity verified.")
else:
    print("Hashes DO NOT MATCH!")

conn.close()
