/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package esante;


import java.sql.*;
import java.util.ArrayList;

public class Patient extends SqlTable {
    
    Patient() {
        data = new Object[12];
        tableName = "Patient";
    }
    
    public String getNSS() { return (String)data[0]; }
    public String getNom() { return (String)data[1]; }
    public String getAlergie() { return (String)data[11]; }
    public String getPrenom() { return (String)data[2]; }
    public java.sql.Date getDateNaissance() { return (java.sql.Date)data[3]; }
    public String getAdresse() { return (String)data[4]; }
    public String getTel() { return (String)data[5]; }
    public String getEmail() { return (String)data[6]; } 
    public String getSexe() { return (String)data[7]; }
    public int getTaille() { return (Integer)data[8]; }
    public int getPoids() { return (Integer)data[9]; } 
    public int getNbrVisites() { return (Integer)data[10]; } 
    
    public void setNSS(String nss) { data[0] = nss; } 
    public void setAlergie(String aler) { data[11] = aler; }
    public void setNom(String nom) { data[1] = nom; }
    public void setPrenom(String prenom) { data[2] = prenom; }
    public void setAdresse(String adresse) { data[4] = adresse; }
    public void setTel(String tel) { data[5] = tel; }
    public void setEmail(String email) { data[6] = email; } 
    public void setTaille(int taille) { data[8] = taille; }
    public void setPoids(int poids) { data[9] = poids; } 
    
    static Patient get(ResultSet rs) {
        Patient pat = null;
        try {
            pat = new Patient();
            pat.data[0] = rs.getString(1);
            pat.data[1] = rs.getString(2);
            pat.data[2]= rs.getString(3);
            pat.data[3] = rs.getDate(4);
            pat.data[4] = rs.getString(5);
            pat.data[5] = rs.getString(6);
            pat.data[6] = rs.getString(7);            
            pat.data[7] = rs.getString(8);
            pat.data[8] = rs.getInt(9);
            pat.data[9] = rs.getInt(10);    
            pat.data[10] = rs.getInt(11);
            pat.data[11] = rs.getString(12);
        } catch(Exception e) {
            
        }
        return pat;
    }
    
    public static boolean insert(SqlSession session, Patient patient) {
        boolean success = false;
        try {
            String query = "INSERT INTO Patient VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, default, ?)";
            PreparedStatement pstmt = session.getConnection().prepareStatement(query);
            pstmt.setString(1, patient.getNSS());
            pstmt.setString(2, patient.getNom());
            pstmt.setString(3, patient.getPrenom());
            pstmt.setDate(4, patient.getDateNaissance());
            pstmt.setString(5, patient.getAdresse());
            pstmt.setString(6, patient.getTel());
            pstmt.setString(7, patient.getEmail());            
            pstmt.setString(8, patient.getSexe());
            pstmt.setInt(9, patient.getTaille());
            pstmt.setInt(10, patient.getPoids());
            pstmt.setString(11, patient.getAlergie());


            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                success = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return success;
    }

        public static boolean modify(SqlSession session, Patient pat) {
        boolean success = false;
        try {
            String query = """
                           UPDATE Patient
                           SET
                           AdresseP=?,
                           TelP=?,
                           EmailP=?,
                           Alergies=?
                           WHERE NssP=?
                           """;
            
            PreparedStatement pstmt = session.getConnection().prepareStatement(query);
            pstmt.setString(1, pat.getAdresse());
            pstmt.setString(2, pat.getTel());
            pstmt.setString(3, pat.getEmail());
            pstmt.setString(4, pat.getAlergie());
            pstmt.setString(5, pat.getNSS());

            int rowsInserted = pstmt.executeUpdate();
            success = rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return success;
    } 
    
    public static ArrayList<Patient> findAll(SqlSession session) {
        ArrayList<Patient> pats = new ArrayList<>();
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Patient");
            while(rs.next()) {
                pats.add(get(rs));
            }
            
        } catch(Exception e) {
            pats = null;
        }
        return pats;
    }
    
    public static Patient findByNSS(SqlSession session, String NSS) {
        Patient pat = null;
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Patient WHERE NssP like '"+NSS+"'");
            if(rs.next()) {
                pat = get(rs);
            }
            
        } catch(Exception e) {
            
        }
        return pat;
    }
    
    public static ArrayList<Patient> search(SqlSession session, String search) {
        ArrayList<Patient> pats = new ArrayList<>();
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Patient WHERE LOWER(CONCAT(NomP,PremonP)) like '%" + search.toLowerCase() + "%'");
            while(rs.next()) {
                pats.add(get(rs));
            }
            
        } catch(Exception e) {
            pats = null;
            e.printStackTrace();
        }
        return pats;
    }
    
    public static void deleteByNSS(SqlSession session, String NSS) {
        try {
            session.SqlUpdate("DELETE FROM Patient WHERE NssP like '"+NSS+"'");
        } catch(Exception e) {
            
        }
    }
    
    public static ArrayList<Patient> searchByNSS(SqlSession session, String NSS_beg, int max) {
          ArrayList<Patient> pats = new ArrayList<>();
        try {
            String req = "SELECT * FROM Patient WHERE NssP LIKE '" + NSS_beg + "%'";
            if (max != 0) req += " LIMIT " + max;
            ResultSet rs = session.SqlSelect(req);
            while(rs.next()) {
                pats.add(get(rs));
            }
            
        } catch(Exception e) {
            
        }
        return pats;
    } 
}
