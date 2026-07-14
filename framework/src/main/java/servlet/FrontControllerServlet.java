package servlet;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import util.*;

public class FrontControllerServlet extends HttpServlet {
    
    private Map<UrlMethod, RouteMapping> routes = new HashMap<>();

    private Utilitaire util;

    public void init() throws ServletException {
        try {
            this.util = new Utilitaire();

            this.routes = (Map<UrlMethod, RouteMapping>) getServletContext().getAttribute("routes");

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    protected void processRequest(HttpServletRequest req,
                                  HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");
        String url = req.getRequestURI();

        output(url, req, resp);
    }

    private void output(String url,
                        HttpServletRequest req,
                        HttpServletResponse resp)
            throws ServletException, IOException {

        
        PrintWriter out = resp.getWriter();

        out.println(url);
        
        boolean lien = false;
        String urlCont = url.substring(req.getContextPath().length());

        UrlMethod urlMethod1 = new UrlMethod(MethodeType.valueOf(req.getMethod()), urlCont);

        RouteMapping route = routes.get(urlMethod1);

        out.println(urlCont);
        
        if (route != null) {
            out.println("<br>URL existant : " + urlCont + ", type : " + urlMethod1.getMethod() + " methode : "  + route.getControllerClass() + "->" + route.getMethod().getName());
            try {

                Object obj = route.getControllerClass().getDeclaredConstructor().newInstance();
                Object result = route.getMethod().invoke(obj);

                if (result != null) {
                    out.println("<br>Resultat : " + result);
                } else {
                    out.println("<br>Resultat : null");
                }
            } catch(Exception e) {
                e.printStackTrace();
                throw new ServletException(e);

            }
            lien = true;
        }

        if(!lien) {
            out.println("Voici les liens existants : ");

            boolean hasMappedMethod = false;
            for(UrlMethod urlMethod : routes.keySet()) {
                RouteMapping routeM = routes.get(urlMethod);
                hasMappedMethod = true;

                out.println("<br>URL existant : " + urlMethod.getPath() + ", type : " + urlMethod1.getMethod() + " Classe : " +routeM.getControllerClass().toString()+", methode : "+ routeM.getMethod().getName());
            }

            if (!hasMappedMethod) {
                out.println("<br>Aucune methode annotee @UrlMapping");
            }
            
        }        
    }
    
    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
            throws ServletException, IOException {

        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
            throws ServletException, IOException {

        processRequest(req, resp);
    }

}