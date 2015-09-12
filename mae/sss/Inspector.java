// M A Eyler
//V1.0 22.2.2003 simplified from Inspector V1.55
//V1.1 7.3 use object names ==> Presentation
//V1.2 26.3 hot deployment and ClassChooser
//V1.3 12.5 use inner classes; mae.util.Loader and More
//V1.4 28.8 demo mode (applet) and inspector, Properties

package mae.sss;
import java.util.*;
import java.io.File;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Component;
import java.awt.event.*;
import java.lang.reflect.*;
import javax.swing.*;
import javax.swing.event.*;
import mae.util.ArrayListModel;
import mae.util.PropertyManager;
import mae.util.Reporter; //V1.65

public class Inspector {

    static Inspector ins;
    static Comp comp = new Comp(); //compares members of a class
    ArrayListModel list; //classes and objects on the left
    List flds, meth; //members on the right (& middle)
    final Map clsMap = new HashMap(); //String --> Class
    final Map objMap = new HashMap(); //Integer --> Data
    final Map idObj = new HashMap(); //String --> Object
    Class cls; //currently shown Class
    Object obj; //currently shown Object
    //Object prev; //previous Class or Object
    History hist;
    //boolean demo;
    int objCount, firstM, lastM;
    JFrame frm; //used in ParamDialog
    InspectorPanel panel;
    ParamDialog dlg;
    String[] defArgs;
    String defName, returnedName;
    boolean objectSelected;
    final Data NULL = new Data(null, null);
    final static int BASE = 10, //begin counting at #10
        MAX = 200, //lines in arrays
        HIST_SIZE = 8; //objects remembered
    final static String PREFIX = "x", BECOMES = "; //--> ",
        THICK_SEP = "==================================",
        MID_SEP = "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=",
        THIN_SEP = "--------------------------------------------";
    //String MENU = "mae.sss.Menu";
    //Class MENU; //not static
    final static Class 
        MENU = Menu.class,
        CLASS = Class.class, 
        OBJECT = Object.class,
        STRING = String.class;
    static Class DEPREC;  //assigned below
    final static Class[]
        EMPTY = {};  //not needed in 5.0

    static Method rename, hasAnnot;
    static {
        try {
            Class c1 = Inspector.class;
            rename = c1.getDeclaredMethod("rename", EMPTY);
            //rename.setAccessible(true);
            DEPREC = Class.forName("java.lang.Deprecated"); //5.0
            Class c2 = AccessibleObject.class;
            String s = "isAnnotationPresent";
            hasAnnot = c2.getDeclaredMethod(s, new Class[] {CLASS});
        } catch (Exception x) {
            System.err.println(x);
        }
    }

    public Inspector(JFrame f, InspectorPanel p) {
        objCount = BASE;
        ins = this;
        frm = f;
        panel = p;
        //demo = p.demo;  if (!demo) 
        Reporter.clearFile();

        dlg = new ParamDialog(frm, idObj);
        panel.addListeners(new Ear());
        if (f != null) {
            PropertyManager.setIcon(f, "sss.gif");
            //panel.splash.setIconImage(f.getIconImage());
        }
        list = new ArrayListModel();
        panel.left.setModel(list);
        list.add(THICK_SEP);
        flds = new ArrayList();
        meth = new ArrayList();
        panel.right.setCellRenderer(new Renderer());
        Menu.frm = f;
        /*if (demo) {
            MENU = mae.sss.DemoMenu.class;
        } else*/ {
            //MENU = mae.sss.Menu.class;
            new Dropper(this, panel.left); //for drop support
            new Init().start(); //init(frm);
        }
        hist = new History(HIST_SIZE, MENU);
    }

