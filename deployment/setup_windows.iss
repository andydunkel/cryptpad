; DA-CryptPad Inno Setup Script

; ===== Defines =====
#define MyAppName "DA-CryptPad"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "DA-Software"
#define MyAppURL "https://www.da-software.net"
#define MyAppExeName "cryptpad.exe"

[Setup]
; Application Info
AppId={{67347003-2D90-4209-B9BA-EF949A5EC85B}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
AppCopyright={#MyAppPublisher}

; Version Info
VersionInfoVersion={#MyAppVersion}
VersionInfoCompany=DA-Software.net
VersionInfoDescription={#MyAppName}
VersionInfoProductName={#MyAppName}
VersionInfoProductVersion={#MyAppVersion}

; Installation Settings
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
LicenseFile=..\LICENSE
OutputDir=installer
OutputBaseFilename=cryptpad

; Compression
Compression=lzma2
SolidCompression=yes

; Architecture
ArchitecturesAllowed=x64
ArchitecturesInstallIn64BitMode=x64

; UI
WizardStyle=modern
ShowLanguageDialog=auto
LanguageDetectionMethod=uilanguage
ShowUndisplayableLanguages=false

; Associations
ChangesAssociations=true

; Code Signing
;SignTool=yubikey /d $q{#MyAppName}$q /du $q{#MyAppURL}$q /v $f
;SignedUninstaller=yes

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "german"; MessagesFile: "compiler:Languages\German.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
; Main Application
Source: "{#MyAppExeName}"; DestDir: "{app}"; Flags: overwritereadonly ignoreversion replacesameversion

; Bundled JRE
Source: "jre\*"; DestDir: "{app}\jre"; Flags: overwritereadonly ignoreversion replacesameversion recursesubdirs createallsubdirs

; Icons
Source: "..\res\DA-CryptPadFile.ico"; DestDir: "{app}"; Flags: overwritereadonly ignoreversion replacesameversion
Source: "..\res\DA-CryptPad.ico"; DestDir: "{app}"; Flags: overwritereadonly ignoreversion replacesameversion

; License
Source: "..\LICENSE"; DestDir: "{app}"; Flags: overwritereadonly ignoreversion replacesameversion

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"; IconFilename: "{app}\DA-CryptPad.ico"; IconIndex: 0
Name: "{group}\Uninstall {#MyAppName}"; Filename: "{uninstallexe}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"; IconFilename: "{app}\DA-CryptPad.ico"; Tasks: desktopicon

[Registry]
; File Association for .cryptpad files
Root: HKCR; Subkey: ".cryptpad"; ValueType: string; ValueData: "{#MyAppName}file"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "{#MyAppName}file"; ValueType: string; ValueData: "{#MyAppName} File"; Flags: uninsdeletekey
Root: HKCR; Subkey: "{#MyAppName}file\DefaultIcon"; ValueType: string; ValueData: "{app}\DA-CryptPadFile.ico"
Root: HKCR; Subkey: "{#MyAppName}file\shell\open\command"; ValueType: string; ValueData: """{app}\{#MyAppExeName}"" ""%1"""

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#MyAppName}}"; Flags: nowait postinstall skipifsilent

[UninstallDelete]
Type: filesandordirs; Name: "{app}"
Type: filesandordirs; Name: "{localappdata}\{#MyAppName}"

[Code]
function GetUninstallString(): String;
var
  sUnInstPath: String;
  sUnInstallString: String;
begin
  sUnInstPath := 'Software\Microsoft\Windows\CurrentVersion\Uninstall\{67347003-2D90-4209-B9BA-EF949A5EC85B}_is1';
  sUnInstallString := '';
  if not RegQueryStringValue(HKLM, sUnInstPath, 'UninstallString', sUnInstallString) then
    RegQueryStringValue(HKCU, sUnInstPath, 'UninstallString', sUnInstallString);
  Result := sUnInstallString;
end;

function IsUpgrade(): Boolean;
begin
  Result := (GetUninstallString() <> '');
end;

function UnInstallOldVersion(): Integer;
var
  sUnInstallString: String;
  iResultCode: Integer;
begin
  // Return Values:
  // 1 - uninstall string is empty
  // 2 - error executing the UnInstallString
  // 3 - successfully executed the UnInstallString
  
  Result := 0;
  sUnInstallString := GetUninstallString();
  
  if sUnInstallString <> '' then begin
    sUnInstallString := RemoveQuotes(sUnInstallString);
    if Exec(sUnInstallString, '/SILENT /NORESTART /SUPPRESSMSGBOXES','', SW_HIDE, ewWaitUntilTerminated, iResultCode) then
      Result := 3
    else
      Result := 2;
  end else
    Result := 1;
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if (CurStep=ssInstall) then
  begin
    if (IsUpgrade()) then
    begin
      UnInstallOldVersion();
    end;
  end;
end;