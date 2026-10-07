package mg.rr.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sprint 6 : marque une methode de controleur comme endpoint REST.
 * Si value() == true, la valeur de retour est serialisee en JSON et ecrite
 * directement dans la reponse HTTP (aucun forward vers une JSP).
 * Si value() == false (ou annotation absente), comportement MVC normal.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface APIRest {
    boolean value() default true;
}