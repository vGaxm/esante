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
public class ResultatVisite extends SqlTable {

    public ResultatVisite() {
        data = new Object[4];
        tableName = "ResultatVisite";
    }
    
    public int getReservatio () { return (int)data[0]; } 
    public String getEtat() { return (String)data[1]; }  
    public String getTraitementt() { return (String)data[2]; }  
    public String getRecom() { return (String)data[3]; } 
        
    static ResultatVisite get(ResultSet rs) {
        ResultatVisite rv = null;
        try {
            rv = new ResultatVisite();
            rv.data[0] = rs.getInt(1);
            rv.data[1] = rs.getString(2);
            rv.data[2] = rs.getString(3);
            rv.data[3] = rs.getString(4);
        } catch(Exception e) {
            
        }
        return rv;
    }
    
}
