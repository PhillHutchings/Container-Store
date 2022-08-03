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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;


public class Warehouse {
    
    private Database db;
    private Log log;
       
    private PreparedStatement ps;
    private ResultSet rs;
    private DatabaseMetaData dbmd;
    private ResultSetMetaData rsmd;
    
    private String action;
    private String details;
    
    public Warehouse(){
        
        db = new Database();
        log = new Log();

        
        createWarehouseTable();
        createEmptiesTable();
        createFloatingTable();       
        
    }
    
    /**
    * creates the warehouse table in warehouse.db
    */
    
    private void createWarehouseTable(){
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS Warehouse"
                    + "(Id INTEGER,"
                    + "Container INTEGER (5),"
                    + "Location STRING (5),"
                    + "Status STRING)");

            ps.executeUpdate();
   
        }catch(SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
           
    /**
    * creates the empties  table in warehouse.db
    */
    
    private void createEmptiesTable(){
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS Empties"
                    + "(Container INTEGER (5))");

            ps.executeUpdate();
                  
        }catch(SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
    * creates the Inventory table for the container
    * @param container 
    */
    
    public void createInventory(int container){
        
        try(Connection con = db.inventories()){
            
            ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS '" + container + "'"
                    + "(NumberOfItems INTEGER,"
                    + "Item STRING,"
                    + "Description STRING,"
                    + "Condition STRING,"
                    + "PhotoFile STRING) ");

            ps.executeUpdate();

        }catch(SQLException ex){
            
            Logger.getLogger(Inventories.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }
    
    /**
    * creates the floating table in warehouse.db
    * table where containers with owners are not in a location in the warehouse but still in use
    */
    
    private void createFloatingTable(){
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS Floating"
                    + "(ID INTEGER,"
                    + "Container INTEGER,"
                    + "Status STRING)");

            ps.executeUpdate();  

        }catch(SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * adds container to the floating table
     * @param id
     * @param container 
     * @param status 
     */
    
    public void addToFloationgTable(int id, int container, String status){
        
        try(Connection con = db.warehouse()){
                       
            ps = con.prepareStatement("INSERT INTO floating VALUES(?,?,?)");
            
            ps.setInt(1, id);
            ps.setInt(2, container);
            ps.setString(3, status);
            
            ps.executeUpdate();
            
            ps = con.prepareStatement("UPDATE warehouse SET id = ? , container = ? , status = ? WHERE container = ?");
            
            ps.setInt(1, 0);
            ps.setInt(2, 0);
            ps.setString(3, "FREE");
            ps.setInt(4, container);
            
            ps.executeUpdate();
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    /**
     * creates temporary table to store data from the oldest container button in view analytic window 
     */
    
    public void createOldestContainerTempTable(){
        
        try(Connection con = db.ContainerHistory()){          
            
            ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS Temp"
                    + "(Container INTEGER, "
                    + "Action STRING,"
                    + "TimeStamp STRING)");

            ps.executeUpdate();
  
        }catch(SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
     /**
     * drops temporary table to store data from the oldest container button in view analytic window 
     */
    
    public void removeTempTable(){
        
        try(Connection con = db.ContainerHistory()){  
            
            ps = con.prepareStatement("DROP TABLE Temp");
            ps.executeUpdate();
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    /**
    * creates the container history table in ContainerHistory.db
    * @param container
    */
    
    public void createContainerHistoryTable(int container){
        
        String cont = String.valueOf(container);
        String timeStamp = ContainerMain.timeStamp();
        
        try(Connection con = db.ContainerHistory()){          
            
            ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS '" + cont + "'"
                    + "(Owner STRING, "
                    + "Action STRING,"
                    + "TimeStamp STRING)");

            ps.executeUpdate();

            ps = con.prepareStatement("INSERT INTO '" + cont + "' VALUES(?,?,?)");
            ps.setString(1, "Company");
            ps.setString(2, "Created");
            ps.setString(3, timeStamp);

            ps.executeUpdate();
  
        }catch(SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    * adds multiple containers into the warehouse table in warehouse.db
    * @param id
    * @param containers
    * @param location 
    * @param status 
     * @param addition   
    */    
    
    public void assignContainers(int id, int[] containers, String[] location, String status, boolean addition){
               
        try(Connection con = db.warehouse()){
            
            for(int i = 0; i < containers.length; i++){
                
                ps = con.prepareStatement("UPDATE warehouse SET id = ?, container = ?, status = ? WHERE location = ?");
                
                ps.setInt(1, id);
                ps.setInt(2, containers[i]);
                ps.setString(3, status);
                ps.setString(4, location[i]);
                
                ps.executeUpdate();
            
                action = "Assigned Container";
                
                log.updateContainerHistory(id, containers[i], action);      //updates the container history database
  
                removeFromEmpties(containers[i]);       //removes the container form the empty list
                
                addContainerFile(id ,containers[i]);     //adds Container folder to customer folder
            }
                     
        }catch(SQLException ex){
            
           Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(addition){           //if adding container to an existing customer container count is updated
            
            try(Connection con = db.customers()){

     
                int conCount = getNumberOfContainers(id);       //get container count                              
                int newCount = conCount + containers.length;

                ps = con.prepareStatement("UPDATE customers SET containers = ? WHERE id = ?");      //updates customer container count
                ps.setInt(1, newCount);
                ps.setInt(2, id);

                ps.executeUpdate();
          
            } catch (SQLException ex) {

                Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
            }   
        }
    }
    
    /**
     * adds brand new container straight to customer (add new container window)
     * @param id
     * @param container
     * @param location
     * @param status 
     */
    
    public void assignNewContainer(int id, int container, String location, String status){
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("UPDATE warehouse SET id = ?, container = ?, status = ? WHERE location = ?");
            
            ps.setInt(1, id);
            ps.setInt(2, container);
            ps.setString(3, status);
            ps.setString(4, location);

            ps.executeUpdate();
            
            createInventory(container);              //creates inventory table
            
            createContainerHistoryTable(container);             //creates container history table
              
            action = "Assigned Container";
                
            log.updateContainerHistory(id, container, action);      //updates the container history database
            
            addContainerFile(id ,container);            //adds Container folder to customer folder
              
            String name = getCustomerNameByID(id);
            
            action = "Created New Container";
            
            details = "New Container: '" + container + "' Added To; '" + name + "' Id: '" + id + "'";
            
            String timeS = ContainerMain.timeStamp();
            
            log.logAction(ContainerMain.user, name, id, action, details, timeS);
            
            JOptionPane.showMessageDialog(null, "Container : " + container + " Has Been Added To " + name);
                           
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }      
        
        try(Connection con = db.customers()){
            
            int conCount = getNumberOfContainers(id);       //get container count                              
            int newCount = conCount++;

            ps = con.prepareStatement("UPDATE customers SET containers = ? WHERE id = ?");      //updates customer container count
            ps.setInt(1, newCount);
            ps.setInt(2, id);
            
            ps.executeUpdate();
                
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
    
    /**
     * adds brand new container straight to the empties table
     * @param container 
     */
    
    public void addNewContainerToEmpties(int container){
        
        try(Connection con  = db.warehouse()){
            
            ps = con.prepareStatement("INSERT INTO empties VALUES(?)");
            
            ps.setInt(1, container);
            
            ps.executeUpdate();
            
            createInventory(container);              //creates inventory table
            
            createContainerHistoryTable(container);             //creates container history table
                                   
            action = "Created New Container";
            
            details = "New Container: '" + container + "' Added To; Empties";
            
            String timeS = ContainerMain.timeStamp();
            
            log.logAction(ContainerMain.user, "Empties", 0, action, details, timeS);
                   
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * removes the container form the empty list
     * @param container 
     */
    
    public void removeFromEmpties(int container){
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("DELETE FROM Empties WHERE Container = ?");
            ps.setInt(1, container);
            
            ps.executeUpdate();
            
        } catch (SQLException ex) {
            
             Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    
    /**
     * gets container status
     * @param container
     * @return 
     */
    
    public String getStatus(int container){
        
        String status = "";
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT status FROM warehouse WHERE container  = ?");
            ps.setInt(1, container);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                status = rs.getString("status");
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return status;
    }
    
    /**
    * adds container folders to customer folder and adds text file to log inventory
    * @param id
    * @param container 
    */
    
    public void addContainerFile(int id, int container){
               
        
        try {
            
            ArrayList<Path> paths = Files.walk(Customers.customerFile)
                    .filter(p -> p.toFile().toString().contains(String.valueOf(id))).collect(Collectors.toCollection(ArrayList::new));
                            
            Path enter = Paths.get(paths.get(0).toString(),String.valueOf(container));

            Files.createDirectory(enter);

            Path enterTextFile = Paths.get(enter.toString(), "Inventory.txt");
           
            if(!enterTextFile.toFile().exists()){           //check if same container readded to same customer
                
                Files.createFile(enterTextFile);
            }

        }catch(IOException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }      
    }
     
    /**
    * retrieves the container inventory file
    * @param id
    * @param container
    * @return 
    */
    
    public static String getContainerInventoryFile(int id, int container){
        
        String containerInventoryFile = "";
        
        try{
            
            Optional<Path> path = Files.walk(Customers.customerFile)                //finds the customer folder
                    .filter(p -> p.toFile().toString().contains(String.valueOf(id)))
                    .findFirst();
                                      
            if(path.isPresent()){
                
                Path pathCon = path.get().toAbsolutePath();
               
               containerInventoryFile = Paths.get(pathCon.toString(),String.valueOf(container),"Inventory.txt").toString();      //finds the container inventory file
                
            }else{
                
                throw new IOException("Customer Not Found");
            }
            
        }catch(IOException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return containerInventoryFile;
    }
       
    /**
    * finds the container location
    * @param container
    * @return 
    */
    
    public String findLocation(int container){
        
        String location = "Not Found";         // default if container not there
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT location FROM warehouse WHERE container =? ");
            ps.setInt(1, container);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                location = rs.getString("Location");
            }
              
        }catch(SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return location;
    }
    
    /**
     * gets all free locations with prefix
     * @param preFix
     * @return 
     */
    
    public ArrayList<String> getLocationsWithPrefix(String preFix){
        
        ArrayList<String> locations = new ArrayList<>();
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT location FROM warehouse WHERE location LIKE  '" + preFix + "%' AND status = ?");
            ps.setString(1, "FREE");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                locations.add(rs.getString("location"));
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
              
        Collections.reverse(locations);             //so next available space is shown first
        
        return locations;
    }
    
    /**
     * gets container occupying location
     * @param location
     * @return 
     */
    
    public int getContainerByLocation(String location){
        
        int container = 0;
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT container FROMW warehouse WHERE location = ?");            
            ps.setString(1, location);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                container = rs.getInt("container");
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return container;
    }
        
    /**
    * checks if the container number is not already in use
    * @param container
    * @return 
    */
    
    public boolean checkContainer(int container){
        
        boolean available = true;
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT container FROM warehouse WHERE container = ?");
            ps.setInt(1, container);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
   
                available = false;               
            }
            
        }catch(SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return available;
    }
    
    /**
    * checks empties if the container number is not already taken
    * @param container
    * @return 
    */
    
    public boolean checkEmptiesContainer(int container){
        
        boolean available = true;
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT container FROM empties WHERE container = ?");
            ps.setInt(1, container);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
   
                available = false;               
            }
            
        }catch(SQLException ex) {
            
           Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return available;
    }
    
    /**
    * checks if the location is not already in use
    * @param location
    * @return 
    */
    
    public boolean checkLocation(String location){
        
        boolean available = true;
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT status FROM Warehouse WHERE Location = ?");
            ps.setString(1, location);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                if(!rs.getString("status").matches("FREE")){
                    
                    available = false;
                }
                  
            }
            
        }catch(SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return available;
    }

    
    /**
     * adds existing containers to empties
     * @param container 
     */
    
    private void addContainerToEmpty(int container){
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("INSERT INTO empties VALUES(?)");
            ps.setInt(1, container);
            
            ps.executeUpdate();
                  
        } catch (SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
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
    * get the customers containers and locations
    * @param id
    * @return 
    */  
    
    public Object[][] getCustomerContainersAndLocations(int id){
        
        int newCon = 0;
        int count = getNumberOfContainers(id);
        Object[][] conLocs = new Object[count][2];
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT * FROM warehouse WHERE id = ?");
            ps.setInt(1, id);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                conLocs[newCon][0] = rs.getInt("container");
                conLocs[newCon][1] = rs.getString("Location");
                
                newCon++;
            }
            
        }catch(SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return conLocs;
    }
    
    /**
    * gets the customers containers
    * @param id
    * @return 
    */  
    
    public int[] getCustomerContainers(int id){
        
        int newCon = 0;
        int count = getNumberOfContainers(id);
        int[] cons = new int[count];
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT container FROM warehouse WHERE id = ?");
            ps.setInt(1, id);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                cons[newCon] = rs.getInt("container");
                             
                newCon++;
            }
            
        }catch(SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return cons;
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
     * gets customer id by the container number
     * @param container
     * @return 
     */
    
    public int getCustomerIdByContainer(int container){
        
        int getID = 0;
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT Id FROM warehouse WHERE container = ?");
            ps.setInt(1, container);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                getID = rs.getInt("Id");
                
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return getID;
    }
    
       /**
    * gets all the customer information by container
     * @param container
    * @return 
    */
    
    public String getCustomerNameAndIdByContainer(int container){
        
        Object[] cusDetails = new Object[9];
        int id = 0;
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT id FROM warehouse WHERE container = ?");
            ps.setInt(1, container);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                id = rs.getInt("id");
                
            }            
            
        }catch(SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try(Connection con = db.customers()){
            
            ps = con.prepareStatement("SELECT * FROM customers WHERE id = ?");
            ps.setInt(1, id);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                cusDetails[0] = rs.getInt("id");
                cusDetails[1] = rs.getString("title");
                cusDetails[2] = rs.getString("FirstName");
                cusDetails[3] = rs.getString("LastName");
                
            }   
            
        }catch(SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return String.valueOf("Id : " + cusDetails[0] + " Name: " + cusDetails[1] + " " + cusDetails[2] + " " + cusDetails[3]);
    }
    
        
    /**
     * gets customer id by the container number
     * @param container
     * @return 
     */
    
    public int getCustomerIdByContainerInFloating(int container){
        
        int getID = 0;
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT Id FROM floating WHERE container = ?");
            ps.setInt(1, container);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                getID = rs.getInt("Id");
                
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return getID;
    }
    /**
     * returns letter of aisle (used for combo box in move container window)
     * @return 
     */
    
    public LinkedHashSet<String> getAisleLetters(){
        
        LinkedHashSet<String> aisles = new LinkedHashSet<>();
        
        aisles.add("Aisle");        //first item as item listener will not show first
        
        try(Connection con = db.warehouse()){
        
            ps = con.prepareStatement("SELECT location FROM warehouse");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                
                aisles.add(rs.getString("location").substring(0, 1));
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
                  
        return aisles;
    }
    
     /**
     * returns all locations in aisle
     * @param aisle
     * @return 
     */
    
    public ArrayList<String> getLocations(String aisle){
        
        ArrayList<String> aisles = new ArrayList<>();
        
        aisles.add("Aisle");        //first item as item listener will not show first
        
        try(Connection con = db.warehouse()){
        
            ps = con.prepareStatement("SELECT location FROM warehouse WHERE location LIKE '" + aisle + "%'");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                
                aisles.add(rs.getString("location"));
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
                  
        return aisles;
    }
      
    /**
     * gets the rowHeight of the row by aisle letter
     * @param letter
     * @return 
     */
    
    public int getRowHeightByAisleLetter(String letter){
        
        LinkedHashSet<String> heightMarker = new LinkedHashSet();
        int height = 0;
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT * FROM warehouse WHERE location LIKE '" + letter + "%'");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                heightMarker.add(rs.getString("location").substring(rs.getString("location").indexOf("(") + 1, rs.getString("location").indexOf(")")));
            }
            
        } catch (SQLException ex) {
            
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        height = heightMarker.size();
        
        return height;
    }
    
    /**
    * gets the number of container spaces per aisle (for the Move Container Window)
    * @param letter
    * @return 
    */
    
    public int getNumberOfContainersInAisleByLetter(String letter){
        
        int number = 0;
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT COUNT(*) AS count FROM warehouse WHERE location LIKE '" + letter + "%'");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                number = rs.getInt("count");
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return number;
    }
    
    /**
    * gets the details of the location (for the Move Container Window)
    * @param letter
    * @return 
    */
    
    public Object[][] getLocationDetailsByAisleLetter(String letter){
        
        int number = getNumberOfContainersInAisleByLetter(letter);
        
        Object[][] aisleDetails = new Object[number][4];
        
        int space = 0;
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT * FROM warehouse WHERE location LIKE '" + letter + "%'");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                aisleDetails[space][0] = rs.getInt("id");
                aisleDetails[space][1] = rs.getInt("container");
                aisleDetails[space][2] = rs.getString("location");
                aisleDetails[space][3] = rs.getString("status");
                
                space++;
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return aisleDetails;
    }
    
    /**
     * gets number of containers in given aisle
     * @param letter
     * @return 
     */
    
    public int numberOfContainersInAisle(String letter){
        
        int count = 0;
        
         try(Connection con = db.warehouse()){
             
             ps = con.prepareStatement("SELECT COUNT(id) AS count FROM warehouse WHERE id > 0 AND location LIKE '" + letter + "%'");
            
             rs = ps.executeQuery();
             
             while(rs.next()){
                 
                 count = rs.getInt("count");
             }
             
         } catch (SQLException ex) {
             
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
         return count;
    }
    
    /**
     * sorts aisle by moving all containers to the back so all free spaces are at the front
     * @param aisle 
     */
    
    public void sortAisle(String aisle){
             
        int row = 0;
        
        int number = numberOfContainersInAisle(aisle);
        
        Object[][] allContainers = new Object[number][3];
        
        ArrayList<String> locations = getLocations(aisle);
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT * FROM warehouse WHERE location LIKE '" + aisle + "%'");      //retrieves all details of entries by aisle
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                if(rs.getInt("id") > 0){                    //adds detaisl to array
                    
                    allContainers[row][0] = rs.getInt("id");
                    allContainers[row][1] = rs.getInt("container");
                    allContainers[row][2] = rs.getString("status");
                    
                    row++;
                    
                }                
            }
            
            for(int i = 0; i < locations.size(); i++){
            
                ps = con.prepareStatement("UPDATE warehouse SET id = ?, container = ?, status = ? WHERE location = ?");             //resets all the entries in aisle
                
                ps.setInt(1, 0);
                ps.setInt(2, 0);
                ps.setString(3, "FREE");
                ps.setString(4, locations.get(i));
                
                ps.executeUpdate();
                
            }
            
            row--;        
                    
            //re-enters all containers from array into db from back to front simulating taken a container out of row and replacing the containers back in, in reverse order taken
            
            for(int i = locations.size() -1; i >= locations.size() - allContainers.length ; i--){
                
                ps = con.prepareStatement("UPDATE warehouse SET id = ?, container = ?, status = ? WHERE location = ?");     
                
                ps.setInt(1, Integer.valueOf(String.valueOf(allContainers[row][0])));
                ps.setInt(2, Integer.valueOf(String.valueOf(allContainers[row][1])));
                ps.setString(3, String.valueOf(allContainers[row][2]));
                ps.setString(4, locations.get(i));
                 
                ps.executeUpdate();
                
                int container = Integer.valueOf(String.valueOf(allContainers[row][1]));
                String location = locations.get(i);
                String name = getCustomerNameAndIdByContainer(container);
                int id = Integer.valueOf(String.valueOf(allContainers[row][0]));
                
                action = "Container Moved";
                details = "Container: '" + container + "' Moved To: '" + location + "'";
                
                log.logAction(ContainerMain.user, name, id, action, details, ContainerMain.timeStamp());
                log.updateContainerHistory(id, container, action);
                
                row--;
                
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * gets number of containers in floating table
     * @return 
     */
    
    public int getFloatCount(){
        
        int count = 0;
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT COUNT(*) AS count FROM floating");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                count = rs.getInt("count");
                
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return count;
    }
    
    /**
     * gets all containers an ids in the floating table
     * @return 
     */
    
    public Object[][] getFloatingContainers(){
        
        int count = getFloatCount();
        int cont = 0;
        
        Object[][] floats = new Object[count][3];
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT * FROM floating");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                floats[cont][0] = rs.getInt("id");
                floats[cont][1] = rs.getInt("container");
                floats[cont][2] = rs.getString("status");
                        
                cont++;
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return floats;
    }
    
    /**
     * removes floating container form floating table and reinserts it back into warehouse
     * @param id
     * @param container
     * @param status
     * @param location 
     */
    
    public void floatToWarehouse(int id, int container, String status, String location){
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("UPDATE warehouse SET id = ?, container = ?, status = ? WHERE location = ?");
            
            ps.setInt(1, id);
            ps.setInt(2, container);
            ps.setString(3,status);
            ps.setString(4, location);
            
            ps.executeUpdate();
            
            ps = con.prepareStatement("DELETE FROM floating WHERE container = ?");           
            ps.setInt(1, container);
            
            ps.executeUpdate();
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * empties the container (Move Container Window)
     * @param container 
     */
    
    public void floatToEmpty(int container){
        
        int id = getCustomerIdByContainerInFloating(container);           //get customer id
          
        String name = getCustomerNameByID(id);                  //get customer name
        
        try(Connection con = db.inventories()){
            
            ps = con.prepareStatement("DELETE FROM '" + container + "'");     //delete inventory
            
            ps.executeUpdate();
            
            action = "Container Emptied";
        
            log.updateContainerHistory(id, container, action);              //logs action
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("UPDATE warehouse SET id = ?, container = ?, status = ? WHERE container = ?");     //removes from warehouse table
            ps.setInt(1, 0);
            ps.setInt(2, 0);
            ps.setString(3, "FREE");
            ps.setInt(4, container);
            
            ps.executeUpdate();

            ps = con.prepareStatement("DELETE FROM floating WHERE Container = ?");           //removes from floating table            
            ps.setInt(1,container);
            
            ps.executeUpdate();
            
            ps = con.prepareStatement("INSERT INTO empties VALUES(?)");           //adds to empties table       
            ps.setInt(1,container);
            
            ps.executeUpdate();
                                                                
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        int conCount = getNumberOfContainers(id);           //get number of containers 
        
        try(Connection con = db.customers()){
                   
            int newCount = conCount -1;

            ps = con.prepareStatement("UPDATE customers SET containers = ? WHERE id = ?");      //updates customer container count
            ps.setInt(1, newCount);
            ps.setInt(2, id);
            
            ps.executeUpdate();
            
            action = "Container Emptied ";                                                          //logs action
            
            details = "Container: " + container + " Emptied";
            
            log.logAction(ContainerMain.user, name, id, action, details, ContainerMain.timeStamp());
            
            log.updateContainerHistory(id, container, action);
            
            log.logCustomerAction(id, action + " - " + details);
            
            conCount = getNumberOfContainers(id);           //check number of containers again
            
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
    * removes container  (empty container window)
    * @param container 
     * @param removeCustomer 
    */
    
    public void emptySingleContainer(int container, boolean removeCustomer){
        
        int id = 0;
             
        String name = "";                  //get customer name
           
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT container From warehouse WHERE container = ?");
            ps.setInt(1, container);
            
            rs = ps.executeQuery();
            
            if(rs.next()){                  //checks if container is in warehouse of floating table
                
                id = getCustomerIdByContainer(container);
                name = getCustomerNameByID(id);                  //get customer name
                
                ps = con.prepareStatement("UPDATE warehouse SET id = ?, container = ?, status = ? WHERE container = ?");    //removes from warehouse
                ps.setInt(1, 0);
                ps.setInt(2, 0);
                ps.setString(3, "FREE");
                ps.setInt(4, container);

                ps.executeUpdate();

                ps = con.prepareStatement("INSERT INTO empties VALUES(?)");           //adds to empties table
                ps.setInt(1,container);

                ps.executeUpdate();
                
                action = "Container Emptied ";                                                          //logs action
            
                details = "Container: " + container + " Emptied";

                log.logAction(ContainerMain.user, name, id, action, details, ContainerMain.timeStamp());

                log.updateContainerHistory(id, container, action);

                log.logCustomerAction(id, action + " - " + details);

            }else{
                
                id = getCustomerIdByContainerInFloating(container);
                name = getCustomerNameByID(id);                  //get customer name
                
                ps = con.prepareStatement("DELETE FROM floating WHERE Container = ?");           //removes from floating table
                ps.setInt(1,container);
            
                ps.executeUpdate();
                
                ps = con.prepareStatement("INSERT INTO empties VALUES(?)");           //adds to empties table
                ps.setInt(1,container);

                ps.executeUpdate();
                
                action = "Container Emptied ";                                                          //logs action
            
                details = "Container: " + container + " Emptied";

                log.logAction(ContainerMain.user, name, id, action, details, ContainerMain.timeStamp());

                log.updateContainerHistory(id, container, action);

                log.logCustomerAction(id, action + " - " + details);
                
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try(Connection con = db.inventories()){
        
            ps = con.prepareStatement("DELETE FROM '" + container + "'");         //deletes inventory
            ps.executeUpdate();
            
            action = "Container: '" + container + "' Inventory Removed";        //logs action
            
            log.logCustomerAction(id, action);
            
            action = "Inventory Removed";
            details = "Container: " + container + " Removed";

            log.logAction(ContainerMain.user, name, id, action, details, ContainerMain.timeStamp());
            log.updateContainerHistory(id, container, action);
               
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        int conCount = getNumberOfContainers(id);                   //gets  total number of containers from customer
        
        try(Connection con = db.customers()){
                               
            int newCount = conCount - 1;

            ps = con.prepareStatement("UPDATE customers SET containers = ? WHERE id = ?");      //updates customer container count
            ps.setInt(1, newCount);
            ps.setInt(2, id);
            
            ps.executeUpdate();
            
            conCount = getNumberOfContainers(id);       //checks container count again
            
            if(conCount == 0 && removeCustomer == false){          //if count = 0 option to remove customer
               
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
     * gets all containers
     * @return 
     */
    
    public int[] getContainers(){
        
        int count = 0;
        
        try(Connection con = db.ContainerHistory()){
            
            ps = con.prepareStatement("SELECT COUNT (*) AS count FROM 'sqlite_master' WHERE type = 'table'");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                count = rs.getInt("count");
                
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        int[] cons = new int[count];
        int place = 0;
        
        try(Connection con = db.ContainerHistory()){
            
            ps = con.prepareStatement("SELECT name AS name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                if(rs.getInt("name") > 0){           //to catch the temp 
                    
                    cons[place] = rs.getInt("name");
                    place++;
                }
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
     
        return cons;
    }
 
        
    /**
     * gets the column names from the container history database (View Analytic Window)
     * @return 
     */
    
    public String[] getContainerHistroyDBColumnNames(){
        
        int[] cons = getContainers();
        String[] columns = new String[3];
        int place = 0;
        
        try(Connection con = db.ContainerHistory()){
            
            ps = con.prepareStatement("SELECT * FROM '" + cons[0] + "'");           //abitrary container just for column names
            
            rs = ps.executeQuery();
            
            rsmd = rs.getMetaData();
            
            for(int i = 1 ; i <= rsmd.getColumnCount(); i++){

                columns[place] = rsmd.getColumnName(i);
                place++;
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Customers.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return columns;
    }
    
    /**
     * gets the oldest containers in the data base in the view analytic window
     * @return 
     */
    
    public Object[][] getOldestContainer(){
                       
        int[] cons = getContainers();
        Object[][]oldCon = new Object[cons.length][3];
        int place = 0;
        
        createOldestContainerTempTable();           //create temp table
        
        for(int i = 0; i < cons.length; i++){
            
            try(Connection con = db.ContainerHistory()){
                
                int container = cons[i];

                ps = con.prepareStatement("SELECT * FROM '" + container + "' LIMIT 1");       //selects first row where creation date is

                rs = ps.executeQuery();

                while(rs.next()){

                    ps = con.prepareStatement("INSERT INTO Temp VALUES(?,?,?)");            //inserts into temp table
                    ps.setInt(1,cons[i]);
                    ps.setString(2, rs.getString("Action"));
                    ps.setString(3, rs.getString("TimeStamp"));

                    ps.executeUpdate();
                }
            }catch (SQLException ex) {

            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
            
            }
        }
        
            try(Connection con = db.ContainerHistory()){
                
                ps = con.prepareStatement("SELECT * FROM Temp ORDER BY timeStamp");         //selects by date 

                rs = ps.executeQuery();

                while(rs.next() && place < cons.length){

                    oldCon[place][0] = rs.getInt("Container");                       //stores info
                    oldCon[place][1] = rs.getString("Action");
                    oldCon[place][2] = rs.getString("TimeStamp");

                    place++;
                }
            } catch (SQLException ex) {

                Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        removeTempTable();          //drop temp table
        return oldCon;
    }
    
    /**
     * gets the container accolades for the view analytic window
     * @return 
     */
    public Object[][] getContainerAccolades(){
        
        int[] cons = getContainers();
        Object[][] conDetails = new Object[cons.length][3];
        int place = 0;
        
        for(int i = 0; i < cons.length; i++){
            
            try(Connection con = db.ContainerHistory()){

                ps = con.prepareStatement("SELECT COUNT(*) AS count FROM '" + cons[i] + "' WHERE action = ?");
                ps.setString(1, "Assigned Container");
                
                rs = ps.executeQuery();
                
                while(rs.next()){
                    
                    conDetails[place][0] = cons[i];
                    conDetails[place][1] = rs.getInt("count");
                    
                    ps = con.prepareStatement("SELECT COUNT(*) AS count FROM '" + cons[i] + "' WHERE action = ?");
                    ps.setString(1, "Container Moved");
                
                    rs = ps.executeQuery();
                    
                    while(rs.next()){
                        
                        conDetails[place][2] = rs.getInt("count");
                        place++;
                    }       
                }
            } catch (SQLException ex) {

                Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
                
        return conDetails;
    }
    
    /**
     * permanently removes container form system
     * @param container 
     */
    
    public void removeContainer(int container){
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT container FROM empties WHERE container = ?");
            ps.setInt(1, container);
            
            rs = ps.executeQuery();
            
            if(rs.next()){
                
                ps = con.prepareStatement("DELETE FROM empties WHERE container = ?");
                ps.setInt(1, container);
                
                ps.executeUpdate();
                
                JOptionPane.showMessageDialog(null, "Container: '" + container + "' Has Been Removed From System");
                
                action = "Container Removed";                                 //logs action
   
                details = "Container: '" + container + "' Removed";

                log.logAction(ContainerMain.user, "", 0, action, details, ContainerMain.timeStamp());
                
                
            }else{
                
                JOptionPane.showMessageDialog(null, "Container Not Found Please Make Sure It Has Been Added To Empties");
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    /**
     * adds location into warehouse
     * @param location 
     */
    
    public void addLocation(String location){
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("INSERT INTO warehouse VALUES (?,?,?,?)");
            ps.setInt(1, 0);
            ps.setInt(2, 0);
            ps.setString(3, location);
            ps.setString(4, "FREE");
            
            ps.executeUpdate();
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * gets last aisle letter in warehouse
     * @return 
     */
    
    public char getLastAisleLetter(){
        
        char aisle = 'A';
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT location FROM warehouse");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                aisle = rs.getString("location").charAt(0);
                
            }
        } catch (SQLException ex) {
            
            Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return aisle;
    }
}
