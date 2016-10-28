// M A Eyler
//V1.2 7.11.2002 Simpler interface
//V1.5 13.2.2003 use Swing
//scale   22.3.15

package mae.sss;
import java.awt.*;
import javax.swing.*;
import java.util.Date;
import mae.util.Console;
import mae.util.PropertyManager;
import mae.util.TinyButton;
import mae.util.Loader;
import mae.util.Scaler;

public class InspectorPanel extends JPanel {

    static Window splash, about;  //two distinct variables V1.66
    static JLabel label;
    JScrollPane drop;
    JList left, middle, right;
    JTextField cmd, msg;
    JLabel cls, fld, mem;
    JButton clear, console, editor, browser;
    JCheckBox dispAll;
    Font normal, italic;
    //boolean demo;
    String clsTip, objTip;
    Color clsColor = new Color(130, 210, 120), //.green.darker(),
            objColor = Color.yellow;
    final static int GAP = 5, CHARS = 21, ROWS = 18;
    final static int verHeight = 10;
    final static Color verColor = Color.blue.darker();
    final static Font 
            verFont = Scaler.scaledFont("SansSerif", 0, 9), 
            NORM = Scaler.scaledFont("SansSerif", 0, 11), 
            TTYP = Scaler.scaledFont("Monospaced", 0, 12),
            LARGE = Scaler.scaledFont("Serif", 0, 14),
            BOLD = new Font("Serif", 1, 14), 
            ABOUT = new Font("Serif", 1, 30);
    //static final String TIP3 = "You may drop selections here",
    //        TIP4 = "Copy/Paste and Drag/Drop are enabled here";

