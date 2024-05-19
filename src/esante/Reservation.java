/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package esante;

/**
 *
 * @author ixlam
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Reservation extends SqlTable {
    
    Reservation() {
        data = new Object[7];
        tableName = "Reservation";
    }
    
    public int getNumero() { return (int)data[0]; }
    public Date getDate() { return (Date)data[1]; }
    public String getType() { return (String)data[2]; }
    public int getCodeService() { return (int)data[3]; }
    public String getNSS() { return (String)data[4]; }
    public boolean getPaye() { return (boolean)data[5]; }
    public int getEmploye() { return (int)data[6]; }
    
    public void setDate(Date date) { data[1] = date; }
    public void setType(String type) { data[2] = type; }
    public void setCodeService(int cs) { data[3] = cs; }
    public void setEmploye(int ce) { data[6] = ce; } 
    public void setPaye(boolean paye) { data[5] = paye; }
    public void setNSS(String nss) { data[4] = nss; }
    
    public static boolean insert(SqlSession session, Reservation r) {
        boolean success = false;
        try {
            String query = "INSERT INTO Reservation VALUES (null, ?, ?, ?, ?, false, ?)";
            PreparedStatement pstmt = session.getConnection().prepareStatement(query);
            pstmt.setDate(1, r.getDate());
            pstmt.setString(2, r.getType());
            pstmt.setInt(3, r.getCodeService());
            pstmt.setString(4, r.getNSS());
            pstmt.setInt(5, r.getEmploye());
            
            ResultSet rs = session.SqlSelect("SELECT last_insert_id()");
            rs.next();
            
            int rowsInserted = pstmt.executeUpdate();
            success = rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return success;
    }
        
    public static int getLastNumero(SqlSession session) {
       int num = 0;
        try {
            ResultSet rs = session.SqlSelect("SELECT @@IDENTITY");
            if(rs.next()) {
                num = rs.getInt(1);
            }
            
        } catch(Exception e) {
            
        }
        return num;
    }
    
    public static boolean modify(SqlSession session, Reservation r) {
        boolean success = false;
        try {
            String query = """
                           UPDATE Reservation
                           SET
                           DateR=?,TypeR=?,CodeService=?,CodeE=?
                           WHERE NumeroR=?
                           """;
            
            PreparedStatement pstmt = session.getConnection().prepareStatement(query);
            pstmt.setDate(1, r.getDate());
            pstmt.setString(2, r.getType());
            pstmt.setInt(3, r.getCodeService());
            pstmt.setInt(4, r.getEmploye());            
            pstmt.setInt(5, r.getNumero());


            int rowsInserted = pstmt.executeUpdate();
                success = rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return success;
    }
    
    public static boolean setEnded(SqlSession session, Reservation r) {
        boolean success = false;
        try {
            String query = """
                           UPDATE Reservation
                           SET
                           TypeR='VISITE'
                           WHERE NumeroR=?
                           """;
            
            PreparedStatement pstmt = session.getConnection().prepareStatement(query);           
            pstmt.setInt(1, r.getNumero());


            int rowsInserted = pstmt.executeUpdate();
            success = rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return success;
    }
    
    static Reservation get(ResultSet rs) {
        Reservation res = null;
        try {
            res = new Reservation();
            res.data[0] = rs.getInt(1);
            res.data[1] = rs.getDate(2);
            res.data[2] = rs.getString(3);
            res.data[3] = rs.getInt(4);
            res.data[4] = rs.getString(5);
            res.data[5] = rs.getBoolean(6);
            res.data[6] = rs.getInt(7);
            
        } catch(Exception e) {
            
        }
        return res;
    }
    
    public static ArrayList<Reservation> findAll(SqlSession session) {
        ArrayList<Reservation> ress = new ArrayList<>();
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Reservation");
            while(rs.next()) {
                ress.add(get(rs));
            }
            
        } catch(Exception e) {
            ress = null;
        }
        return ress;
    }
    
    public static Reservation findByNumero(SqlSession session, int numero) {
        Reservation res = null;
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Reservation WHERE NumeroR="+numero);
            if(rs.next()) {
                res = get(rs);
            }
            
        } catch(Exception e) {
            
        }
        return res;
    }
    
    public static ArrayList<Reservation> findByPatientNSS(SqlSession session, String NSS) {
        ArrayList<Reservation> ress = new ArrayList<>(0);
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Reservation WHERE NssP like '"+ NSS + "'");
            while(rs.next()) {
                ress.add(get(rs));
            }
            
        } catch(Exception e) {
            ress = null;
        }
        return ress;
    }
    
    public static Reservation findByDate(SqlSession session, String date) {
        Reservation res = null;
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Reservation WHERE Date="+date);
            if(rs.next()) {
                res = get(rs);
            }
            
        } catch(Exception e) {
            
        }
        return res;
    }
    
    public static Reservation findByDateAfterForEmploye(SqlSession session, String date, int emp) {
        Reservation res = null;
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Reservation WHERE DateR >= '"+date+"' AND CodeE="+emp+" ORDER BY DateR ASC LIMIT 1");
            if(rs.next()) {
                res = get(rs);
            }
            
        } catch(Exception e) {
            e.printStackTrace();
        }
        return res;
    }
    
    public static ArrayList<Reservation> findByEmploye(SqlSession session, int code) {
        ArrayList<Reservation> ress = new ArrayList<>(0);
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Reservation WHERE CodeE=" + code);
            while(rs.next()) {
                ress.add(get(rs));
            }
            
        } catch(Exception e) {
            ress = null;
        }
        return ress;
    }
    
    public static ArrayList<Reservation> findByYearAndMonth(SqlSession session, Date date) {
        ArrayList<Reservation> ress = new ArrayList<>(0);
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Reservation WHERE DateR like '" + (new SimpleDateFormat("yyyy-MM")).format(date) + "%'");
            while(rs.next()) {
                ress.add(get(rs));
            }
            
        } catch(Exception e) {
            ress = null;
        }
        return ress;
    }

    public static void deleteByNumero(SqlSession session, int numero) {
        try {
            session.SqlUpdate("DELETE FROM Reservation WHERE NumeroR="+numero);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void deleteByEmploye(SqlSession session, int code) {
        try {
            session.SqlUpdate("DELETE FROM Reservation WHERE CodeE="+code);
        } catch(Exception e) {
            
        }
    }
    
    public static void deleteByPatientNSS(SqlSession session, String NSS) {
        try {
            session.SqlUpdate("DELETE FROM Reservation WHERE NssP like '"+NSS+"'");
        } catch(Exception e) {
            
        }
    }
}

