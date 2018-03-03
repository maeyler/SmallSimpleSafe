//Eyler  12.12.2002
//swing   13.4.2003
//drag     9.8.2004

package mae.brow; 

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.text.DateFormat;
import java.util.zip.ZipFile;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.geom.AffineTransform;
import mae.util.TreePanel;
import mae.util.TinyButton;
import mae.util.ClassSummary;
import mae.util.PropertyManager;
import mae.util.Loader;
import mae.util.Console;
import mae.util.LineNumberPane; //V1.65
import mae.util.Scaler; //V1.68

public class BrowserPanel extends JSplitPane {

   DateFormat form;
   JFrame frm;
   JList<Object> root = new JList<>();
   JList<Object> list = new JList<>();;
   JPanel infoPan, imgPan;
   JLabel pathL, fileL, contL;
   JViewport port;
   JTextArea txt;
   LineNumberPane lines; //V1.65
   JLabel lab;
   JEditorPane htm;
   TinyButton save, move, open, refr, plus, minus, rotL, rotR, fix, insp;
   Component current;
   ImageIcon icn;
   Image img;
   int width, height;
   float factor;
   Metadata meta;  //V1.67
   final static int GAP = 3, MAX = 70; //chars
   final static int verHeight = 10;
   final static Color verColor = Color.blue;
   final static Font 
      verFont = Scaler.scaledFont("SansSerif", 0, 9),
      TTYP  = Scaler.scaledFont("Monospaced", 0, 13),
      BOLD  = Scaler.scaledFont("Dialog", 1, 12),
      NORM  = Scaler.scaledFont("SansSerif", 0, 12);

   public BrowserPanel(JFrame f) {
      super(VERTICAL_SPLIT, null, null);
      frm = f;
      form = DateFormat.getDateTimeInstance(
          DateFormat.MEDIUM, DateFormat.SHORT);

      setOneTouchExpandable(true);
      setContinuousLayout(true);
      setDividerSize(GAP+4);
      setBorder( 
         BorderFactory.createEmptyBorder(0, GAP, GAP, GAP)
      );

      root.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      root.setToolTipText("Root of the file system");
      JScrollPane one = new JScrollPane(root);
      one.setPreferredSize(Scaler.scaledDimension(100, 160));

      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      list.setToolTipText("Folders & files in the current folder");
      JScrollPane two = new JScrollPane(list);
      
      infoPan = new JPanel(new BorderLayout());  //extra level 
      infoPan.setBorder(
         BorderFactory.createBevelBorder(1)
      );
      infoPan.add(infoPanel(), "North");  //for fixed-size panel
      imgPan = imagePanel();
      infoPan.add(imgPan, "East");
      imgPan.setVisible(false);
      contL = new JLabel(".");
      contL.setFont(verFont);
      contL.setForeground(verColor);
      infoPan.add(contL, "West");
      fix = new TinyButton(Browser.FIX); //V1.67
      fix.setToolTipText("Actual date taken (internal Exif data) -- ALT-X to fix");
      fix.setMnemonic('X');
      fix.setVisible(false);
      Scaler.scaleComp(fix); //V2.06
      infoPan.add(fix, "South");

      JPanel top = new JPanel(new BorderLayout(GAP+2, 0));
      top.setOpaque(false);
      top.add(one, "West");
      top.add(two, "Center");
      top.add(infoPan, "East");
      setTopComponent(top);
      
      txt = new JTextArea(); 
      txt.setFont(TTYP);
      txt.setEditable(false);
      if (Console.setDragEnabled(txt))
          Console.setDragFeedback(txt);
      lab = new JLabel("", JLabel.CENTER);
      lab.setFont(BOLD);
      htm = new JEditorPane();  //"text/html", "");
      htm.setEditable(false);
      htm.setFont(NORM);
      Console.setDragEnabled(htm);
      current = txt;
      JScrollPane bot = new JScrollPane(txt);
      bot.setPreferredSize(new Dimension(680, 340));
      port = bot.getViewport();
      setBottomComponent(bot);
      lines = LineNumberPane.addLineNumbers(txt);  //V1.65
      
      frm.setContentPane(this);
   }
   JPanel imagePanel() {
      JPanel p = new JPanel(new FlowLayout(0,5,0)); 
      p.setOpaque(false);
      rotL = new TinyButton("<");
      rotL.setToolTipText("Rotate left -- <CTRL><left>");
      p.add(rotL);
      plus = new TinyButton("+");
      plus.setToolTipText("Zoom in");
      p.add(plus);
      minus = new TinyButton("-");
      minus.setToolTipText("Zoom out");
      p.add(minus);
      rotR = new TinyButton(">");
      rotR.setToolTipText("Rotate right -- <CTRL><right>");
      p.add(rotR);
      Scaler.scaleComp(p); //V1.68
      return p;
   }
   JPanel infoPanel() {
      JPanel p = new JPanel(new BorderLayout()); 
      p.setOpaque(false);
      p.setBorder( 
         BorderFactory.createEmptyBorder(verHeight+2, GAP, GAP, GAP)
      );

      String empty = "<HTML>\n&nbsp;<BR>&nbsp;\n";
      pathL = new JLabel(empty);
      pathL.setFont(NORM);
      pathL.setToolTipText("Information about the current folder");
      Border bor = BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(Color.lightGray),
         BorderFactory.createEmptyBorder(0, 2, 0, 2)
      );
      pathL.setBorder(bor);
      //.createTitledBorder
      //   (null, "Current Folder", 0, 0, verFont, verColor));
      fileL = new JLabel(empty);
      fileL.setFont(NORM);
      fileL.setToolTipText("Information about the displayed file");
      fileL.setBorder(bor);
      //fileL.setBorder(BorderFactory.createTitledBorder
      //   (null, "Displayed File", 0, 0, verFont, verColor));
      
