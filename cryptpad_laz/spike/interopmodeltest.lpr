program interopmodeltest;

{$mode objfpc}{$H+}
{$codepage UTF8}

uses
  SysUtils, Classes, uentrytreenode, udatamodel, uxmlmanager, umessages;

procedure DumpTree(Node: TEntryTreeNode; Indent: Integer; var Buf: UTF8String);
var
  i: Integer;
begin
  Buf := Buf + UTF8String(StringOfChar(' ', Indent * 2)) + 'title=' + UTF8String(Node.Title) +
         ' content=' + Node.Content + #10;
  for i := 0 to Node.ChildCount - 1 do
    DumpTree(Node.Children[i], Indent + 1, Buf);
end;

procedure WriteUtf8File(const Path: string; const Content: UTF8String);
var
  fs: TFileStream;
begin
  fs := TFileStream.Create(Path, fmCreate);
  try
    if Length(Content) > 0 then
      fs.WriteBuffer(Content[1], Length(Content));
  finally
    fs.Free;
  end;
end;

var
  cmd, password: string;
  model: TDataModel;
  bank, umlautNode, sub: TEntryTreeNode;
  dump: UTF8String;
begin
  cmd := ParamStr(1);
  password := UTF8String(ParamStr(2));

  if cmd = 'create-file' then
  begin
    model := TDataModel.Create;
    try
      model.Password := password;

      bank := model.AddNode(nil, 'Bankkonten');
      bank.Content := 'IBAN: DE12 3456 7890, PIN: 1234';
      umlautNode := model.AddNode(nil, 'Umlaut-Test äöüß€');
      umlautNode.Content := 'Inhalt mit Umlauten: äöüß, Euro: €, Zeilen' + #10 + 'zweite Zeile';
      sub := model.AddNode(bank, 'Unterknoten');
      sub.Content := 'verschachtelter Inhalt';

      model.SaveFile(ParamStr(3));
      WriteLn('wrote ', ParamStr(3));
    finally
      model.Free;
    end;
  end
  else if cmd = 'dump-file' then
  begin
    model := TDataModel.Create;
    dump := '';
    try
      model.Password := password;
      model.LoadFile(ParamStr(3));
      DumpTree(model.RootNode, 0, dump);
      WriteUtf8File(ParamStr(4), dump);
      WriteLn('wrote ', ParamStr(4));
    finally
      model.Free;
    end;
  end
  else
  begin
    WriteLn(StdErr, 'unknown command: ', cmd);
    Halt(1);
  end;
end.
