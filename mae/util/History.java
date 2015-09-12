/*
 * History.java
 *
 * Created on January 1, 2004, 6:51 PM
 */

package mae.util;

/**
 * @author  Eyler
 */
public abstract class History {
    
      Object[] data;
      int first, last, current, max;
      protected History(int m, Object x) {
         data = new Object[m];
         max = m; data[0] = x; 
      }
      protected abstract boolean accept();
      public void backward() {
         do {
            if (current == first) return;
            current = decr(current); 
         } while (!accept());
      }
      public void forward() {
         do {
            if (current == last) return;
            current = incr(current);
         } while (!accept());
      }
      public String toString() {
         return "["+first+":"+current+":"+last+"]"; 
      }
      
      int incr(int i) { return (i == max-1)? 0 : i+1; }
      int decr(int i) { return (i == 0)? max-1 : i-1; }
      void set(int i, Object x) { data[i] = x; }
      Object get(int i) { return data[i]; }
      protected Object current() { return get(current); }
      protected void setCurrent(Object x) { set(current, x); }
      protected void append(Object x) {
         if (current == last) {
            last = incr(last);  
            if (first == last) first = incr(first);
         } 
         current = incr(current); set(current, x); 
      }
}