      JPanel but = new JPanel();
      but.setOpaque(false);
      refr = new TinyButton(Browser.REFR);
      refr.setMnemonic('H');
      refr.setToolTipText("Refresh the current folder");
      but.add(refr);
      save = new TinyButton(Browser.SAVE);
      save.setMnemonic('S');
      save.setToolTipText("Save a copy of the displayed file");
      save.setEnabled(false);
      but.add(save);
      move = new TinyButton(Browser.MOVE);
      move.setMnemonic('M');
      move.setToolTipText("Rename/Move the displayed file");
      move.setEnabled(false);
      but.add(move);
      insp = new TinyButton(Browser.INSPECT);
      insp.setMnemonic('I');
      insp.setToolTipText("Inspect the file in SSS");
      insp.setEnabled(false);
      but.add(insp);
      open = new TinyButton(Browser.OPEN);
      open.setMnemonic('O');
      open.setToolTipText("Edit/Open/Run the displayed file");
      open.setEnabled(false);
      but.add(open);
      Scaler.scaleComp(but); //V1.68
      
      p.add(pathL, "North");
      p.add(but, "Center");
      p.add(fileL, "South");
      return p;
   }
   void enableButtons() {
      save.setEnabled(true);
      move.setEnabled(true);
      insp.setEnabled(true);
      move.setText(Browser.RENAME);
   }
   void addListeners(Browser.Ear ear) {
      lab.addKeyListener(ear);
      txt.addKeyListener(ear);
      htm.addKeyListener(ear);
      htm.addHyperlinkListener(ear);
      root.addListSelectionListener(ear);
      root.addKeyListener(ear);
      list.addListSelectionListener(ear);
      list.addKeyListener(ear);
      save.addActionListener(ear);
      save.addKeyListener(ear);
      move.addActionListener(ear);
      move.addKeyListener(ear);
      insp.addActionListener(ear);
      insp.addKeyListener(ear);
      open.addActionListener(ear);
      open.addKeyListener(ear);
      refr.addActionListener(ear);
      refr.addKeyListener(ear);
      plus.addActionListener(ear);
      plus.addKeyListener(ear);
      minus.addActionListener(ear);
      minus.addKeyListener(ear);
      rotL.addActionListener(ear);
      rotL.addKeyListener(ear);
      rotR.addActionListener(ear);
      rotR.addKeyListener(ear);
      fix.addKeyListener(ear);  //V2.06
      fix.addActionListener(ear);
   }
