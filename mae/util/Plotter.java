package mae.util;

import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import javax.swing.border.EmptyBorder;
import static java.awt.Color.*;

public class Plotter {
    public static Plotter P;
    final JFrame frm;
    final Panel pan = new Panel(); //inner class
    int xM, yM;  //panel size
    int xZ, yZ;  //zero point
    int N;       //number of points
    int count;   //number of paint() calls
    int[][] D = new int[0][0];  //integer data
    static final int GAP = 10;
    static final String ERR_MSG = "lengths must be the same";

    public Plotter(int x, int y) { this(null, x, y); }
    public Plotter(JFrame f, int x, int y) {
        frm = f; setSize(x, y); 
    }
    public JComponent panel() { return pan; }
    public void setSize(int x, int y) {
        pan.setPreferredSize(new Dimension(x, y));
        xM = x;  yM = y;  
        if (frm != null) frm.pack(); 
    }
    public void plot1(double[] a, double[] b) { //x-y plot uses the same scale 
        N = a.length;
        verify(N == b.length, ERR_MSG);
        D = new int[2][N]; 
        MinMax P = new MinMax(0);
        P.setMinMax(a);
        P.setMinMax(b);
        xZ = convert(a, D[0], P.min, P.max, xM);
        yZ = convert(b, D[1], P.max, P.min, yM);
        pan.repaint();
    }
    public void plot2(double[]... A) { //x-axis may have a different scale
        N = A[0].length;
        verify(N == A[1].length, ERR_MSG);
        D = new int[A.length][N]; 
        MinMax P = new MinMax(0);
        P.setMinMax(A[0]);
        xZ = convert(A[0], D[0], P.min, P.max, xM);
        P = new MinMax(0);
        for (int j=1; j<A.length; j++) 
            P.setMinMax(A[j]);
        for (int j=1; j<A.length; j++) 
            yZ = convert(A[j], D[j], P.max, P.min, yM);
        pan.repaint();
    }

    class MinMax {
        double min, max; 
        public MinMax(double n) { this(n, n); }
        public MinMax(double m, double n) { 
            min = m; max = n; 
        }
        public void setMinMax(double[] a) {
            for (int i=0; i<a.length; i++) {
                if (a[i] > max) max = a[i];
                if (a[i] < min) min = a[i];
            }
        }
    }

    class Panel extends JComponent {
        Color[] col = { black, blue, red, darkGray, green, orange, magenta, cyan, yellow };
        public void paint(Graphics g) {
            super.paint(g); count++;
            g.setColor(lightGray);
            g.drawLine(0, yZ, xM, yZ); //x axis
            g.drawLine(xZ, 0, xZ, yM); //y axis
            for (int j=1; j<D.length; j++) {
                g.setColor(col[j]);
                for (int i=1; i<N; i++) 
                    g.drawLine(D[0][i-1], D[j][i-1], D[0][i], D[j][i]);
            }
        }
    }
    
    static int convert(double[] a, int[] b, double min, double max, int M) {
        if (max == min) max++;
        double c = (M-2)/(max-min);
        for (int i=0; i<a.length; i++) 
            b[i] = 1 + (int)Math.round(c*(a[i] - min));
        return     1 + (int)Math.round(-c*min);
    }
    static void verify(boolean condition, String msg) {
        if (!condition) throw new RuntimeException(msg);
    }
    static void makeFrame() {
        JFrame f = new JFrame();
        f.setResizable(false);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JComponent cp = (JComponent)f.getContentPane(); 
        cp.setBorder(new EmptyBorder(GAP, GAP, GAP, GAP));
        P = new Plotter(f, 500, 500); 
        cp.add(P.pan); 
        f.pack(); f.setVisible(true);
    }
    public static void init(String msg) {
        if (P == null) makeFrame();
        P.frm.setTitle(msg); 
    }
    public static void p1(String msg, double[] x, double[] y) {
        init(msg); P.plot1(x, y); //same scale
    }
    public static void p2(String msg, double[]... Y) {
        init(msg); P.plot2(Y);
    }
    public static void main(String[] args) throws InterruptedException {
        double[] a = { -50, -35, -10, 30, 50, 60, 80, 150 };
        double[] b = { -10, 10, 50, 60, 40, 10, 10, -30 };
        double[] c = {   0, 25, 50, 50, 50, 20, 50, -10 };
        p1("Plotter Test", a, b); 
        Thread.sleep(2000); 
        p2("Plotter ьзlь deneme", a, b, c);
    }
}
