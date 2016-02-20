/*
 * SourceHandler.java
 *
 * Created on July 20, 2003, 8:14 PM
 */

package mae.util;

import java.io.File;
import java.lang.reflect.Method;
import java.awt.FileDialog;

/**
 * Handles source files for Fide
 * 
 * @author Eyler
 */
public abstract class SourceHandler {

	protected File source;
	protected Editor edit;

	/** Returns true if compiler is available */
	public abstract boolean canCompile();
	/** Compiles the source file */
	public abstract void compile();
	/** Returns true if program is compiled */
	public abstract boolean readyToRun();
	/** Executes the compiled program */
	public abstract void run();
	/** Sets fields of this SourceHandler */
	public abstract boolean setSource(File f, Editor d);

	/** Returns the source file */
	public File getSource() {
		return source;
	}
	/** Returns the Editor */
	public Editor getEditor() {
		return edit;
	}
	/** String representation of this SourceHandler */
	public String toString() {
		return getClass().getName() + " for " + source;
	}
	/** Returns true if compiler reads file (default: true) */
	public boolean requiresSave() {
		return true;
	}
	/** Stops running program (default: do nothing) */
	public void stop() {
	}
	/** Returns additional menu (default: null) */
	public javax.swing.JMenu menu() {
		return null;
	}

	/** Factory method to make instances */
	public static SourceHandler newHandler(File f, Editor d) {
		PropertyManager pm = d.propertyManager();
		String ext = extension(f);
		String k1 = ext + ".handler";
		String cls = pm.getProperty(k1);
		//System.out.println(k1+" -> "+cls);
		if (cls == null)
			return null;
		String k2 = ext + ".archive";
		String jar = pm.getProperty(k2);
		ClassLoader L = d.getClass().getClassLoader();
		Class hand = null;
		try {
			hand = Class.forName(cls, true, L);
	      //} catch (NoClassDefFoundError x) { cannot happen
		} catch (ClassNotFoundException x) {
			//try to extend class path
			openArchive(ext, cls, jar, d);
		}
		try {
			hand = Class.forName(cls, true, L); //again!
			//System.out.println(k1 + " -> " + hand);  removed V1.65
			SourceHandler h = (SourceHandler) hand.newInstance();
			pm.setProperty(k1, cls);
			pm.setProperty(k2, jar);
			h.setSource(f, d);
			return h;
			//return h.setSource(f, d)? h : null;
		} catch (Exception x) {
			System.err.println(x);
			pm.remove(k1);
			pm.remove(k2);
			return null;
		}
	}
	public static Class openArchive(String ext, String cls, String jar, Editor d) {
		PropertyManager pm = d.propertyManager();
		String k3 = ext + ".path";
		String path = pm.getProperty(k3);
		File here = new File(jar).getAbsoluteFile();
		File loc = (here.exists() || path == null) ? here : new File(path);
		if (!loc.exists()) {
		    System.err.println(jar + " not found");
		    final char Q = '"';
		    String desc = "Please locate archive " + Q + jar + Q;
		    FileDialog D = new FileDialog(Console.NULL, desc, FileDialog.LOAD);
		    D.setDirectory(System.getProperty("java.home")); 
		    D.setFile(jar); D.setVisible(true);
		    String fa = D.getFile();
		    if (fa == null) return null;
		    loc = new File(D.getDirectory(), fa); 
		}
		//System.err.println(jar+" in "+loc);
		try {
//         Class c = Loader.loadClass(loc.toURL(), cls);
//         ClassLoader L = ClassLoader.getSystemClassLoader();
		    ClassLoader L = d.getClass().getClassLoader();
		    Class c = java.net.URLClassLoader.class;
		    Class[] typ = {java.net.URL.class};
		    Method m = c.getDeclaredMethod("addURL", typ);
		    m.setAccessible(true);
		    Object[] arg = {loc.toURL()};
		    m.invoke(L, arg); //L.addURL(loc.toURL());
                    if (here.exists()) { //modified V1.65
                        System.err.println(jar + " is in startup location");
                    } else {
		        System.err.println(loc + " is added to class path");
		        pm.setProperty(k3, "" + loc);
                    }
		    return Class.forName(cls, true, L);
		} catch (Exception x) {
		    System.err.println(x);
		    pm.remove(k3);
		    return null;
		}
	}
	static String extension(File f) {//local use
		if (f == null)
			return null;
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}
}
