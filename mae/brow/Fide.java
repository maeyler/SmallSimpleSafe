// Author: Eyler -- 21.9.2002 
// 22.3.03  add-in for SSS
// 18.7.03  add-in for Browser
// 20.7.03  handler: separate editor and compiler
// 22.7.03  swing components, confirm dialog
// 22.7.04  actions: Compile, Run, Undo, Redo
// 16.8.04  UndoManager, LineNumberPane 
// 25.3.12  V1.52 instance and main() 
// 26.8.12  V1.53 Open Previous
// 22.3.15  scale  

package mae.brow;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import mae.sss.InspectorPanel;
import mae.util.PropertyManager;
import mae.util.Console;
import mae.util.SourceHandler;
import mae.util.SimpleFilter;
import mae.util.UndoManager;
import mae.util.LineNumberPane;
import mae.util.Reporter; //V1.65
import mae.util.Scaler;   //V1.68

public class Fide extends JPanel implements mae.util.Editor {
   
   public static Fide instance;  //V1.64
   PropertyManager pm;
   boolean exit;
   SourceHandler handler;
   String text;  //, searchStr; local
   String TAB;   //replaces TAB key
   File file, prevF;
   int prevS, prevE;
   JMenu recent, trans;
   JFrame frm;
   final JTextArea src = new JTextArea();
   final JTextField search = new JTextField(15);
   final JTextField msg = new JTextField();
   final JMenuBar bar = new JMenuBar(); 
   final JToolBar tool = new JToolBar();
   final UndoManager undoMgr = new UndoManager(src);
   final Action undo = undoMgr.getUndoAction();
   final Action redo = undoMgr.getRedoAction();
   final Act comp = new Act(COMP);  
   final Act exec = new Act(RUN);
   final Act stop = new Act(STOP);
   final Act again = new Act(AGAIN);
   final Ear ear = new Ear();
   String filter;
   //final Map filters = new HashMap();

   public final static int 
      GAP = Scaler.scaledInt(4),  //used in BorderLayout
      MAX_ITEMS = 15,  //items in recent  10->15  V1.65
      MAX_SPACE = 16;  //blanks in TAB
   final static String EMPTY 
      = "class XXX {\n//Enter java program\n"
      + "    public static void main(String[] args) {\n"
      + "    }\n}";
   public final static Font MENU = Scaler.scaledFont("Dialog", 1, 12);
   public final static Font ITEM = Scaler.scaledFont("SansSerif", 0, 11);
   public final static Font ttypeS = Scaler.scaledFont("MonoSpaced", 0, 13);
   public final static String TITLE = " - Fide",  //V1.68
      NEW = "New", OPEN = "Open", PREV = "Open Previous", 
      SAVE = "Save", SAVEAS = "Save As...", STOP = "Stop",
      UNDO = "Undo", REDO = "Redo", TAB_SIZE = "Set TAB Size",
      FIND = "Find Selection", AGAIN = "Find Next", GOTO = "Go to Line",
      COMP = "Compile", RUN = "Run", ABOUT = "About", QUIT = "Quit";

