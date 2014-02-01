dim fso: set fso = CreateObject("Scripting.FileSystemObject")
dim CurrentDirectory
CurrentDirectory = fso.GetAbsolutePathName(".")
Set oWS = WScript.CreateObject("WScript.Shell")
sLinkFile = CurrentDirectory & "\..\TinySnap2.lnk"
Set oLink = oWS.CreateShortcut(sLinkFile)
oLink.TargetPath = CurrentDirectory & "\..\target\TinySnap2.jar"
oLink.IconLocation = CurrentDirectory & "\..\.icon\icon.ico"
oLink.Save