/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package esante;

import java.sql.*;

/**
 *
 * @author islam
 */
public class Donnees extends SqlTable {
    
    Donnees() {
        data = new Object[1];
        tableName = "Donnees";
    }
    
    public int getNbrResAn() { return (int)data[0]; } 
        
    public static Donnees find(SqlSession session) {
        Donnees don = null;
        try {
            ResultSet rs = session.SqlSelect("SELECT NbrResAn FROM Donnees");
            if(rs.next()) {
                don = get(rs);
            }
            
        } catch(Exception e) {
            
        }
        return don;
    }
    
    static Donnees get(ResultSet rs) {
        Donnees don = null;
        try {
            don = new Donnees();
            don.data[0] = rs.getInt(1);
        } catch(Exception e) {
            
        }
        return don;
    }
    
}
