[Files]
;DestDir: {app}; Source: files\*; Flags: recursesubdirs overwritereadonly ignoreversion replacesameversion

Source: DA-CryptPad.exe; DestDir: {app}; Flags: overwritereadonly ignoreversion replacesameversion

Source: ..\LICENSE; DestDir: {app}; Flags: overwritereadonly ignoreversion replacesameversion
Source: ..\res\icon\DA-CryptPadFile.ico; DestDir: {app}; Flags: overwritereadonly ignoreversion replacesameversion
Source: ..\res\icon\DA-CryptPad.ico; DestDir: {app}; Flags: overwritereadonly ignoreversion replacesameversion

[Icons]
Name: {group}\DA-CryptPad; Filename: {app}\DA-CryptPad.exe; WorkingDir: {app}; IconFilename: {app}\DA-CryptPad.ico; IconIndex: 0; Languages: 
Name: {group}\Uninstall; Filename: {uninstallexe};

[Run]
Filename: {app}\DA-CryptPad.exe; WorkingDir: {app}; Flags: nowait postinstall; Description: DA-CryptPad

[Setup]
AppCopyright=Andy Dunkel
AppName=DA-CryptPad
AppVerName=DA-CryptPad 1.0.0
DefaultDirName={pf}\DA-CryptPad
AppID={{67347003-2D90-4209-B9BA-EF949A5EC85B}
VersionInfoVersion=1.0.0
VersionInfoCompany=AndyDunkel.net
VersionInfoDescription=DA-CryptPad
LanguageDetectionMethod=uilanguage
DefaultGroupName=DA-CryptPad
ShowUndisplayableLanguages=false
OutputBaseFilename=DA-CryptPad
VersionInfoProductName=DA-CryptPad
VersionInfoProductVersion=1.0.0
LicenseFile=../LICENSE
AppPublisher=Andy Dunkel
AppPublisherURL=http://andydunkel.net
AppSupportURL=http://andydunkel.net
AppUpdatesURL=http://andydunkel.net
ChangesAssociations=true
SignTool=yubikey /d $qDA-CryptPad$q /du $qhttps://www.da-software.net$q /v $f
SignedUninstaller=yes

[Registry]
Root: HKCR; Subkey: .DA-CryptPad; ValueType: string; ValueData: DA-CryptPadfile; Flags: uninsdeletevalue
Root: HKCR; Subkey: DA-CryptPadfile; ValueType: string; ValueData: DA-CryptPad; Flags: uninsdeletekey
Root: HKCR; Subkey: DA-CryptPadfile\DefaultIcon; ValueType: string; ValueData: {app}\DA-CryptPadFile.ico
Root: HKCR; Subkey: DA-CryptPadfile\shell\open\command; ValueType: string; ValueData: """{app}\DA-CryptPad.exe"" ""%1"""
                                                                                          
[UninstallDelete]
Name: {app}; Type: filesandordirs

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


/////////////////////////////////////////////////////////////////////
function IsUpgrade(): Boolean;
begin
  Result := (GetUninstallString() <> '');
end;


/////////////////////////////////////////////////////////////////////
function UnInstallOldVersion(): Integer;
var
  sUnInstallString: String;
  iResultCode: Integer;
begin
// Return Values:
// 1 - uninstall string is empty
// 2 - error executing the UnInstallString
// 3 - successfully executed the UnInstallString

  // default return value
  Result := 0;

  // get the uninstall string of the old app
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

/////////////////////////////////////////////////////////////////////
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








