unit usynhighlightermarkdown;

{$mode objfpc}{$H+}
{$codepage UTF8}

interface

uses
  Classes, SysUtils, Graphics, SynEditTypes, SynEditHighlighter;

type
  TtkTokenKind = (tkHeader, tkBold, tkItalic, tkCode, tkCodeFence, tkBlockquote,
                  tkListMarker, tkText, tkSpace, tkNull);

  { TSynMarkdownSyn }

  TSynMarkdownSyn = class(TSynCustomHighlighter)
  private
    fLine: PChar;
    fLineNumber: Integer;
    Run: LongInt;
    fTokenPos: Integer;
    FTokenID: TtkTokenKind;

    fHeaderAttri, fBoldAttri, fItalicAttri, fCodeAttri, fCodeFenceAttri,
    fBlockquoteAttri, fListMarkerAttri, fTextAttri, fSpaceAttri: TSynHighlighterAttributes;

    procedure ScanHeader;
    procedure ScanBlockquote;
    procedure ScanCodeFence;
    procedure ScanListMarker;
    procedure ScanBoldOrItalic(Marker: Char);
    procedure ScanInlineCode;
    procedure ScanText;
    procedure ScanSpace;
    function IsUnorderedListStart: Boolean;
    function IsOrderedListStart: Boolean;
  protected
    function GetIdentChars: TSynIdentChars; override;
    function GetSampleSource: String; override;
  public
    class function GetLanguageName: string; override;
    constructor Create(AOwner: TComponent); override;
    function GetDefaultAttribute(Index: integer): TSynHighlighterAttributes; override;
    function GetEol: Boolean; override;
    function GetTokenID: TtkTokenKind;
    procedure SetLine(const NewValue: String; LineNumber: Integer); override;
    function GetToken: String; override;
    procedure GetTokenEx(out TokenStart: PChar; out TokenLength: integer); override;
    function GetTokenAttribute: TSynHighlighterAttributes; override;
    function GetTokenKind: integer; override;
    function GetTokenPos: Integer; override;
    procedure Next; override;
  published
    property HeaderAttri: TSynHighlighterAttributes read fHeaderAttri write fHeaderAttri;
    property BoldAttri: TSynHighlighterAttributes read fBoldAttri write fBoldAttri;
    property ItalicAttri: TSynHighlighterAttributes read fItalicAttri write fItalicAttri;
    property CodeAttri: TSynHighlighterAttributes read fCodeAttri write fCodeAttri;
    property CodeFenceAttri: TSynHighlighterAttributes read fCodeFenceAttri write fCodeFenceAttri;
    property BlockquoteAttri: TSynHighlighterAttributes read fBlockquoteAttri write fBlockquoteAttri;
    property ListMarkerAttri: TSynHighlighterAttributes read fListMarkerAttri write fListMarkerAttri;
    property TextAttri: TSynHighlighterAttributes read fTextAttri write fTextAttri;
    property SpaceAttri: TSynHighlighterAttributes read fSpaceAttri write fSpaceAttri;
  end;

implementation

