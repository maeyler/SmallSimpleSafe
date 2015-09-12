//  InputDialog  1.8.2002  (for EV)
//  ParamDialog  17.11.2002
//V1.0  18.2.2003 use JPanel and simplify error handling 
//V1.56  2.6.2004 use (editable) JComboBox//JTextField

package mae.sss;
import java.util.*;
import java.awt.Font;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.*;
import java.lang.reflect.Array;
import javax.swing.*;
import mae.util.TinyButton;
import mae.util.Scaler;

public class ParamDialog extends JDialog {

   JTextField msg;
   Input[] txt;  //JTextField[] txt;
   JButton ok;
   JPanel bot;
   Class[] typ;
   Object[] arg;
   Class ret;
   String memName, objName;
   boolean returnsObject;
   boolean cancelled;
   Map idObj; //String --> Object
   Ear ear = new Ear();
   final static Class 
      CLASS  = Class.class,
      OBJECT = Object.class,
      STRING = String.class;
   final static int  COLS = 8,
      BASE = Inspector.BASE,
      GAP = InspectorPanel.GAP;
   final static Font 
      FONT = Scaler.scaledFont("SansSerif", 0, 11); //V1.68
   final static Object[] 
      //BOOL = { Boolean.FALSE, Boolean.TRUE };
      BOOL = { "false", "true" };

