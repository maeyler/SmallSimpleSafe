/*
 * Test.java
 *
 * Created on July 23, 2003, 2:05 PM
 */

package mae.util;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import java.lang.reflect.Method;
import javax.swing.*;
import javax.swing.text.JTextComponent;

/** Redirects system input/output to swing components.
 *
 * @author  Eyler
 */
public class Console extends JFrame {
    
    public static Console cons;   //singleton
    final static Move move = new Move();
    final Ear ear = new Ear();
    PropertyManager pm;
    TextAreaPrintStream out;
    DialogInputStream in;
    PrintStream err;
    JButton start;
    JButton reset;
    JButton clear;
    JButton save;
    JCheckBox error;
    JTextArea text;
    JPanel top;
    
    static final Cursor 
      MOVE = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR),
      TEXT = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
    static final String 
        START = "Start", RESET = "Reset", 
        CLEAR = "Clear", SAVE = "Save",
        TIP0 = "Copy and Drag", 
        TIP4 = "Copy/Paste and Drag/Drop",
        ENAB = " are enabled here";
    
    /** Creates new singleton frame and starts redirection */
    private Console() {
        initComponents(); 
        //setTitle("Console");
        PropertyManager.setIcon(this, "console.gif");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        loadProps();
        start(); 
        //setVisible(true);  handled by start()
    }
    
    /** Returns singleton frame, Creates it if null */
    public static Console getInstance() {
        if (cons == null) cons = new Console();
        return cons;
    }
    
    /** Sets console text font */
    public static void setTextFont(Font f) {
        cons.text.setFont(f);
    }
    
    /** Sets message to be used in subsequent read operations. <BR>
     * Default is "Input String is requested"
     */    
    public void setMessage(String s) {
        if (in != null) in.setMessage(s);
    }

    /** Redirects system input/output */
    public void start() {
        if (!closed())
            throw new RuntimeException("Console is already open");
        System.out.println();  //printed on current stream
        System.out.println("System input/output is redirected to Console");
        out = TextAreaPrintStream.redirect(text);
        setButtons(true);  setVisible(true);
        java.awt.Window w = SwingUtilities.getWindowAncestor(text);
        //if you redirect(text), location of dialog depends on scrollers
        in = DialogInputStream.redirect(w);
    }
    
    /** Resets system input/output */
    public void reset() {
        if (closed()) return;
        System.out.println();  //printed on Console
        System.out.println("System input/output is reset");
        if (out != null) out.close();
        if (in != null) in.close();
        out = null; in = null;
        setButtons(false);
    }

    boolean closed() {
        return (out == null && in == null);
    }    

    void setButtons(boolean started) {
        start.setEnabled(!started);
        reset.setEnabled(started);
    }    

    /** Creates new TinyButton */
    public JButton addButton(String s, char c) {
        JButton b = new TinyButton(s);  //JButton(s);
        b.addActionListener(ear);
        b.setMnemonic(c); top.add(b); 
        return b;
    }
    
   /** Saves the contents into a text file */
   public boolean save() {
      JFileChooser fileD = Scaler.fileChooser();   //V1.68
      int k = fileD.showSaveDialog(this);
      if (k != JFileChooser.APPROVE_OPTION) return false;
      File f = fileD.getSelectedFile();
      if (!Console.confirm(f, this)) return false;
      return Console.saveToFile(text.getText(), f);
   }

   /** This method is called from within the constructor to
     * initialize the form.
     */
    void initComponents() {
        java.awt.Container cp = getContentPane();
        
        top = new JPanel();
        top.setOpaque(false);
        start = addButton(START, 'T');
        start.setToolTipText("Redirect System input/output here");
        reset = addButton(RESET, 'T');
        reset.setToolTipText("Reset System input/output");
        clear = addButton(CLEAR, 'C');
        clear.setToolTipText("Clear console text");
        save = addButton(SAVE, 'S');
        save.setToolTipText("Save console text to file");
        error = new JCheckBox("Capture err", false);
        error.setMnemonic('E');
        error.setOpaque(false);
        error.setFont(save.getFont());
        String tip = "Capture System.err, as well as System.out";
        error.setToolTipText(tip); 
        error.addItemListener(ear);
        top.add(error);
        cp.add(top, BorderLayout.NORTH);

        text = new JTextArea("", 25, 60);
//        text.setFont(new Font("Monospaced", 0, 12));
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        if (setDragEnabled(text)) setDragFeedback(text);
        JScrollPane pan = new JScrollPane(text);
        cp.add(pan, BorderLayout.CENTER);
        Scaler.scaleComp(top);  //pack();  
    }

    void loadProps() {
        Dimension t = Toolkit.getDefaultToolkit().getScreenSize();
        int W = Scaler.scaledInt(500), H = Scaler.scaledInt(500);
        int x = t.width-W, y = t.height-H-25;
        pm = new PropertyManager("mae", "Console", getClass());
        setTitle(pm.getProperty("title", "Console"));
        setBounds(pm.getBounds("frame", 0, y, W, H));
        Color c = pm.getColor("color", Color.orange);
        getContentPane().setBackground(c);
        Font f = Scaler.scaledFont("Monospaced", 0, 13);
        //System.err.println(f);
        Font g = pm.getFont("font", f);
        text.setFont(g);
        System.err.println(g);
    }    

    void saveProps() {
        pm.setProperty("title", getTitle());
        pm.setBounds("frame", getBounds());
        Color c = getContentPane().getBackground();
        pm.setColor("color", c);
        pm.setFont("font", text.getFont());
        pm.setProperty("saved.on", ""+new Date());  //not used
        pm.save("Console properties");  //one-line comment
    }    

    public void dispose() {
        reset(); 
        saveProps(); 
        super.dispose();
    }    

    static class Move implements MouseMotionListener {
         public void mouseDragged(MouseEvent e) { 
         }
         public void mouseMoved(MouseEvent e) {
            Object s = e.getSource();
            if (s instanceof JTextComponent) {
               JTextComponent src = (JTextComponent)s;
               int p = src.viewToModel(e.getPoint());
               int i = src.getSelectionStart();
               int j = src.getSelectionEnd();
               //System.out.println(i+" "+p+" "+j);
               src.setCursor((i<=p && p<j)? MOVE : TEXT);
            }
         }
    }    

    class Ear implements ActionListener, ItemListener {
        public void actionPerformed(ActionEvent evt) {
            String cmd = evt.getActionCommand();
            if (cmd.equals(START)) start();
            else if (cmd.equals(RESET)) reset();
            else if (cmd.equals(CLEAR)) text.setText("");
            else if (cmd.equals(SAVE)) save();
        }
        public void itemStateChanged(ItemEvent e) {
           if (e.getSource() != error) return;
           if (err == null) {
              err = System.err; System.setErr(out);
           } else {
              System.setErr(err); err = null;
           }
        }
    }
    
   public static boolean confirm(File f, Component parent) {
      if (f == null || !f.exists()) return true;  
      String msg = f+" exists.\n"
            +"Do you want to replace it?";
      String title = "File already exists";
      int opt = JOptionPane.YES_NO_OPTION;
      int typ = JOptionPane.QUESTION_MESSAGE;
      String[] but = {"Replace", "Cancel"};
      JOptionPane pane = new JOptionPane(msg, typ, opt, null, but);
      Scaler.scaleComp(pane);
      JDialog dialog = pane.createDialog(parent, title);
      dialog.show(); //modal
      dialog.dispose();
      Object reply = pane.getValue();
      return (reply == but[0]);
      /*int reply = JOptionPane.showOptionDialog(
         cmp, msg, title, opt, typ, null, but, null
      );
//System.err.println("JOptionPane: "+reply);
      return (reply == JOptionPane.YES_OPTION);*/
   }
   public static boolean saveToFile(String s, File f) {
      try {
         BufferedReader in
           = new BufferedReader(new StringReader(s));
         PrintWriter out 
           = new PrintWriter(new FileWriter(f));
         String t;
         while ((t=in.readLine()) != null) out.println(t);
         out.flush(); out.close(); return true;
      } catch (IOException x) {
         System.err.println(x.getMessage());
         throw new RuntimeException("Cannot write to "+f);
      }
   }
   public static void setDragFeedback(JTextComponent c) {
      c.addMouseMotionListener(move);
      if (c.isEditable()) c.setToolTipText(TIP4 + ENAB);
   }
   public static boolean setDragEnabled(JComponent c) {
      try {
         //Class[] b = {Boolean.TYPE};  
         //getMethod() will fail in JVM 1.3
         Method m = c.getClass().getMethod("setDragEnabled", Boolean.TYPE); //b);
         //Boolean[] t = {Boolean.TRUE};
         m.invoke(c, Boolean.TRUE);  //t);  
         //c.setToolTipText(TIP0 + ENAB);  removed Mar 2009
         return true;
      } catch (Exception x) {
         String s = "setDragEnabled: "+x.getMessage();
         System.err.println(s); 
         return false;
      }
   }
   public static boolean isRunning() {
      return (cons != null);
   }
 
   /**
    * Reads a line from System-in, without any Reader object
    */
    public static String nextLine() {
        String s = "";
        try {
            int c = System.in.read();
            while (c>0 && c!=10 && c!=13) {
                s = s + (char)c;
                c = System.in.read();
            }
            if (System.in.available() == 1)
                System.in.read();  //skip \n if any
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return s;
    }
    
    /** Starts an instance */
    public static void main(String[] args) throws Exception {
        getInstance();
        cons.setMessage("How are you?");
        //nextLine();
        Object s = Toolkit.getDefaultToolkit().getSystemClipboard()
          .getData(java.awt.datatransfer.DataFlavor.stringFlavor);
        System.out.println(s);
        System.out.println("Test complete");
    }
}
