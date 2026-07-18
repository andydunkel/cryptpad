unit usha256;

{$mode objfpc}{$H+}

interface

uses
  Classes, SysUtils;

type
  TSHA256Digest = array[0..31] of Byte;

  TSHA256Context = record
    State: array[0..7] of UInt32;
    BitLen: UInt64;
    Buffer: array[0..63] of Byte;
    BufLen: Integer;
  end;

procedure SHA256Init(out Ctx: TSHA256Context);
procedure SHA256Update(var Ctx: TSHA256Context; const Data; DataLen: PtrUInt);
procedure SHA256Final(var Ctx: TSHA256Context; out Digest: TSHA256Digest);

function SHA256Buf(const Data; DataLen: PtrUInt): TSHA256Digest;
function SHA256Bytes(const Data: TBytes): TSHA256Digest;

// HMAC-SHA256
procedure HMACSHA256(const Key; KeyLen: PtrUInt; const Data; DataLen: PtrUInt;
  out Digest: TSHA256Digest);
function HMACSHA256Bytes(const Key, Data: TBytes): TSHA256Digest;

// PBKDF2-HMAC-SHA256. Password/Salt are treated as raw byte buffers.
function PBKDF2_HMACSHA256(const Password: TBytes; const Salt: TBytes;
  Iterations: Integer; DKLenBytes: Integer): TBytes;

implementation

const
  K: array[0..63] of UInt32 = (
    $428a2f98, $71374491, $b5c0fbcf, $e9b5dba5, $3956c25b, $59f111f1, $923f82a4, $ab1c5ed5,
    $d807aa98, $12835b01, $243185be, $550c7dc3, $72be5d74, $80deb1fe, $9bdc06a7, $c19bf174,
    $e49b69c1, $efbe4786, $0fc19dc6, $240ca1cc, $2de92c6f, $4a7484aa, $5cb0a9dc, $76f988da,
    $983e5152, $a831c66d, $b00327c8, $bf597fc7, $c6e00bf3, $d5a79147, $06ca6351, $14292967,
    $27b70a85, $2e1b2138, $4d2c6dfc, $53380d13, $650a7354, $766a0abb, $81c2c92e, $92722c85,
    $a2bfe8a1, $a81a664b, $c24b8b70, $c76c51a3, $d192e819, $d6990624, $f40e3585, $106aa070,
    $19a4c116, $1e376c08, $2748774c, $34b0bcb5, $391c0cb3, $4ed8aa4a, $5b9cca4f, $682e6ff3,
    $748f82ee, $78a5636f, $84c87814, $8cc70208, $90befffa, $a4506ceb, $bef9a3f7, $c67178f2
  );

function ROTR(x: UInt32; n: Byte): UInt32; inline;
begin
  Result := (x shr n) or (x shl (32 - n));
end;

procedure SHA256Init(out Ctx: TSHA256Context);
begin
  FillChar(Ctx, SizeOf(Ctx), 0);
  Ctx.State[0] := $6a09e667;
  Ctx.State[1] := $bb67ae85;
  Ctx.State[2] := $3c6ef372;
  Ctx.State[3] := $a54ff53a;
  Ctx.State[4] := $510e527f;
  Ctx.State[5] := $9b05688c;
  Ctx.State[6] := $1f83d9ab;
  Ctx.State[7] := $5be0cd19;
end;

procedure SHA256Transform(var Ctx: TSHA256Context; const Block: array of Byte);
var
  W: array[0..63] of UInt32;
  a, b, c, d, e, f, g, h, t1, t2: UInt32;
  i: Integer;
