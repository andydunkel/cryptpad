unit main;

{$mode objfpc}{$H+}
{$codepage UTF8}

interface

{$WARN 5079 off}
uses
  Classes, SysUtils, Forms, Controls, Graphics, Dialogs, Menus, ComCtrls,
  ExtCtrls, SynEdit, SynEditWrappedView, ImgList, ActnList, LCLProc, Process, uabout,
  {$IFDEF WINDOWS}Windows,{$ENDIF}
  uentrytreenode, udatamodel, uxmlmanager, umessages, uappsettings,
  usynhighlightermarkdown,
  uencryptiondialog, upasswordpromptform, uencryptpasswordform,
  upasswordgenform, usearchform, usettingsform;
{$WARN 5079 on}

type

  { TMainForm }

  TMainForm = class(TForm)
    ActionAbout: TAction;
    ActionAddChild: TAction;
    ActionAddSibling: TAction;
    ActionCheckUpdates: TAction;
    ActionCopy: TAction;
    ActionCut: TAction;
    ActionDelete: TAction;
    ActionExit: TAction;
    ActionList: TActionList;
    ActionMoveDown: TAction;
    ActionMoveUp: TAction;
    ActionNew: TAction;
    ActionNewSibling: TAction;
    ActionOpen: TAction;
    ActionPaste: TAction;
    ActionPasswordGen: TAction;
    ActionRename: TAction;
    ActionSave: TAction;
    ActionSaveAs: TAction;
    ActionSearch: TAction;
    ActionSetPassword: TAction;
    ActionSettings: TAction;
    ActionTextEncryption: TAction;
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
    MenuFileRecent: TMenuItem;
    MenuFileSep2: TMenuItem;
    MenuFileExit: TMenuItem;
    MenuEdit: TMenuItem;
    MenuEditCut: TMenuItem;
    MenuEditCopy: TMenuItem;
    MenuEditPaste: TMenuItem;
    MenuNode: TMenuItem;
    MenuNodeAddSibling: TMenuItem;
    MenuNodeNewSibling: TMenuItem;
    MenuNodeAddChild: TMenuItem;
    MenuNodeRename: TMenuItem;
    MenuNodeDelete: TMenuItem;
    MenuNodeMoveUp: TMenuItem;
    MenuNodeMoveDown: TMenuItem;
    MenuTools: TMenuItem;
    MenuToolsPasswordGen: TMenuItem;
    MenuToolsSearch: TMenuItem;
    MenuToolsSep1: TMenuItem;
    MenuToolsSettings: TMenuItem;
    MenuToolsSetPassword: TMenuItem;
    MenuToolsTextEncryption: TMenuItem;
    MenuHelp: TMenuItem;
    MenuHelpCheckUpdates: TMenuItem;
    MenuHelpSep1: TMenuItem;
    MenuHelpAbout: TMenuItem;
    ToolButton4: TToolButton;
    ToolButton5: TToolButton;
    TreePopupMenu: TPopupMenu;
    PopupNewMainNode: TMenuItem;
    PopupNewSibling: TMenuItem;
    PopupNewChild: TMenuItem;
    PopupSep1: TMenuItem;
    PopupEditNode: TMenuItem;
    PopupDeleteNode: TMenuItem;
    PopupSep2: TMenuItem;
    PopupMoveUp: TMenuItem;
    PopupMoveDown: TMenuItem;
    ToolButton1: TToolButton;
    ToolButton2: TToolButton;
    ToolButton3: TToolButton;
    ToolButtonNew: TToolButton;
    ToolButtonOpen: TToolButton;
    ToolButtonSave: TToolButton;
    ToolButtonSep1: TToolButton;
    ToolButtonCut: TToolButton;
    ToolButtonCopy: TToolButton;
    ToolButtonPaste: TToolButton;
    ToolButtonSep3: TToolButton;
    ToolButtonAddSibling: TToolButton;
    ToolButtonNewSibling: TToolButton;
    ToolButtonAddChild: TToolButton;
    ToolButtonDelete: TToolButton;
    ToolButtonEncrypt: TToolButton;
    ToolButtonSep4: TToolButton;
    ToolButtonMoveUp: TToolButton;
    ToolButtonMoveDown: TToolButton;

    procedure ActionAboutExecute(Sender: TObject);
    procedure ActionCheckUpdatesExecute(Sender: TObject);
    procedure ActionCutExecute(Sender: TObject);
    procedure ActionCopyExecute(Sender: TObject);
    procedure ActionPasteExecute(Sender: TObject);
    procedure ActionNewExecute(Sender: TObject);
    procedure ActionOpenExecute(Sender: TObject);
    procedure ActionSaveExecute(Sender: TObject);
    procedure ActionSaveAsExecute(Sender: TObject);
    procedure ActionExitExecute(Sender: TObject);
    procedure ActionAddSiblingExecute(Sender: TObject);
    procedure ActionNewSiblingExecute(Sender: TObject);
    procedure ActionAddChildExecute(Sender: TObject);
    procedure ActionRenameExecute(Sender: TObject);
    procedure ActionDeleteExecute(Sender: TObject);
    procedure ActionMoveUpExecute(Sender: TObject);
    procedure ActionMoveDownExecute(Sender: TObject);
    procedure ActionSettingsExecute(Sender: TObject);
    procedure ActionTextEncryptionExecute(Sender: TObject);
    procedure ActionPasswordGenExecute(Sender: TObject);
    procedure ActionSetPasswordExecute(Sender: TObject);
    procedure ActionSearchExecute(Sender: TObject);

    procedure TreeSelectionChanged(Sender: TObject);
    procedure TreeExpanded(Sender: TObject; Node: TTreeNode);
    procedure TreeCollapsed(Sender: TObject; Node: TTreeNode);
    procedure EditorChange(Sender: TObject);
    procedure TreeDragOver(Sender, Source: TObject; X, Y: Integer; State: TDragState; var Accept: Boolean);
    procedure TreeDragDrop(Sender, Source: TObject; X, Y: Integer);
    procedure FormCloseQuery(Sender: TObject; var CanClose: Boolean);
  private
    FModel: TDataModel;
    FCurrentFileName: string;
    FDirty: Boolean;
    FSelectedModelNode: TEntryTreeNode;
    FSuppressEditorChange: Boolean;
    // Not registered as an IDE package, so it can't be a .lfm-streamed component;
    // created and assigned in code instead (see Create).
    FMarkdownHighlighter: TSynMarkdownSyn;

    procedure ApplyTranslations;

    procedure RebuildTree;
    procedure PopulateTreeNode(ParentTVNode: TTreeNode; ModelNode: TEntryTreeNode);
    procedure SyncNodeIcons(TVNode: TTreeNode);
    function GetSelectedTVModelNode(TVNode: TTreeNode): TEntryTreeNode;
    function IsAncestor(Ancestor, Node: TEntryTreeNode): Boolean;
    function PromptText(const ATitle, APrompt, ADefault: string; out AValue: string): Boolean;
    function SaveToFile(const FileName: string): Boolean;
    function FindTVNode(ModelNode: TEntryTreeNode): TTreeNode;
    procedure SelectModelNode(ModelNode: TEntryTreeNode; FocusEditor: Boolean = True);
    procedure SelectFirstNode(FocusEditor: Boolean = True);
    procedure MarkDirty;
    function ShowSaveConfirmation: TModalResult; // mrYes/mrNo/mrCancel

    procedure UpdateTitle;
    procedure LoadNodeIntoEditor(ModelNode: TEntryTreeNode);

    procedure DoOpenFile(const AFileName: string);
    procedure DoOpenRecentFile(Sender: TObject);
    procedure DoClearRecentFiles(Sender: TObject);
    procedure UpdateRecentFilesMenu;
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

  // SynEdit only auto-populates its default keyboard bindings (arrow keys, Home/End,
  // Ctrl+Home/End, etc.) when created directly in code. When streamed from a .lfm (as
  // FEditor is), that step is skipped because the .lfm is expected to carry the full
  // Keystrokes list itself -- and ours currently stores an empty one, leaving the editor
  // with no cursor/navigation keys at all. Force the defaults back in explicitly.
  FEditor.Keystrokes.ResetDefaults;
  TLazSynEditLineWrapPlugin.Create(FEditor);

  ApplyTranslations;
  UpdateRecentFilesMenu;

  RebuildTree;
  SelectFirstNode(False);
  UpdateTitle;

  // Support opening a .cryptpad file passed on the command line (e.g. via
  // the file association / "Open with" registry entry, or double-click).
  if (ParamCount >= 1) and (ParamStr(1) <> '') and FileExists(ParamStr(1)) then
    DoOpenFile(ParamStr(1));