constructor TSynMarkdownSyn.Create(AOwner: TComponent);
begin
  inherited Create(AOwner);

  fHeaderAttri := TSynHighlighterAttributes.Create('Header', 'Header');
  fHeaderAttri.Style := [fsBold];
  fHeaderAttri.Foreground := clNavy;
  AddAttribute(fHeaderAttri);

  fBoldAttri := TSynHighlighterAttributes.Create('Bold', 'Bold');
  fBoldAttri.Style := [fsBold];
  AddAttribute(fBoldAttri);

  fItalicAttri := TSynHighlighterAttributes.Create('Italic', 'Italic');
  fItalicAttri.Style := [fsItalic];
  AddAttribute(fItalicAttri);

  fCodeAttri := TSynHighlighterAttributes.Create('Code', 'Code');
  fCodeAttri.Foreground := clMaroon;
  fCodeAttri.Background := TColor($E8E8E8);
  AddAttribute(fCodeAttri);

  fCodeFenceAttri := TSynHighlighterAttributes.Create('CodeFence', 'CodeFence');
  fCodeFenceAttri.Foreground := clGray;
  fCodeFenceAttri.Background := TColor($E8E8E8);
  AddAttribute(fCodeFenceAttri);

  fBlockquoteAttri := TSynHighlighterAttributes.Create('Blockquote', 'Blockquote');
  fBlockquoteAttri.Foreground := clGreen;
  fBlockquoteAttri.Style := [fsItalic];
  AddAttribute(fBlockquoteAttri);

  fListMarkerAttri := TSynHighlighterAttributes.Create('ListMarker', 'ListMarker');
  fListMarkerAttri.Foreground := clNavy;
  fListMarkerAttri.Style := [fsBold];
  AddAttribute(fListMarkerAttri);

  fTextAttri := TSynHighlighterAttributes.Create('Text', 'Text');
  AddAttribute(fTextAttri);

  fSpaceAttri := TSynHighlighterAttributes.Create('Space', 'Space');
  AddAttribute(fSpaceAttri);

  SetAttributesOnChange(@DefHighlightChange);
  fDefaultFilter := 'Markdown files (*.md,*.markdown)|*.md;*.markdown';
end;

procedure TSynMarkdownSyn.SetLine(const NewValue: String; LineNumber: Integer);
begin
  inherited;
  fLine := PChar(NewValue);
  Run := 0;
  fLineNumber := LineNumber;
  Next;
end;

function TSynMarkdownSyn.IsUnorderedListStart: Boolean;
begin
  Result := (fLine[0] in ['-', '*', '+']) and (fLine[1] = ' ');
end;

function TSynMarkdownSyn.IsOrderedListStart: Boolean;
var
  p: Integer;
begin
  Result := False;
  p := 0;
  while fLine[p] in ['0'..'9'] do Inc(p);
  if (p > 0) and (fLine[p] = '.') and (fLine[p + 1] = ' ') then
    Result := True;
end;

procedure TSynMarkdownSyn.ScanHeader;
begin
  fTokenID := tkHeader;
  while fLine[Run] <> #0 do Inc(Run);
end;

procedure TSynMarkdownSyn.ScanBlockquote;
begin
  fTokenID := tkBlockquote;
  while fLine[Run] <> #0 do Inc(Run);
end;

procedure TSynMarkdownSyn.ScanCodeFence;
begin
  fTokenID := tkCodeFence;
  while fLine[Run] <> #0 do Inc(Run);
end;

procedure TSynMarkdownSyn.ScanListMarker;
begin
  fTokenID := tkListMarker;
  if fLine[Run] in ['0'..'9'] then
  begin
    while fLine[Run] in ['0'..'9'] do Inc(Run);
    Inc(Run); // '.'
    Inc(Run); // space
  end
  else
    Inc(Run, 2); // marker char + space
end;

procedure TSynMarkdownSyn.ScanBoldOrItalic(Marker: Char);
var
  isBold: Boolean;
  closeLen: Integer;
begin
  isBold := fLine[Run + 1] = Marker;
  if isBold then closeLen := 2 else closeLen := 1;
  Inc(Run, closeLen);

  while fLine[Run] <> #0 do
  begin
    if (fLine[Run] = Marker) and ((not isBold) or (fLine[Run + 1] = Marker)) then
    begin
      Inc(Run, closeLen);
      Break;
    end;
    Inc(Run);
  end;

  if isBold then fTokenID := tkBold else fTokenID := tkItalic;
end;

