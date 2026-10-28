package mg.rr.framework;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Sprint 9 : gestionnaire centralisé des sessions.
 * - Map<sessionId, Session>
 * - Nettoyage périodique pour éviter les fuites mémoire.
 * - Cookie "MYSESSIONID" pour lier le client à sa session.
 */
public class SessionManager {

    private static final String COOKIE_NAME = "MYSESSIONID";
    private static final long CLEAN_INTERVAL_MINUTES = 5;

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final long timeoutMillis;
    private final ScheduledExecutorService cleaner;

    public SessionManager(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        this.cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SessionCleaner");
            t.setDaemon(true);
            return t;
        });
        this.cleaner.scheduleAtFixedRate(
            this::cleanExpired,
            CLEAN_INTERVAL_MINUTES,
            CLEAN_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        );
    }

    /**
     * Récupère ou crée la session du client.
     * Met à jour le cookie si besoin.
     */
    public Session getSession(HttpServletRequest req, HttpServletResponse res) {
        String sessionId = null;

        // 1. Cherche l'ID dans les cookies
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if (COOKIE_NAME.equals(c.getName())) {
                    sessionId = c.getValue();
                    break;
                }
            }
        }

        // 2. Cherche la session correspondante
        Session session = null;
        if (sessionId != null) {
            session = sessions.get(sessionId);
            // Session expirée ?
            if (session != null && System.currentTimeMillis() - session.getLastAccessedTime() > timeoutMillis) {
                sessions.remove(sessionId);
                session = null;
            }
        }

        // 3. Création si nécessaire
        if (session == null) {
            session = new Session();
            sessions.put(session.getId(), session);

            if (res != null) {
                Cookie cookie = new Cookie(COOKIE_NAME, session.getId());
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge((int) (timeoutMillis / 1000));
                res.addCookie(cookie);
            }
        }

        session.access();
        return session;
    }

    /** Nettoyage des sessions inactives. */
    private void cleanExpired() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(e -> now - e.getValue().getLastAccessedTime() > timeoutMillis);
    }

    public void shutdown() {
        cleaner.shutdownNow();
    }
}