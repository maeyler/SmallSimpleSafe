package mae.sss;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.lang.reflect.Method;

import mae.util.Console;
import mae.util.Loader;
import mae.util.TinyButton;
import mae.util.ProxyMaker;
import mae.brow.*;
import mae.util.Scaler;
//final static Font FONT = Scaler.scaledFont("SansSerif", 0, 11); //V1.68

/**
 * Instance methods simplify class loading from a jar file; <BR>
 * class methods add functionality to SSS, mostly to choose from something
 */
public class Chooser {

	static Chooser classD;
	static FontDialog fontD;
	static File lastDir;  
	static final String //SimpleFilter 
		CLASS_FLTR = "*class", JAR_FLTR = "*jar", 
	        TEACH_FLTR = "*.teacher;*.txt";
        static final Runtime RT = Runtime.getRuntime();

	/** Opens Browser */
	public static Class browser() {  //modified V1.65
            if (Browser.instance == null) Browser.main();
            Menu.toWindow(Browser.instance.getPanel()).setVisible(true);
	    return null;
	}
	/** Opens ProxyMaker */
	public static Class proxyMaker() {  //V1.66
	    return ProxyMaker.class;
	}
	/** Pick your color using JColorChooser */
	public static Color pickColor(Color c) {  //V1.65
		return JColorChooser.showDialog(Menu.frm, "Choose your color", c);
	}
	/** 
         * input: a single java file
         * Compile all java files in its folder
        */ 
        public static void compileAll(File f) { //V2.02
            if (!f.getName().endsWith(".java")) 
                throw new RuntimeException(f+" not a java file");
            if (Fide.instance == null) Fide.main();
            Fide F = Fide.instance; F.open(f);
            JavaSourceHandler h = (JavaSourceHandler)F.getHandler();  
            //new JavaSourceHandler(); h.setSource(f, F);
	    h.compileAll();
	}
	/** An array of all framed windows, including hidden ones */
	public static Window[] windows() {  //V1.66
		return Frame.getFrames();
	}
	/** An array of active Threads */
	public static Thread[] threads() {
		int n = Thread.activeCount();
		Thread[] t = new Thread[n];
		Thread.enumerate(t);
		return t;
	}
	/** Invokes System.gc(), showing time and free memory */
	public static void garbageCollection() {
		long t1 = System.currentTimeMillis();
		RT.gc(); RT.runFinalization();
		long t2 = System.currentTimeMillis();
		String t = Loader.divide(t2 - t1, 1000);
		long x = RT.freeMemory();
		long y = RT.totalMemory();
		String f = Loader.divide(x, 1000000);
		String m = Loader.divide(y, 1000000);
		String s = "Memory: " + m + "M  Free: " + f + "M (";
		System.out.println(s + (100*x/y) + "%) in "+ t +"sec");
	}

