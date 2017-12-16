//Aug 2004 

package mae.sss;

import java.io.IOException;
import java.util.List;
import java.net.URL;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.JList;

public class Dropper implements DropTargetListener {
   
   Inspector ins; InspectorPanel pan;  
   JList lst; Point loc; int objCount;
   static final String 
    PREFIX = "t",  //used for default id
    FAILED = "Failed to accept any data flavor";
   static final Color DROP = Color.blue;
   static final Stroke THICK = new BasicStroke(2);
   
   public Dropper(Inspector i, JList j) {
      ins = i; pan = ins.panel; lst = j;
      objCount = Inspector.BASE;
      DropTarget dt = new DropTarget(j, this);
      
      KeyStroke p = KeyStroke.getKeyStroke("control V");
      Object key = j.getInputMap().get(p); //--> 
      if (key == null) key = "paste";
      j.getActionMap().put(key, new Paste());
   }
   public void drawBox(Color c) {
      if (loc == null)  //do just once
          loc = pan.drop.getParent().getLocation();
      Graphics2D g = (Graphics2D)pan.getGraphics();
      g.setColor(c); g.setStroke(THICK);
      Rectangle r = pan.drop.getBounds();
      g.drawRect(loc.x-1, loc.y-1, r.width+1, r.height+1);
   }
   public void dragEnter(DropTargetDragEvent dtde) {
      DataFlavor[] a = dtde.getCurrentDataFlavors();
      if (bestFlavor(a) == null) 
         dtde.rejectDrag();
      else {
         drawBox(DROP); 
         dtde.acceptDrag(DnDConstants.ACTION_COPY);
         pan.setMessage(a.length+" flavors");
      }
   }
   public void dragExit(DropTargetEvent dte)  {
      drawBox(pan.getBackground());
   }
   public void dragOver(DropTargetDragEvent dtde)  {
   }
   public void dropActionChanged(DropTargetDragEvent dtde)  {
      drawBox(pan.getBackground());
   }
   public void drop(DropTargetDropEvent dtde)  {
      drawBox(pan.getBackground());
      //dtde.acceptDrop(dtde.getDropAction());
      dtde.acceptDrop(DnDConstants.ACTION_COPY);
      if (display(dtde.getTransferable())) 
         dtde.dropComplete(true);
      else {
         dtde.dropComplete(false);
         pan.setMessage(FAILED);
      }
   }
   public boolean isAccepted(DataFlavor f) {
      if (f.getSubType().equals("html")) return false;
      Class c = f.getRepresentationClass();
      return (c == String.class || c == List.class 
           || c == Image.class || c == URL.class);
   }
   public DataFlavor bestFlavor(DataFlavor[] a) {
      for (int i=0; i<a.length; i++) 
         if (isAccepted(a[i])) return a[i];
      return null;
   }
   public boolean display(Transferable t) {
      if (display(DataFlavor.imageFlavor, t)) return true;
      if (display(DataFlavor.javaFileListFlavor, t)) return true;
      //quietly go on with all flavors in t
      DataFlavor[] a = t.getTransferDataFlavors();
      for (int i=0; i<a.length; i++) 
         if (isAccepted(a[i]) && display(a[i], t)) 
               return true;
      return false;
   }
   boolean display(DataFlavor f, Transferable t) {
      try {
          Object x = t.getTransferData(f);     
          if (x instanceof List) {
             List L = (List)x;
             if (L.size() == 1) x = L.get(0);
             else x = L.toArray();
          }
          String id = PREFIX+objCount; objCount++;
          ins.addObject(x, id);
          ins.inspectObject(x);
          System.out.println(id+": "+f.getMimeType()); 
          return true;
      } catch (IOException ex) { 
          return false;
      } catch (UnsupportedFlavorException ex) { 
          return false;
      }
   }

   class Paste extends AbstractAction {
      public Paste() { super("Paste"); }
      public void actionPerformed(ActionEvent e) {
         Clipboard clip =
            lst.getToolkit().getSystemClipboard();
         if (!display(clip.getContents(null))) 
            pan.setMessage(FAILED);
      }
   }
}
