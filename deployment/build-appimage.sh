#!/bin/bash

# Configuration
APP_NAME="DA-CryptPad"
VERSION="1.0.0"
LINUX_DIR="linux"
JAR_FILE="$LINUX_DIR/cryptpad.jar"
JRE_DIR="$LINUX_DIR/jre"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Building ${APP_NAME} AppImage${NC}"
echo -e "${BLUE}========================================${NC}"

# Step 1: Check if JAR exists
echo -e "\n${BLUE}[1/5] Checking JAR file...${NC}"
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${RED}Error: JAR file not found at $JAR_FILE${NC}"
    echo "Please place cryptpad.jar in the linux/ folder"
    exit 1
fi
echo -e "${GREEN}✓ JAR found${NC}"

# Step 2: Check if JRE exists
echo -e "\n${BLUE}[2/5] Checking JRE...${NC}"
if [ ! -d "$JRE_DIR" ]; then
    echo -e "${RED}Error: JRE directory not found at $JRE_DIR${NC}"
    echo "Please extract JRE to linux/jre/"
    exit 1
fi
if [ ! -f "$JRE_DIR/bin/java" ]; then
    echo -e "${RED}Error: Invalid JRE - bin/java not found${NC}"
    exit 1
fi
echo -e "${GREEN}✓ JRE found${NC}"

# Step 3: Create AppImage directory structure
echo -e "\n${BLUE}[3/5] Creating AppImage directory structure...${NC}"
APPDIR="build/AppDir"
rm -rf build
mkdir -p "$APPDIR/lib"
echo -e "${GREEN}✓ Directory structure created${NC}"

# Step 4: Copy application files
echo -e "\n${BLUE}[4/5] Copying application files...${NC}"
cp -r "$JRE_DIR" "$APPDIR/jre"
cp "$JAR_FILE" "$APPDIR/lib/"
cp appimage-resources/AppRun "$APPDIR/"
cp appimage-resources/DA-CryptPad.desktop "$APPDIR/"
cp appimage-resources/cryptpad.png "$APPDIR/"
chmod +x "$APPDIR/AppRun"
echo -e "${GREEN}✓ Files copied${NC}"

# Step 5: Create AppImage
echo -e "\n${BLUE}[5/5] Building AppImage...${NC}"
if ! command -v appimagetool &> /dev/null; then
    echo -e "${RED}Error: appimagetool not found${NC}"
    echo "Install with:"
    echo "  wget https://github.com/AppImage/AppImageKit/releases/download/continuous/appimagetool-x86_64.AppImage"
    echo "  chmod +x appimagetool-x86_64.AppImage"
    echo "  sudo mv appimagetool-x86_64.AppImage /usr/local/bin/appimagetool"
    exit 1
fi

OUTPUT_FILE="build/${APP_NAME}-${VERSION}-x86_64.AppImage"
appimagetool "$APPDIR" "$OUTPUT_FILE"
chmod +x "$OUTPUT_FILE"
echo -e "${GREEN}✓ AppImage created${NC}"

# Summary
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}AppImage created successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "Location: ${BLUE}$OUTPUT_FILE${NC}"
echo -e "Size: $(du -h "$OUTPUT_FILE" | cut -f1)"
echo -e "\nTest with: ./$OUTPUT_FILE"