	/** Shows standard file chooser */
	public static File file() {
		return Console.fileToOpen(null);
	}
	/** Starts a Teacher session from a named resource */
	public static void runTeacher(String s) {
		Teacher.start("teach/" + s + ".teacher");
	}
	/** Starts a Teacher session from a File */
	public static Class runTeacher() {
		File f = Console.fileToOpen(TEACH_FLTR); 
		if (f != null) Teacher.start(f);
		return null;
	}
	/**
	 * Shows a dialog to choose a java class <BR>
	 * (classes that begin with java and javax can be chosen).
	 * 
	 * @throws ClassNotFoundException
	 */
	public static Class systemClass() throws ClassNotFoundException {
		return classD.chooseClass();
	}
	/**
	 * Chooses system class by name <BR>
	 * (searching java classes, extensions, and the class path)
	 * 
	 * @throws ClassNotFoundException
	 */
	public static Class systemClass(String name) throws ClassNotFoundException {
		return Class.forName(name);
	}
	/** Who loaded this Object? */
	public static ClassLoader getLoader(Object x) {
		Class c = (x instanceof Class) ? (Class) x : x.getClass();
		return c.getClassLoader();
	}
	/**
	 * Shows standard file chooser to choose a class file. Choosing a loaded
	 * class will load the new version, if modified since last loading
	 * 
	 * @throws ClassNotFoundException
	 */
	public static Class loadClass() throws ClassNotFoundException {
		File f = Console.fileToOpen(CLASS_FLTR);
		if (f == null)
			return null;
		return mae.util.Loader.loadClass(f);
	}
	/**
	 * Most general loading method: loads any class anywhere, if its URL is
	 * known.
	 * <P>
	 * <B>Use of <tt>loadClass(URL, String)</tt> </B>
	 * 
	 * <pre>
	 * 
	 *  1. choose jar File that contains your class
	 *    f = Chooser.file(&quot;jar&quot;);
	 * 
	 *  2. get URL of that File object
	 *    u = f.toURL();
	 * 
	 *  3. press ESC and load your class
	 *    Chooser.loadClass(u, &quot;Hello&quot;);
	 *    class Hello
	 *  
	 * </pre>
	 * 
	 * <P>
	 * <B>How to load a remote class </B>
	 * 
	 * <pre>
	 * 
	 *  1. get URL class (click on java.net and then on URL)
	 *    Chooser.systemClass();
	 *    class java.net.URL
	 * 
	 *  2. enter URL of a remote Applet (the slash at the end is crucial)
	 *    u2 = new URL(&quot;http://java.sun.com/applets/jdk/1.4/demo/applets/ArcTest/&quot;);
	 * 
	 *  3. press ESC and load your class (MAY TAKE SOME TIME)
	 *    Chooser.loadClass(u2, &quot;ArcTest&quot;);
	 *    class ArcTest
	 * 
	 *  4. make an instance
	 *    arc = new ArcTest();
	 * 
	 *  5. press ESC and show the Applet (you need to resize the Frame)
	 *    Menu.toFrame(arc);
	 *  
	 * </pre>
	 * 
	 * @throws ClassNotFoundException
	 */
	public static Class loadClass(URL u, String name)
			throws ClassNotFoundException {
		return mae.util.Loader.loadClass(u, name);
	}
	/** Shows a font chooser */
	public static void fonts() {
		if (fontD == null) {
			fontD = FontDialog.newDialog(Inspector.ins);
                        Scaler.scaleWindow(fontD);  //V1.68
			System.err.println("Font dialog initialized");
		}
		fontD.setVisible(true);
	}
	/**
	 * Shows SSS version (splash screen) public static Class version() {
	  InspectorPanel.splash.setVisible(true); return null; }
	 */

	final Map cls = new HashMap(); //package name --> List
	final List top = new ArrayList(); //top-level package list
	JList lst;
	String pckg;
	int count;
	boolean javaOnly;
	Dialog dlg;
	Comparator comp = new Comp();
	Ear ear = new Ear();
	final static int GAP = 5;
	final static Font NORM = Scaler.scaledFont("SansSerif", 0, 11);
	final static Font BOLD = Scaler.scaledFont("SansSerif", 1, 11);
	final static String TITLE = "Choose Class", CANCEL = "Cancel",
			BACK = "Back";
	ClassLoader ldr;

