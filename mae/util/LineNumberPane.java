//Aug 2004 

package mae.util;

import java.lang.reflect.Method;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.*;
import javax.swing.text.BadLocationException;

public class LineNumberPane extends JPanel implements Scrollable {
   
   static boolean exit = (JFrame.getFrames().length == 0);
   Lines lin; JTextArea txt;
   static int BRHT = 230, DARK = 130;
   static final Font
      font = new Font("Monospaced", 0, 12);
   static final Color 
      BKG = new Color(BRHT, BRHT, BRHT),
      FRG = new Color(DARK, DARK, BRHT);
   static final Cursor 
      HAND = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

   public LineNumberPane() {
      this(new JTextArea("", 30, 72));
   }
   public LineNumberPane(JTextArea t) {
      setLayout(new BorderLayout());
      Ear ear = new Ear();
      lin = new Lines(); 
      lin.setBackground(BKG);
      lin.setCursor(HAND);
      lin.addMouseListener(ear);
      lin.addMouseMotionListener(ear);

      txt = t;
      //System.out.println("Font: "+txt.getFont());
      //t.addPropertyChangeListener(ear);
      Container p = t.getParent();
      if (p == null) {
         t.setFont(font);
      } else { //no need to setFont
         p.remove(t); p.add(this);
      }
      Container q = getParent();
      //System.out.println("Parent: "+q); 
      if (q != null) q.setBackground(Color.white);
      //int w = 3*invokeMethod("getColumnWidth") + 5;
      //System.out.println(w+": "+t.getFont());
      //lin.setPreferredSize(new Dimension(w, 1));
      //System.out.println("Size: "+lin.getPreferredSize());
      add(lin, "West");
      add(txt, "Center");
   }
   public Dimension getPreferredScrollableViewportSize() {
      Dimension d = txt.getPreferredScrollableViewportSize();
      int w = lin.getPreferredWidth();
      return new Dimension(d.width + w, d.height);
   }
   public int getScrollableUnitIncrement(Rectangle r, int t, int d) {
      return txt.getScrollableUnitIncrement(r, t, d);
      //return invokeMethod("getRowHeight");
   }
   public int getScrollableBlockIncrement(Rectangle r, int t, int d) {
      int h = invokeMethod("getRowHeight");
      if (d == SwingConstants.VERTICAL)
           return (r.height/h - 1)*h;
      else return txt.getScrollableBlockIncrement(r, t, d);
      //return getSize().height - invokeMethod("getRowHeight");
   }
   public boolean getScrollableTracksViewportHeight() {
       return txt.getScrollableTracksViewportHeight();
   }
   public boolean getScrollableTracksViewportWidth() {
       return txt.getScrollableTracksViewportWidth();
   }
   int invokeMethod(String met) {
      Integer x = (Integer)invokeNoArgs
         (txt, "javax.swing.JTextArea", met);
      return x.intValue();
   }
   static Object invokeNoArgs(Object obj, String cls, String met) {
      Object[] args = { };
      Class[] types = { };
      try {
         Class c = Class.forName(cls);
         Method m = c.getDeclaredMethod(met, types);
         m.setAccessible(true);
         return m.invoke(obj, args);
      } catch (Exception x) {
         System.err.println(x);
         return null;
      }
   }
   public static LineNumberPane addLineNumbers(JTextArea t)  {
      return new LineNumberPane(t);
   }
   public static LineNumberPane main()  {
      JFrame frm = new JFrame("Test LineNumberPane"); 
      int d = exit? JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE;
      frm.setDefaultCloseOperation(d);
      LineNumberPane p = new LineNumberPane();
      frm.setContentPane(new JScrollPane(p));
      frm.pack(); //frm.setSize(400, 600);
      frm.setVisible(true); return p;
   }
   public static void main(String[] args) {
      main();
   }
   
   public class Lines extends JPanel {
      public void paintComponent(Graphics g) {
         super.paintComponent(g);
         g.setColor(FRG); g.setFont(txt.getFont());
         //int h = txt.getRowHeight();  protected!
         int h = invokeMethod("getRowHeight");
         //first version: update all rows: visible or invisible
         /* int n = txt.getHeight() / h;
         for (int i=1; i<=n; i++) {
            g.drawString(s+i, 1, i*h-4);
         }*/
         //second version: use Clip Rectangle
         Rectangle r = g.getClipBounds();
         int n1 = r.y/h;  //first line to paint
         int n2 = n1 + r.height/h + 2; //paint 2 extra lines 
         /* String s;
         for (int i=Math.max(n1-2, 1); i<=n2; i++) {
            if (i < 10) s = "  ";
            else if (i < 100) s = " ";
            else s = "";
            g.drawString(s+i, 1, i*h-4); //s & s+i are new objects
         }*/
         //third version: use one Object per call
         char[] num = new char[3];  //3 digits
         setDigit(num, 0, n1/100, n1 < 1000);
         setDigit(num, 1, n1/10, num[0]==' ');
         setDigit(num, 2, n1, false);
         for (int i=n1; i<=n2; i++) {
            g.drawChars(num, 0, 3, 1, i*h-4);
            boolean dummy //increment required digits
            = incr(num, 2) && incr(num, 1) && incr(num, 0);
         }
         //System.out.println("Paint "+n1+" to "+n2);
      }
      void setDigit(char[] a, int i, int m, boolean useBlank) {
         char c = (char)('0' + m%10);
         if (useBlank && c=='0') c = ' ';
         a[i] = c;
      }
      boolean incr(char[] a, int i) {
         boolean carry = false;
         char c = a[i]; 
         if (c == ' ') c = '1';
         else if (c != '9') c++;
         else {// c == '9'
            c = '0'; carry = true;
         }
         a[i] = c; return carry;
      }
      public int getPreferredWidth() {
         return 3*invokeMethod("getColumnWidth") + 5;
      }
      public Dimension getPreferredSize() {
         int w = getPreferredWidth();
         return new Dimension(w, 1);
      }
   }
   class Ear extends MouseAdapter implements
      MouseMotionListener, PropertyChangeListener {
      int p1, p2;
      public void mousePressed(MouseEvent e) {
         int p = txt.viewToModel(e.getPoint());
         try {
            int k = txt.getLineOfOffset(p);
            p1 = txt.getLineStartOffset(k);
            p2 = txt.getLineEndOffset(k);
            //System.out.println(p+" "+k+" "+p1);
            txt.select(p1, p2);
         } catch (BadLocationException x) {
            System.err.println(x);
         }
      }
      public void mouseDragged(MouseEvent e) { 
         int p = txt.viewToModel(e.getPoint());
         try {
            int k = txt.getLineOfOffset(p);
            //System.out.println(p+" "+k+" "+p1);
            int i = txt.getLineStartOffset(k);
            if (i <= p1) txt.select(i, p2);
            else {
               int j = txt.getLineEndOffset(k);
               if (p2 <= j) txt.select(p1, j);
            }
         } catch (BadLocationException x) {
            System.err.println(x);
         }
      }
      public void mouseMoved(MouseEvent e) {
      }
      public void propertyChange(PropertyChangeEvent e) {
         String s = e.getPropertyName();
         System.out.println(s+" -> "+e.getNewValue());
         if (s.equals("font")) {
            //int w = 3*txt.getColumnWidth() + 3;
            int w = 3*invokeMethod("getColumnWidth") + 3;
            System.out.println(w+": "+txt.getFont());
            lin.setSize(new Dimension(w, txt.getHeight())); 
         }
      }
   }
}
