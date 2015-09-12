package mae.tut.fun;

/** Represents a  implemented as
 * <P>
 * @author Eyler
 * @version 1.2
 */
public class Constant extends Polynomial {
    
    /** Creates a new instance of Constant */
   public Constant(float a) {
      super(new float[] {a}); 
   }
   public String toString() { 
      return numToStr(c[0]);
   }
}