    public InspectorPanel() {  //String v, boolean d) {
        //version = v;
        //demo = d;
        setLayout(new BorderLayout(GAP, GAP));
        setBorder(BorderFactory.createEmptyBorder(GAP + 2, GAP, GAP, GAP));
        JPanel top = topPanel();
                Scaler.scaleComp(top);  //V1.68
                add(top, "North");
        add(mainPanel(), "Center");
        add(bottomPanel(), "South");
        setBackground(clsColor);
        setFont(0, NORM);
        setFont(1, TTYP);
        setFont(2, LARGE);
    }
    static JPanel flowPanel(Component... ca) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 3*GAP, 0));
        p.setOpaque(false);
        for (Component c : ca)
            if (c != null) p.add(c);
        return p;
    }
    static JButton newButton(String s) {
        return newButton(s.substring(0,1), s.charAt(0), "Display "+s);
    }
    static JButton newButton(String n, char c, String s) {
        JButton b = new TinyButton(n);
        b.setMnemonic(c); b.setToolTipText(s);
        return b;
    }
    JPanel topPanel() {
        JPanel top = new JPanel(new GridLayout(1, 3, GAP, 0));
        top.setOpaque(false);
        cls = new JLabel("Classes & Objects");
        cls.setName("Classes");
        cls.setForeground(Color.black);
        cls.setToolTipText("Class=Green  Object=Yellow");
        clear = newButton("Clear", 'l', "Clear Classes or Objects");
        console = newButton("Console");
        editor  = newButton("Editor");
        //browser = newButton("Browser");
        top.add(flowPanel(cls, clear));  //
        fld = new JLabel("Fields");
        fld.setName("Fields");
        fld.setForeground(Color.black);
        fld.setToolTipText("Fields & Identities in class hierarchy");
        dispAll = new JCheckBox("Display all", false);
        dispAll.setMnemonic('A');
        dispAll.setOpaque(false);
        String m0 = "<HTML>";
        String m1 = "Display all fields, public & private"
                + "<BR>*** <B>FOR EXPERT USE</B> ***";
        dispAll.setToolTipText(m0 + m1);
        top.add(flowPanel(fld, dispAll));
        String m5 = "<BR><I>Inherited members are shown in italic</I>";
        objTip = m0 + "Public Methods" + m5;
        clsTip = m0 + "Static Methods" + ", Constructors, Identities" + m5;
        mem = new JLabel("Public Methods");;
        mem.setName("Methods");
        mem.setForeground(Color.black);
        top.add(flowPanel(mem, console, editor));  //, browser
        return top;
    }
    JPanel mainPanel() {
        JPanel p = new JPanel(new GridLayout(1, 3, GAP, 0));
        p.setOpaque(false);
        left = new Inspector.Lst("Left");
        boolean drag = Console.setDragEnabled(left);
      //left.setToolTipText(drag ? TIP4 : TIP3); removed Mar 09
        drop = new JScrollPane(left);
        p.add(drop);
        middle = new Inspector.Lst("Middle");
        middle.setSelectionMode(0);
        //if (!demo)
            p.add(new JScrollPane(middle));
        right = new Inspector.Lst("Right");
        right.setSelectionMode(0);
        right.setVisibleRowCount(ROWS);
        p.add(new JScrollPane(right));
        //drop = p;
        return p;
    }
    JPanel bottomPanel() {
        JPanel bot = new JPanel(new BorderLayout(GAP, GAP));
        bot.setOpaque(false);
        cmd = new JTextField("", CHARS);
        cmd.setName("Command");
        String s = "Invoke method with no arguments -- just method name";
        cmd.setToolTipText("Command area: " + s);
        bot.add(cmd, "West");
        //int x = demo ? 2 : 20;
        msg = new JTextField("Message area", CHARS + 20);
        msg.setName("Message");
        msg.setEditable(false);
        msg.setToolTipText("Message area: shows the result");
        bot.add(msg, "Center");
        return bot;
    }
    void addListeners(Inspector.Ear e) {
        if (e == null) return;
        left.addListSelectionListener(e);
        //left.addMouseListener(e);
        left.addKeyListener(e);
        middle.addListSelectionListener(e);
        //middle.addMouseListener(e);
        middle.addKeyListener(e);
        right.addListSelectionListener(e);
        //right.addMouseListener(e);
        right.addKeyListener(e);
        msg.addKeyListener(e);
        cmd.addActionListener(e);
        clear.addActionListener(e);
        clear.addKeyListener(e);
        console.addActionListener(e);
        console.addKeyListener(e);
        editor.addActionListener(e);
        editor.addKeyListener(e);
        dispAll.addItemListener(e);
        dispAll.addKeyListener(e);
    }
    void adjustPanel(boolean cls, int k) {
        left.setSelectedIndex(k);
        left.ensureIndexIsVisible(k);
        left.requestFocus();
        if (cls) {
            setBackground(clsColor);
            mem.setToolTipText(clsTip);
        } else {
            setBackground(objColor);
            mem.setToolTipText(objTip);
        }
    }
    void setCounts(Object x) {
        int f = middle.getModel().getSize();
        int m = right.getModel().getSize();
        //String s = demo ? "" : f + ",";
        setMessage(x + "   <" + f + "," + m + ">");
    }
    void setMessage(String s) {
        msg.setText(s);
    }
    public Font getFont(int k) {
        switch (k) {
            case 0 :
                return left.getFont();
            case 1 :
                return cmd.getFont();
            case 2 :
                return msg.getFont();
            default :
                return null;
        }
    }
    public void setFont(int k, Font f) {
        switch (k) {
            case 0 :
                clear.setFont(f);
                console.setFont(f);
                editor.setFont(f);
                dispAll.setFont(f);
                left.setFont(f);
                middle.setFont(f);
                //right.setFont(f);
                italic = new Font(f.getName(), 2, f.getSize());
                normal = f;
                break;
            case 1 :
                Console.setTextFont(f);
                cmd.setFont(f);
                break;
            case 2 :
                msg.setFont(f);
                String n = f.getName();
                int s = f.getSize();
                Font g = new Font(n, 1, s); //bold
                cls.setFont(g);
                fld.setFont(g);
                mem.setFont(g);
                break;
        }
    }
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(verColor);
        g.setFont(verFont);
        //      Graphics2D g2d = (Graphics2D)g;
        int w = g.getFontMetrics().stringWidth(SSS.version());
        //      int x = (getWidth() - w)/2;
        g.drawString(SSS.version(), getWidth() - w - 2, verHeight);
    }

    void loadProps(PropertyManager pm) {
        objColor = pm.getColor("color.object", objColor);
        clsColor = pm.getColor("color.class", objColor);
        int loc = pm.getInteger("divider.loc", Scaler.scaledInt(310));
        setFont(0, pm.getFont("font.norm", NORM));
        setFont(1, pm.getFont("font.ttyp", TTYP));
        setFont(2, pm.getFont("font.large", LARGE));
    }

    void saveProps(PropertyManager pm) {
        //        pm.setProperty("title", frm.getTitle());
        pm.setColor("color.object", objColor);
        pm.setColor("color.class", clsColor);
        pm.setFont("font.norm", getFont(0));
        pm.setFont("font.ttyp", getFont(1));
        pm.setFont("font.large", getFont(2));
    }

        static final String jVersion = System.getProperty("java.version");
        static final String user_dir = System.getProperty("user.dir");
        static final String
        HEAD = "<HTML><FONT size="+Scaler.HTML_SIZE+"> <CENTER>",
        TAIL = "</FONT>",
        WEB2 = "http://maeyler.github.io/SmallSimpleSafe/",
        SPL2 = HEAD + "<b>web:</b> "+ WEB2
            +"<br><b>mail:</b> small.simple@gmail.com " 
            +"<br><b>user.dir:</b> "+user_dir 
            +"<br><b>Java version:</b> "+jVersion + TAIL,
        SPL3 = HEAD+"Started on "+TAIL,
        SPL4 = "(C)  Akif Eyler, "+SSS.version();
    static final Color 
                COLOR = (jVersion.compareTo("1.7")>=0? Color.yellow : Color.green);
        //was Color.green; before V1.65
        static final long time = System.currentTimeMillis();
        static final Date date = new Date(time);
    static void disposeSplash() {  //V1.66
                splash.dispose(); splash = null; //for gc() to work
        }
    static void initSplash() { //a quick Frame shown while starting
        //if (splash != null) return; 
        splash = new Frame("Starting SSS");
        Panel p = new Panel();
        p.setLayout(new GridLayout(4, 1, 8, 8));
        p.setBackground(COLOR);
        //splash.setBackground(Color.cyan);
        int c = Label.CENTER;
        Label lab = new Label(SSS.title, c);
        lab.setFont(ABOUT);
        p.add(lab);
        lab = new Label(WEB2, c);
        System.out.println(lab.getText());
        lab.setFont(LARGE);
        p.add(lab);
        lab = new Label(SPL4, c);
        lab.setFont(BOLD);
        p.add(lab);
        splash.add(p);
        Scaler.scaleWindow(splash);  //splash.pack();
        centerWindow(splash, -1, -1);
        splash.setVisible(true);
        try {
            Thread.sleep(10); //give time to paint splash screen
        } catch (InterruptedException x) {
        }
    }
    public static void makeAboutDlg(JFrame f) {  //simplified V1.66
        JDialog sp = new JDialog(f, "About SSS");
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(4, 1, 8, 8));
        p.setBackground(COLOR);
        p.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
        //sp.setBackground(Color.cyan);

        JLabel lab1 = new JLabel(SSS.title, SwingConstants.CENTER);
        lab1.setFont(ABOUT);
                lab1.setForeground(Color.black);
        p.add(lab1);

        JEditorPane ep = new JEditorPane(); 
        ep.setContentType("text/html");
        ep.setText(SPL2);
        ep.setBackground(COLOR);
        ep.setEditable(false);
        p.add(ep);

                label = new JLabel(SPL3, SwingConstants.CENTER);
        label.setFont(LARGE);
                label.setForeground(Color.black);
        p.add(label);

        JLabel lab4 = new JLabel(SPL4, SwingConstants.CENTER);
        lab4.setFont(BOLD);
                lab4.setForeground(Color.black);
        p.add(lab4);

        sp.setContentPane(p); Scaler.scaleComp(p);  //V1.68
        if (Console.setDragEnabled(ep)) Console.setDragFeedback(ep);
        about = sp;
                //int W = Scaler.scaledInt(330), H = Scaler.scaledInt(310);
        //about.setSize(W, H);  
                about.pack(); centerWindow(about, -1, -1);
    }
    public static void showAboutDlg() {  //redesigned V1.65
                if (about == null) makeAboutDlg(null);
                long t1 = (System.currentTimeMillis() - time)/1000;
                long t2 = System.nanoTime()/1000000000;
                String u = "<br>Running for "+ Loader.secondsToString(t1)
                       + " -- System up for "+ Loader.secondsToString(t2);
                label.setText(SPL3 + date + u);
                about.setVisible(true);
    }
    public static void centerWindow(Window w, int x, int y) {  //V1.65
        Dimension d = w.getToolkit().getScreenSize();
        if (x < 0)
            x = d.width - w.getWidth();
        if (y < 0)
            y = d.height - w.getHeight();
        w.setLocation(x / 2, y / 2 - 200);
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Test version");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        InspectorPanel pan = new InspectorPanel();
        f.setContentPane(pan);
        f.pack(); f.setVisible(true);
    }
} 
