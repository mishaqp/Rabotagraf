#!/bin/bash
set -e

PROJ_DIR="/home/user/Rabotagraf"
APP_SRC="$PROJ_DIR/app/src/main"
BUILD_DIR="$PROJ_DIR/build"

ANDROID_JAR="/usr/lib/android-sdk/platforms/android-23/android.jar"
AAPT2="/usr/lib/android-sdk/build-tools/debian/aapt2"
DX="dalvik-exchange"
ZIPALIGN="/usr/lib/android-sdk/build-tools/debian/zipalign"
APKSIGNER="/usr/lib/android-sdk/build-tools/debian/apksigner"
KEYSTORE="$BUILD_DIR/debug.keystore"

echo "=== Очистка build директории ==="
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/gen"
mkdir -p "$BUILD_DIR/obj"
mkdir -p "$BUILD_DIR/dex"
mkdir -p "$BUILD_DIR/compiled_res"

echo "=== Компиляция ресурсов (aapt2) ==="
$AAPT2 compile \
    --dir "$APP_SRC/res" \
    -o "$BUILD_DIR/compiled_res/res.zip" \
    -v 2>&1 | grep -v "^$" | head -30

echo "=== Линковка ресурсов (aapt2 link) ==="
$AAPT2 link \
    -o "$BUILD_DIR/resources.ap_" \
    -I "$ANDROID_JAR" \
    --manifest "$APP_SRC/AndroidManifest.xml" \
    --java "$BUILD_DIR/gen" \
    --min-sdk-version 21 \
    --target-sdk-version 33 \
    --version-code 1 \
    --version-name "1.0" \
    "$BUILD_DIR/compiled_res/res.zip" \
    -v 2>&1 | grep -v "^$" | head -30

echo "=== Компиляция Java ==="
find "$APP_SRC/java" "$BUILD_DIR/gen" -name "*.java" > "$BUILD_DIR/sources.txt"
javac \
    --release 8 \
    -cp "$ANDROID_JAR" \
    -d "$BUILD_DIR/obj" \
    @"$BUILD_DIR/sources.txt"

echo "=== Создание DEX ==="
$DX --dex \
    --output="$BUILD_DIR/dex/classes.dex" \
    "$BUILD_DIR/obj"

echo "=== Упаковка APK ==="
cp "$BUILD_DIR/resources.ap_" "$BUILD_DIR/rabotagraf-unsigned.apk"
cd "$BUILD_DIR/dex"
zip -r "$BUILD_DIR/rabotagraf-unsigned.apk" classes.dex
cd "$PROJ_DIR"

echo "=== Создание debug keystore ==="
keytool -genkeypair \
    -keystore "$KEYSTORE" \
    -alias androiddebugkey \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -storepass android \
    -keypass android \
    -dname "CN=Android Debug,O=Android,C=US" 2>/dev/null || true

# Android 11+: zipalign BEFORE apksigner (V2/V3 sign AFTER align)
echo "=== Выравнивание APK (zipalign) ==="
$ZIPALIGN -f 4 \
    "$BUILD_DIR/rabotagraf-unsigned.apk" \
    "$BUILD_DIR/rabotagraf-aligned.apk"

echo "=== Подпись APK (apksigner V2+V3) ==="
$APKSIGNER sign \
    --ks "$KEYSTORE" \
    --ks-pass pass:android \
    --key-pass pass:android \
    --ks-key-alias androiddebugkey \
    --v1-signing-enabled true \
    --v2-signing-enabled true \
    --v3-signing-enabled true \
    --out "$BUILD_DIR/rabotagraf-debug.apk" \
    "$BUILD_DIR/rabotagraf-aligned.apk"

echo "=== Верификация подписи ==="
$APKSIGNER verify --verbose "$BUILD_DIR/rabotagraf-debug.apk" 2>&1 | grep -E "Verified|scheme"

echo ""
echo "=== ГОТОВО! ==="
ls -lh "$BUILD_DIR/rabotagraf-debug.apk"

echo ""
echo "=== Проверка содержимого APK ==="
unzip -l "$BUILD_DIR/rabotagraf-debug.apk" | head -20

echo ""
echo "=== Метаданные APK ==="
aapt dump badging "$BUILD_DIR/rabotagraf-debug.apk" 2>&1 | head -10
