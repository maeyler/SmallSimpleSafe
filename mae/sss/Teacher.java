// Author: Eyler -- 1/11/2003

package mae.sss;
import java.io.*;
import java.util.*;
import java.lang.reflect.Method;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mae.util.TinyButton;
import mae.util.Reporter;

public class Teacher implements Runnable {
   
   final static String NAME = "SSS.teacher";
   final static boolean exit = (JFrame.getFrames().length == 0);
   final static Dialog D = new Teacher.Dialog();
   final static int UNIT = 500, MIN = 1, MAX = 30, NORM = 6;
   final List com = new ArrayList(); //comment
   final List res = new ArrayList(); //result
   final List cmd = new ArrayList(); //command
   String name; int time; //InputStream in;
   Inspector ins;  //ParamDialog dlg;
   boolean stopped; Thread thd;
   
   public static void start(String s) {
      Teacher t = new Teacher(s);
      t.start(new Thread(t, s));
   }
   public static void start(File f) {
      Teacher t = new Teacher(f);
      t.start(new Thread(t, t.name));
   }

   public Teacher() { this(NAME); }
   Teacher(String s) {
      name = s;
      InputStream in = getClass().getResourceAsStream(s);
      if (in != null) readData(in);
      else readData(new File(s));
   }
   Teacher(File f) {
      name = f.getName(); readData(f);
   }
   void readData(File f) {
      try {
         readData(new FileInputStream(f));
      } catch(Exception x) {
         System.out.println(x);  //x.printStackTrace();
      }  //throw new RuntimeException("Cannot open "+f);
   }
   void readData(InputStream str) {
      ins = Inspector.ins; D.setTitle(name); 
      if (D.slid.getValue() == 0) D.slid.setValue(NORM);
      time = D.slid.getValue() * UNIT;
      try {
         Reader rdr = new InputStreamReader(str);
         BufferedReader in = new BufferedReader(rdr);
         String t, r, s;
         while ((s = in.readLine()) != null) {
            if (s.equals("")) continue;
            t = ""; r = "";
            while (s != null && s.startsWith("//")) {
               if (s.length() > 2 && s.charAt(2) == '+')
                    r += s.substring(3)+"\n";
               else t += s.substring(2)+"\n";
               s = in.readLine();
            }
            com.add(t); res.add(t+r); cmd.add(s); 
         }
         in.close();
         new Thread(new Aid()).start();
         //D.setVisible(true); //calling Thread is 
         //stuck here when D is modal
      } catch(IOException x) {
         System.out.println(x);
      }
   }
   public void start(Thread d) {
      thd = d; d.start(); 
   }
   public void wakeup() {
      thd.interrupt(); 
      if (ins.dlg.cancelled) D.dispose();
   }
   public void run() {
      D.teach = this; 
      System.out.println(">> Begin "+name);
      ins.clearAll();
      stopped = false;
      for (int i=0; i<com.size(); i++) 
         try {
            if (stopped) break; 
            process(i);
         } catch(Exception x) {
            System.err.println(x);
            if (!ins.demo) Reporter.append(x);
         }
      D.setVisible(false);
      System.out.println("<< End "+name);
      D.teach = null; 
      if (exit) System.exit(0);
   }
   public void process(int i) {
      SwingUtilities.invokeLater(new Aid(D.com, com.get(i)));
      //D.com.setText((String)com.get(i));
      String s = (String)cmd.get(i);
      if (s.startsWith("interface ")) {
         SwingUtilities.invokeLater(new Aid(s.substring(10)));
         //ins.inspectClass(s.substring(10)); D.cmd.setText("");
         delay(time);
      } else if (s.startsWith("class ")) {
         SwingUtilities.invokeLater(new Aid(s.substring(6)));
         //ins.inspectClass(s.substring(6)); D.cmd.setText("");
         delay(time);
      } else { //Field, Method, or Constructor
         processMember(s);
         delay(time*3); //wait for param dialog
         if (ins.dlg.isShowing()) //ins.dlg.accept();
            SwingUtilities.invokeLater(new Aid(ins.dlg));
         delay(40); //brief delay after dlg is accepted
         for (int j=0; j<50; j++) //poll for 50 second
            if (modalDialogIsShown())
               delay(1000); //poll once a second
      }
      SwingUtilities.invokeLater(new Aid(D.com, res.get(i)));
      //D.com.setText((String)res.get(i)); //show result
      SwingUtilities.invokeLater(new Aid(D.cmd, s));
      //D.cmd.setText(s); 
      delay(time);
   }
   boolean modalDialogIsShown() {
		FocusManager fm = FocusManager.getCurrentManager();
		//if version >= 1.4 use Window w = fm.getFocusedWindow();
		try {
	   		Class c = FocusManager.class;
			//final Class[] EMPTY = {};  //removed V1.65
			Method m = c.getMethod("getFocusedWindow"); //, EMPTY);
			Window w = (Window)m.invoke(fm);  //, EMPTY); 
			if (w instanceof JDialog)
				return (w != D) && ((JDialog) w).isModal();
			else throw new RuntimeException();
		} catch (Exception x) {
			return Chooser.fileD != null && Chooser.fileD.isShowing();
		}
	}
   void processMember(String s) {
      String stat = substrUpTo(s, ';');
      SwingUtilities.invokeLater(new Aid(D.cmd, stat));
      //D.cmd.setText(stat); 
      String id = substrUpTo(stat, ' '); //space before '='
      String met = substrAfter(stat, "=");
      if (met == null) return;
      boolean isField = false;
      String obj, nam; 
      if (met.startsWith("new ")) { //Constructor
         obj = substrBetween(met, "new ", '(');
         nam = obj; 
      } else if (met.indexOf('(') < 0) { //Field;
         obj = substrUpTo(met, '.'); 
         nam = substrAfter(met, ".");
         isField = true;
      } else { //Method
         obj = substrUpTo(met, '.'); 
         nam = substrBetween(met, ".", '(');
      }
      SwingUtilities.invokeLater(new Aid(obj, obj));
      //ins.selectByName(obj); //SELECT OBJECT OR CLASS
      String arg; String[] A;
      if (isField) {
         arg = null; A = null;
      } else {
         arg = substrBetween(met, "(", ')');
         A = ParamDialog.parseArgs(arg);
      }
      SwingUtilities.invokeLater(new Aid(nam, id, A));
      //ins.selectMethod(nam, id, arg); not in this Thread
   }
   static String substrBetween(String s, String b, char c) {
      return substrUpTo(substrAfter(s, b), c);
   }
   static String substrAfter(String s, String c) {
      if (s == null || c == null) return null;
      int k = s.indexOf(c) + c.length();
      if (k == 0) return s;
      while (k<s.length() && s.charAt(k)==' ') k++;
      if (k == s.length()) return s;
      return s.substring(k);
   }
   static String substrUpTo(String s, char c) {
      if (s == null) return null;
      int k = s.indexOf(c);
      if (k < 0) return null;
      return s.substring(0, k);
   }
   public void delay(long d) {
      try {
         Thread.sleep(d);
      } catch(InterruptedException x) {}
   }
   
