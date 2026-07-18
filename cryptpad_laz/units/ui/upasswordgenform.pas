unit upasswordgenform;

{$mode objfpc}{$H+}
{$codepage UTF8}

interface

uses
  Classes, SysUtils, Forms, StdCtrls, Controls, Spin, Clipbrd, Dialogs,
  umessages, upasswordgenerator;

type

  { TPasswordGenForm }

  TPasswordGenForm = class(TForm)
    ButtonClose: TButton;
    ButtonCopy: TButton;
    ButtonGenerate: TButton;
    CheckCapitals: TCheckBox;
    CheckNumbers: TCheckBox;
    CheckSpecial: TCheckBox;
    EditResult: TEdit;
    LabelLength: TLabel;
    SpinLength: TSpinEdit;
    procedure ButtonCopyClick(Sender: TObject);
    procedure ButtonGenerateClick(Sender: TObject);
    procedure FormShow(Sender: TObject);
  private
    FGen: TPasswordGenerator;
  public
    constructor Create(AOwner: TComponent); override;
    destructor Destroy; override;
  end;

procedure ShowPasswordGenerator(AOwner: TComponent);

implementation

{$R *.lfm}

procedure ShowPasswordGenerator(AOwner: TComponent);
var
  frm: TPasswordGenForm;
begin
  frm := TPasswordGenForm.Create(AOwner);
  try
    frm.ShowModal;
  finally
    frm.Free;
  end;
end;

constructor TPasswordGenForm.Create(AOwner: TComponent);
begin
  inherited Create(AOwner);
  FGen := TPasswordGenerator.Create;

  Caption := GetString('passwordgen.title');
  LabelLength.Caption := GetString('passwordgen.length');
  CheckCapitals.Caption := GetString('passwordgen.capitals');
  CheckNumbers.Caption := GetString('passwordgen.numbers');
  CheckSpecial.Caption := GetString('passwordgen.specialchars');
  ButtonGenerate.Caption := GetString('passwordgen.generate');
  ButtonCopy.Caption := GetString('passwordgen.copy');
end;

destructor TPasswordGenForm.Destroy;
begin
  FGen.Free;
  inherited Destroy;
end;

procedure TPasswordGenForm.FormShow(Sender: TObject);
begin
  ButtonGenerateClick(Self);
end;

procedure TPasswordGenForm.ButtonGenerateClick(Sender: TObject);
begin
  FGen.SpecialCharsAllowed := CheckSpecial.Checked;
  FGen.NumbersAllowed := CheckNumbers.Checked;
  FGen.CapitalsAllowed := CheckCapitals.Checked;
  EditResult.Text := string(FGen.GeneratePassword(SpinLength.Value));
end;

procedure TPasswordGenForm.ButtonCopyClick(Sender: TObject);
begin
  Clipboard.AsText := EditResult.Text;
  MessageDlg(GetString('passwordgen.success.copied'), mtInformation, [mbOK], 0);
end;

end.
