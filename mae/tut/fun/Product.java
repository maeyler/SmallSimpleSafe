package mae.tut.fun;

public class Product extends Function  {
   protected Function f1, f2;
   public Product(Function f1, Function f2) { 
      this.f1 = f1; this.f2 = f2;
   }
   public float value(float x) { 
      return f1.value(x) * f2.value(x);
   }
   public float deriv(float x) { 
      float y1 = f1.value(x); float y2 = f2.value(x);
      float d1 = f1.deriv(x); float d2 = f2.deriv(x);
      return d1*y2 + d2*y1;
   }
   public String toString() { 
      return "("+f1+")("+f2+")";
   }
}
