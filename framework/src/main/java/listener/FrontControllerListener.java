package listener;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.HashMap;
import util.*;

@WebListener
public class FrontControllerListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {

            ServletContext context = sce.getServletContext();

            Utilitaire util = new Utilitaire();

            List<Class<?>> controllers = new ArrayList<>();

            Map<UrlMethod, RouteMapping> routes = new HashMap<>();


            util.findController("test",controllers,routes);

            // util.findRoutes(controllers,routes);

            context.setAttribute("routes", routes);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
