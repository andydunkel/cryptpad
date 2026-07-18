unit main;

{$mode objfpc}{$H+}
{$codepage UTF8}

interface

uses
  Classes, SysUtils, Forms, Controls, Graphics, Dialogs, Menus, ComCtrls,
  ExtCtrls, SynEdit, ImgList, ActnList, uabout,
  {$IFDEF WINDOWS}Windows,{$ENDIF}
  uentrytreenode, udatamodel, uxmlmanager, umessages, uappsettings,
  usynhighlightermarkdown,
  uencryptiondialog, upasswordpromptform, uencryptpasswordform,
  upasswordgenform, usearchform, usettingsform;

type

  { TMainForm }

  TMainForm = class(TForm)
    ActionAbout: TAction;
    ActionList: TActionList;
    FEditor: TSynEdit;
    FOpenDialog: TOpenDialog;
    FSaveDialog: TSaveDialog;
    FSplitter: TSplitter;
    FToolBar: TToolBar;
    FToolImages: TImageList;
    FTreeView: TTreeView;
    MainMenu1: TMainMenu;
    MenuFile: TMenuItem;
    MenuFileNew: TMenuItem;
    MenuFileOpen: TMenuItem;
    MenuFileSave: TMenuItem;
    MenuFileSaveAs: TMenuItem;
    MenuFileSep1: TMenuItem;
    MenuFileExit: TMenuItem;
    MenuNode: TMenuItem;
    MenuNodeAddSibling: TMenuItem;
    MenuNodeAddChild: TMenuItem;
    MenuNodeRename: TMenuItem;
    MenuNodeDelete: TMenuItem;
    MenuTools: TMenuItem;
    MenuToolsPasswordGen: TMenuItem;
    MenuToolsSearch: TMenuItem;
    MenuToolsSettings: TMenuItem;
    MenuToolsTextEncryption: TMenuItem;
    MenuHelp: TMenuItem;
    MenuHelpAbout: TMenuItem;
    ToolButton1: TToolButton;
    ToolButton2: TToolButton;
    ToolButtonNew: TToolButton;
    ToolButtonOpen: TToolButton;
    ToolButtonSave: TToolButton;
    ToolButtonSep1: TToolButton;
    ToolButtonAddSibling: TToolButton;
    ToolButtonAddChild: TToolButton;
    ToolButtonDelete: TToolButton;
    ToolButtonSep2: TToolButton;
    ToolButtonEncrypt: TToolButton;

    procedure ActionAboutExecute(Sender: TObject);
    procedure TreeSelectionChanged(Sender: TObject);
    procedure EditorChange(Sender: TObject);
    procedure TreeDragOver(Sender, Source: TObject; X, Y: Integer; State: TDragState; var Accept: Boolean);
    procedure TreeDragDrop(Sender, Source: TObject; X, Y: Integer);

    procedure DoNew(Sender: TObject);
    procedure DoOpen(Sender: TObject);
    procedure DoSave(Sender: TObject);
    procedure DoSaveAs(Sender: TObject);

    procedure DoAddSiblingNode(Sender: TObject);
    procedure DoAddChildNode(Sender: TObject);
    procedure DoRenameNode(Sender: TObject);
    procedure DoDeleteNode(Sender: TObject);
    procedure DoSettings(Sender: TObject);
    procedure DoTextEncryption(Sender: TObject);
    procedure DoPasswordGenerator(Sender: TObject);
    procedure DoSearch(Sender: TObject);
    procedure MenuFileExitClick(Sender: TObject);
  private
    FModel: TDataModel;
    FCurrentFileName: string;
    FSelectedModelNode: TEntryTreeNode;
    FSuppressEditorChange: Boolean;
    // Not registered as an IDE package, so it can't be a .lfm-streamed component;
    // created and assigned in code instead (see Create).
    FMarkdownHighlighter: TSynMarkdownSyn;

    procedure ApplyToolBarHints;
    procedure ApplyTranslations;

    procedure RebuildTree;
    procedure PopulateTreeNode(ParentTVNode: TTreeNode; ModelNode: TEntryTreeNode);
    function GetSelectedTVModelNode(TVNode: TTreeNode): TEntryTreeNode;
    function IsAncestor(Ancestor, Node: TEntryTreeNode): Boolean;
    function PromptText(const ATitle, APrompt, ADefault: string; out AValue: string): Boolean;
    function SaveToFile(const FileName: string): Boolean;
    function FindTVNode(ModelNode: TEntryTreeNode): TTreeNode;
    procedure SelectModelNode(ModelNode: TEntryTreeNode);

    procedure UpdateTitle;
    procedure LoadNodeIntoEditor(ModelNode: TEntryTreeNode);
  public
    constructor Create(AOwner: TComponent); override;
    destructor Destroy; override;
  end;

