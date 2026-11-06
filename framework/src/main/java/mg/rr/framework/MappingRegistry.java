package mg.rr.framework;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MappingRegistry {
    private final List<Class<?>> controllers;
    private final Map<UrlMethode, RouteMapping> routes;

    // Sprint 10 : Singleton
    private final Map<Class<?>, Object> controllerInstances = new ConcurrentHashMap<>(); 
    /**
     * Stocke les controleurs trouves et la table de routage construite au demarrage.
     */
    public MappingRegistry(List<Class<?>> controllers, Map<UrlMethode, RouteMapping> routes) {
        this.controllers = controllers;
        this.routes = routes;
    }

    /**
     * Retourne la liste des controleurs detectes en lecture seule.
     */
    public List<Class<?>> getControllers() {
        return Collections.unmodifiableList(controllers);
    }

    /**
     * Retourne la table des routes en lecture seule.
     */
    public Map<UrlMethode, RouteMapping> getRoutes() {
        return Collections.unmodifiableMap(routes);
    }

    /**
     * Cherche une route par le couple URL + methode HTTP.
     */
    public RouteMapping findRoute(String url, HttpMethod httpMethod) {
        return routes.get(new UrlMethode(url, httpMethod));
    }

    /**
     * Retourne une chaine lisible contenant toutes les routes disponibles.
     */
    public String getExistingUrls() {
        if (routes == null || routes.isEmpty()) {
            return "aucune URL disponible";
        }

        StringBuilder builder = new StringBuilder();
        for (UrlMethode key : routes.keySet()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(key);
        }

        return builder.toString();
    }

    // Sprint 10: recupere ou cree l'instance unique de controller
    // on utilise computeIfAbsent pour la thread-safety
    public Object getControllerInstance(Class<?> clazz) {
        return controllerInstances.computeIfAbsent(clazz, c -> {
            try {
                return c.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Impossible d'instancier le controlleur : " + c.getName(), e);

            }
        });
    }
}
