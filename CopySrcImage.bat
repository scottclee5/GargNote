@ECHO OFF
SETLOCAL

IF "%1"=="" GOTO PrintHelp
IF "%2"=="" GOTO PrintHelp

SET SRCDIR=C:\Src\material-design-icons-master\%1
SET DSTDIR=C:\Users\Scott\Documents\AndroidStudioProjects\GargNote\app\src\main\res
SET SRCFILENAME=%2
SET DSTFILENAME=%3

IF "%DSTFILENAME%"=="" SET DSTFILENAME = %SRCFILENAME%

COPY %SRCDIR%\drawable-mdpi\%SRCFILENAME% %DSTDIR%\drawable-mdpi\%DSTFILENAME%
COPY %SRCDIR%\drawable-hdpi\%SRCFILENAME% %DSTDIR%\drawable-hdpi\%DSTFILENAME%
COPY %SRCDIR%\drawable-xhdpi\%SRCFILENAME% %DSTDIR%\drawable-xhdpi\%DSTFILENAME%
COPY %SRCDIR%\drawable-xxhdpi\%SRCFILENAME% %DSTDIR%\drawable-xxhdpi\%DSTFILENAME%
COPY %SRCDIR%\drawable-xxxhdpi\%SRCFILENAME% %DSTDIR%\drawable-xxxhdpi\%DSTFILENAME%

GOTO End

:PrintHelp

ECHO SYNTAX: CopySrcImage ^<Folder^> ^<ImageFileName^> [NewImageFileName]

:End

ENDLOCAL
