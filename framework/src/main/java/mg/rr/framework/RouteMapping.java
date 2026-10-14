package mg.rr.framework;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class RouteMapping {
    private final UrlMethode urlMethode;
    private final Class<?> controllerClass;
    private final Method method;

    public RouteMapping(UrlMethode urlMethode, Class<?> controllerClass, Method method) {
        this.urlMethode = urlMethode;
        this.controllerClass = controllerClass;
        this.method = method;
    }

    public UrlMethode getUrlMethode() { return urlMethode; }
    public String getUrl() { return urlMethode.getUrl(); }
    public HttpMethod getHttpMethod() { return urlMethode.getMethode(); }
    public Class<?> getControllerClass() { return controllerClass; }
    public Method getMethod() { return method; }

    public boolean isApiRest() {
        APIRest annotation = method.getAnnotation(APIRest.class);
        return annotation != null && annotation.value();
    }

    /**
     * Sprint 7 : construit les arguments de la methode a partir de la requete.
     *   - Aucun parametre                        -> invoke() simple
     *   - SpringContext en parametre             -> injecte le contexte
     *   - Autre type                             -> matching par NOM avec
     *                                               request.getParameter(nom)
     *                                               puis conversion
     */
    public Object invoke(SpringContext springContext, HttpServletRequest request)
            throws ReflectiveOperationException {
        Object controller = controllerClass.getDeclaredConstructor().newInstance();
        method.setAccessible(true);

        Class<?>[] types = method.getParameterTypes();
        if (types.length == 0) {
            return method.invoke(controller);
        }

        Parameter[] params = method.getParameters();
        Object[] args = new Object[types.length];

        for (int i = 0; i < types.length; i++) {
            Class<?> type = types[i];

            // 1. SpringContext
            if (type.equals(SpringContext.class)) {
                args[i] = springContext;
                continue;
            }

            // 2. Parametre de formulaire : matching par NOM
            String paramName = params[i].getName();
            String rawValue = request == null ? null : request.getParameter(paramName);
            args[i] = convert(rawValue, type);
        }

        return method.invoke(controller, args);
    }

    /**
     * Conversion String -> type Java attendu.
     * Valeurs par defaut pour les primitifs si absent.
     */
    private static Object convert(String value, Class<?> type) {
        if (type.equals(String.class)) {
            return value;
        }
        if (value == null || value.isEmpty()) {
            if (type == int.class)     return 0;
            if (type == long.class)    return 0L;
            if (type == double.class)  return 0.0;
            if (type == float.class)   return 0f;
            if (type == boolean.class) return false;
            if (type == short.class)   return (short) 0;
            if (type == byte.class)    return (byte) 0;
            if (type == char.class)    return '\0';
            return null;
        }

        if (type == int.class || type == Integer.class)       return Integer.parseInt(value);
        if (type == long.class || type == Long.class)         return Long.parseLong(value);
        if (type == double.class || type == Double.class)     return Double.parseDouble(value);
        if (type == float.class || type == Float.class)       return Float.parseFloat(value);
        if (type == boolean.class || type == Boolean.class)   return Boolean.parseBoolean(value);
        if (type == short.class || type == Short.class)       return Short.parseShort(value);
        if (type == byte.class || type == Byte.class)         return Byte.parseByte(value);
        if (type == char.class || type == Character.class)    return value.charAt(0);

        return value;
    }

    // Ancien appel sans requete, garde pour usage interne
    public Object invoke() throws ReflectiveOperationException {
        Object controller = controllerClass.getDeclaredConstructor().newInstance();
        method.setAccessible(true);
        return method.invoke(controller);
    }
}