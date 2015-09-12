package mae.tut.num;

/** The abstract superclass of Comparable numbers:
 * Whole, Rational, Decimal.
 * <P>
 * Both equality and order are based upon value(), independent of type.
 *  
 * <P>
 * @author Eyler
 * @version 2.0
 */
public abstract class Ordinal extends Number implements Comparable {
    
   Ordinal() {} //in order to exclude from JavaDoc

    /** Compares this Number to another Object for order.
     * <P>
     * If the Object is an Ordinal, returns -1, 0, or +1 
     * depending on the values. <BR> 
     * Otherwise, it throws a ClassCastException.
     * @throws  ClassCastException
     * @throws  NullPointerException
     */    
    public int compareTo(Object x) {
        float v = value();
        float n = ((Ordinal)x).value();
        if (v == n) return 0;
        else if (v < n) return -1;
        else return 1;
    }
    /** Compares this Number to another Object for equality.
     * <P>
     * The result is true if and only if the argument (not null)
     * is an Ordinal with the same value <BR> OR
     * Object x is a Number equal to this Number.
     */    
    public boolean equals(Object x) {
        if (x instanceof Ordinal) {
            return (value() == ((Ordinal)x).value());
        } else if (x instanceof Number) {
            return x.equals(this);
        } else {
            return false;
        }
    }
}
