/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package esante;


import java.sql.*;
import java.util.ArrayList;

public class Employe extends SqlTable {

    Employe() {
        data = new Object[9];
        tableName = "Employe";
    }
    
    public int getCode() { return (int)data[0]; }
    public String getNom() { return (String)data[1]; }
    public String getPrenom() { return (String)data[2]; }
    public String getSecteur() { return (String)data[3]; }
    public int getExp() { return (int)data[4]; }
    public int getSpecCode() { return (int)data[5]; }    
    public String getTel() { return (String)data[6]; }    
    public String getPass() { return (String)data[7]; }
    public int getNbrVisites() { return (int)data[8]; } 


    static Employe get(ResultSet rs) {
        Employe emp = null;
        try {
            emp = new Employe();
            emp.data[0] = rs.getInt(1);
            emp.data[1] = rs.getString(2);
            emp.data[2] = rs.getString(3);
            emp.data[3] = rs.getString(4);
            emp.data[4] = rs.getInt(5);
            emp.data[5] = rs.getInt(6);
            emp.data[6] = rs.getString(7);
            emp.data[7] = rs.getString(8);
            emp.data[8] = rs.getInt(9);
        } catch(Exception e) {
            
        }
        return emp;
    }
    
    public static boolean insert(SqlSession session, Employe emp) {
        boolean success = false;
        try {
            if (Employe.findByTel(session, emp.getTel()) != null) {
                ErrorMessage.show(null, "Ce numéro de téléphone existe déja !");
                return success;
            }
            
            String query = "INSERT INTO Employe VALUES (null, ?, ?, ?, ?, ?, ?, ?, default)";
            PreparedStatement pstmt = session.getConnection().prepareStatement(query);
            pstmt.setString(1, emp.getNom());
            pstmt.setString(2, emp.getPrenom());
            pstmt.setString(3, emp.getSecteur());
            pstmt.setInt(4, emp.getExp());
            pstmt.setInt(5, emp.getSpecCode());            
            pstmt.setString(6, emp.getTel());            
            pstmt.setString(7, emp.getPass());
            
            int rowsInserted = pstmt.executeUpdate();

            if (rowsInserted > 0) {
                success = true;
                            
                ResultSet rs = session.SqlSelect("SELECT last_insert_id()");
                rs.next();
                String CodeE = rs.getString(1);
                
                String username = "'emp" + CodeE +"'@'localhost'";
                System.out.println("User: " + username);
                session.SqlUpdate("DROP USER IF EXISTS " + username);
                session.SqlUpdate("CREATE USER "+username+" IDENTIFIED BY '"+emp.getPass()+"'");
                session.SqlUpdate("GRANT ALL PRIVILEGES ON *.* TO "+username);
                session.SqlUpdate("FLUSH PRIVILEGES");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return success;
    }
    
    public static boolean modify(SqlSession session, Employe emp) {
        boolean success = false;
        try {
            String query = """
                           UPDATE Employe
                           SET
                           NomE=?,PrenomE=?,SecteurE=?,ExpE=?,CodeSpec=?,TelE=?,PassE=?
                           WHERE CodeE=?
                           """;
            
            PreparedStatement pstmt = session.getConnection().prepareStatement(query);
            pstmt.setString(1, emp.getNom());
            pstmt.setString(2, emp.getPrenom());
            pstmt.setString(3, emp.getSecteur());
            pstmt.setInt(4, emp.getExp());
            pstmt.setInt(5, emp.getSpecCode());            
            pstmt.setString(6, emp.getTel());            
            pstmt.setString(7, emp.getPass());            
            pstmt.setInt(8, emp.getCode());

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                success = true;
                
                String username = "emp" + emp.getCode() +"'@'localhost";
                session.SqlUpdate("ALTER USER '"+username+"' IDENTIFIED BY '"+emp.getPass()+"'");
                session.SqlUpdate("FLUSH PRIVILEGES");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return success;
    } 
    
    public static ArrayList<Employe> findAll(SqlSession session) {
        return findAllOrdered(session, false);
    }
    
    public static ArrayList<Employe> findAllOrdered(SqlSession session, boolean ordered) {
        ArrayList<Employe> emps = new ArrayList<>();
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Employe " + (ordered ? "ORDER BY NomE ASC" : ""));
            while(rs.next()) {
                emps.add(get(rs));
            }
            
        } catch(Exception e) {
            emps = null;
        }
        return emps;
    }
    
    public static ArrayList<Employe> findBySecteur(SqlSession session, String secteur) {
        ArrayList<Employe> emps = new ArrayList<>();
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Employe WHERE SecteurE like '" + secteur + "'");
            while(rs.next()) {
                emps.add(get(rs));
            }
            
        } catch(Exception e) {
            emps = null;
        }
        return emps;
    }
    
    public static ArrayList<Employe> search(SqlSession session, String search) {
        ArrayList<Employe> emps = new ArrayList<>();
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Employe WHERE LOWER(CONCAT(NomE,PrenomE)) like '%" + search.toLowerCase() + "%'");
            while(rs.next()) {
                emps.add(get(rs));
            }
            
        } catch(Exception e) {
            emps = null;
            e.printStackTrace();
        }
        return emps;
    }
    
    public static Employe findByCode(SqlSession session, int code) {
        Employe emp = null;
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Employe WHERE CodeE="+code);
            if(rs.next()) {
                emp = get(rs);
            }
            
        } catch(Exception e) {
            
        }
        return emp;
    }

        
    public static Employe findByTel(SqlSession session, String tel) {
        Employe emp = null;
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Employe WHERE TelE like '"+tel+"'");
            if(rs.next()) {
                emp = get(rs);
            }
            
        } catch(Exception e) {
            
        }
        return emp;
    }
    
    public static ArrayList<Employe> searchByCodeOrName(SqlSession session, String search) {
        ArrayList<Employe> emps = new ArrayList<>();
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Employe WHERE LOWER(CONCAT(NomE,PrenomE,CodeE)) like '%" + search.toLowerCase() + "%'");
            while(rs.next()) {
                emps.add(get(rs));
            }
            
        } catch(Exception e) {
            emps = null;
            e.printStackTrace();
        }
        return emps;
    }
    
    
    public static void deleteByCode(SqlSession session, int code) {
        try {
            session.SqlUpdate("DELETE FROM Employe WHERE CodeE="+code);
        } catch(Exception e) {
            
        }
    }
}
