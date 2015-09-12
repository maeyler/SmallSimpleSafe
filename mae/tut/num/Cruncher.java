package mae.tut.num;

/** Defines static methods to calculate several functions.
 * <P>
 * Greatest Common Divisor and Exponential functions are included.
 *
 * @author Eyler
 * @version 2.0
 */
public class Cruncher {
    
    /** Complex i */
    public static final Number i = new Complex(0, 1);
    /** Decimal e */
    public static final Number e = new Decimal((float)Math.E);
    /** Maximum number of iterations used in exp() */    
    static final int MAX = 20;
    /** A small value to stop evaluation used in exp() */    
    static final double EPS = 1E-7;

    Cruncher() {} //in order to exclude from JavaDoc
    /** String representation of this Object */   
    public String toString() { return " (singleton)"; }
   
    /** Raises Number n to e (power series approximation) */    
    public Number expPS(Number n) {
       float x = n.value();
       int k = 0;
       Number t = new Whole(1);
       Number s = new Whole(1);
       while (k<MAX && EPS<Math.abs(t.value()/x)) {
          k++; 
          t = t.mult(n).mult(new Rational(1, k));
          s = s.add(t);
       }
       return s;
    }
    /** Raises Number n to e  (uses Math class) */    
    public Number exp(Number n) {
       Complex z = Complex.fromNumber(n);
       float r = (float)(Math.exp(z.re));
       float a = (float)(r * Math.cos(z.im));
       float b = (float)(r * Math.sin(z.im));
       return Factory.newComplex(a, b);
    }
    /** Inverse of exp: log of n to base e  (Works with any subclass) */    
    public Number log(Number n) {
       Complex z = Complex.fromNumber(n);
       float a = (float)(z.angle()/180*Math.PI);
       return Factory.newComplex(log(z.value()), a);
    }
    /** Square root of n (Works with any subclass) */    
    public Number sqrt(Number n) {
       Complex z = Complex.fromNumber(n);
       return Complex.fromPolar(sqrt(z.value()), z.angle()/2);
    }
    /** Power to the a of x (Works with any subclass) */    
    public Number pow(Number n, Number a) {
       return exp(log(n).mult(a));
    }
    
    /** Raises argument x to e approximately.
     * (Should return a value close to Math.exp(x))
     */    
    public static float expPS(float x) {
       int k = 0;
       float t = 1;
       float s = 1;
       while (k<MAX && EPS<Math.abs(t/x)) {
          k++; 
          t = t * x / k;
          s = s + t;
       }
       return s;
    }
    /** Raises argument x to e (calls Math.log(x)) */
    public static float exp(float x) {
       return (float)Math.exp(x);
    }
    /** Inverse of exp: log of x to base e (calls Math.log(x)) */    
    public static float log(float x) {
       return (float)Math.log(x);
    }
    /** Square root of x (calls Math.sqrt(x)) */    
    public static float sqrt(float x) {
       return (float)Math.sqrt(x);
    }
    /** Power to the a of x (calls Math.pow(x, a)) */    
    public static float pow(float x, float a) {
       return (float)Math.pow(x, a);
    }

    /** Returns the greatest common divisor of the arguments.
     * <P>
     * Invokes static method Rational.gcd().
     */    
    public static long gcd(long a, long b) {
        return Rational.gcd(a, b);
    }
}
