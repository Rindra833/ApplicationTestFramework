package test;

import mg.rr.framework.Controller;
import mg.rr.framework.APIRest;      // ← corrigé
import mg.rr.framework.HttpMethod;
import mg.rr.framework.ModelAndView;
import mg.rr.framework.SpringContext;
import mg.rr.framework.UrlMapping;

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