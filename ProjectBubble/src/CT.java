import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/*
 * All methods and data manipulation related to Connecticut
 * (add State super class later for cleaner design)
 */
public class CT {
    //insert csv data from CT into voter_data.ct_raw schema
    public static void RawToDB(String jdbcURL, String sqlUsername,
                               String sqlPassword, String csvFilepath) {
        //setup starting variables
        final int BATCH_SIZE = 15;
        final int NUM_COMMAS = 44;
        Connection connection = null;
        final String[] sqlFields = { "town_id", "voter_id", "last_name", "first_name",
                "middle_name", "name_prefix", "name_suffix", "cd_status_code",
                "cd_off_reason", "voting_district", "voting_precinct", "state_congress_code",
                "state_senate_code", "state_assembly_code", "address_number", "address_unit",
                "street_name", "town_name", "state", "zip5", "zip4", "dob", "phone_number",
                "party_code", "unqualified_party_code", "gender", "registration_date",
                "election_history" };
        //csv column num that coorelate to sqlFields (i.e. town_id is the first column in the csv)
        final int[] csvFields = {1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15,  21, 22, 23,
                24, 25, 26, 27, 38, 39, 40, 41, 42, 43, 44};
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
                    statement.setString(fieldCount, csvColumns[i - 1].trim());
                    fieldCount++;
                }
                statement.addBatch();

                if (count % BATCH_SIZE == 0) {

                    statement.executeBatch();
                }
                if(count % 10000 == 0)
                {
                    System.out.print('\r' + "count: " + count);
                }
            }
            System.out.println();
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