package mae.tut.num;

/** Represents a 2x2 matrix, implemented as (a b : c d).
 * <PRE>
 *  a  b
 *  c  d
 * </PRE>
 * Matrices can be compared for equality, but
 * order is not defined.
 *
 * @author Eyler
 * @version 2.0
 */
public class Matrix extends Number {
    
    /** Coefficients */    
    float a, b, c, d;
    
    /** Use factory method {@link mae.tut.num.Factory#newMatrix(float,float,float,float)},
     * rather than this constructor
     */    
    public Matrix(float x, float y, float z, float t) {
        a = x; b = y; c = z; d = t;
    }

    public Number add(Number n) {
        Matrix m = fromNumber(n);
        return new Matrix(a+m.a, b+m.b, c+m.c, d+m.d);
    }
    public Number mult(Number n) {
        Matrix m = fromNumber(n);
        float x = a*m.a + b*m.c;
        float y = a*m.b + b*m.d;
        float z = c*m.a + d*m.c;
        float t = c*m.b + d*m.d;
        return new Matrix(x, y, z, t);
    }
    public Number inverse() {
        float x = determinant();
        return new Matrix(d/x, -b/x, -c/x, a/x);
    }
    /** Returns square root of the determinant of this Matrix */   
    public float value() {
        return (float)Math.sqrt(Math.abs(determinant()));
    }
    /** Returns the determinant of this Matrix */   
    public float determinant() {
        return (a*d - b*c);
    }
    /** String representation of this Number
     * as an array of four floats
     */   
    public String toString() {
        return "("+a+", "+b+" : "+c+", "+d+")";
    }
    /** Compares this Number to another Object for equality.
     * <P>
     * The result is true if and only if the argument (not null)
     * is a Matrix with the same coefficients <BR> OR this Matrix
     * happens to be diagonal with the same value as x.
     */    
    public boolean equals(Object x) {
        if (x instanceof Number) {
            Matrix m = fromNumber((Number)x);
            return (a==m.a && b==m.b && c==m.c && d==m.d);
        } else {
            return false;
        }
    }

    /** Converts Number n into Matrix, if not already Matrix */    
    public static Matrix fromNumber(Number n) {
        float x = n.value();
        if (n instanceof Matrix) return (Matrix)n;
        else return new Matrix(x, 0, 0, x);    
    }
}
