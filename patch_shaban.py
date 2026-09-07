import json
import re

def process_transcript():
    with open('/.aistudio/artifacts/brain/8499f3c2-1358-4fcd-ba58-190ba6e21c9c/.system_generated/logs/transcript.jsonl', 'r', encoding='utf-8') as f:
        lines = f.readlines()
        
    shaban_content = None
    for line in lines:
        try:
            data = json.loads(line)
            if data.get('role') == 'user':
                content = data.get('content', '')
                if 'الا حدث أعمال شهر شعبان المبارك حسب المعلومات التالية' in content:
                    shaban_content = content
        except:
            pass

    if not shaban_content:
        print("Could not find Shaban content in transcript.")
        return

    # Extract everything after the introductory text
    start_marker = "سوف ارسل إليك الاعمال حسب الصورة بالترتيب وارسل إليك النصوص الكلام نفسه فقط اكتبه وكل واحد يعني كل عنوان أسفل منه اكتب معلوماته...."
    if start_marker in shaban_content:
        shaban_data = shaban_content.split(start_marker)[1].strip()
    else:
        shaban_data = shaban_content

    # Now we need to parse titles and content.
    # The user says "كل عنوان أسفل منه اكتب معلوماته"
    # Usually it might be "اليوم الأول.. "
    # Let's write the raw shaban data to a file first so we can inspect it.
    with open('shaban_raw.txt', 'w', encoding='utf-8') as f:
        f.write(shaban_data)
    print("Wrote shaban_raw.txt")

process_transcript()
