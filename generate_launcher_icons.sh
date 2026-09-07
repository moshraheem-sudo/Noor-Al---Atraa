#!/bin/bash
SRC="app/src/main/res/drawable/noor.png"

# Generate mipmap icons
convert "$SRC" -resize 48x48 app/src/main/res/mipmap-mdpi/ic_launcher.png
convert "$SRC" -resize 48x48 app/src/main/res/mipmap-mdpi/ic_launcher_round.png
convert "$SRC" -resize 48x48 app/src/main/res/mipmap-mdpi/ic_launcher_foreground.png

convert "$SRC" -resize 72x72 app/src/main/res/mipmap-hdpi/ic_launcher.png
convert "$SRC" -resize 72x72 app/src/main/res/mipmap-hdpi/ic_launcher_round.png
convert "$SRC" -resize 72x72 app/src/main/res/mipmap-hdpi/ic_launcher_foreground.png

convert "$SRC" -resize 96x96 app/src/main/res/mipmap-xhdpi/ic_launcher.png
convert "$SRC" -resize 96x96 app/src/main/res/mipmap-xhdpi/ic_launcher_round.png
convert "$SRC" -resize 96x96 app/src/main/res/mipmap-xhdpi/ic_launcher_foreground.png

convert "$SRC" -resize 144x144 app/src/main/res/mipmap-xxhdpi/ic_launcher.png
convert "$SRC" -resize 144x144 app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png
convert "$SRC" -resize 144x144 app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.png

convert "$SRC" -resize 192x192 app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
convert "$SRC" -resize 192x192 app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png
convert "$SRC" -resize 192x192 app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png

echo "Icons generated successfully."
