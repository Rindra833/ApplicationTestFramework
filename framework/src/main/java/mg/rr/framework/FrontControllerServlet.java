package mg.rr.framework;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

public class FrontControllerServlet extends HttpServlet {

    private MappingRegistry registry;
    private SpringContext springContext;
    private String viewPrefix;
    private String viewSuffix;

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
     * Sprint 6 : point de branchement JSON vs JSP.
     *   1. Cas special "/" : page de debug (HTML)
     *   2. Route introuvable : erreur (JSON si header Accept, sinon ServletException)
     *   3. Route avec @APIRest(true)  -> bloc 1 : JSON, pas de dispatcher
     *   4. Route sans @APIRest / false -> bloc 2 : ModelAndView + RequestDispatcher
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
     * Sprint 6, bloc 1 : invoque la methode et serialise la valeur de retour en JSON.
     * Content-Type: application/json;charset=UTF-8. Aucun forward.
     */
    private void processApiRequest(HttpServletRequest req, HttpServletResponse res, RouteMapping route)
            throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        try {
            Object result = route.invoke(springContext);
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
     * Sprint 6 : ecrit une erreur JSON structuree.
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
     * Sprint 6 : indique si le client prefere du JSON (header Accept).
     * Utilise uniquement pour le rendu des erreurs de routage.
     */
    private boolean wantsJson(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        return accept != null && accept.contains("application/json");
    }

    // ---------- Bloc 2 : rendu MVC (inchange depuis Sprint 5) ----------

    private void dispatchModelAndView(HttpServletRequest req, HttpServletResponse res, RouteMapping route)
            throws ServletException, IOException {
        Object result = invokeRoute(route);

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

    private Object invokeRoute(RouteMapping route) throws ServletException {
        try {
            return route.invoke(springContext);
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
     * Sprint 6 : la page de debug indique pour chaque route son mode : [VIEW] ou [API/JSON].
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
}