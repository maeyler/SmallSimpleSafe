//Sample program to show simple AWT components
//24.6.2002 AWT
//10.2.2003 Swing    
//1.12.2003 public fields -> methods
//14.3.2015 use screen resolution

package mae.util;import java.awt.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;

public class Small implements ListSelectionListener {

    public static final String MSG = "Small is beautiful";
    String msg = MSG;
    JFrame frm;
    JLabel lab;
    JTextArea text;
    JList list;
    static final int GAP = 10, M =12;
    static final Color COL = Color.pink;
    static final Font SMALL = new Font("Serif", 0, 13);
    static final Font LARGE = new Font("Serif", 3, 16);
    
    public Small() {
        int d = (Frame.getFrames().length==0)?
           JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE;

        JPanel pan = new JPanel();
        pan.setLayout(new BorderLayout(GAP, GAP-4)); //vgap needed
        pan.setBorder(new EmptyBorder(GAP, GAP, GAP, GAP));
        pan.setBackground(COL);
        pan.setFont(SMALL);
        lab = new JLabel(MSG, SwingConstants.CENTER);
        lab.setName("title");
        lab.setFont(LARGE);
        lab.setForeground(Color.black);
        pan.add(lab, "North");
        text = new JTextArea("JTextArea\n");
        text.setName("area");
        text.setFont(SMALL);
        JScrollPane scr1 = new JScrollPane(text);
        scr1.setPreferredSize(new Dimension(140, 100));
        pan.add(scr1, "Center");
        String[] a = new String[M];
        a[0] = "JList";
        for (int i=1; i<M; i++)
            a[i] = "List item "+ i;
        list = new JList(a);  //Multiple mode by default
        list.setName("items");
        list.setFont(SMALL);
        list.setBackground(Color.yellow);
        list.addListSelectionListener(this);
        JScrollPane scr2 = new JScrollPane(list);
        scr2.setPreferredSize(new Dimension(112, 100));
        pan.add(scr2, "East");

        pan.setToolTipText("Swing components are wonderful");
        lab.setToolTipText("Really");
        text.setToolTipText("Copy/Paste with keyboard");
        list.setToolTipText("A cute colorful list");

        frm = new JFrame(MSG);
        frm.setContentPane(pan); 
        frm.setDefaultCloseOperation(d);
        frm.setLocation(90, 50);
        Scaler.scaleWindow(frm);  //frm.pack(); 
        frm.setVisible(true);
    }
    public String message() { return msg; }
    public JFrame getFrame() { return frm; }
    public JLabel getLabel() { return lab; }
    public JTextArea getTextArea() { return text; }
    public JList getList() { return list; }
    public void setMessage(String s) {
        msg = s; frm.setTitle(s); 
        lab.setText(s); text.append(s+"\n");
    }
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        System.out.println(list.getSelectedValue());
    }
    public static void main(String[] args) {
         System.out.println("Begin Small");
         new Small();
    }
}
