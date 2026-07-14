package test;

import annotation.*;
import util.*;

@Controller
public class Essai
 {
    @UrlMapping(path="/test/good",method=MethodeType.GET)
    public String rien(){
        return "Bonjour!";
    }

    @UrlMapping(path="/test2/ss",method=MethodeType.POST)
    public void zavatra(){
        
    }
}
