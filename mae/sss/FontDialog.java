/*
 * FontDialog.java
 * Created on June 5, 2003  by Forte
 */

package mae.sss;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import mae.util.TinyButton;

/**
 * @author  Eyler
 */
 
public class FontDialog extends JDialog {

    public Font[] font, orig;
    InspectorPanel panel;
    Font curFont;
    JPanel top, right, pan;
    JPanel buttons, labels;
    JRadioButton[] but = new JRadioButton[M];
    JLabel[] lab = new JLabel[M];
    int curBut;
    JScrollPane scr;
    JList list;
    JTextField size;
    JCheckBox bold;
    JCheckBox italic;
    JButton ok, reset, cancel;
    JTextField sample;
    Ear ear = new Ear();
    final static int M = 3; 
    final static String
       OK = "OK", CANCEL = "Cancel", RESET = "Reset";
    final static Color 
       BKGD  = new Color(255, 255, 140); 
    final static Font 
       NORM  = new Font("SansSerif", 0, 11),
       TTYP  = new Font("Monospaced", 0, 12),
       LARGE = new Font("Serif", 0, 14);
    final static Font[] 
       FONTS = { NORM, TTYP, LARGE};
    final static String[] 
       LABS  = { "Normal", "Mono  ", "Large " }; 

    /** Creates new form FontDialog */
    public FontDialog() {
        this(null, FONTS);
    }

    public FontDialog(Frame frm, Font[] f) {
        super(frm, true);
        font = f;
        orig = (Font[])font.clone();
        initComponents();
        initFonts(false);
        pack();
    }
   
    public static FontDialog newDialog(Inspector ins) {
        InspectorPanel p = ins.panel;
        Font[] f = new Font[M];
        for (int i=0; i<M; i++) 
           f[i] = p.getFont(i);
        FontDialog d = new FontDialog(ins.frm, f);
        d.panel = p; return d;
    }

    /** This method is called from within 
     * the constructor to initialize the form.
     */
    void initComponents() {
      top    = new JPanel();
      buttons= new JPanel();
      labels = new JPanel();
      scr    = new JScrollPane();
      list   = new JList();
      right  = new JPanel();
      pan    = new JPanel();
      size   = new JTextField("14");
      bold   = new JCheckBox("Bold");
      italic = new JCheckBox("Italic");
      cancel = new TinyButton(CANCEL);
      reset  = new TinyButton(RESET);
      ok     = new TinyButton(OK);
      sample = new JTextField("Sample Text");
      for (int i=0; i<M; i++) {
         but[i] = new JRadioButton(LABS[i]);
         lab[i] = new JLabel("JLabel "+i);
      }
/* Accelerator doesn't work by itself   
      //dummy menu items for key events
      JMenuItem mi = new JMenuItem(OK);
      int CR = KeyEvent.VK_ENTER;
      mi.setAccelerator(KeyStroke.getKeyStroke(CR, 0));
      mi.addActionListener(ear);
      mi = new JMenuItem(CANCEL);
      int ESC = KeyEvent.VK_ESCAPE;
      mi.setAccelerator(KeyStroke.getKeyStroke(ESC, 0));
      mi.addActionListener(ear);
*/
      JComponent cp = (JComponent)getContentPane();
      cp.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
      cp.setBackground(BKGD);
      cp.setLayout(new BorderLayout(5, 5));
      setTitle("Font Selection");
      addWindowListener(ear);
      
//      top.setLayout(new GridLayout(M, 2, 5, 5));
      top.setLayout(new BorderLayout(5, 5));
      buttons.setLayout(new GridLayout(M, 1));
      labels.setLayout(new GridLayout(M, 1));
      top.setOpaque(false);
      buttons.setOpaque(false);
      labels.setOpaque(false);
      for (int i=0; i<M; i++) {
         but[i].setFont(NORM);
         but[i].setOpaque(false);
         but[i].setActionCommand(""+i);
         but[i].addActionListener(ear);
         buttons.add(but[i]); 
//         lab[i].setHorizontalAlignment(SwingConstants.CENTER);
         labels.add(lab[i]);  
         curFont = font[i];
         updateButton(i);
      }
      top.add(buttons, BorderLayout.WEST); 
      top.add(labels, BorderLayout.CENTER); 
      cp.add(top, BorderLayout.NORTH);
      
      list.setVisibleRowCount(6);
      list.setFont(NORM);
      list.addListSelectionListener(ear);
      scr.setViewportView(list);
      cp.add(scr, BorderLayout.CENTER);

      right.setLayout(new BorderLayout(5, 5));
      right.setOpaque(false);
      pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
      pan.setOpaque(false);
      
      size.setFont(new Font("SansSerif", 0, 12));
      size.addActionListener(ear);
      right.add(size, BorderLayout.NORTH);
      
      bold.setFont(NORM);
      bold.setMnemonic('B');
      bold.setOpaque(false);
      bold.addActionListener(ear);
      pan.add(bold);
      
      italic.setFont(NORM);
      italic.setMnemonic('I');
      italic.setOpaque(false);
      italic.addActionListener(ear);
      pan.add(italic);
      
      pan.add(Box.createVerticalGlue());

      cancel.setMnemonic('C');
      cancel.addActionListener(ear);
      pan.add(cancel);

      pan.add(Box.createRigidArea(new Dimension(5, 5)));

      reset.setMnemonic('R');
      reset.addActionListener(ear);
      pan.add(reset);
      
      right.add(pan, BorderLayout.CENTER);

      ok.setMnemonic('O');
      ok.addActionListener(ear);
      right.add(ok, BorderLayout.SOUTH);
      
      cp.add(right, BorderLayout.EAST);

      sample.setFont(new Font("SansSerif", 0, 36));
      cp.add(sample, BorderLayout.SOUTH);
    }

