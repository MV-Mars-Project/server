package be.howest.ti.mars.logic.data.util;

import org.h2.tools.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/*
MBL: this is only a starter class to use a H2 database.
To make this class useful, please complete it with the topics seen in the module OOA & SD
- Make sure the conf/config.json properties are correct.
- The h2 web console is available at http://localhost:9000
- The h2 database file is located at ~/mars-db
- Hint:
  - Mocking this repository is not needed. Create database creating and population script in plain SQL.
    Use the @Before or @Before each (depending on the type of test) to quickly setup a fully populated db.
 */
public class MarsConnection {
    private static final MarsConnection INSTANCE = new MarsConnection();
    private static final Logger logger = Logger.getLogger(MarsConnection.class.getName());
    private Server dbWebConsole;
    private String username;
    private String password;
    private String url;

    private MarsConnection() {
    }

    public static MarsConnection getInstance() {
        return INSTANCE;
    }

    public static void configure(String url, String username, String password, int console)
            throws SQLException {
        INSTANCE.username = username;
        INSTANCE.password = password;
        INSTANCE.url = url;
        INSTANCE.dbWebConsole = Server.createWebServer(
                "-ifNotExists",
                "-webPort", String.valueOf(console)).start();
        initDatabase();
        addEndpointsDB();
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(INSTANCE.url, INSTANCE.username, INSTANCE.password);
    }

    private static void executeScript(String filename) {
        try (
                Connection connection = getConnection();
                PreparedStatement stmt = connection.prepareStatement(readFile(filename))
        ) {
            stmt.executeUpdate();
        } catch (SQLException | IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private static String readFile(String fileName) throws IOException {
        try (InputStream resource = MarsConnection.class.getClassLoader().getResourceAsStream(fileName)) {
            if (resource == null) throw new NoSuchFileException("can't find file: " + fileName);
            return new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8)).lines().map(str -> str + "\n").collect(Collectors.joining());
        }
    }

    private static void initDatabase() {
        executeScript("h2/setupDB.sql");
    }

    private static void addEndpointsDB() {
        executeScript("h2/initEndpointsDB.sql");
    }

    public void cleanUp() {
        dbWebConsole.stop();
    }
}
