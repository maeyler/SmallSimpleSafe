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

    Class<?> prog;
    File target;
    Method main;
    boolean ready;

    static Method javac;
    static final String[] ARGS = new String[0]; 
    static final Class[] stringArray = {ARGS.getClass()};
    static final String COMP = "com.sun.tools.javac.Main";

    /** Sets fields of this SourceHandler */
    public boolean setSource(File f, Editor d) {
        source = f;
        edit = d;
        if (javac == null)
            try {
                Class<?> c = //Class.forName(COMP);
                openArchive("java", COMP, "tools.jar", d);
                javac = c.getMethod("compile", stringArray);
            } catch (Exception x) {
                edit.setMessage(x.getMessage());
            }
        String s = f.getName();
        int k = s.lastIndexOf(".java");
        if (k < 0) return false;
        s = s.substring(0, k) + ".class";
        target = new File(f.getParent(), s);
        ready = !target.exists() ? false : new ClassSummary(target).hasMain();
        return canCompile();
    }

    void loadTarget() {
        if (!target.exists()) return;
        try {
            prog = Loader.loadClass(target);
            main = prog.getMethod("main", stringArray);
            ready = true;
        } catch (NoSuchMethodException x) {
        } catch (ClassNotFoundException x) {
            edit.setMessage(x.getMessage());
        }
    }
    public boolean canCompile() {
        return (javac != null && source != null);
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

    public JMenu menu() { return null; }  //V1.65
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
        if (javac == null || !canCompile())
            throw new RuntimeException("not initialized");
        prog = null;
        String[] a = { "-cp", getClassPath(f), "-g" };
        //compiler options such as "-Xlint:unchecked" should be at a[0]
        List<String> L = new ArrayList<>(Arrays.asList(a));
        //for (String s : a) L.add(s);
        if (all) {
            final File p = f.getParentFile();
            for (String s : p.list()) 
                if (s.endsWith(".java")) L.add(p+File.separator+s);
        } else {
            L.add(f.getAbsolutePath());
        }
        new Comp(L.toArray(a)).start();
    }
    int compile(String[] a) { //called by class Comp
        System.out.print("javac ");
        for (String s : a) System.out.print(s+" ");
        System.out.println();
        try {
            return (int)javac.invoke(null, (Object)a);
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
                edit.setMessage("compiler error "+k, false);
            }
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
