package mae.tut.num;

/** Represents a complex number, implemented as x+iy.
 * <P>
 * Where x is the real part, y is the imaginary part.
 * <P>
 * Complex numbers can be compared for equality, but
 * order is not defined.
 * @author Eyler
 * @version 2.0
 */
public class Complex extends Number {
    
    /** Real part of this Number */    
    float re;
    /** Imaginary part of this Number */    
    float im;
    
    /** Use factory method {@link mae.tut.num.Factory#newComplex(float,float)},
     * rather than this constructor
     */    
    public Complex(float r, float i) {
        re = r; im = i;
    }

    public Number add(Number n) {
        Complex c = fromNumber(n);
        return Factory.newComplex(re + c.re, im + c.im);
    }
    public Number mult(Number n) {
        Complex c = fromNumber(n);
        return Factory.newComplex(re*c.re - im*c.im, re*c.im + im*c.re);
    }
    public Number inverse() {
        float d = re*re + im*im;
        return Factory.newComplex(re/d, -im/d);
    }
    /** Distance to origin (rho) */   
    public float value() {
        return (float)Math.sqrt(re*re + im*im);
    }
    /** Theta angle of polar coordinates,
     * in the range of -90 through 270 degrees
     */   
    public float angle() {
        final double eps = 1E-10; 
        double a;
        if (Math.abs(re) < eps) {
            a = (im < -eps? -90 : 90);
        } else {
            a = Math.atan(im/re)*180/Math.PI;
            if (re < -eps) a = a +180;
        }
        return (float)a;
    }
    /** String representation of this Number
     * in polar coordinates
     */   
    public String toPolar() {
        return "r:"+value()+"  a:"+angle();
    }
    /** String representation of this Number
     * in cartesian coordinates
     */   
    public String toString() {
        return "("+re+", "+im+")";
    }
    /** Compares this Number to another Object for equality.
     * <P>
     * The result is true if and only if the argument (not null)
     * is a Complex with the same field values <BR> OR this Number
     * happens to be real with the same value as x.
     */    
    public boolean equals(Object x) {
        if (x instanceof Number) {
            Complex c = fromNumber((Number)x);
            return (re==c.re && im==c.im);
        } else {
            return false;
        }
    }

    /** Makes a (Complex) Number from polar coordinates
     */   
    public static Number fromPolar(float r, float a) {
        double d = a/180*Math.PI;
        float x = r * (float)Math.cos(d);
        float y = r * (float)Math.sin(d);
        return Factory.newComplex(x, y);
    }
    /** Converts Number n into Complex, if not already Complex */    
    public static Complex fromNumber(Number n) {
        if (n instanceof Complex) return (Complex)n;
        else return new Complex(n.value(), 0);    
    }
}
