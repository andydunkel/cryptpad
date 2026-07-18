unit upasswordpromptform;

{$mode objfpc}{$H+}
{$codepage UTF8}

interface

uses
  Classes, SysUtils, Forms, StdCtrls, Controls;

type

  { TPasswordPromptForm }

  TPasswordPromptForm = class(TForm)
    ButtonCancel: TButton;
    ButtonOK: TButton;
    EditPassword: TEdit;
    LabelPrompt: TLabel;
  end;

function PromptPassword(AOwner: TComponent; const ATitle, APromptLabel: string;
  out APassword: string): Boolean;

implementation

{$R *.lfm}

function PromptPassword(AOwner: TComponent; const ATitle, APromptLabel: string;
  out APassword: string): Boolean;
var
  frm: TPasswordPromptForm;
begin
  Result := False;
  APassword := '';

  frm := TPasswordPromptForm.Create(AOwner);
  try
    frm.Caption := ATitle;
    frm.LabelPrompt.Caption := APromptLabel;
    if frm.ShowModal = mrOK then
    begin
      APassword := frm.EditPassword.Text;
      Result := APassword <> '';
    end;
  finally
    frm.Free;
  end;
end;

end.
