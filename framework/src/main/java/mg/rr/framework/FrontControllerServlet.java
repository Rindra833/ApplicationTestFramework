package mg.rr.framework;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

@MultipartConfig( 
    fileSizeThreshold = 1024 * 1024,      // 1 MB en mémoire avant écriture disque
    maxFileSize = 1024 * 1024 * 10,       // 10 MB max par fichier
    maxRequestSize = 1024 * 1024 * 50     // 50 MB max par requête (plusieurs fichiers)
)
public class FrontControllerServlet extends HttpServlet {

    private MappingRegistry registry;
    private SpringContext springContext;
    private String viewPrefix;
    private String viewSuffix;
    private SessionManager sessionManager;
    private String authRoleAttribute;

    @Override
    public void init() throws ServletException {
        super.init();
        viewPrefix = getConfiguredValue("viewPrefix", "/");
        viewSuffix = getConfiguredValue("viewSuffix", ".jsp");

        Object value = getServletContext().getAttribute(FrameworkContextListener.REGISTRY_ATTRIBUTE);
        if (value instanceof MappingRegistry) {
            registry = (MappingRegistry) value;
        } else {
            registry = new FrameworkInitializer(getServletContext()).init();
            getServletContext().setAttribute(FrameworkContextListener.REGISTRY_ATTRIBUTE, registry);
        }

        Object springValue = getServletContext().getAttribute(FrameworkContextListener.SPRING_CONTEXT_ATTRIBUTE);
        if (springValue instanceof SpringContext) {
            springContext = (SpringContext) springValue;
        } else {
            springContext = SpringContext.fromServletContext(getServletContext());
            getServletContext().setAttribute(FrameworkContextListener.SPRING_CONTEXT_ATTRIBUTE, springContext);
        }
        
        // Initialiser le SessionManager (timeout par défaut 30 min)
        String timeoutMin = getConfiguredValue("sessionTimeoutMinutes", "30");
        long timeoutMillis = Long.parseLong(timeoutMin) * 60L * 1000L;
        this.sessionManager = new SessionManager(timeoutMillis);
        this.authRoleAttribute = (String) getServletContext().getAttribute(FrameworkContextListener.AUTH_ROLE_ATTRIBUTE);
    if (this.authRoleAttribute == null) this.authRoleAttribute = "role";
    }

