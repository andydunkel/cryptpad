unit uxmlmanager;

{$mode objfpc}{$H+}

interface

uses
  Classes, SysUtils, Laz2_DOM, Laz2_XMLRead, Laz2_XMLWrite,
  uentrytreenode, udatamodel, ucryptpadcrypto;

procedure XMLManagerSave(const FileName: string; Model: TDataModel);
procedure XMLManagerLoad(const FileName: string; Model: TDataModel);

implementation

const
  XML_VERSION = '1';
  XML_APP_NAME = 'DA-CryptPad';

function NodeToXML(Doc: TXMLDocument; Node: TEntryTreeNode): TDOMElement;
var
  titleEl, contentEl: TDOMElement;
  i: Integer;
begin
  Result := Doc.CreateElement('entry');

  titleEl := Doc.CreateElement('title');
  titleEl.AppendChild(Doc.CreateTextNode(Node.Title));
  Result.AppendChild(titleEl);

  contentEl := Doc.CreateElement('content');
  contentEl.SetAttribute('type', 'text');
  contentEl.AppendChild(Doc.CreateTextNode(UnicodeString(Node.Content)));
  Result.AppendChild(contentEl);

  for i := 0 to Node.ChildCount - 1 do
    Result.AppendChild(NodeToXML(Doc, Node.Children[i]));
end;

function StreamToUtf8String(Stream: TMemoryStream): UTF8String;
begin
  SetLength(Result, Stream.Size);
  if Stream.Size > 0 then
  begin
    Stream.Position := 0;
    Stream.ReadBuffer(Result[1], Stream.Size);
  end;
end;

procedure XMLManagerSave(const FileName: string; Model: TDataModel);
var
  doc: TXMLDocument;
  rootEl, fileInfo, appNameEl, entriesEl: TDOMElement;
  ms: TMemoryStream;
  xmlText: UTF8String;
  encryptedText: UTF8String;
  fs: TFileStream;
begin
  doc := TXMLDocument.Create;
  ms := TMemoryStream.Create;
  try
    rootEl := doc.CreateElement('xml');
    doc.AppendChild(rootEl);

    fileInfo := doc.CreateElement('fileinfo');
    appNameEl := doc.CreateElement('appname');
    appNameEl.SetAttribute('version', XML_VERSION);
    appNameEl.AppendChild(doc.CreateTextNode(XML_APP_NAME));
    fileInfo.AppendChild(appNameEl);
    rootEl.AppendChild(fileInfo);

    entriesEl := doc.CreateElement('entries');
    entriesEl.AppendChild(NodeToXML(doc, Model.RootNode));
    rootEl.AppendChild(entriesEl);

    WriteXMLFile(doc, ms);
    xmlText := StreamToUtf8String(ms);
  finally
    ms.Free;
    doc.Free;
  end;

  encryptedText := EncryptionWrapperEncryptFile(xmlText, Model.Password);

  fs := TFileStream.Create(FileName, fmCreate);
  try
    if Length(encryptedText) > 0 then
      fs.WriteBuffer(encryptedText[1], Length(encryptedText));
  finally
    fs.Free;
  end;
end;

function GetChildText(Element: TDOMElement; const TagName: string): UTF8String;
var
  list: TDOMNodeList;
  child: TDOMNode;
begin
  Result := '';
  list := Element.GetElementsByTagName(TagName);
  try
    if list.Count > 0 then
    begin
      child := list[0].FirstChild;
      if child <> nil then
        Result := UTF8String(child.NodeValue);
    end;
  finally
    list.Free;
  end;
end;

procedure ParseEntries(Element: TDOMNode; ParentNode: TEntryTreeNode);
var
  children: TDOMNodeList;
  i: Integer;
  childNode: TDOMNode;
  childElement: TDOMElement;
  title: string;
  content: UTF8String;
  treeNode: TEntryTreeNode;
begin
  children := Element.ChildNodes;
  for i := 0 to children.Count - 1 do
  begin
    childNode := children[i];
    if childNode.NodeType = ELEMENT_NODE then
    begin
      childElement := TDOMElement(childNode);
      if childElement.NodeName = 'entry' then
      begin
        title := string(GetChildText(childElement, 'title'));
        content := GetChildText(childElement, 'content');

        treeNode := TEntryTreeNode.Create(title);
        treeNode.Content := content;
        ParentNode.Add(treeNode);

        if childElement.HasChildNodes then
          ParseEntries(childElement, treeNode);
      end;
    end;
  end;
end;

procedure XMLManagerLoad(const FileName: string; Model: TDataModel);
var
  fs: TFileStream;
  encryptedText: UTF8String;
  xmlText: UTF8String;
  ms: TMemoryStream;
  doc: TXMLDocument;
  docElement: TDOMElement;
  entryList: TDOMNodeList;
begin
  fs := TFileStream.Create(FileName, fmOpenRead or fmShareDenyNone);
  try
    SetLength(encryptedText, fs.Size);
    if fs.Size > 0 then
      fs.ReadBuffer(encryptedText[1], fs.Size);
  finally
    fs.Free;
  end;

  xmlText := EncryptionWrapperDecryptMessage(encryptedText, Model.Password);

  ms := TMemoryStream.Create;
  doc := nil;
  try
    if Length(xmlText) > 0 then
      ms.WriteBuffer(xmlText[1], Length(xmlText));
    ms.Position := 0;
    ReadXMLFile(doc, ms);

    Model.ClearModel;

    docElement := doc.DocumentElement;
    entryList := docElement.GetElementsByTagName('entry');
    try
      if entryList.Count > 0 then
        ParseEntries(entryList[0], Model.RootNode);
    finally
      entryList.Free;
    end;
  finally
    doc.Free;
    ms.Free;
  end;
end;

end.