   Fide() {
      System.out.println("Fide begins "+new Date());
      if (instance == null) instance = this;
      exit = (JFrame.getFrames().length == 0);
      UIManager.put("ToolTip.font", ITEM);  //V1.68

      setLayout(new BorderLayout(GAP, GAP));      
      src.setFont(ttypeS);
      if (Console.setDragEnabled(src))
          Console.setDragFeedback(src);
      src.setToolTipText(null); 
      src.setColumns(80); src.setRows(25);
      add(new JScrollPane(src), "Center");
      LineNumberPane.addLineNumbers(src); //after adding src
      src.getInputMap().put(
         KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), new Tab()
      );
      //src.getActionMap().put(TAB, new Tab());
      Object brk = src.getInputMap().get(
         KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)
      );
      Enter enter = new Enter(src.getActionMap().get(brk));
      //System.out.println("New: "+enter+"\nOld: "+enter.old);
      if (enter.old != null) 
         src.getActionMap().put(brk, enter);
      search.setAction(again);
      search.setFont(ITEM);
      
      msg.setEditable(false);
      add(msg, "South");
      
      frm = new JFrame("Fide"); 
      frm.addWindowListener(ear);
      frm.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      frm.setJMenuBar(setupMenus());
      frm.setContentPane(this); 
      Scaler.scaleComp(this);
      loadProps();  //frm.pack();  
      URL u = getClass().getResource("img/fide.gif");
      if (u != null) PropertyManager.setIcon(frm, u);
      //frm.setVisible(true); in setSource()
      if (exit) InspectorPanel.makeAboutDlg(frm);
}
///==============================================================
   public java.awt.Frame getFrame() { return frm; }
   public PropertyManager propertyManager() { return pm; }
   public String getText() { return src.getText(); }
   public void select(int i, int j) { src.select(i, j); }
   public void setMessage(String s) {
      msg.setText(s);
      System.out.println(s);
   }
   public void setMessage(Throwable x) {
      setMessage(x.getClass().getName()+": " + x.getMessage());
      Reporter.append(x); //V1.65
   }
   public void setMessage(String s, boolean OK) {
      setMessage(s);
      exec.setEnabled(OK);
   }
   public void startRunning() {
      Console.getInstance().setVisible(true);
      setMessage("Running", false);
      stop.setEnabled(true);
   }
   public void stopRunning(String s) {
      setMessage(s, true);
      stop.setEnabled(false);
   }
   public SourceHandler getHandler() {
      return handler;
   }
   public void compile()  {
      if (handler == null) return;
      if (handler.requiresSave()) save(); 
      handler.compile();
   }
   public void run() {
      if (handler == null) return;
      if (!src.getText().equals(text)) compile();  
      handler.run();
   }
   public void stop() {
      if (handler == null) return;
      handler.stop();
   }
   public void open() {
      confirmedSave();
      if (file != null) setFileFilter();
      File f = Console.fileToOpen(file, filter);
      if (f == null || f.equals(file)) return;
      open(f);
   }
   public void open(File f) {  //called from another class
      confirmedSave();
      setSource(Browser.fileToString(f), f);
      src.select(0, 0);
   }
   public void openPrev() { //V1.65
      int S = prevS;
      int E = prevE;
      if (prevF == null) empty();
      else {
          open(prevF);
          src.select(S, E);
      }
   }
   public void empty() {
      confirmedSave();
      setSource(EMPTY, null);
      src.select(6, 9);
   }
   void addToRecent(File f) {
      String s = ""+f;
      for (int i=0; i<recent.getItemCount(); i++) 
         if (recent.getItem(i).getText().equals(s))
            recent.remove(i);
      JMenuItem mi = newItem(s);
      mi.setFont(ITEM);
      recent.add(mi, 0);
      if (recent.getItemCount() > MAX_ITEMS)
         recent.remove(MAX_ITEMS);
   }
   void setSource(String t, File f) {
      prevF = file;
      text = t; 
      file = f; 
      prevS = src.getSelectionStart();
      prevE = src.getSelectionEnd();
      String s = (f == null)? "[new file]" : f.getName(); 
      if (f != null) addToRecent(f);
      handler = SourceHandler.newHandler(f, this);
      if (trans != null) {
         bar.remove(trans); bar.repaint();
      }
      trans = (handler == null)? null : handler.menu();
      if (trans != null) {
         bar.add(trans, 3); //bar.updateUI();
      }
      boolean OK = (handler != null);
      boolean C = OK && handler.canCompile();
      comp.setEnabled(C);
      boolean run = C && handler.readyToRun();
      setMessage(s+" opened", run);
      src.setText(t); 
      undoMgr.discardAllEdits(); 
      stop.setEnabled(false);
      frm.setTitle(s + TITLE);
      frm.setVisible(true);
      src.requestFocus(); 
   }
   void confirmedSave() {
      String s = src.getText();
      if (text == null || s.equals(text)) return;  
      String title = "Text is modified";
      String msg = "Do you want to save";
      if (file != null) msg += "\n"+file;
      int typ = JOptionPane.QUESTION_MESSAGE;
      int opt = JOptionPane.YES_NO_CANCEL_OPTION;
      String[] but = {"Save", "Discard", "Cancel"};
      //int reply = JOptionPane.showOptionDialog(...
      JOptionPane pane = new JOptionPane(msg, typ, opt, null, but);
      Scaler.scaleComp(pane);
      JDialog dialog = pane.createDialog(this,title);
      dialog.show(); //modal
      dialog.dispose();
      Object reply = pane.getValue();
      //System.err.println("JOptionPane: "+reply);
      if (reply == but[0]) save();
      else if (reply != but[1]) //cancel
         throw new RuntimeException("action cancelled");
   }
   public void save() {
      String s = src.getText();
      if (text == null || s.equals(text)) return;  
      if (file == null) saveAs();
      else  {
         Console.saveToFile(src.getText(), file);
         text = s;
      }
   }
   public void saveAs() { 
      String s = src.getText();
      if (s.equals("")) return;
      if (file == null) {
         //setFileFilter("java");
         filter = "*.java";
      } else {
         //setFileFilter();
         filter = file.getName();
      }
      File f = Console.fileToSave(file, filter);
      if (f == null) return;
      Console.saveToFile(s, f);
      if (!s.equals(text) || !f.equals(file))
         setSource(s, f);
      System.out.println(s.length()+" bytes saved");
   }
   void setFileFilter() { 
      filter = "*."+SimpleFilter.extension(file);
   }

   /** Shows Find dialog and searches for the string entered */
   public void find() {
       String t = src.getSelectedText();
       if (t == null || t.length() > 30) {
           search.selectAll(); search.requestFocus(); 
       }
       else {
           search.setText(t); doSearch(); src.requestFocus(); 
       }
   }

   /** Searches for the current search string  */
   public void doSearch() {
       String searchStr = search.getText();
       //if (searchStr == null) find();
       if (searchStr.equals("")) return;
       int k = src.getSelectionEnd();
       k = src.getText().indexOf(searchStr, k);
       if (k >= 0) src.select(k, k + searchStr.length());
       else getToolkit().beep();
   }

    public static String showInputDialog(Component parent, String msg, String initial) {
        String title = "Input";
        int typ = JOptionPane.QUESTION_MESSAGE;
        int opt = JOptionPane.OK_CANCEL_OPTION;
        JOptionPane pane = new JOptionPane(msg, typ, opt);
        pane.setWantsInput(true);
        pane.setInitialSelectionValue(initial);
        pane.selectInitialValue();
        Scaler.scaleComp(pane);
        JDialog dialog = pane.createDialog(parent, title);
        dialog.show(); //modal
        dialog.dispose();
        Object value = pane.getInputValue();
        //System.err.println("JOptionPane: "+value);
        return (value == JOptionPane.UNINITIALIZED_VALUE? null : value.toString());
    }

   /** Sets selection to a given line */
   public void goTo() {
       String s = showInputDialog(this, GOTO, "");
       if (s == null) return;
       int k = 0;
       try {
           k = Integer.parseInt(s);
           int i = src.getLineStartOffset(k-1);
           src.select(i, i);
       } catch (NumberFormatException e) {
           setMessage(e);
       } catch (BadLocationException e) {
           setMessage("Line "+k+" not found");
       }
   }

   /** Sets TAB size, after asking the user */
   public void setTAB() {
       String t = ""+TAB.length(); //Initial Selection Value
       String s = showInputDialog(this, TAB_SIZE, t);
       if (s == null || s.equals(t)) return;
       try {
           setTAB(Integer.parseInt(s));
       } catch (NumberFormatException e) {
           setMessage(e); 
       }
   }
   /** Sets TAB to n blanks */
   void setTAB(int n) {
        TAB = "";
        if (0 <= n && n >= MAX_SPACE) n = MAX_SPACE;
        for (int i=0; i<n; i++) TAB += " ";
   }

    void loadProps() {
        Dimension t = getToolkit().getScreenSize();
        int W = Scaler.scaledInt(500), H = Scaler.scaledInt(500);
        int x = t.width-W, y = t.height-H-25;
        pm = new PropertyManager(mae.sss.SSS.PREFS, "Fide", getClass());
        frm.setBounds(pm.getBounds("frame", x, y, W, H));
        Font f = pm.getFont("font", ttypeS);
        src.setFont(f); 
        //System.err.printf("%s %s%n", f, f.getSize2D());
        setTAB(pm.getInteger("tab.size", 4));
        recent.removeAll();
        for (int i=0; i<MAX_ITEMS; i++) {
           String s = pm.getProperty("recent."+i);
           if (s == null) continue;
           JMenuItem mi = newItem(s);
           mi.setFont(ITEM);
           recent.add(mi);
        }
    }    
    void saveProps() {
        pm.setBounds("frame", frm.getBounds());
        pm.setFont("font", src.getFont());
        pm.setProperty("tab.size", ""+TAB.length());
        int n = recent.getItemCount();
        for (int i=0; i<n && i<MAX_ITEMS; i++) {
           String s = recent.getItem(i).getText();
           pm.setProperty("recent."+i, s);
        }
        pm.save("Fide properties");  //one-line comment
    }    

