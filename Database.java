/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContainerStore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Database {
    
    Connection con;
    String url;
    
    /**
    * connects to customer database
    * @return con
    */
    
    public Connection users(){
        
        url = "jdbc:sqlite:users.db";
        
        try {
            
            con = DriverManager.getConnection(url);
            
        } catch (SQLException ex) {
            
            Logger.getLogger(LogIn.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return con;
    }
    
    /**
    * connects to customer database
    * @return con
    */
    
    public Connection customers(){
        
        url = "jdbc:sqlite:Customers.db";
        
        try {
            
            con = DriverManager.getConnection(url);
            
        } catch (SQLException ex) {
            
            Logger.getLogger(LogIn.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return con;
    }
    
    /**
    * connects to warehouse database
    * @return con
    */
        
    public Connection warehouse(){
        
        url = "jdbc:sqlite:Warehouse.db";
        
        try {
            
            con = DriverManager.getConnection(url);
            
        } catch (SQLException ex) {
            
            Logger.getLogger(LogIn.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return con;
    }
                
    /**
    * connects to inventory database
    * @return con
    */
        
    public Connection inventories(){
        
        url = "jdbc:sqlite:Inventorys.db";
        
        try {
            
            con = DriverManager.getConnection(url);
            
        } catch (SQLException ex) {
            
            Logger.getLogger(LogIn.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return con;
    }   
        
    /**
    * connects to Log database
    * @return con
    */
        
    public Connection Logs(){
        
        url = "jdbc:sqlite:Logs.db";
        
        try {
            
            con = DriverManager.getConnection(url);
            
        } catch (SQLException ex) {
            
            Logger.getLogger(LogIn.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return con;
    }
    
    /**
    * connects to container History database
    * @return con
    */  
    
    public Connection ContainerHistory(){

        url = "jdbc:sqlite:ContainerHistory.db";

        try {

            con = DriverManager.getConnection(url);

        } catch (SQLException ex) {

           Logger.getLogger(LogIn.class.getName()).log(Level.SEVERE, null, ex);
        }

        return con;
    }
}
