//Eyler   23.4.2002
//package 6.12.02
//swing   13.4.03
//enhance 15.7.03  default text, rename/move, run/edit
//open    22.7.03
//keys     8.8.03  keyboard shortcuts 
//MVC    29.12.03  simplified, history
//        25.3.12  instance and main() 
//scale   22.3.15
//       30.12.16  use Desktop to open files

package mae.brow;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import mae.sss.InspectorPanel;
import mae.util.PropertyManager;
import mae.util.SimpleFilter;
import mae.util.Console;
import mae.util.Loader;
import mae.util.Scaler;
//import mae.ezvu.EvReader;  //removed V1.66

public class Browser { 

   public static String version = mae.sss.SSS.version();  //"Aug 2012 V1.65";
   public static Browser instance;  //V1.52 (= SSS V1.64)
   PropertyManager pm;
   boolean exit;
   BrowserPanel pan;
   JList root, list;
   File dir, file; 
   String path;
   URL mouseOn;
   History hist;
   HashMap map;
   final static int HIST_SIZE = 50;
   final static String 
      RENAME = "Rename", MOVE = "Move", SAVE = "Save As", 
      REFR = "Refresh", VIEW = "View", FIX = "Fix date",
      RUN = "Run", EDIT = "Edit", OPEN = "Open";
   final static int      //integers for known file types
      TEXT=0, PICT=1, HTML=2, /*EZVU=3,*/ CLASS=3, ZIP=4;  //ezvu removed V1.66
   final static String[] //keys for known file types
      ID = {"text", "pict", "html", /*"ezvu",*/ "class", "zip"};
   final static int[] MAX_SIZE = new int[6];
/*#Format specs: file extensions & max file size
format.text=txt,java,64
format.pict=gif,jpg,png,800
format.html=html,htm,40
format.ezvu=ev,0
format.class=class,0
format.zip=zip,jar,0
*/

   public Browser() { this(".", false); }
   Browser(String d, boolean e) {
      System.out.println("Browser begins "+new Date());
      if (instance == null) instance = this;
      exit = e;
      Frame frm = new Frame(this);
      pan = new BrowserPanel(frm);
      root = pan.root; list = pan.list;
      map = new HashMap(); 
      makeRoot(); makeList(d); 
      hist = new History(HIST_SIZE, dir); //hist.add(dir); 
      loadProps();  //frm.pack(); V1.68
      frm.setVisible(true);
      pan.addListeners(new Ear());
      URL u = null;
      if (exit) InspectorPanel.makeAboutDlg(frm); //V1.66
   }    

   void loadProps() {
      pm = new PropertyManager("mae", "Browser", getClass());
      pan.loadProps(pm);
      for (int i=0; i<ID.length; i++) loadExtensions(i);
   }    
   void saveProps() {
      pan.saveProps(pm);
      pm.save("Browser properties");  //one-line comment
   }    
/* public Fide getEditor() { return Fide.instance; } //removed V1.65
*/ 
   public BrowserPanel getPanel() { return pan; }
   void add(String s, int n) { 
      map.put(s, new Integer(n));
   }
   void loadExtensions(int i) {
      final char COMMA = ',';
      String key = "format."+ID[i];
      String val = pm.getProperty(key);
      if (val == null) return;
      int k = 0;
      int j = val.indexOf(COMMA);
      if (j < 0) return;
      while (j > 0) {
         add(val.substring(k, j), i);
         k = j+1;
         j = val.indexOf(COMMA, k);
      }
      int size = Integer.parseInt(val.substring(k));
      MAX_SIZE[i] = size;
   }
   int indexOf(String s) { 
      Integer n = (Integer)map.get(s);
      if (n == null) return -1;
      else return n.intValue();
   }

