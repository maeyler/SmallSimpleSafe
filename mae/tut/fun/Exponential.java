package mae.tut.fun;

/**
 *
 * @author  unknown
 */
public class Exponential extends Function {
    
   protected  float a, k;

   /** Creates a new instance of Expon */
   public Exponential(float a, float k) { 
      this.a = a; this.k = k;
   }
   public float value(float x) { 
      return a*(float)Math.exp(k*x);
   }
   public float deriv(float x) { 
      return k*value(x);
   }
   public String toString() { 
      return term(a,"exp("+coef(k)+"x)");
   }
}
