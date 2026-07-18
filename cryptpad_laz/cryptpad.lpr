program cryptpad;

{$mode objfpc}{$H+}

uses
  {$IFDEF UNIX}
  cthreads,
  {$ENDIF}
  {$IFDEF HASAMIGA}
  athreads,
  {$ENDIF}
  Interfaces, // this includes the LCL widgetset
  Forms, main, uaes256, uaesgcm, ucryptpadcrypto, usha256, udatamodel,
  uentrytreenode, usearchservice, uxmlmanager, uappsettings, uencryptiondialog,
  uencryptpasswordform, upasswordgenform, upasswordpromptform, usearchform,
  usettingsform, usynhighlightermarkdown, upasswordgenerator, uabout, uappinfo
  { you can add units after this };

{$R *.res}

begin
  RequireDerivedFormResource:=True;
  Application.Title:='DA-CryptPad';
  Application.Scaled:=True;
  Application.ShowHint:=True;
  {$PUSH}{$WARN 5044 OFF}
  Application.MainFormOnTaskbar:=True;
  {$POP}
  Application.Initialize;
  Application.CreateForm(TMainForm, MainForm);
  Application.CreateForm(TFormAbout, FormAbout);
  Application.Run;
end.