var
  MainForm: TMainForm;

implementation

{$R *.lfm}

function ResolveLanguage(const Lang: string): string;
{$IFDEF WINDOWS}
var
  buf: array[0..8] of Char;
{$ENDIF}
begin
  if Lang <> 'system' then
  begin
    Result := Lang;
    Exit;
  end;

  Result := 'en';
  {$IFDEF WINDOWS}
  if GetLocaleInfo(GetUserDefaultLCID, LOCALE_SISO639LANGNAME, buf, SizeOf(buf)) > 0 then
    if LowerCase(buf) = 'de' then
      Result := 'de';
  {$ENDIF}
end;

{ TMainForm }

constructor TMainForm.Create(AOwner: TComponent);
begin
  inherited Create(AOwner);

  umessages.SetLanguage(ResolveLanguage(uappsettings.GetLanguage));

  FModel := TDataModel.Create;
  FCurrentFileName := '';
  FSelectedModelNode := nil;
  FSuppressEditorChange := False;

  FMarkdownHighlighter := TSynMarkdownSyn.Create(Self);
  FEditor.Highlighter := FMarkdownHighlighter;

  ApplyToolBarHints;
  ApplyTranslations;

  RebuildTree;
  UpdateTitle;
end;

destructor TMainForm.Destroy;
begin
  FModel.Free;
  inherited Destroy;
end;

procedure TMainForm.ApplyToolBarHints;
begin
  ToolButtonNew.Hint := GetString('tooltip.new');
  ToolButtonOpen.Hint := GetString('tooltip.open');
  ToolButtonSave.Hint := GetString('tooltip.save');
  ToolButtonAddSibling.Hint := GetString('tooltip.newsibling');
  ToolButtonAddChild.Hint := GetString('tooltip.newchild');
  ToolButtonDelete.Hint := GetString('tooltip.deletenode');
  ToolButtonEncrypt.Hint := GetString('menu.encryption.textencryption');
end;

procedure TMainForm.ApplyTranslations;
begin
  MenuToolsPasswordGen.Caption := GetString('menu.encryption.passwordgen');
  MenuToolsSearch.Caption := GetString('menu.tools.search');
  MenuToolsSettings.Caption := GetString('menu.edit.settings');
  MenuToolsTextEncryption.Caption := GetString('menu.encryption.textencryption');
end;

procedure TMainForm.RebuildTree;
var
  i: Integer;
begin
  FTreeView.Items.Clear;
  for i := 0 to FModel.RootNode.ChildCount - 1 do
    PopulateTreeNode(nil, FModel.RootNode.Children[i]);
  if FTreeView.Items.Count > 0 then
    FTreeView.Items[0].Expand(True);
end;

procedure TMainForm.PopulateTreeNode(ParentTVNode: TTreeNode; ModelNode: TEntryTreeNode);
var
  tvNode: TTreeNode;
  i: Integer;
begin
  if ParentTVNode = nil then
    tvNode := FTreeView.Items.AddObject(nil, ModelNode.Title, TObject(ModelNode))
  else
    tvNode := FTreeView.Items.AddChildObject(ParentTVNode, ModelNode.Title, TObject(ModelNode));

  for i := 0 to ModelNode.ChildCount - 1 do
    PopulateTreeNode(tvNode, ModelNode.Children[i]);
end;

function TMainForm.GetSelectedTVModelNode(TVNode: TTreeNode): TEntryTreeNode;
begin
  if (TVNode <> nil) and (TVNode.Data <> nil) then
    Result := TEntryTreeNode(TVNode.Data)
  else
    Result := nil;
end;

procedure TMainForm.LoadNodeIntoEditor(ModelNode: TEntryTreeNode);
begin
  FSuppressEditorChange := True;
  try
    if ModelNode = nil then
      FEditor.Lines.Text := ''
    else
      FEditor.Lines.Text := string(ModelNode.Content);
    FEditor.Enabled := ModelNode <> nil;
  finally
    FSuppressEditorChange := False;
  end;