begin
  for i := 0 to 15 do
    W[i] := (UInt32(Block[i * 4]) shl 24) or (UInt32(Block[i * 4 + 1]) shl 16) or
            (UInt32(Block[i * 4 + 2]) shl 8) or UInt32(Block[i * 4 + 3]);

  for i := 16 to 63 do
  begin
    W[i] := (ROTR(W[i-2], 17) xor ROTR(W[i-2], 19) xor (W[i-2] shr 10)) + W[i-7] +
            (ROTR(W[i-15], 7) xor ROTR(W[i-15], 18) xor (W[i-15] shr 3)) + W[i-16];
  end;

  a := Ctx.State[0]; b := Ctx.State[1]; c := Ctx.State[2]; d := Ctx.State[3];
  e := Ctx.State[4]; f := Ctx.State[5]; g := Ctx.State[6]; h := Ctx.State[7];

  for i := 0 to 63 do
  begin
    t1 := h + (ROTR(e,6) xor ROTR(e,11) xor ROTR(e,25)) + ((e and f) xor ((not e) and g)) + K[i] + W[i];
    t2 := (ROTR(a,2) xor ROTR(a,13) xor ROTR(a,22)) + ((a and b) xor (a and c) xor (b and c));
    h := g; g := f; f := e; e := d + t1;
    d := c; c := b; b := a; a := t1 + t2;
  end;

  Ctx.State[0] := Ctx.State[0] + a;
  Ctx.State[1] := Ctx.State[1] + b;
  Ctx.State[2] := Ctx.State[2] + c;
  Ctx.State[3] := Ctx.State[3] + d;
  Ctx.State[4] := Ctx.State[4] + e;
  Ctx.State[5] := Ctx.State[5] + f;
  Ctx.State[6] := Ctx.State[6] + g;
  Ctx.State[7] := Ctx.State[7] + h;
end;

procedure SHA256Update(var Ctx: TSHA256Context; const Data; DataLen: PtrUInt);
var
  p: PByte;
  n: PtrUInt;
begin
  p := PByte(@Data);
  Ctx.BitLen := Ctx.BitLen + (UInt64(DataLen) * 8);

  while DataLen > 0 do
  begin
    n := 64 - PtrUInt(Ctx.BufLen);
    if n > DataLen then n := DataLen;
    Move(p^, Ctx.Buffer[Ctx.BufLen], n);
    Inc(Ctx.BufLen, n);
    Inc(p, n);
    Dec(DataLen, n);

    if Ctx.BufLen = 64 then
    begin
      SHA256Transform(Ctx, Ctx.Buffer);
      Ctx.BufLen := 0;
    end;
  end;
end;

procedure SHA256Final(var Ctx: TSHA256Context; out Digest: TSHA256Digest);
var
  finalBitLen: UInt64;
  i: Integer;
begin
  finalBitLen := Ctx.BitLen;

  Ctx.Buffer[Ctx.BufLen] := $80;
  Inc(Ctx.BufLen);

  if Ctx.BufLen > 56 then
  begin
    while Ctx.BufLen < 64 do
    begin
      Ctx.Buffer[Ctx.BufLen] := 0;
      Inc(Ctx.BufLen);
    end;
    SHA256Transform(Ctx, Ctx.Buffer);
    Ctx.BufLen := 0;
  end;

  while Ctx.BufLen < 56 do
  begin
    Ctx.Buffer[Ctx.BufLen] := 0;
    Inc(Ctx.BufLen);
  end;

  for i := 0 to 7 do
    Ctx.Buffer[56 + i] := Byte(finalBitLen shr ((7 - i) * 8));

  SHA256Transform(Ctx, Ctx.Buffer);

  for i := 0 to 7 do
  begin
    Digest[i*4]   := Byte(Ctx.State[i] shr 24);
    Digest[i*4+1] := Byte(Ctx.State[i] shr 16);
    Digest[i*4+2] := Byte(Ctx.State[i] shr 8);
    Digest[i*4+3] := Byte(Ctx.State[i]);
  end;
end;

function SHA256Buf(const Data; DataLen: PtrUInt): TSHA256Digest;
var
  Ctx: TSHA256Context;
begin
  SHA256Init(Ctx);
  SHA256Update(Ctx, Data, DataLen);
  SHA256Final(Ctx, Result);
