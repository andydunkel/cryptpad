program sha256test;

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

function DigestToHex(const D: TSHA256Digest): string;
var
  i: Integer;
begin
  Result := '';
  for i := 0 to 31 do
    Result := Result + LowerCase(IntToHex(D[i], 2));
end;

function Utf8ToBytes(const S: string): TBytes;
begin
  Result := TBytes(TEncoding.UTF8.GetBytes(S));
end;

var
  d: TSHA256Digest;
  b: TBytes;
  dk: TBytes;

begin
  // 1: sha256 empty
  b := HexToBytes('');
  d := SHA256Bytes(b);
  WriteLn('sha256_empty=', DigestToHex(d));

  // 2: sha256 "abc"
  b := HexToBytes('616263');
  d := SHA256Bytes(b);
  WriteLn('sha256_abc=', DigestToHex(d));

  // 3: sha256 of a 130-byte pattern (spans multiple 64-byte blocks)
  b := HexToBytes(StringOfChar('a', 260)); // 130 bytes of 0xaa
  d := SHA256Bytes(b);
  WriteLn('sha256_130xAA=', DigestToHex(d));

  // 4: HMAC-SHA256, key=0b*20, data="Hi There"
  d := HMACSHA256Bytes(HexToBytes('0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b'),
                        Utf8ToBytes('Hi There'));
  WriteLn('hmac_case1=', DigestToHex(d));

  // 5: HMAC-SHA256, key="key" (short), long data
  d := HMACSHA256Bytes(Utf8ToBytes('key'), Utf8ToBytes('The quick brown fox jumps over the lazy dog, twice, to make it longer than one block.'));
  WriteLn('hmac_case2=', DigestToHex(d));

  // 6: HMAC-SHA256, key longer than 64 bytes (forces key hashing path)
  d := HMACSHA256Bytes(HexToBytes(StringOfChar('c', 200)), Utf8ToBytes('data'));
  WriteLn('hmac_case3=', DigestToHex(d));

  // 7: PBKDF2-HMAC-SHA256, password="password", salt="salt", iter=1, dkLen=32
  dk := PBKDF2_HMACSHA256(Utf8ToBytes('password'), Utf8ToBytes('salt'), 1, 32);
  WriteLn('pbkdf2_case1=', BytesToHex(dk));

  // 8: PBKDF2-HMAC-SHA256, iter=4096, dkLen=32
  dk := PBKDF2_HMACSHA256(Utf8ToBytes('password'), Utf8ToBytes('salt'), 4096, 32);
  WriteLn('pbkdf2_case2=', BytesToHex(dk));

  // 9: PBKDF2 matching real app parameters: iter=100000, dkLen=32, random-ish salt
  dk := PBKDF2_HMACSHA256(Utf8ToBytes('correct horse battery staple'),
                            HexToBytes('0102030405060708090a0b0c0d0e0f10'), 100000, 32);
  WriteLn('pbkdf2_case3=', BytesToHex(dk));

  // 10: PBKDF2 with dkLen=64 (spans two SHA-256 blocks, exercises blockCount>1 path)
  dk := PBKDF2_HMACSHA256(Utf8ToBytes('p'), Utf8ToBytes('s'), 10, 64);
  WriteLn('pbkdf2_case4=', BytesToHex(dk));
end.
