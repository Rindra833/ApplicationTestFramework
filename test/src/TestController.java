package test;

import mg.hasner.framework.Controller;
import mg.hasner.framework.APIRest;      // ← corrigé
import mg.hasner.framework.HttpMethod;
import mg.hasner.framework.ModelAndView;
import mg.hasner.framework.SpringContext;
import mg.hasner.framework.UrlMapping;

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
}