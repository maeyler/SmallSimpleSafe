/*
 * PropertyManager.java
 * Created on July 14, 2003, 2:39 PM
 */

package mae.util;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.awt.Font;
import java.awt.Color;
import java.awt.Rectangle;
import javax.swing.ImageIcon;

/** Loads and saves Properties, with support for Color, Font, Bounds.
 * <P>
 * <OL>Three levels are allowed:
 * <LI>A file in the directory where the VM is started
 * (depends on the IDE) ie, new File(".")  <BR>
 * When this file is empty, level two is used
 * <LI>A read/only file (resource) in the directory where this class
 *  is located (this could be in a jar file)
 * <LI>Defaults coded in the program
 * </OL>
 * We try to find properties in this order on construction.<BR>
 * When saved, currnt values of properties are written to 
 * the first file above.
 * 
 * @author Eyler
 */
public class PropertyManager extends Properties {
    
    /** First level Properties object (read/write) */    
//    protected Properties prop;
    /** Second level Properties object (defaults: read/only) */    
//    protected Properties defa;
    String fold;  //mae
    String pack;  //util
    File path;    //mae/util.properties at user.home
    /** Default extension used in all property files */    
    public static final String EXT = ".properties";
    
    /** Returns an instance, using file p.EXT
     * in the directory where VM is started
     * with no defaults.
     */    
    public PropertyManager(String p) {
        this(null, p, null);
    }
    /** Returns an instance, using file p.EXT
     * in the directory where VM is started, with 
     * defaults together with caller class.
     */    
    public PropertyManager(String p, Class caller) {
        this(null, p, caller);
    }
    /** Returns an instance, using file f/p.EXT
     * in the home directory of the user. <P>
     * Caller class is needed to locate resource file.
     */    
    public PropertyManager(String f, String p, Class caller) {
        fold = f; pack = p; 
        defaults = new Properties();
        String name = p + EXT;
        File dir = (f == null)? new File("").getAbsoluteFile() :  
                 new File(System.getProperty("user.home"), f);
        dir = dir.getAbsoluteFile();
        if (!dir.exists()) dir.mkdir();
        path = new File(dir, name);
        try {
           InputStream in;
           if (caller != null) {
               in = caller.getResourceAsStream(name);
               if (in != null) {
           //System.err.println("Default Properties: "+name+"  "+in.available());
                   defaults.load(in); in.close();
               }
           }
           if (!path.exists()) return;
           in = new FileInputStream(path);
           //System.err.println("Read Properties:    "+path+"  "+in.available());
           load(in); in.close();
        } catch (IOException x) { 
           System.err.println(x.getMessage());
        } 
    }

    /** Stores first level Properties object, overrides store()  */
    public void store(OutputStream os, String head) throws IOException {
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        super.store(ba, head);
        byte[] buf = ba.toByteArray();
        ba.close();
        BufferedReader in = new BufferedReader(
           new StringReader(new String(buf))
        );
        String s;
        List L = new ArrayList();
        while ((s = in.readLine()) != null) L.add(s); 
        in.close();
        Collections.sort(L);
        int n = L.size();
        PrintWriter out = new PrintWriter(os);
        for (int i=0; i<n; i++) 
           out.println(L.get(i));
        out.flush();
        n = n-2;  //two lines are extra
        System.err.println("Save "+n+" Properties: "+path+"  "+buf.length);
    }
    /** Saves first level Properties object, with one-line comment desc */ 
    public void save(String desc) {
        try {
           OutputStream out = new FileOutputStream(path);
           Properties p = (size()>0)? this : defaults;
           p.store(out, desc);
           out.close();
        } catch (IOException x) { 
           throw new RuntimeException(x.getMessage());
        } 
    }

    /** Lists first level Properties object on System output */    
    public void list() {
        Properties p = (size()>0)? this : defaults;
        p.list(System.out);
    }
    /** Returns an integer property with name d, default def */    
    public int getInteger(String d, int def) {
        try {
            return Integer.parseInt(getProperty(d, ""+def));
        } catch (Exception x) { 
            System.err.println("getInteger "+x);
            return def;  //default value
        } 
    }
    /** Sets a Color property with name d, value c */    
    public void setColor(String d, Color c) {
        String val = Integer.toHexString(c.getRGB());
        setProperty(d, "0x"+val);  //hex notation
    }
    /** Returns a Color property with name d, default def */    
    public Color getColor(String d, Color def) {
        try {
            Long rgb = Long.decode(getProperty(d));
            return new Color(rgb.intValue());
        } catch (Exception e) { 
            return def;  //default value
        } 
    }
    /** Sets a Rectangle property with name d, value r */    
    public void setBounds(String d, Rectangle r) {
        setProperty(d+".x", ""+r.x);
        setProperty(d+".y", ""+r.y);
        setProperty(d+".w", ""+r.width);
        setProperty(d+".h", ""+r.height);
    }
    /** Returns a Rectangle property with name d, default rect */
    public Rectangle getBounds(String d, int x, int y, int w, int h) {
        return new Rectangle(
            getInteger(d+".x", x), getInteger(d+".y", y),
            getInteger(d+".w", w), getInteger(d+".h", h)
        );
    }
    /** Returns a Rectangle property with name d, default width & height */
    public Rectangle getBounds(String d, int w, int h) {
        return getBounds(d, 0, 0, w, h);
    }
    /** Sets a Font property with name d, value f */    
    public void setFont(String d, Font f) {
        setProperty(d+".name", f.getName());
        setProperty(d+".style", ""+f.getStyle());
        setProperty(d+".size",  ""+f.getSize());
    }
    /** Returns a Font property with name d, default def */    
    public Font getFont(String d, Font def) {
        return new Font(
            getProperty(d+".name", def.getName()),
            getInteger(d+".style", def.getStyle()), 
            getInteger(d+".size",  def.getSize())
        );
    }
    /** Sets Icon for f, using image with name s */    
    public static boolean setIcon(javax.swing.JFrame f, String s) {
        URL u = f.getClass().getResource(s);
        return (u == null)? false : setIcon(f, u);
    }
    /** Sets Icon for f, using image at given URL */    
    public static boolean setIcon(javax.swing.JFrame f, URL u) {
        ImageIcon img = new ImageIcon(u);
        if (img.getIconWidth() <= 0) return false;
        f.setIconImage(img.getImage()); 
        //System.err.println("image icon: "+u);   removed V1.65
        return true;
    }
}
