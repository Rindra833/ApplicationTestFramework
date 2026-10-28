package mg.rr.framework;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sprint 9 : Session personnalisée.
 * Une instance par client, stockée côté serveur dans SessionManager.
 */
public class Session {

    private final String id;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final long creationTime;
    private long lastAccessedTime;

    public Session() {
        this.id = UUID.randomUUID().toString();
        this.creationTime = System.currentTimeMillis();
        this.lastAccessedTime = this.creationTime;
    }

    public String getId() { return id; }
    public long getCreationTime() { return creationTime; }
    public long getLastAccessedTime() { return lastAccessedTime; }

    /** Met à jour l'heure du dernier accès (pour le timeout). */
    public void access() { this.lastAccessedTime = System.currentTimeMillis(); }

    // ---- Attributs de session ----

    public Object getAttribute(String name) { return attributes.get(name); }

    public void setAttribute(String name, Object value) {
        if (value == null) attributes.remove(name);
        else attributes.put(name, value);
    }

    public void removeAttribute(String name) { attributes.remove(name); }

    public Map<String, Object> getAttributes() { return attributes; }

    /** Détruit tous les attributs (utile pour logout). */
    public void invalidate() { attributes.clear(); }
}