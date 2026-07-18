unit uencryptiondialog;

{$mode objfpc}{$H+}
{$codepage UTF8}

interface

uses
  Classes, SysUtils, Forms, Controls, Graphics, Dialogs, Menus, StdCtrls,
  ComCtrls, ImgList, Clipbrd, LCLProc, ActnList,
  umessages, ucryptpadcrypto, upasswordpromptform, uencryptpasswordform;

type

  { TEncryptionForm }

  TEncryptionForm = class(TForm)
    ActionList: TActionList;
    ActionNew: TAction;
    ActionOpen: TAction;
    ActionSave: TAction;
    ActionSaveAs: TAction;
    ActionCut: TAction;
    ActionCopy: TAction;
    ActionPaste: TAction;
    ActionEncrypt: TAction;
    ActionDecrypt: TAction;
    ActionExit: TAction;
    FEditor: TMemo;
    FToolBar: TToolBar;
    FToolImages: TImageList;
    ToolButtonNew: TToolButton;
    ToolButtonOpen: TToolButton;
    ToolButtonSave: TToolButton;
    ToolButtonSep1: TToolButton;
    ToolButtonCut: TToolButton;
    ToolButtonCopy: TToolButton;
    ToolButtonPaste: TToolButton;
    ToolButtonSep2: TToolButton;
    ToolButtonEncrypt: TToolButton;
    ToolButtonSep3: TToolButton;
    ToolButtonExit: TToolButton;
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
    procedure ActionNewExecute(Sender: TObject);
    procedure ActionOpenExecute(Sender: TObject);
    procedure ActionSaveExecute(Sender: TObject);
    procedure ActionSaveAsExecute(Sender: TObject);
    procedure ActionCutExecute(Sender: TObject);
    procedure ActionCopyExecute(Sender: TObject);
    procedure ActionPasteExecute(Sender: TObject);
    procedure ActionEncryptExecute(Sender: TObject);
    procedure ActionDecryptExecute(Sender: TObject);
    procedure ActionExitExecute(Sender: TObject);
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
  MenuEdit.Caption := GetString('encryption.menu.edit');
  MenuEncryption.Caption := GetString('encryption.menu.encryption');

  ActionNew.Caption := GetString('encryption.menu.file.new');
  ActionNew.Hint := GetString('encryption.tooltip.new');
  ActionNew.ShortCut := TextToShortCut('Ctrl+N');
  ActionOpen.Caption := GetString('encryption.menu.file.open');
  ActionOpen.Hint := GetString('encryption.tooltip.open');
  ActionOpen.ShortCut := TextToShortCut('Ctrl+O');
  ActionSave.Caption := GetString('encryption.menu.file.save');
  ActionSave.Hint := GetString('encryption.tooltip.save');
  ActionSave.ShortCut := TextToShortCut('Ctrl+S');
  ActionSaveAs.Caption := GetString('encryption.menu.file.saveas');
  ActionExit.Caption := GetString('encryption.menu.file.exit');
  ActionExit.Hint := GetString('encryption.tooltip.exit');

  ActionCut.Caption := GetString('encryption.menu.edit.cut');
  ActionCut.Hint := GetString('encryption.tooltip.cut');
  ActionCut.ShortCut := TextToShortCut('Ctrl+X');
  ActionCopy.Caption := GetString('encryption.menu.edit.copy');
  ActionCopy.Hint := GetString('encryption.tooltip.copy');
  ActionCopy.ShortCut := TextToShortCut('Ctrl+C');
  ActionPaste.Caption := GetString('encryption.menu.edit.paste');
  ActionPaste.Hint := GetString('encryption.tooltip.paste');
  ActionPaste.ShortCut := TextToShortCut('Ctrl+V');

  ActionEncrypt.Caption := GetString('encryption.menu.encryption.encrypt');
  ActionEncrypt.Hint := GetString('encryption.tooltip.encrypt');
  ActionDecrypt.Caption := GetString('encryption.menu.encryption.decrypt');
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

  Result := MessageDlg(GetString('encryption.save.message'), GetString('encryption.save.title'),
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

procedure TEncryptionForm.ActionNewExecute(Sender: TObject);
begin
  if ShowSaveConfirmation = mrCancel then Exit;
  FEditor.Text := '';
  FSaved := False;
  FSavedFileName := '';
  UpdateTitle;
end;

procedure TEncryptionForm.ActionOpenExecute(Sender: TObject);
begin
  if ShowSaveConfirmation = mrCancel then Exit;
  if not FOpenDialog.Execute then Exit;
  LoadFile(FOpenDialog.FileName);
end;

procedure TEncryptionForm.ActionSaveExecute(Sender: TObject);
begin
  DoSaveInternal(False);
end;

procedure TEncryptionForm.ActionSaveAsExecute(Sender: TObject);
begin
  DoSaveInternal(True);
end;

procedure TEncryptionForm.ActionCutExecute(Sender: TObject);
begin
  FEditor.CutToClipboard;
end;

procedure TEncryptionForm.ActionCopyExecute(Sender: TObject);
begin
  FEditor.CopyToClipboard;
end;

procedure TEncryptionForm.ActionPasteExecute(Sender: TObject);
begin
  FEditor.PasteFromClipboard;
end;

procedure TEncryptionForm.ActionEncryptExecute(Sender: TObject);
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

procedure TEncryptionForm.ActionDecryptExecute(Sender: TObject);
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

procedure TEncryptionForm.ActionExitExecute(Sender: TObject);
begin
  Close;
end;

procedure TEncryptionForm.FormCloseHandler(Sender: TObject; var CloseAction: TCloseAction);
begin
  CloseAction := caFree;
end;

end.
