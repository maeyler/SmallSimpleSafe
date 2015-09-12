//johnbokma.com/java/obtaining-image-metadata.html
//stackoverflow.com/questions/13559551/get-date-taken-of-an-image
//stackoverflow.com/questions/16115851/read-image-metadata-from-single-file-with-java
//forums.oracle.com/thread/1265339
//modified Sep 2013

package mae.brow;

import org.w3c.dom.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class Metadata {

    static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
    File file;
    IIOMetadata metadata;
    String date;
    long time;
    boolean intelOrder = false;
    ArrayList<Entry> ed;
    byte[] data;

    public Metadata(File f) throws IOException, ParseException { 
        if (!f.exists()) 
           throw new RuntimeException("File not found: " + file.getName()); 
        file = f;            
        ImageInputStream iis = ImageIO.createImageInputStream(f);
        Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

        // pick the first available ImageReader
        ImageReader reader = readers.next();
        // attach source to the reader
        reader.setInput(iis, true);
        // read metadata of first image
        metadata = reader.getImageMetadata(0);
        date = ""; time = -1;
        parseMetadata(false);
        iis.close(); //IMPORTANT!!
        if (date.length() > 0) time = SDF.parse(date).getTime(); 
        //else throw new RuntimeException("Date field not found");;
    }
    public Entry[] exifEntries() {
        Entry[] a = new Entry[ed.size()];
        ed.toArray(a); Arrays.sort(a);
        return a;
    }
    public IIOMetadata getMetadata() { return metadata; }
    public String getDate() { return date; }
    public long getTime() { return time; }
    public boolean correctTime() { 
        //accept time equality within 1000 msec
        return Math.abs(time - file.lastModified()) <= 1000;
    }
    public void parseMetadata(boolean TRACE) {
        data = null;
        String name = metadata.getNativeMetadataFormatName();
        displayMetadata(TRACE, metadata.getAsTree(name), 0);
    }
    void indent(int level, String msg) {
        for (int i = 0; i < level; i++)
            System.out.print("    ");
        System.out.print(msg);
    }
    void displayMetadata(boolean TRACE, Node node, int level) {
        if (TRACE) indent(level, "<" + node.getNodeName());
        NamedNodeMap map = node.getAttributes();
        if (map == null) return;
        // print attribute values
        int length = map.getLength();
        for (int i = 0; i < length; i++) {
                Node attr = map.item(i);
                String nam = attr.getNodeName();
                String val = attr.getNodeValue();
                if (TRACE) System.out.print(" " + nam + "=\"" + val + "\"");
                if (nam.equals("MarkerTag") && val.equals("225") && data == null) {
                    IIOMetadataNode iin = (IIOMetadataNode)node;
                    data = (byte[])iin.getUserObject();
                    try {
                        parseExif(false);
                        if (TRACE) System.out.print(" date=\"" + date + "\"");
                    } catch (Exception e) {
                        if (TRACE) System.out.print(" error=" +e.getMessage());
                    }
                }
        }
        Node child = node.getFirstChild();
        if (child == null) {
            // no children, so close element and return
            if (TRACE) System.out.println("/>");
            return;
        }
        // children, so close current tag
        if (TRACE) System.out.println(">");
        while (child != null) {
            // print children recursively
            displayMetadata(TRACE, child, level + 1);
            child = child.getNextSibling();
        }
        if (TRACE) indent(level, "</" + node.getNodeName() + ">\n");
    }
//******************************************************************************
//**  simplified from MetadataParser by (c) 2003 Norman Walsh
//**  from   www.javaxt.com/javaxt-core/javaxt.io.Image/
//******************************************************************************
    public void parseExif(boolean TRACE) {
        ed = new ArrayList<Entry>();
        if (TRACE) System.out.println(data.length+" bytes");
        String exif = new String(data, 0, 4);
        if (!"Exif".equals(exif)) error("Not proper EXIF data");

            if (data[6] == 'I' && data[7] == 'I') {
                intelOrder = true;
            } else if (data[6] == 'M' && data[7] == 'M') {
                intelOrder = false;
            } else {
                error("Incorrect byte order in EXIF data");
            }

        int checkValue = get16u(intelOrder, data, 8);
        if (checkValue != 0x2a) error("Check value fails: "+ checkValue);

        int base = 6;
        int start = get32u(intelOrder, data, 10);
        parseExif(TRACE, base + start, base);
        //return ed;
    }
    void parseExif(boolean TRACE, int dirStart, int offsetBase) {
        if (dirStart >= data.length) error("Wrong dirStart: "+ dirStart);
        int n = get16u(intelOrder, data, dirStart);
        if (TRACE) System.out.println(n+" entries");
        //ed = new Entry[n];
        for (int k = 0; k < n; k++) {
            int dirOffset = dirStart + 2 + (12 * k);
            int tag = get16u(intelOrder, data, dirOffset);
            if (tag == 0x8769 || tag == 0xa005) { //INTEROP_OFFSET
                int s = get32u(intelOrder, data, dirOffset + 8);
                parseExif(TRACE, offsetBase + s, offsetBase);
                continue;
            }
            int fmt = get16u(intelOrder, data, dirOffset + 2);
            int len = get32u(intelOrder, data, dirOffset + 4);
            int valOffset = dirOffset + 8;
            if (len > 4 || fmt == 5 || fmt == 10)
                valOffset = offsetBase + get32u(intelOrder, data, dirOffset + 8);
            String val = getValue(intelOrder, data, fmt, valOffset, len);
            if (tag == 0x0132 || tag == 0x9003) date = val; //DATE_TAKEN
            Entry ent = new Entry(tag, val);
            ed.add(ent);
            if (TRACE) 
                System.out.printf("%2s %3s %s %n", k, fmt, ent);
        }
    }

  //**************************************************************************
  //** get numeric data
  //**************************************************************************
    public static int get16u(boolean intel, byte[] d, int k) {
        int hi, lo;
        if (intel) {
            hi = d[k + 1];
            lo = d[k];
        } else {
            hi = d[k];
            lo = d[k + 1];
        }
        lo = lo & 0xFF;
        hi = hi & 0xFF;
        return ((hi << 8) + lo) & 0xFFFF;
    }
    public static int get32u(boolean intel, byte[] d, int k) {
        int n1, n2, n3, n4;
        if (intel) {
            n1 = d[k + 3] & 0xFF;
            n2 = d[k + 2] & 0xFF;
            n3 = d[k + 1] & 0xFF;
            n4 = d[k] & 0xFF;
        } else {
            n1 = d[k] & 0xFF;
            n2 = d[k + 1] & 0xFF;
            n3 = d[k + 2] & 0xFF;
            n4 = d[k + 3] & 0xFF;
        }
        return (n1 << 24) + (n2 << 16) + (n3 << 8) + n4;
    }
    static String getValue(boolean intel, byte[] d, int format, int k, int len) {
                switch (format) {
                case 2: //FMT_STRING:
                    return new String(d, k, len-1); //, "UTF-8");
                case 6: //FMT_SBYTE:
                    return ""+d[k];
                case 1: //FMT_BYTE:
                    return ""+(d[k] & 0xFF);
                case 8: //FMT_SSHORT:
                case 3: //FMT_USHORT:
                    return ""+get16u(intel, d, k);
                case 9: //FMT_SLONG:
                case 4: //FMT_ULONG:
                    return ""+get32u(intel, d, k);
                case 5: //FMT_URATIONAL:
                case 10: //FMT_SRATIONAL:
                    return getRational(intel, d, k);
                default: 
                    return "FMT_UNDEFINED";  
                    //Arrays.copyOfRange(d, k, len);
                }
    }
    static String getRational(boolean intel, byte[] d, int offset) {
        int num = get32u(intel, d, offset);
        int den = get32u(intel, d, offset + 4);
        if (den == 1) {
            return "" + num; 
        } else {
            return num + "/" + den;
        }
    }
    
    class Entry implements Comparable<Entry> {
        int tag; String value;
        public Entry(int t, String v) {
            tag = t; value = v;
        }
        public int compareTo(Entry t) {
            return tag - t.tag;
        }
        public int hashCode() {
            return tag;
        }
        public boolean equals(Object t) {
            if (t instanceof Entry) 
                return compareTo((Entry)t) == 0;
            return false;
        }
        public String toString() {
            return tag+" = "+value;
        }
    }

    static void error(String msg) {
        throw new RuntimeException(msg);
    }
    public static void main(String[] args) throws IOException, ParseException {
        Metadata md = new Metadata(new File("photo.JPG"));
        System.out.println(md.date);
    }
}
