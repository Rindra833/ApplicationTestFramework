package Genius.util;
import Genius.annotation.Controller;
import Genius.annotation.UrlMapping;

import java.util.List;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.Set;

public class Utilitaire {
    public Utilitaire(){};

    public Set<Class<?>> chercherClasse(String packageClasse) {

        Reflections reflections = new Reflections(packageClasse, Scanners.SubTypes.filterResultsBy(s -> true));

        Set<Class<?>> toutesLesClasses = reflections.getSubTypesOf(Object.class);

        return toutesLesClasses;
    }

    public void findController(String packageClasse,List<Class<?>> liste,Map<UrlMethod, RouteMapping> result) throws Exception {
        Set<Class<?>> toutesLesClasses = chercherClasse(packageClasse);

        for (Class<?> clazz : toutesLesClasses) {
            if( clazz.isAnnotationPresent(Controller.class)){
                liste.add(clazz);

                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(UrlMapping.class)) {
                        UrlMapping annotation = method.getAnnotation(UrlMapping.class);
                        UrlMethod urlMethod = new UrlMethod(annotation.method(), annotation.path());

                        if (result.containsKey(urlMethod)) {
                            throw new Exception("La clé '" + urlMethod + "' existe déjà.");
                        }
                        
                        result.put(urlMethod, new RouteMapping(clazz, method));
                    }
                }
                
            } 
        }
    }
}
