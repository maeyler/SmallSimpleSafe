/*
 * TextAreaPrintStream.java
 *
 * Created on July 23, 2003, 2:18 PM
 */

package mae.util;

import java.io.BufferedInputStream;
import java.awt.Component;
import javax.swing.JOptionPane;

/** Redirects system input to a JDialog.
 * <PRE>
 * Usage:
 * DialogInputStream in = DialogInputStream.redirect(frame);
 * ...  //in.setMessage(msg);
 * ...  //System.in.read();
 * in.close();
 * </PRE>
 * Cancel key signals end of file.
 * @author Eyler
 */
public class DialogInputStream extends BufferedInputStream {
    
//    InputStream in;   all defined in super class
//    byte[] buf;
//    int pos, count;
    Component parent;
    String msg = "Input String is requested";
    final static String NL = System.getProperty("line.separator");
    
    /** Creates a new instance of DialogInputStream
     * and redirects system input.
     */
    DialogInputStream(Component c) {
        super(System.in);  //in = System.in;
        System.setIn(this);
        parent = c;
    }
    
    /** Returns a new instance */    
    public static DialogInputStream redirect() {
        return new DialogInputStream(null);
    }
    
    /** Returns a new instance, using c as the parent for JDialog */    
    public static DialogInputStream redirect(Component c) {
        return new DialogInputStream(c);
    }
    
    /** Low-level read: fills the buffer */    
    void readBuffer() {
        String s = JOptionPane.showInputDialog(parent, msg);
        if (s == null) {
            System.out.println("(cancelled)");
            buf = null;
        } else {  //synchronized (this) {
            System.out.println(s);
            s += NL;
            buf = s.getBytes();
            count = buf.length;
            pos = 0;
        }
    }
    
    /** Reads and returns one byte */    
    public int read() {
        if (count == pos) readBuffer();
        if (buf == null) return -1;
        int c = buf[pos++];
//        System.out.println(count+" read "+c+" at "+(pos-1));
        return c;
    }

    /** Reads at most n bytes into b[off] */    
    public int read(byte[] b, int off, int n) {
        if (count == pos) readBuffer();
        if (buf == null) return -1;
        int k=0;
        while (k < n && pos < count) {
           b[off + k] = buf[pos++];  k++;
        }
        return k;
    }

    /** Returns remaining number of bytes in the buffer */    
    public int available() {
	     return (count - pos);
    }

    /** Closes this InputStream and resets system input */    
    public void close() {
        if (in == null) return;
        System.setIn(in);
        in = null;
    }
    
    /** Indicates if this InputStream is closed */    
    public boolean isClosed() {
        return (in == null);
    }

    /** Returns false */    
    public boolean markSupported() {
        return false;
    }

    /** Sets message to be used in subsequent read operations. <BR>
     * Default is "Input String is requested"
     */    
    public void setMessage(String s) {
        if (in != null) msg = s;
    }
}
