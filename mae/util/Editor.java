package mae.util;

import java.io.File;
import java.awt.Frame;

public interface Editor {

   void open(File f);
   void startRunning();
   void stopRunning(String s);
   void setMessage(String s);
   void setMessage(Throwable x);
   void setMessage(String s, boolean OK);
   void select(int i, int j);
   String getText();
   Frame getFrame();
   PropertyManager propertyManager();
}

