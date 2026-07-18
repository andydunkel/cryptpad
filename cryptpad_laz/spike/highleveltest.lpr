program highleveltest;

{$mode objfpc}{$H+}

uses
  SysUtils, ucryptpadcrypto;

var
  encB64: UTF8String;
  dec: UTF8String;
  wrapped: UTF8String;
  unwrapped: UTF8String;
  rnd: TBytes;
begin
  rnd := RandomBytes(16);
  WriteLn('random_bytes_len=', Length(rnd));

  encB64 := AESEncryptString('correct horse battery staple', 'Hello, Welt! Umlaut test: äöüß');
  WriteLn('enc_b64=', encB64);
  dec := AESDecryptString('correct horse battery staple', encB64);
  WriteLn('dec=', dec);
  if dec = 'Hello, Welt! Umlaut test: äöüß' then
    WriteLn('AES roundtrip: OK')
  else
    WriteLn('AES roundtrip: FAIL');

  // wrong password should fail
  try
    dec := AESDecryptString('wrong password', encB64);
    WriteLn('wrong password did NOT fail: BUG');
  except
    on E: Exception do
      WriteLn('wrong password correctly rejected: ', E.Message);
  end;

  wrapped := EncryptionWrapperEncryptFile('<xml>test content</xml>', 'mypassword123');
  WriteLn('---wrapped---');
  WriteLn(wrapped);
  WriteLn('---end wrapped---');

  unwrapped := EncryptionWrapperDecryptMessage(wrapped, 'mypassword123');
  if unwrapped = '<xml>test content</xml>' then
    WriteLn('Wrapper roundtrip: OK')
  else
    WriteLn('Wrapper roundtrip: FAIL, got: ', unwrapped);
end.
