package mae.tut.fun;

public class Sum extends Function  {
   protected Function f1, f2;
   public Sum(Function f1, Function f2) { 
      this.f1 = f1; this.f2 = f2;
   }
   public float value(float x) { 
      return f1.value(x) + f2.value(x);
   }
   public float deriv(float x) { 
      return f1.deriv(x) + f2.deriv(x);
   }
   public String toString() { 
      return "("+f1+") + ("+f2+")";
   }
}