   class Aid implements Runnable {
      int kind; String name, str; 
      String[] args; JTextComponent txt;
      Aid() { kind = 0; }
      Aid(String n) { name = n; kind = 1; }
      Aid(String n, String s, String[] a) {
         name = n; str = s; args = a;
         kind = (a == null)? 2 : 3; 
      }
      Aid(String n, String s) {
         name = n; str = s; kind = 4; 
      }
      Aid(JTextComponent t, Object s) {
         txt = t; str = ""+s; kind = 5; 
      }
      Aid(ParamDialog p) { kind = 6; }
      public void run() {
         switch (kind) {
         case 0: D.setVisible(true); break;
         case 1: 
            ins.inspectClass(name); 
            D.cmd.setText(""); break;
         case 2: 
            ins.selectField(name, str); 
            wakeup(); break;
         case 3: 
            ins.selectMethod(name, str, args); 
            wakeup(); break;
         case 4: ins.selectByName(name); break;
         case 5: txt.setText(str); break;
         case 6: ins.dlg.accept(); break;
         default :
         } 
      }
   }
   
   static class Dialog extends JDialog 
      implements ActionListener, KeyListener, ChangeListener {

      Teacher teach;
      JLabel lab = new JLabel("Delay");
      JSlider slid = new JSlider(MIN, MAX, NORM);
      JButton next = new TinyButton("Next");
      JButton clos = new TinyButton("Close");
      JTextArea com = new JTextArea("Comments", 6, 30);
      JTextField cmd = new JTextField("Command");
      
      Dialog() {
         super(Menu.frm, true);  //modal  
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         JPanel p = new JPanel(new java.awt.BorderLayout(5,5));
         p.setBackground(java.awt.Color.orange);
         p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
         p.add(topPanel(), "North");
         com.setEditable(false);
         p.add(new JScrollPane(com), "Center");
         cmd.setEditable(false);
         p.add(cmd, "South");
         lab.setFont(InspectorPanel.NORM);
         next.setFont(InspectorPanel.NORM);
         com.setFont(InspectorPanel.LARGE);
         cmd.setFont(InspectorPanel.LARGE);
         setContentPane(p); pack();
         slid.requestFocus();
      }
      JPanel topPanel() {
         JPanel top = new JPanel();
         top.setOpaque(false);
         top.add(lab);
         slid.setOpaque(false);
         slid.setPreferredSize(new Dimension(100, 16));
         slid.addChangeListener(this);
         slid.addKeyListener(this);
         top.add(slid);
         next.addActionListener(this);
         next.addKeyListener(this);
         top.add(next);
         clos.addActionListener(this);
         clos.addKeyListener(this);
         top.add(clos);
         return top;
      }
      public void next() {
        if (teach == null) dispose();
        else teach.wakeup();
      }
      public void dispose() {
         super.dispose();
         if (teach == null) return;
         teach.stopped = true; teach.wakeup();
      }
      public void stateChanged(ChangeEvent e) {
        if (e.getSource() != slid) return;
        if (slid.getValueIsAdjusting()) return;
        int old = teach.time;
        int k = slid.getValue();
        teach.time = k * UNIT;
        if (teach.time < old) teach.wakeup();
      }
      public void keyPressed(KeyEvent e) {}
      public void keyReleased(KeyEvent e) {}
      public void keyTyped(KeyEvent e) {
         char c = e.getKeyChar();
         if (c == KeyEvent.VK_ESCAPE) dispose();
         else if (c == KeyEvent.VK_ENTER) next();
      }
      public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == clos) dispose();
        else if (src == next) next();
      }
   }
}
