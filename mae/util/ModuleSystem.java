package mae.util; 
 

import java.util.List; 
import java.util.ArrayList; 
import java.io.IOException; 
 
 
import java.lang.module.ModuleFinder; 
import java.lang.module.ModuleReference; 
import java.lang.module.ModuleReader; 
 
public class ModuleSystem { 
 
 
  public static List<String> readClassesFromModule() { 
 
 
      ModuleFinder finder = ModuleFinder.ofSystem(); 
 
      List<String> stream = new ArrayList<>(); 
 
      finder.findAll().stream().filter(f -> f.descriptor().name().startsWith("java")) 
      .forEach(moduleReference -> 
               { 
                   try { moduleReference.open().list().filter(f -> f.startsWith("java")).forEach(p -> stream.add(p)); } 
                   catch (IOException ex) { System.err.println(ex); } 
               }); 
      return stream; 
  } 
 
 
 
} 

