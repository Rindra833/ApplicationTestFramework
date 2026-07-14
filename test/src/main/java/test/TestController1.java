package test;

import annotation.Controller;
import annotation.UrlMapping;
import util.*;

@Controller
public class TestController1 {
    @UrlMapping(path="/test2/ss",method=MethodeType.GET)
    public void nothing(){
        
    }
     @UrlMapping(path="/test2/ss",method=MethodeType.POST)
    public void nothing(){
        
    }
}
