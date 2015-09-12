package mae.tut.fun;

/** Represents a  implemented as
 * <P>
 * @author Eyler
 * @version 1.2
 */
public class Polynomial extends Function  {
   protected int n; 
   protected float[] c;
   public Polynomial(float[] c) { 
      n = c.length; this.c = c; 
   }
   public float value(float x) { 
      float y = 0;
      for (int i=n-1; i>=0; i--) y = y*x + c[i];
      return y; 
   }
   public float deriv(float x) { 
      float y = 0;
      for (int i=n-1; i>0; i--) y = y*x + i*c[i];
      return y; 
   }
   public Polynomial add(Polynomial p) {
      int m = Math.max(n, p.n);
      float[] a = new float[m];
      for (int i=0; i<m; i++) 
         a[i] = (i<n? c[i]: 0) + (i<p.n? p.c[i]: 0);
      return new Polynomial(a);
   }
   public Polynomial mult(Polynomial p) {
      float[] a = new float[n+p.n-1];
      for (int i=0; i<n; i++) 
         for (int j=0; j<p.n; j++) 
            a[i+j] += c[i]*p.c[j];
      return new Polynomial(a);
   }
   public String toString() { 
      String s = "";
      for (int i=n-1; i>1; i--) 
         s += term(c[i],"x^"+i+" ");
      s += term(c[1],"x ");
      return s + lastTerm(c[0]);
   }
}
