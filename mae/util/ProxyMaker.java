package mae.util;

import java.util.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;

public class ProxyMaker<T> {  //generic version
    static ProxyMaker pm = new ProxyMaker();  //non-generic instance
    public ProxyMaker() { }   //other instances may be needed for generic code
    public T makeProxy(Class<T> c, T s) { //generic methods cannot be static!!
        if (!c.isInterface()) 
            throw new RuntimeException("Not an interface: "+c);
        if (!c.isInstance(s)) 
            throw new RuntimeException("Wrong interface: "+c);
        ClassLoader d = s.getClass().getClassLoader();
        Class[] ca = { c };
        InvocationHandler h = new Logger<T>(s);
        return (T)Proxy.newProxyInstance(d, ca, h);
    }   
    public static Object makeProxy(String cn, Object s)  { 
        try {
            Class c = Class.forName(cn);
            return pm.makeProxy(c, s);
        } catch (ClassNotFoundException x) {
            throw new RuntimeException(x);
        }
    }   
    public static ActionListener newProxy(ActionListener s) {
        return new ProxyMaker<ActionListener>().makeProxy(ActionListener.class, s);
    }   
    public static KeyListener newProxy(KeyListener s) {
        return new ProxyMaker<KeyListener>().makeProxy(KeyListener.class, s);
    }   
    public static MouseListener newProxy(MouseListener s) {
        return new ProxyMaker<MouseListener>().makeProxy(MouseListener.class, s);
    }
    public static WindowListener newProxy(WindowListener s) {
        return new ProxyMaker<WindowListener>().makeProxy(WindowListener.class, s);
    }
    public static Comparable newProxy(Comparable s) {
        return new ProxyMaker<Comparable>().makeProxy(Comparable.class, s);
    }   
    public static List newProxy(List s) {
        return new ProxyMaker<List>().makeProxy(List.class, s);
    }   
    public static Map newProxy(Map s) {
        return new ProxyMaker<Map>().makeProxy(Map.class, s);
    }
    public static Set newProxy(Set s) {
        return new ProxyMaker<Set>().makeProxy(Set.class, s);
    }

    static class Logger<T> implements InvocationHandler { 
        final T obj;
        public Logger(T u) { obj = u; }
        public Object invoke(Object p, Method m, Object[] a) throws Throwable {
            String s = m.getName();  
            Object res = m.invoke(obj , a);
            //these two methods are invoked too often in SSS
            if (s.equals("size") || s.equals("toString")) return res;
            if (res != null) s += " -> "+res;
            System.out.print("  "); System.out.println(s);
            return res;
        }
    }
}
