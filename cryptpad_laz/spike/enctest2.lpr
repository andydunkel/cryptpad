program enctest2;

{$mode objfpc}{$H+}

uses
  SysUtils, Classes;

function DumpHex(const S: RawByteString): string;
var
  i: Integer;
begin
  Result := '';
  for i := 1 to Length(S) do
    Result := Result + LowerCase(IntToHex(Ord(S[i]), 2)) + ' ';
end;

var
  lit: string;
  u8: UTF8String;
  back: string;
  fs: TFileStream;
  bytes: TBytes;
begin
  lit := 'äöüß€';
  WriteLn('lit codepage=', StringCodePage(lit));
  WriteLn('lit hex=', DumpHex(RawByteString(lit)));

  u8 := UTF8String(lit);
  WriteLn('u8 codepage=', StringCodePage(u8));
  WriteLn('u8 hex=', DumpHex(RawByteString(u8)));

  back := string(u8);
  WriteLn('back codepage=', StringCodePage(back));
  WriteLn('back hex=', DumpHex(RawByteString(back)));

  // write raw bytes of each stage directly to files for inspection outside any console/codepage issues
  SetLength(bytes, Length(lit));
  Move(lit[1], bytes[0], Length(lit));
  fs := TFileStream.Create('out\enctest2_lit.bin', fmCreate);
  fs.WriteBuffer(bytes[0], Length(bytes));
  fs.Free;

  SetLength(bytes, Length(u8));
  Move(u8[1], bytes[0], Length(u8));
  fs := TFileStream.Create('out\enctest2_u8.bin', fmCreate);
  fs.WriteBuffer(bytes[0], Length(bytes));
  fs.Free;

  SetLength(bytes, Length(back));
  Move(back[1], bytes[0], Length(back));
  fs := TFileStream.Create('out\enctest2_back.bin', fmCreate);
  fs.WriteBuffer(bytes[0], Length(bytes));
  fs.Free;
end.
