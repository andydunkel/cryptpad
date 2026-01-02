# Deployment information

## Windows deployment and installer

The following tools are used:

- Inno Setup [https://jrsoftware.org/isinfo.php](https://jrsoftware.org/isinfo.php)
- Launch4j [https://launch4j.sourceforge.net/](https://launch4j.sourceforge.net/)
- Temurin JRE 11 [https://adoptium.net/de/temurin/releases](https://adoptium.net/de/temurin/releases)

Place the JRE in the JRE folder. The installier signed, remove signing from .iss file if needed.


## Linux

### Install app image tool

    wget https://github.com/AppImage/AppImageKit/releases/download/continuous/appimagetool-x86_64.AppImage
    chmod +x appimagetool-x86_64.AppImage
    sudo mv appimagetool-x86_64.AppImage /usr/local/bin/appimagetool

### JAR and JRE

Create the JAR, download the JRE and place them in the folder "linux":

    linux
    ├── cryptpad.jar
    └── jre

Other files are in "appimage-resources". 

Run:

    ./build-appimage.sh