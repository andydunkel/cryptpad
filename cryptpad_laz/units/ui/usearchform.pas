unit usearchform;

{$mode objfpc}{$H+}
{$codepage UTF8}

interface

uses
  Classes, SysUtils, Forms, StdCtrls, Controls, contnrs,
  umessages, uentrytreenode, udatamodel, usearchservice;

type

  { TSearchForm }

  TSearchForm = class(TForm)
    ButtonClose: TButton;
    EditSearch: TEdit;
    ListResults: TListBox;
    procedure EditSearchChange(Sender: TObject);
    procedure ListResultsDblClick(Sender: TObject);
  private
    FModel: TDataModel;
    FResults: TFPObjectList;
  public
    SelectedNode: TEntryTreeNode;
    procedure SetModel(AModel: TDataModel);
    destructor Destroy; override;
  end;

function ShowSearchDialog(AOwner: TComponent; Model: TDataModel): TEntryTreeNode;

implementation

{$R *.lfm}

function ShowSearchDialog(AOwner: TComponent; Model: TDataModel): TEntryTreeNode;
var
  frm: TSearchForm;
begin
  Result := nil;
  frm := TSearchForm.Create(AOwner);
  try
    frm.Caption := GetString('search.results');
    frm.SetModel(Model);
    if frm.ShowModal = mrOK then
      Result := frm.SelectedNode;
  finally
    frm.Free;
  end;
end;

destructor TSearchForm.Destroy;
begin
  FResults.Free;
  inherited Destroy;
end;

procedure TSearchForm.SetModel(AModel: TDataModel);
begin
  FModel := AModel;
end;

procedure TSearchForm.EditSearchChange(Sender: TObject);
var
  i: Integer;
  res: TSearchResult;
  line: string;
begin
  FreeAndNil(FResults);
  ListResults.Items.Clear;

  FResults := SearchTree(FModel.RootNode, EditSearch.Text);
  if FResults.Count = 0 then
  begin
    if Trim(EditSearch.Text) <> '' then
      ListResults.Items.Add(GetString('search.noresults'));
    Exit;
  end;

  for i := 0 to FResults.Count - 1 do
  begin
    res := TSearchResult(FResults[i]);
    line := res.NodePath;
    if res.ContextSnippet <> '' then
      line := line + '  -  ' + res.ContextSnippet;
    ListResults.Items.Add(line);
  end;
end;

procedure TSearchForm.ListResultsDblClick(Sender: TObject);
var
  idx: Integer;
begin
  if (FResults = nil) or (FResults.Count = 0) then Exit;
  idx := ListResults.ItemIndex;
  if (idx < 0) or (idx >= FResults.Count) then Exit;

  SelectedNode := TSearchResult(FResults[idx]).Node;
  ModalResult := mrOK;
end;

end.
