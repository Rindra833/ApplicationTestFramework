package test;

import mg.rr.framework.Controller;
import mg.rr.framework.APIRest;      // ← corrigé
import mg.rr.framework.HttpMethod;
import mg.rr.framework.ModelAndView;
import mg.rr.framework.SpringContext;
import mg.rr.framework.UrlMapping;
import mg.rr.framework.MaFile;                         // ← nouveau
import jakarta.servlet.http.HttpServletRequest;        // ← nouveau
import java.io.File;                                   // ← nouveau
import java.nio.file.Files;

import java.util.List;

@Controller
public class TestController {

    /**
     * API REST : la liste est serialisee en JSON, aucun forward JSP.
     */
    @APIRest(true)                                       // ← corrigé
    @UrlMapping("/employees")
    public List<String> employees(SpringContext springContext) {
        EmployeeService employeeService =
                springContext.getBean("employeeService", EmployeeService.class);
        return employeeService.findAll();
    }

    @UrlMapping("/list")
    public ModelAndView list(SpringContext springContext) {
        EmployeeService employeeService =
                springContext.getBean("employeeService", EmployeeService.class);
        List<String> employees = employeeService.findAll();

        ModelAndView modelAndView = new ModelAndView("list");
        modelAndView.addAttribut("titre", "Liste des employes depuis Spring");
        modelAndView.addAttribut("employees", employees);
        modelAndView.addAttribut("controller", "TestController");
        modelAndView.addAttribut("methode", "list");
        modelAndView.addAttribut("httpMethod", "GET");
        return modelAndView;
    }

    @UrlMapping(value = "/list", methode = HttpMethod.POST)
    public ModelAndView listPost(SpringContext springContext) {
        EmployeeService employeeService =
                springContext.getBean(EmployeeService.class);
        List<String> employees = employeeService.findAllForPost();

        ModelAndView modelAndView = new ModelAndView("list");
        modelAndView.addAttribut("titre", "Liste des employes POST depuis Spring");
        modelAndView.addAttribut("employees", employees);
        modelAndView.addAttribut("controller", "TestController");
        modelAndView.addAttribut("methode", "listPost");
        modelAndView.addAttribut("httpMethod", "POST");
        return modelAndView;
    }

    @UrlMapping("/form")
    public ModelAndView afficherFormulaire() {
        return new ModelAndView("form");   // forward vers form.jsp
    }

   @UrlMapping(value = "/save", methode = HttpMethod.POST)
    public ModelAndView save(Employee employee, MaFile photo, HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("resultat");
        
        mv.addAttribut("employee", employee);

        // Vérification de l'existence du fichier
        if (photo != null && photo.exists()) {
            try {
                // Récupération du chemin depuis web.xml
                String uploadDir = request.getServletContext().getInitParameter("uploadPath");
                if (uploadDir == null) {
                    uploadDir = System.getProperty("java.io.tmpdir"); // fallback de sécurité
                }

                // Création du dossier s'il n'existe pas
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                // Sauvegarde du fichier sur le disque
                File dest = new File(dir, photo.getFileName());
                Files.write(dest.toPath(), photo.getContent());

                mv.addAttribut("fichierMessage", "Fichier sauvegardé avec succès dans : " + dest.getAbsolutePath());
                mv.addAttribut("fichierNom", photo.getFileName());
                mv.addAttribut("fichierTaille", photo.getContent().length);
            } catch (Exception e) {
                mv.addAttribut("fichierMessage", "Erreur sauvegarde : " + e.getMessage());
            }
        } else {
            mv.addAttribut("fichierMessage", "Aucun fichier fourni ou fichier vide.");
        }

        return mv;
    }
}