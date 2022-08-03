/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContainerStore;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class Log {
    
    private Database db;     
    private Warehouse wh;
    
    private PreparedStatement ps;
    private ResultSet rs;
    private DatabaseMetaData dbmd;

    
    public Log(){
        
        db = new Database();
        
        createActionHistoryTable();
 
    }
    
           
    /**
    * creates the Action Log table in Logs.db
    */
    
    private void createActionHistoryTable(){
        
        try(Connection con = db.Logs()){

            ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS ActionHistory"
                    + "(User STRING,"
                    + "Customer STRING,"
                    + "Id INTEGER,"
                    + "Action STRING,"
                    + "Details STRING,"
                    + "TimeStamp STRING)");

            ps.executeUpdate();
            
        }catch(SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }
    
    /**
    * logs program actions
    * @param user
    * @param customer
    * @param id
    * @param action
    * @param details
    * @param timeStamp 
    */
    
    public void logAction(String user, String customer, int id, String action, String details, String timeStamp){
        
       try(Connection con = db.Logs()){
           
           ps = con.prepareStatement("INSERT INTO ActionHistory VALUES (?,?,?,?,?,?)");
           
           ps.setString(1, user);
           ps.setString(2, customer);
           ps.setInt(3, id);
           ps.setString(4, action);
           ps.setString(5, details);
           ps.setString(6, timeStamp);
           
           ps.executeUpdate();
           
       }catch(SQLException ex) {
           
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * checks the ware house has been set up
     * @return 
     */
    
    public boolean isWarehouseManaged(){
       
        boolean warehouseSetUp = false;
        
        try(Connection con = db.Logs()){
     
            ps = con.prepareStatement("SELECT * FROM ActionHistory WHERE action = ?");
            
            ps.setString(1, "WAREHOUSE SET UP");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                warehouseSetUp = true;
                
            }
    
        } catch (SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return warehouseSetUp;
    }
    
    /**
    * logs action to action log text file in customer folder
    * @param id
    * @param action 
    */
    
    public void logCustomerAction(int id, String action){
        
        String actionLog = Customers.getCustomerActionLog(id);
        String time = ContainerMain.timeStamp();
        
        try(BufferedWriter bs = new BufferedWriter(new FileWriter(actionLog, true))){
            
                bs.write(action + " " + time);
                bs.newLine();
            
        }catch(IOException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
    * logs the inventory into the text file of the container
    * @param id
    * @param container
    * @param inventory 
    */
    
    public void logInventoryConTxt(int id, int container, String[][] inventory){
        
        String inventoryLog = Warehouse.getContainerInventoryFile(id, container);
        
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(inventoryLog, true))){
            
            for(int i = 0; i < inventory.length; i++){
                
                for(int j = 0; j < inventory[i].length; j++){
                    
                    bw.write(String.valueOf(inventory[i][j]) + " ");
                   
                }
                
                bw.newLine();
            }
            
        }catch(IOException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }      
    }
    /**
     * updates container history database
     * @param id
     * @param container
     * @param action 
     */
    
    public void updateContainerHistory(int id, int container, String action){
        
        String conn = String.valueOf(container);
        
        String name = getCustomerNameByID(id);
        
        String timeS = ContainerMain.timeStamp();
        
        try(Connection con = db.ContainerHistory()){
            
            ps = con.prepareStatement("INSERT INTO '" + conn + "' VALUES(?,?,?)");
            
            ps.setString(1, name);
            ps.setString(2, action);
            ps.setString(3, timeS);
            
            ps.executeUpdate();
            
        } catch (SQLException ex) {
            
           Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
    * gets all the customer information by id
     * @param id
    * @return 
    */
    
    public String getCustomerNameByID(int id){
          
        Object[] cusDetails = new Object[9];
        
        try(Connection con = db.customers()){
            
            ps = con.prepareStatement("SELECT * FROM customers WHERE id = ?");
            ps.setInt(1, id);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                cusDetails[0] = rs.getInt("ID");
                cusDetails[1] = rs.getString("Title");
                cusDetails[2] = rs.getString("FirstName");
                cusDetails[3] = rs.getString("LastName");
               
            }
            
        }catch(SQLException ex) {
            
           Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return String.valueOf("Id : " + cusDetails[0] + " Name: " + cusDetails[1] + " " + cusDetails[2] + " " + cusDetails[3]);
    }
    
       
    /**
     * gets the data from the container history table
     * @param container
     * @return 
     */
    
    public Object[][] getContainerHistory(int container){
        
        int rows = 0;
        try(Connection con = db.ContainerHistory()){            //gets number of rows
            
            ps = con.prepareStatement("SELECT COUNT(*) AS count FROM '" + container + "'");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                rows = rs.getInt("count");
                                 
            }     
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Object[][] conHis = new Object[rows][3];
        int place = 0;
        
        try(Connection con = db.ContainerHistory()){            //gets container history data
            
            ps = con.prepareStatement("SELECT * FROM '" + container + "'");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                conHis[place][0] = rs.getString("Owner");
                conHis[place][1] = rs.getString("Action");
                conHis[place][2] = rs.getString("TimeStamp");
                place++;
                                 
            }     
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return conHis;
    }
    
    /**
     * gets all users in activity history table
     * @return 
     */
    
   public ArrayList<String> getUsers(){
       
       ArrayList<String> users = new ArrayList<>();
       
       try(Connection con = db.Logs()){
           
           ps = con.prepareStatement("SELECT DISTINCT user FROM ActionHistory");
           
           rs = ps.executeQuery();
           
           while(rs.next()){
               
               users.add(rs.getString("user"));
           }
           
       } catch (SQLException ex) {
           
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       return users;
   }
         
    /**
     * gets all actions in activity history table
     * @return 
     */
    
   public ArrayList<String> getAction(){
       
       ArrayList<String> actions = new ArrayList<>();
       
       try(Connection con = db.Logs()){
           
           ps = con.prepareStatement("SELECT DISTINCT action FROM ActionHistory");
           
           rs = ps.executeQuery();
           
           while(rs.next()){
               
               actions.add(rs.getString("action"));
           }
           
       } catch (SQLException ex) {
           
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       return actions;
   }
           
    /**
     * gets all dates in activity history table
     * @return 
     */
    
   public ArrayList<String> getDates(){
       
       ArrayList<String> dates = new ArrayList<>();
       
       try(Connection con = db.Logs()){
           
           ps = con.prepareStatement("SELECT DISTINCT timeStamp FROM ActionHistory");
           
           rs = ps.executeQuery();
           
           while(rs.next()){
               
               dates.add(rs.getString("timeStamp"));        
           }
           
       } catch (SQLException ex) {
           
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       return dates;
   }
   
   /**
    * gets data from the action history table
    * @return 
    */
   
    public Object[][] getActionLog(){
       
        int count = 0;
       
        try(Connection con = db.Logs()){
           
           ps = con.prepareStatement("SELECT COUNT(*) AS count FROM ActionHistory");
           
           rs = ps.executeQuery();
           
           while(rs.next()){
                   
               count = rs.getInt("count");
           }
           
        } catch (SQLException ex) {
           
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        Object[][] actHis = new Object[count][6];
        int row = 0;
       
        try(Connection con = db.Logs()){
           
            ps = con.prepareStatement("SELECT * FROM ActionHistory");

            rs = ps.executeQuery();

            while(rs.next()){

                actHis[row][0] = rs.getString("user");
                actHis[row][1] = rs.getString("customer");
                actHis[row][2] = rs.getInt("id");
                actHis[row][3] = rs.getString("action");
                actHis[row][4] = rs.getString("details");
                actHis[row][5] = rs.getString("timeStamp");

                row++;
            }
           
        } catch (SQLException ex) {
           
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       return actHis;
   }
    
    /**
     * gets all data from action history where user, action and date match
     * @param user
     * @param action
     * @param date
     * @return 
     */
    
    public Object[][] getActionHistoryByUserActionDate(String user, String action, String date){
        
        int count = 0;
        
        try(Connection con = db.Logs()){
            
            ps = con.prepareStatement("SELECT COUNT(*) AS count FROM ActionHistory WHERE user = ? AND action = ? AND timeStamp = ?");
            ps.setString(1, user);
            ps.setString(2, action);
            ps.setString(3, date);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                count = rs.getInt("count");
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Object[][] actHis = new Object[count][6];
        int row = 0;
        
        try(Connection con = db.Logs()){
            
            ps = con.prepareStatement("SELECT * FROM ActionHistory WHERE user = ? AND action = ? AND timeStamp = ?");
            ps.setString(1, user);
            ps.setString(2, action);
            ps.setString(3, date);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                actHis[row][0] = rs.getString("user");
                actHis[row][1] = rs.getString("customer");
                actHis[row][2] = rs.getInt("id");
                actHis[row][3] = rs.getString("action");
                actHis[row][4] = rs.getString("details");
                actHis[row][5] = rs.getString("timeStamp");

                row++;
                
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
         return actHis;
    }
        
    /**
     * gets all data from action history by user
     * @param user
     * @return 
     */
    
    public Object[][] getActionHistoryByUser(String user){
        
        int count = 0;
        
        try(Connection con = db.Logs()){
            
            ps = con.prepareStatement("SELECT COUNT(*) AS count FROM ActionHistory WHERE user = ?");
            ps.setString(1, user);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                count = rs.getInt("count");
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Object[][] actHis = new Object[count][6];
        int row = 0;
        
        try(Connection con = db.Logs()){
            
            ps = con.prepareStatement("SELECT * FROM ActionHistory WHERE user = ?");
            ps.setString(1, user);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                actHis[row][0] = rs.getString("user");
                actHis[row][1] = rs.getString("customer");
                actHis[row][2] = rs.getInt("id");
                actHis[row][3] = rs.getString("action");
                actHis[row][4] = rs.getString("details");
                actHis[row][5] = rs.getString("timeStamp");

                row++;
                
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
         return actHis;
    }
        
    /**
     * gets all data from action history where user and action match
     * @param user
     * @param action
     * @return 
     */
    
    public Object[][] getActionHistoryByUserAction(String user, String action){
        
        int count = 0;
        
        try(Connection con = db.Logs()){
            
            ps = con.prepareStatement("SELECT COUNT(*) AS count FROM ActionHistory WHERE user = ? AND action = ?");
            ps.setString(1, user);
            ps.setString(2, action);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                count = rs.getInt("count");
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Object[][] actHis = new Object[count][6];
        int row = 0;
        
        try(Connection con = db.Logs()){
            
            ps = con.prepareStatement("SELECT * FROM ActionHistory WHERE user = ? AND action = ?");
            ps.setString(1, user);
            ps.setString(2, action);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                actHis[row][0] = rs.getString("user");
                actHis[row][1] = rs.getString("customer");
                actHis[row][2] = rs.getInt("id");
                actHis[row][3] = rs.getString("action");
                actHis[row][4] = rs.getString("details");
                actHis[row][5] = rs.getString("timeStamp");

                row++;
                
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
         return actHis;
    }
        
    /**
     * gets all data from action history where user and date match
     * @param user
     * @param date
     * @return 
     */
    
    public Object[][] getActionHistoryByUserDate(String user, String date){
        
        int count = 0;
        
        try(Connection con = db.Logs()){
            
            ps = con.prepareStatement("SELECT COUNT(*) AS count FROM ActionHistory WHERE user = ? AND timeStamp = ?");
            ps.setString(1, user);
            ps.setString(2, date);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                count = rs.getInt("count");
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Object[][] actHis = new Object[count][6];
        int row = 0;
        
        try(Connection con = db.Logs()){
            
            ps = con.prepareStatement("SELECT * FROM ActionHistory WHERE user = ? AND timeStamp = ?");
            ps.setString(1, user);
            ps.setString(2, date);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                actHis[row][0] = rs.getString("user");
                actHis[row][1] = rs.getString("customer");
                actHis[row][2] = rs.getInt("id");
                actHis[row][3] = rs.getString("action");
                actHis[row][4] = rs.getString("details");
                actHis[row][5] = rs.getString("timeStamp");

                row++;
                
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
         return actHis;
    }
           
    /**
     * gets all data from action history where action match
     * @param action
     * @return 
     */
    
    public Object[][] getActionHistoryByAction(String action){
        
        int count = 0;
        
        try(Connection con = db.Logs()){
            
            ps = con.prepareStatement("SELECT COUNT(*) AS count FROM ActionHistory WHERE action = ?");
            ps.setString(1, action);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                count = rs.getInt("count");
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Object[][] actHis = new Object[count][6];
        int row = 0;
        
        try(Connection con = db.Logs()){
            
            ps = con.prepareStatement("SELECT * FROM ActionHistory WHERE action = ?");
            ps.setString(1, action);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                actHis[row][0] = rs.getString("user");
                actHis[row][1] = rs.getString("customer");
                actHis[row][2] = rs.getInt("id");
                actHis[row][3] = rs.getString("action");
                actHis[row][4] = rs.getString("details");
                actHis[row][5] = rs.getString("timeStamp");

                row++;
                
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
         return actHis;
    }
       
    /**
     * gets all data from action history where action and date match
     * @param action
     * @param date
     * @return 
     */
    
    public Object[][] getActionHistoryByActionDate(String action, String date){
        
        int count = 0;
        
        try(Connection con = db.Logs()){
            
            ps = con.prepareStatement("SELECT COUNT(*) AS count FROM ActionHistory WHERE action = ? AND timeStamp = ?");
            ps.setString(1, action);
            ps.setString(2, date);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                count = rs.getInt("count");
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Object[][] actHis = new Object[count][6];
        int row = 0;
        
        try(Connection con = db.Logs()){
            
            ps = con.prepareStatement("SELECT * FROM ActionHistory WHERE  action = ? AND timeStamp = ?");
            ps.setString(1, action);
            ps.setString(2, date);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                actHis[row][0] = rs.getString("user");
                actHis[row][1] = rs.getString("customer");
                actHis[row][2] = rs.getInt("id");
                actHis[row][3] = rs.getString("action");
                actHis[row][4] = rs.getString("details");
                actHis[row][5] = rs.getString("timeStamp");

                row++;
                
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
         return actHis;
    }
                
    /**
     * gets customer with most containers in database
     * @return 
     */
    
    public Object[][] getMostContainers(){
        
        int count = 0;
        
        try(Connection con = db.customers()){
            
            ps = con.prepareStatement("SELECT COUNT(*) AS count FROM customers");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                count = rs.getInt("count");
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Object[][] cusDetails = new Object[count][9];
        int place = 0;
        
        try(Connection con = db.customers()){
            
            ps = con.prepareStatement("SELECT * FROM customers ORDER BY containers DESC");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                cusDetails[place][0] = rs.getInt("ID");
                cusDetails[place][1] = rs.getString("Title");
                cusDetails[place][2] = rs.getString("FirstName");
                cusDetails[place][3] = rs.getString("LastName");
                cusDetails[place][4] = rs.getInt("Telephone");
                cusDetails[place][5] = rs.getString("Email");
                cusDetails[place][6] = rs.getString("Address");   
                cusDetails[place][7] = rs.getInt("Containers");
                cusDetails[place][8] = rs.getString("InDate");
                
                place++;
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return cusDetails;
    }

    /**
     * gets counts of all the users actions (view analytic window)
     * @param user
     * @return 
     */
    
    public Object[][] getUserData(String user){
        
        Object[][] userData = new Object[1][10];
        
        try(Connection con = db.Logs()){
            
            ps = con.prepareStatement("SELECT COUNT(*) AS count FROM ActionHistory WHERE user = ?");
            ps.setString(1, user);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                userData[0][0] = user;                      //user
                userData[0][1] = rs.getInt("count");            //Entries
                
                ps = con.prepareStatement("SELECT COUNT(*) AS count FROM ActionHistory WHERE user = ? AND action = ?");
                ps.setString(1, user);
                ps.setString(2, "Add New Customer");
                
                rs = ps.executeQuery();
                
                while(rs.next()){
                    
                    userData[0][2] = rs.getInt("count");            //customers added
                    
                    ps = con.prepareStatement("SELECT COUNT(*) AS count FROM ActionHistory WHERE user = ? AND action = ?");
                    ps.setString(1, user);
                    ps.setString(2, "Created New Container");

                    rs = ps.executeQuery();

                    while(rs.next()){

                        userData[0][3] = rs.getInt("count");                //containers created
                    
                        ps = con.prepareStatement("SELECT COUNT(*) AS count FROM ActionHistory WHERE user = ? AND action = ?");
                        ps.setString(1, user);
                        ps.setString(2, "Container Moved");

                        rs = ps.executeQuery();

                        while(rs.next()){

                            userData[0][4] = rs.getInt("count");                //containers moved
                    
                            ps = con.prepareStatement("SELECT COUNT(*) AS count FROM ActionHistory WHERE user = ? AND action = ?");
                            ps.setString(1, user);
                            ps.setString(2, "Container Emptied");

                            rs = ps.executeQuery();

                            while(rs.next()){

                                userData[0][5] = rs.getInt("count");            //containers emptied

                                ps = con.prepareStatement("SELECT COUNT(*) AS count FROM ActionHistory WHERE user = ? AND action = ?");
                                ps.setString(1, user);
                                ps.setString(2, "Inventory Added");

                                rs = ps.executeQuery();

                                while(rs.next()){

                                    userData[0][6] = rs.getInt("count");            //inventories added

                                    ps = con.prepareStatement("SELECT COUNT(*) AS count FROM ActionHistory WHERE user = ? AND action = ?");
                                    ps.setString(1, user);
                                    ps.setString(2, "Item Updated");

                                    rs = ps.executeQuery();

                                    while(rs.next()){

                                        userData[0][7] = rs.getInt("count");            //items updated

                                        ps = con.prepareStatement("SELECT COUNT(*) AS count FROM ActionHistory WHERE user = ? AND action = ?");
                                        ps.setString(1, user);
                                        ps.setString(2, "Item removed");

                                        rs = ps.executeQuery();

                                        while(rs.next()){

                                            userData[0][8] = rs.getInt("count");            //items removed
                                            
                                            ps = con.prepareStatement("SELECT COUNT(*) AS count FROM ActionHistory WHERE user = ? AND action = ?");
                                            ps.setString(1, user);
                                            ps.setString(2, "Removed From Database");

                                            rs = ps.executeQuery();

                                            while(rs.next()){

                                                userData[0][9] = rs.getInt("count");            //customers removed

                                            }
                                        }  
                                    }
                                } 
                            }   
                        }
                    }
                }
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return userData;
    }
}
