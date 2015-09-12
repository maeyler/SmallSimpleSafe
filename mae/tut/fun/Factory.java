package mae.tut.fun;

/** Defines static methods to construct Functions 
 * and to calculate several functions.
 * <P>
 *
 * @author Eyler
 * @version 1.2
 */
public class Factory {
    
    Factory() {} //in order to exclude from JavaDoc
   
    /** 
     * Interpolates a set of points (xi, f(xi))
     */    
   public static Polynomial interpolate(float[] x, float[] y) {
      Polynomial p = new Constant(0);
      for (int j=0; j<x.length; j++) {
         Polynomial q = new Constant(1);
         for (int i=0; i<x.length; i++) 
            if (i!=j) {
               float d = 1/(x[j]-x[i]);
               float[] a = {-d*x[i], d}; 
               q = q.mult(new Polynomial(a));
            }
         for (int i=0; i<q.n; i++) q.c[i] *= y[j];
         p = p.add(q);
      }
      return p;
   }

    /** Factory method that constructs a {@link mae.fun.Constant}.
     * <P>
     * This is equivalant to <CODE>new Constant(n)</CODE>.
     */
    public static Constant newConstant(float a) {
        return new Constant(a);
    }
}
