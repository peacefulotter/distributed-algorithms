package cs451.utils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class Logger
{
    private static final boolean ENABLED = true;
    private static final boolean DEBUG_ENABLED = true;
    private static final Clock clock = new HighLevelClock();
    private static final Map<Integer, Color> colorMap = new HashMap<>();

    public Logger()
    {
        colorMap.put( 0, Color.WHITE );
    }

    public static void addColor( int id, Color color )
    {
        colorMap.put( id, color );
    }

    public enum Color
    {
        RED("\033[31m", "\033[0m"),
        BLUE("\033[34m", "\033[0m"),
        GREEN("\033[32m", "\033[0m"),
        WHITE("", "");

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


    public static void log( Object o )
    {
        if ( !DEBUG_ENABLED ) return;
        log(0, o);
    }

    public static void log( int id, Object o )
    {
        Color color = colorMap.get( id );
        log( color, "", o );
    }

    public static void log( String prefix, Object o )
    {
        if ( !DEBUG_ENABLED ) return;
        log( 0, "[" + prefix + "] " + o );
    }

    private static String formatPrefix( int id, String prefix )
    {
        return "[" + id + " " + prefix + "]";
    }

    public static void log( int id, String prefix, Object o )
    {
        Color color = colorMap.get( id );
        log( color, formatPrefix( id, prefix ),  o );
    }

    public static void log( Logger.Color color, String prefix, Object o)
    {
        if ( !ENABLED ) return;
        String c1 = color == null ? "" : color.c1;
        String c2 = color == null ? "" : color.c2;
        System.out.println(c1 + time() + " " + prefix + " " + c2 + " " + o);
    }

    public static void print( int id, String prefix, Object o )
    {
        Color color = colorMap.get( id );
        String c1 = color == null ? "" : color.c1;
        String c2 = color == null ? "" : color.c2;
        System.out.println(c1 + time() + " " + formatPrefix( id, prefix ) + " " + c2 + " " + o);
    }
}
