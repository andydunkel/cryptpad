unit uencryptpasswordform;

{$mode objfpc}{$H+}
{$codepage UTF8}

interface

uses
  Classes, SysUtils, Forms, StdCtrls, Controls, Dialogs,
  umessages;

type

  { TEncryptPasswordForm }

  TEncryptPasswordForm = class(TForm)
    ButtonCancel: TButton;
    ButtonOK: TButton;
    EditPassword1: TEdit;
    EditPassword2: TEdit;
    LabelPrompt1: TLabel;
    LabelPrompt2: TLabel;
    procedure ButtonOKClick(Sender: TObject);
  end;

function PromptNewPassword(AOwner: TComponent; const ATitle: string; out APassword: string): Boolean;

implementation

{$R *.lfm}

function PromptNewPassword(AOwner: TComponent; const ATitle: string; out APassword: string): Boolean;
var
  frm: TEncryptPasswordForm;
begin
  Result := False;
  APassword := '';

  frm := TEncryptPasswordForm.Create(AOwner);
  try
    frm.Caption := ATitle;
    if frm.ShowModal = mrOK then
    begin
      APassword := frm.EditPassword1.Text;
      Result := True;
    end;
  finally
    frm.Free;
  end;
end;

procedure TEncryptPasswordForm.ButtonOKClick(Sender: TObject);
begin
  if EditPassword1.Text = '' then
  begin
    MessageDlg(GetString('password.encrypt.empty'), mtError, [mbOK], 0);
    Exit;
  end;

  if EditPassword1.Text <> EditPassword2.Text then
  begin
    MessageDlg(GetString('password.encrypt.nomatch'), mtError, [mbOK], 0);
    Exit;
  end;

  ModalResult := mrOK;
end;

end.
