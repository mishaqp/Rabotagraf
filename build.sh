#!/bin/bash
set -e

PROJ_DIR="/home/user/Rabotagraf"
APP_SRC="$PROJ_DIR/app/src/main"
BUILD_DIR="$PROJ_DIR/build"
PKG="com.workschedule"
PKG_PATH="com/workschedule"

ANDROID_JAR="/usr/lib/android-sdk/platforms/android-23/android.jar"
AAPT="/usr/lib/android-sdk/build-tools/debian/aapt"
DX="dalvik-exchange"
ZIPALIGN="/usr/lib/android-sdk/build-tools/debian/zipalign"
APKSIGNER="apksigner"
KEYSTORE="$BUILD_DIR/debug.keystore"

echo "=== Очистка build директории ==="
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/gen"
mkdir -p "$BUILD_DIR/obj"
mkdir -p "$BUILD_DIR/dex"
mkdir -p "$BUILD_DIR/apk_contents"

echo "=== Генерация R.java ==="
$AAPT package -f -m \
    -J "$BUILD_DIR/gen" \
    -M "$APP_SRC/AndroidManifest.xml" \
    -S "$APP_SRC/res" \
    -I "$ANDROID_JAR"

echo "=== Компиляция Java ==="
find "$APP_SRC/java" "$BUILD_DIR/gen" -name "*.java" > "$BUILD_DIR/sources.txt"
javac -source 1.8 -target 1.8 \
    -cp "$ANDROID_JAR" \
    -d "$BUILD_DIR/obj" \
    @"$BUILD_DIR/sources.txt"

echo "=== Создание DEX ==="
$DX --dex \
    --output="$BUILD_DIR/dex/classes.dex" \
    "$BUILD_DIR/obj"

echo "=== Упаковка ресурсов в APK ==="
$AAPT package -f \
    -M "$APP_SRC/AndroidManifest.xml" \
    -S "$APP_SRC/res" \
    -I "$ANDROID_JAR" \
    -F "$BUILD_DIR/rabotagraf-unaligned.apk"

echo "=== Добавление DEX в APK ==="
cd "$BUILD_DIR/dex"
zip -r "$BUILD_DIR/rabotagraf-unaligned.apk" classes.dex
cd "$PROJ_DIR"

echo "=== Создание debug keystore ==="
keytool -genkeypair -v \
    -keystore "$KEYSTORE" \
    -alias debug \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -storepass android \
    -keypass android \
    -dname "CN=Debug, OU=Debug, O=Debug, L=Debug, S=Debug, C=Debug" 2>/dev/null || true

echo "=== Выравнивание APK (zipalign) ==="
$ZIPALIGN -f 4 \
    "$BUILD_DIR/rabotagraf-unaligned.apk" \
    "$BUILD_DIR/rabotagraf-aligned.apk"

echo "=== Подпись APK ==="
$APKSIGNER sign \
    --ks "$KEYSTORE" \
    --ks-pass pass:android \
    --key-pass pass:android \
    --ks-key-alias debug \
    --out "$BUILD_DIR/rabotagraf-debug.apk" \
    "$BUILD_DIR/rabotagraf-aligned.apk"

echo ""
echo "=== ГОТОВО! ==="
ls -lh "$BUILD_DIR/rabotagraf-debug.apk"
echo "APK: $BUILD_DIR/rabotagraf-debug.apk"
