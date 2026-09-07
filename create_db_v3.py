import sqlite3
import os

old_db_path = './app/src/main/assets/quran/quran_hafs_uthmani_v2.db'
new_db_path = './app/src/main/assets/quran/quran_hafs_uthmani_v3.db'

if os.path.exists(new_db_path):
    os.remove(new_db_path)

old_conn = sqlite3.connect(old_db_path)
old_c = old_conn.cursor()

new_conn = sqlite3.connect(new_db_path)
new_c = new_conn.cursor()

# 1. Create original tables
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

# 2. Copy data
old_c.execute('SELECT * FROM surahs')
new_c.executemany('INSERT INTO surahs VALUES (?, ?, ?, ?, ?, ?)', old_c.fetchall())

old_c.execute('SELECT * FROM verses')
new_c.executemany('INSERT INTO verses VALUES (?, ?, ?)', old_c.fetchall())

old_c.execute('SELECT * FROM integrity_lock')
new_c.executemany('INSERT INTO integrity_lock VALUES (?, ?, ?, ?, ?)', old_c.fetchall())

# 3. Create bookmarks table
new_c.execute('''
CREATE TABLE bookmarks(
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    surah_id INTEGER NOT NULL,
    ayah_number INTEGER NOT NULL,
    created_at INTEGER NOT NULL
)
''')

# 4. Create juz_boundaries table
new_c.execute('''
CREATE TABLE juz_boundaries(
    juz_number INTEGER PRIMARY KEY NOT NULL,
    surah_id INTEGER NOT NULL,
    ayah_number INTEGER NOT NULL
)
''')

juz_data = [
    (1, 1, 1),
    (2, 2, 142),
    (3, 2, 253),
    (4, 3, 93),
    (5, 4, 24),
    (6, 4, 148),
    (7, 5, 82),
    (8, 6, 111),
    (9, 7, 88),
    (10, 8, 41),
    (11, 9, 93),
    (12, 11, 6),
    (13, 12, 53),
    (14, 15, 1),
    (15, 17, 1),
    (16, 18, 75),
    (17, 21, 1),
    (18, 23, 1),
    (19, 25, 21),
    (20, 27, 56),
    (21, 29, 46),
    (22, 33, 31),
    (23, 36, 28),
    (24, 39, 32),
    (25, 41, 47),
    (26, 46, 1),
    (27, 51, 31),
    (28, 58, 1),
    (29, 67, 1),
    (30, 78, 1)
]

new_c.executemany('INSERT INTO juz_boundaries VALUES (?, ?, ?)', juz_data)

new_conn.commit()
new_conn.close()
old_conn.close()
print("v3 Database created.")
