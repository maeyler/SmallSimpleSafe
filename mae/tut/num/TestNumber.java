// Author: Eyler -- 12/1/2002
// V2.1 Cruncher -- 30/12/2003

package mae.tut.num;

class TestNumber  {
   
    final static Number one = new Whole(1);
    final static Number oneOverTwo = new Rational(1, 2);
    final static Number oneAndHalf = new Decimal(1.5f);
    final static Number onePlusI = new Complex(1, 1);
    
    static void print(Number n) {
      if (n instanceof Complex) {
          Complex c = (Complex)n;
          System.out.println(c+"  "+c.toPolar());
      } else {
          System.out.println(n+"  v:"+n.value());
        }
    }
    static void test(Number n) {
        System.out.println(n.getClass().getName()+":");
        print(n);
        n = n.add(one); 
        print(n);
        n = n.mult(oneOverTwo); 
        print(n);
        n = n.add(oneAndHalf); 
        print(n);
    }
    static void expon(Number n) {
        print(Factory.cruncher().expPS(n));
    }
    public static void main(String[] args)  {
        test(new Whole(11));
        test(new Rational(4000, 5000));
        test(new Decimal(0.8f));
        test(new Complex(3, 3));

        expon(new Whole(1));
        expon(new Rational(1, 1));
        expon(new Decimal(1));
        expon(new Complex(1, 0));
        expon(new Complex(0, (float)Math.PI));
    }
}
