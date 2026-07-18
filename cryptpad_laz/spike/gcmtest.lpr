program gcmtest;

{$mode objfpc}{$H+}

uses
  SysUtils, uaes256, uaesgcm;

function HexToKey(const Hex: string): TAES256Key;
var
  i: Integer;
begin
  for i := 0 to 31 do
    Result[i] := StrToInt('$' + Copy(Hex, i * 2 + 1, 2));
end;

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

function TagToHex(const T: TAESBlock): string;
var
  i: Integer;
begin
  Result := '';
  for i := 0 to 15 do
    Result := Result + LowerCase(IntToHex(T[i], 2));
end;

procedure RunCase(const Name, KeyHex, IVHex, PtHex: string);
var
  key: TAES256Key;
  iv, pt, ct: TBytes;
  tag: TAESBlock;
begin
  key := HexToKey(KeyHex);
  iv := HexToBytes(IVHex);
  pt := HexToBytes(PtHex);
  ct := AES256GCM_Encrypt(key, iv, pt, tag);
  WriteLn(Name, '_ct=', BytesToHex(ct));
  WriteLn(Name, '_tag=', TagToHex(tag));
end;

const
  KeyHex = '0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20';
  IVHex  = '0102030405060708090a0b0c';

begin
  RunCase('empty', KeyHex, IVHex, '');
  RunCase('oneblock', KeyHex, IVHex, '000102030405060708090a0b0c0d0e0f');
  RunCase('multiblock', KeyHex, IVHex,
    '000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425');
  RunCase('allzero', StringOfChar('0', 64), StringOfChar('0', 24), StringOfChar('0', 32));
end.
