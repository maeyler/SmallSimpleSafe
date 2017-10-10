## Version history

### Binary version history is [here](https://github.com/maeyler/SmallSimpleSafe/commits/master/sss.jar)

### V2.07  Oct 8, 2017
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
