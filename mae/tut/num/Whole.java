package mae.tut.num;

/** Represents an integer, implemented as a rational with den=1.
 * <P>
 * This is the most specific subclass of Number.
 * <P>
 * @author Eyler
 * @version 2.0
 */
public class Whole extends Rational {
    
    /** Use factory method {@link mae.tut.num.Factory#newWhole(long)}, 
     * rather than this constructor
     */    
    public Whole(long n) { super(n, 1); }
    public Number add(Number n) {
        if (n instanceof Whole) {
            Whole w = (Whole)n;
            return new Whole(num + w.num);
        } else {
            return n.add(this);
        }
    }
    public Number mult(Number n) {
        if (n instanceof Whole) {
            Whole w = (Whole)n;
            return new Whole(num * w.num);
        } else {
            return n.mult(this);
        }
    }
    /** String representation of this Number */   
    public String toString() { return num+""; }
}
