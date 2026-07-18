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
  usettingsform, usynhighlightermarkdown, upasswordgenerator
  { you can add units after this };

{$R *.res}

begin
  RequireDerivedFormResource:=True;
  Application.Scaled:=True;
  {$PUSH}{$WARN 5044 OFF}
  Application.MainFormOnTaskbar:=True;
  {$POP}
  Application.Initialize;
  Application.CreateForm(TMainForm, MainForm);
  Application.Run;
end.
