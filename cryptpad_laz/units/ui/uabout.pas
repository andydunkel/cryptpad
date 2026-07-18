unit uabout;

{$mode ObjFPC}{$H+}

interface

uses
  Classes, SysUtils, Forms, Controls, Graphics, Dialogs, ExtCtrls, StdCtrls, lclintf,
  uappinfo;

type

  { TFormAbout }

  TFormAbout = class(TForm)
    Button1: TButton;
    ImageAbout: TImage;
    Label1: TLabel;
    Label2: TLabel;
    Label3: TLabel;
    procedure Button1Click(Sender: TObject);
    procedure FormCreate(Sender: TObject);
    procedure Label3Click(Sender: TObject);
  private

  public

  end;

var
  FormAbout: TFormAbout;

implementation

{$R *.lfm}

{ TFormAbout }

procedure TFormAbout.FormCreate(Sender: TObject);
begin
  Label1.Caption := 'Version ' + APP_VERSION;
end;

procedure TFormAbout.Button1Click(Sender: TObject);
begin
  Close;
end;

procedure TFormAbout.Label3Click(Sender: TObject);
begin
  OpenUrl('https://da-software.net');
end;

end.

