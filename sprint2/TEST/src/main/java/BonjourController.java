import mg.hasner.framework.Controller;
import mg.hasner.framework.UrlMapping;

@Controller
public class BonjourController {
    @UrlMapping("/Bonjour")
    public String diteBonjour() {
        return "Bonjour";
    }

    @UrlMapping("/Salama")
    public String diteSalama() {
        return "Salama";
    }
    
}