///==============================================================
   class Tab extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         int k = src.getSelectionStart();
         int p = src.getSelectionEnd();
         if (k != p) getToolkit().beep();
         else try {
            String t = (TAB.length() == 0)? "\t" : TAB;
            src.getDocument().insertString(p, t, null);
         } catch (BadLocationException x) {
            setMessage(x);
         }
      }
   }
   class Enter extends AbstractAction {
      Action old;
      Enter(Action a) { old = a; }
      public void actionPerformed(ActionEvent e) {
         old.actionPerformed(e);
         Document doc = src.getDocument();
         int p = src.getSelectionEnd();
         try {
            int k = src.getLineOfOffset(p);
            int i = src.getLineStartOffset(k-1);
            int len = Math.min(MAX_SPACE, p-i);
            String t = doc.getText(i, len);
            int j = 0; String s = ""; 
            //for (int j=0; j<len && t.charAt(j)==' '; j++);
            while (j<len && t.charAt(j++)==' ') s += " ";
            if (s.length() > 0)
               doc.insertString(p, s, null);
            //setMessage(p+" "+k+" "+len);
         } catch (BadLocationException x) {
            setMessage(x);
         }
      }
   }
   class Act extends AbstractAction {
      public Act(String s) { super(s); }
      public void setName(String s) { putValue(NAME, s); }
      public void setDesc(String s) {
         putValue(Action.SHORT_DESCRIPTION, s); 
      }
      public void actionPerformed(ActionEvent e) {
         msg.setText("");
         String cmd = (String)getValue(NAME);
//         String s = e.getActionCommand();
//         System.out.println(s+" Action: "+cmd);
         try {
             if (cmd.equals(QUIT)) ear.windowClosing(null);
             else if (cmd.equals(COMP)) compile();
             else if (cmd.equals(RUN)) run();
             else if (cmd.equals(STOP)) stop();
             //else if (cmd.equals(UNDO)) undoMgr.undo();
             //else if (cmd.equals(REDO)) undoMgr.redo();
             else if (cmd.equals(FIND)) find();
             else if (cmd.equals(AGAIN)) doSearch();
             else if (cmd.equals(GOTO)) goTo();
             else if (cmd.equals(TAB_SIZE)) setTAB();
             else if (cmd.equals(NEW)) empty();
             else if (cmd.equals(PREV)) openPrev();
             else if (cmd.equals(OPEN)) open();
             else if (cmd.equals(SAVE)) save();
             else if (cmd.equals(SAVEAS)) saveAs();
             else if (cmd.equals(ABOUT)) InspectorPanel.showAboutDlg();  //V1.66
             else open(new File(cmd));  //must be recent
         } catch (Throwable x)  {
            setMessage(x);
         }
         if (!cmd.equals(FIND)) src.requestFocus(); 
      }
   }
   class Ear extends WindowAdapter { 
      public void windowClosing(WindowEvent e) { 
         try {
            confirmedSave(); //may cancel
            saveProps(); 
            frm.dispose();
            if (exit) System.exit(0);
         } catch (Exception x) {
            setMessage(x);
         }
      }
   }
