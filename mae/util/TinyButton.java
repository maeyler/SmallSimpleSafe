/*
 * TinyButton.java
 * Created on June 8, 2003, 10:55 AM
 */

package mae.util;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.BorderFactory;

/**
 * @version 1.0
 */
public class TinyButton extends JButton {

    /** Creates new TinyButton */
    public TinyButton() {
      this("");
    }

    public TinyButton(String name) {
      super(name);
      tinyButton();
    }

    void tinyButton() {
      setBackground(Color.lightGray);
      setFont(new Font("SansSerif", 0, 11));
      setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createRaisedBevelBorder(),
         BorderFactory.createEmptyBorder(0, 6, 0, 6)
      ));
    }
}