   public ParamDialog(Frame f, Map m) {
      super(f, true);   //modal
      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      addWindowListener(ear);  //V1.66
      //f == null  in Applet mode
      Toolkit t = Toolkit.getDefaultToolkit();
      Dimension d = t.getScreenSize();
      setLocation(d.width/2-100, d.height/2-100);
      idObj = m; bot = bottom(); //reused in each call
   }
   public void setParams
      (String s, Class[] t, Class r, String id, String[] a) {
      setTitle(s); memName = s; typ = t; ret = r; 
      returnsObject = (!ret.isPrimitive() && ret!=CLASS);
      makeDialog(id, a);
   }
   public void setParams(String s, String id) {
      setTitle("Field "+s); memName = s; 
      typ = Inspector.EMPTY; ret = null; 
      returnsObject = true;
      makeDialog(id, null);
   }
   void makeDialog(String id, String[] a) {
      objName = id; arg = null; cancelled = false; 
      int n = typ.length;
      int k = (returnsObject? n+1 : n);
      txt = new Input[n+1]; //JTextField[n+1];

      JPanel pan = new JPanel();
      pan.setLayout(new BorderLayout(GAP, GAP));
      pan.setBackground(Color.orange);
      pan.setBorder(BorderFactory.createEmptyBorder
         (GAP+2, GAP, GAP, GAP));
         
      //labels must be added before fields
      pan.add(labels(n, k), "West");
      pan.add(fields(n, k), "Center");
      pan.add(bot, "South");  //use the same panel,
      msg.setText("");  //just make sure it is clean
      if (a!=null && a.length==n)
         for (int i=0; i<n; i++) {
            txt[i].setText(a[i]);
            //txt[i].selectAll();
         }
//      addWindowListener(ear);  removed V1.66
      setContentPane(pan);
      //Scaler.scaleComp(pan); //V1.68
      pack(); 
   }
   JPanel labels(int n, int k) {
      JPanel pan = new JPanel();
      pan.setOpaque(false);
      pan.setLayout(new GridLayout(k, 1, GAP, GAP));

      //JTextField t = new JTextField(COLS);
      //txt[n] = t;
      txt[n] = new Input.Text(JTextField.RIGHT);
      txt[n].setText(objName);
      if (returnsObject) {
         Component c = txt[n].component();
         c.setFont(FONT); 
         c.addKeyListener(ear);
         pan.add(c);
      }
      for (int i=0; i<n; i++) {
         String s = typeToName(typ[i]);
         JLabel b = new JLabel(s, JLabel.RIGHT);
         b.setFont(FONT); 
         b.setForeground(Color.black);
         pan.add(b);
      }
      return pan;
   }
   Object[] instancesOf(Class c) {
      List L = new ArrayList(); //of String
      Iterator i = idObj.keySet().iterator();
      while (i.hasNext()) {
         Object x = i.next();
         if (c.isInstance(idObj.get(x))) L.add(x);
      } 
      Collections.sort(L);
      L.add("null");  //null is compatible with any class
      return L.toArray();
   }
   Input makeEdit(Object[] a, String s) {
      if (a.length == 0) 
         return new Input.Text(s);   //no instance
      else return new Input.Edit(a, s);
   }
   Input makeInput(Class c) {
      //primitive types
      if (c == Boolean.TYPE)
         return new Input.Combo(BOOL); //false-true
      if (c == Character.TYPE) 
         return new Input.Text("''");  //char delimiters
      if (c.isPrimitive())
         return new Input.Text("0");   //number 0
      //reference types
      Object[] a = instancesOf(c);
      if (c.isArray()) 
         return makeEdit(a, "{}");   //array delimiters
      if (c == STRING) 
         return makeEdit(a, "\"\""); //String delimiters
      if (c == OBJECT)
         return makeEdit(a, "");     //empty, editable
      else { //any other class -- not editable
         if (a.length == 1)          //just "null"
            //String msg = "no instance, press ESC";
            return new Input.Text("null", false);
         else return new Input.Combo(a);  
      }
   }
   JPanel fields(int n, int k) {
      JPanel pan = new JPanel();
      pan.setOpaque(false);
      pan.setLayout(new GridLayout(k, 1, GAP, GAP));

      if (returnsObject) {
         JLabel b = new JLabel("= "+memName, JLabel.LEFT);
         b.setFont(FONT);            //"Returned object"
         b.setForeground(Color.black); pan.add(b);
      }
      for (int i=0; i<n; i++) {
         txt[i] = makeInput(typ[i]); //new JTextField(COLS);
         txt[i].addKeyListener(ear);
         Component c = txt[i].component();
         c.setFont(FONT); pan.add(c);
      }
      return pan;
   }
   JPanel bottom() {
      JPanel pan = new JPanel();
      pan.setLayout(new BorderLayout(GAP, GAP));
      pan.setOpaque(false);
      msg = new JTextField(3*COLS);
      msg.setFont(FONT); 
      msg.setEditable(false);
      msg.setFocusable(false);  //V1.66
      msg.addKeyListener(ear);
      //msg.addActionListener(ear);
      pan.add(msg, "Center");
      ok = new TinyButton("OK");
      ok.setFont(FONT); 
      ok.setMnemonic('O');
      ok.addActionListener(ear);
      ok.addKeyListener(ear);
      pan.add(ok, "East");
      return pan;
   }
///==============================================================
   static boolean isQuoted(String s, char Q) {
      return isEnclosed(s, Q, Q);
   }
   static boolean isEnclosed(String s, char B, char E) {
      int n = s.length()-1;
      return (s.charAt(0)==B && s.charAt(n)==E);
   }
   Object getArray(Class c, String val) {
      val = val.substring(1, val.length()-1);
      String[] s = parseArgs(val);
      Object a = Array.newInstance(c, s.length);
      for (int i=0; i<s.length; i++) 
         Array.set(a, i, convert(s[i], c));
      return a;
   }
   Object getObject(Class c, String val) {
      if (val.equals("null")) return null; 
      Object x;
      if (isQuoted(val, '"'))   //String
         x = val.substring(1, val.length()-1);
      else if (isEnclosed(val, '{', '}')) //array
         x = getArray(c.getComponentType(), val);
      else x = idObj.get(val);  //other
      if (x == null) throw
         new IllegalArgumentException(val+" undefined");
      String objName = typeToName(x.getClass());
      if (c.isInstance(x)) return x;  //normal exit
      throw new IllegalArgumentException(objName+" wrong type");
   }
  Object convert(String val, Class t) {
      if (!t.isPrimitive()) return getObject(t, val);
      else if (t == Byte.TYPE)    return Byte.valueOf(val);
      else if (t == Short.TYPE)   return Short.valueOf(val);
      else if (t == Integer.TYPE) return Integer.valueOf(val);
      else if (t == Long.TYPE)    return Long.valueOf(val);
      else if (t == Float.TYPE)   return Float.valueOf(val);
      else if (t == Double.TYPE)  return Double.valueOf(val);
      else if (t == Boolean.TYPE) {
         if (val.equalsIgnoreCase("true")) return Boolean.TRUE;
         if (val.equalsIgnoreCase("false"))return Boolean.FALSE;
         throw new IllegalArgumentException("True or False");
      } else if (t == Character.TYPE) {
         char c = (val.length()==3 && isQuoted(val, '\''))?
            val.charAt(1) : (char)Integer.parseInt(val);
         return new Character(c);
      } 
      else return null;
   }
   void checkType(String val, Class t) {
      //accept silently or throw something 
      if (!t.isPrimitive()) getObject(t, val);
      else if (t == Byte.TYPE)    Byte.parseByte(val);
      else if (t == Short.TYPE)   Short.parseShort(val);
      else if (t == Integer.TYPE) Integer.parseInt(val);
      else if (t == Long.TYPE)    Long.parseLong(val);
      else if (t == Float.TYPE)   Float.parseFloat(val);
      else if (t == Double.TYPE)  Double.parseDouble(val);
      else if (t == Boolean.TYPE) {
         if (!val.equalsIgnoreCase("true") &&
             !val.equalsIgnoreCase("false"))
               throw new IllegalArgumentException("");
      } else if (t == Character.TYPE) {
         char c = (val.length()==3 && isQuoted(val, '\''))?
            val.charAt(1) : (char)Integer.parseInt(val);
      } 
   }
   boolean accepts(Class[] p, String[] a) {
      for (int i=0; i<a.length; i++)
         try {
            checkType(a[i], p[i]);
         } catch(Exception x) {
            return false; //reject silently
         }
      return true;
   }
   void checkValidID(String s) {
      if (s==null || s.length()==0) return;
      if (idObj.get(s) != null) throw
         new IllegalArgumentException(s+" is in use");
      int n = s.length();
      if (!Character.isJavaIdentifierStart(s.charAt(0))) 
         throw new IllegalArgumentException(s+" bad objName");
      for (int i=1; i<n; i++) 
         if (!Character.isJavaIdentifierPart(s.charAt(i))) 
            throw new IllegalArgumentException(s+" bad objName");
   }
   boolean argIsValid() {
      int n = typ.length;
      Object[] a = new Object[n];
      int i = n; 
      try {
         if (returnsObject) 
            checkValidID(txt[n].getText());
         for (i=0; i<n; i++) 
            a[i] = convert(txt[i].getText(), typ[i]); 
      } catch (Exception x) {
         //txt[i].selectAll();
         txt[i].focus();  //requestFocus();
         String s = x.getClass().getName();
         s = stripIds(s, 1);
         msg.setText(s+": "+x.getMessage());
//         x.printStackTrace();
         return false;
      }
      arg = a; objName = txt[n].getText();
      return true;
   }
   static boolean isBlank(char c) {
      return (c == ' ' || c == ',');
   }
   static int skipTo(char[] b, int k, char c) {
      while (k < b.length && b[k] != c) k++;
      return k;
   }
   static int skipAfter(char[] b, int k, char c) {
      k = skipTo(b, k+1, c);
      return (k < b.length)? k+1 : k;
   }
   static String[] parseArgs(String s) {
      if (s==null || s.equals("")) return new String[0];
      List L = new ArrayList(); //of String
      char[] b = s.toCharArray(); int k = 0;
      while (k < b.length) {
         int i = k;
         switch (b[k]) {
         case '\'': k = skipAfter(b, k, '\''); break;
         case '"': k = skipAfter(b, k, '"'); break;
         case '{': k = skipAfter(b, k, '}'); break;
         default : k = skipTo(b, i, ',');
            //while (k < b.length && b[k] != ',') k++;
         }
      //System.out.println("  :"+k);
         L.add(s.substring(i, k));
         while (k < b.length && isBlank(b[k])) k++;
      }
      String[] a = new String[L.size()]; 
      return (String[])L.toArray(a);
   }
   static String stripIds(String s, int n) {
      int k = s.length();
      for (int i=0; i<n; i++) 
         k = s.lastIndexOf('.', k-1);
      return s.substring(k+1);
   }
   static String typeToName(Class c) {
      String t = "";
      while (c.isArray()) {
         c = c.getComponentType(); t += "[]";
      }
      return stripIds(c.getName(), 1)+t;
   }
   void accept() {
      if (!isShowing()) return;
      if (argIsValid()) setVisible(false); 
   }
   void cancel() {
      if (!isShowing()) return;
      objName = null; arg = null; 
      cancelled = true; setVisible(false);
   }
   
