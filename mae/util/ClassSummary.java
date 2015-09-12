// Eyler  15.5.2002

package mae.util;

import java.io.*;
import java.util.ArrayList;
import java.lang.reflect.Modifier;

/** Summary information for a java Class, obtained from class file.
 * <P>
 * Note that the related Class is not loaded.
 * Class information is read as a data file.
 * <PRE>
 * 15.05.2002 ClassInfo & ClassReader
 *  3.11.02   ConstAttrib
 *  6.12.02   use package
 *  7.7.03    simplify: remove attributes
 * 22.7.03    use HTML format
 *
 * Usage:
 * new ClassSummary("Small.class").description(false) 
 * --> summary for class Small in text format
 *
 * new ClassSummary("Test.class").toHTML()  
 * --> summary for class Test in HTML format
 * </PRE>
 * @author M A Eyler
 * @version 1.3
 */
public class ClassSummary  { 
    
   DataInputStream in;
   Constant[] cons;
   final static int MAGIC = 0xcafebabe;
   int versLo, versHi, access, thisC, superC;
   String[] intf;
   Member[] field, method;
   String attrib;
   boolean hasMain;
   final static String 
       MAIN ="main",  STR = "([Ljava/lang/String;)V";   

   /** Invokes main constructor by making a new File(s). */
   public ClassSummary(String s) { this(new File(s)); }
   /**
    * Main constructor: opens the class file f, decodes it, and closes it.
    * <P>
    * File is read to the end. 
    * Throws a RuntimeException, if end of file is not reached.
    */   
   public ClassSummary(File f) {
      try {
         in = new DataInputStream(new FileInputStream(f));
         if (in.readInt() != MAGIC) throw new 
            RuntimeException("Not a valid class file");
         versLo = in.readUnsignedShort();
         versHi = in.readUnsignedShort();
         cons = readConstants();     //1
         access = in.readUnsignedShort();
         thisC  = in.readUnsignedShort();
         superC = in.readUnsignedShort();
         intf = readInterfaces();    //2
         field = readMembers();      //3
         method = readMembers();     //4
         attrib = readAttributes();  //5
         if (in.available() > 0) throw new 
            RuntimeException(in.available()+" bytes left");
         in.close(); 
      } catch (IOException e) {
         throw new RuntimeException(e.getMessage());
      }
   }

   /** Returns class name */   
   public String getName() {
      return constant(thisC); 
   }
   /** Returns super class name */   
   public String getSuper() {
      return constant(superC); 
   }
   /** Returns true if and only if method "void main(String[])"  
    * exists in this class */   
   public boolean hasMain() {
      return hasMain; 
   }
   /**
    * Returns a short description for this class:
    * access, name, super class.
    */   
   public String toString() {
      return toString(false);
   }
   /** Returns a short description for this class,
    * in plain text or HTML.
    * <P>
    * If HTML, use bold tags around class name.
    * <PRE>
    *       FIND THE ERROR:
    *       if (Modifier.isInterface(n))
    *           s = "interface "+ constant(thisC);
    *           if (Modifier.isPublic(n)) s = "public "+s;
    *       else {
    *           if (n != 0) s = Modifier.toString(n)+" ";
    *           s += "class "+ constant(thisC) +" extends "+ constant(superC);
    *       }
    * </PRE>
    */   
   String toString(boolean HTML) {
      int n = access & (~Modifier.SYNCHRONIZED); //turn it off
      String name = getName();
      if (HTML) name = "<B>"+ name +"</B> ";
      String s = "";
      if (Modifier.isInterface(n)) {
          if (Modifier.isPublic(n)) s = "public ";
          s += "interface "+ name;
      } else {
          if (n != 0) s = Modifier.toString(n)+" ";
          s += "class "+ name +"\nextends "+ getSuper();
      }
      return s; 
   }
   /** Returns a full description for this class 
    * in HTML or plain text  */   
   public String description(boolean HTML) {
      String s = HTML? "<HTML>\n" : "";
      s += "(V"+versHi+"."+versLo+")  ";
      s += (cons.length-1)+" constants  \n";
      if (HTML) s += "<BR>\n";
      s += toString(HTML)+ "\n"; 
      if (HTML) s += "<BR>\n";
      int n = intf.length;
      if (n > 0) {
          s += "implements\n";
          for (int i=0; i<n-1; i++) 
             s += "  "+intf[i]+",\n";
          s += "  "+intf[n-1]+"\n";
      }
      s += memberInfo(field, "Fields", HTML);
      s += memberInfo(method, "Methods", HTML);
      s += attrib + "\n";
      return s;
   }
   static String memberInfo(Member[] mem, String desc, boolean HTML) {
      String s = "";
      int n = mem.length;
      if (n == 0) return s;
      if (HTML) s += "<OL>\n<B><I>"+ desc +"</I></B>\n";
      else s += n +" "+ desc +"\n";
      for (int i=0; i<n; i++) 
         s += "  "+mem[i].toString(HTML) + "\n";
      if (HTML) s += "</OL>\n";
      return s;
   }
   /** Returns a full description in HTML format
    * by invoking description(true);
    */   
   public String toHTML() {
      return description(true);
   }
   byte[] nextBytes(int n) throws IOException {
      byte[] b = new byte[n];
      in.readFully(b);
      return b;
   }
   Constant[] readConstants() throws IOException {
      int n = in.readUnsignedShort();
      cons = new Constant[n];
      for (int i=1; i<n; i++) {
         int t; Constant c = null;
         t = in.readUnsignedByte();
         switch (t) {
         case 1: 
            c = new Constant(t, in.readUTF()); break;
         case 3: 
            c = new Constant(t, ""+in.readInt()); break;
         case 4: 
            c = new Constant(t, ""+in.readFloat()); break;
         case 5: 
            c = new Constant(t, ""+in.readLong()); break;
         case 6: 
            c = new Constant(t, ""+in.readDouble()); break;
         case 7: case 8:
            int k = in.readUnsignedShort();
            c = new Binary(t, k, 0); break;
         case 9: case 10: case 11: case 12:
            int k1 = in.readUnsignedShort();
            int k2 = in.readUnsignedShort();
            c = new Binary(t, k1, k2); break;
         default :
            String msg = "illegal tag "+t+" at item "+i;
            throw new RuntimeException(msg);
         }
         cons[i] = c;
         if (t==5 || t==6) i++; //for double & long
      }
      for (int i=1; i<n; i++) 
         if (cons[i] instanceof Binary) cons[i].setStr(cons);
      return cons;
   }
   String constant(int k) { 
       return cons[k].toString().replace('/', '.'); 
   }
   String[] readInterfaces() throws IOException {
      int n = in.readUnsignedShort();
      String[] s = new String[n];
      for (int i=0; i<n; i++) {
         int k = in.readUnsignedShort();
         s[i] = constant(k);
      }
      return s;
   }
   String readAttributes() throws IOException {
      int n = in.readUnsignedShort();
      String s = "";
      for (int i=0; i<n; i++) {
         int nam = in.readUnsignedShort();
         int len = in.readInt();
         byte[] b = nextBytes(len); 
         s += "  -- ";
         s += cons[nam];
         if (len != 2)
            s += ":"+len;
         else {
            int k = (b[0] << 8) | (b[1] & 0xFF);
            s += "="+cons[k];
         }
      }
      return s;
   }
   Member[] readMembers() throws IOException {
      int n = in.readUnsignedShort();
      Member[] M = new Member[n];
      for (int i=0; i<n; i++) {
         int k1 = in.readUnsignedShort();
         int k2 = in.readUnsignedShort();
         int k3 = in.readUnsignedShort();
         String name = cons[k2].toString();
         String type = cons[k3].toString();
         if (name.equals(MAIN) && type.equals(STR)) hasMain = true;
         M[i] = new Member(k1, name, type, readAttributes());
      }
      return M;
   }

