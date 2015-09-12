// Eyler  15.5.2012

package mae.util;
import java.net.URL;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

//an ArrayList of lines obtained from a file
public class FileList extends ArrayList<String> {

    public FileList(File f) throws IOException { 
       readData(new FileInputStream(f)); //local file
    }
    public FileList(URL u) throws IOException {
       readData(u.openStream()); //remote file
    }
    public FileList(Class c, String r) throws IOException { 
       if (c == null) c = getClass();
       readData(c.getResourceAsStream(r)); //resource file
    }
    void readData(InputStream in) throws IOException {
       //System.out.println(in.available()+" bytes");
       InputStreamReader rdr = new InputStreamReader(in);
       BufferedReader br = new BufferedReader(rdr);
       int c = 0;
       String s = br.readLine(); 
       while (s != null) {
           add(s);
           c += s.length(); 
           s = br.readLine(); 
       }
       in.close();
       //System.out.println(size()+" lines, "+c+" chars");
    }
}
