import os

icons = {
    "ic_worship_taqibat.xml": """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:pathData="M12,5 A8,8 0 1,1 12,21 A8,8 0 1,1 12,5" android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5"/>
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:strokeLineCap="round" android:pathData="M12,9 L12,13 L15,13 M4.5,4.5 L7.5,7.5 M19.5,4.5 L16.5,7.5 M7,21 L5,22 M17,21 L19,22"/>
</vector>""",
    "ic_worship_masbaha.xml": """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:strokeDashArray="2,3" android:pathData="M12,4 A7,7 0 1,1 5,11 C5,8 8,4 12,4 Z"/>
    <path android:fillColor="#FF000000" android:pathData="M5,11 A1.5,1.5 0 1,1 5,14 A1.5,1.5 0 1,1 5,11 M5,14 L5,18 L3,18 L3,14 Z"/>
    <path android:fillColor="#FF000000" android:pathData="M12,3 A1,1 0 1,1 12,5 A1,1 0 1,1 12,3 M16,4 A1,1 0 1,1 16,6 A1,1 0 1,1 16,4 M18.5,7 A1,1 0 1,1 18.5,9 A1,1 0 1,1 18.5,7 M19,10 A1,1 0 1,1 19,12 A1,1 0 1,1 19,10 M17.5,14 A1,1 0 1,1 17.5,16 A1,1 0 1,1 17.5,14 M14,16.5 A1,1 0 1,1 14,18.5 A1,1 0 1,1 14,16.5 M10,16 A1,1 0 1,1 10,18 A1,1 0 1,1 10,16 M7,13.5 A1,1 0 1,1 7,15.5 A1,1 0 1,1 7,13.5 M8.5,4.5 A1,1 0 1,1 8.5,6.5 A1,1 0 1,1 8.5,4.5"/>
</vector>""",
    "ic_worship_qibla.xml": """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:pathData="M12,3 A9,9 0 1,1 12,21 A9,9 0 1,1 12,3" android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5"/>
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:strokeLineJoin="round" android:pathData="M12,5 L14,12 L12,19 L10,12 Z"/>
    <path android:fillColor="#FF000000" android:pathData="M12,5 L14,12 L10,12 Z"/>
</vector>""",
    "ic_worship_counter.xml": """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:pathData="M6,6 L18,6 C19.1,6 20,6.9 20,8 L20,16 C20,17.1 19.1,18 18,18 L6,18 C4.9,18 4,17.1 4,16 L4,8 C4,6.9 4.9,6 6,6 Z"/>
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:pathData="M7,10 A1,1 0 0,1 9,10 L9,14 A1,1 0 0,1 7,14 Z"/>
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:pathData="M11,10 A1,1 0 0,1 13,10 L13,14 A1,1 0 0,1 11,14 Z"/>
    <path android:fillColor="#00000000" android:strokeColor="#FF000000" android:strokeWidth="1.5" android:pathData="M15,10 A1,1 0 0,1 17,10 L17,14 A1,1 0 0,1 15,14 Z"/>
</vector>""",
    "ic_ornate_divider.xml": """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="120dp" android:height="8dp" android:viewportWidth="120" android:viewportHeight="8">
    <path android:fillColor="#00000000" android:strokeColor="#E2C275" android:strokeWidth="1" android:pathData="M10,4 L50,4 M70,4 L110,4"/>
    <path android:fillColor="#00000000" android:strokeColor="#E2C275" android:strokeWidth="1" android:pathData="M60,1 L63,4 L60,7 L57,4 Z"/>
    <path android:pathData="M54,2.5 A1.5,1.5 0 1,1 54,5.5 A1.5,1.5 0 1,1 54,2.5" android:fillColor="#00000000" android:strokeColor="#E2C275" android:strokeWidth="1"/>
    <path android:pathData="M66,2.5 A1.5,1.5 0 1,1 66,5.5 A1.5,1.5 0 1,1 66,2.5" android:fillColor="#00000000" android:strokeColor="#E2C275" android:strokeWidth="1"/>
</vector>"""
}

for name, content in icons.items():
    with open(f"app/src/main/res/drawable/{name}", "w") as f:
        f.write(content)
