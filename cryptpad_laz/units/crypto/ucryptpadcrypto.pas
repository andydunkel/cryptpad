unit ucryptpadcrypto;

{$mode objfpc}{$H+}

interface

uses
  Classes, SysUtils, base64, usha256, uaes256, uaesgcm
  {$IFDEF WINDOWS}, Windows{$ENDIF};

const
  APP_NAME = 'DA-CryptPad';
  APP_VERSION = '1.2.0';

  FORMAT_VERSION: Byte = 1;
  SALT_LENGTH = 16;
  PBKDF2_ITERATIONS = 100000;
  AES_KEY_BYTES = 32; // 256 bit

  BEGIN_MESSAGE = '-----BEGIN ENCRYPTED MESSAGE-----';
  BEGIN_FILE = '-----BEGIN ENCRYPTED FILE-----';
  END_MESSAGE = '-----END ENCRYPTED MESSAGE-----';
  BEGIN_ENC = '-----BEGIN-----';
  ARMOR_LINE_WIDTH = 64;

type
  ECryptPadCrypto = class(Exception);

// Cryptographically secure random bytes.
function RandomBytes(Count: Integer): TBytes;

// Mirrors de.dasoftware.cryptpad.crypto.AESEncryption (Base64 of
// version(1) + salt(16) + iv(12) + ciphertext+tag).
function AESEncryptString(const Passphrase, Message: UTF8String): UTF8String;
function AESDecryptString(const Passphrase, EncryptedBase64: UTF8String): UTF8String;

// Mirrors de.dasoftware.cryptpad.crypto.EncryptionWrapper (PGP-style ASCII armor).
function EncryptionWrapperEncryptMessage(const Message, Key: UTF8String): UTF8String;
function EncryptionWrapperEncryptFile(const FileContent, Key: UTF8String): UTF8String;
function EncryptionWrapperDecryptMessage(const WrappedMessage, Key: UTF8String): UTF8String;

implementation

{$IFDEF WINDOWS}
type
  TBCryptGenRandom = function(hAlgorithm: Pointer; pbBuffer: PByte; cbBuffer: ULONG;
    dwFlags: ULONG): LongInt; stdcall;
const
  BCRYPT_USE_SYSTEM_PREFERRED_RNG = $00000002;
{$ENDIF}

function RandomBytes(Count: Integer): TBytes;
{$IFDEF WINDOWS}
var
  hMod: THandle;
  genRandom: TBCryptGenRandom;
  status: LongInt;
{$ELSE}
var
  f: file;
  bytesRead: LongInt;
{$ENDIF}
begin
  SetLength(Result, Count);
  if Count = 0 then Exit;

  {$IFDEF WINDOWS}
  hMod := LoadLibrary('bcrypt.dll');
  if hMod = 0 then
    raise ECryptPadCrypto.Create('bcrypt.dll not available');
  try
    Pointer(genRandom) := GetProcAddress(hMod, 'BCryptGenRandom');
    if not Assigned(genRandom) then
      raise ECryptPadCrypto.Create('BCryptGenRandom not found');
    status := genRandom(nil, @Result[0], Count, BCRYPT_USE_SYSTEM_PREFERRED_RNG);
    if status <> 0 then
      raise ECryptPadCrypto.CreateFmt('BCryptGenRandom failed: 0x%x', [status]);
  finally
    FreeLibrary(hMod);
  end;
  {$ELSE}
  AssignFile(f, '/dev/urandom');
  Reset(f, 1);
  try
    BlockRead(f, Result[0], Count, bytesRead);
    if bytesRead <> Count then
      raise ECryptPadCrypto.Create('/dev/urandom read failed');
  finally
    CloseFile(f);
  end;
  {$ENDIF}
end;

function BytesToRawString(const B: TBytes): RawByteString;
begin
  SetLength(Result, Length(B));
  if Length(B) > 0 then
    Move(B[0], Result[1], Length(B));
  SetCodePage(Result, CP_NONE, False);
end;

function RawStringToBytes(const S: RawByteString): TBytes;
begin
  SetLength(Result, Length(S));
  if Length(S) > 0 then
    Move(S[1], Result[0], Length(S));
end;

function Base64EncodeBytes(const B: TBytes): UTF8String;
begin
  Result := UTF8String(EncodeStringBase64(BytesToRawString(B)));
end;

function Base64DecodeBytes(const S: UTF8String): TBytes;
begin
  Result := RawStringToBytes(DecodeStringBase64(RawByteString(S)));
end;

function DeriveKey(const Passphrase: UTF8String; const Salt: TBytes): TAES256Key;
var
  pwBytes: TBytes;
  dk: TBytes;
begin
  SetLength(pwBytes, Length(Passphrase));
  if Length(Passphrase) > 0 then
    Move(Passphrase[1], pwBytes[0], Length(Passphrase));

  dk := PBKDF2_HMACSHA256(pwBytes, Salt, PBKDF2_ITERATIONS, AES_KEY_BYTES);
  Move(dk[0], Result[0], AES_KEY_BYTES);
end;

function AESEncryptString(const Passphrase, Message: UTF8String): UTF8String;
var
  salt, iv, plaintext, ciphertext, combined: TBytes;
  key: TAES256Key;
  tag: TAESBlock;
