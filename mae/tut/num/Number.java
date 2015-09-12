package mae.tut.num;

import java.io.Serializable;

/** The abstract superclass of all numbers:
 * Whole, Rational, Decimal, Complex, Matrix.
 * <P>
 * Subclasses of Number must provide methods to add, multiply
 * by another Number, find the multiplicative inverse,
 * and give the float value.
 * <P>
 * Number objects are immutable: constructed once, never modified.
 * <PRE>
 * V1.0 Jan 12, 2002 Mutable objects
 * V1.1 Jan 26, 2002 exp() generalized
 * V2.0 Jun 30, 2003 NetBeans implementation: immutable
 * V2.1 Dec 30, 2003 Factory class redesigned -> Cruncher
 * </PRE>
 * @author Eyler
 * @version 2.0
 */
public abstract class Number implements Serializable {
    
    Number() {} //in order to exclude from JavaDoc
   
    /** Adds this Number to another Number n and returns the result.
     * <P>
     * If this Number and n are of the same type, returned result
     * must be of that type. <BR>
     * If this and n are of different types, returned result
     * must be as narrow as possible. <BR>
     * But in all cases add(n)
     * and n.add(this) must return the same Number.
     */   
    public abstract Number add(Number n);
    
    /** Multiplies this Number by another Number n and returns the result. 
     * <P>
     * If this Number and n are of the same type, returned result
     * must be of that type. <BR>
     * If this and n are of different types, returned result
     * must be as narrow as possible. <BR>
     * But in all cases mult(n)
     * and n.mult(this) must return the same Number.
     */    
    public abstract Number mult(Number n);
    
    /** Returns the multiplicative inverse of this Number.
     * <P>
     * For any Number n, if inverse() is defined,
     * n.inverse().inverse() must be equal to n. <BR>
     * Also n.inverse().mult(n).value() must be 1.0
     */    
    public abstract Number inverse();

    /** Returns the float value of this Number. */    
    public abstract float value();

    /** hashCode that would be returned by a Float object with the same value.
     * <P>
     * Equal Numbers have the same value, thus the same hashCode.
     * <P>
     * Needed by subclasses that redefine equals(Object)
     *
     * @see mae.tut.num.Ordinal#equals(Object)
     * @see mae.tut.num.Complex#equals(Object)
     */    
    public int hashCode() {
        return new Float(value()).hashCode();
//      return Float.floatToIntBits(value());  actual implementation
    }
    /*public Object clone() {
       try {
          return super.clone();
       } catch (CloneNotSupportedException x) {
          return null;
       }
    }*/
}