   void makeRoot() {
      File[] F = File.listRoots();
      if (F.length > 1) root.setListData(F);
      else {
          F = F[0].listFiles();
          List a = new ArrayList(F.length+1);
          for (int i=0; i<F.length; i++)
              if (!F[i].isHidden() && F[i].isDirectory()) 
                 a.add(F[i].toString());
          Comparator comp = String.CASE_INSENSITIVE_ORDER;
          java.util.Collections.sort(a, comp);
          root.setListData(a.toArray());
      }
   }
   void makeList(String newDir) {
      Object old = null;
      if (newDir.equals("..")) {
         File p = dir.getParentFile();
         if (p != null) dir = p;  
      } else if (newDir.equals(".")) {
         old = list.getSelectedValue();
         dir = (path != null)? new File(path) 
            : new File("").getAbsoluteFile();
      } else if (!newDir.equals("")) {
         dir = new File(path, newDir);
      }
      try {
         path = dir.getCanonicalPath();
      } catch (IOException x) {
         path = dir.getAbsolutePath();
      }
      
      File[] F = dir.listFiles();
      List a = new ArrayList(F.length+1);
      a.add(" [..]");
      int nD = 0, nF = 0;
      for (int i=0; i<F.length; i++)
         if (!F[i].isHidden()) {
            String name = F[i].getName();
            /* if (name.length() > 25)
               name = name.substring(0, 22)+"...";*/
            if (F[i].isDirectory()) {
               a.add(" ["+name+"]"); nD++;
            } else  {
               a.add(name); nF++;
            }
         }
      Comparator comp = String.CASE_INSENSITIVE_ORDER;
      java.util.Collections.sort(a, comp);
      list.setListData(a.toArray());
      pan.setFolderText(path, nD, nF);
      
      if (old != null) {
         list.setSelectedValue(old, true);
      }
      
      if (file!=null && !file.getParent().equals(path))
           pan.move.setText(MOVE);
      else pan.move.setText(RENAME);
      //list.requestFocus(); 
   }
   public void display(URL u) {
      if (u.getProtocol().equals("file") && u.getRef() == null) {
         String s = u.toString();
         char sep = File.separatorChar;
         if (s.charAt(5) == sep) s = s.substring(5); //unix
         else s = s.substring(6).replace('/', sep);  //other
         //System.err.println("setPage "+s);
         display(new File(s)); hist.add(dir); 
      } else {
         pan.displayPage(u); //not file or reference within file
      }
   }
   public void display(File f) {
       if (f.isDirectory()) {
         if (f.equals(dir)) return;
         dir = f; makeList(""); //hist.add(dir); 
      } else /*if (f.isFile())*/ {
         if (f.equals(file)) return;
         display(f.getParentFile());
         list.setSelectedValue(f.getName(), true);
      }
   }
   void display(String name) {
      file = new File(path, name);
      long n = file.length();
      String size = pan.setFileText(file); //, size);
      char Q = '\"';
      String fn = Q+file.getName()+Q;
      String fName = file.getAbsolutePath();
      String ext = SimpleFilter.extension(file); //getExtension(fName);
      int i = indexOf(ext);
      int m = (i < 0)? MAX_SIZE[0] : MAX_SIZE[i];
      boolean large = (m>0 && (n>>10)>m); //n in Kbytes
      String txt = large? null : fileToString(fName);
      if (i < 0) {
         if (!large && isPlainText(txt)) 
              pan.displayText(txt); 
         else pan.showText(Q+ext+Q+" is not supported"); 
      } else if (large) { //throw new RuntimeException
         pan.showText(fn+" is too large: "+size);
      } else try {
         switch (i) {
         case PICT: 
            pan.showPicture(fName); break;
         case TEXT: 
            pan.displayText(txt); break;
         case HTML: 
            pan.displayPage(file.toURL()); break;
         case CLASS: 
            pan.displayClass(fName); break;
/*         case EZVU: 
            EvReader ev = new EvReader(fName);
            pan.displayText(ev.toString());
            pan.open.setText(VIEW); break;
            //pan.showText("open "+fName); 
            //new EasyView(fName); break;
*/
         case ZIP: 
            pan.displayZip(file); break;
         }
      } catch (Throwable x) {
         String msg = x.getMessage();
         if (msg == null) msg = ""+x;
         pan.showText(msg); 
         //x.printStackTrace();
         System.err.println(x);
      }
      pan.save.setEnabled(true);
      pan.move.setEnabled(true);
      pan.move.setText(RENAME);
   }

