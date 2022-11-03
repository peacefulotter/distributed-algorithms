package cs451;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class Logger
{
    private static final boolean ENABLED = true;
    private static final Clock clock = new HighLevelClock();
    private static final Map<Long, Color> colorMap = new HashMap<>();

    public static void addColor( Color color )
    {
        colorMap.put( Thread.currentThread().getId(), color );
    }

    public enum Color
    {
        RED("\033[31m", "\033[0m"),
        BLUE("\033[32m", "\033[0m"),
        GREEN("\033[34m", "\033[0m");

        public final String c1, c2;

        Color( String c1, String c2 )
        {
            this.c1 = c1;
            this.c2 = c2;
        }
    }

    private static class TimeProvider
    {
        private final static long jvm_diff = System.currentTimeMillis() * 1000_000 - System.nanoTime();
        public static long getAccurateNow() {
            return System.nanoTime() + jvm_diff;
        }
    }

    private static class HighLevelClock extends Clock
    {
        private final ZoneId zoneId = ZoneId.systemDefault();

        static long nano_per_second = 1000_000_000L;

        @Override
        public ZoneId getZone() {
            return zoneId;
        }

        @Override
        public Clock withZone(ZoneId zoneId) {
            return new HighLevelClock();
        }

        @Override
        public Instant instant() {
            long nanos = TimeProvider.getAccurateNow();
            return Instant.ofEpochSecond(nanos/nano_per_second, nanos%nano_per_second);
        }
    }

    private static LocalTime time()
    {
        return LocalTime.now(clock);
    }


    public static void log( String s )
    {
        long id = Thread.currentThread().getId();
        Color color = colorMap.get( id );
        log( color, id, s );
    }

    public static void log( String prefix, String s )
    {
        log( "[" + prefix + "] " + s );
    }

    public static void log( Logger.Color color, long id , String s)
    {
        if ( !ENABLED ) return;
        String c1 = color == null ? "" : color.c1;
        String c2 = color == null ? "" : color.c2;
        System.out.println(c1 + time() + " " + id + c2 + " " + s);
    }
}
