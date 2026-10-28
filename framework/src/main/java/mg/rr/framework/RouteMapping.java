package mg.rr.framework;

import jakarta.servlet.http.HttpServletRequest;
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
     * Sprint 7 BIS : binding objet.
     * Pour chaque parametre :
     *   1. SpringContext        -> injecte le contexte
     *   2. String / primitif    -> request.getParameter(nom)
     *   3. POJO (Employee, ...) -> instancie + remplit ses champs
     *                              a partir des parametres de la requete
     */
   public Object invoke(SpringContext springContext, HttpServletRequest request)
            throws ReflectiveOperationException {
        Object controller = controllerClass.getDeclaredConstructor().newInstance();
        method.setAccessible(true);

        Class<?>[] types = method.getParameterTypes();
        if (types.length == 0) return method.invoke(controller);

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

            // 2. HttpServletRequest (utile pour lire uploadPath ou faire des choses custom)
            if (type.equals(HttpServletRequest.class)) {
                args[i] = request;
                continue;
            }

            // 3. Fichier (Sprint 8)
            if (type.equals(MaFile.class)) {
                args[i] = extractFile(request, paramName);
                continue;
            }

            // 4. Type simple (Sprint 7)
            if (isSimpleType(type)) {
                String rawValue = request == null ? null : request.getParameter(paramName);
                args[i] = convert(rawValue, type);
                continue;
            }

            // 5. POJO (Sprint 7 BIS)
            args[i] = bindObject(type, request);
        }

        return method.invoke(controller, args);
    }

    /**
     * Sprint 8 : extrait un fichier multipart de la requete.
     * Retourne null si le champ n'est pas un fichier ou s'il est vide.
     */
        private static MaFile extractFile(HttpServletRequest request, String paramName) {
        if (request == null) return null;
        try {
            Part part = request.getPart(paramName);
            if (part == null || part.getSize() == 0) {
                return null; // champ non rempli
            }

            MaFile mf = new MaFile();
            mf.setFileName(part.getSubmittedFileName());
            mf.setType(part.getContentType());

            // Lecture du flux binaire
            try (java.io.InputStream is = part.getInputStream()) {
                mf.setContent(is.readAllBytes()); // Java 9+
            }
            return mf;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture fichier '" + paramName + "' : " + e.getMessage(), e);
        }
    }

    // ---------- Helpers ----------

    /** Types qui se convertissent directement depuis une chaine. */
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

    /**
     * Instancie le POJO (constructeur sans argument) puis remplit ses champs
     * (publics ou prives) avec les parametres de la requete par nom.
     */
    private static Object bindObject(Class<?> type, HttpServletRequest request)
            throws ReflectiveOperationException {
        Object instance;
        try {
            instance = type.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("La classe " + type.getName()
                    + " doit avoir un constructeur sans argument pour le binding.");
        }

        if (request == null) {
            return instance;
        }

        // Parcours de tous les champs declares (publics ou prives)
        for (Field field : type.getDeclaredFields()) {
            String name = field.getName();
            String value = request.getParameter(name);
            if (value == null) continue;   // champ non fourni → on laisse la valeur par defaut

            field.setAccessible(true);
            Object converted = convert(value, field.getType());
            field.set(instance, converted);
        }

        return instance;
    }

    /** Conversion String -> type Java (String, primitif, enveloppes). */
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

        // Type non gere : on renvoie la chaine brute
        return value;
    }

    // Ancien appel sans requete, garde pour usage interne
    public Object invoke() throws ReflectiveOperationException {
        Object controller = controllerClass.getDeclaredConstructor().newInstance();
        method.setAccessible(true);
        return method.invoke(controller);
    }
}