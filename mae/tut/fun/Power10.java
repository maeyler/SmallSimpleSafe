package mae.tut.fun;

public class Power10 extends Function  {
   protected float a;
   public Power10(float a) { 
      this.a = a;
   }
   public float value(float x) { 
      return (float)Math.pow(10,x)-a; 
   }
   public String toString() { 
      return "10^x - "+numToStr(a);
   }
}