end;

destructor TMainForm.Destroy;
begin
  FModel.Free;
  inherited Destroy;
end;

procedure TMainForm.ApplyTranslations;
begin
  MenuFile.Caption := GetString('menu.file');
  MenuEdit.Caption := GetString('menu.edit');
  MenuNode.Caption := GetString('menu.node');
  MenuTools.Caption := GetString('menu.tools');
  MenuHelp.Caption := GetString('menu.help');

  ActionNew.Caption := GetString('menu.file.new');
  ActionNew.Hint := GetString('tooltip.new');
  ActionNew.ShortCut := TextToShortCut('Ctrl+N');
  ActionOpen.Caption := GetString('menu.file.open');
  ActionOpen.Hint := GetString('tooltip.open');
  ActionOpen.ShortCut := TextToShortCut('Ctrl+O');
  ActionSave.Caption := GetString('menu.file.save');
  ActionSave.Hint := GetString('tooltip.save');
  ActionSave.ShortCut := TextToShortCut('Ctrl+S');
  ActionSaveAs.Caption := GetString('menu.file.saveas');
  ActionSaveAs.ShortCut := TextToShortCut('Ctrl+Shift+S');
  MenuFileRecent.Caption := GetString('menu.file.recent');
  ActionExit.Caption := GetString('menu.file.exit');
  ActionAbout.Caption := GetStringF('menu.help.about', ['DA-CryptPad']);
  ActionAbout.Hint := GetStringF('tooltip.about', ['DA-CryptPad']);
  ActionCheckUpdates.Caption := GetString('menu.help.checkupdates');
  // ActionAddSibling always inserts at root level (like Java's onNewMainNode),
  // so it uses the "main node" caption/tooltip; ActionNewSibling is the true sibling insert.
  ActionAddSibling.Caption := GetString('popup.newmainnode');
  ActionAddSibling.Hint := GetString('tooltip.newmainnode');
  ActionNewSibling.Caption := GetString('popup.newsibling');
  ActionNewSibling.Hint := GetString('tooltip.newsibling');
  ActionAddChild.Caption := GetString('popup.newchild');
  ActionAddChild.Hint := GetString('tooltip.newchild');
  ActionRename.Caption := GetString('popup.editnode');
  ActionRename.Hint := GetString('tooltip.editnode');
  ActionDelete.Caption := GetString('popup.deletenode');
  ActionDelete.Hint := GetString('tooltip.deletenode');
  ActionMoveUp.Caption := GetString('menu.edit.moveup');
  ActionMoveUp.Hint := GetString('tooltip.moveup');
  ActionMoveUp.ShortCut := TextToShortCut('Ctrl+Up');
  ActionMoveDown.Caption := GetString('menu.edit.movedown');
  ActionMoveDown.Hint := GetString('tooltip.movedown');
  ActionMoveDown.ShortCut := TextToShortCut('Ctrl+Down');

  ActionCut.Caption := GetString('menu.edit.cut');
  ActionCut.Hint := GetString('tooltip.cut');
  ActionCut.ShortCut := TextToShortCut('Ctrl+X');
  ActionCopy.Caption := GetString('menu.edit.copy');
  ActionCopy.Hint := GetString('tooltip.copy');
  ActionCopy.ShortCut := TextToShortCut('Ctrl+C');
  ActionPaste.Caption := GetString('menu.edit.paste');
  ActionPaste.Hint := GetString('tooltip.paste');
  ActionPaste.ShortCut := TextToShortCut('Ctrl+V');

  ActionPasswordGen.Caption := GetString('menu.encryption.passwordgen');
  ActionSearch.Caption := GetString('menu.tools.search');
  ActionSettings.Caption := GetString('menu.edit.settings');
  ActionSetPassword.Caption := GetString('menu.encryption.setpassword');
  ActionSetPassword.Hint := GetString('menu.encryption.setpassword');
  ActionTextEncryption.Caption := GetString('menu.encryption.textencryption');
  ActionTextEncryption.Hint := GetString('menu.encryption.textencryption');
