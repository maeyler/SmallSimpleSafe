package mae.tut.num;

/** Represents a rational number, implemented as num/den.
 * <P>
 * <P>
 * @author Eyler
 * @version 2.0
 */
public class Rational extends Ordinal {
    
    /** Numerator of this Number */    
    long num;
    /** Denominator of this Number.
     * <P>
     * Must be equal to 1 for Whole
     */    
    long den;
    
    /** Use factory method {@link mae.tut.num.Factory#newRational(long,long)}, 
     * rather than this constructor
     */    
    public Rational(long n, long d) {
        if (d == 0) {
            String msg = "Zero denominator";
            throw new IllegalArgumentException(msg);
        }
        num = n;  den = d; 
    }
    public Number add(Number n) {
        if (n instanceof Rational) {
            Rational r = (Rational)n;
            return Factory.newRational(num*r.den + r.num*den, den*r.den);
        } else {
            return n.add(this);
        }
    }
    public Number mult(Number n) {
        if (n instanceof Rational) {
            Rational r = (Rational)n;
            return Factory.newRational(num * r.num, den * r.den);
        } else {
            return n.mult(this);
        }
    }
    public Number inverse() {
        return Factory.newRational(den, num);
    }
    public float value() {
        return num/(float)den;
    }
    /** String representation of this Number */   
    public String toString() {
        return num+"/"+den;
    }
    /** Returns the greatest common divisor of the arguments.
     * <P>
     * Absolute values of the arguments are used.
     * If either argument is zero, it returns 1.
     */    
    public static long gcd(long a, long b) {
        if (a < 0) a = -a;
        if (b < 0) b = -b;
        while (a!=0 && b!=0) 
            if (a < b) b = b % a;
            else a = a % b;
        if (a == 0) a = b;
        if (a == 0) return 1;
        return a;
    }
}
