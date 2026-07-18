unit usearchservice;

{$mode objfpc}{$H+}
{$codepage UTF8}

interface

uses
  Classes, SysUtils, contnrs, LazUTF8, uentrytreenode, udatamodel;

// Returns a list of TSearchResult (caller owns and must Free the list; it owns its items).
function SearchTree(Root: TEntryTreeNode; const SearchText: string): TFPObjectList;

implementation

const
  SNIPPET_LENGTH = 50;

function ContainsText(const Haystack, NeedleLower: string): Boolean;
begin
  Result := Pos(NeedleLower, UTF8LowerCase(Haystack)) > 0;
end;

function CreateSnippet(const Content, SearchTextLower: string): string;
var
  contentLower: string;
  index, startPos, endPos: Integer;
begin
  contentLower := UTF8LowerCase(Content);
  index := Pos(SearchTextLower, contentLower);
  if index = 0 then
  begin
    Result := '';
    Exit;
  end;

  startPos := index - (SNIPPET_LENGTH div 2);
  if startPos < 1 then startPos := 1;
  endPos := index + UTF8Length(SearchTextLower) + (SNIPPET_LENGTH div 2);
  if endPos > UTF8Length(Content) + 1 then endPos := UTF8Length(Content) + 1;

  Result := '';
  if startPos > 1 then
    Result := Result + '...';
  Result := Result + UTF8Copy(Content, startPos, endPos - startPos);
  if endPos <= UTF8Length(Content) then
    Result := Result + '...';

  Result := StringReplace(Result, #10, ' ', [rfReplaceAll]);
  Result := StringReplace(Result, #13, '', [rfReplaceAll]);
end;

procedure SearchRecursive(Node: TEntryTreeNode; const SearchTextLower, ParentPath: string;
  Results: TFPObjectList);
var
  title, currentPath, childPath, snippet: string;
  titleMatch, contentMatch: Boolean;
  matchType: TMatchType;
  i: Integer;
begin
  title := Node.Title;
  if ParentPath = '' then
    currentPath := title
  else
    currentPath := ParentPath;

  titleMatch := ContainsText(title, SearchTextLower);
  contentMatch := ContainsText(string(Node.Content), SearchTextLower);

  if titleMatch or contentMatch then
  begin
    if titleMatch and contentMatch then
      matchType := mtBoth
    else if titleMatch then
      matchType := mtTitle
    else
      matchType := mtContent;

    if contentMatch then
      snippet := CreateSnippet(string(Node.Content), SearchTextLower)
    else
      snippet := '';

    Results.Add(TSearchResult.Create(Node, matchType, snippet, currentPath));
  end;

  for i := 0 to Node.ChildCount - 1 do
  begin
    if ParentPath = '' then
      childPath := title
    else
      childPath := ParentPath + ' > ' + title;
    SearchRecursive(Node.Children[i], SearchTextLower, childPath, Results);
  end;
end;

function SearchTree(Root: TEntryTreeNode; const SearchText: string): TFPObjectList;
begin
  Result := TFPObjectList.Create(True);
  if (Root = nil) or (Trim(SearchText) = '') then
    Exit;

  SearchRecursive(Root, UTF8LowerCase(SearchText), '', Result);
end;

end.
