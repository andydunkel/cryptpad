#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
REPO_ROOT=$(cd -- "$SCRIPT_DIR/../.." && pwd)
BUILD_ROOT="$REPO_ROOT/build/appimage"
TOOLS_DIR="$BUILD_ROOT/tools"
DIST_DIR="$REPO_ROOT/dist"
CRYPTPAD_BIN="$REPO_ROOT/cryptpad_laz/out/cryptpad"
UPDATER_BIN="$REPO_ROOT/cryptpad_laz/out/updater"
UPDATER_INI="$REPO_ROOT/cryptpad_laz/out/updater.ini"
UPDATER_LANG="$REPO_ROOT/cryptpad_laz/out/updlang.ini"
LANG_DIR="$REPO_ROOT/cryptpad_laz/out/lang"
DESKTOP_FILE="$SCRIPT_DIR/net.dasoftware.cryptpad.desktop"
APPSTREAM_FILE="$SCRIPT_DIR/net.dasoftware.cryptpad.metainfo.xml"
ICON_FILE="$SCRIPT_DIR/net.dasoftware.cryptpad.png"

LINUXDEPLOY=${LINUXDEPLOY:-$TOOLS_DIR/linuxdeploy-x86_64.AppImage}
GTK_PLUGIN=${GTK_PLUGIN:-$TOOLS_DIR/linuxdeploy-plugin-gtk.sh}
LINUXDEPLOY_URL=${LINUXDEPLOY_URL:-https://github.com/linuxdeploy/linuxdeploy/releases/download/continuous/linuxdeploy-x86_64.AppImage}
GTK_PLUGIN_URL=${GTK_PLUGIN_URL:-https://raw.githubusercontent.com/linuxdeploy/linuxdeploy-plugin-gtk/master/linuxdeploy-plugin-gtk.sh}

ini_value() {
  local key=$1
  awk -F= -v key="$key" '$1 == key { print $2; exit }' "$UPDATER_INI" | tr -d '\r'
}

detect_version() {
  local major minor patch
  major=$(ini_value version)
  minor=$(ini_value subversion)
  patch=$(ini_value bugfix)
  printf '%s.%s.%s\n' "$major" "$minor" "$patch"
}

require_file() {
  if [[ ! -f $1 ]]; then
    printf 'Required file not found: %s\n' "$1" >&2
    exit 1
  fi
}

require_directory() {
  if [[ ! -d $1 ]]; then
    printf 'Required directory not found: %s\n' "$1" >&2
    exit 1
  fi
}

download_tool() {
  local url=$1
  local destination=$2
  if [[ -x $destination ]]; then
    return
  fi
  printf 'Downloading %s\n' "$url"
  curl --fail --location --retry 3 --output "$destination" "$url"
  chmod +x "$destination"
}

command -v lazbuild >/dev/null
command -v curl >/dev/null

require_file "$UPDATER_BIN"
require_file "$UPDATER_INI"
require_file "$UPDATER_LANG"
require_directory "$LANG_DIR"
require_file "$DESKTOP_FILE"
require_file "$APPSTREAM_FILE"
require_file "$ICON_FILE"

APP_VERSION=${APP_VERSION:-$(detect_version)}
OUTPUT_NAME="DA-CryptPad-$APP_VERSION-x86_64.AppImage"
OUTPUT_PATH="$DIST_DIR/$OUTPUT_NAME"

mkdir -p "$BUILD_ROOT" "$TOOLS_DIR" "$DIST_DIR"
WORK_DIR=$(mktemp -d "$BUILD_ROOT/work.XXXXXX")
APPDIR="$WORK_DIR/DA-CryptPad.AppDir"
trap 'rm -rf -- "$WORK_DIR"' EXIT

download_tool "$LINUXDEPLOY_URL" "$LINUXDEPLOY"
download_tool "$GTK_PLUGIN_URL" "$GTK_PLUGIN"

printf 'Building DA-CryptPad %s\n' "$APP_VERSION"
lazbuild --build-mode='Linux Release' "$REPO_ROOT/cryptpad_laz/cryptpad.lpi"
require_file "$CRYPTPAD_BIN"

install -d "$APPDIR/usr/bin/lang"
install -d "$APPDIR/usr/lib"
install -d "$APPDIR/usr/share/metainfo"
install -d "$APPDIR/usr/share/licenses/da-cryptpad"
install -m 644 "$UPDATER_INI" "$APPDIR/usr/bin/updater.ini"
install -m 644 "$UPDATER_LANG" "$APPDIR/usr/bin/updlang.ini"
install -m 644 "$LANG_DIR/Messages.properties" \
  "$APPDIR/usr/bin/lang/Messages.properties"
install -m 644 "$LANG_DIR/Messages_de.properties" \
  "$APPDIR/usr/bin/lang/Messages_de.properties"
install -m 644 "$APPSTREAM_FILE" \
  "$APPDIR/usr/share/metainfo/net.dasoftware.cryptpad.appdata.xml"
install -m 644 "$REPO_ROOT/LICENSE" \
  "$APPDIR/usr/share/licenses/da-cryptpad/LICENSE"

# Lazarus' GTK2 interface also loads these unversioned names at runtime. Keep
# them inside the AppDir so it cannot load a second GTK/GDK copy from the host.
ln -s libgdk-x11-2.0.so.0 "$APPDIR/usr/lib/libgdk-x11-2.0.so"
ln -s libgtk-x11-2.0.so.0 "$APPDIR/usr/lib/libgtk-x11-2.0.so"

rm -f -- "$OUTPUT_PATH" "$OUTPUT_PATH.sha256"

export ARCH=x86_64
export APPIMAGE_EXTRACT_AND_RUN=1
export LDAI_OUTPUT="$OUTPUT_PATH"
export LINUXDEPLOY_OUTPUT_VERSION="$APP_VERSION"

"$LINUXDEPLOY" \
  --appdir "$APPDIR" \
  --executable "$CRYPTPAD_BIN" \
  --executable "$UPDATER_BIN" \
  --desktop-file "$DESKTOP_FILE" \
  --icon-file "$ICON_FILE" \
  --plugin gtk \
  --output appimage

require_file "$OUTPUT_PATH"
(
  cd "$DIST_DIR"
  sha256sum "$OUTPUT_NAME" > "$OUTPUT_NAME.sha256"
)

printf '\nCreated:\n  %s\n  %s\n' "$OUTPUT_PATH" "$OUTPUT_PATH.sha256"
