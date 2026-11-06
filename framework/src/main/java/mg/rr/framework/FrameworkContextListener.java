package mg.rr.framework;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class FrameworkContextListener implements ServletContextListener {
    public static final String REGISTRY_ATTRIBUTE = "mg.rr.framework.mappingRegistry";
    public static final String SPRING_CONTEXT_ATTRIBUTE = "mg.rr.framework.springContext";
    public static final String AUTH_ROLE_ATTRIBUTE = "mg.rr.framework.authRoleAttribute";

    /**
     * Au demarrage de l'application web, recupere Spring puis scanne les controleurs du framework.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        SpringContext springContext = SpringContext.fromServletContext(context);
        MappingRegistry registry = new FrameworkInitializer(context).init();

        context.setAttribute(SPRING_CONTEXT_ATTRIBUTE, springContext);
        context.setAttribute(REGISTRY_ATTRIBUTE, registry);
    

            // Sprint 10 Bis : Récupération du nom de l'attribut de rôle depuis web.xml
        String roleAttrName = context.getInitParameter("authRoleAttribute");

        if (roleAttrName == null || roleAttrName.trim().isEmpty()) {
            roleAttrName = "role"; // Valeur par défaut
        }
        context.setAttribute(AUTH_ROLE_ATTRIBUTE, roleAttrName);
    }

    /**
     * Au shutdown de l'application web, nettoie les objets stockes dans le ServletContext.
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().removeAttribute(REGISTRY_ATTRIBUTE);
        sce.getServletContext().removeAttribute(SPRING_CONTEXT_ATTRIBUTE);
        sce.getServletContext().removeAttribute(AUTH_ROLE_ATTRIBUTE);
    }
}
