program mdhltest;

{$mode objfpc}{$H+}
{$codepage UTF8}

uses
  Classes, SysUtils, usynhighlightermarkdown;

procedure DumpLine(hl: TSynMarkdownSyn; const Line: string);
begin
  WriteLn('LINE: ', Line);
  hl.SetLine(Line, 0);
  while not hl.GetEol do
  begin
    WriteLn('  token=[', hl.GetToken, '] kind=', Ord(hl.GetTokenID));
    hl.Next;
  end;
  WriteLn;
end;

var
  hl: TSynMarkdownSyn;
begin
  hl := TSynMarkdownSyn.Create(nil);
  try
    DumpLine(hl, '# Header');
    DumpLine(hl, 'Some **bold** and *italic* and `code`.');
    DumpLine(hl, '> quote');
    DumpLine(hl, '- item one');
    DumpLine(hl, '1. ordered item');
    DumpLine(hl, '```');
  finally
    hl.Free;
  end;
end.
