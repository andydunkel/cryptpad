unit uaes256;

{$mode objfpc}{$H+}

interface

uses
  Classes, SysUtils;

type
  TAESBlock = array[0..15] of Byte;
  TAES256Key = array[0..31] of Byte;

  TAES256Context = record
    RoundKeys: array[0..59] of UInt32; // 4 words per round key, 15 round keys (Nr=14)
  end;

procedure AES256Init(out Ctx: TAES256Context; const Key: TAES256Key);
procedure AES256EncryptBlock(const Ctx: TAES256Context; const InBlock: TAESBlock; out OutBlock: TAESBlock);

implementation

const
  Nb = 4;
  Nk = 8;
  Nr = 14;

  SBox: array[0..255] of Byte = (
    $63,$7c,$77,$7b,$f2,$6b,$6f,$c5,$30,$01,$67,$2b,$fe,$d7,$ab,$76,
    $ca,$82,$c9,$7d,$fa,$59,$47,$f0,$ad,$d4,$a2,$af,$9c,$a4,$72,$c0,
    $b7,$fd,$93,$26,$36,$3f,$f7,$cc,$34,$a5,$e5,$f1,$71,$d8,$31,$15,
    $04,$c7,$23,$c3,$18,$96,$05,$9a,$07,$12,$80,$e2,$eb,$27,$b2,$75,
    $09,$83,$2c,$1a,$1b,$6e,$5a,$a0,$52,$3b,$d6,$b3,$29,$e3,$2f,$84,
    $53,$d1,$00,$ed,$20,$fc,$b1,$5b,$6a,$cb,$be,$39,$4a,$4c,$58,$cf,
    $d0,$ef,$aa,$fb,$43,$4d,$33,$85,$45,$f9,$02,$7f,$50,$3c,$9f,$a8,
    $51,$a3,$40,$8f,$92,$9d,$38,$f5,$bc,$b6,$da,$21,$10,$ff,$f3,$d2,
    $cd,$0c,$13,$ec,$5f,$97,$44,$17,$c4,$a7,$7e,$3d,$64,$5d,$19,$73,
    $60,$81,$4f,$dc,$22,$2a,$90,$88,$46,$ee,$b8,$14,$de,$5e,$0b,$db,
    $e0,$32,$3a,$0a,$49,$06,$24,$5c,$c2,$d3,$ac,$62,$91,$95,$e4,$79,
    $e7,$c8,$37,$6d,$8d,$d5,$4e,$a9,$6c,$56,$f4,$ea,$65,$7a,$ae,$08,
    $ba,$78,$25,$2e,$1c,$a6,$b4,$c6,$e8,$dd,$74,$1f,$4b,$bd,$8b,$8a,
    $70,$3e,$b5,$66,$48,$03,$f6,$0e,$61,$35,$57,$b9,$86,$c1,$1d,$9e,
    $e1,$f8,$98,$11,$69,$d9,$8e,$94,$9b,$1e,$87,$e9,$ce,$55,$28,$df,
    $8c,$a1,$89,$0d,$bf,$e6,$42,$68,$41,$99,$2d,$0f,$b0,$54,$bb,$16
  );

  Rcon: array[1..14] of Byte = (
    $01,$02,$04,$08,$10,$20,$40,$80,$1b,$36,$6c,$d8,$ab,$4d
  );

function XTime(x: Byte): Byte; inline;
begin
  if (x and $80) <> 0 then
    Result := Byte((x shl 1) xor $1b)
  else
    Result := Byte(x shl 1);
end;

function SubWord(w: UInt32): UInt32;
begin
  Result := (UInt32(SBox[(w shr 24) and $ff]) shl 24) or
            (UInt32(SBox[(w shr 16) and $ff]) shl 16) or
            (UInt32(SBox[(w shr 8) and $ff]) shl 8) or
             UInt32(SBox[w and $ff]);
end;

function RotWord(w: UInt32): UInt32; inline;
begin
  Result := ((w shl 8) or (w shr 24)) and $ffffffff;
