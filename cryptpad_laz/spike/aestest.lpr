program aestest;

{$mode objfpc}{$H+}

uses
  SysUtils, uaes256;

function HexToKey(const Hex: string): TAES256Key;
var
  i: Integer;
begin
  for i := 0 to 31 do
    Result[i] := StrToInt('$' + Copy(Hex, i * 2 + 1, 2));
end;

function HexToBlock(const Hex: string): TAESBlock;
var
  i: Integer;
begin
  for i := 0 to 15 do
    Result[i] := StrToInt('$' + Copy(Hex, i * 2 + 1, 2));
end;

function BlockToHex(const B: TAESBlock): string;
var
  i: Integer;
begin
  Result := '';
  for i := 0 to 15 do
    Result := Result + LowerCase(IntToHex(B[i], 2));
end;

var
  ctx: TAES256Context;
  key: TAES256Key;
  pt, ct: TAESBlock;

begin
  key := HexToKey('000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f');
  pt := HexToBlock('00112233445566778899aabbccddeeff');
  AES256Init(ctx, key);
  AES256EncryptBlock(ctx, pt, ct);
  WriteLn('aes_case1=', BlockToHex(ct));

  key := HexToKey(StringOfChar('0', 64)); // all-zero key
  pt := HexToBlock(StringOfChar('0', 32)); // all-zero plaintext
  AES256Init(ctx, key);
  AES256EncryptBlock(ctx, pt, ct);
  WriteLn('aes_case2=', BlockToHex(ct));

  key := HexToKey('0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20');
  pt := HexToBlock('0102030405060708090a0b0c0d0e0f10');
  AES256Init(ctx, key);
  AES256EncryptBlock(ctx, pt, ct);
  WriteLn('aes_case3=', BlockToHex(ct));
end.
