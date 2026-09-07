import sqlite3
import hashlib

conn = sqlite3.connect('./app/src/main/assets/quran/quran_hafs_uthmani.db')
c = conn.cursor()
c.execute('SELECT text FROM verses ORDER BY surah_id ASC, ayah_number ASC')
full_text = "".join(v[0] for v in c.fetchall())
calculated_hash = hashlib.sha256(full_text.encode('utf-8')).hexdigest()

c.execute('SELECT full_mushaf_sha256 FROM integrity_lock WHERE id=1')
expected_hash = c.fetchone()[0]

print(f"Calculated Hash: {calculated_hash}")
print(f"Expected Hash:   {expected_hash}")
if calculated_hash == expected_hash:
    print("Match!")
else:
    print("Mismatch!")
