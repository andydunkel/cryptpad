unit uencryptiondialog;

{$mode objfpc}{$H+}
{$codepage UTF8}

interface

uses
  Classes, SysUtils, Forms, Controls, Graphics, Dialogs, Menus, StdCtrls,
  Clipbrd, LCLProc,
  umessages, ucryptpadcrypto, upasswordpromptform, uencryptpasswordform;

type

  { TEncryptionForm }

  TEncryptionForm = class(TForm)
    FEditor: TMemo;
    MainMenu1: TMainMenu;
    MenuFile: TMenuItem;
    MenuFileNew: TMenuItem;
    MenuFileOpen: TMenuItem;
    MenuFileSave: TMenuItem;
    MenuFileSaveAs: TMenuItem;
    MenuFileSep1: TMenuItem;
    MenuFileExit: TMenuItem;
    MenuEdit: TMenuItem;
    MenuEditCut: TMenuItem;
    MenuEditCopy: TMenuItem;
    MenuEditPaste: TMenuItem;
    MenuEncryption: TMenuItem;
    MenuEncryptionEncrypt: TMenuItem;
    MenuEncryptionDecrypt: TMenuItem;
    FOpenDialog: TOpenDialog;
    FSaveDialog: TSaveDialog;
    procedure DoNew(Sender: TObject);
    procedure DoOpen(Sender: TObject);
    procedure DoSave(Sender: TObject);
    procedure DoSaveAs(Sender: TObject);
    procedure DoCut(Sender: TObject);
    procedure DoCopy(Sender: TObject);
    procedure DoPaste(Sender: TObject);
    procedure DoEncrypt(Sender: TObject);
    procedure DoDecrypt(Sender: TObject);
    procedure MenuFileExitClick(Sender: TObject);
    procedure FormCloseHandler(Sender: TObject; var CloseAction: TCloseAction);
  private
    FSaved: Boolean;
    FSavedFileName: string;

    procedure ApplyTranslations;
    procedure UpdateTitle;
    function ShowSaveConfirmation: TModalResult; // mrYes/mrNo/mrCancel
    procedure WriteFile(const FileName: string);
    procedure LoadFile(const FileName: string);
    procedure DoSaveInternal(ShowDialog: Boolean);
  public
    constructor Create(AOwner: TComponent); override;
  end;

procedure ShowEncryptionWindow;

implementation

{$R *.lfm}

procedure ShowEncryptionWindow;
var
  frm: TEncryptionForm;
begin
  frm := TEncryptionForm.Create(Application);
  frm.Show;
end;

constructor TEncryptionForm.Create(AOwner: TComponent);
begin
  inherited Create(AOwner);

  FSaved := False;
  FSavedFileName := '';

  ApplyTranslations;
  UpdateTitle;

  OnClose := @FormCloseHandler;
end;

procedure TEncryptionForm.ApplyTranslations;
begin
  MenuFile.Caption := GetString('encryption.menu.file');
  MenuFileNew.Caption := GetString('encryption.menu.file.new');
  MenuFileNew.ShortCut := TextToShortCut('Ctrl+N');
  MenuFileOpen.Caption := GetString('encryption.menu.file.open');
  MenuFileOpen.ShortCut := TextToShortCut('Ctrl+O');
  MenuFileSave.Caption := GetString('encryption.menu.file.save');
  MenuFileSave.ShortCut := TextToShortCut('Ctrl+S');
  MenuFileSaveAs.Caption := GetString('encryption.menu.file.saveas');
  MenuFileExit.Caption := GetString('encryption.menu.file.exit');

  MenuEdit.Caption := GetString('encryption.menu.edit');
  MenuEditCut.Caption := GetString('encryption.menu.edit.cut');
  MenuEditCut.ShortCut := TextToShortCut('Ctrl+X');
  MenuEditCopy.Caption := GetString('encryption.menu.edit.copy');
  MenuEditCopy.ShortCut := TextToShortCut('Ctrl+C');
  MenuEditPaste.Caption := GetString('encryption.menu.edit.paste');
  MenuEditPaste.ShortCut := TextToShortCut('Ctrl+V');

  MenuEncryption.Caption := GetString('encryption.menu.encryption');
  MenuEncryptionEncrypt.Caption := GetString('encryption.menu.encryption.encrypt');
  MenuEncryptionDecrypt.Caption := GetString('encryption.menu.encryption.decrypt');
end;

procedure TEncryptionForm.UpdateTitle;
var
  title: string;
begin
  title := GetString('encryption.title');
  if FSavedFileName <> '' then
    title := title + ' - ' + ExtractFileName(FSavedFileName);
  if not FSaved then
    title := title + ' *';
  Caption := title;
end;

function TEncryptionForm.ShowSaveConfirmation: TModalResult;
begin
  if (FEditor.Text = '') or FSaved then
  begin
    Result := mrNo;
    Exit;
  end;

  Result := MessageDlg(GetString('encryption.save.title'), GetString('encryption.save.message'),
    mtConfirmation, [mbYes, mbNo, mbCancel], 0);

  if Result = mrYes then
    DoSaveInternal(False);
end;

procedure TEncryptionForm.WriteFile(const FileName: string);
var
  fs: TFileStream;
  content: UTF8String;
