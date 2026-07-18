program interoptest;

{$mode objfpc}{$H+}

uses
  SysUtils, Classes, ucryptpadcrypto;

function ReadFileUtf8(const Path: string): UTF8String;
var
  fs: TFileStream;
  bytes: TBytes;
begin
  fs := TFileStream.Create(Path, fmOpenRead or fmShareDenyNone);
  try
    SetLength(bytes, fs.Size);
    if fs.Size > 0 then
      fs.ReadBuffer(bytes[0], fs.Size);
  finally
    fs.Free;
  end;
  SetLength(Result, Length(bytes));
  if Length(bytes) > 0 then
    Move(bytes[0], Result[1], Length(bytes));
end;

procedure WriteFileUtf8(const Path: string; const Content: UTF8String);
var
  fs: TFileStream;
begin
  fs := TFileStream.Create(Path, fmCreate);
  try
    if Length(Content) > 0 then
      fs.WriteBuffer(Content[1], Length(Content));
  finally
    fs.Free;
  end;
end;

var
  cmd, password: string;
begin
  cmd := ParamStr(1);
  password := UTF8String(ParamStr(2));

  if cmd = 'encrypt-message' then
  begin
    WriteFileUtf8(ParamStr(4), EncryptionWrapperEncryptFile(ReadFileUtf8(ParamStr(3)), password));
    WriteLn('wrote ', ParamStr(4));
  end
  else if cmd = 'decrypt-message' then
  begin
    WriteFileUtf8(ParamStr(4), EncryptionWrapperDecryptMessage(ReadFileUtf8(ParamStr(3)), password));
    WriteLn('wrote ', ParamStr(4));
  end
  else if cmd = 'encrypt-raw' then
  begin
    WriteFileUtf8(ParamStr(4), AESEncryptString(password, ReadFileUtf8(ParamStr(3))));
    WriteLn('wrote ', ParamStr(4));
  end
  else if cmd = 'decrypt-raw' then
  begin
    WriteFileUtf8(ParamStr(4), AESDecryptString(password, Trim(ReadFileUtf8(ParamStr(3)))));
    WriteLn('wrote ', ParamStr(4));
  end
  else
  begin
    WriteLn(StdErr, 'unknown command: ', cmd);
    Halt(1);
  end;
end.
