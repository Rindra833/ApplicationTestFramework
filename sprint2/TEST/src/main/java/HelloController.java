import mg.hasner.framework.Controller;
import mg.hasner.framework.UrlMapping;

@Controller
public class HelloController {

    @UrlMapping("/hello")
    public String sayHello() {
        return "Hello";
    }

    @UrlMapping("/hi")
    public String sayHi() {
        return "Hi";
    }
}
