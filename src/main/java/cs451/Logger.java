package cs451;

public class Logger
{
    private static final boolean DISABLED = false;

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
    public static void log( Logger.Color color, Host h, String s)
    {
        if ( DISABLED ) return;
        String c1 = color == null ? "" : color.c1;
        String c2 = color == null ? "" : color.c2;
        System.out.println(c1 + h + c2 + " " + s);
    }
}