begin
  salt := RandomBytes(SALT_LENGTH);
  iv := RandomBytes(GCM_IV_LENGTH);
  key := DeriveKey(Passphrase, salt);

  SetLength(plaintext, Length(Message));
  if Length(Message) > 0 then
    Move(Message[1], plaintext[0], Length(Message));

  ciphertext := AES256GCM_Encrypt(key, iv, plaintext, tag);

  SetLength(combined, 1 + SALT_LENGTH + GCM_IV_LENGTH + Length(ciphertext) + GCM_TAG_LENGTH);
  combined[0] := FORMAT_VERSION;
  Move(salt[0], combined[1], SALT_LENGTH);
  Move(iv[0], combined[1 + SALT_LENGTH], GCM_IV_LENGTH);
  if Length(ciphertext) > 0 then
    Move(ciphertext[0], combined[1 + SALT_LENGTH + GCM_IV_LENGTH], Length(ciphertext));
  Move(tag[0], combined[1 + SALT_LENGTH + GCM_IV_LENGTH + Length(ciphertext)], GCM_TAG_LENGTH);

  Result := Base64EncodeBytes(combined);
end;

function AESDecryptString(const Passphrase, EncryptedBase64: UTF8String): UTF8String;
var
  decoded, salt, iv, ciphertext, plaintext: TBytes;
  key: TAES256Key;
  tag: TAESBlock;
  ctLen: Integer;
begin
  decoded := Base64DecodeBytes(EncryptedBase64);

  if Length(decoded) < 1 + SALT_LENGTH + GCM_IV_LENGTH + GCM_TAG_LENGTH then
    raise ECryptPadCrypto.Create('Encrypted payload too short');

  if decoded[0] <> FORMAT_VERSION then
    raise ECryptPadCrypto.CreateFmt('Unsupported format version: %d', [decoded[0]]);

  SetLength(salt, SALT_LENGTH);
  Move(decoded[1], salt[0], SALT_LENGTH);

  SetLength(iv, GCM_IV_LENGTH);
  Move(decoded[1 + SALT_LENGTH], iv[0], GCM_IV_LENGTH);

  ctLen := Length(decoded) - 1 - SALT_LENGTH - GCM_IV_LENGTH - GCM_TAG_LENGTH;
  SetLength(ciphertext, ctLen);
  if ctLen > 0 then
    Move(decoded[1 + SALT_LENGTH + GCM_IV_LENGTH], ciphertext[0], ctLen);

  Move(decoded[Length(decoded) - GCM_TAG_LENGTH], tag[0], GCM_TAG_LENGTH);

  key := DeriveKey(Passphrase, salt);

  if not AES256GCM_Decrypt(key, iv, ciphertext, tag, plaintext) then
    raise ECryptPadCrypto.Create('Decryption failed: wrong password or corrupted data (tag mismatch)');

  SetLength(Result, Length(plaintext));
  if Length(plaintext) > 0 then
    Move(plaintext[0], Result[1], Length(plaintext));
end;

function WrapLines(const Text: UTF8String; LineWidth: Integer): UTF8String;
var
  pos, len, endPos: Integer;
begin
  Result := '';
  len := Length(Text);
  pos := 1;
  while pos <= len do
  begin
    endPos := pos + LineWidth - 1;
    if endPos > len then endPos := len;
    Result := Result + Copy(Text, pos, endPos - pos + 1);
    if endPos < len then
      Result := Result + #10;
    pos := endPos + 1;
  end;
end;

function BuildEncryptedOutput(const BeginMarker: UTF8String; const Content, Key: UTF8String): UTF8String;
var
  encrypted: UTF8String;
begin
  encrypted := AESEncryptString(Key, Content);
  Result := BeginMarker + #10 +
            'Version: ' + APP_NAME + ' ' + APP_VERSION + #10#10 +
            BEGIN_ENC + #10 +
            WrapLines(encrypted, ARMOR_LINE_WIDTH) + #10 +
            END_MESSAGE;
end;

function EncryptionWrapperEncryptMessage(const Message, Key: UTF8String): UTF8String;
begin
  Result := BuildEncryptedOutput(BEGIN_MESSAGE, Message, Key);
end;

function EncryptionWrapperEncryptFile(const FileContent, Key: UTF8String): UTF8String;
begin
  Result := BuildEncryptedOutput(BEGIN_FILE, FileContent, Key);
end;

function EncryptionWrapperDecryptMessage(const WrappedMessage, Key: UTF8String): UTF8String;
var
  startPos, endPos: Integer;
  encryptedContent: UTF8String;
  i: Integer;
  cleaned: UTF8String;
  c: Char;
begin
  startPos := Pos(BEGIN_ENC, WrappedMessage);
  endPos := Pos(END_MESSAGE, WrappedMessage);

  if (startPos = 0) or (endPos = 0) then
    raise ECryptPadCrypto.Create('Missing encryption headers');

  startPos := startPos + Length(BEGIN_ENC);
  encryptedContent := Copy(WrappedMessage, startPos, endPos - startPos);

  cleaned := '';
  for i := 1 to Length(encryptedContent) do
  begin
    c := encryptedContent[i];
    if not (c in [' ', #9, #10, #13]) then
      cleaned := cleaned + c;
  end;

  if cleaned = '' then
    raise ECryptPadCrypto.Create('No encrypted content found');

  Result := AESDecryptString(Key, cleaned);
end;

end.
