// 20.4.2003  into mae.util
// 18.5.2004  use lower case; toString()

package mae.util;
import java.io.File;
import javax.swing.filechooser.FileFilter;

/** Used in file selection, checks for a given extension */
public class SimpleFilter extends FileFilter implements java.io.FileFilter {
	String prefix, ext, desc;
	public SimpleFilter(String p, String e, String d) {
		prefix = p;
		desc = d;
		ext = (e.length() == 0) ? "" : "." + e;
	}
	public SimpleFilter(String e, String d) {
		this("", e, d);
	}
	public String getDescription() {
		return desc;
	}
	public String getExtension() {
		return ext;
	}
	public boolean accept(File f) {
		if (f == null || f.isDirectory())
			return true;
		String s = f.getName().toLowerCase();
		return s.startsWith(prefix) && s.endsWith(ext);
	}
	public String toString() {
		return prefix + "*" + ext;
	}
	public static String extension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}
}