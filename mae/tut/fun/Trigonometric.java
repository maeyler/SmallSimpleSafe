package mae.tut.fun;

/**
 *
 * @author  unknown
 */
public class Trigonometric extends Function  {
   protected float a, b;
   public Trigonometric(float a, float b) { 
      this.a = a; this.b = b;
   }
   public float value(float x) { 
      return (float)(a*Math.sin(x)+b*Math.cos(x)); 
   }
   public float deriv(float x) { 
      return (float)(a*Math.cos(x)-b*Math.sin(x)); 
   }
   public String toString() { 
      return term(a,"sin(x) ")+term(b,"cos(x)");
   }
}
