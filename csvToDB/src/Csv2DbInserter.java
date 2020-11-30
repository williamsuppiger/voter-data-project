import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Csv2DbInserter {
    //modified from: https://www.codejava.net/coding/java-code-example-to-insert-data-from-csv-to-database

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/voter_data?serverTimezone=UTC";
        String username = System.getenv("SQL_USERNAME");
        String password = System.getenv("SQL_PASSWORD");
        String filepathCsv = "/EXT3.csv";
        CTcsvToDB(url, username, password, filepathCsv);
    }

    //insert csv data from CT into voter data database
    public static void CTcsvToDB(String jdbcURL, String sqlUsername,
                                 String sqlPassword, String csvFilepath) {
        //setup starting variables
        final int BATCH_SIZE = 20;
        final int NUM_COMMAS = 43;
        Connection connection = null;
        final String[] sqlFields = { "town_id", "voter_id", "last_name", "first_name",
                "middle_name", "name_prefix", "name_suffix", "cd_status_code",
                "cd_off_reason", "voting_district", "voting_precinct", "state_congress_code",
                "state_senate_code", "state_assembly_code", "address_number", "address_unit",
                "street_name", "town_name", "state", "zip5", "zip4", "election_history" };
        //csv column num that coorelate to sqlFields (i.e. town_id is the first column in the csv)
        final int[] csvFields = {1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15,  21, 22, 23,
                24, 25, 26, 27, 43};
        try {
            long start = System.currentTimeMillis();

            connection = DriverManager.getConnection(jdbcURL, sqlUsername, sqlPassword);
            connection.setAutoCommit(false);

            StringBuffer sqlCommand = new StringBuffer("INSERT IGNORE INTO ct_raw(");
            //setup fields
            for (String s : sqlFields)
            {
                sqlCommand.append(s + ", ");
            }
            //remove final comma and space from INSERT fields
            sqlCommand.setLength(sqlCommand.length() - 2);
            sqlCommand.append(")  VALUES ( ");
            for(int i = 1; i <= sqlFields.length; i++)
            {
                sqlCommand.append("?, ");
            }
            //remove final comma and space VALUE fields
            sqlCommand.setLength(sqlCommand.length() - 2);
            sqlCommand.append(") ");

            //convert to sql prepared statement
            PreparedStatement statement = connection.prepareStatement(sqlCommand.toString());

            //loop over each line in csv
            int count = 0;
            InputStream fis=new FileInputStream(csvFilepath);
            BufferedReader br=new BufferedReader(new InputStreamReader(fis));
            for (String line = br.readLine(); line != null; line = br.readLine(), count++) {
                String[] csvColumns = line.split(",", NUM_COMMAS);

                //handle last line blank
                if(line.length() == 1) {
                    break;
                }

                //for each column needed to be inserted
                int fieldCount = 1;
                for(int i : csvFields)
                {
                    //since csvColumns is 0th indexed and csvFields is 1st indexed, use i - 1
                    statement.setString(fieldCount, csvColumns[i - 1]);
                    fieldCount++;
                }
                statement.addBatch();

                if (count % BATCH_SIZE == 0) {

                    statement.executeBatch();
                    if (count % 10000 == 0) {
                        System.out.println("Current count:" + count);
                    }
                }
            }
            System.out.println("Final count:" + count);

            br.close();
            // execute the remaining queries
            statement.executeBatch();

            connection.commit();
            connection.close();

            long end = System.currentTimeMillis();
            System.out.println("Execution Time: " + (end - start));
        } catch (IOException ex) {
            System.err.println(ex);
        } catch (SQLException ex) {
            ex.printStackTrace();

            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}