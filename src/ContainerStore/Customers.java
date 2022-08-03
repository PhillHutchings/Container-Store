/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContainerStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;


public class Customers {
      
    private final Database db;
    private final Log log;
    private final Warehouse wh;
    
    private PreparedStatement ps;
    private ResultSet rs;
    private DatabaseMetaData dbmd;
    private ResultSetMetaData rsmd;
    
    private String action;
    private String details;
    
    private LocalDateTime ldt;
    public static Path customerFile;        //customer log folder static because accesed globally
        
    public Customers(){
        
        customerFile = Paths.get("CustomerFiles");
        
        db = new Database();
        log = new Log();
        wh = new Warehouse();
        
        createCustomerTable();
        createCustomerFile();
        createCustomerDeadFile();
    }
    
    /**
    * creates the folder in which customer actions will be logged
    */ 
    
    private void createCustomerFile(){
       
       if(!customerFile.toFile().exists()){
        
           try {
               
               Files.createDirectory(customerFile);
               
           } catch (IOException ex) {
               
               Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
           }
        }
    }
    
    /**
    * creates folder to store information from removed customers
    */
    
    private void createCustomerDeadFile(){
        
        Path deadFile = Paths.get("Customer Dead File");
        
        if(!deadFile.toFile().exists()){
            
            try {
                
                Files.createDirectory(deadFile);
                
            } catch (IOException ex) {
                
                Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
           
    /**
    * Creates the Customer Table in Customer.db
    */
    
    private void createCustomerTable(){
        
        try(Connection con = db.customers()){
          
            ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS Customers" + 
                    "(Id INTEGER not NULL," +
                    "Title STRING (5)," +
                    "FirstName STRING (50)," + 
                    "LastName STRING (50)," + 
                    "Telephone STRING (25)," +
                    "Email STRING (50)," + 
                    "Address STRING (300)," +
                    "Containers INTEGER," +
                    "InDate STRING (10)," +
                    "PRIMARY KEY (Id))");

            ps.executeUpdate();
            
        }catch(SQLException ex) {
            
           Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }          
    }
    
  
    /**
    * adds a new customer to the customer table in Customer.db
    * @param id
    * @param title
    * @param firstName
    * @param lastName
    * @param telephone
    * @param eMail
    * @param address
    * @param contNum
    * @param inDate
    */
    
    public void addNewCustomer(int id , String title,  String firstName, String lastName, String telephone, String eMail, String address, int contNum, String inDate){
               
        try(Connection con = db.customers()){
            
            ps = con.prepareStatement("INSERT INTO Customers VALUES (?,?,?,?,?,?,?,?,?)");
            ps.setInt(1, id);
            ps.setString(2, title);
            ps.setString(3, firstName);
            ps.setString(4, lastName);
            ps.setString(5, "0" + telephone);
            ps.setString(6, eMail);
            ps.setString(7, address);
            ps.setInt(8, contNum);
            ps.setString(9, inDate);
            
            ps.executeUpdate();
            
            String fullName = title + " " + firstName + " " + lastName;
            
            JOptionPane.showMessageDialog(null, "'" + fullName + "' Added To DataBase");
            
            action = "Add New Customer";                            //logs action
            details = fullName + " Added To Database";
            
            log.logAction(ContainerMain.user, fullName, id, action, details, inDate);
            
            String fileName = "" + id + "-" + fullName + "-" + inDate;           //creates file name
            
            addNewCustomerFiling(fileName);     //adds file to system
            log.logCustomerAction(id, "Customer Added " + ContainerMain.dateStamp());
            
        }catch(SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }
    
    /**
    * creates individual customer folder and action log text file
    * @param customer 
    */   
    
    public void addNewCustomerFiling(String customer){
    
        Path cus = Paths.get(customerFile.toString(), customer);        //customer id - name - inDate
        
        if(!cus.toFile().exists()){
            
            try {
                
                Files.createDirectory(cus);
                
                Path actionLog = Paths.get(cus.toString(), "actionLog.txt");
                
                Files.createFile(actionLog);
                
            }catch(IOException ex) {
                
                Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
            }
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
     * changes the name of the folder to the new name entered in the edit customer details window
     * @param id
     * @param newName
     * @param oldName 
     */
    
    public void changeOfNameFolderReName(int id, String[] newName, String oldName){
        
        String cusFileId = getCustomerFileId(id);
        
        String fullName = newName[0] + " " + newName[1] + " " + newName[2];
        
        try{
            
            Path oldNameFile = Paths.get(cusFileId);
           // String nameFind = cusFileId.substring(cusFileId.indexOf(oldName), cusFileId.indexOf(oldName + oldName.length() + 1 ));
            
            File newNameFile = new File(cusFileId.replace(oldName, fullName));
            
            Files.move(oldNameFile, newNameFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
        } catch (IOException ex) {
            
            Logger.getLogger(Customers.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
    * gets customer file as path
    * @param id
    * @return 
    */
    
    public Path getCustomerFileIdPath(int id){
        
        Path cusPath = Paths.get(getCustomerFileId(id));
        
        return cusPath;
    }
    
    /**
    * returns the customer action log path as String 
    * @param id
    * @return 
    */
    
    public static String getCustomerActionLog(int id){
        
        String cusActionLog = "";
        
        try{
            
             Optional<Path> path = Files.walk(Customers.customerFile)                //finds the customer folder
                    .filter(p -> p.toFile().toString().contains(String.valueOf(id)))
                    .findFirst();
             
             if(path.isPresent()){
            
                 Path getAction = path.get().toAbsolutePath();

                 cusActionLog = Paths.get(getAction.toString(), "actionLog.txt").toString();        //gets absolute path of the action log
             }
             
        }catch(IOException ex) {
            
           Logger.getLogger(Warehouse.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return cusActionLog;       
    }
    
    /**
    * creates an id for the customer
    * @return unique ID
    */
    
    public int createID(){
        
        String id = "";
        
        do{
            
            for(int i = 0; i < 5; i++){
                
                int digit = (int) (Math.random() * 10);
                
                id += String.valueOf(digit);
            }
            
        }while(checkId(Integer.valueOf(id)) == false);
        
        return Integer.valueOf(id);
    }
    
    /**
    * checks if the Id is unique
    * @param Id
    * @return true or false
    */
    
    public boolean checkId(int Id){
        
        boolean unique = true;
        
        try(Connection con = db.customers()){
            
            ps = con.prepareStatement("SELECT Id FROM Customers WHERE Id  = ?");
            ps.setInt(1, Id);
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                unique = false;
                break;
            }  
            
        }catch(SQLException ex){
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return unique;
    }
    
    /**
    * finds customers by prefix letters (used for the edit customer window find button 
    * ContainerMain line: 1269
    * @param prefix
    * @return 
    */
    
    public ArrayList<String> findCustomers(String prefix){
        
        ArrayList<String> foundNames = new ArrayList<>();
        
        try(Connection con = db.customers()){
            
            ps = con.prepareStatement("SELECT LastName, FirstName FROM customers WHERE LastName LIKE '" + prefix + "%'");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
               foundNames.add(rs.getString("LastName") + ", " + rs.getString("FirstName"));
            }
            
        }catch(SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return foundNames;
    }
    
    /**
    * gets all the customer information by first and last name
    * @param firstName
    * @param lastName
    * @return 
    */
    
    public Object[] getCustomerDetailsByName(String firstName, String lastName){
        
        Object[] cusDetails = new Object[9];
        
        try(Connection con = db.customers()){
            
            ps = con.prepareStatement("SELECT * FROM customers WHERE FirstName = ? AND LastName = ?");
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            
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
     * gets customer name by id
     * @param id
     * @return 
     */
    
    public String getCustomerNameById(int id){
         
        Object[] getName = getCustomerDetailsByID(id);
        
        return String.valueOf(getName[1] + " " + getName[2] + " " + getName[3]);
        
    }
    
    /**
     * gets customer name by last name, first name 
     * @param id
     * @return 
     */
    
    public String getCustomerNameByIdLastToFirst(int id){
         
        Object[] getName = getCustomerDetailsByID(id);
        
        return String.valueOf(getName[3] + ", " + getName[2]);
        
    }
    
    /**
    * updates the customers information used with the EDCWindow save button
    * containerMain line 1346
    * @param id
    * @param nameChange
    * @param telePhoneChange
    * @param emailChange
    * @param addressChange 
    * @param logChange 
    */
    
    public void editCustomerDetails(int id, String[] nameChange, String telePhoneChange, String emailChange, String addressChange, Object[][] logChange){
     
        try(Connection con = db.customers()){
            
            ps = con.prepareStatement("UPDATE customers SET title = ?, firstName = ?, lastName = ?, telephone = ?, email = ?, address = ? WHERE id = ?");
            ps.setString(1, nameChange[0]);
            ps.setString(2, nameChange[1]);
            ps.setString(3, nameChange[2]);
            ps.setString(4, telePhoneChange);
            ps.setString(5, emailChange);
            ps.setString(6, addressChange);
            ps.setInt(7, id);
            
            ps.executeUpdate();
            
            String fullName = nameChange[0] + " " + nameChange[1] + " " + nameChange[2];
            JOptionPane.showMessageDialog(null, fullName + " Details Have Been Updated");
            
            action = "Edit Customer";                            //logs action
            details = "";
            
            for(int i = 0; i < logChange.length; i++){              //checks array for true if field changed 
                
                boolean changed = Boolean.parseBoolean(logChange[i][0].toString());
                
                if(changed == true){
                    
                    details += String.valueOf(logChange[i][1]) + " - ";      //appends the details of what has changed to the details variable
                }
            }
            
            log.logAction(ContainerMain.user, fullName, id, action, details, ContainerMain.timeStamp());         //logs the changes
            
        }catch(SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
    * checks wether the name and id match in the database (used mainly for Edit Customer)
    * @param id
    * @param lastName
    * @return 
    */
    
    public boolean nameIDCheck(int id, String lastName){
        
        boolean match = false;
        
        try(Connection con = db.customers()){
            
            ps = con.prepareStatement("SELECT * FROM customers WHERE id = ?");
            ps.setInt(1, id);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                if(rs.getString("LastName").matches(lastName)){
                    
                    match = true;
                }
            }
    
        }catch(SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(match == false){
            
             JOptionPane.showMessageDialog(null, "Id and Name DO NOT match Saved Information");
        }
        
        return match;
    }
    
    /**
    * removes the customer, moves customer file to dead files and empties containers
    * @param id
    * @param containers 
    */
    
    public void removeCustomer(int id, int[]containers){
        
        try(Connection con = db.customers()){
            
            for(int c : containers){            //adds container to empties table and removes from the warehouse table
                
                wh.emptySingleContainer(c, true);
            }
             
            ps = con.prepareStatement("DELETE FROM customers WHERE id = ?");
            ps.setInt(1, id);
            
            ps.executeUpdate();
                      
            action = "Removed From DataBase: " + ContainerMain.timeStamp() + " By: " + ContainerMain.user;      //text to be added to file
            log.logCustomerAction(id, action);             //logs in customer action file
       
        //moves customer folder to the customer dead folder
            
            Files.move(getCustomerFileIdPath(id), Paths.get("Customer Dead File/'" + getCustomerFileIdPath(id).getFileName() + "'"), StandardCopyOption.REPLACE_EXISTING); 
  
        }catch(SQLException | IOException ex) {
            
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
        
        try(Connection con = db.warehouse()){
            
            ps = con.prepareStatement("SELECT COUNT(container) AS count FROM warehouse WHERE id = ?");
            ps.setInt(1, id);
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                count = rs.getInt("count");
            }
            
        }catch (SQLException ex) {
            
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return count;
    }
    
    /**
     * updates customer container count
     * @param id 
     * @param ammount 
     */
    
    public void upDateCustomerContainerCount(int id, int ammount){
        
        int cont = getNumberOfContainers(id) + ammount;
        
        try(Connection con = db.customers()){
            
            ps = con.prepareStatement("UPDATE customers SET containers = ?");
            
            ps.setInt(1, cont);
            
            ps.executeUpdate();
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Customers.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
                
    /**
     * gets the longest customer in database
     * @return 
     */
    
    public Object[][] getLongestCustomer(){
        
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
            
            ps = con.prepareStatement("SELECT * FROM customers ORDER BY inDate");
            
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
     * gets the column names from the customers database (View Analytic Window)
     * @return 
     */
    
    public String[] getCustomerDBColumnNames(){
        
        String[] columns = new String[9];
        int place = 0;
        try(Connection con = db.customers()){
            
            ps = con.prepareStatement("SELECT * FROM customers");
            
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
     * gets number of customers in database
     * @return 
     */
    
    public int getNumberOfCustomers(){
                
        int count = 0;
        
        try(Connection con = db.customers()){
            
            ps = con.prepareStatement("SELECT COUNT(*) AS count FROM customers");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                count = rs.getInt("count");
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Customers.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return count;
    }
    
    /**
     * gets all customers in database details
     * @return 
     */
    
    public Object[][] getAllCustomerData(){
        
        Object[][] cusDetails = new Object[getNumberOfCustomers()][7];
        
        int place = 0;
        
        try(Connection con = db.customers()){
            
            ps = con.prepareStatement("SELECT * FROM customers");
            
            rs = ps.executeQuery();
            
            while(rs.next()){
                
                cusDetails[place][0] = rs.getInt("ID");
                cusDetails[place][1] = rs.getString("Title");
                cusDetails[place][2] = rs.getString("FirstName");
                cusDetails[place][3] = rs.getString("LastName");
                cusDetails[place][4] = rs.getInt("Telephone");
                cusDetails[place][5] = rs.getString("Email");
                cusDetails[place][6] = rs.getString("Address");   
                         
                place++;
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Customers.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return cusDetails;
    }
}
