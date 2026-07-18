unit usettingsform;

{$mode objfpc}{$H+}
{$codepage UTF8}

interface

uses
  Classes, SysUtils, Forms, StdCtrls, Controls, Dialogs,
  umessages, uappsettings;

type

  { TSettingsForm }

  TSettingsForm = class(TForm)
    ButtonCancel: TButton;
    ButtonOK: TButton;
    ComboLanguage: TComboBox;
    LabelLanguage: TLabel;
  public
    constructor Create(AOwner: TComponent); override;
  end;

procedure ShowSettingsDialog(AOwner: TComponent);

implementation

{$R *.lfm}

procedure ShowSettingsDialog(AOwner: TComponent);
var
  frm: TSettingsForm;
  oldLanguage: string;
begin
  frm := TSettingsForm.Create(AOwner);
  try
    oldLanguage := uappsettings.GetLanguage;
    case oldLanguage of
      'en': frm.ComboLanguage.ItemIndex := 1;
      'de': frm.ComboLanguage.ItemIndex := 2;
    else
      frm.ComboLanguage.ItemIndex := 0;
    end;

    if frm.ShowModal = mrOK then
    begin
      case frm.ComboLanguage.ItemIndex of
        1: uappsettings.SetLanguage('en');
        2: uappsettings.SetLanguage('de');
      else
        uappsettings.SetLanguage('system');
      end;

      if uappsettings.GetLanguage <> oldLanguage then
        MessageDlg(GetString('settings.restart.message'), mtInformation, [mbOK], 0);
    end;
  finally
    frm.Free;
  end;
end;

constructor TSettingsForm.Create(AOwner: TComponent);
begin
  inherited Create(AOwner);

  Caption := GetString('settings.title');
  LabelLanguage.Caption := GetString('settings.language');

  ComboLanguage.Items.Clear;
  ComboLanguage.Items.Add(GetString('settings.language.system'));
  ComboLanguage.Items.Add(GetString('settings.language.english'));
  ComboLanguage.Items.Add(GetString('settings.language.german'));
end;

end.
