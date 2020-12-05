import com.opencsv.CSVReader;

import java.io.*;
import java.sql.*;

/*
 * All methods and data manipulation related to Connecticut
 * (add State super class later for cleaner design)
 */
public class CT {

    //insert csv data from CT into voter_data.ct_raw schema
    public static void extractRaw(String jdbcURL, String sqlUsername,
                                  String sqlPassword, String csvFilepath) {
        //setup starting variables
        final int BATCH_SIZE = 15;
        final int NUM_COMMAS = 44;
        Connection connection = null;
        final String[] sqlFields = {"town_id", "voter_id", "last_name", "first_name",
                "middle_name", "name_prefix", "name_suffix", "cd_status_code",
                "cd_off_reason", "voting_district", "voting_precinct", "state_congress_code",
                "state_senate_code", "state_assembly_code", "address_number", "address_unit",
                "street_name", "town_name", "state", "zip5", "zip4", "dob", "phone_number",
                "party_code", "unqualified_party_code", "gender", "registration_date",
                "election_history"};
        //csv column num that coorelate to sqlFields (i.e. town_id is the first column in the csv)
        final int[] csvFields = {1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15, 21, 22, 23,
                24, 25, 26, 27, 38, 39, 40, 41, 42, 43, 44};
        try {
            long start = System.currentTimeMillis();

            connection = DriverManager.getConnection(jdbcURL, sqlUsername, sqlPassword);
            connection.setAutoCommit(false);

            StringBuffer sqlCommand = new StringBuffer("INSERT IGNORE INTO ct_raw(");
            //setup fields
            for (String s : sqlFields) {
                sqlCommand.append(s + ", ");
            }
            //remove final comma and space from INSERT fields
            sqlCommand.setLength(sqlCommand.length() - 2);
            sqlCommand.append(")  VALUES ( ");
            for (int i = 1; i <= sqlFields.length; i++) {
                sqlCommand.append("?, ");
            }
            //remove final comma and space VALUE fields
            sqlCommand.setLength(sqlCommand.length() - 2);
            sqlCommand.append(") ");

            //convert to sql prepared statement
            PreparedStatement statement = connection.prepareStatement(sqlCommand.toString());

            //loop over each line in csv
            int count = 0;
            InputStream fis = new FileInputStream(csvFilepath);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            for (String line = br.readLine(); line != null; line = br.readLine(), count++) {
                String[] csvColumns = line.split(",", NUM_COMMAS);

                //handle last line blank
                if (line.length() == 1) {
                    break;
                }

                //for each column needed to be inserted
                int fieldCount = 1;
                for (int i : csvFields) {
                    //since csvColumns is 0th indexed and csvFields is 1st indexed, use i - 1
                    statement.setString(fieldCount, csvColumns[i - 1].trim());
                    fieldCount++;
                }
                statement.addBatch();

                if (count % BATCH_SIZE == 0) {

                    statement.executeBatch();
                }
                if (count % 10000 == 0) {
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

    public static void getCoordsCensus(String jdbcURL, String sqlUsername,
                                       String sqlPassword, String stateCode) {
        // extract MAX_ROWS rows at a time from ct_clean table in DB
        final int MAX_ROWS = 5000;
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(jdbcURL, sqlUsername, sqlPassword);
            connection.setAutoCommit(false); //false for prod

            //sql read vars
            String query = "SELECT voter_id, address_number, street_name, " +
                    "town_name, state, zip5 FROM ct_clean";
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(query);

            //csv write vars
            StringBuilder sb = new StringBuilder();

            // iterate through the java resultset adding lines to csv
            int count = 0;
            System.out.print('\r' + "count: " + count);
            while (rs.next()) {
                //format: voterid, address_number street_name, town_name, state, zip5
                sb.append(rs.getString("voter_id"));
                sb.append(',');
                sb.append(rs.getString("address_number"));
                sb.append(' ');
                sb.append(rs.getString("street_name"));
                sb.append(',');
                sb.append(rs.getString("town_name"));
                sb.append(',');
                sb.append(rs.getString("state"));
                sb.append(',');
                sb.append(rs.getString("zip5"));
                sb.append('\n');
                // increase count forr tracking
                count++;

                // if reached max rows that the census can handle
                if (count % MAX_ROWS == 0) {
                    // call extractCoords to get coords into db
                    extractCoordsCensus(sb, connection);
                    sb = new StringBuilder();
                    System.out.print('\r' + "count: " + count);
                }
                //save changes every 10 stages lookups
                if (count % (MAX_ROWS*10) == 0) {
                    connection.commit();
                }
            }
            extractCoordsCensus(sb, connection); // lookup remaining records
            System.out.println();
            System.out.println("Final count:" + count);
            // clean up
            st.close();
            connection.commit();
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // looks up census data and inserts into DB
    public static String extractCoordsCensus(StringBuilder sb, Connection connection) {
        final String tempFileName = "temp.csv";
        final int BATCH_SIZE = 20;
        // create temporary csv formatted for Census lookup
        try {
            FileWriter fileWriter = new FileWriter(tempFileName);
            fileWriter.write(sb.toString());
            fileWriter.flush();

            // send census 10,000 row CSV
            ProcessBuilder pb = new ProcessBuilder(
                    "curl",
                    "--form", "addressFile=@" + tempFileName,
                    "--form", "benchmark=Public_AR_Current",
                    "https://geocoding.geo.census.gov/geocoder/locations/addressbatch");
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(pb.start().getInputStream()));

            //insert Census data into DB
            String sqlCommand = "INSERT INTO ct_geo VALUES (?,?,?,point(?, ?),?,?, 1)";
            PreparedStatement statement = connection.prepareStatement(sqlCommand);
            CSVReader csvReader = new CSVReader(br);
            String[] line;
            int count = 0;
            // iterate over each record in the file
            while ((line = csvReader.readNext()) != null) {

                // parse match and non match separately since they have different row lengths
                if(line[2].equals("Match"))
                {
                    //split up longitude and latitude
                    String[] coords = line[5].split(",");
                    statement.setString(1, line[0]);
                    statement.setString(2, line[2]);
                    statement.setString(3, line[3]);
                    //insert point as lat, long
                    statement.setString(4, coords[1]);
                    statement.setString(5, coords[0]);
                    statement.setString(6, line[6]);
                    statement.setString(7, line[7]);
                }
                // non-match or tie
                else
                {
                    statement.setString(1, line[0]);
                    statement.setString(2, line[2]);
                    statement.setNull(3, java.sql.Types.VARCHAR);
                    statement.setNull(4, Types.DOUBLE);
                    statement.setNull(5, Types.DOUBLE);
                    statement.setNull(6, java.sql.Types.VARCHAR);
                    statement.setNull(7, java.sql.Types.VARCHAR);

                }

                count++;
                statement.addBatch();
                if (count % BATCH_SIZE == 0) {
                    statement.executeBatch();
                }
            }
            // clean up
            csvReader.close();
            statement.executeBatch();
            br.close();
            new File(tempFileName).delete();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // store response in temporary file to return
        return null;
    }
}