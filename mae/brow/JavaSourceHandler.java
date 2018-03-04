/*
 * JavaSourceHandler.java
 *
 * Created on July 20, 2003, 8:17 PM
 * Compile folder contents -- Dec 2015
 */

package mae.brow;

import java.io.*;
import java.util.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import mae.util.ClassSummary;
import mae.util.Loader;
import mae.util.Editor;

/**
 * Handles Java source file
 * 
 * @author Eyler
 */
public class JavaSourceHandler extends mae.util.SourceHandler {

    Class<?> prog;
    File target;
    Method main;
    boolean ready; //java program has main() -- ready to run

    static JavaCompiler JC = ToolProvider.getSystemJavaCompiler();
    static boolean isAvailable = (JC != null); //under JDK use javax.tools
    static private Method javac = null;    //under JRE 6-8 use tools.jar
    static final String[] ARGS = new String[0]; 
    static final Class[] stringArray = {ARGS.getClass()};
    static final String COMP = "com.sun.tools.javac.Main";
    static {
        if (!isAvailable && mae.sss.SSS.JAVA_version.compareTo("9")<0) 
            try {
                Class<?> c = //Class.forName(COMP);
                openArchive("java", COMP, "tools.jar", Fide.instance);
                javac = c.getMethod("compile", stringArray);
                isAvailable = true; //tools.jar is open
            } catch (Exception x) {
                Fide.instance.setMessage(x.getMessage());
            }
    }

    /** Sets fields of this SourceHandler */
    public boolean setSource(File f, Editor d) {
        source = f;
        edit = d;
        if (!isAvailable) return false;
        String s = f.getName();
        int k = s.lastIndexOf(".java");
        if (k < 0) return false;
        s = s.substring(0, k) + ".class";
        return setTarget(new File(f.getParent(), s));
    }
    public boolean setTarget(File t) {
        target = t;
        ready = !target.exists() ? false : new ClassSummary(target).hasMain();
        //if (ready) loadTarget();
        return canCompile();
    }

    void loadTarget() {
        if (!target.exists()) return;
        try {
            prog = Loader.loadClass(target);
            main = prog.getMethod("main", stringArray);
            main.setAccessible(true);
            ready = true;
        } catch (NoSuchMethodException x) {
        } catch (ClassNotFoundException x) {
            edit.setMessage(x.getMessage());
        }
    }
    public boolean canCompile() {
        return (isAvailable && source != null);
    }
    public void compile() { compile(source, false); }
    public void compileAll() { compile(source, true); }
    
    public boolean readyToRun() {
        return (ready && source.exists() && target.exists() 
                && source.lastModified() <= target.lastModified());
    }
    public void run() {
        if (!readyToRun())
            throw new RuntimeException("not compiled");
        if (prog == null)
            loadTarget();
        run(prog);
    }
    public JMenu menu() {  //V2.10
        Ear e = new Ear();
        JMenuItem i;
        JMenu menu = new JMenu("Java");
        menu.setMnemonic('J');
        i = new JMenuItem("Compile All");
        i.addActionListener(e);
        menu.add(i);
        return menu; 
    }
    public static String getClassPath(File f) {
        String name;
        try {
            name = new Name(f).getName();
        } catch (Exception x) {
            System.err.println(x);
            return f.getParent();
        }
        f = f.getAbsoluteFile();
        int k = name.lastIndexOf(".");
        while (k > 0) {
            f = f.getParentFile();
            k = name.lastIndexOf(".", k - 1);
        }
        System.out.println(name+ " in " +f.getParent());
        return f.getParent();
    }
    void compile(File f, boolean all) {
        if (!canCompile())
            throw new RuntimeException("not initialized");
        prog = null;
        if (all) {
            compileFiles(f.getParentFile().listFiles());
        } else {
            compileFiles(f);
        }
    }
    public void compileFiles(File... fa) {
        if (fa.length == 0) return;
        String[] sa = { "-cp", getClassPath(fa[0]), "-g" };
        //compiler options such as "-Xlint:unchecked" should be at a[0]
        List<String> L = new ArrayList<>(Arrays.asList(sa));
        for (File f : fa) {
            String s = f.getAbsolutePath();
            if (s.endsWith(".java")) L.add(s);
        }
        if (L.size() <= 3) return;
        System.err.println((L.size()-3)+" files will be compiled");
        new Comp(L.toArray(sa)).start();
    }
    int compile(String[] a) { //called by class Comp
        System.out.print("javac ");
        for (String s : a) System.out.print(s+" ");
        System.out.println();
        try {
            int k = JC != null?
                JC.run(null, System.out, System.err, a) :  //Java Compiler
                (Integer)javac.invoke(null, (Object)a);    //use tools.jar
            return k;
        } catch (Exception x) {
            edit.setMessage(x.getMessage()); return -1;
        }
    }
    public void run(File f) throws ClassNotFoundException {
        run(Loader.loadClass(f));
    }
    public void run(Class c) {
        new Exec(main).start();
    }

    class Exec extends Thread {
        Method met;
        Exec(Method m) {
            met = m;
        }
        public void run() {
            System.err.println("run "+met);
            String s = "";
            try {
                met.invoke(null, (Object)ARGS);
                return;
            } catch (InvocationTargetException x) {
                Throwable t = x.getTargetException();
                t.printStackTrace(System.out); /*!!*/
                if (edit != null)
                    edit.setMessage("" + t);
            } catch (Exception x) { //IllegalAccessException
                if (edit != null)
                    edit.setMessage("main() is not accessible");
                System.err.println(x);
                x.printStackTrace();
            }
        }
    }

    class Comp extends Thread {
        String[] args;
        Comp(String[] a) { args = a; }
        public void run() {
            edit.setMessage("compiling  " + source);
            long t = System.currentTimeMillis();
            System.gc();
            PrintStream err = System.err;
            System.setErr(System.out);
            ready = false;
            int k = compile(args);
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

    class Ear implements ActionListener {  //V2.10
        public void actionPerformed(ActionEvent e) {
           System.out.println("Action: "+e.getActionCommand());
           compileAll();
        }
    }

    static class Name extends StreamTokenizer {
        String name;
        Name(File f) throws IOException {
            super(new FileReader(f));
            name = f.getName();
            int k = name.lastIndexOf('.');
            if (k < 0) return;
            name = name.substring(0, k);
            slashSlashComments(true);
            slashStarComments(true);
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
}