	void setFields(Frame f, ClassLoader L) {
		dlg = new Dialog(f);
                int x = f.getX() + (f.getWidth() - dlg.getWidth())/2;
                int y = f.getY() + 50;
                dlg.setLocation(x, y);
		ldr = L;
	}
	Chooser(Frame f) { //called from Inspector & main()
		setFields(f, ClassLoader.getSystemClassLoader());
		try {
		    readModules();  //V2.08  Java 9
		} catch (Exception x) {
		    addJavaRT();
		}
		classD = this;
	}
	void readModules() throws Exception {  //V2.08  Java 9
                count = 0;
                int numPack = cls.size();
                Class<?> c = Class.forName("mae.util.ModuleSystem");
                Method m = c.getDeclaredMethod("readClassesFromModule");
                Object list = m.invoke(null);
                for (Object s : (List)list) addToMap(s.toString());
                makeListAndReport(numPack, "Module System");
	}
	/**
	 * Chooses a jar file and makes a Chooser with it using a new Loader object;
	 * <BR>
	 * returns null if a jar file is not chosen
	 */
	public Chooser() throws IOException, ClassNotFoundException {
		File f = Console.fileToOpen(JAR_FLTR);
		setFields(Menu.frm, new Loader(f));
		addJarFile(f);
	}
	/**
	 * Makes an instance using the ClassLoader L.
	 * <P>
	 * All jar files related to L will be scanned and a dialog will be made.
	 */
	public Chooser(ClassLoader L) throws IOException, ClassNotFoundException {
		setFields(Menu.frm, L);
		if (!(L instanceof URLClassLoader))
			return;
		URL[] u = ((URLClassLoader) L).getURLs();
		for (int i = 0; i < u.length; i++)
			addJarContents(Loader.toFile(u[i]), false);
	}
	static boolean isJarFile(File f) {
		return (f != null) && f.getName().endsWith(".jar");
	}
	/** Returns the ClassLoader associated with this Chooser */
	public ClassLoader loader() {
		return ldr;
	}
	void addJavaRT() {
		String home = System.getProperty("java.home");
		File rt = new File(new File(home, "lib"), "rt.jar");
		try {
			addJarContents(rt, true);
		} catch (Exception x) {
			System.err.println(x);
		}
	}
	/** Returns the class selected by the user in the dialog */
	public Class chooseClass() throws ClassNotFoundException {
		String s = getSelection();
		return (s == null) ? null : ldr.loadClass(s);
	}
	/** Adds a jar file selected by the user */
	public Class addJarFile() throws IOException, ClassNotFoundException {
		return addJarFile(Console.fileToOpen(JAR_FLTR));
	}
	/** Adds a jar file to the ClassLoader and the dialog */
	public Class addJarFile(File f) throws IOException, ClassNotFoundException {
		addJarContents(f, false);
		Loader.addURLto(f.toURL(), ldr);
		String m = Loader.mainClassOf(f);
		return ldr.loadClass(m);
	}
	void addJarContents(File f, boolean java) throws IOException,
			ClassNotFoundException {
		if (!f.exists() || !isJarFile(f))
			throw new IOException("not a JAR: " + f);
		//System.err.println("add "+f);
		javaOnly = java;
		ZipFile z = new ZipFile(f);
		readZipFile(z);
		z.close();
	}
	String getSelection() {
		setData(top, null);
		dlg.setVisible(true);
		Object s = lst.getSelectedValue();
		return (s == null) ? null : pckg + "." + s;
	}
	void setData(Collection L, String s) {
		dlg.setTitle((s == null) ? TITLE : s);
		pckg = s;
		lst.clearSelection();
		lst.setListData(L.toArray());
		lst.ensureIndexIsVisible(0);
	}
	void addToMap(String s) {
		if (javaOnly && !s.startsWith("java"))
			return;
		int j = s.lastIndexOf(".class");
		if (j < 0 || s.indexOf('$') > 0 || s.indexOf('+') > 0)
			return;
		s = s.substring(0, j); //drop class extension
		if (s.endsWith("Exception") || s.endsWith("Error"))
			return;
		try {
			int k = s.lastIndexOf('/');
			//if (k == s.length()-1) return;
			String dir = s.substring(0, k).replace('/', '.');
			Set L = (Set) cls.get(dir);
			if (L == null) {//keep L in order
				L = new TreeSet(comp);
				cls.put(dir, L);
			}
			L.add(s.substring(k + 1));
			count++;
		} catch (StringIndexOutOfBoundsException x) {
			//return silently
		}
	}
	void makeTopList() {
		top.clear();
		//each package goes into top or a list in cls
		Object[] keys = cls.keySet().toArray();
		int n = keys.length;
		if (n == 0)
			return;
		Arrays.sort(keys);
		top.add(keys[0]);
		for (int i = 1; i < n; i++) {
			String s = (String) keys[i];
			int k = s.lastIndexOf('.');
			String p = (k < 0) ? "" : s.substring(0, k);
			Set L2 = (Set) cls.get(p);
			if (L2 != null)
				L2.add(s);
			else {
				String t = (String) top.get(top.size() - 1);
				if (!p.startsWith(t))
					top.add(s);
				else
					((Set) cls.get(t)).add(s);
			}
		}
		//Collections.sort(top);
	}
	void readZipFile(ZipFile f) {
		count = 0;
		int numPack = cls.size();
		//add each zip entry to its list in cls
		Enumeration e = f.entries();
		while (e.hasMoreElements()) {
			ZipEntry z = (ZipEntry) e.nextElement();
			if (!z.isDirectory())
				addToMap(z.getName());
		}
		makeListAndReport(numPack, f.getName());
	}
	void makeListAndReport(int numPack, String msg) {
                if (count == 0) return;
		makeTopList();
		System.out.printf("in %s:%n", msg);
		String t = (cls.size() - numPack) + " packages and "
				 + count + " classes available";
		System.out.println(t);
	}

