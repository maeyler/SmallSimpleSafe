/*
 * JavaSourceHandler.java
 *
 * Created on July 20, 2003, 8:17 PM
 */

package mae.brow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
//import com.sun.tools.javac.Main; use dynamic loading
import mae.util.ClassSummary;
import mae.util.Loader;
import mae.util.Editor;

/**
 * Handles Java source file
 * 
 * @author Eyler
 */
public class JavaSourceHandler extends mae.util.SourceHandler {

	Class prog;
	File target;
	boolean ready;

	static Method javac;
	static Object in;
	static final String[] ARGS = new String[0]; 
	static final Class[] stringArray = {ARGS.getClass()};
	static final String COMP = "com.sun.tools.javac.Main";

	/** Sets fields of this SourceHandler */
	public boolean setSource(File f, Editor d) {
		source = f;
		edit = d;
		if (javac == null)
			try {
				Class c = //Class.forName(COMP);
				openArchive("java", COMP, "tools.jar", d);
				javac = c.getMethod("compile", stringArray);
				in = c.newInstance();
			} catch (Exception x) {
				edit.setMessage(x.getMessage());
			}
		String s = f.getName();
		int k = s.lastIndexOf(".java");
		if (k < 0)
			return false;
		s = s.substring(0, k) + ".class";
		target = new File(f.getParent(), s);
		ready = !target.exists() ? false : new ClassSummary(target).hasMain();
		return canCompile();
	}

	void loadTarget() {
		if (!target.exists())
			return;
		try {
			prog = Loader.loadClass(target);
			prog.getMethod("main", stringArray);
			ready = true;
		} catch (NoSuchMethodException x) {
		} catch (ClassNotFoundException x) {
			edit.setMessage(x.getMessage());
		}
	}
	public boolean canCompile() {
		return (javac != null && source != null);
	}
	public void compile() {
		if (!canCompile())
			throw new RuntimeException("not initialized");
		prog = null;
		//      compile(source);
		new Comp().start();
	}
	public boolean readyToRun() {
		return (ready && source.exists() && target.exists() && source
				.lastModified() <= target.lastModified());
	}
	public void run() {
		if (!readyToRun())
			throw new RuntimeException("not compiled");
		if (prog == null)
			loadTarget();
		run(prog);
	}

	public JMenu menu() {
                return null;  //V1.65
/*
		Ear e = new Ear();
		JMenuItem i;
		JMenu menu = new JMenu("Java");
		menu.setMnemonic('J');
		JMenu sub = new JMenu("Templates");
		menu.add(sub);
		i = new JMenuItem("for");
		i.setMnemonic('f');
		i.addActionListener(e);
		sub.add(i);
		i = new JMenuItem("try");
		i.setMnemonic('t');
		i.addActionListener(e);
		sub.add(i);
		menu.add(new JMenuItem("Statistics"));
		return menu;
*/
	}
	public static File getClassPath(File f) {
		String name;
		try {
			name = new Name(f).getName();
		} catch (Exception x) {
			System.err.println(x);
			return f.getParentFile();
		}
		f = f.getAbsoluteFile();
		int k = name.lastIndexOf(".");
		while (k > 0) {
			f = f.getParentFile();
			k = name.lastIndexOf(".", k - 1);
		}
		//System.out.println(f.getParent() + " --> " + name);  removed V1.65
		return f.getParentFile();
	}
	public int compile(File f) {
		if (javac == null)
			return -1;
		String name = f.getName();
		if (!name.endsWith(".java"))
			throw new RuntimeException("not a java file");
		int k = name.lastIndexOf('.');
		name = name.substring(0, k);
		Object out = null;
		try {
			Object[] a = {makeArray(f)};
			out = javac.invoke(in, a);
		} catch (Exception x) {
			edit.setMessage(x.getMessage());
		}
		return ((Integer) out).intValue();
	}
	static String[] makeArray(File f) {
		if (!f.isAbsolute())
			f = f.getAbsoluteFile();
		String[] a = new String[4];
		a[0] = "-classpath";
		a[1] = "" + getClassPath(f); //f.getParent();
		a[2] = "-g";
		a[3] = "" + f;
		return a;
	}
	public void run(File f) throws ClassNotFoundException {
		run(Loader.loadClass(f));
	}
	public void run(Class c) {
		try {
			Method m = c.getMethod("main", stringArray);
			m.setAccessible(true); //perhaps not public
			new Exec(m).start();
		} catch (NoSuchMethodException x) {
			edit.setMessage("main() not found");
			System.err.println(x);
		}
	}

	class Exec extends Thread {
		Method met;
		Exec(Method m) {
			met = m;
		}
		public void run() {
			String s = "";
			try {
				met.invoke(null, new Object[]{ARGS});
				return;
			} catch (InvocationTargetException x) {
				Throwable t = x.getTargetException();
				t.printStackTrace(System.out); /*!!*/
				if (edit != null)
					edit.setMessage("" + t);
				//edit.msg.setText(""+t);
			} catch (IllegalAccessException x) {
				if (edit != null)
					edit.setMessage("main() is not accessible");
				System.err.println(x);
			}
		}
	}

	class Comp extends Thread {
		public void run() {
			edit.setMessage("compiling  " + source);
			long t = System.currentTimeMillis();
			System.gc();
			PrintStream err = System.err;
			System.setErr(System.out);
			ready = false;
			int k = compile(source);
			System.setErr(err);
			if (k == 0) {
				loadTarget(); //ready = true;
				t = System.currentTimeMillis() - t + 500;
				edit.setMessage("compiled in " + (t / 1000) + " sec", ready);
				//System.err.println(target+" compiled "+ready);
			} else {
				edit.setMessage("compiler error", false);
			}
		}
	}

	static class Name extends StreamTokenizer {

		String name;
		Name(File f) throws IOException {
			super(new FileReader(f));
			name = f.getName();
			int k = name.lastIndexOf('.');
			if (k < 0)
				return;
			name = name.substring(0, k);
			slashSlashComments(true);
			slashStarComments(true);
//          ordinaryChar('/'); //default was comment
			wordChars('.', '.');

			int t;
			while ((t = nextToken()) != TT_EOF) {
				if (t != TT_WORD)
					continue;
				if (sval.equals("class"))
					break;
				if (!sval.equals("package"))
					continue;
				t = nextToken();
				name = sval + '.' + name;
			}
		}
		public String getName() {
			return name;
		}
	}

	class Ear implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			//System.out.println("Action: "+cmd);
		}
	}
}
