package esante;

import java.sql.*;

public class SqlSession {
    
    private String path;
    private String user;
    private Connection connection;
    private String password;
    
    public String getUsername() {
        return user;
    }
    
    public String getPassword() {
        return password;
    }
    
    public SqlSession(String database, String user) {
        this.path = "jdbc:mysql://localhost/" + database;
        this.user = user;
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public boolean connect(String password) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(path, user, password); 
        } catch(ClassNotFoundException e) {
            System.out.println("Erreur: driver");
            return false;
        } catch(SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur: impossible de se connecter");
            return false;
        }
        System.out.println("Connection reussite");
        this.password = password;
        return true;
    }
    
    public boolean disconnect() {
        try {
            connection.close();
        } catch(SQLException e) {
            System.out.println("Erreur: impossible de se deconnecter");
            return false;
        }
        System.out.println("Deconnection reussite");
        return true;
    }
    
    public void printResults(ResultSet result) {
        try {
            ResultSetMetaData rsmd = result.getMetaData();
            int colCount = rsmd.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                System.out.print(rsmd.getColumnName(i) + "\t");
            }
            System.out.println();
            while(result.next()) {
                for (int i = 1; i <= colCount; i++) {
                    System.out.print(result.getString(i) + "\t");
                }
                System.out.println();
            }
        } catch(Exception e) {
            
        }
    }
    
    public ResultSet SqlSelect(String req) {
        Statement statement = null;
        ResultSet result = null;
        try {
            statement = connection.createStatement();
            result = statement.executeQuery(req);
            return result;
        } catch(Exception e) {
            return null;
        } 
    }
    public boolean SqlUpdate(String req) {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.executeUpdate(req);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
