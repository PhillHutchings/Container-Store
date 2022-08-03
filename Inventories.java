/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContainerStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class Inventories {
    
    private Database db;
    private Log log;
    private Customers cs;
    
    private PreparedStatement ps;
    private ResultSet rs;
    private DatabaseMetaData dbmd;
    
    private String action;
    private String details;
    
    public Inventories(){
        
        db = new Database();
        log = new Log();
        cs = new Customers();
    }
    
    
    /**
    * gets customer name by id
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

                cusDetails[0] = rs.getString("Title");
                cusDetails[1] = rs.getString("FirstName");
                cusDetails[2] = rs.getString("LastName");
               
            }
            
        }catch(SQLException ex) {
            
            Logger.getLogger(Inventories.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return String.valueOf(" Name: " + cusDetails[0] + " " + cusDetails[1] + " " + cusDetails[2]);
    }
    
    /**
    * adds the items into the chosen container
    * @param id
    * @param container
    * @param numOfItems
    * @param item
    * @param description
    * @param condition
    * @param photoFile 
    */
    
    public void addToInventory(int id, int container, int[] numOfItems, String[] item, String[] description, String[] condition, String[] photoFile){
                
        try(Connection con = db.inventories()){
            
            Object[][] currentItems = getInventory(container);         //gets current inventory items to check if item needs to be added new of item count updated
            
            for(int i = 0; i < numOfItems.length; i++){

                if(currentItems.length == 0){           //needed for adding to empty container or else will not add
                    
                    ps = con.prepareStatement("INSERT INTO '" + container + "' VALUES (?,?,?,?,?)");
                    ps.setInt(1, numOfItems[i]);
                    ps.setString(2, item[i]);
                    ps.setString(3, description[i]);
                    ps.setString(4, condition[i]);
                    ps.setString(5, photoFile[i]);

                    ps.executeUpdate();         //adds new item
                    
                }else{
                    
                    for(int j = 0 ; j < currentItems.length; j++){              // searches for match in inventory

                        if(currentItems[j][1].toString().matches(item[i]) &&
                            currentItems[j][2].toString().matches(description[i]) &&
                                currentItems[j][3].toString().matches(condition[i]) &&
                                    currentItems[j][4].toString().matches(photoFile[i])){ 

                            int currentNumberOfItems = Integer.valueOf(String.valueOf(currentItems[j][0]));         //current number of items stored
                            int udatedItems = numOfItems[i] + currentNumberOfItems;                                 //number of items to add to existing item

                            ps = con.prepareStatement("UPDATE '" + container + "' SET NumberOfItems = ? WHERE NumberOfItems = ? AND Item = ? AND Description = ? AND condition = ? AND PhotoFile = ?");
                            ps.setInt(1, udatedItems);
                            ps.setInt(2, currentNumberOfItems);
                            ps.setString(3, item[i]);
                            ps.setString(4, description[i]);
                            ps.setString(5, condition[i]);
                            ps.setString(6, photoFile[i]);

                            ps.executeUpdate();         //udates item count

                        }else{

                                ps = con.prepareStatement("INSERT INTO '" + container + "' VALUES (?,?,?,?,?)");
                                ps.setInt(1, numOfItems[i]);
                                ps.setString(2, item[i]);
                                ps.setString(3, description[i]);
                                ps.setString(4, condition[i]);
                                ps.setString(5, photoFile[i]);

                                ps.executeUpdate();         //adds new item

                        }   
                    }
                }
            }
            for(int i = 0; i < numOfItems.length; i++){         //logs all action taken
                
                action = "Inventory Added";     
                details = "Container: " + container + " - " + " Items Added: '" + numOfItems[i] + "' '" + item[i] + "' '" + description[i] + "' " + condition[i];

                log.logAction(ContainerMain.user, cs.getCustomerNameById(id), id, action, details, ContainerMain.timeStamp());       //adds to database log

                log.logCustomerAction(id, action + " " + details);   ///adds to customer action log file
                
            }
            
           JOptionPane.showMessageDialog(null, "Container: '" + container + "' Inventory Has Been Updated");
            
        }catch(SQLException ex){
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
      
    /**
    * retrieves the inventory of the chosen container
    * @param container
    * @return Object[][] inventories
    */
    
    public Object[][] getInventory(int container){
        
        int rowCount = getinventoryRowCount(container);
        Object[][] inventory = new Object[rowCount][5];
        
        int newRow = 0;
             
        try(Connection con = db.inventories()){
            
            ps = con.prepareStatement("SELECT * FROM '" + container + "'");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                int numOfItems = rs.getInt("numberOfItems");
                String item = rs.getString("Item");
                String description = rs.getString("Description");
                String condition = rs.getString("Condition");
                String photoFile = rs.getString("PhotoFile");
                                
                inventory[newRow][0] = numOfItems;
                inventory[newRow][1] = item;
                inventory[newRow][2] = description;
                inventory[newRow][3] = condition;
                inventory[newRow][4] = photoFile;
                
                newRow++;
            }
      
        }catch(SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return inventory;
    }
    
    /**
    * gets the number of rows from the chosen container
    * @param container
    * @return 
    */
    
    public int getinventoryRowCount(int container){
        
        int rowCount = 0;
        
        try(Connection con = db.inventories()){
            
            ps = con.prepareStatement("SELECT COUNT(*) AS count FROM '" + container + "'");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                rowCount = rs.getInt("count");
                
            }           
        }catch(SQLException ex){
            
           Logger.getLogger(Inventories.class.getName()).log(Level.SEVERE, null, ex);
        }    
        
        return rowCount;
    }
    
    /**
     * gets the containers who's inventories are incomplete
     * @return 
     */
    
    public ArrayList<String> getUnInventoried(){
            
        ArrayList<String> unInventorized = new ArrayList<>();

        try(Connection con = db.warehouse()){

            ps = con.prepareStatement("SELECT container FROM warehouse WHERE status = ?");               
            ps.setString(1, "ENTERED");

            rs = ps.executeQuery();

            while(rs.next()){

                unInventorized.add(rs.getString("container"));
            }

        } catch (SQLException ex) {

        Logger.getLogger(Inventories.class.getName()).log(Level.SEVERE, null, ex);
    }

        return unInventorized;
    }
       
    /**
    * removes items from the inventory or sends them to be updated
     * @param id
    * @param container
    * @param numOfItems
    * @param item
    * @param description
    * @param condition 
     * @param photoFile 
    */
    
    public void removeItems(int id, int container, int numOfItems, String item, String description, String condition, String photoFile){

        Object[][] inventory = getInventory(container);
        int currentItems = 0;
        
        try(Connection con = db.inventories()){
                
                ps = con.prepareStatement("SELECT NumberOfItems FROM '" + container + "' WHERE Item = ? AND Description = ? AND condition = ? AND PhotoFile = ?");
                ps.setString(1, item);
                ps.setString(2, description);
                ps.setString(3, condition);
                ps.setString(4, photoFile);
                
                rs = ps.executeQuery();
                
                while(rs.next()){
                    
                    currentItems = rs.getInt("NumberOfItems");
       
                    if(currentItems == numOfItems){                   //checks to see if number removed is same as stored and then removes all

                        ps = con.prepareStatement("DELETE FROM '" + container + "' WHERE NumberOfItems = ? AND Item = ? AND Description = ? AND condition = ? AND PhotoFile = ?");
                        ps.setInt(1, numOfItems);
                        ps.setString(2, item);
                        ps.setString(3, description);
                        ps.setString(4, condition);
                        ps.setString(5, photoFile);
                        
                        ps.executeUpdate();

                        action = "Item removed";     
                        details = "Number Of Items: '" + numOfItems + " Item: '" + item + " Description: '" + description;

                        log.logAction(ContainerMain.user, getCustomerNameByID(id), id, action, details, ContainerMain.timeStamp());       //adds to database log

                        log.logCustomerAction(id, action + " " + details);   ///adds to customer action log file


                    }else{                              //if number is not the same the item number is updated
                          
                        String addRemove = "";
                        int difference = 0;

                        int updated = currentItems - numOfItems;
                        addRemove = "Removed";
        
                        ps = con.prepareStatement("UPDATE '" + container + "' SET NumberOfItems = ? WHERE NumberOfItems = ? AND Item = ? AND Description = ? AND condition = ? AND PhotoFile = ?");
                        ps.setInt(1, updated);
                        ps.setInt(2, currentItems);
                        ps.setString(3, item);
                        ps.setString(4, description);
                        ps.setString(5, condition);
                        ps.setString(6, photoFile);


                        ps.executeUpdate();

                        action = "Item Updated";     
                        details = "Item: '" + item + " Description: '" + description + " '" + numOfItems + " - " + addRemove;

                        log.logAction(ContainerMain.user, getCustomerNameByID(id), id, action, details, ContainerMain.timeStamp());       //adds to database log

                        log.logCustomerAction(id, action + " " + details);   ///adds to customer action log file

                        JOptionPane.showMessageDialog(null, "Item: '" + item + "' Has Been Updated");
                    }
                }
            
        }catch(SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        int rowCount = getinventoryRowCount(container);         //checks for empty Container
        
        if(rowCount == 0){
            
            int choice = JOptionPane.showConfirmDialog(null, "Container: '" + container + " Is Now Empty, Would You Like To Remove It From The Customer?", "Empty Container", JOptionPane.YES_NO_OPTION);
            
            if(choice == JOptionPane.YES_OPTION){
 
                try(Connection con = db.warehouse()){

                    ps = con.prepareStatement("SELECT container From warehouse WHERE container = ?");
                    ps.setInt(1, container);

                    rs = ps.executeQuery();

                    if(rs.next()){                  //checks if container is in warehouse or floating table

                        ps = con.prepareStatement("UPDATE warehouse SET id = ?, container = ?, status = ? WHERE container = ?");    //removes from warehouse
                        ps.setInt(1, 0);
                        ps.setInt(2, 0);
                        ps.setString(3, "FREE");
                        ps.setInt(4, container);

                        ps.executeUpdate();

                        ps = con.prepareStatement("INSERT INTO empties VALUES(?)");           //adds to empties table
                        ps.setInt(1,container);

                        ps.executeUpdate();

                        action = "Container Emptied";                                                          //logs action

                        details = "Container: " + container + " Emptied";

                        log.logAction(ContainerMain.user, getCustomerNameByID(id), id, action, details, ContainerMain.timeStamp());

                        log.updateContainerHistory(id, container, action);

                        log.logCustomerAction(id, action + " - " + details);
                        
                        updateCustomerContainerCountRemove(id);         //updates the container count

                    }else{

                        ps = con.prepareStatement("DELETE FROM floating WHERE Container = ?");           //removes from floating table
                        ps.setInt(1,container);

                        ps.executeUpdate();

                        ps = con.prepareStatement("INSERT INTO empties VALUES(?)");           //adds to empties table
                        ps.setInt(1,container);

                        ps.executeUpdate();

                        action = "Container Emptied";                                                          //logs action

                        details = "Container: " + container + " Emptied";

                        log.logAction(ContainerMain.user, getCustomerNameByID(id), id, action, details, ContainerMain.timeStamp());

                        log.updateContainerHistory(id, container, action);

                        log.logCustomerAction(id, action + " - " + details);

                    }
                            

                } catch (SQLException ex) {

                    Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                
                setContainerStatus(container, "ENTERED");                   //sets customers container to entered if empty but still staying with customer
            }
        }
    }
    
    /**
     * finds item to search for in the search inventory window
     * @param item
     * @return 
     */
    
    public ArrayList<String> searchInventoryByItem(String item){
        
        ArrayList<String> items = new ArrayList<>();
        ResultSet rs2;
        
        try(Connection con = db.inventories()){

            dbmd = con.getMetaData();
            rs = dbmd.getTables(null, null, "%", null);

            while(rs.next()){

                ps  = con.prepareStatement("SELECT * FROM '" + rs.getString(3) + "' WHERE item LIKE '" + item + "%'");
                rs2 = ps.executeQuery();

                while(rs2.next()){
                    
                    String photoFile = rs2.getString("photoFile").isBlank() ? "None" : rs2.getString("photoFile");
                    
                    String getItem = "" + rs.getString(3) + "-" +  rs2.getInt("NumberOfItems") + "-" +  rs2.getString("item") + "-"
                            +  rs2.getString("description") + "-" +  rs2.getString("condition") + "-" +  photoFile;
                    
                    items.add(getItem);
                }
            }

        } catch (SQLException ex) {
                
            Logger.getLogger(Inventories.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return items;
    }
        
    /**
     * finds item with description to search for in the search inventory window 
     * @param item
     * @param description
     * @return 
     */
    
    public ArrayList<String> searchInventoryByItemAndDescription(String item, String description){
        
        ArrayList<String> items = new ArrayList<>();
        ResultSet rs2;
        
        try(Connection con = db.inventories()){

            dbmd = con.getMetaData();
            rs = dbmd.getTables(null, null, "%", null);

            while(rs.next()){

                ps  = con.prepareStatement("SELECT * FROM '" + rs.getString(3) + "' WHERE item LIKE '" + item + "%' AND description LIKE '" + description + "%'");
                rs2 = ps.executeQuery();

                while(rs2.next()){
                    
                    String photoFile = rs2.getString("photoFile").isBlank() ? "None" : rs2.getString("photoFile");
                    
                    String getItem = "" + rs.getString(3) + "-" +  rs2.getInt("NumberOfItems") + "-" +  rs2.getString("item") + "-" +  rs2.getString("description") + "-"
                            +  rs2.getString("condition") + "-" +  photoFile;
                    items.add(getItem);
                }
            }

        } catch (SQLException ex) {
                
            Logger.getLogger(Inventories.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return items;
    }
    
    /**
     * updates the customers container count
     * @param id 
     */
    
    public void updateCustomerContainerCountRemove(int id){
        
        int conCount = getNumberOfContainers(id);       //checks container count again
        String name = getCustomerNameById(id);
        
        try(Connection con = db.customers()){
                               
            int newCount = conCount - 1;

            ps = con.prepareStatement("UPDATE customers SET containers = ? WHERE id = ?");      //updates customer container count
            ps.setInt(1, newCount);
            ps.setInt(2, id);
            
            ps.executeUpdate();
            
            conCount = getNumberOfContainers(id);       //checks container count again
            
            if(conCount == 0){          //if count = 0 option to remove customer
               
                int choice = JOptionPane.showConfirmDialog(null, name + " Has No More Containers In The Warehouse\n Remove Customer From DataBase?", "Remove Customer",JOptionPane.OK_CANCEL_OPTION);
                
                if(choice == JOptionPane.OK_OPTION){

                    action = "Removed From Databse";                                 //logs action
                    log.logCustomerAction(id, action);
                    
                    details = "Customer: " + name + " Removed";

                    log.logAction(ContainerMain.user, name, id, action, details, ContainerMain.timeStamp());
                    
                    ps = con.prepareStatement("DELETE FROM customers WHERE id = ?");
                    ps.setInt(1, id);
                    
                    ps.executeUpdate();                                             //adds customer folder to dead file
                    
                    Files.move(Paths.get(getCustomerFileId(id)), Paths.get("Customer Dead File/'" + Paths.get(getCustomerFileId(id)).getFileName() + "'"), StandardCopyOption.REPLACE_EXISTING); 
                    
                }
            }
      
        } catch (SQLException | IOException ex) {
                       
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
     /**
    * gets the number of containers in use by the customer
    * @param id
    * @return 
    */  
    
    public int getNumberOfContainers(int id){
        
        int count = 0;
        
        try(Connection con = db.customers()){
            
            ps = con.prepareStatement("SELECT containers FROM customers WHERE Id = ?");
            ps.setInt(1, id);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                count = rs.getInt("containers");
            }
            
        }catch (SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return count;
    }
    
    /**
     * gets customer name by id
     * @param id
     * @return 
     */
    
    public String getCustomerNameById(int id){
         
        Object[] getName = getCustomerDetailsByID(id);
        
        return String.valueOf(getName[1] + " " + getName[2] + " " + getName[3]);
        
    }
    
            
    /**
    * gets all the customer information by id number
    * @param id
    * @return 
    */
    
    public Object[] getCustomerDetailsByID(int id){
        
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
                cusDetails[4] = "0" + rs.getString("Telephone");
                cusDetails[5] = rs.getString("Email");
                cusDetails[6] = rs.getString("Address");   
                cusDetails[7] = rs.getInt("Containers");
                cusDetails[8] = rs.getString("InDate");
                
            }
            
        }catch(SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return cusDetails;
    }
    
           
    /**
    * returns the customer folder path as String 
    * @param id
    * @return 
    */
    
    public String getCustomerFileId(int id){
        
        String cusFileId = "";
        
        try{
            
             Optional<Path> path = Files.walk(Customers.customerFile)                //finds the customer folder
                    .filter(p -> p.toFile().toString().contains(String.valueOf(id)))
                    .findFirst();
             
             if(path.isPresent()){
                 
                 cusFileId = path.get().toAbsolutePath().toString();
             }
             
        }catch(IOException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return cusFileId;        
    }
        
    /**
    * changes the status of the container
    * @param container 
    * @param status 
    */
    
    public void setContainerStatus(int container, String status){
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("UPDATE warehouse SET status = ? WHERE container = ?");
            ps.setString(1, status);
            ps.setInt(2, container);
            
            ps.executeUpdate();
            
        }catch(SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