///==============================================================
   JMenu newMenu(String s, char c) {
      JMenu f = new JMenu(s);
      f.setFont(MENU);
      f.setMnemonic(c);
      return f;
   }
   JMenuItem newItem(String s) {
      return newItem(s, (char)0);
   }
   JMenuItem newItem(String s, char c) {
      int m = ActionEvent.CTRL_MASK;
      return newItem(new Act(s), m, c);
   }
   JMenuItem newItem(Action a, int mask, char c) {
      JMenuItem mi = new JMenuItem(a);
      mi.setFont(MENU);
      if (c > 0) {
         mi.setMnemonic(c);
         mi.setAccelerator(KeyStroke.getKeyStroke(c, mask));
      } //do not addActionListener
      String name = (String)a.getValue(Action.NAME);
      String s = "img/"+name+".gif";
      URL u = getClass().getResource(s);
      if (u != null) {
         Icon icn = new ImageIcon(u);
         a.putValue(Action.SMALL_ICON, icn);
         a.putValue(Action.SHORT_DESCRIPTION, name);
         JButton but = tool.add(a);
         if (name.equals(FIND)) tool.add(search);
         but.setText("");
      }
      return mi;
   }
   JMenuBar setupMenus()  {
      JMenu f = newMenu("File", 'F');
      f.add(newItem(NEW,  'N')); 
      f.add(newItem(OPEN, 'O')); 
      f.add(newItem(PREV, 'P')); 
      recent = newMenu("Open Recent", 'T');
      f.add(recent);
      f.addSeparator();
      f.add(newItem(SAVE, 'S'));
      f.add(newItem(SAVEAS)); 
      f.addSeparator();
      f.add(newItem(QUIT, 'Q'));
      bar.add(f);
      tool.addSeparator();
      
      JMenu e = newMenu("Edit", 'E');
      e.add(newItem(undo, ActionEvent.CTRL_MASK, 'Z')); 
      e.add(newItem(redo, ActionEvent.CTRL_MASK, 'Y')); 
      e.addSeparator();
      e.add(newItem(FIND, 'F')); 
      JMenuItem i = newItem(AGAIN, 'N');
      i.setAccelerator(
         KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0)
      );
      e.add(i); 
      e.addSeparator();
      e.add(newItem(GOTO, 'L'));
      e.add(newItem(TAB_SIZE));
      bar.add(e);
      tool.addSeparator();
      
      JMenu p = newMenu("Program", 'P');
      p.add(newItem(comp, ActionEvent.ALT_MASK, 'C')); 
      p.add(newItem(exec, ActionEvent.ALT_MASK, 'R')); 
      p.add(newItem(stop, 0, (char)0)); 
      p.addSeparator();
      p.add(newItem(ABOUT));  //V1.66
      bar.add(p);
      
      tool.setFloatable(false);
      tool.addSeparator();
      bar.add(tool);
      
      Dimension dim = new Dimension(GAP, 0);
      bar.add(Box.createRigidArea(dim));
      JLabel lab = new JLabel(mae.sss.SSS.version());
      lab.setForeground(BrowserPanel.verColor);
      lab.setFont(BrowserPanel.verFont);
      bar.add(lab);
      bar.add(Box.createRigidArea(dim));
      return bar;
   }
   static Fide start(File f) {
      //if (!VersionChecker.accept("Fide", "1.3")) return null;  removed V1.65
      Fide d = new Fide();
      Console.getInstance();  
      if (f == null) d.empty(); 
      else d.open(f);
      return d;
   }
   public static Fide main() { 
       if (instance == null) start(null); 
       return instance;
   }
   public static void main(String[] args) {
      if (args.length == 0) start(null);
      else start(new File(args[0]));
   }
}
