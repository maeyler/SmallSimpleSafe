//21.3.2015 scale fonts and sizes using screen resolution

package mae.util;
import java.awt.*;
import java.util.*;
import javax.swing.*;

public class Scaler {

    public static final int 
        RESOLUTION = Toolkit.getDefaultToolkit().getScreenResolution(),
        HTML_SIZE = RESOLUTION/24;  //4,5,6
                  //add 1 for Arabic: 5,6,7
    public static final float RES_RATIO = RESOLUTION/96f;  //default resolution 96
    public static final Scaler ins = new Scaler(RES_RATIO); //default instance
    Map<Font, Font> map = new HashMap<Font, Font>(); //keep derived fonts
    float ratio; int nF, nD;
    
    public Scaler(float r) { ratio = r; }
    public float scaled(float x) { return x*ratio; }
    public int scaled(int k) { return Math.round(k*ratio); }
    public Font scaled(Font f) {
        if (f == null) return null;
        Font g = map.get(f);
        if (g != null) return g;
        g = f.deriveFont(scaled(f.getSize2D()));
        if (map.size() > 200) map.clear();
        map.put(f, g); return g;
    }
    public Dimension scaled(Dimension d) {
        if (d == null) return null;
        return new Dimension(scaled(d.width), scaled(d.height));
    }
    void scaleComponent(Component c) {
        c.setFont(scaled(c.getFont())); nF++;
        if (c.isPreferredSizeSet()) {
            Dimension d = c.getPreferredSize();
            c.setPreferredSize(scaled(d)); nD++;
        }
        if (c instanceof Container) {
            Container p = (Container)c;
            for (Component t: p.getComponents())
                scaleComponent(t); //recursive call
        }
    }
    public void scale(Component c) {
        nF = 0;  nD = 0;
        scaleComponent(c);
        System.err.printf("%s fonts and %s dimensions scaled \n", nF, nD);
    }
    
    public static JFileChooser fileChooser() {
        return new JFileChooser() {
          protected JDialog createDialog(Component p) throws HeadlessException {
             JDialog g = super.createDialog(p);
             //g.setSize(ins.scaled(g.getSize()));
             scaleComp(g); g.pack();
             return g;
          }
        };
    }
    public static float scaledFloat(float x) { return ins.scaled(x); }
    public static int scaledInt(int k) { return ins.scaled(k); }
    public static Font scaledFont(String name, int style, float size) {
        Font f = new Font(name, style, 1); //unit font
        return ins.scaled(f.deriveFont(size));
    }
    public static Dimension scaledDimension(int w, int h) {
        return ins.scaled(new Dimension(w, h));
    }
    public static void scaleWindow(Window w) { ins.scale(w); w.pack(); }
    public static void scaleComp(Component c) { ins.scale(c); }
    public static void main(String[] args) throws Exception {
        Small s = new Small();
        Window w = s.getFrame();
        w.setLocation(100, 180);
        Thread.sleep(999);
        scaleWindow(w);
    }
}
