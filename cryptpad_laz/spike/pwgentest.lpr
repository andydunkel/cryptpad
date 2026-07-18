program pwgentest;

{$mode objfpc}{$H+}
{$codepage UTF8}

uses
  SysUtils, upasswordgenerator;

var
  gen: TPasswordGenerator;
  i: Integer;
begin
  gen := TPasswordGenerator.Create;
  try
    WriteLn('lowercase only:');
    for i := 1 to 3 do WriteLn('  ', gen.GeneratePassword(16));

    gen.CapitalsAllowed := True;
    gen.NumbersAllowed := True;
    WriteLn('lower+upper+digits:');
    for i := 1 to 3 do WriteLn('  ', gen.GeneratePassword(16));

    gen.SpecialCharsAllowed := True;
    WriteLn('all sets (incl. special/UTF8):');
    for i := 1 to 5 do WriteLn('  ', gen.GeneratePassword(20));
  finally
    gen.Free;
  end;
end.
