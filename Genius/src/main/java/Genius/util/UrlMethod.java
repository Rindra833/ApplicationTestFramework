package Genius.util;

import java.util.Objects;

public class UrlMethod {
    private String path;
    private MethodeType method;

    public UrlMethod(MethodeType method, String path) {
        this.method = method;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public MethodeType getMethod() {
        return method;
    }

    public void setMethod(MethodeType method) {
        this.method = method;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UrlMethod that = (UrlMethod) obj;
        return method == that.method && path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, method);
    }

    @Override
    public String toString() {
        return "UrlMethod{" +
                "path='" + path + '\'' +
                ", method=" + method +
                '}';
    }

}
