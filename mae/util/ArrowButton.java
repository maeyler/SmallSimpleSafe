/**
 * Nov 05, 2006  from BasicArrowButton
 */

package mae.util;

import java.awt.Dimension;
import javax.swing.plaf.basic.BasicArrowButton;

/**
 *
 * @author  Eyler
 */
public class ArrowButton extends BasicArrowButton {
    Dimension pref;
    public ArrowButton(int direction) { 
        this(direction, 16, 18);
    }
    public ArrowButton(int direction, int w, int h) { 
        super(direction); 
        setPreferredSize(new Dimension(w, h));
    }
    public void setPreferredSize(Dimension d) {
        pref = d; super.setPreferredSize(d); 
    }
    public Dimension getPreferredSize() {
        return pref;
    }
}
