/*
 * TextAreaPrintStream.java
 *
 * Created on July 23, 2003, 2:18 PM
 */

package mae.util;

import java.io.PrintStream;
import javax.swing.JTextArea;
//import javax.swing.SwingUtilities;

/** Redirects system output to a JTextArea.
 * <PRE>
 * Usage:
 * JTextArea text = ...;
 * TextAreaPrintStream out = TextAreaPrintStream.redirect(text);
 * ...  //System.out.print();
 * ...  //System.out.println();
 * out.close();
 * </PRE>
 * @author Eyler
 */
public class TextAreaPrintStream extends PrintStream {
    
    JTextArea txt;
    
    /** Creates a new instance of TextAreaPrintStream. */
    TextAreaPrintStream(JTextArea t) {
        super(System.out, false);  //out = System.out
        System.setOut(this);
        txt = t;
    }
    
    /** Makes a new instance and redirects system output */    
    public static TextAreaPrintStream redirect(JTextArea t) {
        TextAreaPrintStream ps = new TextAreaPrintStream(t);
        return ps;
    }
    
    /** Appends a String into the JTextArea */    
    void write(String x) {
       txt.append(x);
       int n = txt.getText().length();
       txt.select(n, n);
//       SwingUtilities.getWindowAncestor(txt).setVisible(true);
    }

    /** Appends (char)b into the JTextArea */    
    public void write(int b) {
        write(String.valueOf((char)b));
    }
    
    /** Appends a String into the JTextArea */    
    public void write(byte[] buf, int off, int len) {
        write(new String(buf, off, len));
    }
    
    /** Empty method, it does nothing */    
    public void flush() {
    }

    /** Closes this PrintStream and resets system output */    
    public void close() {
        if (out == null) return;
        System.setOut((PrintStream)out);
        txt = null; out = null;
    }
    
    /** Indicates if this PrintStream is closed */    
    public boolean isClosed() {
        return (out == null);
    }
}
