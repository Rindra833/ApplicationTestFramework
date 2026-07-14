package test;

import Genius.annotation.Controller;
import Genius.annotation.UrlMapping;
import Genius.util.*;

@Controller
public class TestController1 {
    @UrlMapping(path="/rien",method=MethodeType.GET)
    public String tsisy(){
        return "/b";
    }
}
