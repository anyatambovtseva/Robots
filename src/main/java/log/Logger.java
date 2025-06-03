package log;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;

public final class Logger {
    private static final LogWindowSource defaultLogSource;
    private static final TemporalAmount DEFAULT_MAX_LOG_AGE = Duration.of(1, ChronoUnit.HOURS);

    static {
        defaultLogSource = new LogWindowSource(100, DEFAULT_MAX_LOG_AGE);
    }

    private Logger() {
    }

    public static void debug(String strMessage) {
        defaultLogSource.append(LogLevel.Debug, strMessage);
    }

    public static void error(String strMessage) {
        defaultLogSource.append(LogLevel.Error, strMessage);
    }

    public static LogWindowSource getDefaultLogSource() {
        return defaultLogSource;
    }
}