unit uentrytreenode;

{$mode objfpc}{$H+}

interface

uses
  Classes, SysUtils, contnrs;

type
  TEntryTreeNode = class
  private
    FTitle: string;
    FContent: UTF8String;
    FSelected: Boolean;
    FParent: TEntryTreeNode;
    FChildren: TFPObjectList;
    function GetChildCount: Integer;
    function GetChild(Index: Integer): TEntryTreeNode;
  public
    constructor Create(const ATitle: string);
    destructor Destroy; override;

    procedure Add(Child: TEntryTreeNode);
    procedure Insert(Index: Integer; Child: TEntryTreeNode);
    procedure Remove(Child: TEntryTreeNode);
    function IndexOfChild(Child: TEntryTreeNode): Integer;

    property Title: string read FTitle write FTitle;
    property Content: UTF8String read FContent write FContent;
    property Selected: Boolean read FSelected write FSelected;
    property Parent: TEntryTreeNode read FParent;
    property ChildCount: Integer read GetChildCount;
    property Children[Index: Integer]: TEntryTreeNode read GetChild;
  end;

implementation

constructor TEntryTreeNode.Create(const ATitle: string);
begin
  inherited Create;
  FTitle := ATitle;
  FContent := '';
  FSelected := False;
  FParent := nil;
  FChildren := TFPObjectList.Create(True); // owns children, frees them recursively
end;

destructor TEntryTreeNode.Destroy;
begin
  FChildren.Free;
  inherited Destroy;
end;

function TEntryTreeNode.GetChildCount: Integer;
begin
  Result := FChildren.Count;
end;

function TEntryTreeNode.GetChild(Index: Integer): TEntryTreeNode;
begin
  Result := TEntryTreeNode(FChildren[Index]);
end;

procedure TEntryTreeNode.Add(Child: TEntryTreeNode);
begin
  if Child.FParent <> nil then
    Child.FParent.Remove(Child);
  Child.FParent := Self;
  FChildren.Add(Child);
end;

procedure TEntryTreeNode.Insert(Index: Integer; Child: TEntryTreeNode);
begin
  if Child.FParent <> nil then
    Child.FParent.Remove(Child);
  Child.FParent := Self;
  FChildren.Insert(Index, Child);
end;

procedure TEntryTreeNode.Remove(Child: TEntryTreeNode);
var
  idx: Integer;
begin
  idx := FChildren.IndexOf(Child);
  if idx >= 0 then
  begin
    FChildren.OwnsObjects := False;
    FChildren.Delete(idx);
    FChildren.OwnsObjects := True;
    Child.FParent := nil;
  end;
end;

function TEntryTreeNode.IndexOfChild(Child: TEntryTreeNode): Integer;
begin
  Result := FChildren.IndexOf(Child);
end;

end.