end;

procedure AES256Init(out Ctx: TAES256Context; const Key: TAES256Key);
var
  w: array[0..(Nb * (Nr + 1)) - 1] of UInt32;
  i: Integer;
  temp: UInt32;
begin
  for i := 0 to Nk - 1 do
    w[i] := (UInt32(Key[4*i]) shl 24) or (UInt32(Key[4*i+1]) shl 16) or
            (UInt32(Key[4*i+2]) shl 8) or UInt32(Key[4*i+3]);

  for i := Nk to (Nb * (Nr + 1)) - 1 do
  begin
    temp := w[i-1];
    if (i mod Nk) = 0 then
      temp := SubWord(RotWord(temp)) xor (UInt32(Rcon[i div Nk]) shl 24)
    else if (Nk > 6) and ((i mod Nk) = 4) then
      temp := SubWord(temp);
    w[i] := w[i - Nk] xor temp;
  end;

  for i := 0 to (Nb * (Nr + 1)) - 1 do
    Ctx.RoundKeys[i] := w[i];
end;

procedure AddRoundKey(var State: TAESBlock; const Ctx: TAES256Context; Round: Integer);
var
  c: Integer;
  w: UInt32;
begin
  for c := 0 to 3 do
  begin
    w := Ctx.RoundKeys[Round * 4 + c];
    State[c*4]   := State[c*4]   xor Byte(w shr 24);
    State[c*4+1] := State[c*4+1] xor Byte(w shr 16);
    State[c*4+2] := State[c*4+2] xor Byte(w shr 8);
    State[c*4+3] := State[c*4+3] xor Byte(w);
  end;
end;

procedure SubBytes(var State: TAESBlock);
var
  i: Integer;
begin
  for i := 0 to 15 do
    State[i] := SBox[State[i]];
end;

procedure ShiftRows(var State: TAESBlock);
var
  tmp: TAESBlock;
begin
  // State is column-major: byte index = col*4 + row
  tmp := State;
  // row 0: no shift
  State[0]  := tmp[0];  State[4]  := tmp[4];  State[8]  := tmp[8];  State[12] := tmp[12];
  // row 1: shift left 1
  State[1]  := tmp[5];  State[5]  := tmp[9];  State[9]  := tmp[13]; State[13] := tmp[1];
  // row 2: shift left 2
  State[2]  := tmp[10]; State[6]  := tmp[14]; State[10] := tmp[2];  State[14] := tmp[6];
  // row 3: shift left 3
  State[3]  := tmp[15]; State[7]  := tmp[3];  State[11] := tmp[7];  State[15] := tmp[11];
end;

procedure MixColumns(var State: TAESBlock);
var
  c: Integer;
  a0, a1, a2, a3: Byte;
begin
  for c := 0 to 3 do
  begin
    a0 := State[c*4]; a1 := State[c*4+1]; a2 := State[c*4+2]; a3 := State[c*4+3];
    State[c*4]   := Byte(XTime(a0) xor (XTime(a1) xor a1) xor a2 xor a3);
    State[c*4+1] := Byte(a0 xor XTime(a1) xor (XTime(a2) xor a2) xor a3);
    State[c*4+2] := Byte(a0 xor a1 xor XTime(a2) xor (XTime(a3) xor a3));
    State[c*4+3] := Byte((XTime(a0) xor a0) xor a1 xor a2 xor XTime(a3));
  end;
end;

procedure AES256EncryptBlock(const Ctx: TAES256Context; const InBlock: TAESBlock; out OutBlock: TAESBlock);
var
  State: TAESBlock;
  round: Integer;
begin
  State := InBlock;
  AddRoundKey(State, Ctx, 0);

  for round := 1 to Nr - 1 do
  begin
    SubBytes(State);
    ShiftRows(State);
    MixColumns(State);
    AddRoundKey(State, Ctx, round);
  end;

  SubBytes(State);
  ShiftRows(State);
  AddRoundKey(State, Ctx, Nr);

  OutBlock := State;
end;

end.