end;

const
  IMG_FOLDER_CLOSED = 11;
  IMG_FOLDER_OPEN = 12;
  IMG_LEAF = 13;

procedure TMainForm.RebuildTree;
var
  i: Integer;
begin
  FTreeView.Items.Clear;
  for i := 0 to FModel.RootNode.ChildCount - 1 do
    PopulateTreeNode(nil, FModel.RootNode.Children[i]);
  if FTreeView.Items.Count > 0 then
    FTreeView.Items[0].Expand(True);
  SyncNodeIcons(FTreeView.Items.GetFirstNode);
end;

procedure TMainForm.SyncNodeIcons(TVNode: TTreeNode);
begin
  while TVNode <> nil do
  begin
    if TVNode.HasChildren then
    begin
      if TVNode.Expanded then
        TVNode.ImageIndex := IMG_FOLDER_OPEN
      else
        TVNode.ImageIndex := IMG_FOLDER_CLOSED;
    end
    else
      TVNode.ImageIndex := IMG_LEAF;
    TVNode.SelectedIndex := TVNode.ImageIndex;

    SyncNodeIcons(TVNode.GetFirstChild);
    TVNode := TVNode.GetNextSibling;
  end;
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

procedure TMainForm.TreeExpanded(Sender: TObject; Node: TTreeNode);
begin
  if Node = nil then Exit;
  Node.ImageIndex := IMG_FOLDER_OPEN;
  Node.SelectedIndex := IMG_FOLDER_OPEN;
