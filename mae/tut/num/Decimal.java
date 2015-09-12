package mae.tut.num;

/** Represents a decimal number, implemented as a float.
 * <P>
 * <P>
 * @author Eyler
 * @version 2.0
 */
public class Decimal extends Ordinal {
    
    /** Value of this Number */    
    float num;
    
    /** Use factory method {@link mae.tut.num.Factory#newDecimal(float)}, 
     * rather than this constructor
     */    
    public Decimal(float x) { num = x; }
    public Number add(Number n) {
        if (n instanceof Ordinal) {
            Ordinal r = (Ordinal)n;
            return Factory.newDecimal(num + n.value());
        } else {
            return n.add(this);
        }
    }
    public Number mult(Number n) {
        if (n instanceof Ordinal) {
            Ordinal r = (Ordinal)n;
            return Factory.newDecimal(num * n.value());
        } else {
            return n.mult(this);
        }
    }
    public Number inverse() {
        return Factory.newDecimal(1/num);
    }
    public float value() { return num; }
    /** String representation of this Number */   
    public String toString() { return num+""; }
}
