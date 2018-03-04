## Version history

### Binary version history is [here](https://github.com/maeyler/SmallSimpleSafe/commits/master/sss.jar)

### V2.10  Mar 06 2018
* Menu.fixEncoding() fixes text files encoded in Cp1254 (by B E Harmansa)
* Console.selectFiles() allows multiple file selection (by B E Harmansa)
* Deprecated fields are hidden in Inspector (by B E Harmansa)
* File type in Browser is probed using java.nio.file.Files.probeContentType()
* Fide opens text files only, throws Exception otherwise
* Fide has Java menu as replacement for Chooser.compileAll()
* Inspect button in Browser opens SSS to inspect the selected file
* Requires Java 7 or higher

### V2.09  Dec 16, 2017
* Paste order is modified: Image, then File, then String and others
* New constructor ClassSummary(InputStream)

### V2.08  Oct 27, 2017
* In Java 9, rt.jar is replaced by the module system (by B E Harmansa)
* SystemJavaCompiler is used under JDK -- if not available we revert to tools.jar 
* Swing components are scaled by Java 9 in Hi DPI screens, no need to scale again
* Class file format is modified in Java 9, new tags are added
* UndoManager failure in Java 9 is fixed using reflection (by B E Harmansa)

### V2.07  Oct 15, 2017
* FileDialog filters correctly in Unix (contribution by B E Harmansa)
* Add `Menu.system()` that returns Sytem.class
* Use system console when invoked from terminal
* Splash panel removed

### V2.06  Dec 31, 2016
* Use java.awt.Desktop to open files (idea by F Yilmaz)
* "Fix Date" button for JPG files in Browser

### V2.05  Oct 28, 2016
* Console and Editor buttons added (contribution by F Yilmaz)

### V2.04  Feb 20, 2016
* java files are removed from sss.jar
* GitHub web page and user.dir on the Version dialog
* JFileChooser eliminated completely -- use FileDialog 
* Fide: FileDialog opens in the current folder
* HashSet.teacher modified

### V2.03  Jan 26, 2016
* Compiled with Java 6
* Shift-BackSpace deletes the item (same as DEL key)
* Small window is relocated to make it visible under the teacher

### V2.02  Dec 15, 2015
* `Chooser.compileAll()` in order to compile all files in a folder
* Use FileDialog instead of JFileChooser

### V2.01  Sep 21, 2015
* txt files accepted in `Chooser.runTeacher()`

### V2.00  May 02, 2015
* Class structure simplified, `Singleton` pattern is used
* Scaler: windows and dialogs scaled using `getScreenResolution()`
