/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package esante;


import java.sql.*;
import java.util.ArrayList;

public class Service extends SqlTable {

    Service() {
        data = new Object[3];
        tableName = "Serv";
    }
    
    public int getServiceCode() { return (int)data[0]; }    
    public String getServiceName() { return (String)data[1]; }
    public String getSecteur() { return (String)data[2]; } 


    static Service get(ResultSet rs) {
        Service sp = null;
        try {
            sp = new Service();
            sp.data[0] = rs.getInt(1);
            sp.data[1] = rs.getString(2);
            sp.data[2] = rs.getString(3);
        } catch(Exception e) {
            
        }
        return sp;
    }
    
    public static ArrayList<Service> getAll(SqlSession session) {
        ArrayList<Service> specs = new ArrayList<>();
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Serv");
            while(rs.next()) {
                specs.add(get(rs));
            }
            
        } catch(Exception e) {
            specs = null;
        }
        return specs;
    }
   
    public static ArrayList<Service> findBySecteur(SqlSession session, String secteur) {
       ArrayList<Service> serv = new ArrayList<>();
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Serv WHERE Secteur like '" + secteur + "' OR Secteur IS NULL");
            while(rs.next()) {
                serv.add(get(rs));
            }
            
        } catch(Exception e) {
            
        }
        return serv;
    }  
    
    public static ArrayList<Service> findBySpecCode(SqlSession session, int code) {
       ArrayList<Service> serv = new ArrayList<>();
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Serv WHERE CodeService IN (SELECT CodeService FROM ServiceSpecialite WHERE CodeSpec=" + code + ")");
            if(rs.next()) {
                serv.add(get(rs));
            }
            
        } catch(Exception e) {
            
        }
        return serv;
    }    
    
    public static Service findByCode(SqlSession session, int code) {
        Service sp = null;
        try {
            ResultSet rs = session.SqlSelect("SELECT * FROM Serv WHERE CodeService="+code);
            if(rs.next()) {
                sp = get(rs);
            }
            
        } catch(Exception e) {
            
        }
        return sp;
    }
}