   class Ear extends WindowAdapter 
      implements KeyListener, ActionListener {
      public void windowActivated(WindowEvent e) {
         if (returnsObject || typ.length>0)
            txt[0].focus(); //requestFocus();
         else ok.requestFocus();
      }
      public void windowClosing(WindowEvent e) {
         cancel();
      }
      public void keyPressed(KeyEvent e) {
         char c = e.getKeyChar();
         //System.out.println(KeyEvent.getKeyText(c));
         if (c == KeyEvent.VK_ESCAPE) {
            cancel(); //setVisible(false); 
         } else if (c == KeyEvent.VK_ENTER) {
            accept();
         }
      }
      public void keyReleased(KeyEvent e) {}
      public void keyTyped(KeyEvent e) {}
      public void actionPerformed(ActionEvent e) {
         Object src = e.getSource(); 
         //String s = e.getActionCommand();
         if (src == ok) accept(); 
      }
   }
}

/*JComboBox newComboBox(int i) {//Deniz Demir
    //Object obj[] = { Boolean.FALSE, Boolean.TRUE };
    JComboBox cb = new JComboBox(BOOL);
    cb.setFont(FONT);
    cb.setEditable(true);
    //cb.setBackground(Color.white);
    JTextField t = txt[i];  //new JTextField(COLS);
    //cb.setSize(t.getPreferredSize());
    //cb.setPreferredSize(t.getPreferredSize());
    ComboBoxEar cbe = new ComboBoxEar(t);
    cb.addItemListener(cbe);
    cb.addKeyListener(ear);
    //t.setFont(font);
    //Boolean bb = (Boolean)cb.getSelectedItem();
    //t.setText(String.valueOf(bb.booleanValue()));
    t.setText(cb.getSelectedItem()+""); 
    //txt[i] = t;
    return cb;
}*/

