; DA-CryptPad NSIS Installer Script

; ===== Defines =====
!define APP_NAME        "DA-CryptPad"
!define APP_VERSION     "1.1.0"
!define APP_PUBLISHER   "DA-Software"
!define APP_URL         "https://www.da-software.net"
!define APP_EXE         "cryptpad.exe"
!define APP_GUID        "{67347003-2D90-4209-B9BA-EF949A5EC85B}"
!define REG_UNINSTALL   "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_GUID}"
!define REG_ASSOC_EXT   ".cryptpad"
!define REG_ASSOC_KEY   "${APP_NAME}file"

; ===== General =====
Name            "${APP_NAME}"
OutFile         "installer\cryptpad.exe"
InstallDir      "$PROGRAMFILES64\${APP_NAME}"
InstallDirRegKey HKLM "${REG_UNINSTALL}" "InstallLocation"
RequestExecutionLevel admin
Unicode True

; Compression
SetCompressor /SOLID lzma

; Version Info
VIProductVersion  "${APP_VERSION}.0"
VIAddVersionKey   "ProductName"      "${APP_NAME}"
VIAddVersionKey   "ProductVersion"   "${APP_VERSION}"
VIAddVersionKey   "CompanyName"      "DA-Software.net"
VIAddVersionKey   "FileDescription"  "${APP_NAME} Installer"
VIAddVersionKey   "FileVersion"      "${APP_VERSION}"
VIAddVersionKey   "LegalCopyright"   "${APP_PUBLISHER}"

; ===== Includes =====
!include "MUI2.nsh"
!include "x64.nsh"
!include "LogicLib.nsh"
!include "FileFunc.nsh"

; ===== MUI Settings =====
!define MUI_ABORTWARNING
!define MUI_ICON   "..\res\DA-CryptPad.ico"
!define MUI_UNICON "..\res\DA-CryptPad.ico"

; Pages (Installer)
!insertmacro MUI_PAGE_LICENSE "..\LICENSE"
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_INSTFILES
!define MUI_FINISHPAGE_RUN          "$INSTDIR\${APP_EXE}"
!define MUI_FINISHPAGE_RUN_TEXT     "$(^LaunchProgram) ${APP_NAME}"
!insertmacro MUI_PAGE_FINISH

; Pages (Uninstaller)
!define MUI_PAGE_CUSTOMFUNCTION_PRE un.ConfirmPre
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

; Languages
!insertmacro MUI_LANGUAGE "English"
!insertmacro MUI_LANGUAGE "German"

; ===== Installer Sections =====

Section "-Main" SecMain
    SectionIn RO  ; Pflicht – nicht abwählbar

    SetOutPath "$INSTDIR"

    ; Main executable
    File /oname=${APP_EXE} "${APP_EXE}"

    ; Bundled JRE
    SetOutPath "$INSTDIR\jre"
    File /r "jre\*"
    SetOutPath "$INSTDIR"

    ; Icons
    File /oname=DA-CryptPadFile.ico "..\res\DA-CryptPadFile.ico"
    File /oname=DA-CryptPad.ico     "..\res\DA-CryptPad.ico"

    ; License
    File /oname=LICENSE "..\LICENSE"

    ; Start Menu Shortcut
    CreateDirectory "$SMPROGRAMS\${APP_NAME}"
    CreateShortcut  "$SMPROGRAMS\${APP_NAME}\${APP_NAME}.lnk" \
                    "$INSTDIR\${APP_EXE}" "" \
                    "$INSTDIR\DA-CryptPad.ico" 0
    CreateShortcut  "$SMPROGRAMS\${APP_NAME}\Uninstall ${APP_NAME}.lnk" \
                    "$INSTDIR\uninstall.exe"

    ; File Association (.cryptpad)
    WriteRegStr HKCR "${REG_ASSOC_EXT}"                         "" "${REG_ASSOC_KEY}"
    WriteRegStr HKCR "${REG_ASSOC_KEY}"                         "" "${APP_NAME} File"
    WriteRegStr HKCR "${REG_ASSOC_KEY}\DefaultIcon"             "" "$INSTDIR\DA-CryptPadFile.ico"
    WriteRegStr HKCR "${REG_ASSOC_KEY}\shell\open\command"      "" '"$INSTDIR\${APP_EXE}" "%1"'

    ; Uninstall Registry Entry
    WriteRegStr   HKLM "${REG_UNINSTALL}" "DisplayName"          "${APP_NAME}"
    WriteRegStr   HKLM "${REG_UNINSTALL}" "DisplayVersion"       "${APP_VERSION}"
    WriteRegStr   HKLM "${REG_UNINSTALL}" "Publisher"            "${APP_PUBLISHER}"
    WriteRegStr   HKLM "${REG_UNINSTALL}" "URLInfoAbout"         "${APP_URL}"
    WriteRegStr   HKLM "${REG_UNINSTALL}" "InstallLocation"      "$INSTDIR"
    WriteRegStr   HKLM "${REG_UNINSTALL}" "UninstallString"      '"$INSTDIR\uninstall.exe"'
    WriteRegStr   HKLM "${REG_UNINSTALL}" "QuietUninstallString" '"$INSTDIR\uninstall.exe" /S'
    WriteRegStr   HKLM "${REG_UNINSTALL}" "DisplayIcon"          "$INSTDIR\DA-CryptPad.ico"
    WriteRegDWORD HKLM "${REG_UNINSTALL}" "NoModify"             1
    WriteRegDWORD HKLM "${REG_UNINSTALL}" "NoRepair"             1

    ; Write Uninstaller
    WriteUninstaller "$INSTDIR\uninstall.exe"