   static String fileToString(String fName)  {
      return fileToString(new File(fName));
   }
   public static String fileToString(File f)  {
      String msg = "Cannot ";
      try {
         InputStream is = new FileInputStream(f);
         BufferedReader in
           = new BufferedReader(new InputStreamReader(is));
         int n = is.available();
         StringBuffer buf = new StringBuffer(n);
         String t;
         while ((t = in.readLine()) != null) 
            buf.append(t+"\n");
         in.close();
         return new String(buf);
      } catch (FileNotFoundException x) {
         msg += "open "+f;
      } catch (IOException x) {
         msg += "read "+f;
      }
      throw new RuntimeException(msg); //+"\nbecause of "+x);
   }
   static boolean isPlainText(String s)  {
      int n = Math.min(s.length()-1, 200); //last byte can be ^Z
      for (int i=0; i<n; i++) {
          char c = s.charAt(i);
          if (c<32 && c!=9 && c!=10 && c!=13) 
              return false;
      }
      return true;
   }
   
   void refresh(File g) {
      makeRoot(); makeList("."); 
      if (g.getParentFile().equals(dir))
         display(g.getName());
   }
   void doMove() {
      //if (moveD == null) {
         int m = JOptionPane.QUESTION_MESSAGE;
         int y = JOptionPane.YES_NO_OPTION;
         JOptionPane pane = new JOptionPane(null, m, y); //no message
         JDialog dialog = pane.createDialog(pan, "Rename or Move");
         pane.setWantsInput(true);
      //}
      File f = new File(path, file.getName());
      String cmd = pan.move.getText();
      String msg = cmd+"\n"+file+"  to";
      pane.setMessage(msg);
      pane.setInitialSelectionValue(f.toString());
      Scaler.scaleComp(pane); dialog.pack();   //V1.68
      dialog.setVisible(true);  //waits until user action
      dialog.dispose();
      Object res = pane.getValue();
      int k = (res instanceof Integer)?
          ((Integer)res).intValue() : -1;
      if (k != JOptionPane.YES_OPTION) return;
      File g = new File(pane.getInputValue().toString());
      if (file.equals(g)) return;
      if (!file.renameTo(g)) 
         throw new RuntimeException(cmd+" failed");
      System.out.println(cmd+" "+file+" to "+g);
      file = g; pan.setFileText(g);
      refresh(g);
   }
   void doSave() throws IOException {
      //JFileChooser fileD = Scaler.fileChooser();   //V1.68
      File f = new File(path, file.getName());
/*      fileD.setSelectedFile(f); 
      int k = fileD.showSaveDialog(pan);
      if (k != JFileChooser.APPROVE_OPTION) return;
      File g = fileD.getSelectedFile();
      if (g == null) return;
      if (!Console.confirm(g, pan)) return;*/
      File g = Console.fileToSave(f, null);
      String cmd = pan.save.getText();
      copy(file, g); 
      System.out.println(cmd+" "+file+" to "+g);
      refresh(g);
   }
   void doRun() throws ClassNotFoundException {
      JavaSourceHandler h = new JavaSourceHandler();
      if (!h.setTarget(file)) return; 
      h.loadTarget(); h.run(file);
   }
   void doOpen() throws IOException, ClassNotFoundException {
      String cmd = pan.open.getText();
      if (mouseOn != null) {
         pan.displayPage(mouseOn); 
         mouseOn = null;
         pan.htm.setToolTipText(null); 
      } else if (cmd.equals(EDIT)) {
         if (Fide.instance == null) Fide.main();
            Fide.instance.open(file);
      } else  if (cmd.equals(RUN)) {
         System.out.println(cmd+" "+file);
         if (pan.current == pan.htm) doRun();
         else Loader.startJAR(file);
      } else  if (cmd.equals(OPEN)) {
         java.awt.Desktop.getDesktop().open(file);
         System.out.println("Open "+file); 
      }
   }
   static void copy(File f1, File f2) throws IOException {
      InputStream in = new FileInputStream(f1);
      OutputStream out = new FileOutputStream(f2);
      int n1 = (int)f1.length();
      byte[] buf = new byte[n1];
      int n2 = in.read(buf);
      out.write(buf, 0, n2);
      in.close(); out.close();
      f2.setLastModified(f1.lastModified());
   }

