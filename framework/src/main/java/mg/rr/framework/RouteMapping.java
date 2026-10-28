package mg.rr.framework;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.lang.reflect.Field;
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
     * Sprint 9 : signature mise a jour avec HttpServletResponse et SessionManager.
     */
    public Object invoke(SpringContext springContext, HttpServletRequest request,
                         HttpServletResponse response, SessionManager sessionManager)
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
            String paramName = params[i].getName();

            // 1. SpringContext
            if (type.equals(SpringContext.class)) {
                args[i] = springContext;
                continue;
            }

            // 2. Requete HTTP (si le developpeur veut la recuperer manuellement)
            if (type.equals(HttpServletRequest.class)) {
                args[i] = request;
                continue;
            }

            // 3. Reponse HTTP
            if (type.equals(HttpServletResponse.class)) {
                args[i] = response;
                continue;
            }

            // 4. Session (Sprint 9)
            if (type.equals(Session.class)) {
                args[i] = sessionManager.getSession(request, response);
                continue;
            }

            // 5. Fichier (Sprint 8)
            if (type.equals(MaFile.class)) {
                args[i] = extractFile(request, paramName);
                continue;
            }

            // 6. Type simple (Sprint 7)
            if (isSimpleType(type)) {
                String rawValue = request == null ? null : request.getParameter(paramName);
                args[i] = convert(rawValue, type);
                continue;
            }

            // 7. POJO (Sprint 7 BIS)
            args[i] = bindObject(type, request);
        }

        return method.invoke(controller, args);
    }

    // ---------- Helpers : Sprint 7, 7 BIS, 8 ----------

    private static boolean isSimpleType(Class<?> type) {
        return type == String.class
            || type == Integer.class || type == int.class
            || type == Long.class    || type == long.class
            || type == Double.class  || type == double.class
            || type == Float.class   || type == float.class
            || type == Boolean.class || type == boolean.class
            || type == Short.class   || type == short.class
            || type == Byte.class    || type == byte.class
            || type == Character.class || type == char.class;
    }

    private static MaFile extractFile(HttpServletRequest request, String paramName) {
        if (request == null) return null;
        try {
            Part part = request.getPart(paramName);
            if (part == null || part.getSize() == 0) return null;

            MaFile mf = new MaFile();
            mf.setFileName(part.getSubmittedFileName());
            mf.setType(part.getContentType());

            try (java.io.InputStream is = part.getInputStream()) {
                mf.setContent(is.readAllBytes());
            }
            return mf;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture fichier '" + paramName + "' : " + e.getMessage(), e);
        }
    }

    private static Object bindObject(Class<?> type, HttpServletRequest request)
            throws ReflectiveOperationException {
        Object instance;
        try {
            instance = type.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("La classe " + type.getName()
                    + " doit avoir un constructeur sans argument pour le binding.");
        }

        if (request == null) return instance;

        for (Field field : type.getDeclaredFields()) {
            String name = field.getName();
            String value = request.getParameter(name);
            if (value == null) continue;

            field.setAccessible(true);
            field.set(instance, convert(value, field.getType()));
        }

        return instance;
    }

    private static Object convert(String value, Class<?> type) {
        if (type.equals(String.class)) return value;

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

    public Object invoke() throws ReflectiveOperationException {
        Object controller = controllerClass.getDeclaredConstructor().newInstance();
        method.setAccessible(true);
        return method.invoke(controller);
    }
}