unit uaesgcm;

{$mode objfpc}{$H+}

interface

uses
  Classes, SysUtils, uaes256;

const
  GCM_IV_LENGTH = 12;
  GCM_TAG_LENGTH = 16;

// IV must be exactly GCM_IV_LENGTH (12) bytes. AAD is always empty (matches the app's usage).
function AES256GCM_Encrypt(const Key: TAES256Key; const IV: TBytes;
  const Plaintext: TBytes; out Tag: TAESBlock): TBytes;

// Returns False (and empty Plaintext) if the authentication tag does not match.
function AES256GCM_Decrypt(const Key: TAES256Key; const IV: TBytes;
  const Ciphertext: TBytes; const ReceivedTag: TAESBlock; out Plaintext: TBytes): Boolean;

implementation

function GF128Mul(const X, Y: TAESBlock): TAESBlock;
const
  R0 = $E1;
var
  Z, V: TAESBlock;
  i, byteIdx, bitIdx, j: Integer;
  xBitSet, lsbSet: Boolean;
begin
  FillChar(Z, SizeOf(Z), 0);
  V := Y;

  for i := 0 to 127 do
  begin
    byteIdx := i div 8;
    bitIdx := 7 - (i mod 8);
    xBitSet := ((X[byteIdx] shr bitIdx) and 1) = 1;
    if xBitSet then
      for j := 0 to 15 do
        Z[j] := Z[j] xor V[j];

    lsbSet := (V[15] and 1) = 1;

    for j := 15 downto 1 do
      V[j] := Byte((V[j] shr 1) or ((V[j-1] and 1) shl 7));
    V[0] := V[0] shr 1;

    if lsbSet then
      V[0] := V[0] xor R0;
  end;

  Result := Z;
end;

function GHashCiphertext(const H: TAESBlock; const Ciphertext: TBytes): TAESBlock;
var
  blk, lenBlock: TAESBlock;
  i, remaining, offset, chunkLen: Integer;
  ctBits: UInt64;
begin
  FillChar(Result, SizeOf(Result), 0);

  offset := 0;
  remaining := Length(Ciphertext);
  while remaining > 0 do
  begin
    FillChar(blk, SizeOf(blk), 0);
    chunkLen := remaining;
    if chunkLen > 16 then chunkLen := 16;
    Move(Ciphertext[offset], blk[0], chunkLen);

    for i := 0 to 15 do
      Result[i] := Result[i] xor blk[i];
    Result := GF128Mul(Result, H);

    Inc(offset, chunkLen);
    Dec(remaining, chunkLen);
  end;

  FillChar(lenBlock, SizeOf(lenBlock), 0);
  ctBits := UInt64(Length(Ciphertext)) * 8;
  for i := 0 to 7 do
    lenBlock[15 - i] := Byte(ctBits shr (i * 8));
  // bytes 0..7 stay zero: AAD length is always 0 in this app

  for i := 0 to 15 do
    Result[i] := Result[i] xor lenBlock[i];
  Result := GF128Mul(Result, H);
end;

// The GCM 32-bit block counter is defined to wrap at $FFFFFFFF -> 0.
{$PUSH}{$R-}{$Q-}
procedure IncrCounter(var Block: TAESBlock); inline;
var
  counter: UInt32;
begin
  counter := (UInt32(Block[12]) shl 24) or (UInt32(Block[13]) shl 16) or
             (UInt32(Block[14]) shl 8) or UInt32(Block[15]);
  Inc(counter);
  Block[12] := Byte(counter shr 24);
  Block[13] := Byte(counter shr 16);
  Block[14] := Byte(counter shr 8);
  Block[15] := Byte(counter);
end;
{$POP}

procedure GCTR(const Ctx: TAES256Context; StartCounter: TAESBlock; const Input: TBytes;
  out Output: TBytes);
var
  counter, keystream: TAESBlock;
  offset, remaining, chunkLen, i: Integer;
begin
  SetLength(Output, Length(Input));
  counter := StartCounter;
  offset := 0;
  remaining := Length(Input);
  while remaining > 0 do
  begin
    AES256EncryptBlock(Ctx, counter, keystream);
    chunkLen := remaining;
    if chunkLen > 16 then chunkLen := 16;
    for i := 0 to chunkLen - 1 do
      Output[offset + i] := Input[offset + i] xor keystream[i];
    IncrCounter(counter);
    Inc(offset, chunkLen);
    Dec(remaining, chunkLen);
  end;
end;

function BuildJ0(const IV: TBytes): TAESBlock;
begin
  FillChar(Result, SizeOf(Result), 0);
  Move(IV[0], Result[0], GCM_IV_LENGTH);
  Result[12] := 0; Result[13] := 0; Result[14] := 0; Result[15] := 1;
end;

function AES256GCM_Encrypt(const Key: TAES256Key; const IV: TBytes;
  const Plaintext: TBytes; out Tag: TAESBlock): TBytes;
var
  ctx: TAES256Context;
  zeroBlock, H, J0, Y0Enc, s, startCtr: TAESBlock;
  i: Integer;
begin
  if Length(IV) <> GCM_IV_LENGTH then
    raise Exception.Create('AES256GCM_Encrypt: IV must be 12 bytes');

  AES256Init(ctx, Key);

  FillChar(zeroBlock, SizeOf(zeroBlock), 0);
  AES256EncryptBlock(ctx, zeroBlock, H);

  J0 := BuildJ0(IV);

  startCtr := J0;
  IncrCounter(startCtr);
  GCTR(ctx, startCtr, Plaintext, Result);

  s := GHashCiphertext(H, Result);
  AES256EncryptBlock(ctx, J0, Y0Enc);
  for i := 0 to 15 do
    Tag[i] := s[i] xor Y0Enc[i];
end;

function AES256GCM_Decrypt(const Key: TAES256Key; const IV: TBytes;
  const Ciphertext: TBytes; const ReceivedTag: TAESBlock; out Plaintext: TBytes): Boolean;
var
  ctx: TAES256Context;
  zeroBlock, H, J0, Y0Enc, s, computedTag, startCtr: TAESBlock;
  i: Integer;
  diff: Byte;
begin
  if Length(IV) <> GCM_IV_LENGTH then
    raise Exception.Create('AES256GCM_Decrypt: IV must be 12 bytes');

  AES256Init(ctx, Key);

  FillChar(zeroBlock, SizeOf(zeroBlock), 0);
  AES256EncryptBlock(ctx, zeroBlock, H);

  J0 := BuildJ0(IV);

  s := GHashCiphertext(H, Ciphertext);
  AES256EncryptBlock(ctx, J0, Y0Enc);
  for i := 0 to 15 do
    computedTag[i] := s[i] xor Y0Enc[i];

  diff := 0;
  for i := 0 to 15 do
    diff := diff or (computedTag[i] xor ReceivedTag[i]);
  Result := diff = 0;

  if Result then
  begin
    startCtr := J0;
    IncrCounter(startCtr);
    GCTR(ctx, startCtr, Ciphertext, Plaintext);
  end
  else
    SetLength(Plaintext, 0);
end;

end.
