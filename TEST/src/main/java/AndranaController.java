import mg.hasner.framework.Controller;
import mg.hasner.framework.UrlMapping;

@Controller
public class AndranaController {
    @UrlMapping("/Andrana")
    public String andrana() {
        return "Bonjour depuis Andrana";
    }
    
}
