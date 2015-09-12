// 15.4.2003  inner class in Menu
// 19.4       into mae.util
// 18.5.2004  cooperate with ClassChooser
// 23.5       remove all GUI components 

package mae.util;

import java.io.File;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class Loader extends URLClassLoader {
   public static int count;
   static Field classes;
   static {
      try {
         Class c = ClassLoader.class;
         classes = c.getDeclaredField("classes");
         classes.setAccessible(true);
      } catch (Exception x) {
         System.err.println(x);
      }
   }
   int id;     //identifier
   long time;  //loading time

   public Loader(URL u) {
      super(new URL[] {u});  
      count++; id = count; //url = u; 
      time = System.currentTimeMillis();
      System.err.println(id()+" new Loader "+u);
   }
   public Loader(File f) throws MalformedURLException {
      this(f.toURL());  
   }
   /** Who loaded this Object? */   
   public static ClassLoader getClassLoader(Object x) {
      Class c = (x instanceof Class)? (Class)x : x.getClass();
      return c.getClassLoader();
   }
   /** List of classes loaded by ClassLoader L  */
   public static void listClasses(ClassLoader L) {
      System.out.println(listToString(listOfClasses(L))); 
   }
   public void listClasses() {
      String s = listToString(listOfClasses(this));
      System.out.println(id()+" "+s); 
   }

   public static File getClassPath(String name, File f) {
      f = f.getAbsoluteFile();
      int k = name.lastIndexOf(".");
      while (k > 0) {
          f = f.getParentFile();
          k = name.lastIndexOf(".", k-1);
      }
      return f.getParentFile();
   }
   public static Class loadClass(File f) 
      throws ClassNotFoundException { 
      if (!SimpleFilter.extension(f).equals("class"))  {
         final String MSG = " not a class file";
         throw new IllegalArgumentException(f + MSG);
      }
      String name = new ClassSummary(f).getName();
      f = getClassPath(name, f);
      URL u = null;
      try {
         u = f.toURL();
      } catch (MalformedURLException x) {
         System.err.println(x); 
         throw new ClassNotFoundException(""+x, x);
      }
      return loadClass(u, name);
   }
   public static Class loadClass(URL u, String name) 
      throws ClassNotFoundException { 
      Loader cl = new Loader(u);
//      cl = new URLClassLoader(new URL[] {u});
      Class c = Class.forName(name, true, cl);
      String s = (c.getClassLoader() == cl)? ""+u : "system";
      //System.err.println("load "+name+" from "+s);
      return c;
   }
   protected void finalize() throws Throwable {
      //if (listOfClasses(this).size() > 0)
      System.err.println(id()+" finalize "+this);
      super.finalize();
   }
   public void addURL(URL u) {
      URL[] a = getURLs();
      for (int i=0; i<a.length; i++) 
         if (a[i].equals(u)) return;
      super.addURL(u); 
      System.err.println(id()+" addURL "+u);
   }
   public static List listOfClasses(ClassLoader L) {
      try {
         if (classes != null) 
            return (List)classes.get(L);
      } catch (Exception x) {
         System.err.println(x);
      }
      return null;
   }
   public static String listToString(List cls) {
      String s = "";
      if (cls == null) return "unkown list";
      int n = cls.size();
      if (n == 0) return "[]";
      for (int i=0; i<n-1; i++) {
         Class c = (Class)cls.get(i);
         s += c.getName()+", ";
      }
      Class c = (Class)cls.get(n-1);
      return "["+ s + c.getName() +"] "+cls.size();
   }
   public static String divide(long t, int a) {  //redesigned V1.65
      if (t/a >= 20) return Long.toString(t/a);
      float x = 10*t/a;  //long division to float
      return Float.toString(x/10f); //one digit accuracy
   }
   public static String secondsToString(long t) {  //V1.65
      if (t > 86400) return divide(t, 86400)+" days";
      if (t > 3600) return divide(t, 3600)+" hr";
      if (t > 60) return divide(t, 60)+" min";
      return t+" sec";
   }
   public String time() {
      long t = (System.currentTimeMillis()-time)/1000;
      return secondsToString(t);
   }
   public String id() {
      return "#"+id+":";
   }
   public void showTime() {
      System.out.println(id()+" "+time()); 
   }
   public String toString() {
      String s = ""+getURLs()[0];  //url;
      int t = (int)(System.currentTimeMillis()-time)/1000;
      List cls = listOfClasses(this);
      if (t==0 || cls==null) return s;
      int n = cls.size();
      if (n > 3) s = n+" classes from "+s;
      else if (n > 0) s = listToString(cls)+" from "+s;
      return s+"  "+time();
   }

   /** Converts a file URL to File */   
   public static File toFile(URL u) {
      if (u.getProtocol().equals("file") && u.getRef() == null) {
         String s = u.toString();
         char sep = File.separatorChar;
         if (s.charAt(5) == sep) s = s.substring(5); //unix
         else s = s.substring(6).replace('/', sep);  //other
         return new File(s); 
      } else {
         return null; //not file
      }
   }
   /** Adds a URL to a ClassLoader */   
   public static void addURLto(URL u, ClassLoader L) {
      Class c = URLClassLoader.class;
      Class[] typ = { URL.class };
      try {
         ((URLClassLoader)L).getClass();  //just cast
         Method m = c.getDeclaredMethod("addURL", typ);
         m.setAccessible(true);
         Object[] arg = { u };
         m.invoke(L, arg);  //L.addURL(loc.toURL());
         //System.err.println(u+" is added to\n"+L);
      } catch (Exception x) {
         System.err.println(x);
         System.err.println("Cannot add URL "+u);
      }
   }
   /** Reads manifest of a jar File and returns main class name */   
   public static String mainClassOf(File f) {
      String s = null;
      try {
         Manifest man = new JarFile(f).getManifest();
         if (man != null)
            s = man.getMainAttributes().getValue("Main-Class");
      } catch (Exception x) {
         System.err.println(x);
      }
      return s;
   }
   /** Starts main() of main class of a jar File */   
   public static void startJAR(File f) {
      String s = mainClassOf(f);
      if (s == null) return;
      String[] args = {};
      Class[] typ = { args.getClass() };
      try {
         Class c = new Loader(f).loadClass(s);
         Method m = c.getDeclaredMethod("main", typ);
         m.setAccessible(true);
         Object[] a = { args };
         m.invoke(null, a);  
      } catch (Exception x) {
         System.err.println(x);
      }
   }
}
