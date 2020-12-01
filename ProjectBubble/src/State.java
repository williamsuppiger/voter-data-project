/*
 * Abstract class that will eventually be inherited by all state child classes.
 *
 */
public abstract class State {
    protected static String jdbcURL;
    protected static String sqlUsername;
    protected static String sqlPassword;

    protected State( String jdbcURL, String sqlUsername, String sqlPassword )
    {
        this.jdbcURL = jdbcURL;
        this.sqlUsername = sqlUsername;
        this.sqlPassword = sqlPassword;
    }

    public static void RawToDB(String filepath){};
    public static void GeoLookup(){};
    public static void GeoToDB(){};

}