   static String[] parseType(String s) {
      ArrayList L = new ArrayList();
      int i = 0; 
      while (i < s.length()) {
         int n = 0; //number of []
         String t; char ch;
         while ((ch = s.charAt(i++)) == '[') n++;
         switch (ch) {
         case 'B': t = "byte"; break;
         case 'C': t = "char"; break;
         case 'D': t = "double"; break;
         case 'F': t = "float"; break;
         case 'I': t = "int"; break;
         case 'J': t = "long"; break;
         case 'S': t = "short"; break;
         case 'V': t = "void"; break;
         case 'Z': t = "boolean"; break;
         case 'L': 
            int k = s.indexOf(';',i);
            t = s.substring(i, k);
            int j = t.lastIndexOf('/');
            t = t.substring(j+1);
            i = k+1; break;
         default: t = null;
         }
         while (n-->0) t = t + "[]";
         if (t != null) L.add(t);
      }
      return (String[])L.toArray(new String[L.size()]);
   }

   static class Member { 
      int acc; String name, type, attr;
      Member(int a, String n, String t, String r) { 
        acc = a; name = n; type = t; attr = r; 
      }
      public String toString() { 
         return toString(false);
      }
      public String toString(boolean HTML) {
         String li = "";
         String a = (acc == 0? "" : Modifier.toString(acc)+" ");
         String n = name;
         if (HTML) {
            li = "<LI>";
            if (n.startsWith("<"))
               n = "&lt;"+ n.substring(1, n.length());
            n = "<B>" + n +"</B>";
         }
         String[] s = parseType(type);
         int k = s.length;
         if (!type.startsWith("("))  //Field
             return li + a + s[0] +" "+ n + attr;
         String t = "";  //Method
         if (k > 1) {
             for (int i=0; i<k-2; i++)
                 t += s[i] +", "; 
             t += s[k-2]; 
         }
         return li + a + s[k-1] +" "+ n +"("+ t +")"+ attr;
      }
   }
   static class Constant { 
      int tag; String str;
      Constant(int t, String s) { tag = t; str = s; }
      public String toString() { return str; }
      String setStr(Constant[] cons) { return str; }
   }
   static class Binary extends Constant {
      int c1, c2;  
      Binary(int t, int n1, int n2) { 
         super(t, null); c1 = n1; c2 = n2; 
      }
      String setStr(Constant[] cons) {
         if (str != null) return str;
         str = cons[c1].setStr(cons); 
         if (c2 == 0) return str;
         if (tag == 12) 
            str = "."+str+" "; //separate Name & Type
         str += cons[c2].setStr(cons);
         return str; 
      }
   }

   /** Prints a full description for the class in args[0]. */
   public static void main(String args[]) {
//      System.out.println(new File("").getAbsolutePath());
      String fName = (args.length>0 ? args[0] : "Small");
      ClassSummary s = new ClassSummary(fName+".class");
      System.out.println(s.description(false));
      System.out.println("hasMain = "+s.hasMain());
   }
}