end;

procedure TMainForm.TreeCollapsed(Sender: TObject; Node: TTreeNode);
begin
  if Node = nil then Exit;
  Node.ImageIndex := IMG_FOLDER_CLOSED;
  Node.SelectedIndex := IMG_FOLDER_CLOSED;
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

procedure TMainForm.ActionCheckUpdatesExecute(Sender: TObject);
var
  updaterPath: string;
  proc: TProcess;
begin
  {$IFDEF WINDOWS}
  updaterPath := ExtractFilePath(ParamStr(0)) + 'updater.exe';
  {$ELSE}
  updaterPath := ExtractFilePath(ParamStr(0)) + 'updater';
  {$ENDIF}
  if not FileExists(updaterPath) then
  begin
    MessageDlg(GetStringF('menu.help.checkupdates.notfound', [updaterPath]), GetString('dialog.error.title'),
      mtError, [mbOK], 0);
    Exit;
  end;

  proc := TProcess.Create(nil);
  try
    proc.Executable := updaterPath;
    proc.CurrentDirectory := ExtractFilePath(updaterPath);
    proc.Parameters.Add('updater.ini');
    proc.Parameters.Add(ResolveLanguage(uappsettings.GetLanguage));
    proc.Execute;
  finally
    proc.Free;
  end;
end;

procedure TMainForm.ActionCutExecute(Sender: TObject);
begin
  FEditor.CutToClipboard;
end;

procedure TMainForm.ActionCopyExecute(Sender: TObject);
begin
  FEditor.CopyToClipboard;
end;

procedure TMainForm.ActionPasteExecute(Sender: TObject);
begin
  FEditor.PasteFromClipboard;
end;

procedure TMainForm.EditorChange(Sender: TObject);
begin
  if FSuppressEditorChange or (FSelectedModelNode = nil) then Exit;
  FModel.SetNodeContent(FSelectedModelNode, UTF8String(FEditor.Lines.Text));
  MarkDirty;
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

  MarkDirty;
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
  if FDirty then
    Caption := Caption + ' *';
