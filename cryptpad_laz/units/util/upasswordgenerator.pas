unit upasswordgenerator;

{$mode objfpc}{$H+}
{$codepage UTF8}

interface

uses
  Classes, SysUtils, LazUTF8, ucryptpadcrypto;

type
  TPasswordGenerator = class
  private
    FSpecialCharsAllowed: Boolean;
    FNumbersAllowed: Boolean;
    FCapitalsAllowed: Boolean;
    function BuildCharacterPool: UTF8String;
  public
    function GeneratePassword(ALength: Integer): UTF8String;
    property SpecialCharsAllowed: Boolean read FSpecialCharsAllowed write FSpecialCharsAllowed;
    property NumbersAllowed: Boolean read FNumbersAllowed write FNumbersAllowed;
    property CapitalsAllowed: Boolean read FCapitalsAllowed write FCapitalsAllowed;
  end;

implementation

const
  LOWERCASE_CHARS = 'abcdefghijklmnopqrstuvwxyz';
  UPPERCASE_CHARS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
  NUMBER_CHARS = '0123456789';
  SPECIAL_CHARS = '!"§$%&/()[]{}+*#''-.,><|';

function TPasswordGenerator.BuildCharacterPool: UTF8String;
begin
  Result := LOWERCASE_CHARS;
  if FCapitalsAllowed then
    Result := Result + UPPERCASE_CHARS;
  if FNumbersAllowed then
    Result := Result + NUMBER_CHARS;
  if FSpecialCharsAllowed then
    Result := Result + SPECIAL_CHARS;
end;

function TPasswordGenerator.GeneratePassword(ALength: Integer): UTF8String;
var
  pool: UTF8String;
  poolCharCount: Integer;
  randBuf: TBytes;
  i: Integer;
  idx: Integer;
begin
  if ALength <= 0 then
    raise Exception.Create('Password length must be greater than 0');

  pool := BuildCharacterPool;
  if pool = '' then
    pool := LOWERCASE_CHARS;
  poolCharCount := UTF8Length(pool);

  // Cryptographically strong randomness, matching the Java version's use of SecureRandom.
  // Character selection is codepoint-based (UTF8Copy), not byte-based, since the special
  // character set includes non-ASCII characters (e.g. section sign).
  randBuf := RandomBytes(ALength * 4);

  Result := '';
  for i := 0 to ALength - 1 do
  begin
    idx := (PByte(@randBuf[i * 4])^ or (PByte(@randBuf[i * 4 + 1])^ shl 8) or
            (PByte(@randBuf[i * 4 + 2])^ shl 16)) mod poolCharCount;
    Result := Result + UTF8Copy(pool, idx + 1, 1);
  end;
end;

end.
