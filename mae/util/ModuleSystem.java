package mae.util;

import java.util.List; 
import java.util.ArrayList; 
import java.io.IOException;

import java.lang.module.ModuleFinder; 
import java.lang.module.ModuleReference; 
import java.lang.module.ModuleReader; 
 
public class ModuleSystem { 
 
  public static List<String> readClassesFromModule() { 

      List<String> stream = new ArrayList<>();

      ModuleFinder.ofSystem().findAll().stream()
      .filter(f -> f.descriptor().name().startsWith("java"))
      .forEach(moduleReference ->
               { 
                   try { moduleReference.open().list()
                           .filter(f -> f.startsWith("java"))
                           .map(n -> moduleReference.
                                   descriptor().
                                   name()+ "#" + n)
                           .forEach(p -> stream.add(p)); 
                        //p represents java packages with prefix which is 
                        //module name; i.e., java.base#java.lang
                   }
                   catch (IOException ex) { System.err.println(ex); } 
               }); 
      return stream; 
      
  }
}