    static class Init extends Thread {
        public void run() {
            setPriority(3);
            setName("Menu.Init");
            //ClassD takes time
            Chooser cc = new Chooser(Menu.frm);
            //cc.addJavaRT(); Chooser.classD = cc;
        }
    }
    ///==============================================================
    Data getData(Object x) {
        Integer h = new Integer(System.identityHashCode(x));
        Data d = (Data) objMap.get(h);
        return (d == null) ? NULL : d;
    }
    Data addObject(Object x, String id) {
        Data d = getData(x);
        if (d != NULL)
            return d;
        if (id == null || id.equals("") || idObj.get(id) != null) {
            id = PREFIX + objCount;
            objCount++;
        }
        d = new Data(x, id);
        Integer h = new Integer(System.identityHashCode(x));
        objMap.put(h, d);
        idObj.put(id, x);
        return d;
    }
    void adjustRows(int r) {
        Iterator i = objMap.values().iterator();
        while (i.hasNext()) {
            Object x = i.next();
            if (!(x instanceof Data))
                continue;
            Data t = (Data) x;
            if (t.row > r)
                t.row--;
        }
    }
    void removeObject(Data d) {
        if (d == null || d == NULL)
            throw new IllegalArgumentException("no such object");
        int hash = System.identityHashCode(d.obj);
        objMap.remove(new Integer(hash));
        idObj.remove(d.id);
        adjustRows(d.row);
        list.remove(d);
        if (obj == d.obj)
            obj = null;
        d.clear(); //allow gc() to reclaim obj
        //if (prev == d.obj) prev = null;
    }
    public void renameObject(Object x, String id) {
        Data d = getData(x);
        if (d == NULL)
            throw new IllegalArgumentException("no such object");
        if (id == null || id.equals(""))
            throw new IllegalArgumentException("invalid name");
        if (idObj.get(id) != null)
            throw new IllegalArgumentException(id + " is used");
        idObj.remove(d.id);
        idObj.put(id, x);
        d.id = id;
    }
    public Object rename() {
        if (obj != null)
            renameObject(obj, returnedName);
        return obj;
    }
    void removeClass(Class c) {
        Object x = clsMap.remove(c.getName());
        if (x == null)
            return;
        list.remove(c);
        if (cls == c)
            cls = null;
        //if (prev == c) prev = null;
    }
    void addClassName(Class c, List L, List lst) {
        String name = c.getName();
        if (name.startsWith("["))
            return;
        name = stripIds(name, 1);
        L.add(c); //Class as a separator
        int m = c.getModifiers();
        String sep = c.isInterface() ? THIN_SEP : (Modifier.isAbstract(m)
                ? MID_SEP
                : THICK_SEP);
        int k = (int) sep.length() / 2 - name.length() / 2;
        if (k < 3)
            k = 3;
        sep = sep.substring(0, k);
        lst.add(sep + name + sep);
    }
    void addInterfaces(Class c, List L, List lst, boolean title) {
        if (title) addClassName(c, L, lst);
        Class[] N = c.getInterfaces();
        for (int i = 0; i < N.length; i++)
            addInterfaces(N[i], L, lst, true);
    }
    void addFields(Object ref, Class c, List L, boolean isStatic, List lst) {
        if (L.contains(c)) return;
        /*if (!demo)*/ addClassName(c, L, lst);
        Field[] F = /*demo ? c.getFields() :*/ c.getDeclaredFields();
        /*if (!demo)*/ AccessibleObject.setAccessible(F, true);
        boolean all = panel.dispAll.isSelected(); //dispAll not checked
        for (int i = 0; i < F.length; i++) {
            Field f = (Field) F[i];
            int mod = f.getModifiers();
            if (!all && !Modifier.isPublic(mod))
                continue;
            if (Modifier.isStatic(mod) != isStatic)
                continue;
            //displayField(ref, f, L, lst);
            Class typ = f.getType();
            String s = f.getName();
            try {
                Object val = f.get(ref);
                String v = valueToName(typ, val);
                lst.add(typeToName(typ) + "  " + s + " = " + v);
                if (typ.isPrimitive())
                    L.add(null);
                else
                    L.add(new Data(val, s));
                //} catch (NullPointerException x) {
                //    errorMessage(x, "NullPointer at "+f);
            } catch (Exception x) {
                errorMessage(x);
            }
        }
        //      addInterfaces(c, L, lst, false);
        Class[] N = c.getInterfaces();
        for (int i = 0; i < N.length; i++)
            addFields(null, N[i], L, isStatic, lst);
    }
    public static boolean isDeprecated(AccessibleObject a) {
        //return a.isAnnotationPresent(DEPREC);  5.0
        if (hasAnnot == null) return false;
        try {
               //Object d = hasAnnot.invoke(a, new Class[] {DEPREC});
            Object d = hasAnnot.invoke(a, DEPREC);
            return ((Boolean)d).booleanValue();
        } catch (Exception x) {
            return false;
        }
    }
    void addConstructors(Object ref, Class c, List L, List lst) {
        if (!c.isInterface()) addClassName(c, L, lst);
        if (c == OBJECT) return;
        Constructor[] S = c.getConstructors();
        Arrays.sort(S, comp);
        boolean abst = Modifier.isAbstract(c.getModifiers());
        if (!c.isInterface() && !abst)
            for (int i = 0; i < S.length; i++) {
                Constructor con = S[i];
                //int mod = con.getModifiers();
                //if (!Modifier.isPublic(mod)) continue;
                if (isDeprecated(con)) continue;
                lst.add(constrName(con));
                L.add(con);
            }
        Class[] N = c.getInterfaces();
        for (int i = 0; i < N.length; i++)
            addClassName(N[i], L, lst);
    }
    void addMethods(Object ref, Class c, List L, boolean isStatic, List lst) {
        //if (!isStatic && demo) addClassName(c, L, lst);
        firstM = lst.size();
        Method[] M = c.getMethods();
        Arrays.sort(M, comp);
        for (int i = 0; i < M.length; i++) {
            Method met = M[i];
            if (met.getDeclaringClass() == OBJECT)
                continue;
            int mod = met.getModifiers();
 //         if (!Modifier.isPublic(mod)) continue;
            if (!c.isInterface() && Modifier.isStatic(mod) != isStatic)
                continue;
            if (isDeprecated(met)) continue;
            lst.add(methodName(met));
            L.add(met);
        }
        lastM = lst.size();
    }
    void displayArray(Object ref) {
        JList J = /*demo ? panel.right :*/ panel.middle;
        J.clearSelection();
        int len = Array.getLength(ref);
        Class typ = ref.getClass().getComponentType();
        List L = /*demo ? meth :*/ flds;
        L.clear(); //meth.clear();
        ArrayListModel M = new ArrayListModel();
        int m = (len < MAX) ? len : MAX;
        for (int i = 0; i < m; i++)
            try {
                Object val = Array.get(ref, i);
                M.add("[" + i + "] = " + valueToName(typ, val));
                L.add(typ.isPrimitive() ? null : new Data(val, null));
            } catch (Exception x) {
                errorMessage(x);
            }
        J.setModel(M);
    }
    void displayFields(Object ref) {
        JList J = /*demo ? panel.right :*/ panel.middle;
        J.clearSelection();
        boolean isStatic = (ref == null);
        Class c = isStatic ? cls : ref.getClass();
        List L = /*demo ? meth :*/ flds;
        L.clear(); //flds.clear();
        ArrayListModel M = new ArrayListModel();
        while (c != null) {
            addFields(ref, c, L, isStatic, M);
            c = c.getSuperclass();
        }
        J.setModel(M);
    }
    void displayMethods(Object ref) {
        JList J = panel.right;
        J.clearSelection();
        boolean isStatic = (ref == null);
        Class c = isStatic ? cls : ref.getClass();
        meth.clear();
        ArrayListModel M = new ArrayListModel();
        if (c.isInterface())
            addClassName(c, meth, M);
        addMethods(ref, c, meth, isStatic, M);
        if (isStatic)
            while (c != null) {
                addConstructors(ref, c, meth, M);
                c = c.getSuperclass();
            }
        J.setModel(M);
    }
    void displayMembers(Object ref) {
        JList J = panel.right;
        J.clearSelection();
        boolean isStatic = (ref == null);
        Class c = isStatic ? cls : ref.getClass();
        meth.clear();
        ArrayListModel M = new ArrayListModel();
        if (c.isInterface())
            addClassName(c, meth, M);
        addFields(ref, c, meth, isStatic, M);
        addMethods(ref, c, meth, isStatic, M);
        if (isStatic)
            while (c != null) {
                addConstructors(ref, c, meth, M);
                c = c.getSuperclass();
            }
        J.setModel(M);
    }
    void inspectClass(String name) {
        if (name == null)
            return;
        try {
            //inspectClass(Class.forName(name));
            ClassLoader L = getClass().getClassLoader();
            inspectClass(L.loadClass(name));
        } catch (ClassNotFoundException x) {
            errorMessage(x, name + " not found " + x);
        } catch (Exception x) {
            errorMessage(x);
            x.printStackTrace();
        }
    }
    void inspectClass(Class c) {
        //if (hist.current() != c)
        hist.add(c);
        //Object x = objectSelected? obj : cls;
        //if (prev!=x && c!=x) prev = x;
        cls = c;
        objectSelected = false;
        String name = c.getName();
        Class old = (Class) clsMap.get(name);
        int ind = 0;
        boolean found = false;
        while (ind < clsMap.size()) { //search name in list
            int cmp = c.toString().compareTo(list.get(ind) + "");
            if (cmp == 0) {
                found = true;
                break;
            }
            if (cmp < 0)
                break;
            ind++;
        }
        if (!found) { //c was not in list
            clsMap.put(name, c);
            list.add(ind, c);
            output(c);
        } else if (c != old) { //new class
            clsMap.put(name, c);
            list.set(ind, c);
            output(c + " //again");
        }
        //      frm.setVisible(true);
        panel.adjustPanel(true, ind);
        //      panel.left.setSelectedIndex(ind);
        //      panel.left.ensureIndexIsVisible(ind);
        /*if (demo) {
            displayMembers(null);
        } else*/ {
            displayFields(null);
            displayMethods(null);
        }
        panel.setCounts(c);
    }
    void inspectObject(Object x) {
        if (x == null)
            return;
        inspectObject(getData(x));
    }
    void inspectObject(Data d) {
        if (d.id == null)
            return; //primitive
        if (d.row == -1) { //not found
            d.row = list.size() - clsMap.size() - 1;
            list.add(d);
        }
        //if (hist.current() != d.obj) hist.add(d.obj);
        hist.add(d);
        //Object x = objectSelected? obj : cls;
        //if (prev!=x && d.obj!=x) prev = x;
        obj = d.obj;
        objectSelected = true;
        int k = d.row + clsMap.size() + 1;
        panel.adjustPanel(false, k);
        if (obj.getClass().isArray()) {
            displayArray(obj);
            //if (!demo)
                displayMethods(obj);
        //} else if (demo) {
        //    displayMembers(obj);
        } else {
            displayFields(obj);
            displayMethods(obj);
        }
        panel.setCounts(d);
    }
    void inspectItem(int k) {
        if (k >= 0 && k < clsMap.size())
            inspectClass((Class) list.get(k));
        else if (k > clsMap.size())
            inspectObject((Data) list.get(k));
    }
    static String parameters(Class[] C) {
        int n = C.length;
        if (n == 0)
            return "()";
        String s = "";
        for (int i = 0; i < n - 1; i++)
            s += typeToName(C[i]) + ", ";
        return "(" + s + typeToName(C[n - 1]) + ")";
    }
    static String constrName(Constructor m) {
        return "new " + stripIds(m.getName(), 1)
                + parameters(m.getParameterTypes());
    }
    static String methodName(Method m) {
        Class ret = m.getReturnType();
        String t = (ret == Void.TYPE) ? "" : " -> " + typeToName(ret);
        return //typeToName(m.getReturnType()) +
        stripIds(m.getName(), 1) + parameters(m.getParameterTypes()) + t;
    }
    static String stripIds(String s, int n) {
        int k = s.length();
        for (int i = 0; i < n; i++)
            k = s.lastIndexOf('.', k - 1);
        return s.substring(k + 1);
    }
    static String typeToName(Class typ) {
        String t = "";
        while (typ.isArray()) {
            typ = typ.getComponentType();
            t += "[]";
        }
        return stripIds(typ.getName(), 1) + t;
    }
    static String valueToName(Object val) {
        return valueToName(val.getClass(), val);
    }
    static String valueToName(Class typ, Object val) {
        final int M = 30; //max M chars
        if (val == null || typ.isPrimitive())
            return "" + val;
        if (typ.isArray()) {
            String t = typeToName(typ);
            t = t.substring(0, t.length() - 2); //strip "[]"
            return t + "[" + Array.getLength(val) + "]";
        }
        Class c = val.getClass(); //runtime class, not typ
        try { //is toString() defined in c?
            int h = System.identityHashCode(val);
            Method m = c.getMethod("toString", new Class[0]);
            if (m.getDeclaringClass() == OBJECT)
                return "@" + Integer.toHexString(h);
        } catch (NoSuchMethodException x) {
            //errorMessage(x, x+"???"); not a class method
        }
        String s;
        try {
            if (val instanceof String) {
                final char quote = '"';
                s = (String) val;
                if (s.length() > M)
                    s = quote + s.substring(0, M - 4) + "...";
                else
                    s = quote + s + quote;
            } else if (val instanceof Frame) {
                Frame f = (Frame) val;
                s = f.getName() + " - " + f.getTitle();
            } else if (val instanceof Dialog) {
                Dialog f = (Dialog) val;
                s = f.getName() + " - " + f.getTitle();
            } else if (val instanceof AbstractButton) {
                s = ((AbstractButton) val).getText();
            } else if (val instanceof Component) {
                String name = ((Component) val).getName();
                if (name != null)
                    s = name;
                else
                    s = stripIds(c.getName(), 1);
            } else if (val instanceof Map) {
                Map m = (Map) val;
                if (m.size() < 10)
                    s = "" + val;
                else
                    s = "{" + m.entrySet().iterator().next() + "...";
            } else if (val instanceof Collection) {
                Collection p = (Collection) val;
                if (p.size() < 10)
                    s = "" + val;
                else
                    s = "[" + p.iterator().next() + "...";
            } else
                s = "" + val;
        } catch (NullPointerException x) {
            s = "null";
        } catch (RuntimeException x) { 
            s = "???"; //for JDI, after VMDisconnect
        } catch (Throwable x) { //shouldn't happen
            s = x.getMessage();
        }
        if (s.length() > M)
            s = s.substring(0, M - 3) + "...";
        return s;
    }
    void errorMessage(Throwable x) {
        errorMessage(x, "" + x);
    }
    void errorMessage(Throwable x, String s) {
        panel.setMessage(s);
        System.err.println(s);
        /*if (!demo)*/ Reporter.append(x); //detailed report
    }
    void output(Object x) {
        System.out.println(x);
        //      panel.out.append(x+"\n");
        //      int n = panel.out.getText().length() - 1;
        //      panel.out.select(n, n);
    }
    void clearAll() {
        list.removeRange(clsMap.size() + 1, list.size() - 1);
        list.removeRange(0, clsMap.size() - 1);
        obj = null;
        objMap.clear();
        idObj.clear();
        objCount = BASE;
        cls = null;
        clsMap.clear();
        inspectClass(MENU);
    }
    void clearPanel() {
        if (objectSelected) {
            list.removeRange(clsMap.size() + 1, list.size() - 1);
            obj = null;
            objMap.clear();
            idObj.clear();
            objCount = BASE;
        } else {
            list.removeRange(0, clsMap.size() - 1);
            cls = null;
            clsMap.clear();
        }
        inspectClass(MENU);
    }
    String getArgument(Class t, Object x) {
        if (t == Character.TYPE) {
            int c = ((Character) x).charValue();
            if (c >= 32 && c <= 255)
                return "'" + x + "'";
            else
                return "" + c;
        }
        if (t.isPrimitive())
            return "" + x;
        if (x == null)
            return "null"; //no quotes
        Data d = getData(x);
        if (d != NULL)
            return d.id; //id for x
        if (t.isArray())
            return "{...}"; //**TO BE DONE**
        return valueToName(t, x);
    }
    String getArgList(Class[] C, Object[] a) {
        String s = "";
        int n = C.length - 1;
        if (n < 0)
            return s;
        for (int i = 0; i < n; i++)
            s += getArgument(C[i], a[i]) + ", ";
        s += getArgument(C[n], a[n]);
        return s;
    }
    String caller() {
        return objectSelected ? getData(obj).id : typeToName(cls);
    }
    void invoke(Object in, String cmd) {
        Class c = (in instanceof Class) ? (Class) in : in.getClass();
        try {
            returnedName = null; //default name will be assigned
            if (cmd.equals("new")) {
                Constructor m = c.getConstructor(EMPTY);
                invoke(null, m, EMPTY, EMPTY);
            } else if (cmd.equals("class")) {
                addObject(cls, typeToName(cls));
                inspectObject(cls);
            } else {
                if (in == c) in = null;
                Method m = c.getMethod(cmd, EMPTY);
                invoke(in, m, EMPTY, EMPTY);
            }
        } catch (Throwable x) {
            errorMessage(x);
        }
    }
    void invoke(Object in, Member m, Class[] C, Object[] a) {
        String name = "";
        String param = "(" + getArgList(C, a) + ")";
        try {
            Object out;
            Class typ;
            if (m instanceof Method) {
                Method met = (Method) m;
                /*if (!demo)*/ met.setAccessible(true);
                int mod = met.getModifiers();
                if (Modifier.isStatic(mod))
                    in = null;
                typ = met.getReturnType();
                Class dec = met.getDeclaringClass();
                if (dec.isInterface()) {
                    if (obj == null)
                        throw new IllegalArgumentException("no object");
                    in = obj;
                }
                name = caller() + '.' + m.getName() + param;
                out = met.invoke(in, a);
            } else {
                Constructor con = (Constructor) m;
                /*if (!demo)*/ con.setAccessible(true);
                typ = con.getDeclaringClass();
                name = "new " + typeToName(typ) + param;
                out = con.newInstance(a);
            }
            boolean prim = typ.isPrimitive();
            boolean discard = !prim && "".equals(returnedName);
            if (typ == Void.TYPE || discard) {
                refresh(name + ";"); //output
            } else if (prim || out == null) {
                refresh(name + BECOMES + out); //output
                panel.setMessage("result: " + out);
            } else if (out instanceof Class) {
                output(name + ";");
                inspectClass((Class) out);
            } else {
                String id = addObject(out, returnedName).id;
                String v = valueToName(out);
                output(id + " = " + name + BECOMES + v);
                inspectObject(out);
            }
        } catch (InvocationTargetException x) {
            Throwable t = x.getTargetException();
            errorMessage(t);
            //         t.printStackTrace();
        } catch (Throwable x) {
            errorMessage(x, x.getClass() + "\n" + m.getName() + "()");
        } finally {
            panel.left.repaint();
            panel.left.requestFocus();
        }
    }
    void refresh(String s) {
        panel.left.repaint();
        if (s != null)
            output(s);
        if (objectSelected)
            inspectObject(obj);
        else
            inspectClass(cls);
    }
    void pickMethod(Object sel, int k) {
        if (sel == null)
            return;
        //if (teach != null) { //show selection in Teacher
        panel.right.repaint();
        panel.middle.repaint();
        //}
        String def = (defName != null) ? 
            defName : PREFIX + (objCount); //don't increment
        if (sel instanceof Method || sel instanceof Constructor) {
            Object in = objectSelected ? obj : null;
            interactive(in, (Member) sel, def, defArgs);
            defName = null;
            defArgs = null;
        } else if (sel instanceof Class) {
            inspectClass((Class) sel);
        } else { //field value
            inspectField(sel, k, def);
        }
        //if (teach != null) teach.interrupt(); //wake up!
    }
    void inspectField(Object sel, int k, String def) {
        Object val;
        String id, name;
        if (sel instanceof Data) {
            Data d = (Data) sel;
            val = d.obj;
            name = d.id;
            if (defName != null)
                id = defName;
            else if (d.id != null)
                id = d.id;
            else
                id = def;
        } else {
            val = sel;
            id = def; //use default id
            name = "";
        }
        if (val instanceof Class)
            inspectClass((Class) val);
        else if (val != null) {
            //is it an Object already displayed?
            if (getData(val) == NULL) {
                if (objectSelected && obj.getClass().isArray())
                    name = caller() + "[" + k + "]";
                else
                    name = caller() + '.' + name;
                dlg.setParams(name, id);
                dlg.setVisible(true); //modal -- waits until closed
                if (dlg.cancelled) {
                    JList lst = /*demo ? panel.right :*/ panel.middle;
                    lst.setSelectedIndices(new int[0]);
                    return;
                }
                id = dlg.objName;
                if ((PREFIX + objCount).equals(id))
                    id = null; //no name given
                id = addObject(val, id).id;
                String v = valueToName(val);
                output(id + " = " + name + BECOMES + v);
            }
            //output(caller()+'.'+d.id+" = "+valueToName(val));
            inspectObject(val);
        }
    }
    void selectByName(String name) {//Class or Object name
        //if (name == null) return;
        Object x = idObj.get(name);
        if (x != null) {
            inspectObject(x);
            return;
        }
        if ("Menu".equals(name)) {
            inspectClass(MENU);
            return;
        }
        for (int i = 0; i < clsMap.size(); i++) {
            //search name in Class list
            Class c = (Class) list.get(i);
            String s = c.getName();
            int k = s.lastIndexOf('.');
            if (s.substring(k + 1).equals(name)) {
                inspectClass(c);
                return;
            }
        }
        output("Object " + name + " not found");
    }
    void selectField(String name, String id) {
        if (name == null)
            return;
        JList J = /*demo ? panel.right :*/ panel.middle;
        ArrayListModel m = (ArrayListModel) J.getModel();
        for (int i = 0; i < m.size(); i++) {
            String fld = (String) m.get(i);
            int k = fld.indexOf(" = ");
            if (k < 0)
                continue;
            if (!fld.substring(0, k).endsWith(name))
                continue;
            //System.out.println(fld+" "+i);
            defName = id;
            J.ensureIndexIsVisible(i);
            J.setSelectedIndex(i);
            return;
        }
        output("Field " + name + " not found");
    }
    void selectMethod(String name, String id, String[] args) {
        //called with Member name only
        if (name == null)
            return;
        JList J = panel.right;
        //String[] A = dlg.parseArgs(args);
        Data d = findMember(name, args);
        int k = d.row;
        Member mem = (Member) d.obj;
        if (k >= 0) {
            defName = id;
            defArgs = args;
            //System.out.println(mem+" "+k);
            J.ensureIndexIsVisible(k);
            J.setSelectedIndex(k); //use defArgs
            return;
        }
        output("Method " + name + " not found");
    }
    Data findMember(String name, String[] args) {
        int k = -1;
        Member mem = null;
        for (int i = 0; i < meth.size(); i++) {
            Object x = meth.get(i);
            if (x instanceof Method) {
                Method met = (Method) x;
                Class[] typ = met.getParameterTypes();
                if (!met.getName().equals(name) 
                 || typ.length != args.length) continue;
                k = i;
                mem = met; //name & length OK
                if (dlg.accepts(typ, args))
                    break;
            } else if (x instanceof Constructor) {
                Constructor con = (Constructor) x;
                Class[] typ = con.getParameterTypes();
                if (typ.length != args.length)
                    continue;
                k = i;
                mem = con; //length OK
                if (dlg.accepts(typ, args))
                    break;
            }
        }
        //System.out.println(" >>>"+mem);
        return new Data(mem, k);
    }
    void interactive(Object in, Member mem, String id, String[] a) {
        String nam = null;
        Class[] typ = null;
        Class ret = null;
        if (mem instanceof Method) {
            Method met = (Method) mem;
            nam = methodName(met);
            typ = met.getParameterTypes();
            ret = met.getReturnType();
        } else if (mem instanceof Constructor) {
            Constructor con = (Constructor) mem;
            nam = constrName(con);
            typ = con.getParameterTypes();
            ret = con.getDeclaringClass();
        }
        if (a != null && typ.length != a.length) {
            System.out.println("poor args length " + a.length);
            a = null;
        }
        if (typ.length > 0 || ret != CLASS) {
            dlg.setParams(nam, typ, ret, id, a);
            dlg.setVisible(true); //modal -- waits until closed
            if (dlg.cancelled) {
                panel.right.setSelectedIndices(new int[0]);
                return;
            }
        }
        Object[] args; //returned arguments
        boolean prim = (ret == CLASS || ret.isPrimitive());
        if (typ.length == 0 && prim) {
            args = new Object[0];
            returnedName = null;
        } else {
            args = dlg.arg;
            returnedName = dlg.objName;
            if (returnedName == null && a != null)
                args = a; //use default args
            else if (returnedName.equals(PREFIX + (objCount)))
                returnedName = null; //no name given
        }
        if (args != null)
            invoke(in, mem, typ, args);
    }
    public void handleSelection(JList lst, int k) {
            //if (lst == panel.right) System.err.println("pickMethod "+k);
        try { //main dispatcher of JList events
            if (lst == panel.left)
                inspectItem(k);
            else if (lst == panel.middle)
                pickMethod(flds.get(k), k);
            else
                pickMethod(meth.get(k), k);
        } catch (Throwable x) {
            errorMessage(x, x.getMessage() + " in pickMethod");
        }
    }
    void removeSelection() {
        int[] a = panel.left.getSelectedIndices();
        int i = a.length;
        while (i > 0) { //process the array backward
            i--;
            int k = a[i];
            //System.out.println(">>"+i+". "+k);
            if (k < clsMap.size())
                removeClass((Class) list.get(k));
            if (k > clsMap.size())
                removeObject((Data) list.get(k));
        }
        //current becomes prev
        //if (prev instanceof Class) cls = (Class)prev;
        //else obj = prev;
        inspectClass(MENU);
    }
    void renameSelection() {
        if (!objectSelected || obj == null)
            return;
        String id = getData(obj).id;
        //String[] a = { id }; //current name
        interactive(this, rename, id, null);
    }
    void scrollTo(char c) {
        c = Character.toLowerCase(c);
        int i=firstM, j=lastM;
        if (j - i < 25) { //V1.66 select method by first char
               while (i < j) {
                        Member m = (Member) meth.get(i);
                        char d = m.getName().charAt(0);
                        if (d == c) handleSelection(panel.right, i); 
                        if (d >= c) break;
                        i++;
                    }
                    return;
                } //else 
                while (i < j) {
                        int k = (i + j) / 2;
                        Method m = (Method) meth.get(k);
                        char d = m.getName().charAt(0);
                        if (d < c) i = k + 1;
                        else if (d > c) j = k - 1;
                        else { i = k; break; }
                }
        if (i == lastM) i--;
        panel.right.ensureIndexIsVisible(i);
        //System.out.println(c+" scrollTo "+i);
    }
    //static final String[] TEST = {"Empty list", "-----------"};
    static class Lst extends JList {
        public Lst(String s) {
            setName(s);
            //setSelectionMode(0);
        }
        protected void processKeyEvent(KeyEvent e) {
            char c = e.getKeyChar();
            if (!Character.isLetter(c) || e.getModifiers() != 0)
                super.processKeyEvent(e);
            else if (e.getID() == KeyEvent.KEY_PRESSED)
                ins.scrollTo(c);
        }
    }