end;

procedure TMainForm.MarkDirty;
begin
  if not FDirty then
  begin
    FDirty := True;
    UpdateTitle;
  end;
end;

function TMainForm.ShowSaveConfirmation: TModalResult;
begin
  if not FDirty then
  begin
    Result := mrNo;
    Exit;
  end;

  Result := MessageDlg(GetString('dialog.save.message'), GetString('dialog.save.title'),
    mtConfirmation, [mbYes, mbNo, mbCancel], 0);

  if Result = mrYes then
    ActionSaveExecute(Self);
end;

procedure TMainForm.FormCloseQuery(Sender: TObject; var CanClose: Boolean);
begin
  CanClose := True;

  if FDirty and (FCurrentFileName <> '') and (FModel.Password <> '') then
  begin
    try
      FModel.SaveFile(FCurrentFileName);
      FDirty := False;
    except
      on E: Exception do
      begin
        MessageDlg(GetStringF('dialog.error.autosave', [E.Message]), GetString('dialog.error.title'),
          mtError, [mbOK], 0);
        CanClose := MessageDlg(GetString('dialog.confirmexit.message'), GetString('dialog.confirmexit.title'),
          mtWarning, [mbYes, mbNo], 0) = mrYes;
      end;
    end;
  end
  else
  begin
    if ShowSaveConfirmation = mrCancel then
      CanClose := False;
  end;
end;

procedure TMainForm.ActionNewExecute(Sender: TObject);
begin
  if ShowSaveConfirmation = mrCancel then Exit;

  FModel.Free;
  FModel := TDataModel.Create;
  FCurrentFileName := '';
  FDirty := False;
  FSelectedModelNode := nil;
  RebuildTree;
  SelectFirstNode;
  UpdateTitle;
end;

procedure TMainForm.ActionOpenExecute(Sender: TObject);
begin
  if ShowSaveConfirmation = mrCancel then Exit;
  if not FOpenDialog.Execute then Exit;
  DoOpenFile(FOpenDialog.FileName);
end;

procedure TMainForm.DoOpenFile(const AFileName: string);
var
  pw: string;
begin
  if not upasswordpromptform.PromptPassword(Self, GetString('password.decrypt.title'), GetString('password.decrypt.label'), pw) then Exit;

  try
    FModel.Free;
    FModel := TDataModel.Create;
    FModel.Password := UTF8String(pw);
    FModel.LoadFile(AFileName);
    FCurrentFileName := AFileName;
    FDirty := False;
    uappsettings.AddRecentFile(FCurrentFileName);
    UpdateRecentFilesMenu;
    RebuildTree;
    SelectFirstNode;
    UpdateTitle;
  except
    on E: Exception do
      MessageDlg(GetStringF('dialog.error.load', [E.Message]), mtError, [mbOK], 0);
  end;
end;

procedure TMainForm.DoOpenRecentFile(Sender: TObject);
var
  fileName: string;
begin
  fileName := (Sender as TMenuItem).Hint;
  if not FileExists(fileName) then
  begin
    MessageDlg(GetStringF('menu.file.recent.notfound', [ExtractFileName(fileName)]), GetString('dialog.error.title'),
      mtError, [mbOK], 0);
    UpdateRecentFilesMenu; // stale entry gets pruned by GetRecentFiles
    Exit;
  end;

  if ShowSaveConfirmation = mrCancel then Exit;
  DoOpenFile(fileName);
end;

procedure TMainForm.DoClearRecentFiles(Sender: TObject);
begin
  uappsettings.ClearRecentFiles;
  UpdateRecentFilesMenu;
end;

procedure TMainForm.UpdateRecentFilesMenu;
var
  files: TStringArray;
  i: Integer;
  item: TMenuItem;
