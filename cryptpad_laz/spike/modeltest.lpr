program modeltest;

{$mode objfpc}{$H+}
{$codepage UTF8}

uses
  SysUtils, Classes, uentrytreenode, udatamodel, uxmlmanager, umessages;

var
  model: TDataModel;
  child1, child2, grandchild: TEntryTreeNode;

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
  before, after: UTF8String;
begin
  before := '';
  after := '';

  model := TDataModel.Create;
  try
    model.Password := 'testpass123';

    child1 := model.AddNode(nil, 'Bankkonten');
    child1.Content := 'IBAN: DE12 3456 7890, PIN: 1234';
    child2 := model.AddNode(nil, 'Umlaut-Test äöüß€');
    child2.Content := 'Inhalt mit Umlauten: äöüß, Euro: €, Zeilen' + #10 + 'zweite Zeile';
    grandchild := model.AddNode(child1, 'Unterknoten');
    grandchild.Content := 'verschachtelter Inhalt';

    DumpTree(model.RootNode, 0, before);
    WriteUtf8File('out\model_before.txt', before);

    model.SaveFile('out\testmodel.cryptpad');
  finally
    model.Free;
  end;

  model := TDataModel.Create;
  try
    model.Password := 'testpass123';
    model.LoadFile('out\testmodel.cryptpad');

    DumpTree(model.RootNode, 0, after);
    WriteUtf8File('out\model_after.txt', after);
  finally
    model.Free;
  end;

  if before = after then
    WriteLn('MODEL ROUNDTRIP: OK')
  else
    WriteLn('MODEL ROUNDTRIP: MISMATCH (see out\model_before.txt / out\model_after.txt)');
end.
