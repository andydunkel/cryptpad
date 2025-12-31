[Files]
;DestDir: {app}; Source: files\*; Flags: recursesubdirs overwritereadonly ignoreversion replacesameversion

Source: jCryptPad.exe; DestDir: {app}; Flags: overwritereadonly ignoreversion replacesameversion

Source: ..\doc\gpl.txt; DestDir: {app}; Flags: overwritereadonly ignoreversion replacesameversion
Source: ..\resources\icon\jCryptPadFile.ico; DestDir: {app}; Flags: overwritereadonly ignoreversion replacesameversion
Source: ..\resources\icon\jCryptPad.ico; DestDir: {app}; Flags: overwritereadonly ignoreversion replacesameversion

[Icons]
Name: {group}\jCryptPad; Filename: {app}\jCryptPad.exe; WorkingDir: {app}; IconFilename: {app}\jCryptPad.ico; IconIndex: 0; Languages: 
Name: {group}\Uninstall; Filename: {uninstallexe};

[Run]
Filename: {app}\jCryptPad.exe; WorkingDir: {app}; Flags: nowait postinstall; Description: jCryptPad

[Setup]
AppCopyright=Andy Dunkel
AppName=jCryptPad
AppVerName=jCryptPad 1.0.0
DefaultDirName={pf}\jCryptPad
AppID={{BA2A0D41-ABAC-4A7B-861D-E13578246922}
VersionInfoVersion=1.0.0
VersionInfoCompany=AndyDunkel.net
VersionInfoDescription=jCryptPad
LanguageDetectionMethod=uilanguage
DefaultGroupName=jCryptPad
ShowUndisplayableLanguages=false
OutputBaseFilename=jcryptpad
VersionInfoProductName=jCryptPad
VersionInfoProductVersion=1.0.0
LicenseFile=../doc/gpl.txt
AppPublisher=Andy Dunkel
AppPublisherURL=http://andydunkel.net
AppSupportURL=http://andydunkel.net
AppUpdatesURL=http://andydunkel.net
ChangesAssociations=true

[Registry]
Root: HKCR; Subkey: .jcryptpad; ValueType: string; ValueData: jcryptpadfile; Flags: uninsdeletevalue
Root: HKCR; Subkey: jcryptpadfile; ValueType: string; ValueData: jCryptPad; Flags: uninsdeletekey
Root: HKCR; Subkey: jcryptpadfile\DefaultIcon; ValueType: string; ValueData: {app}\jCryptPadFile.ico
Root: HKCR; Subkey: jcryptpadfile\shell\open\command; ValueType: string; ValueData: """{app}\jCryptPad.exe"" ""%1"""
                                                                                          
[UninstallDelete]
Name: {app}; Type: filesandordirs

[Code]
function GetUninstallString(): String;
var
  sUnInstPath: String;
  sUnInstallString: String;
begin
  sUnInstPath := 'Software\Microsoft\Windows\CurrentVersion\Uninstall\{94ccae57-7cb2-4978-b109-daaf110f34d4}_is1';
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