end;

function SHA256Bytes(const Data: TBytes): TSHA256Digest;
begin
  if Length(Data) = 0 then
    Result := SHA256Buf(Pointer(nil)^, 0)
  else
    Result := SHA256Buf(Data[0], Length(Data));
end;

procedure HMACSHA256(const Key; KeyLen: PtrUInt; const Data; DataLen: PtrUInt;
  out Digest: TSHA256Digest);
var
  keyBlock: array[0..63] of Byte;
  ipad, opad: array[0..63] of Byte;
  innerDigest: TSHA256Digest;
  ctx: TSHA256Context;
  i: Integer;
  keyDigest: TSHA256Digest;
  pk: PByte;
begin
  FillChar(keyBlock, SizeOf(keyBlock), 0);

  if KeyLen > 64 then
  begin
    keyDigest := SHA256Buf(Key, KeyLen);
    Move(keyDigest[0], keyBlock[0], 32);
  end
  else if KeyLen > 0 then
  begin
    pk := PByte(@Key);
    Move(pk^, keyBlock[0], KeyLen);
  end;

  for i := 0 to 63 do
  begin
    ipad[i] := keyBlock[i] xor $36;
    opad[i] := keyBlock[i] xor $5c;
  end;

  SHA256Init(ctx);
  SHA256Update(ctx, ipad, 64);
  if DataLen > 0 then
    SHA256Update(ctx, Data, DataLen);
  SHA256Final(ctx, innerDigest);

  SHA256Init(ctx);
  SHA256Update(ctx, opad, 64);
  SHA256Update(ctx, innerDigest, 32);
  SHA256Final(ctx, Digest);
end;

function HMACSHA256Bytes(const Key, Data: TBytes): TSHA256Digest;
var
  keyPtr, dataPtr: PByte;
begin
  if Length(Key) > 0 then keyPtr := @Key[0] else keyPtr := nil;
  if Length(Data) > 0 then dataPtr := @Data[0] else dataPtr := nil;
  HMACSHA256(keyPtr^, Length(Key), dataPtr^, Length(Data), Result);
end;

function PBKDF2_HMACSHA256(const Password: TBytes; const Salt: TBytes;
  Iterations: Integer; DKLenBytes: Integer): TBytes;
var
  hLen: Integer;
  blockCount, i, j, k: Integer;
  saltPlusInt: TBytes;
  u, t: TSHA256Digest;
  pwPtr: PByte;
  pwLen: PtrUInt;
  outPos: Integer;
  remaining: Integer;
  copyLen: Integer;
begin
  hLen := 32;
  blockCount := (DKLenBytes + hLen - 1) div hLen;
  SetLength(Result, DKLenBytes);

  if Length(Password) > 0 then pwPtr := @Password[0] else pwPtr := nil;
  pwLen := Length(Password);

  outPos := 0;
  for i := 1 to blockCount do
  begin
    SetLength(saltPlusInt, Length(Salt) + 4);
    if Length(Salt) > 0 then
      Move(Salt[0], saltPlusInt[0], Length(Salt));
    saltPlusInt[Length(Salt)]     := Byte(i shr 24);
    saltPlusInt[Length(Salt) + 1] := Byte(i shr 16);
    saltPlusInt[Length(Salt) + 2] := Byte(i shr 8);
    saltPlusInt[Length(Salt) + 3] := Byte(i);

    HMACSHA256(pwPtr^, pwLen, saltPlusInt[0], Length(saltPlusInt), u);
    t := u;

    for j := 2 to Iterations do
    begin
      HMACSHA256(pwPtr^, pwLen, u[0], 32, u);
      for k := 0 to 31 do
        t[k] := t[k] xor u[k];
    end;

    remaining := DKLenBytes - outPos;
    copyLen := hLen;
    if copyLen > remaining then copyLen := remaining;
    Move(t[0], Result[outPos], copyLen);
    Inc(outPos, copyLen);
  end;
end;

end.