abstract class Input {

   abstract String getText();
   abstract void setText(String s);
   abstract void focus();
   abstract void addKeyListener(KeyListener e);
   abstract Component component();

   static class Text extends Input {
      JTextField tf = new JTextField(ParamDialog.COLS);
      Text(String s) { setText(s); } //selects initial text
      Text(int align) { tf.setHorizontalAlignment(align); }
      Text(String s, boolean edit) {
         this(s); tf.setEditable(edit);
      }
      String getText() { return tf.getText(); }
      void setText(String s) { tf.setText(s); tf.selectAll(); }
      void focus() { tf.requestFocus(); tf.selectAll(); }
      void addKeyListener(KeyListener e) {
         tf.addKeyListener(e);
      }
      Component component() { return tf; }
   }
   static class Combo extends Input { //Deniz Demir
      JComboBox cb;
      Combo(Object[] a) { cb = new JComboBox(a); }
      String getText() { return cb.getSelectedItem()+""; }
      void setText(String s) { cb.setSelectedItem(s); }
      void focus() { cb.requestFocus(); } 
      void addKeyListener(KeyListener e) {
         cb.addKeyListener(e);
      }
      Component component() { return cb; }
   }
   static class Edit extends Combo { //Afsin Büyüksaraç
      ComboBoxEditor ed;
      Edit(Object[] a, String s) {
         super(a); cb.insertItemAt(s, 0); 
         cb.setSelectedIndex(0); cb.setEditable(true);
         ed = cb.getEditor(); ed.selectAll();
      }
      String getText() { return ed.getItem()+""; }
      void addKeyListener(KeyListener e) {
         ed.getEditorComponent().addKeyListener(e);
      }
   }
}

