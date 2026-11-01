#!/usr/bin/env bash
# Runs the Maestro UI tests in .maestro/ against the release APK on a running
# emulator. --public-chat also posts a real message to the public chat.
set -uo pipefail

cd "$(dirname "$0")/.." || exit 1

APK=app/build/outputs/apk/release/app-release.apk
OUT=build/ui-tests
DEVICE_ARGS=()
if [ -n "${ANDROID_SERIAL:-}" ]; then
    DEVICE_ARGS=(--device "$ANDROID_SERIAL")
fi

export MAESTRO_CLI_NO_ANALYTICS=1
export MAESTRO_DRIVER_STARTUP_TIMEOUT=180000

set -e
mkdir -p "$OUT"
adb install -r "$APK"
for permission in CAMERA RECORD_AUDIO ACCESS_FINE_LOCATION ACCESS_COARSE_LOCATION; do
    adb shell pm grant com.android.camera2 "android.permission.$permission"
done
for fixture in route.gpx broken.gpx; do
    adb push ".maestro/fixtures/$fixture" /sdcard/Download/
    adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE \
        -d "file:///sdcard/Download/$fixture" > /dev/null
done
set +e

status=0
maestro test "${DEVICE_ARGS[@]+"${DEVICE_ARGS[@]}"}" .maestro \
    --format junit --output "$OUT/report.xml" --debug-output "$OUT/debug" || status=$?

if [ "${1:-}" = "--public-chat" ]; then
    maestro test "${DEVICE_ARGS[@]+"${DEVICE_ARGS[@]}"}" .maestro/manual/chat_send.yaml \
        --format junit --output "$OUT/report-chat.xml" --debug-output "$OUT/debug-chat" || status=$?
fi

adb logcat -d > "$OUT/logcat.txt"

exit "$status"
