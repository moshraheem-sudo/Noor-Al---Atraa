import os

icons = {
    "ic_worship_azkar.xml": """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:pathData="M21,6V20C21,20 18.5,18.5 12,19.5C5.5,18.5 3,20 3,20V6C3,6 5.5,4.5 12,5.5C18.5,4.5 21,6 21,6Z"/>
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:pathData="M12,5.5V19.5"/>
    <path android:fillColor="#FF000000" android:pathData="M17,16 A1,1 0 1,1 17,14 A1,1 0 1,1 17,16 M14,15 A1,1 0 1,1 14,13 A1,1 0 1,1 14,15 M10,15 A1,1 0 1,1 10,13 A1,1 0 1,1 10,15 M7,16 A1,1 0 1,1 7,14 A1,1 0 1,1 7,16 M12,17.5 A1,1 0 1,1 12,15.5 A1,1 0 1,1 12,17.5" />
</vector>""",
    "ic_worship_munajat.xml": """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:pathData="M12,5 C12.5,4 13.5,3.5 14.5,4 C15.5,4.5 16,5.5 15.5,6.5 C15,7.5 14,7 13.5,7 L11.5,10 C11,11 11,13 11,13 L11,19 C11,19.5 10.5,20 10,20 L6,20 C5.5,20 5,19.5 5,19 L5,18 L9,18 L9,13 L6,13 C5.5,13 5,12.5 5,12 L5,9 C5,8 6,7 7,7 L11,7 Z"/>
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:pathData="M15,15 L18,15 M18,15 L19,16 M18,15 L17,14"/>
</vector>""",
    "ic_worship_mosque.xml": """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:pathData="M12,4 C14,6 16,8 16,12 L16,20 L8,20 L8,12 C8,8 10,6 12,4 Z"/>
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:pathData="M12,2 L12,4 M10,20 L10,15 A2,2 0 0,1 14,15 L14,20 M4,20 L4,10 M20,20 L20,10 M4,7 L4,10 L6,10 L6,7 M20,7 L20,10 L18,10 L18,7"/>
</vector>""",
    "ic_worship_duas.xml": """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:strokeLineCap="round" android:strokeLineJoin="round" android:pathData="M9,20 L6,20 C4.5,20 3,19 3,17 L3,14 C3,13.5 3.5,13 4,13 L8,13 M15,20 L18,20 C19.5,20 21,19 21,17 L21,14 C21,13.5 20.5,13 20,13 L16,13 M8,13 L8,6 C8,5.5 8.5,5 9,5 C9.5,5 10,5.5 10,6 L10,12 M16,13 L16,6 C16,5.5 15.5,5 15,5 C14.5,5 14,5.5 14,6 L14,12 M10,10 L10,4 C10,3.5 10.5,3 11,3 C11.5,3 12,3.5 12,4 L12,12 M14,10 L14,4 C14,3.5 13.5,3 13,3 C12.5,3 12,3.5 12,4"/>
</vector>""",
    "ic_worship_sahifa.xml": """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#FF000000" android:pathData="M14,4 L14,14 L12,12 L10,14 L10,4 Z"/>
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:pathData="M5,4 C5,2.9 5.9,2 7,2 L17,2 C18.1,2 19,2.9 19,4 L19,20 C19,21.1 18.1,22 17,22 L7,22 C5.9,22 5,21.1 5,20 Z"/>
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:pathData="M5,18 L19,18 M8,22 L8,2 M5,4 L8,4 M5,20 L8,20"/>
</vector>""",
    "ic_worship_aamal.xml": """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:pathData="M9,4 L7,4 C5.9,4 5,4.9 5,6 L5,20 C5,21.1 5.9,22 7,22 L17,22 C18.1,22 19,21.1 19,20 L19,6 C19,4.9 18.1,4 17,4 L15,4"/>
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:pathData="M10,2 L14,2 A1,1 0 0 1 15,3 L15,5 A1,1 0 0 1 14,6 L10,6 A1,1 0 0 1 9,5 L9,3 A1,1 0 0 1 10,2 Z"/>
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:strokeLineCap="round" android:strokeLineJoin="round" android:pathData="M9,11 L10.5,12.5 L15,8 M9,16 L10.5,17.5 L15,13"/>
</vector>""",
    "ic_worship_taqibat.xml": """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <circle android:cx="12" android:cy="13" android:r="8" android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5"/>
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:strokeLineCap="round" android:pathData="M12,9 L12,13 L15,13 M4.5,4.5 L7.5,7.5 M19.5,4.5 L16.5,7.5 M7,21 L5,22 M17,21 L19,22"/>
</vector>""",
    "ic_worship_masbaha.xml": """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:strokeDashArray="2,3" android:pathData="M12,4 A7,7 0 1,1 5,11 C5,8 8,4 12,4 Z"/>
    <path android:fillColor="#FF000000" android:pathData="M5,11 A1.5,1.5 0 1,1 5,14 A1.5,1.5 0 1,1 5,11 M5,14 L5,18 L3,18 L3,14 Z"/>
    <circle android:cx="12" android:cy="4" android:r="1" android:fillColor="#FF000000"/>
    <circle android:cx="16" android:cy="5" android:r="1" android:fillColor="#FF000000"/>
    <circle android:cx="18.5" android:cy="8" android:r="1" android:fillColor="#FF000000"/>
    <circle android:cx="19" android:cy="11" android:r="1" android:fillColor="#FF000000"/>
    <circle android:cx="17.5" android:cy="15" android:r="1" android:fillColor="#FF000000"/>
    <circle android:cx="14" android:cy="17.5" android:r="1" android:fillColor="#FF000000"/>
    <circle android:cx="10" android:cy="17" android:r="1" android:fillColor="#FF000000"/>
    <circle android:cx="7" android:cy="14.5" android:r="1" android:fillColor="#FF000000"/>
    <circle android:cx="8.5" android:cy="5.5" android:r="1" android:fillColor="#FF000000"/>
</vector>""",
    "ic_worship_selected.xml": """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:pathData="M7,6 L17,6 C18.1,6 19,6.9 19,8 L19,16 C19,17.1 18.1,18 17,18 L7,18 C5.9,18 5,17.1 5,16 L5,8 C5,6.9 5.9,6 7,6 Z"/>
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:pathData="M9,10 L15,10 M9,14 L15,14 M6,18 C6,18 3,18 3,14 C3,14 6,14 6,18 M18,18 C18,18 21,18 21,14 C21,14 18,14 18,18"/>
</vector>""",
    "ic_worship_qibla.xml": """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <circle android:cx="12" android:cy="12" android:r="9" android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5"/>
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:strokeLineJoin="round" android:pathData="M12,5 L14,12 L12,19 L10,12 Z"/>
    <path android:fillColor="#FF000000" android:pathData="M12,5 L14,12 L10,12 Z"/>
</vector>""",
    "ic_worship_counter.xml": """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:pathData="M6,6 L18,6 C19.1,6 20,6.9 20,8 L20,16 C20,17.1 19.1,18 18,18 L6,18 C4.9,18 4,17.1 4,16 L4,8 C4,6.9 4.9,6 6,6 Z"/>
    <rect android:x="7" android:y="9" android:width="2" android:height="6" android:rx="1" android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5"/>
    <rect android:x="11" android:y="9" android:width="2" android:height="6" android:rx="1" android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5"/>
    <rect android:x="15" android:y="9" android:width="2" android:height="6" android:rx="1" android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5"/>
</vector>""",
    "ic_ornate_divider.xml": """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="120dp" android:height="8dp" android:viewportWidth="120" android:viewportHeight="8">
    <path android:fillColor="#00000000" android:strokeColor="#E2C275" android:strokeWidth="1" android:pathData="M10,4 L50,4 M70,4 L110,4"/>
    <path android:fillColor="#00000000" android:strokeColor="#E2C275" android:strokeWidth="1" android:pathData="M60,1 L63,4 L60,7 L57,4 Z"/>
    <circle android:cx="54" android:cy="4" android:r="1.5" android:strokeColor="#E2C275" android:strokeWidth="1"/>
    <circle android:cx="66" android:cy="4" android:r="1.5" android:strokeColor="#E2C275" android:strokeWidth="1"/>
</vector>"""
}

for name, content in icons.items():
    with open(f"app/src/main/res/drawable/{name}", "w") as f:
        f.write(content)