   class Ear implements KeyListener, HyperlinkListener, 
         ActionListener, ListSelectionListener  {
      public void keyPressed(KeyEvent e) {
         int c = e.getKeyCode();
         if (c == KeyEvent.VK_F2) 
            pan.move.doClick();
         else if (c == KeyEvent.VK_F5) 
            pan.refr.doClick();
         else if (c == KeyEvent.VK_LEFT) 
            if (e.isControlDown()) pan.rotate(-1);
            else hist.backward();
         else if (c == KeyEvent.VK_RIGHT) 
            if (e.isControlDown()) pan.rotate(1);
            else hist.forward();
         else if (c == KeyEvent.VK_ENTER) 
            pan.open.doClick();
         //list.requestFocus(); 
      }
      public void keyTyped(KeyEvent e) {
         char c = e.getKeyChar();
         if (c == KeyEvent.VK_COMMA) 
            pan.move.doClick();
         else if (c == KeyEvent.VK_BACK_SPACE) 
            hist.backward();
         else if (c == '+') //KeyEvent.VK_PLUS) 
            pan.zoomIn();
         else if (c == KeyEvent.VK_MINUS) 
            pan.zoomOut();
         else if (c == KeyEvent.VK_PERIOD) { 
            if (dir.getParentFile() == null) 
               java.awt.Toolkit.getDefaultToolkit().beep();
            else {
               makeList(".."); hist.add(dir); 
            }
         }
         //list.requestFocus(); 
      }
      public void keyReleased(KeyEvent e) {}
      public void actionPerformed(ActionEvent e) {
         //String cmd = e.getActionCommand();
         Object src = e.getSource();
         try {
            if (src == pan.fix) //V1.67
               pan.doFix(file);
            else if (src == pan.move) 
               doMove();
            else if (src == pan.save) 
               doSave();
            else if (src == pan.open) 
               doOpen();
            else  if (src == pan.refr) 
               refresh(file);
            else if (src == pan.rotL) 
               pan.rotate(-1);
            else if (src == pan.plus) 
               pan.zoomIn();
            else if (src == pan.minus) 
               pan.zoomOut();
            else  if (src == pan.rotR) 
               pan.rotate(+1);
         } catch (Exception x) {
            pan.showText(""+x);
            System.err.println(x);
         }
         if (src != pan.open) list.requestFocus(); 
      }
      public void valueChanged(ListSelectionEvent e) {
         if (e.getValueIsAdjusting()) return;
         JList L = (JList)e.getSource();
         if (L.getSelectedIndex() < 0) return;
         String s = L.getSelectedValue().toString();
         int n = s.length();
         if (L == root) {
            dir = new File(s); makeList(""); 
            hist.add(dir); 
         } else if (s.charAt(0)==' ' && s.charAt(n-1)==']') {
               makeList(s.substring(2, n-1));
               hist.add(dir); 
         } else {
            display(s); hist.add(file); 
            //V1.40 use only folder info in History
            //V1.41 revert (HTML pages need History)
         }
      }
      public void hyperlinkUpdate(HyperlinkEvent e) {
         URL u = e.getURL();
         if (u == null) return;
         boolean local = u.getProtocol().equals("file");
         HyperlinkEvent.EventType t = e.getEventType();
         //System.err.println(t+":  "+u);
         if (t == HyperlinkEvent.EventType.ENTERED) {
            mouseOn = u;
            String tip = local? ""+u : "(Only local URL's supported)";
            pan.htm.setToolTipText(tip); return;
         } else if (t == HyperlinkEvent.EventType.EXITED) { 
            mouseOn = null;
            pan.htm.setToolTipText(null); return;
         } else if (local) display(u); //EventType.ACTIVATED
      }
   }

   class History extends mae.util.History {
      History(int m, File f) { super(m, f); }
      void add(Object f) {
         if (f == null || f.equals(current())) return;
         append(f);
         //pan.save.setText(toString());
         //System.err.println(f+" >"+this);
      }
      protected boolean accept() {
         File f = (File)current();
         if (f == null || !f.exists()) {
             setCurrent(null); return false;
         }
         if (f.equals(dir) || f.equals(file)) return false;
         dir = f.isDirectory()? f : f.getParentFile();
         makeList(""); //display(f);
         if (f.isFile()) //return false;
            list.setSelectedValue(f.getName(), true);
         return true;
      }
   }

   class Frame extends JFrame {
      Frame(Browser b) { 
         super("Browser");
         PropertyManager.setIcon(this, "img/brow.gif");
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      }
      public void dispose() {
         saveProps(); 
         super.dispose();
         if (exit) System.exit(0);
      }    
   }

   public static void main(String[] args) { main(); }
   public static Browser main() { 
       String s = new File("").getAbsolutePath();
       if (instance == null) 
           new Browser(s, JFrame.getFrames().length==0);
       return instance;
   }
}