    @Override
    public void destroy() {
        if (sessionManager != null) sessionManager.shutdown();
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res, HttpMethod.GET);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res, HttpMethod.POST);
    }

    /**
     * Point de branchement :
     *   1. "/" (racine)     -> page de debug HTML
     *   2. route == null    -> erreur (JSON si header Accept, sinon ServletException)
     *   3. @APIRest(true)   -> BLOC 1 : JSON, aucun RequestDispatcher
     *   4. sinon            -> BLOC 2 : ModelAndView + forward JSP
     */
    private void processRequest(HttpServletRequest req, HttpServletResponse res, HttpMethod httpMethod)
            throws ServletException, IOException {
        String url = getRequestedUrl(req);

        // (1) Page de debug racine
        if ("/".equals(url)) {
            res.setContentType("text/html;charset=UTF-8");
            PrintWriter out = res.getWriter();
            out.println("<h2>URL demandee : " + url + " (" + httpMethod + ")</h2>");
            output(out);
            return;
        }

        RouteMapping route = registry.findRoute(url, httpMethod);

        // (2) Route inconnue
        if (route == null) {
            String message = "URL/Methode non reconnue : " + httpMethod + " " + url
                    + ". URLs existantes : " + registry.getExistingUrls();
            if (wantsJson(req)) {
                writeJsonError(res, HttpServletResponse.SC_NOT_FOUND, message);
                return;
            }
            throw new ServletException(message);
        }

        // SPRINT 10 BIS : Vérification d'accès
        if (!checkAuthorization(route, req, res)) {
            return; // Accès refusé, on arrête le traitement
        }

        // (3) API REST : retour JSON, aucun RequestDispatcher
        if (route.isApiRest()) {
            processApiRequest(req, res, route);
            return;
        }

        // (4) Vue classique : forward vers la JSP
        res.setContentType("text/html;charset=UTF-8");
        dispatchModelAndView(req, res, route);
    }

    /**
     * Sprint 6 + 7 + 8 + 9 : invoque la methode avec les parametres de la requete,
     * la session, le multipart, et serialise la valeur de retour en JSON.
     */
    private void processApiRequest(HttpServletRequest req, HttpServletResponse res, RouteMapping route)
            throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        try {
            Object controller = registry.getControllerInstance(route.getControllerClass());
            // Sprint 9 : on passe req, res et sessionManager + Sprint 10 : controller
            Object result = route.invoke(controller, springContext, req, res, sessionManager);
            res.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = res.getWriter();
            out.write(JsonSerializer.toJson(result));
            out.flush();
        } catch (ReflectiveOperationException | RuntimeException e) {
            String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            writeJsonError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
        }
    }

    /**
     * Ecrit une erreur JSON structuree.
     */
    private void writeJsonError(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("error", message);
        PrintWriter out = res.getWriter();
        out.write(JsonSerializer.toJson(body));
        out.flush();
    }

    /**
     * Indique si le client prefere du JSON (header Accept).
     */
    private boolean wantsJson(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        return accept != null && accept.contains("application/json");
    }

    // ---------- Bloc 2 : rendu MVC ----------

    private void dispatchModelAndView(HttpServletRequest req, HttpServletResponse res, RouteMapping route)
            throws ServletException, IOException {
        // ✅ Correction : on passe bien res à invokeRoute
        Object result = invokeRoute(route, req, res);

        if (!(result instanceof ModelAndView)) {
            throw new ServletException("Type de retour invalide pour "
                    + route.getControllerClass().getSimpleName() + "." + route.getMethod().getName()
                    + "() : ModelAndView attendu (ou activez @APIRest pour du JSON).");
        }

        ModelAndView modelAndView = (ModelAndView) result;
        addArgToRequest(req, modelAndView.getData());

        RequestDispatcher dispatcher = req.getRequestDispatcher(getViewPath(modelAndView.getView()));
        dispatcher.forward(req, res);
    }

    private Object invokeRoute(RouteMapping route, HttpServletRequest req, HttpServletResponse res) throws ServletException {
        try {
            Object controller = registry.getControllerInstance(route.getControllerClass());
            return route.invoke(controller, springContext, req, res, sessionManager);
        } catch (ReflectiveOperationException e) {
            throw new ServletException("Erreur pendant l'invocation de la methode "
                    + route.getMethod().getName(), e);
        } catch (RuntimeException e) {
            throw new ServletException("Erreur pendant l'invocation de la methode "
                    + route.getMethod().getName(), e);
        }
    }

    private void addArgToRequest(HttpServletRequest req, Map<String, Object> data) {
        if (data == null) return;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            req.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    private String getViewPath(String view) throws ServletException {
        if (view == null || view.trim().isEmpty()) {
            throw new ServletException("La vue du ModelAndView est vide.");
        }
        String viewName = view.trim();
        String path = viewName.startsWith("/") ? viewName : viewPrefix + viewName;
        if (!viewSuffix.isEmpty() && !path.endsWith(viewSuffix)) {
            path += viewSuffix;
        }
        return path;
    }

    /**
     * Page de debug : [VIEW] ou [API/JSON] pour chaque route.
     */
    private void output(PrintWriter out) {
        out.println("<h3>Controleurs detectes</h3>");

        if (registry.getControllers().isEmpty()) {
            out.println("<p>Aucun controleur trouve.</p>");
            return;
        }

        out.println("<ul>");
        for (Class<?> clazz : registry.getControllers()) {
            out.println("<li><b>" + clazz.getSimpleName() + "</b>");
            out.println("<ul>");
            boolean hasMappedMethod = false;
            for (RouteMapping route : registry.getRoutes().values()) {
                if (route.getControllerClass().equals(clazz)) {
                    hasMappedMethod = true;
                    out.println("<li>[" + route.getHttpMethod() + "] " + route.getUrl()
                            + (route.isApiRest() ? " <b>[API/JSON]</b>" : " [VIEW]")
                            + " -&gt; " + route.getMethod().getName() + "()</li>");
                }
            }
            if (!hasMappedMethod) {
                out.println("<li>Aucune methode annotee @UrlMapping</li>");
            }
            out.println("</ul>");
            out.println("</li>");
        }
        out.println("</ul>");
    }

    private String getRequestedUrl(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        return uri.isEmpty() ? "/" : uri;
    }

    private String getConfiguredValue(String name, String defaultValue) {
        String value = getServletConfig().getInitParameter(name);
        return value == null ? defaultValue : value;
    }

        /**
     * SPRINT 10 BIS : Vérifie l'authentification et les rôles.
     */
    private boolean checkAuthorization(RouteMapping route, HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        // 1. Chercher @Auth sur la méthode
        Auth auth = route.getMethod().getAnnotation(Auth.class);
        
        // 2. Si absent, chercher sur la classe (Priorité à la méthode)
        if (auth == null) {
            auth = route.getControllerClass().getAnnotation(Auth.class);
        }

        // 3. Si aucune annotation, l'accès est libre
        if (auth == null) {
            return true; 
        }

        boolean isApi = route.isApiRest() || wantsJson(req);
        Session session = sessionManager.getSession(req, res);

        // 4. Vérifier l'authentification (l'utilisateur a-t-il le rôle en session ?)
        Object roleValue = session.getAttribute(authRoleAttribute);

        if (roleValue == null) {
            // Pas de rôle en session = pas connecté
            ResponseHelper.handleUnauthorized(res, isApi);
            return false;
        }

        // 5. Si le rôle demandé est vide, être connecté suffit
        String requiredRole = auth.role();
        if (requiredRole == null || requiredRole.trim().isEmpty()) {
            return true;
        }

        // 6. Si un rôle spécifique est demandé, on vérifie
        boolean hasRole = false;
        if (roleValue instanceof String) {
            hasRole = requiredRole.equals((String) roleValue);
        } else if (roleValue instanceof java.util.Collection) {
            for (Object r : (java.util.Collection<?>) roleValue) {
                if (requiredRole.equals(String.valueOf(r))) {
                    hasRole = true;
                    break;
                }
            }
        }

        if (!hasRole) {
            // L'utilisateur est connecté, mais n'a pas le bon rôle
            ResponseHelper.handleForbidden(res, isApi);
            return false;
        }

        return true;
    }
}