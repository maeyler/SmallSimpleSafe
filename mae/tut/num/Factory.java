package mae.tut.num;

/** Defines static methods to construct Numbers.
 * <P>
 * Factory methods for constructing Whole, Rational, Decimal, Complex. 
 * <P>
 * These methods are preferred to the constructors, because they 
 * select the proper constructor depending on the arguments.
 *
 * @author Eyler
 * @version 2.0
 */
public class Factory {
    
    static final Cruncher crunch = new Cruncher();

    Factory() {} //in order to exclude from JavaDoc & SSS
   
    /** Returns Cruncher instance for calculation on Numbers */    
    public static Cruncher cruncher() {
        return crunch;
    }
    /** Factory method that constructs a {@link mae.tut.num.Whole}.
     * <P>
     * This is equivalant to <CODE>new Whole(n)</CODE>.
     */
    public static Number newWhole(long n) {
        return new Whole(n);
    }
    /** Factory method that constructs a {@link mae.tut.num.Rational}
     * or a {@link mae.tut.num.Whole}.
     * <P>
     * If <CODE>den==gcd(num,den)</CODE>, 
     * a {@link mae.tut.num.Whole} is returned.
     */    
    public static Number newRational(long num, long den) {
        long g = Rational.gcd(num, den);
        if (g == den) 
             return new Whole(num/g);
        else return new Rational(num/g, den/g);
    }
    /** Factory method that constructs a {@link mae.tut.num.Decimal}
     * or a {@link mae.tut.num.Whole}.
     * <P>
     * If String value of x ends with ".0", x represents an integer,
     * a Whole is returned.
     */    
    public static Number newDecimal(float x) {
        if ((""+x).endsWith(".0")) 
             return new Whole((long)x);
        else return new Decimal(x);
    }
    /** Factory method that constructs a {@link mae.tut.num.Complex},
     * a {@link mae.tut.num.Decimal} or a {@link mae.tut.num.Whole}.
     * <P>
     * If <CODE>im==0</CODE>, newDecimal() is invoked.
     */    
    public static Number newComplex(float re, float im) {
        if (im == 0) 
             return newDecimal(re);
        else return new Complex(re, im);
    }
    /** Factory method that constructs a {@link mae.tut.num.Matrix}. */
    public static Number newMatrix(float a, float b, float c, float d) {
        return new Matrix(a, b, c, d);
    }
}