//============================================================
   public void paint(Graphics g) {
      super.paint(g);
      if (getDividerLocation() > 0) paintVersion(g, getWidth());
   }
   //Common code: used in Fide
   static void paintVersion(Graphics g, int wid) {
      g.setColor(verColor);
      g.setFont(verFont);
      int w = g.getFontMetrics().stringWidth(Browser.version);
      int x = wid - w - 2*GAP;
      g.drawString(Browser.version, x-2, verHeight+2);
   }
   static String prefix(String s) {
      int n = s.length();
      if (n > MAX) 
         s = s.substring(0,6)+"..."+s.substring(n-MAX+15);
      return "<HTML><B>"+ s +"</B><BR>\n";
   }  
   void setFolderText(String p, int d, int f) {
      frm.setTitle(p);
      pathL.setText(prefix(p)+"Folders: "+d+" &nbsp; Files: "+f);
   }
   String timeToString(long t) { //V1.67
       return form.format(new Date(t));
   }
   String setFileText(File f) {
      long n = f.length();
      String size;
      if (n < 10000) size = n+"bytes";
      else if (n < 10000000)
           size = (n>>10)+"K";
      else size = (n>>20)+"M";
      open.setEnabled(true);
      String d = prefix(f.toString())+size+" &nbsp;&nbsp;&nbsp; ";
      fileL.setText(d + form.format(new Date(f.lastModified())));
      contL.setText("   "+fileType(f));  //V2.06
      return size;
   }
   String fileType(File f) { //V1.67
      try {
         fix.setVisible(false); meta = null;
         String type = Browser.fileTypeNIO(f);
         if (type == null) type = Browser.fileTypeURL(f);
         System.err.printf("%s  %s%n", f.getName(), type);
         if (!type.startsWith("image")) return type;
         meta = new Metadata(f);
         long t = meta.getTime();
      //V2.06 -- silent bug corrected: (plus -> comma)
      //System.err.printf("fileType %s --> %s \n", f, type);
         if (t <= 0) return type;
         fix.setText(timeToString(t));
         fix.setEnabled(!meta.correctTime());
         fix.setVisible(true);
         return type;
      } catch (Exception x) {
         //System.err.println(x);  //V2.06 found the bug!
         return "???";
      }
   }
   void doFix(File f) { //V1.67
      //System.err.println("Fix "+f);
      if (f.setLastModified(meta.getTime()))
           System.err.print("Fixed -- ");
      else System.err.print("Cannot fix??? ");
      setFileText(f);
      System.err.println(f.getName());
   }
   void displayText(String s) {
      txt.setText(s);
      img = null; 
      setView(lines); //V1.65
      txt.select(0, 0);
      txt.requestFocus(); 
   }
   void showText(String s) {
      lab.setText(s);
      lab.setIcon(null); 
      //lab.setToolTipText(null);
      icn = null; img = null;
      setView(lab);
   }
   void rotate(float r) { //r is -1 or +1
      if (current != lab) return;
      float x = (r<0)? 0 : height;
      float y = (r<0)? width : 0;
      AffineTransform rot = new AffineTransform(0,r,-r,0,x,y);
      Image buf = lab.createImage(height, width);
      Graphics2D g = (Graphics2D)buf.getGraphics();
      g.drawImage(img, rot, null);
      img = buf;
      width = img.getWidth(null);
      height = img.getHeight(null);
      float f = factor;
      factor = 0; scale(f);
   }
   void bestFit() {
      width = img.getWidth(null);
      height = img.getHeight(null);
      Container p = port.getParent();
      float fw = (p.getWidth()-3) / (float)width;
      float fh = (p.getHeight()-3) / (float)height;
      float f = Math.min(fw, fh); //scaling factor
      if (f > 0.95) f = 1; //allow 5% margin
      factor = 1; scale(f);
   }
   void scale(float f) {
      boolean skip = (f == factor); //no need to scale
      //int width = img.getWidth(null);
      //int height = img.getHeight(null);
      int p = Math.round(100*f);
      String tip = width+"x"+height+"  "+p+"%";
      contL.setText("   Image: "+tip);
      //lab.setToolTipText(tip);
      if (skip) return; factor = f; 
      int w = Math.round(f*width); 
    //int h = Math.round(f*height);
      if (w < 10 || f*height < 10) return; //too small
      lab.setIcon(null);
      int s = Image.SCALE_DEFAULT;
      icn.setImage(img.getScaledInstance(w, -1, s));
      lab.setIcon(icn); //lab.repaint();
   }
   /*
   //the only concrete method in abstract java.awt.Image:
   public Image getScaledInstance(int w, int h, int hints) {
      ImageFilter filter =  //pick an ImageFilter
      (hints & (SCALE_SMOOTH | SCALE_AREA_AVERAGING)) != 0 ?
         new AreaAveragingScaleFilter(w, h) :
         new ReplicateScaleFilter(w, h);
    //I THOUGHT multiple calls do not deteriorate image quality since 
    //filter is used on the source, not on this image, BUT IT DOES!
      ImageProducer prod =  
         new FilteredImageSource(getSource(), filter);
      return Toolkit.getDefaultToolkit().createImage(prod);
   }*/
   void showPicture(String name) {
      icn = new ImageIcon(name); img = icn.getImage();
      lab.setText(null); lab.setIcon(icn); 
      bestFit(); setView(lab);
   }
   void displayPage(URL u) {
      img = null;
      try {
         htm.setPage(u); setView(htm);
      } catch (IOException x) {
         System.err.println(x.getMessage());
      }
   }
   void displayHTML(String s) {
      htm.setContentType("text/html");
      htm.setText(s); img = null;
      setView(htm);
      htm.select(0, 0);
   }
   void displayClass(String name) {
      ClassSummary sum = new ClassSummary(name);
      //displayText(sum.description(false));
      String s = sum.toHTML();  //V2.01
      if (s.startsWith("HTML")) s = s.substring(6);
      s = "<HTML><FONT size="+Scaler.HTML_SIZE+"> "+s+"</FONT>";   
      displayHTML(s);
      if (sum.hasMain()) open.setText(Browser.RUN);
      else open.setEnabled(false);
   }
   void displayZip(File f) throws IOException {
      ZipFile z = new ZipFile(f); img = null;
      setView(new TreePanel(z).getTree());
      z.close();
      String m = Loader.mainClassOf(f);
      if (m != null) open.setText(Browser.RUN);
      //else open.setEnabled(false);
   }
   void setView(JComponent c) {
      imgPan.setVisible(img != null);
      if (c == lines || c == htm) { //V1.65
         open.setText(Browser.EDIT);
      } else {
         open.setText(Browser.OPEN);
      }
      if (current == c) return;
      port.setView(c); current = c;
   }
   void zoomIn() {
      if (current == lab && factor < 1.5)
         scale(factor*1.2F);
      //else Toolkit.getDefaultToolkit().beep();
   }
   void zoomOut() {
      if (current == lab && factor > 0.25)
         scale(factor/1.2F);
      //else Toolkit.getDefaultToolkit().beep();
   }

    void loadProps(PropertyManager pm) {
        int x = Scaler.scaledInt(600);
        int y = Scaler.scaledInt(600);
        frm.setBounds(pm.getBounds("frame", x, y));
        int d = Scaler.scaledInt(160);
        setDividerLocation(pm.getInteger("divider.loc", d));
        Color c = pm.getColor("color.panel", Color.pink);
        root.setBackground(c);
        list.setBackground(c);
        infoPan.setBackground(c);
        Font norm = pm.getFont("font.norm", NORM);
        root.setFont(norm);
        list.setFont(norm);
        Font ttyp = pm.getFont("font.ttyp", TTYP);
        txt.setFont(ttyp);
    }    

    void saveProps(PropertyManager pm) {
        pm.setBounds("frame", frm.getBounds());
        pm.setProperty("divider.loc", ""+getDividerLocation());
        pm.setColor("color.panel", infoPan.getBackground());
        pm.setFont("font.norm", root.getFont());
        pm.setFont("font.ttyp", txt.getFont());
    }    

   public static void main(String[] args) {
      int d = (Frame.getFrames().length == 0)?
         JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE;
      JFrame frm = new JFrame("BrowserPanel");
      frm.setDefaultCloseOperation(d);
      BrowserPanel p = new BrowserPanel(frm);
      frm.setVisible(true); //frm.pack();
   }
}
