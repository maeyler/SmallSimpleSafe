package mae.tut.fun;

/**
 * Abstract super class of all Functions
 *
 * @author Eyler
 * @version 1.2
 */
public abstract class Function {
    
    Function() {}

    public abstract float value(float x);
   
   public float deriv(float x) {  //derivative
      float DEL=1E-6f;
      return (value(x+DEL) - value(x)) / DEL;
   }
   public float solve(float x) {
      final int MAX=15; 
      final double EPS=1E-7;
      int k = 0; float y = value(x);
      while (k<MAX && Math.abs(y)>EPS) { 
         x = x - y/deriv(x); 
         y = value(x); k++;
      }
      if (Math.abs(y)<=EPS) return x;
      else return Float.NaN;
   }
   public float solve() {
      int M = 50; 
      float x = Float.NaN; 
      for (int k=0; k<M && Float.isNaN(x); k++) 
         x = solve((float)(100*Math.random()));
      return x;
   }
   public static String numToStr(float v) {
      String s = String.valueOf(v);
      if (s.endsWith(".0"))  //is integer
         s = s.substring(0, s.length()-2);
      return s;
   }
   static String coef(float v) {
      if (v == 1) return "+";
      if (v == -1) return "-";
      String s = numToStr(v);
      return (s.charAt(0)=='-'? s : '+'+s);
   }
   static String term(float v, String s) {
      return (v==0? "" : coef(v)+s);
   }
   static String lastTerm(float v) {
      if (v == 0) return "";
      String s = numToStr(v);
      return (s.charAt(0)=='-'? s : '+'+s);
   }
}
