unit udatamodel;

{$mode objfpc}{$H+}

interface

uses
  Classes, SysUtils, fgl, uentrytreenode, umessages;

type
  IObserver = interface
    ['{6C6E5B1A-6F2E-4B7D-9C9C-3B6D8D9F1A11}']
    procedure Refresh;
  end;

  TObserverList = specialize TFPGList<IObserver>;

  TMatchType = (mtTitle, mtContent, mtBoth);

  TSearchResult = class
  private
    FNode: TEntryTreeNode;
    FMatchType: TMatchType;
    FContextSnippet: string;
    FNodePath: string;
  public
    constructor Create(ANode: TEntryTreeNode; AMatchType: TMatchType;
      const AContextSnippet, ANodePath: string);
    property Node: TEntryTreeNode read FNode;
    property MatchType: TMatchType read FMatchType;
    property ContextSnippet: string read FContextSnippet;
    property NodePath: string read FNodePath;
  end;

  TDataModel = class
  private
    FRootNode: TEntryTreeNode;
    FObservers: TObserverList;
    FPassword: UTF8String;
    procedure InitializeNewDocument;
    procedure AddDefaultNode;
  public
    constructor Create;
    destructor Destroy; override;

    procedure SaveFile(const FileName: string);
    procedure LoadFile(const FileName: string);

    procedure Subscribe(Observer: IObserver);
    procedure Unsubscribe(Observer: IObserver);
    procedure RefreshObservers;

    function AddNode(Parent: TEntryTreeNode; const Title: string): TEntryTreeNode;
    procedure DeleteNode(Node: TEntryTreeNode);
    procedure SetNodeTitle(Node: TEntryTreeNode; const Title: string);
    procedure SetNodeContent(Node: TEntryTreeNode; const Content: UTF8String);
    procedure ClearModel;

    property RootNode: TEntryTreeNode read FRootNode;
    property Password: UTF8String read FPassword write FPassword;
  end;

implementation

uses
  uxmlmanager;

{ TSearchResult }

constructor TSearchResult.Create(ANode: TEntryTreeNode; AMatchType: TMatchType;
  const AContextSnippet, ANodePath: string);
begin
  inherited Create;
  FNode := ANode;
  FMatchType := AMatchType;
  FContextSnippet := AContextSnippet;
  FNodePath := ANodePath;
end;

{ TDataModel }

constructor TDataModel.Create;
begin
  inherited Create;
  FObservers := TObserverList.Create;
  InitializeNewDocument;
  AddDefaultNode;
end;

destructor TDataModel.Destroy;
begin
  FObservers.Free;
  FRootNode.Free;
  inherited Destroy;
end;

procedure TDataModel.InitializeNewDocument;
begin
  FreeAndNil(FRootNode);
  FRootNode := TEntryTreeNode.Create(GetString('tree.rootnode'));
  FPassword := '';
end;

procedure TDataModel.AddDefaultNode;
var
  defaultNode: TEntryTreeNode;
begin
  defaultNode := TEntryTreeNode.Create(GetString('tree.defaultnode'));
  defaultNode.Content := UTF8String(GetString('tree.defaultcontent'));
  FRootNode.Add(defaultNode);
end;

procedure TDataModel.SaveFile(const FileName: string);
begin
  XMLManagerSave(FileName, Self);
end;

procedure TDataModel.LoadFile(const FileName: string);
begin
  XMLManagerLoad(FileName, Self);
  RefreshObservers;
end;

procedure TDataModel.Subscribe(Observer: IObserver);
begin
  if FObservers.IndexOf(Observer) = -1 then
  begin
    FObservers.Add(Observer);
    RefreshObservers;
  end;
end;

procedure TDataModel.Unsubscribe(Observer: IObserver);
var
  idx: Integer;
begin
  idx := FObservers.IndexOf(Observer);
  if idx >= 0 then
    FObservers.Delete(idx);
end;

procedure TDataModel.RefreshObservers;
var
  observer: IObserver;
begin
  for observer in FObservers do
    observer.Refresh;
end;

function TDataModel.AddNode(Parent: TEntryTreeNode; const Title: string): TEntryTreeNode;
begin
  Result := TEntryTreeNode.Create(Title);
  if Parent <> nil then
    Parent.Add(Result)
  else
    FRootNode.Add(Result);
  RefreshObservers;
end;

procedure TDataModel.DeleteNode(Node: TEntryTreeNode);
begin
  if (Node <> nil) and (Node.Parent <> nil) then
  begin
    Node.Parent.Remove(Node);
    Node.Free;
  end;
  RefreshObservers;
end;

procedure TDataModel.SetNodeTitle(Node: TEntryTreeNode; const Title: string);
begin
  Node.Title := Title;
  RefreshObservers;
end;

procedure TDataModel.SetNodeContent(Node: TEntryTreeNode; const Content: UTF8String);
begin
  Node.Content := Content;
end;

procedure TDataModel.ClearModel;
var
  savedPassword: UTF8String;
begin
  savedPassword := FPassword;
  InitializeNewDocument;
  FPassword := savedPassword;
  RefreshObservers;
end;

end.
