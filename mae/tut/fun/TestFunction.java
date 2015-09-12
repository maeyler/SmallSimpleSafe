package mae.tut.fun;

// Author: Eyler -- 17/3/02  25/5/02

public class TestFunction  {
   static void print(String s, float v) {
      System.out.println(s+Function.numToStr(v));
   }
   static void test(Function f) {
      System.out.println(f.getClass()+": "+f);
      print("f(0) = ", f.value(0));
      print("d(0) = ", f.deriv(0));
      print("root = ", f.solve());
      System.out.println();
   }
   public static void main(String[] args) {
      Function f = new Exponential(1, -2);
      test(f);   //e^(-2x)
      Function g = new Power10(1000);
      test(g);   //10^x-1000
      test(new Product(f,g));     //f*g
      test(new Quadratic(-2, 0 , 1));//x^2-2
 //     float[] c1 = {-1, 0 , 1};
      Polynomial p = new Quadratic(-1, 0 , 1);    //x^2-1
      test(p);
 //     float[] c2 = {2, 0, 0, 1}; 
      Polynomial q = new Polynomial(new float[] {2, 0, 0, 1});   //x^3+2
      test(q);
      test(new Sum(p,q));      //p+q
      test(p.add(q));          //x^3+x^2+1
      test(p.mult(q));         //x^5-x^3+2x^2-2
      float[] x = {1, 2, 3};
      float[] y = {3, 7,13};
      p = Factory.interpolate(x, y);
      test(p);                 //x^2+x+1
   }
   public static void main2(String[] args) {
      Function f = new Quadratic(-8, 0 , 2); 
      test(f);   //2x^2-8
      Function g = new Trigonometric(-3, 2);
      test(g);   //-3cos(x)+2sin(x)
      test(new Sum(f,g));      //f+g
      test(new Product(f,g));     //f*g
   }
}