    /** 
     * get system fonts 
     */
    public void initFonts(boolean mono) {
      String[] F = GraphicsEnvironment
         .getLocalGraphicsEnvironment()
         .getAvailableFontFamilyNames();
      list.setListData(F); 
    }

    /** First Button is selected upon entry */
    public void setVisible(boolean vis) {
      if (vis) selectButton(0);
//      list.setSelectedValue(font.getName(), true);
//      setFont(font); 
      super.setVisible(vis);
    }

    /** Set current font */
     public void setFont(Font f) {
         if (curFont == f) return;
         String s = f.getName();
         int n = f.getSize();
         curFont = f;
         sample.setFont(f);
         updateButton(curBut);
         bold.setSelected(f.isBold());
         italic.setSelected(f.isItalic());
         size.setText(""+n);
         list.setSelectedValue(s, true);
//         System.out.println(curFont);
    }

    /** Selected font becomes current */
     void setSelectedFont() {
         String s = list.getSelectedValue().toString();
         int n = Integer.parseInt(size.getText());
         int k = 0;
         if (bold.isSelected()) k += Font.BOLD;
         if (italic.isSelected()) k += Font.ITALIC;
         Font f = new Font(s, k, n);
         setFont(f);
         reset.setEnabled(true);
    }

    /** current font is accepted */
     void commitCurrent() {
         but[curBut].setSelected(false);
         font[curBut] = curFont;
         if (panel != null)
            panel.setFont(curBut, curFont);
     }

    /** Select JRadioButton k */
     void selectButton(int k) {
         curBut = k;
         setFont(font[k]); 
         but[k].setSelected(true);
         reset.setEnabled(false);
     }

    /** Update JRadioButton k */
     void updateButton(int k) {
         String s = curFont.getName()+" ";
         lab[k].setText(s+curFont.getSize());
         lab[k].setFont(curFont);
     }

    /** Inner class for event listening */
   class Ear extends WindowAdapter implements 
       ActionListener, ListSelectionListener {
      public void actionPerformed(ActionEvent e) {
         Object src = e.getSource();
         String cmd = e.getActionCommand();
//         System.out.println(cmd);
         if (src instanceof JRadioButton) {
            int k = Integer.parseInt(cmd);
            if (k == curBut) return;
            commitCurrent();
            selectButton(k);
         } else if (cmd.equals(OK)) {
            setSelectedFont(); 
            commitCurrent();
            orig = (Font[])font.clone();
            setVisible(false);  //dispose();
         } else if (cmd.equals(RESET)) {
            setFont(font[curBut]);
            reset.setEnabled(false);
            if (panel != null)
               panel.setFont(curBut, curFont);
         } else if (cmd.equals(CANCEL)) {
            windowClosing(null);
         } else { //size, bold, italic
            setSelectedFont();
         }
      }
      public void valueChanged(ListSelectionEvent e) {
         if (e.getValueIsAdjusting()) return; 
         setSelectedFont();
//         String s = e.getFirstIndex()+"-"+e.getLastIndex();
//         System.out.println(s+" "+curFont);
      }
      public void windowClosing(WindowEvent e) {
         if (panel != null)
            for (int i=0; i<M; i++) 
               if (!font[i].equals(orig[i]))
                  panel.setFont(i, orig[i]);
         but[curBut].setSelected(false);
         font = (Font[])orig.clone();
         setVisible(false);  //dispose();
      }
   }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new FontDialog().setVisible(true);
    }
}
