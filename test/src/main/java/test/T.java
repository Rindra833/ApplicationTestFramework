package test;

import Genius.annotation.*;
import Genius.util.*;

@Controller
public class T {
    @UrlMapping(path="/ohatra",method=MethodeType.GET)
    public ModelAndView rien(){
        ModelAndView m = new ModelAndView("/test");

        int[] test = {4,5,6,7,8,9};
        
        String[] test1 = {"Test1","Test2","Test3","Test4","Test5","Test6"};


        m.addAttribute("int",test);
        m.addAttribute("String",test1);
        return m;
    }

    @UrlMapping(path="/ohatraB",method=MethodeType.GET)
    public String zavatra(Model model){
        int[] test = {4,5,6,7,8};
        
        String[] test1 = {"Test1","Test2","Test3","Test4","Test5"};
        

        model.addAttribute("int",test);
        model.addAttribute("String",test1);
        return "/test";
    }
}
