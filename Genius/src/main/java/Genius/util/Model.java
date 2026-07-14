package Genius.util;

import java.util.HashMap;
import java.util.Map;

public class Model {
    
    private final Map<String, Object> attribute = new HashMap<>();

    

    public void addAttribute(String cle, Object list){
        attribute.put(cle,list);
    }

    public Map<String, Object> getAttribute() {
        return this.attribute;
    }

}