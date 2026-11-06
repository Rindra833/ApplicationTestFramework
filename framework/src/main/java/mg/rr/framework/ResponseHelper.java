package mg.rr.framework;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

// Sprint 10 BIs : gestion des erruers HTTP d'authentification
public class ResponseHelper {

    public static void handleUnauthorized(HttpServletResponse res, boolean isApi) throws IOException {
        sendError(res, isApi, HttpServletResponse.SC_UNAUTHORIZED, "Authentification requise (401)");
        
    }

    public static void handleForbidden(HttpServletResponse res, boolean isApi) throws IOException {
        sendError(res, isApi, HttpServletResponse.SC_FORBIDDEN, "Accès refusé: role insuffisant (403)");
    }

    private static void sendError (HttpServletResponse res, boolean isApi, int status, String message) throws IOException {
        res.setStatus(status);
        PrintWriter out = res.getWriter();

        if (isApi) {
            res.setContentType("application/json;charset=UTF-8");
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", false);
            body.put("error", message);
            out.write(JsonSerializer.toJson(body));
        } else {
            res.setContentType("text/html;charset=UTF-8");
            out.write("<!DOCTYPE html><html><head><title>Erreur " + status + "</title></head><body>");
            out.write("<h1>Erreur " + status + "</h1>");
            out.write("<p>" + message + "</p>");
            out.write("<p><a href=\"/\">Retour à l'accueil<a/></p>");
            out.write("</body></html>");
        }
        out.flush();
    }
}