begin
  MenuFileRecent.Clear;
  files := uappsettings.GetRecentFiles;

  if Length(files) = 0 then
  begin
    item := TMenuItem.Create(MenuFileRecent);
    item.Caption := GetString('menu.file.recent.empty');
    item.Enabled := False;
    MenuFileRecent.Add(item);
    Exit;
  end;

  for i := 0 to High(files) do
  begin
    item := TMenuItem.Create(MenuFileRecent);
    item.Caption := StringReplace(ExtractFileName(files[i]), '&', '&&', [rfReplaceAll]);
    item.Hint := files[i];
    item.OnClick := @DoOpenRecentFile;
    MenuFileRecent.Add(item);
  end;

  item := TMenuItem.Create(MenuFileRecent);
  item.Caption := '-';
  MenuFileRecent.Add(item);

  item := TMenuItem.Create(MenuFileRecent);
  item.Caption := GetString('menu.file.recent.clear');
  item.OnClick := @DoClearRecentFiles;
  MenuFileRecent.Add(item);
end;

function TMainForm.SaveToFile(const FileName: string): Boolean;
begin
  Result := False;
  try
    FModel.SaveFile(FileName);
    FCurrentFileName := FileName;
    FDirty := False;
    uappsettings.AddRecentFile(FCurrentFileName);
    UpdateRecentFilesMenu;
    UpdateTitle;
    Result := True;
  except
    on E: Exception do
      MessageDlg(GetStringF('dialog.error.save', [E.Message]), mtError, [mbOK], 0);
  end;
end;

procedure TMainForm.ActionSaveExecute(Sender: TObject);
var
  pw: string;
begin
  if FCurrentFileName = '' then
  begin
    ActionSaveAsExecute(Sender);
    Exit;
  end;

  if FModel.Password = '' then
  begin
    if not uencryptpasswordform.PromptNewPassword(Self, GetString('password.encrypt.title'), pw) then Exit;
    FModel.Password := UTF8String(pw);
  end;

  SaveToFile(FCurrentFileName);
end;

procedure TMainForm.ActionSaveAsExecute(Sender: TObject);
var
  pw: string;
begin
  if not FSaveDialog.Execute then Exit;

  if not uencryptpasswordform.PromptNewPassword(Self, GetString('password.encrypt.title'), pw) then Exit;
  FModel.Password := UTF8String(pw);

  SaveToFile(FSaveDialog.FileName);
end;

procedure TMainForm.ActionSetPasswordExecute(Sender: TObject);
var
  pw: string;
begin
  if not uencryptpasswordform.PromptNewPassword(Self, GetString('password.encrypt.title'), pw) then Exit;
  FModel.Password := UTF8String(pw);
  MarkDirty;

  if FCurrentFileName = '' then
  begin
    if not FSaveDialog.Execute then Exit;
    FCurrentFileName := FSaveDialog.FileName;
  end;

  if SaveToFile(FCurrentFileName) then
    MessageDlg(GetString('dialog.success.password'), GetString('dialog.success.title'), mtInformation, [mbOK], 0);
end;

procedure TMainForm.ActionAddSiblingExecute(Sender: TObject);
var
  title: string;
  newNode: TEntryTreeNode;
begin
  if not PromptText(GetString('nodetitle.new.title'), GetString('nodetitle.label'), '', title) then Exit;
  if title = '' then Exit;

  newNode := FModel.AddNode(nil, title);
  MarkDirty;
  RebuildTree;
  SelectModelNode(newNode);
end;

procedure TMainForm.ActionNewSiblingExecute(Sender: TObject);
var
  title: string;
  newNode: TEntryTreeNode;
begin
  if FSelectedModelNode = nil then
  begin
    MessageDlg(GetString('dialog.noselection.message'), mtWarning, [mbOK], 0);
    Exit;
  end;

  if not PromptText(GetString('nodetitle.new.title'), GetString('nodetitle.label'), '', title) then Exit;
  if title = '' then Exit;

  newNode := FModel.AddNode(FSelectedModelNode.Parent, title);
  MarkDirty;
  RebuildTree;
  SelectModelNode(newNode);
end;

procedure TMainForm.ActionAddChildExecute(Sender: TObject);
var
  title: string;
  newNode: TEntryTreeNode;
