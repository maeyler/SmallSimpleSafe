//separated from Inspector V1.65
package mae.util;
import java.io.*; 
import java.util.Date;

public class Reporter {
	static final String FILE = "SSS.exception";
	private Reporter() {
	}
	public static void append(Throwable t) {
		System.err.println("Exception reported to " + FILE);
		try {
			OutputStream os = new FileOutputStream(FILE, true);
			PrintStream out = new PrintStream(new PrintStream(os));
			append(out, t);  //V1.65
			out.flush();
			out.close();
		} catch (Exception x) {
			System.err.println(x);
		}
	}
	public static void clearFile() {
		File f = new File(FILE);
		if (f.exists())
			f.delete();
	}
    public static void append(PrintStream out, Throwable t) {  //V1.65
        out.println("on "+new Date());
        out.println(t.getClass().getName());
        out.println(t.getMessage());
        printStackTrace(out, t.getStackTrace());
        out.println();
    }
    public static void printStackTrace(PrintStream out, StackTraceElement[] st) {
        boolean stop = false;
        for (StackTraceElement e: st) {  //rewritten V1.65
           String s = e.toString();
           if (e.getLineNumber()>0) 
               stop = true;
           else if (stop) return;
           out.println('\t'+s); 
        }
    }
    public static void printStackTrace(PrintStream out, String s) {
        out.println(s);
        printStackTrace(out, Thread.currentThread().getStackTrace());
    }
}