SectionEnd

Section $(DesktopShortcutDesc) SecDesktop
    CreateShortcut "$DESKTOP\${APP_NAME}.lnk" \
                   "$INSTDIR\${APP_EXE}" "" \
                   "$INSTDIR\DA-CryptPad.ico" 0
SectionEnd

; ===== Uninstaller Section =====

Section "Uninstall"
    ; Remove files
    Delete "$INSTDIR\${APP_EXE}"
    Delete "$INSTDIR\DA-CryptPad.ico"
    Delete "$INSTDIR\DA-CryptPadFile.ico"
    Delete "$INSTDIR\LICENSE"
    Delete "$INSTDIR\uninstall.exe"
    RMDir /r "$INSTDIR\jre"
    RMDir "$INSTDIR"

    ; Remove Start Menu shortcuts
    Delete "$SMPROGRAMS\${APP_NAME}\${APP_NAME}.lnk"
    Delete "$SMPROGRAMS\${APP_NAME}\Uninstall ${APP_NAME}.lnk"
    RMDir  "$SMPROGRAMS\${APP_NAME}"

    ; Remove Desktop shortcut (falls vorhanden)
    Delete "$DESKTOP\${APP_NAME}.lnk"

    ; Remove file association
    DeleteRegKey HKCR "${REG_ASSOC_EXT}"
    DeleteRegKey HKCR "${REG_ASSOC_KEY}"

    ; Remove Uninstall entry
    DeleteRegKey HKLM "${REG_UNINSTALL}"

    ; Remove AppData folder
    RMDir /r "$LOCALAPPDATA\${APP_NAME}"
SectionEnd

; ===== Language Strings =====
LangString DesktopShortcutDesc ${LANG_ENGLISH} "Create Desktop Shortcut"
LangString DesktopShortcutDesc ${LANG_GERMAN}  "Desktop-Verknüpfung erstellen"

; ===== Uninstaller Init =====
; /upgrade überspringt die Bestätigungsseite, zeigt aber den Fortschritt

Var IsUpgrade

Function un.onInit
    SetRegView 64
    ${GetParameters} $0
    ${GetOptions} $0 "/upgrade" $1
    ${If} ${Errors}
        StrCpy $IsUpgrade "0"
    ${Else}
        StrCpy $IsUpgrade "1"
        SetAutoClose true
    ${EndIf}
FunctionEnd

; Bestätigungsseite nur bei normaler Deinstallation anzeigen
Function un.ConfirmPre
    ${If} $IsUpgrade == "1"
        Abort
    ${EndIf}
FunctionEnd

; ===== Upgrade Logic =====
; Vorherige Installation erkennen und deinstallieren (sichtbar, automatisch)

Function .onInit
    ; 64-Bit-Prüfung
    ${IfNot} ${RunningX64}
        MessageBox MB_OK|MB_ICONSTOP "This application requires a 64-bit Windows."
        Abort
    ${EndIf}

    ; 64-Bit Registry verwenden (InnoSetup schreibt in SOFTWARE\, nicht SOFTWARE\WOW6432Node\)
    SetRegView 64

    ; NSIS-Vorgänger prüfen → /upgrade: Confirm-Dialog überspringen, Fortschritt sichtbar
    ReadRegStr $0 HKLM "${REG_UNINSTALL}" "UninstallString"
    ${If} $0 != ""
        ExecWait '$0 /upgrade _?=$INSTDIR'
        Return
    ${EndIf}

    ; InnoSetup-Vorgänger prüfen: HKLM zuerst, dann HKCU als Fallback
    ReadRegStr $0 HKLM "${REG_UNINSTALL}_is1" "UninstallString"
    ${If} $0 == ""
        ReadRegStr $0 HKCU "${REG_UNINSTALL}_is1" "UninstallString"
    ${EndIf}

    ${If} $0 != ""
        ExecWait '$0 /SILENT /NORESTART'
    ${EndIf}
FunctionEnd