begin
  if FSelectedModelNode = nil then
  begin
    ActionAddSiblingExecute(Sender);
    Exit;
  end;

  if not PromptText(GetString('nodetitle.new.title'), GetString('nodetitle.label'), '', title) then Exit;
  if title = '' then Exit;

  newNode := FModel.AddNode(FSelectedModelNode, title);
  MarkDirty;
  RebuildTree;
  SelectModelNode(newNode);
end;

procedure TMainForm.ActionRenameExecute(Sender: TObject);
var
  title: string;
begin
  if FSelectedModelNode = nil then Exit;
  if not PromptText(GetString('nodetitle.edit.title'), GetString('nodetitle.label'), FSelectedModelNode.Title, title) then Exit;
  if title = '' then Exit;

  FModel.SetNodeTitle(FSelectedModelNode, title);
  MarkDirty;
  RebuildTree;
end;

procedure TMainForm.ActionDeleteExecute(Sender: TObject);
begin
  if FSelectedModelNode = nil then Exit;
  if MessageDlg(GetString('dialog.deletenode.message'), mtConfirmation, [mbYes, mbNo], 0) <> mrYes then Exit;

  FModel.DeleteNode(FSelectedModelNode);
  FSelectedModelNode := nil;
  MarkDirty;
  RebuildTree;
  LoadNodeIntoEditor(nil);
end;

procedure TMainForm.ActionMoveUpExecute(Sender: TObject);
var
  parentNode, node: TEntryTreeNode;
  idx: Integer;
begin
  if FSelectedModelNode = nil then
  begin
    Exit;
  end;

  node := FSelectedModelNode;
  parentNode := node.Parent;
  if parentNode = nil then Exit;

  idx := parentNode.IndexOfChild(node);
  if idx <= 0 then Exit;

  parentNode.Remove(node);
  parentNode.Insert(idx - 1, node);
  MarkDirty;
  RebuildTree;
  SelectModelNode(node);
end;

procedure TMainForm.ActionMoveDownExecute(Sender: TObject);
var
  parentNode, node: TEntryTreeNode;
  idx: Integer;
begin
  if FSelectedModelNode = nil then
  begin
    Exit;
  end;

  node := FSelectedModelNode;
  parentNode := node.Parent;
  if parentNode = nil then Exit;

  idx := parentNode.IndexOfChild(node);
  if (idx < 0) or (idx >= parentNode.ChildCount - 1) then Exit;

  parentNode.Remove(node);
  parentNode.Insert(idx + 1, node);
  MarkDirty;
  RebuildTree;
  SelectModelNode(node);
end;

procedure TMainForm.ActionSettingsExecute(Sender: TObject);
begin
  usettingsform.ShowSettingsDialog(Self);
end;

procedure TMainForm.ActionTextEncryptionExecute(Sender: TObject);
begin
  uencryptiondialog.ShowEncryptionWindow;
end;

procedure TMainForm.ActionPasswordGenExecute(Sender: TObject);
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

procedure TMainForm.SelectModelNode(ModelNode: TEntryTreeNode; FocusEditor: Boolean);
var
  tvNode: TTreeNode;
begin
  tvNode := FindTVNode(ModelNode);
  if tvNode = nil then Exit;

  tvNode.MakeVisible;
  if tvNode.HasChildren then
    tvNode.Expand(True);
  SyncNodeIcons(FTreeView.Items.GetFirstNode);

  FTreeView.Selected := tvNode;
  TreeSelectionChanged(nil);
  if FocusEditor and Visible and FEditor.CanFocus then
    FEditor.SetFocus;
end;

procedure TMainForm.SelectFirstNode(FocusEditor: Boolean);
begin
  if FModel.RootNode.ChildCount > 0 then
    SelectModelNode(FModel.RootNode.Children[0], FocusEditor)
  else
    LoadNodeIntoEditor(nil);
end;

procedure TMainForm.ActionSearchExecute(Sender: TObject);
var
  node: TEntryTreeNode;
begin
  node := usearchform.ShowSearchDialog(Self, FModel);
  if node <> nil then
    SelectModelNode(node);
end;

procedure TMainForm.ActionExitExecute(Sender: TObject);
begin
  Close;
end;

end.