	class Dialog extends JDialog {
		Dialog(Frame f) {
			super(f, TITLE, true);
			lst = new JList(top.toArray());
			lst.setCellRenderer(new Renderer());
			//lst.setFont(NORM);
			lst.addKeyListener(ear);
			lst.addListSelectionListener(ear);

			JComponent cp = (JComponent) getContentPane();
			cp.setBackground(Color.orange);
			JScrollPane pan = new JScrollPane(lst);
			pan.setBackground(Color.lightGray);
			pan.setPreferredSize(new Dimension(230, 466));
			cp.add(pan, "Center");
			cp.add(bottom(), "South");
			cp.setBorder( //  bottom = 0
					BorderFactory.createEmptyBorder(GAP, GAP, 0, GAP));
			Scaler.scaleComp(cp);  //V1.68
                        pack();
		}
		JPanel bottom() {
			JPanel bot = new JPanel();
			bot.setOpaque(false);
			JButton b = new TinyButton(CANCEL);
			b.setMnemonic('C');
			b.addKeyListener(ear);
			b.addActionListener(ear);
			b.setToolTipText("You may use <ESC> key");
			bot.add(b);
			b = new TinyButton(BACK);
			b.setMnemonic('B');
			b.addKeyListener(ear);
			b.addActionListener(ear);
			b.setToolTipText("You may use <BS> key");
			bot.add(b);
			return bot;
		}
	}

	class Ear extends KeyAdapter
			implements
				ActionListener,
				ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting())
				return;
			JList lst = (JList) e.getSource();
			String s = (String) lst.getSelectedValue();
			if (s == null)
				return;
			Set L = (Set) cls.get(s);
			if (L == null)
				dlg.setVisible(false);
			else
				setData(L, s);
		}
		public void keyPressed(KeyEvent e) {
			char c = e.getKeyChar();
			if (c == KeyEvent.VK_BACK_SPACE)
				setData(top, null);
			else if (c == KeyEvent.VK_ESCAPE)
				dlg.setVisible(false);
		}
		public void actionPerformed(ActionEvent e) {
			String s = e.getActionCommand();
			if (s.equals(BACK))
				setData(top, null);
			else if (s.equals(CANCEL))
				dlg.setVisible(false);
		}
	}

	class Renderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value,
				int i, boolean isSelected, boolean hasFocus) {
			super.getListCellRendererComponent(list, value, i, isSelected,
					hasFocus);
			boolean isPackage = (cls.get(value) != null);
			setFont(isPackage ? BOLD : NORM);
			return this;
		}
	}

	class Comp implements Comparator {
		public int compare(Object x, Object y) {
			boolean xPack = (cls.get(x) != null);
			boolean yPack = (cls.get(y) != null);
			if (xPack && !yPack)
				return -1; //x<y
			else if (!xPack && yPack)
				return 1; //x>y
			else
				return ((String) x).compareTo((String) y);
		}
	}

	/*
	 * should not appear in SSS 
public static void main(String[] args) {
    JFrame frm = new JFrame("Chooser");
    frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
    frm.setVisible(true);
    Chooser p = new Chooser(frm); //p.addJarFile(new File("sss.jar"));
    //System.out.println(p.getSelection()); 
    //p.addJarFile(new File("brow.jar")); 
    String s; 
    while ((s = p.getSelection()) != null)
        System.out.println(s); 
}
	 */
}
