program pwenctest;

{$mode objfpc}{$H+}

uses
  SysUtils, usha256;

function HexToBytes(const Hex: string): TBytes;
var
  i, n: Integer;
begin
  n := Length(Hex) div 2;
  SetLength(Result, n);
  for i := 0 to n - 1 do
    Result[i] := StrToInt('$' + Copy(Hex, i * 2 + 1, 2));
end;

function BytesToHex(const B: TBytes): string;
var
  i: Integer;
begin
  Result := '';
  for i := 0 to Length(B) - 1 do
    Result := Result + LowerCase(IntToHex(B[i], 2));
end;

var
  dk: TBytes;
begin
  // Candidate A: Latin-1 single-byte encoding of 'paßwort'
  dk := PBKDF2_HMACSHA256(HexToBytes('7061df776f7274'), HexToBytes('73616c74'), 1, 32);
  WriteLn('latin1 =', BytesToHex(dk));

  // Candidate B: UTF-8 encoding of 'paßwort'
  dk := PBKDF2_HMACSHA256(HexToBytes('7061c39f776f7274'), HexToBytes('73616c74'), 1, 32);
  WriteLn('utf8   =', BytesToHex(dk));

  // Candidate C: 7-bit truncation of Latin-1 byte
  dk := PBKDF2_HMACSHA256(HexToBytes('70615f776f7274'), HexToBytes('73616c74'), 1, 32);
  WriteLn('trunc7 =', BytesToHex(dk));
end.
