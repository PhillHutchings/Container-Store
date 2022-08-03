/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContainerStore;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author hutch
 */
public class LogIn {
    
    Database db;
    Log log;
    PreparedStatement ps;
    ResultSet rs;
    MessageDigest md;
    
    private String action;
    private String details;
    
    public LogIn(){
        
        db = new Database();
        log = new Log();
        createUserDatabase();
    }
    
    /**
     * creates the user database
     */
    
    private void createUserDatabase(){
        
        try(Connection con  = db.users()){
            
            ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS logInData"
            + "(User STRING, "
                    + "Access STRING, "
                    + "Salt BLOB,"
                    + "Hash BLOB)");

            ps.executeUpdate();
                       
        } catch (SQLException ex) {
            
            Logger.getLogger(LogIn.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    /**
     * check if user data is already entered into the log in data table
     * @return 
     */
    
    public boolean checkFirstTime(){
        
        boolean first = true;
        
        try(Connection con = db.users()){
            
            ps = con.prepareStatement("SELECT * FROM LogInData");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                first = false;
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(LogIn.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return first;
    }
   
    /**
     * adds new username and password
     * @param userName
     * @param password 
     * @param access 
     */
    
    public void addNewUser(String userName, char[] password, String access){
                         
        String pass = new String(password);
        
        for(int i = 0; i < password.length; i++){
            
            password[i] = '*';
        }
        
        try{
            
            md = MessageDigest.getInstance("SHA-256");

            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            md.update(salt);

            byte[] hashedPassword = md.digest(pass.getBytes(StandardCharsets.UTF_8));
            
            try(Connection con = db.users()){
                
                ps = con.prepareStatement("INSERT INTO logInData VALUES (?,?,?,?)");
                ps.setString(1, userName);
                ps.setString(2, access);
                ps.setBytes(3, salt);
                ps.setBytes(4, hashedPassword);
                
                ps.executeUpdate();
                
                action = "User Added";                                 //logs action
   
                details = "User: '" + userName + "' Added";

                log.logAction(ContainerMain.user, "", 0, action, details, ContainerMain.timeStamp());
                
            } catch (SQLException ex) {
                
                 Logger.getLogger(LogIn.class.getName()).log(Level.SEVERE, null, ex);
             }

        } catch(NoSuchAlgorithmException e){

            Logger.getLogger(LogIn.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    /**
     * checks username and password
     * @param userName
     * @param password
     * @return 
     */
    
    public boolean checkuser(String userName, char[] password){
        
        boolean accept = false;
        String pass = new String(password);
        
        for(int i = 0; i < password.length; i++){
            
            password[i] = '*';
        }
 
        try(Connection con = db.users()){
             md = MessageDigest.getInstance("SHA-256");
            
            ps = con.prepareStatement("SELECT Salt, Hash FROM logInData WHERE user = ?");
            ps.setString(1, userName);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                byte[] salt = rs.getBytes("Salt");
                
                md.update(salt);

                byte[] hashedPassword = md.digest(pass.getBytes(StandardCharsets.UTF_8));               
                
                byte[] hPassword = rs.getBytes("Hash");
                
                if(Arrays.equals(hashedPassword, hPassword)){
                    
                    accept = true;
                }
 
            } 
        } catch (SQLException | NoSuchAlgorithmException ex) {
            
            Logger.getLogger(LogIn.class.getName()).log(Level.SEVERE, null, ex);
            
        }
        
        if(accept == false){          
            
            action = "Failed Log In";                                 //logs failed log in
   
            details = "User: '" + userName + "' Failed";

            log.logAction(ContainerMain.user, "", 0, action, details, ContainerMain.timeStamp());
            
        }
        
        return accept;
    }
    
    /**
     * checks users access
     * @param userName
     * @return 
     */
    
    public boolean checkAdmin(String userName){
        
        boolean admin = false;
        
        try(Connection con = db.users()){
            
            ps = con.prepareStatement("SELECT access FROM logInData WHERE user = ?");
            ps.setString(1, userName);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                if(rs.getString("Access").matches("ADMIN")){
                    
                    admin = true;
                }
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(LogIn.class.getName()).log(Level.SEVERE, null, ex);
        }
               
        return admin;
    }
    
    /**
     * removes user from log in data
     * @param user
     */
    
    public void removeUser(String user){
        
        try(Connection con = db.users()){
            
            ps = con.prepareStatement("DELETE FROM logInData WHERE user = ?");
            ps.setString(1, user);
            
            ps.executeUpdate();
            
            action = "User Removed";                                 //logs action
   
            details = "User: '" + user + "' Removed";

            log.logAction(ContainerMain.user, "", 0, action, details, ContainerMain.timeStamp());
            
        } catch (SQLException ex) {
            
            Logger.getLogger(LogIn.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
     /**
     * gets all users from log in data
     * @return 
     */
    
    public ArrayList<String> getUsers(){
        
        ArrayList<String> users = new ArrayList<>();
        
        try(Connection con = db.users()){
            
            ps = con.prepareStatement("SELECT user FROM logInData");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                users.add(rs.getString("user"));
                
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(LogIn.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return users;
    }
}
    

