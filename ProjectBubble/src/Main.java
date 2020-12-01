import java.util.Arrays;
import java.util.HashSet;

public class Main {
    public static void main(String[] args) {
        final String SQL_URL = "jdbc:mysql://localhost:3306/voter_data?serverTimezone=UTC";
        final String SQL_USERNAME = System.getenv("SQL_USERNAME");
        final String SQL_PASSWORD = System.getenv("SQL_PASSWORD");

        HashSet<String> commands = readArgs(args);

        //CT insert raw to db
        if(commands.contains("insert-raw-CT")) {
            for (int i = 1; i <= 4; i++) {
                String filepathCsv = "/Users/william/Documents/other/voter-data/CT-download/extracted-data/EXT" + i + ".csv";
                RawToDB.CT(SQL_URL, SQL_USERNAME, SQL_PASSWORD, filepathCsv);
            }
        }

    }

    //interpret commandline args (for CLI implementation later)
    private static HashSet<String> readArgs(String[] args)
    {
        HashSet<String> commands = new HashSet<>();
        //read args and add applicable commands

        if(Arrays.asList(args).contains("insert-raw-CT")) {
            commands.add("insert-raw-CT");
        }
        return commands;
    }

}
