unit umessages;

{$mode objfpc}{$H+}

interface

uses
  Classes, SysUtils;

procedure SetLanguage(const LangCode: string); // 'en', 'de', or '' for default (en)
function GetString(const Key: string): string;
function GetStringF(const Key: string; const Args: array of string): string;

implementation

var
  Messages: TStringList;
  LangDir: string;

function Latin1ToUtf8(const S: RawByteString): string;
var
  i: Integer;
  b: Byte;
begin
  Result := '';
  for i := 1 to Length(S) do
  begin
    b := Byte(S[i]);
    if b < $80 then
      Result := Result + Chr(b)
    else
      Result := Result + Chr($C0 or (b shr 6)) + Chr($80 or (b and $3F));
  end;
end;

function UnescapeJavaProperties(const S: string): string;
var
  i, len: Integer;
  c: Char;
  hex: string;
  code: Integer;
begin
  Result := '';
  len := Length(S);
  i := 1;
  while i <= len do
  begin
    c := S[i];
    if (c = '\') and (i < len) then
    begin
      Inc(i);
      case S[i] of
        'n': Result := Result + #10;
        't': Result := Result + #9;
        'r': Result := Result + #13;
        '\': Result := Result + '\';
        ':': Result := Result + ':';
        '=': Result := Result + '=';
        'u':
          begin
            hex := Copy(S, i + 1, 4);
            code := StrToIntDef('$' + hex, -1);
            if code >= 0 then
            begin
              // encode codepoint as UTF-8 (BMP range is sufficient for this app's strings)
              if code < $80 then
                Result := Result + Chr(code)
              else if code < $800 then
                Result := Result + Chr($C0 or (code shr 6)) + Chr($80 or (code and $3F))
              else
                Result := Result + Chr($E0 or (code shr 12)) + Chr($80 or ((code shr 6) and $3F)) +
                          Chr($80 or (code and $3F));
              Inc(i, 4);
            end;
          end;
      else
        Result := Result + S[i];
      end;
      Inc(i);
    end
    else
    begin
      Result := Result + c;
      Inc(i);
    end;
  end;
end;

procedure LoadPropertiesFile(const Path: string; IsLatin1: Boolean; List: TStringList);
var
  raw: TStringList;
  i: Integer;
  line, key, value: string;
  eqPos: Integer;
begin
  raw := TStringList.Create;
  try
    raw.LoadFromFile(Path);
    for i := 0 to raw.Count - 1 do
    begin
      line := raw[i];
      if IsLatin1 then
        line := Latin1ToUtf8(RawByteString(line));

      line := Trim(line);
      if (line = '') or (line[1] = '#') or (line[1] = '!') then
        Continue;

      eqPos := Pos('=', line);
      if eqPos = 0 then
        Continue;

      key := Trim(Copy(line, 1, eqPos - 1));
      value := Trim(Copy(line, eqPos + 1, Length(line)));
      value := UnescapeJavaProperties(value);

      List.Values[key] := value;
    end;
  finally
    raw.Free;
  end;
end;

procedure SetLanguage(const LangCode: string);
var
  path: string;
begin
  Messages.Clear;

  // English is the base bundle (no suffix), plain ASCII/UTF-8 in this project.
  // A missing/misplaced resource folder (e.g. a debug build output directory
  // that isn't next to lang\) must not crash the whole app at unit-init time;
  // GetString() already degrades gracefully to "!key!" when a key is unknown.
  try
    path := LangDir + 'Messages.properties';
    if FileExists(path) then
      LoadPropertiesFile(path, False, Messages);
  except
  end;

  if LangCode = 'de' then
  begin
    path := LangDir + 'Messages_de.properties';
    // Originally ISO-8859-1 (Java's default .properties encoding); this repo's copy has
    // since been re-saved as UTF-8, so it no longer needs Latin1->UTF8 recoding on load.
    try
      if FileExists(path) then
        LoadPropertiesFile(path, False, Messages);
    except
    end;
  end;
end;

function GetString(const Key: string): string;
var
  idx: Integer;
begin
  idx := Messages.IndexOfName(Key);
  if idx = -1 then
    Result := '!' + Key + '!'
  else
    Result := Messages.ValueFromIndex[idx];
end;

function GetStringF(const Key: string; const Args: array of string): string;
var
  s: string;
  i: Integer;
begin
  s := GetString(Key);
  for i := 0 to High(Args) do
    s := StringReplace(s, '{' + IntToStr(i) + '}', Args[i], [rfReplaceAll]);
  Result := s;
end;

initialization
  Messages := TStringList.Create;
  Messages.NameValueSeparator := '=';
  LangDir := ExtractFilePath(ParamStr(0)) + 'lang' + PathDelim;
  SetLanguage('en');

finalization
  Messages.Free;

end.
