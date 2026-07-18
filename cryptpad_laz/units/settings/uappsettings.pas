unit uappsettings;

{$mode objfpc}{$H+}
{$codepage UTF8}

interface

uses
  Classes, SysUtils, uappinfo;

function GetLanguage: string; // 'en', 'de', or 'system'
procedure SetLanguage(const Lang: string);

function GetTheme: string;
procedure SetTheme(const Theme: string);
function IsDarkTheme: Boolean;

function GetSettingsFileLocation: string;

procedure AddRecentFile(const FileName: string);
function GetRecentFiles: TStringArray;
procedure ClearRecentFiles;

procedure ResetToDefaults;
procedure SaveSettings;

implementation

const
  SETTINGS_FILENAME = 'settings.properties';
  KEY_LANGUAGE = 'language';
  KEY_THEME = 'theme';
  KEY_RECENT_FILES = 'recent.files';
  DEFAULT_LANGUAGE = 'system';
  DEFAULT_THEME = 'System';
  MAX_RECENT_FILES = 10;
  RECENT_FILES_SEPARATOR = '|';

var
  Props: TStringList;
  Loaded: Boolean = False;

function GetSettingsDir: string;
var
  configDir: string;
begin
  {$IFDEF WINDOWS}
  configDir := GetEnvironmentVariable('APPDATA');
  if configDir = '' then
    configDir := GetEnvironmentVariable('USERPROFILE');
  {$ELSE}
  configDir := GetEnvironmentVariable('HOME') + PathDelim + '.config';
  {$ENDIF}
  Result := IncludeTrailingPathDelimiter(configDir) + APP_NAME;
  ForceDirectories(Result);
end;

function GetSettingsPath: string;
begin
  Result := IncludeTrailingPathDelimiter(GetSettingsDir) + SETTINGS_FILENAME;
end;

procedure InitDefaults;
begin
  Props.Values[KEY_LANGUAGE] := DEFAULT_LANGUAGE;
  Props.Values[KEY_THEME] := DEFAULT_THEME;
end;

procedure EnsureLoaded;
var
  path: string;
begin
  if Loaded then Exit;
  Loaded := True;

  path := GetSettingsPath;
  if FileExists(path) then
  begin
    try
      Props.LoadFromFile(path);
    except
      InitDefaults;
    end;
  end
  else
  begin
    InitDefaults;
    SaveSettings;
  end;
end;

procedure SaveSettings;
begin
  Props.SaveToFile(GetSettingsPath);
end;

procedure ResetToDefaults;
begin
  Props.Clear;
  InitDefaults;
  SaveSettings;
end;

function GetLanguage: string;
begin
  EnsureLoaded;
  Result := Props.Values[KEY_LANGUAGE];
  if Result = '' then
    Result := DEFAULT_LANGUAGE;
end;

procedure SetLanguage(const Lang: string);
begin
  EnsureLoaded;
  Props.Values[KEY_LANGUAGE] := Lang;
  SaveSettings;
end;

function GetTheme: string;
begin
  EnsureLoaded;
  Result := Props.Values[KEY_THEME];
  if Result = '' then
    Result := DEFAULT_THEME;
end;

procedure SetTheme(const Theme: string);
begin
  EnsureLoaded;
  Props.Values[KEY_THEME] := Theme;
  SaveSettings;
end;

function IsDarkTheme: Boolean;
var
  theme: string;
begin
  theme := GetTheme;
  Result := (Pos('Dark', theme) > 0) or (theme = 'FlatLaf Darcula');
end;

function GetSettingsFileLocation: string;
begin
  Result := GetSettingsPath;
end;

function JoinWithSeparator(List: TStringList): string;
var
  i: Integer;
begin
  Result := '';
  for i := 0 to List.Count - 1 do
  begin
    if i > 0 then
      Result := Result + RECENT_FILES_SEPARATOR;
    Result := Result + List[i];
  end;
end;

procedure AddRecentFile(const FileName: string);
var
  files: TStringArray;
  newList: TStringList;
  i: Integer;
begin
  if not FileExists(FileName) then Exit;

  files := GetRecentFiles;
  newList := TStringList.Create;
  try
    newList.Add(FileName);
    for i := 0 to High(files) do
      if not SameFileName(files[i], FileName) then
        newList.Add(files[i]);

    while newList.Count > MAX_RECENT_FILES do
      newList.Delete(newList.Count - 1);

    EnsureLoaded;
    Props.Values[KEY_RECENT_FILES] := JoinWithSeparator(newList);
    SaveSettings;
  finally
    newList.Free;
  end;
end;

function GetRecentFiles: TStringArray;
var
  raw: string;
  parts: TStringArray;
  kept: TStringList;
  i: Integer;
begin
  EnsureLoaded;
  raw := Props.Values[KEY_RECENT_FILES];
  SetLength(Result, 0);
  if raw = '' then Exit;

  parts := raw.Split(RECENT_FILES_SEPARATOR);
  kept := TStringList.Create;
  try
    for i := 0 to High(parts) do
      if (Trim(parts[i]) <> '') and FileExists(Trim(parts[i])) then
        kept.Add(Trim(parts[i]));

    if kept.Count <> Length(parts) then
    begin
      Props.Values[KEY_RECENT_FILES] := JoinWithSeparator(kept);
      SaveSettings;
    end;

    SetLength(Result, kept.Count);
    for i := 0 to kept.Count - 1 do
      Result[i] := kept[i];
  finally
    kept.Free;
  end;
end;

procedure ClearRecentFiles;
begin
  EnsureLoaded;
  Props.Values[KEY_RECENT_FILES] := '';
  SaveSettings;
end;

initialization
  Props := TStringList.Create;
  Props.NameValueSeparator := '=';

finalization
  Props.Free;

end.
