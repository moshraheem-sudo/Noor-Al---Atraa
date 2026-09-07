import re
import sys

def unescape(m):
    return chr(int(m.group(1), 16))

with open("worship.txt", "r", encoding="utf-8") as f:
    content = f.read()

content = re.sub(r'\\u([0-9a-fA-F]{4})', unescape, content)
with open("worship_unescaped.txt", "w", encoding="utf-8") as f:
    f.write(content)
