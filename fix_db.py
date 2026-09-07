import sqlite3
import os

db_path = './app/src/main/assets/quran/quran_hafs_uthmani.db'
if os.path.exists(db_path):
    os.rename(db_path, db_path + '.bak')

old_conn = sqlite3.connect(db_path + '.bak')
old_c = old_conn.cursor()

new_conn = sqlite3.connect(db_path)
new_c = new_conn.cursor()

# Create tables with NOT NULL constraints according to Room's expectations for Kotlin non-nullable types
new_c.execute('''
CREATE TABLE surahs(
    id INTEGER PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    transliteration TEXT NOT NULL,
    type TEXT NOT NULL,
    total_verses INTEGER NOT NULL,
    surah_sha256 TEXT NOT NULL
)
''')

new_c.execute('''
CREATE TABLE verses(
    surah_id INTEGER NOT NULL,
    ayah_number INTEGER NOT NULL,
    text TEXT NOT NULL,
    PRIMARY KEY (surah_id, ayah_number)
)
''')

new_c.execute('''
CREATE TABLE integrity_lock(
    id INTEGER PRIMARY KEY NOT NULL,
    total_surahs INTEGER NOT NULL,
    total_ayahs INTEGER NOT NULL,
    full_mushaf_sha256 TEXT NOT NULL,
    generated_at TEXT NOT NULL
)
''')

# Copy data
old_c.execute('SELECT * FROM surahs')
new_c.executemany('INSERT INTO surahs VALUES (?, ?, ?, ?, ?, ?)', old_c.fetchall())

old_c.execute('SELECT * FROM verses')
new_c.executemany('INSERT INTO verses VALUES (?, ?, ?)', old_c.fetchall())

old_c.execute('SELECT * FROM integrity_lock')
new_c.executemany('INSERT INTO integrity_lock VALUES (?, ?, ?, ?, ?)', old_c.fetchall())

new_conn.commit()
new_conn.close()
old_conn.close()
print("Database schema fixed.")
