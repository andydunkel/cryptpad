program settingstest;

{$mode objfpc}{$H+}
{$codepage UTF8}

uses
  SysUtils, uappsettings;

var
  files: TStringArray;
  i: Integer;
begin
  WriteLn('settings path=', GetSettingsFileLocation);
  WriteLn('language=', GetLanguage);
  WriteLn('theme=', GetTheme);

  SetLanguage('de');
  SetTheme('Dark');
  WriteLn('after set: language=', GetLanguage, ' theme=', GetTheme, ' isDark=', IsDarkTheme);

  AddRecentFile(ParamStr(0)); // exe itself definitely exists
  files := GetRecentFiles;
  WriteLn('recent files count=', Length(files));
  for i := 0 to High(files) do
    WriteLn('  [', i, ']=', files[i]);

  ClearRecentFiles;
  files := GetRecentFiles;
  WriteLn('after clear, count=', Length(files));

  ResetToDefaults;
  WriteLn('after reset: language=', GetLanguage, ' theme=', GetTheme);
end.