begin
  try
    content := UTF8String(FEditor.Text);
    fs := TFileStream.Create(FileName, fmCreate);
    try
      if Length(content) > 0 then
        fs.WriteBuffer(content[1], Length(content));
    finally
      fs.Free;
    end;
    FSaved := True;
    FSavedFileName := FileName;
    UpdateTitle;
  except
    on E: Exception do
      MessageDlg(GetStringF('encryption.error.save', [E.Message]), GetString('encryption.error.title'),
        mtError, [mbOK], 0);
  end;
end;

procedure TEncryptionForm.LoadFile(const FileName: string);
var
  fs: TFileStream;
  content: UTF8String;
begin
  try
    fs := TFileStream.Create(FileName, fmOpenRead or fmShareDenyNone);
    try
      SetLength(content, fs.Size);
      if fs.Size > 0 then
        fs.ReadBuffer(content[1], fs.Size);
    finally
      fs.Free;
    end;
    FEditor.Text := string(content);
    FSaved := True;
    FSavedFileName := FileName;
    UpdateTitle;
  except
    on E: Exception do
      MessageDlg(GetStringF('encryption.error.load', [E.Message]), GetString('encryption.error.title'),
        mtError, [mbOK], 0);
  end;
end;

procedure TEncryptionForm.DoSaveInternal(ShowDialog: Boolean);
var
  fileName: string;
begin
  if (not FSaved) or ShowDialog then
  begin
    if FSavedFileName <> '' then
      FSaveDialog.FileName := FSavedFileName;
    if not FSaveDialog.Execute then Exit;

    fileName := FSaveDialog.FileName;
    if LowerCase(ExtractFileExt(fileName)) <> '.txt' then
      fileName := fileName + '.txt';

    WriteFile(fileName);
  end
  else
    WriteFile(FSavedFileName);
end;

procedure TEncryptionForm.DoNew(Sender: TObject);
begin
  if ShowSaveConfirmation = mrCancel then Exit;
  FEditor.Text := '';
  FSaved := False;
  FSavedFileName := '';
  UpdateTitle;
end;

procedure TEncryptionForm.DoOpen(Sender: TObject);
begin
  if ShowSaveConfirmation = mrCancel then Exit;
  if not FOpenDialog.Execute then Exit;
  LoadFile(FOpenDialog.FileName);
end;

procedure TEncryptionForm.DoSave(Sender: TObject);
begin
  DoSaveInternal(False);
end;

procedure TEncryptionForm.DoSaveAs(Sender: TObject);
begin
  DoSaveInternal(True);
end;

procedure TEncryptionForm.DoCut(Sender: TObject);
begin
  FEditor.CutToClipboard;
end;

procedure TEncryptionForm.DoCopy(Sender: TObject);
begin
  FEditor.CopyToClipboard;
end;

procedure TEncryptionForm.DoPaste(Sender: TObject);
begin
  FEditor.PasteFromClipboard;
end;

procedure TEncryptionForm.DoEncrypt(Sender: TObject);
var
  pw, encrypted: string;
begin
  if Trim(FEditor.Text) = '' then
  begin
    MessageDlg(GetString('encryption.notext.encrypt'), GetString('encryption.notext.title'),
      mtWarning, [mbOK], 0);
    Exit;
  end;

  if not uencryptpasswordform.PromptNewPassword(Self, GetString('password.encrypt.title'), pw) then Exit;

  try
    encrypted := string(EncryptionWrapperEncryptMessage(UTF8String(FEditor.Text), UTF8String(pw)));
    FEditor.Text := encrypted;
    FSaved := False;
    UpdateTitle;
  except
    on E: Exception do
      MessageDlg(GetStringF('encryption.error.encrypt', [E.Message]), GetString('encryption.error.title'),
        mtError, [mbOK], 0);
  end;
end;

procedure TEncryptionForm.DoDecrypt(Sender: TObject);
var
  pw, decrypted: string;
begin
  if Trim(FEditor.Text) = '' then
  begin
    MessageDlg(GetString('encryption.notext.decrypt'), GetString('encryption.notext.title'),
      mtWarning, [mbOK], 0);
    Exit;
  end;

  if not upasswordpromptform.PromptPassword(Self, GetString('password.decrypt.title'), GetString('password.decrypt.label'), pw) then Exit;

  try
    decrypted := string(EncryptionWrapperDecryptMessage(UTF8String(FEditor.Text), UTF8String(pw)));
    FEditor.Text := decrypted;
    FSaved := False;
    UpdateTitle;
  except
    on E: ECryptPadCrypto do
      MessageDlg(GetString('encryption.error.wrongpassword'), GetString('encryption.error.title'),
        mtError, [mbOK], 0);
    on E: Exception do
      MessageDlg(GetStringF('encryption.error.decrypt', [E.Message]), GetString('encryption.error.title'),
        mtError, [mbOK], 0);
  end;
end;

procedure TEncryptionForm.MenuFileExitClick(Sender: TObject);
begin
  Close;
end;

procedure TEncryptionForm.FormCloseHandler(Sender: TObject; var CloseAction: TCloseAction);
begin
  CloseAction := caFree;
end;

end.
