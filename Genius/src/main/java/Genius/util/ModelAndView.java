package Genius.util;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    private Map<String, Object> attribute = new HashMap<>();
    private String URL;

    public ModelAndView(String URL) {
        this.URL = URL;
    }

    public void addAttribute(String cle, Object list){
        attribute.put(cle,list);
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public Map<String, Object> getAttribute() {
        return attribute;
    }

    public String getURL() {
        return URL;
    }
}
