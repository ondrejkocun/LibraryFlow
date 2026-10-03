package library.persistence;

import java.time.LocalDateTime;

public final class Logger {
    private Logger() {
    }

    public static void log(String message) {
        System.err.println("[" + LocalDateTime.now() + "] " + message);
    }
}