procedure TSynMarkdownSyn.ScanInlineCode;
begin
  fTokenID := tkCode;
  Inc(Run);
  while (fLine[Run] <> #0) and (fLine[Run] <> '`') do Inc(Run);
  if fLine[Run] = '`' then Inc(Run);
end;

procedure TSynMarkdownSyn.ScanText;
begin
  fTokenID := tkText;
  Inc(Run);
  while (fLine[Run] <> #0) and not (fLine[Run] in ['*', '_', '`']) do
    Inc(Run);
end;

procedure TSynMarkdownSyn.ScanSpace;
begin
  fTokenID := tkSpace;
  while fLine[Run] in [' ', #9] do Inc(Run);
end;

procedure TSynMarkdownSyn.Next;
begin
  fTokenPos := Run;

  if fLine[Run] = #0 then
  begin
    fTokenID := tkNull;
    Exit;
  end;

  if Run = 0 then
  begin
    if fLine[0] = '#' then
    begin
      ScanHeader;
      Exit;
    end;
    if fLine[0] = '>' then
    begin
      ScanBlockquote;
      Exit;
    end;
    if (fLine[0] = '`') and (fLine[1] = '`') and (fLine[2] = '`') then
    begin
      ScanCodeFence;
      Exit;
    end;
    if IsUnorderedListStart or IsOrderedListStart then
    begin
      ScanListMarker;
      Exit;
    end;
  end;

  case fLine[Run] of
    ' ', #9: ScanSpace;
    '`': ScanInlineCode;
    '*': ScanBoldOrItalic('*');
    '_': ScanBoldOrItalic('_');
  else
    ScanText;
  end;
end;

function TSynMarkdownSyn.GetDefaultAttribute(Index: integer): TSynHighlighterAttributes;
begin
  case Index of
    SYN_ATTR_WHITESPACE: Result := fSpaceAttri;
    SYN_ATTR_KEYWORD: Result := fHeaderAttri;
    SYN_ATTR_STRING: Result := fCodeAttri;
    SYN_ATTR_COMMENT: Result := fBlockquoteAttri;
  else
    Result := nil;
  end;
end;

function TSynMarkdownSyn.GetEol: Boolean;
begin
  Result := fTokenID = tkNull;
end;

function TSynMarkdownSyn.GetTokenID: TtkTokenKind;
begin
  Result := fTokenID;
end;

function TSynMarkdownSyn.GetToken: String;
var
  Len: LongInt;
begin
  Len := Run - fTokenPos;
  SetString(Result, (fLine + fTokenPos), Len);
end;

procedure TSynMarkdownSyn.GetTokenEx(out TokenStart: PChar; out TokenLength: integer);
begin
  TokenLength := Run - fTokenPos;
  TokenStart := fLine + fTokenPos;
end;

function TSynMarkdownSyn.GetTokenAttribute: TSynHighlighterAttributes;
begin
  case fTokenID of
    tkHeader: Result := fHeaderAttri;
    tkBold: Result := fBoldAttri;
    tkItalic: Result := fItalicAttri;
    tkCode: Result := fCodeAttri;
    tkCodeFence: Result := fCodeFenceAttri;
    tkBlockquote: Result := fBlockquoteAttri;
    tkListMarker: Result := fListMarkerAttri;
    tkSpace: Result := fSpaceAttri;
  else
    Result := fTextAttri;
  end;
end;

function TSynMarkdownSyn.GetTokenKind: integer;
begin
  Result := Ord(fTokenID);
end;

function TSynMarkdownSyn.GetTokenPos: Integer;
begin
  Result := fTokenPos;
end;

function TSynMarkdownSyn.GetIdentChars: TSynIdentChars;
begin
  Result := [#33..#255];
end;

class function TSynMarkdownSyn.GetLanguageName: string;
begin
  Result := 'Markdown';
end;

function TSynMarkdownSyn.GetSampleSource: String;
begin
  Result := '# Header' + LineEnding +
            LineEnding +
            'Some **bold** and *italic* text with `inline code`.' + LineEnding +
            LineEnding +
            '> A blockquote' + LineEnding +
            LineEnding +
            '- List item one' + LineEnding +
            '- List item two' + LineEnding +
            LineEnding +
            '```' + LineEnding +
            'code fence' + LineEnding +
            '```';
end;

initialization
  RegisterPlaceableHighlighter(TSynMarkdownSyn);

end.