end;

procedure TMainForm.TreeSelectionChanged(Sender: TObject);
begin
  FSelectedModelNode := GetSelectedTVModelNode(FTreeView.Selected);
  LoadNodeIntoEditor(FSelectedModelNode);
end;

procedure TMainForm.ActionAboutExecute(Sender: TObject);
begin
  FormAbout.ShowModal;
end;

procedure TMainForm.EditorChange(Sender: TObject);
begin
  if FSuppressEditorChange or (FSelectedModelNode = nil) then Exit;
  FModel.SetNodeContent(FSelectedModelNode, UTF8String(FEditor.Lines.Text));
end;

function TMainForm.IsAncestor(Ancestor, Node: TEntryTreeNode): Boolean;
var
  p: TEntryTreeNode;
begin
  Result := False;
  p := Node.Parent;
  while p <> nil do
  begin
    if p = Ancestor then
    begin
      Result := True;
      Exit;
    end;
    p := p.Parent;
  end;
end;

procedure TMainForm.TreeDragOver(Sender, Source: TObject; X, Y: Integer; State: TDragState;
  var Accept: Boolean);
begin
  Accept := (Source = FTreeView) and (FTreeView.GetNodeAt(X, Y) <> nil);
end;

procedure TMainForm.TreeDragDrop(Sender, Source: TObject; X, Y: Integer);
var
  targetTVNode: TTreeNode;
  draggedModelNode, targetModelNode: TEntryTreeNode;
begin
  targetTVNode := FTreeView.GetNodeAt(X, Y);
  if targetTVNode = nil then Exit;

  draggedModelNode := FSelectedModelNode;
  targetModelNode := GetSelectedTVModelNode(targetTVNode);

  if (draggedModelNode = nil) or (targetModelNode = nil) or (draggedModelNode = targetModelNode) then
    Exit;

  // Refuse to drop a node onto its own descendant (would create a cycle)
  if IsAncestor(draggedModelNode, targetModelNode) then
    Exit;

  targetModelNode.Add(draggedModelNode); // Add() detaches from its current parent first

  RebuildTree;
  SelectModelNode(draggedModelNode);
end;

function TMainForm.PromptText(const ATitle, APrompt, ADefault: string; out AValue: string): Boolean;
var
  s: string;
begin
  s := InputBox(ATitle, APrompt, ADefault);
  // InputBox returns ADefault unchanged when the user cancels, so we can't
  // distinguish cancel from "kept default" here; treat empty result as cancel.
  Result := True;
  AValue := s;
end;

procedure TMainForm.UpdateTitle;
var
  fn: string;
begin
  if FCurrentFileName = '' then
    fn := GetString('window.title.untitled')
  else
    fn := ExtractFileName(FCurrentFileName);
  Caption := fn + ' - DA-CryptPad';
end;

procedure TMainForm.DoNew(Sender: TObject);
begin
  FModel.Free;
  FModel := TDataModel.Create;
  FCurrentFileName := '';
  FSelectedModelNode := nil;
  RebuildTree;
  LoadNodeIntoEditor(nil);
  UpdateTitle;
end;

procedure TMainForm.DoOpen(Sender: TObject);
var
  pw: string;
begin
  if not FOpenDialog.Execute then Exit;
  if not upasswordpromptform.PromptPassword(Self, GetString('password.decrypt.title'), GetString('password.decrypt.label'), pw) then Exit;

  try
    FModel.Free;
    FModel := TDataModel.Create;
    FModel.Password := UTF8String(pw);
    FModel.LoadFile(FOpenDialog.FileName);
    FCurrentFileName := FOpenDialog.FileName;
    uappsettings.AddRecentFile(FCurrentFileName);
    RebuildTree;
    LoadNodeIntoEditor(nil);
    UpdateTitle;
  except
    on E: Exception do
      MessageDlg(GetStringF('dialog.error.load', [E.Message]), mtError, [mbOK], 0);
  end;
end;

function TMainForm.SaveToFile(const FileName: string): Boolean;
begin
  Result := False;
  try
    FModel.SaveFile(FileName);
    FCurrentFileName := FileName;
    uappsettings.AddRecentFile(FCurrentFileName);
    UpdateTitle;
    Result := True;
  except
    on E: Exception do
      MessageDlg(GetStringF('dialog.error.save', [E.Message]), mtError, [mbOK], 0);
  end;