    class Ear implements    KeyListener,
        ItemListener,
        ActionListener,
        ListSelectionListener {

        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src == panel.clear)
                clearPanel();
            else if (src == panel.cmd) {
                String s = e.getActionCommand();
                if (objectSelected)
                    invoke(obj, s);
                else
                    invoke(cls, s);
            }
        }
        public void itemStateChanged(ItemEvent e) {
            Object src = e.getSource();
            int k = panel.left.getSelectedIndex();
            if (src == panel.dispAll)
                inspectItem(k);
        }
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting())
                return;
            JList lst = (JList) e.getSource();
            int[] a = lst.getSelectedIndices();
            if (a.length != 1) return;
            handleSelection(lst, a[0]);
        }
        public void keyTyped(KeyEvent e) {
            char c = e.getKeyChar();
            if (c == KeyEvent.VK_ESCAPE) {
                if (InspectorPanel.about.isShowing())  //V1.66
                                InspectorPanel.about.setVisible(false);
                else inspectClass(MENU); 
                //} else if (Character.isLetter(c)) {
                //   ins.scrollTo(c); --> Lst.processKeyEvent()
            } else if (c == KeyEvent.VK_PERIOD) {
                if (objectSelected)
                     inspectClass(cls);
                else inspectObject(obj);
            } else if (c == KeyEvent.VK_COMMA) {
                renameSelection();
            } else if (c == KeyEvent.VK_BACK_SPACE) {
                hist.backward();
            }
        }
        public void keyPressed(KeyEvent e) {
            int c = e.getKeyCode();
            if (c == KeyEvent.VK_F2) {
                renameSelection();
            } else if (c == KeyEvent.VK_F5) {
                refresh(null);
            } else if (c == KeyEvent.VK_LEFT) {
                hist.backward();
            } else if (c == KeyEvent.VK_RIGHT) {
                hist.forward();
            } else if (c == KeyEvent.VK_DELETE) {
                removeSelection();
            }
        }
        public void keyReleased(KeyEvent e) {
        }
    }

    class History extends mae.util.History {
        History(int m, Object x) {
            super(m, x);
        }
        void add(Object x) {
            if (x == null || x.equals(current()))
                return;
            append(x);
            //System.err.println(x+" >"+this);
        }
        public Object current() {
            return super.current();
        }
        protected boolean accept() {
            Object x = current();
            if (x == null)
                return false;
            if (x instanceof Class) {
                inspectClass((Class) x);
                return true;
            } else if (x instanceof Data && ((Data) x).id != null) {
                inspectObject((Data) x);
                return true;
            } else
                return false;
        }
    }

    class Renderer extends DefaultListCellRenderer {
        boolean italic(int i) {
            if (i >= meth.size()) return false;  //V1.66
            Object m = meth.get(i);
            if (m instanceof Member) {
                Class c = ((Member) m).getDeclaringClass();
                return objectSelected ? obj.getClass() != c : cls != c;
            } else return false;
        }
        public Component getListCellRendererComponent(JList list, Object value,
                int i, boolean isSelected, boolean hasFocus) {
            super.getListCellRendererComponent(list, value, i, isSelected,
                    hasFocus);
            setFont(italic(i) ? panel.italic : panel.normal);
            return this;
        }
    }

    static class Data {
        Object obj;
        String id;
        int row;
        Data(Member m, int k) {
            obj = m;
            row = k; //used in findMember() only
        }
        Data(Object x, String i) {
            obj = x;
            id = i;
            row = -1;
        }
        void clear() {
            obj = null;
            id = null;
            row = -1;
        }
        public String toString() {
            if (obj == null)
                return id + " = null";
            String t = typeToName(obj.getClass());
            String v = valueToName(obj);
            return t + "  " + id + " = " + v;
        }
    }

    static class Comp implements Comparator {
        public int compare(Object x, Object y) {
            if (x instanceof Member && y instanceof Member) {
                String i = ((Member) x).getName();
                String j = ((Member) y).getName();
                if (!i.equals(j))
                    return i.compareTo(j);
            }
            return x.toString().compareTo(y.toString());
        }
    }

    public static void main(String[] args) { new SSS(); }
}

