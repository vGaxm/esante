/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package esante;


import java.sql.*;
import java.util.ArrayList;

public class Specialite extends SqlTable {

    Specialite() {
        data = new Object[2];
        tableName = "Specialite";
    }
    
    public int getSpecCode() { return (int)data[0]; }    
    public String getSpecName() { return (String)data[1]; } 


    static Specialite get(ResultSet rs) {
        Specialite sp = null;
        try {
            sp = new Specialite();
            sp.data[0] = rs.getInt(1);
            sp.data[1] = rs.getString(2);
        } catch(Exception e) {
            
        }
        return sp;
    }
    
    public static ArrayList<Specialite> getAll(SqlSession session) {
        ArrayList<Specialite> specs = new ArrayList<>();
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Specialite Where CodeSpec > 0");
            while(rs.next()) {
                specs.add(get(rs));
            }
            
        } catch(Exception e) {
            specs = null;
        }
        return specs;
    }
    
    public static Specialite findByCode(SqlSession session, int code) {
        Specialite sp = null;
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Specialite WHERE CodeSpec="+code);
            if(rs.next()) {
                sp = get(rs);
            }
            
        } catch(Exception e) {
            
        }
        return sp;
    }
}