end;

procedure TMainForm.DoSave(Sender: TObject);
var
  pw: string;
begin
  if FCurrentFileName = '' then
  begin
    DoSaveAs(Sender);
    Exit;
  end;

  if FModel.Password = '' then
  begin
    if not uencryptpasswordform.PromptNewPassword(Self, GetString('password.encrypt.title'), pw) then Exit;
    FModel.Password := UTF8String(pw);
  end;

  SaveToFile(FCurrentFileName);
end;

procedure TMainForm.DoSaveAs(Sender: TObject);
var
  pw: string;
begin
  if not FSaveDialog.Execute then Exit;

  if not uencryptpasswordform.PromptNewPassword(Self, GetString('password.encrypt.title'), pw) then Exit;
  FModel.Password := UTF8String(pw);

  SaveToFile(FSaveDialog.FileName);
end;

procedure TMainForm.DoAddSiblingNode(Sender: TObject);
var
  title: string;
  newNode: TEntryTreeNode;
begin
  if not PromptText(GetString('nodetitle.new.title'), GetString('nodetitle.label'), '', title) then Exit;
  if title = '' then Exit;

  newNode := FModel.AddNode(nil, title);
  RebuildTree;
  LoadNodeIntoEditor(newNode);
end;

procedure TMainForm.DoAddChildNode(Sender: TObject);
var
  title: string;
  newNode: TEntryTreeNode;
begin
  if FSelectedModelNode = nil then
  begin
    DoAddSiblingNode(Sender);
    Exit;
  end;

  if not PromptText(GetString('nodetitle.new.title'), GetString('nodetitle.label'), '', title) then Exit;
  if title = '' then Exit;

  newNode := FModel.AddNode(FSelectedModelNode, title);
  RebuildTree;
  LoadNodeIntoEditor(newNode);
end;

procedure TMainForm.DoRenameNode(Sender: TObject);
var
  title: string;
begin
  if FSelectedModelNode = nil then Exit;
  if not PromptText(GetString('nodetitle.edit.title'), GetString('nodetitle.label'), FSelectedModelNode.Title, title) then Exit;
  if title = '' then Exit;

  FModel.SetNodeTitle(FSelectedModelNode, title);
  RebuildTree;
end;

procedure TMainForm.DoDeleteNode(Sender: TObject);
begin
  if FSelectedModelNode = nil then Exit;
  if MessageDlg(GetString('dialog.deletenode.message'), mtConfirmation, [mbYes, mbNo], 0) <> mrYes then Exit;

  FModel.DeleteNode(FSelectedModelNode);
  FSelectedModelNode := nil;
  RebuildTree;
  LoadNodeIntoEditor(nil);
end;

procedure TMainForm.DoSettings(Sender: TObject);
begin
  usettingsform.ShowSettingsDialog(Self);
end;

procedure TMainForm.DoTextEncryption(Sender: TObject);
begin
  uencryptiondialog.ShowEncryptionWindow;
end;

procedure TMainForm.DoPasswordGenerator(Sender: TObject);
begin
  upasswordgenform.ShowPasswordGenerator(Self);
end;

function TMainForm.FindTVNode(ModelNode: TEntryTreeNode): TTreeNode;
var
  i: Integer;
begin
  Result := nil;
  for i := 0 to FTreeView.Items.Count - 1 do
    if TEntryTreeNode(FTreeView.Items[i].Data) = ModelNode then
    begin
      Result := FTreeView.Items[i];
      Exit;
    end;
end;

procedure TMainForm.SelectModelNode(ModelNode: TEntryTreeNode);
var
  tvNode: TTreeNode;
begin
  tvNode := FindTVNode(ModelNode);
  if tvNode = nil then Exit;

  tvNode.MakeVisible;
  FTreeView.Selected := tvNode;
  TreeSelectionChanged(nil);
end;

procedure TMainForm.DoSearch(Sender: TObject);
var
  node: TEntryTreeNode;
begin
  node := usearchform.ShowSearchDialog(Self, FModel);
  if node <> nil then
    SelectModelNode(node);
end;

procedure TMainForm.MenuFileExitClick(Sender: TObject);
begin
  Close;
end;

end.
