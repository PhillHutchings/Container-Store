/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContainerStore;

import java.awt.BorderLayout;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author hutch
 */
public final class ContainerMain extends javax.swing.JFrame {

    private Customers cs;
    private Warehouse wh;
    private Inventories in;
    private Log log;
    private Database db;
    private LogIn li;
    
    static String user  = "";
    private boolean admin = false;
    
    private int ID;
    private String title;
    private String firstName;
    private String lastName;
    private String telephone;
    private String eMail;
    private String address;
    private int container;   
    private String location;
    
    private boolean chosen = false;
    private boolean saved = true;       //edit inventory variables
    private boolean locked = false;
    private boolean movable = false;
    private int container2;
    private ArrayList<Object[]> movedItems;
           
    private int[] containers;
    private String[] locations;
    
    protected static Object[][] floats;
    protected static Object[][] aisleDetails;
    
    private boolean VATableMax = false;         //boolean to stop table growing bigger with each btn click (view analytic window)
    private boolean changePassword;             //check for when new password is being entered
    private boolean printed = false;
    
    private PreparedStatement ps;
    private ResultSet rs;
    private DatabaseMetaData dbmd;
    private MoveContainersPanel mcp;
        
    public ContainerMain() {
                   
        cs = new Customers();
        wh = new Warehouse();
        in = new Inventories();
        log = new Log();
        db = new Database();
        mcp = new MoveContainersPanel();
        li = new LogIn();
        
        initComponents();
        editTables();
        setUpMoveContainerWindow();
        findUnInventoried();
        populateMainTable();
        initiateMainTables();
    }
    
    /**
    * adds effects to tables
    */
    
    public void editTables(){
        
        CNIInventoryTable.getColumnModel().getColumn(4).setCellEditor(new FileChooserCellEditor());
        EIConTable3.getColumnModel().getColumn(4).setCellEditor(new FileChooserCellEditor());
    }
    
    /**
     * populates main table
     */
    
    public void populateMainTable(){
        
        DefaultTableModel tm = (DefaultTableModel)mainCustomersTable.getModel();
       
        tm.setRowCount(0);
        
        Object[][] cusDetails = cs.getAllCustomerData();
        
        for(Object[] c : cusDetails){
            
            tm.addRow(new Object[]{c[0], c[1], c[2], c[3], c[4], c[5], c[6]});
            
        }           
    }
    
    /**
     * adds list listener to main customer table
     */
    
    public void initiateMainTables(){
        
        DefaultTableModel tm = (DefaultTableModel)mainContainersTable.getModel();
            
        mainCustomersTable.setCellSelectionEnabled(true);
        ListSelectionModel cellSelectionModel = mainCustomersTable.getSelectionModel();
        cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
            
            @Override
            public void valueChanged(ListSelectionEvent e) {
                
                int selectedCustomer = 0;

                int selectedRow = mainCustomersTable.getSelectedRow();
                
                if(selectedRow != -1){
                    
                    selectedCustomer = Integer.valueOf(String.valueOf(mainCustomersTable.getValueAt(selectedRow, 0)));

                    int[] cons = wh.getCustomerContainers(selectedCustomer);

                    tm.setRowCount(0);

                    for(int c : cons){

                        String location = wh.findLocation(c);
                        tm.addRow(new Object[]{c, location});

                    }   
                }
            }
        }); 
        
        DefaultTableModel tm2 = (DefaultTableModel)mainInventoryTable.getModel();
        
        mainContainersTable.setCellSelectionEnabled(true);
        ListSelectionModel cellSelectionModel2 = mainContainersTable.getSelectionModel();
        cellSelectionModel2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        cellSelectionModel2.addListSelectionListener(new ListSelectionListener() {
            
            @Override
            public void valueChanged(ListSelectionEvent e) {
                
                int selectedContainer = 0;

                int selectedRow = mainContainersTable.getSelectedRow();
                   
                if(selectedRow != -1){    
                    
                    selectedContainer = Integer.valueOf(String.valueOf(mainContainersTable.getValueAt(selectedRow, 0)));

                    Object[][] inventory = in.getInventory(selectedContainer);

                    tm2.setRowCount(0);

                    for(Object[] i : inventory){

                        tm2.addRow(new Object[]{i[0], i[1], i[2], i[3], i[4]});

                    }   
                }
            }
        });
        
        mainInventoryTable.setCellSelectionEnabled(true);
        ListSelectionModel cellSelectionModel3 = mainInventoryTable.getSelectionModel();
        cellSelectionModel3.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        cellSelectionModel3.addListSelectionListener(new ListSelectionListener() {
            
            @Override
            public void valueChanged(ListSelectionEvent e) {
                
                String photoFile = "";

                int selectedRow = mainInventoryTable.getSelectedRow();
                   
                if(selectedRow != -1){    
                    
                    photoFile = String.valueOf(mainInventoryTable.getValueAt(selectedRow, 4));

                    File pic = new File(photoFile);
                    
                    try {
                        
                        BufferedImage image = ImageIO.read(pic);
                        
                        JFrame picFrame = new JFrame(Paths.get(photoFile).getFileName().toString());
                        JPanel picPanel = new JPanel();
                        JLabel picLabel = new JLabel();
                        
                        picFrame.setSize(image.getWidth(), image.getHeight());
                        picPanel.setSize(image.getWidth(), image.getHeight());
                        
                        picLabel.setIcon(new ImageIcon(image));
                        
                        picPanel.add(picLabel);
                        
                        picFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                        picFrame.setLocationRelativeTo(null);
                        picFrame.add(picPanel);
                        
                        picFrame.setVisible(true);
                        
                    } catch (IOException ex) {
                        
                        Logger.getLogger(ContainerMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        
        SIInventoryTable.setCellSelectionEnabled(true);
        ListSelectionModel cellSelectionModel4 = SIInventoryTable.getSelectionModel();
        cellSelectionModel4.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        cellSelectionModel4.addListSelectionListener(new ListSelectionListener() {
            
            @Override
            public void valueChanged(ListSelectionEvent e) {
                
                String photoFile = "";

                int selectedRow = SIInventoryTable.getSelectedRow();
                int selectedColumn = SIInventoryTable.getSelectedColumn();
            
                if(selectedRow != -1 && selectedColumn == 5){    
                    
                    photoFile = String.valueOf(SIInventoryTable.getValueAt(selectedRow, 5));

                    File pic = new File(photoFile);
                    
                    try {
                        
                        BufferedImage image = ImageIO.read(pic);
                        
                        JFrame picFrame = new JFrame(Paths.get(photoFile).getFileName().toString());
                        JPanel picPanel = new JPanel();
                        JLabel picLabel = new JLabel();
                        
                        picFrame.setSize(image.getWidth(), image.getHeight());
                        picPanel.setSize(image.getWidth(), image.getHeight());
                        
                        picLabel.setIcon(new ImageIcon(image));
                        
                        picPanel.add(picLabel);
                        
                        picFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                        picFrame.setLocationRelativeTo(null);
                        picFrame.add(picPanel);
                        
                        picFrame.setVisible(true);
                        
                    } catch (IOException ex) {
                        
                        Logger.getLogger(ContainerMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        
        EIConTable1.setCellSelectionEnabled(true);
        ListSelectionModel cellSelectionModel5 = EIConTable1.getSelectionModel();
        cellSelectionModel5.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        cellSelectionModel5.addListSelectionListener(new ListSelectionListener() {
            
            @Override
            public void valueChanged(ListSelectionEvent e) {
                
                String photoFile = "";

                int selectedRow = EIConTable1.getSelectedRow();
                int selectedColumn = EIConTable1.getSelectedColumn();
            
                if(selectedRow != -1 && selectedColumn == 4){    
                    
                    photoFile = String.valueOf(EIConTable1.getValueAt(selectedRow, 4));

                    File pic = new File(photoFile);
                    
                    try {
                        
                        BufferedImage image = ImageIO.read(pic);
                        
                        JFrame picFrame = new JFrame(Paths.get(photoFile).getFileName().toString());
                        JPanel picPanel = new JPanel();
                        JLabel picLabel = new JLabel();
                        
                        picFrame.setSize(image.getWidth(), image.getHeight());
                        picPanel.setSize(image.getWidth(), image.getHeight());
                        
                        picLabel.setIcon(new ImageIcon(image));
                        
                        picPanel.add(picLabel);
                        
                        picFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                        picFrame.setLocationRelativeTo(null);
                        picFrame.add(picPanel);
                        
                        picFrame.setVisible(true);
                        
                    } catch (IOException ex) {
                        
                        Logger.getLogger(ContainerMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });        
        
        EIConTable2.setCellSelectionEnabled(true);
        ListSelectionModel cellSelectionModel6 = EIConTable2.getSelectionModel();
        cellSelectionModel6.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        cellSelectionModel6.addListSelectionListener(new ListSelectionListener() {
            
            @Override
            public void valueChanged(ListSelectionEvent e) {
                
                String photoFile = "";

                int selectedRow = EIConTable2.getSelectedRow();
                int selectedColumn = EIConTable2.getSelectedColumn();
            
                if(selectedRow != -1 && selectedColumn == 4){    
                    
                    photoFile = String.valueOf(EIConTable2.getValueAt(selectedRow, 4));

                    File pic = new File(photoFile);
                    
                    try {
                        
                        BufferedImage image = ImageIO.read(pic);
                        
                        JFrame picFrame = new JFrame(Paths.get(photoFile).getFileName().toString());
                        JPanel picPanel = new JPanel();
                        JLabel picLabel = new JLabel();
                        
                        picFrame.setSize(image.getWidth(), image.getHeight());
                        picPanel.setSize(image.getWidth(), image.getHeight());
                        
                        picLabel.setIcon(new ImageIcon(image));
                        
                        picPanel.add(picLabel);
                        
                        picFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                        picFrame.setLocationRelativeTo(null);
                        picFrame.add(picPanel);
                        
                        picFrame.setVisible(true);
                        
                    } catch (IOException ex) {
                        
                        Logger.getLogger(ContainerMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        
        PLInventoryTable.setCellSelectionEnabled(true);
        ListSelectionModel cellSelectionModel7 = PLInventoryTable.getSelectionModel();
        cellSelectionModel7.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        cellSelectionModel7.addListSelectionListener(new ListSelectionListener() {
            
            @Override
            public void valueChanged(ListSelectionEvent e) {
                
                String photoFile = "";

                int selectedRow = PLInventoryTable.getSelectedRow();
                int selectedColumn = PLInventoryTable.getSelectedColumn();
            
                if(selectedRow != -1 && selectedColumn == 4){    
                    
                    photoFile = String.valueOf(PLInventoryTable.getValueAt(selectedRow, 4));

                    File pic = new File(photoFile);
                    
                    try {
                        
                        BufferedImage image = ImageIO.read(pic);
                        
                        JFrame picFrame = new JFrame(Paths.get(photoFile).getFileName().toString());
                        JPanel picPanel = new JPanel();
                        JLabel picLabel = new JLabel();
                        
                        picFrame.setSize(image.getWidth(), image.getHeight());
                        picPanel.setSize(image.getWidth(), image.getHeight());
                        
                        picLabel.setIcon(new ImageIcon(image));
                        
                        picPanel.add(picLabel);
                        
                        picFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                        picFrame.setLocationRelativeTo(null);
                        picFrame.add(picPanel);
                        
                        picFrame.setVisible(true);
                        
                    } catch (IOException ex) {
                        
                        Logger.getLogger(ContainerMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    /**
    * sets up move container window
    */
    
    public void setUpMoveContainerWindow(){

        MCWindow.setLayout(new BorderLayout());
        MCWindow.add(mcp, BorderLayout.CENTER);     //adds the move container Panel class to the move container window
        MCWindow.addMouseListener(mcp);             //adds the mouse listener
        MCWindow.addMouseMotionListener(mcp);
      
    }
    
    /**
     * gets local date time
     * @return 
     */
    
    public static String timeStamp(){
        
        LocalDateTime ldt = LocalDateTime.now();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss");
        String timeStamped = ldt.format(formatter);
        
        return timeStamped;
    }
    
    /**
    * gets local date
    * @return 
    */
    
    public static String dateStamp(){
        
        LocalDateTime ldt = LocalDateTime.now();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd");
        String dateStamped = ldt.format(formatter);
        
        return dateStamped;
    }
    
    /**
     * indicates how many container have no inventory
     */
    
    public void findUnInventoried(){
        
        int count = in.getUnInventoried().size();
        
        createNewInventoryBTN.setText("Create New Inventory" + " (" + count + ")");
        
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ANCWindow = new javax.swing.JFrame();
        ANCPanel = new javax.swing.JPanel();
        ANCFirstNameField = new javax.swing.JTextField();
        ANCLastNameField = new javax.swing.JTextField();
        ANCTelephoneField = new javax.swing.JTextField();
        ANCEmailField = new javax.swing.JTextField();
        ANCAddressScroll = new javax.swing.JScrollPane();
        ANCAddressField = new javax.swing.JTextArea();
        ANCTitleComboBox = new javax.swing.JComboBox<>();
        ANCTitleLabel = new javax.swing.JLabel();
        ANCFirstNameLabel = new javax.swing.JLabel();
        ANCLastNameLabel = new javax.swing.JLabel();
        ANCTelephoneLabel = new javax.swing.JLabel();
        ANCEmailLabel = new javax.swing.JLabel();
        ANCAddressLabel = new javax.swing.JLabel();
        ANCNextBTN = new javax.swing.JButton();
        ANCClearBTN = new javax.swing.JButton();
        ANCContainerScroll = new javax.swing.JScrollPane();
        ANCConLocTable = new javax.swing.JTable();
        ANCContainerField = new javax.swing.JTextField();
        ANCLocationField = new javax.swing.JTextField();
        ANCContainerLabel = new javax.swing.JLabel();
        ANCLocationLabel = new javax.swing.JLabel();
        ANCAddBTN = new javax.swing.JButton();
        ANCRemoveBTN = new javax.swing.JButton();
        ANCLocComboBox = new javax.swing.JComboBox<>();
        ECDWindow = new javax.swing.JFrame();
        ECDPanel = new javax.swing.JPanel();
        ECDFindLastNameLabel = new javax.swing.JLabel();
        ECDLastNameInput = new javax.swing.JTextField();
        ECDFindBTN = new javax.swing.JButton();
        ECDFoundComboBox = new javax.swing.JComboBox<>();
        ECDFindIDLabel = new javax.swing.JLabel();
        ECDiDInput = new javax.swing.JTextField();
        ECDSearchBTN = new javax.swing.JButton();
        ECDNameLabel = new javax.swing.JLabel();
        ECDNameOutput = new javax.swing.JLabel();
        ECDTelephoneLabel = new javax.swing.JLabel();
        ECDTelephoneOutput = new javax.swing.JLabel();
        ECDEmailLabel = new javax.swing.JLabel();
        ECDEmailOutput = new javax.swing.JLabel();
        ECDAddressLabel = new javax.swing.JLabel();
        ECDAddressOutputScroll = new javax.swing.JScrollPane();
        ECDAddressOutput = new javax.swing.JTextArea();
        ECDTitleLabel = new javax.swing.JLabel();
        ECDTitleComboBox = new javax.swing.JComboBox<>();
        ECDNameChangeInput = new javax.swing.JTextField();
        ECDTelephoneChangeInput = new javax.swing.JTextField();
        ECDEmailChangeInput = new javax.swing.JTextField();
        ECDAddressChangeScroll = new javax.swing.JScrollPane();
        ECDAddressChangeInput = new javax.swing.JTextArea();
        ECDSaveBTN = new javax.swing.JButton();
        SCWindow = new javax.swing.JFrame();
        SCPanel = new javax.swing.JPanel();
        SCIdSearchInput = new javax.swing.JTextField();
        SCDateInOutput = new javax.swing.JLabel();
        SCTelephoneOutput = new javax.swing.JLabel();
        SCEmailOutput = new javax.swing.JLabel();
        SCAddressScroll = new javax.swing.JScrollPane();
        SCAddressOutput = new javax.swing.JTextArea();
        SCNameOutput = new javax.swing.JLabel();
        SCFoundComboBox = new javax.swing.JComboBox<>();
        SCAddressLabel = new javax.swing.JLabel();
        SCEmailLabel = new javax.swing.JLabel();
        SCTelephoneLabel = new javax.swing.JLabel();
        SCNameLabel = new javax.swing.JLabel();
        SCDateInLabel = new javax.swing.JLabel();
        SCIdSearchLabel = new javax.swing.JLabel();
        SCNameSearchLabel = new javax.swing.JLabel();
        SCConLocTableScroll = new javax.swing.JScrollPane();
        SCConLocTable = new javax.swing.JTable();
        SCLastNameSearchInput = new javax.swing.JTextField();
        RCWindow = new javax.swing.JFrame();
        RCPanel = new javax.swing.JPanel();
        RCNameInput = new javax.swing.JTextField();
        RCComboBox = new javax.swing.JComboBox<>();
        RCDetailsScroll = new javax.swing.JScrollPane();
        RCDetailsOutput = new javax.swing.JTextArea();
        RCNameLabel = new javax.swing.JLabel();
        RCDetailsLabel = new javax.swing.JLabel();
        RCRemoveBTN = new javax.swing.JButton();
        ANCONWindow = new javax.swing.JFrame();
        ANCONPanel = new javax.swing.JPanel();
        ANCONConInput = new javax.swing.JTextField();
        ANCONWareCusToggle = new javax.swing.JToggleButton();
        ANCONNameInput = new javax.swing.JTextField();
        ANCONComboBox = new javax.swing.JComboBox<>();
        ANCONAddBTN = new javax.swing.JButton();
        ANCONDetailsScroll = new javax.swing.JScrollPane();
        ANCONDetailsOutput = new javax.swing.JTextArea();
        ANCONLocInput = new javax.swing.JTextField();
        ANCONLocComboBox = new javax.swing.JComboBox<>();
        ANCONLocationLabel = new javax.swing.JLabel();
        MCWindow = new javax.swing.JFrame();
        SConWindow = new javax.swing.JFrame();
        SConPanel = new javax.swing.JPanel();
        SConLocationLabel = new javax.swing.JLabel();
        SCConContainerInput = new javax.swing.JTextField();
        SConNameLabel = new javax.swing.JLabel();
        ECWindow = new javax.swing.JFrame();
        ECPanel = new javax.swing.JPanel();
        ECContainerInput = new javax.swing.JTextField();
        PLWindow = new javax.swing.JFrame();
        PLPanel = new javax.swing.JPanel();
        PLTableScroll = new javax.swing.JScrollPane();
        PLPickedTable = new javax.swing.JTable();
        PLNameInput = new javax.swing.JTextField();
        PLNameComboBox = new javax.swing.JComboBox<>();
        PLAddAllBTN = new javax.swing.JButton();
        PLConComboBox = new javax.swing.JComboBox<>();
        PLAddComBTN = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        PLDetailsOutput = new javax.swing.JTextArea();
        PLInventoryScroll = new javax.swing.JScrollPane();
        PLInventoryTable = new javax.swing.JTable();
        PLAddItemBTN = new javax.swing.JButton();
        PLFinalizeBTN = new javax.swing.JButton();
        PLRemoveBTN = new javax.swing.JButton();
        PLIDLabel = new javax.swing.JLabel();
        PLPrintBTN = new javax.swing.JButton();
        SIWindow = new javax.swing.JFrame();
        SIPanel = new javax.swing.JPanel();
        SIInventoryScroll = new javax.swing.JScrollPane();
        SIInventoryTable = new javax.swing.JTable();
        SIConInput = new javax.swing.JTextField();
        SIItemInput = new javax.swing.JTextField();
        SIDescInput = new javax.swing.JTextField();
        SIContainerLabel = new javax.swing.JLabel();
        SIItemLabel = new javax.swing.JLabel();
        SIDescriptionLabel = new javax.swing.JLabel();
        SISearchBTN = new javax.swing.JButton();
        CNIWindow = new javax.swing.JFrame();
        CNIPanel = new javax.swing.JPanel();
        CNIInventoryTableScroll = new javax.swing.JScrollPane();
        CNIInventoryTable = new javax.swing.JTable();
        CNIContainerComboBox = new javax.swing.JComboBox<>();
        CNIContainersLabel = new javax.swing.JLabel();
        CNIIdOutput = new javax.swing.JTextField();
        CNINameOutput = new javax.swing.JTextField();
        CNITelephoneOutput = new javax.swing.JTextField();
        CNIAddressOutputFieldScroll = new javax.swing.JScrollPane();
        CNIAddressOutput = new javax.swing.JTextArea();
        CNIIdLabel = new javax.swing.JLabel();
        CNINameLabel = new javax.swing.JLabel();
        CNIEmailLabel = new javax.swing.JLabel();
        CNITelephoneLabel = new javax.swing.JLabel();
        CNIAddressLabel = new javax.swing.JLabel();
        CNIEmailOutput = new javax.swing.JTextField();
        CNIFinishBTN = new javax.swing.JButton();
        CNIAddNewItemBTN = new javax.swing.JButton();
        CNIRemoveItemBTN = new javax.swing.JButton();
        CNISaveBTN = new javax.swing.JButton();
        CNIContainerLabel = new javax.swing.JLabel();
        CNIChosenContainerLabel = new javax.swing.JLabel();
        EIWindow = new javax.swing.JFrame();
        EI1Panel = new javax.swing.JPanel();
        EIConTableScroll1 = new javax.swing.JScrollPane();
        EIConTable1 = new javax.swing.JTable();
        EIMoveBTN = new javax.swing.JButton();
        EIUndoBTN = new javax.swing.JButton();
        EISaveBTN2 = new javax.swing.JButton();
        EIConInput1 = new javax.swing.JTextField();
        EIConTableScroll2 = new javax.swing.JScrollPane();
        EIConTable2 = new javax.swing.JTable();
        EIConInput2 = new javax.swing.JTextField();
        EIRemoveBTN = new javax.swing.JButton();
        EIConLabel1 = new javax.swing.JLabel();
        EIConLabel2 = new javax.swing.JLabel();
        EIRelocatRadioBTN = new javax.swing.JRadioButton();
        EILockedLabel = new javax.swing.JLabel();
        EIAddBTN = new javax.swing.JButton();
        EISaveBTN = new javax.swing.JButton();
        EIConTableScroll3 = new javax.swing.JScrollPane();
        EIConTable3 = new javax.swing.JTable();
        VCHWindow = new javax.swing.JFrame();
        VCHPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        VCHLogArea = new javax.swing.JTextArea();
        VCHCustomerInput = new javax.swing.JTextField();
        VCHComboBox = new javax.swing.JComboBox<>();
        VCHCustomerLabel = new javax.swing.JLabel();
        VCHPrintBTN = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        VCHDetailsOutput = new javax.swing.JTextArea();
        VCONHWindow = new javax.swing.JFrame();
        VCONHPanel = new javax.swing.JPanel();
        VCONHScroll = new javax.swing.JScrollPane();
        VCONHTable = new javax.swing.JTable();
        VCONHComboBox = new javax.swing.JComboBox<>();
        VCONHConLabel = new javax.swing.JLabel();
        VAHWindow = new javax.swing.JFrame();
        VAHPanel = new javax.swing.JPanel();
        VAHScroll = new javax.swing.JScrollPane();
        VAHTable = new javax.swing.JTable();
        VAHUserTog = new javax.swing.JToggleButton();
        VAHActionTog = new javax.swing.JToggleButton();
        VAHDateTog = new javax.swing.JToggleButton();
        VAHUserCombo = new javax.swing.JComboBox<>();
        VAHActionCombo = new javax.swing.JComboBox<>();
        VAHPrintBTN = new javax.swing.JButton();
        VAHSearchBTN = new javax.swing.JButton();
        VAHDateCombo = new javax.swing.JComboBox<>();
        VAWindow = new javax.swing.JFrame();
        VAPanel = new javax.swing.JPanel();
        VALongestCustomerBTN = new javax.swing.JButton();
        VAMostContainersBTN = new javax.swing.JButton();
        VAOldestContainerBTN = new javax.swing.JButton();
        VAContainerAccoladesBTN = new javax.swing.JButton();
        VAMostActiveUserrBTN = new javax.swing.JButton();
        VAScroll = new javax.swing.JScrollPane();
        VATable = new javax.swing.JTable();
        FTLIWindow = new javax.swing.JFrame();
        FTLIPanel = new javax.swing.JPanel();
        FTLIUsernameInput = new javax.swing.JTextField();
        FTLIPasswordInput = new javax.swing.JPasswordField();
        FTLIRePasswordInput = new javax.swing.JPasswordField();
        FTLIUserNameLabel = new javax.swing.JLabel();
        FTLIPasswordLabel = new javax.swing.JLabel();
        FTLIRePasswordLabel = new javax.swing.JLabel();
        FTLISubmitBTN = new javax.swing.JButton();
        LIWindow = new javax.swing.JFrame();
        LIPanel = new javax.swing.JPanel();
        LIUserNameInput = new javax.swing.JTextField();
        LILogInBTN = new javax.swing.JButton();
        LIusernameLabel = new javax.swing.JLabel();
        LIPasswordLabel = new javax.swing.JLabel();
        LIPasswordInput = new javax.swing.JPasswordField();
        RUWindow = new javax.swing.JFrame();
        RUPanel = new javax.swing.JPanel();
        RUUserComboBox = new javax.swing.JComboBox<>();
        RUPasswordInput = new javax.swing.JPasswordField();
        RURemoveBTN = new javax.swing.JButton();
        CPWindow = new javax.swing.JFrame();
        CPPanel = new javax.swing.JPanel();
        CPUsernameInput = new javax.swing.JTextField();
        CPPasswordInput = new javax.swing.JPasswordField();
        CPRePasswordInput = new javax.swing.JPasswordField();
        CPUsernameLabel = new javax.swing.JLabel();
        CPPasswordLabel = new javax.swing.JLabel();
        CPRePasswordLabel = new javax.swing.JLabel();
        CPChangeBTN = new javax.swing.JButton();
        changePasswordLabel = new javax.swing.JLabel();
        RCONWindow = new javax.swing.JFrame();
        RCONPanel = new javax.swing.JPanel();
        RCONContainerInput = new javax.swing.JTextField();
        RCONRemoveBTN = new javax.swing.JButton();
        WMWindow = new javax.swing.JFrame();
        WMPanel = new javax.swing.JPanel();
        WMConFromInput = new javax.swing.JTextField();
        WMConScroll = new javax.swing.JScrollPane();
        WMConTable = new javax.swing.JTable();
        WMGenerateConBTN = new javax.swing.JButton();
        WMLocScroll = new javax.swing.JScrollPane();
        WMLocationsTable = new javax.swing.JTable();
        WMRowsInput = new javax.swing.JTextField();
        WMHeightInput = new javax.swing.JTextField();
        WMConToInput = new javax.swing.JTextField();
        WMToLabel = new javax.swing.JLabel();
        WMRowsLabel = new javax.swing.JLabel();
        WMHeightLabel = new javax.swing.JLabel();
        WMLocationGenBTN = new javax.swing.JButton();
        WMColumnsInput = new javax.swing.JTextField();
        WMColumnsLabel = new javax.swing.JLabel();
        WMCompleteBTN = new javax.swing.JButton();
        WMContainerGenLabel = new javax.swing.JLabel();
        WMLocationGenLabel = new javax.swing.JLabel();
        WMLocProgressBar = new javax.swing.JProgressBar();
        WMConProgressBar = new javax.swing.JProgressBar();
        MWAddToExistingRadio = new javax.swing.JRadioButton();
        MainPanel = new javax.swing.JPanel();
        MainLogo = new javax.swing.JLabel();
        SideMainBTNPanel = new javax.swing.JPanel();
        addNewCustomerBTN = new javax.swing.JButton();
        editCustomerDetailsBTN = new javax.swing.JButton();
        searchCustomerBTN = new javax.swing.JButton();
        addNewContainerBTN = new javax.swing.JButton();
        moveContainersBTN = new javax.swing.JButton();
        searchContainerBTN = new javax.swing.JButton();
        emptyContainerBTN = new javax.swing.JButton();
        createNewInventoryBTN = new javax.swing.JButton();
        searchInventoryBTN = new javax.swing.JButton();
        editInventoryBTN = new javax.swing.JButton();
        viewCustomerHistoryBTN = new javax.swing.JButton();
        viewContainerHistoryBTN = new javax.swing.JButton();
        viewActivityHistoryBTN = new javax.swing.JButton();
        viewAnalyticsBTN = new javax.swing.JButton();
        createPickListBTN = new javax.swing.JButton();
        removeCustomerBTN = new javax.swing.JButton();
        TopMainBTNPanel = new javax.swing.JPanel();
        addNewUserBTN = new javax.swing.JButton();
        removeUserBTN = new javax.swing.JButton();
        changePasswordsBTN = new javax.swing.JButton();
        removeContainerBTN = new javax.swing.JButton();
        manageWarehouseBTN = new javax.swing.JButton();
        mainCustomersScroll = new javax.swing.JScrollPane();
        mainCustomersTable = new javax.swing.JTable();
        mainInventoryScroll = new javax.swing.JScrollPane();
        mainInventoryTable = new javax.swing.JTable();
        mainCustomersTableLable = new javax.swing.JLabel();
        mainInventoryTableLabel = new javax.swing.JLabel();
        mainContainersLabel = new javax.swing.JLabel();
        mainContainersScroll = new javax.swing.JScrollPane();
        mainContainersTable = new javax.swing.JTable();

        ANCWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        ANCWindow.setTitle("Add New Customer");
        ANCWindow.setMinimumSize(new java.awt.Dimension(867, 650));
        ANCWindow.setResizable(false);

        ANCPanel.setBackground(new java.awt.Color(255, 255, 255));
        ANCPanel.setMinimumSize(new java.awt.Dimension(867, 650));
        ANCPanel.setLayout(null);
        ANCPanel.add(ANCFirstNameField);
        ANCFirstNameField.setBounds(143, 89, 252, 33);
        ANCPanel.add(ANCLastNameField);
        ANCLastNameField.setBounds(143, 167, 252, 33);
        ANCPanel.add(ANCTelephoneField);
        ANCTelephoneField.setBounds(143, 238, 252, 33);
        ANCPanel.add(ANCEmailField);
        ANCEmailField.setBounds(143, 309, 252, 33);

        ANCAddressField.setColumns(20);
        ANCAddressField.setRows(5);
        ANCAddressScroll.setViewportView(ANCAddressField);

        ANCPanel.add(ANCAddressScroll);
        ANCAddressScroll.setBounds(143, 380, 252, 140);

        ANCTitleComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "", "Mr", "Mrs", "Ms", "Miss", "Dr" }));
        ANCPanel.add(ANCTitleComboBox);
        ANCTitleComboBox.setBounds(74, 89, 51, 33);

        ANCTitleLabel.setText("Title:");
        ANCPanel.add(ANCTitleLabel);
        ANCTitleLabel.setBounds(74, 69, 50, 14);

        ANCFirstNameLabel.setText("First Name:");
        ANCPanel.add(ANCFirstNameLabel);
        ANCFirstNameLabel.setBounds(143, 69, 110, 14);

        ANCLastNameLabel.setText("Last Name:");
        ANCPanel.add(ANCLastNameLabel);
        ANCLastNameLabel.setBounds(143, 147, 130, 14);

        ANCTelephoneLabel.setText("TelePhone:");
        ANCPanel.add(ANCTelephoneLabel);
        ANCTelephoneLabel.setBounds(143, 218, 130, 14);

        ANCEmailLabel.setText("E-Mail:");
        ANCPanel.add(ANCEmailLabel);
        ANCEmailLabel.setBounds(143, 289, 140, 14);

        ANCAddressLabel.setText("Address:");
        ANCPanel.add(ANCAddressLabel);
        ANCAddressLabel.setBounds(143, 360, 140, 14);

        ANCNextBTN.setText("NEXT");
        ANCNextBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ANCNextBTNActionPerformed(evt);
            }
        });
        ANCPanel.add(ANCNextBTN);
        ANCNextBTN.setBounds(720, 530, 85, 23);

        ANCClearBTN.setText("CLEAR");
        ANCClearBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ANCClearBTNActionPerformed(evt);
            }
        });
        ANCPanel.add(ANCClearBTN);
        ANCClearBTN.setBounds(140, 530, 85, 23);

        ANCConLocTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Container", "Location"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                true, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        ANCConLocTable.getTableHeader().setReorderingAllowed(false);
        ANCContainerScroll.setViewportView(ANCConLocTable);
        if (ANCConLocTable.getColumnModel().getColumnCount() > 0) {
            ANCConLocTable.getColumnModel().getColumn(0).setResizable(false);
            ANCConLocTable.getColumnModel().getColumn(1).setResizable(false);
        }

        ANCPanel.add(ANCContainerScroll);
        ANCContainerScroll.setBounds(560, 80, 240, 440);
        ANCPanel.add(ANCContainerField);
        ANCContainerField.setBounds(430, 90, 110, 30);
        ANCPanel.add(ANCLocationField);
        ANCLocationField.setBounds(430, 150, 110, 30);

        ANCContainerLabel.setText("Container:");
        ANCPanel.add(ANCContainerLabel);
        ANCContainerLabel.setBounds(430, 70, 90, 14);

        ANCLocationLabel.setText("Location:");
        ANCPanel.add(ANCLocationLabel);
        ANCLocationLabel.setBounds(430, 130, 80, 14);

        ANCAddBTN.setText("ADD");
        ANCAddBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ANCAddBTNActionPerformed(evt);
            }
        });
        ANCPanel.add(ANCAddBTN);
        ANCAddBTN.setBounds(430, 230, 110, 23);

        ANCRemoveBTN.setText("REMOVE");
        ANCRemoveBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ANCRemoveBTNActionPerformed(evt);
            }
        });
        ANCPanel.add(ANCRemoveBTN);
        ANCRemoveBTN.setBounds(560, 530, 90, 23);

        ANCPanel.add(ANCLocComboBox);
        ANCLocComboBox.setBounds(430, 190, 110, 20);

        javax.swing.GroupLayout ANCWindowLayout = new javax.swing.GroupLayout(ANCWindow.getContentPane());
        ANCWindow.getContentPane().setLayout(ANCWindowLayout);
        ANCWindowLayout.setHorizontalGroup(
            ANCWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ANCPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        ANCWindowLayout.setVerticalGroup(
            ANCWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ANCPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        ANCPanel.getAccessibleContext().setAccessibleName("");

        ECDWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        ECDWindow.setTitle("Edit Customer Details");
        ECDWindow.setMinimumSize(new java.awt.Dimension(823, 760));
        ECDWindow.setResizable(false);

        ECDPanel.setBackground(new java.awt.Color(255, 255, 255));
        ECDPanel.setMinimumSize(new java.awt.Dimension(823, 760));
        ECDPanel.setLayout(null);

        ECDFindLastNameLabel.setText("Last Name:");
        ECDPanel.add(ECDFindLastNameLabel);
        ECDFindLastNameLabel.setBounds(60, 40, 70, 30);
        ECDPanel.add(ECDLastNameInput);
        ECDLastNameInput.setBounds(130, 40, 200, 30);

        ECDFindBTN.setText("FIND");
        ECDFindBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ECDFindBTNActionPerformed(evt);
            }
        });
        ECDPanel.add(ECDFindBTN);
        ECDFindBTN.setBounds(340, 40, 73, 30);

        ECDPanel.add(ECDFoundComboBox);
        ECDFoundComboBox.setBounds(440, 40, 230, 22);

        ECDFindIDLabel.setText("ID:");
        ECDPanel.add(ECDFindIDLabel);
        ECDFindIDLabel.setBounds(100, 100, 15, 14);
        ECDPanel.add(ECDiDInput);
        ECDiDInput.setBounds(130, 90, 80, 30);

        ECDSearchBTN.setText("SEARCH");
        ECDSearchBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ECDSearchBTNActionPerformed(evt);
            }
        });
        ECDPanel.add(ECDSearchBTN);
        ECDSearchBTN.setBounds(233, 90, 100, 30);

        ECDNameLabel.setText("Name:");
        ECDPanel.add(ECDNameLabel);
        ECDNameLabel.setBounds(40, 210, 60, 14);

        ECDNameOutput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        ECDNameOutput.setFocusable(false);
        ECDPanel.add(ECDNameOutput);
        ECDNameOutput.setBounds(40, 230, 320, 30);

        ECDTelephoneLabel.setText("Telephone:");
        ECDPanel.add(ECDTelephoneLabel);
        ECDTelephoneLabel.setBounds(40, 290, 130, 14);

        ECDTelephoneOutput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        ECDTelephoneOutput.setFocusable(false);
        ECDPanel.add(ECDTelephoneOutput);
        ECDTelephoneOutput.setBounds(40, 310, 320, 30);

        ECDEmailLabel.setText("E-Mail:");
        ECDPanel.add(ECDEmailLabel);
        ECDEmailLabel.setBounds(40, 370, 120, 14);

        ECDEmailOutput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        ECDEmailOutput.setFocusable(false);
        ECDPanel.add(ECDEmailOutput);
        ECDEmailOutput.setBounds(40, 390, 320, 30);

        ECDAddressLabel.setText("Address:");
        ECDPanel.add(ECDAddressLabel);
        ECDAddressLabel.setBounds(40, 450, 140, 14);

        ECDAddressOutput.setEditable(false);
        ECDAddressOutput.setColumns(20);
        ECDAddressOutput.setRows(5);
        ECDAddressOutputScroll.setViewportView(ECDAddressOutput);

        ECDPanel.add(ECDAddressOutputScroll);
        ECDAddressOutputScroll.setBounds(40, 470, 320, 170);

        ECDTitleLabel.setText("Title:");
        ECDPanel.add(ECDTitleLabel);
        ECDTitleLabel.setBounds(440, 110, 50, 14);

        ECDTitleComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Mr", "Mrs", "Miss", "Ms", "Dr" }));
        ECDPanel.add(ECDTitleComboBox);
        ECDTitleComboBox.setBounds(440, 130, 60, 30);
        ECDPanel.add(ECDNameChangeInput);
        ECDNameChangeInput.setBounds(440, 230, 320, 30);
        ECDPanel.add(ECDTelephoneChangeInput);
        ECDTelephoneChangeInput.setBounds(440, 310, 320, 30);
        ECDPanel.add(ECDEmailChangeInput);
        ECDEmailChangeInput.setBounds(440, 390, 320, 30);

        ECDAddressChangeInput.setColumns(20);
        ECDAddressChangeInput.setRows(5);
        ECDAddressChangeScroll.setViewportView(ECDAddressChangeInput);

        ECDPanel.add(ECDAddressChangeScroll);
        ECDAddressChangeScroll.setBounds(440, 470, 320, 170);

        ECDSaveBTN.setText("SAVE");
        ECDSaveBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ECDSaveBTNActionPerformed(evt);
            }
        });
        ECDPanel.add(ECDSaveBTN);
        ECDSaveBTN.setBounds(690, 650, 70, 30);

        javax.swing.GroupLayout ECDWindowLayout = new javax.swing.GroupLayout(ECDWindow.getContentPane());
        ECDWindow.getContentPane().setLayout(ECDWindowLayout);
        ECDWindowLayout.setHorizontalGroup(
            ECDWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ECDPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 823, Short.MAX_VALUE)
        );
        ECDWindowLayout.setVerticalGroup(
            ECDWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ECDPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
        );

        SCWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        SCWindow.setTitle("Search Customer");
        SCWindow.setMinimumSize(new java.awt.Dimension(663, 736));
        SCWindow.setResizable(false);

        SCPanel.setBackground(new java.awt.Color(255, 255, 255));
        SCPanel.setMinimumSize(new java.awt.Dimension(663, 736));
        SCPanel.setLayout(null);
        SCPanel.add(SCIdSearchInput);
        SCIdSearchInput.setBounds(100, 130, 100, 30);

        SCDateInOutput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        SCPanel.add(SCDateInOutput);
        SCDateInOutput.setBounds(100, 210, 240, 30);

        SCTelephoneOutput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        SCPanel.add(SCTelephoneOutput);
        SCTelephoneOutput.setBounds(100, 350, 240, 30);

        SCEmailOutput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        SCPanel.add(SCEmailOutput);
        SCEmailOutput.setBounds(100, 420, 240, 30);

        SCAddressOutput.setColumns(20);
        SCAddressOutput.setRows(5);
        SCAddressScroll.setViewportView(SCAddressOutput);

        SCPanel.add(SCAddressScroll);
        SCAddressScroll.setBounds(100, 500, 240, 170);

        SCNameOutput.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        SCPanel.add(SCNameOutput);
        SCNameOutput.setBounds(100, 280, 240, 30);

        SCPanel.add(SCFoundComboBox);
        SCFoundComboBox.setBounds(100, 70, 240, 30);

        SCAddressLabel.setText("Address:");
        SCPanel.add(SCAddressLabel);
        SCAddressLabel.setBounds(100, 480, 110, 14);

        SCEmailLabel.setText("E-Mail:");
        SCPanel.add(SCEmailLabel);
        SCEmailLabel.setBounds(100, 400, 110, 14);

        SCTelephoneLabel.setText("Telephone:");
        SCPanel.add(SCTelephoneLabel);
        SCTelephoneLabel.setBounds(100, 330, 110, 14);

        SCNameLabel.setText("Name:");
        SCPanel.add(SCNameLabel);
        SCNameLabel.setBounds(100, 260, 110, 14);

        SCDateInLabel.setText("Date In:");
        SCPanel.add(SCDateInLabel);
        SCDateInLabel.setBounds(100, 190, 70, 14);

        SCIdSearchLabel.setText("ID:");
        SCPanel.add(SCIdSearchLabel);
        SCIdSearchLabel.setBounds(60, 130, 30, 20);

        SCNameSearchLabel.setText("NAME:");
        SCPanel.add(SCNameSearchLabel);
        SCNameSearchLabel.setBounds(50, 40, 40, 20);

        SCConLocTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Container", "Location"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        SCConLocTable.getTableHeader().setReorderingAllowed(false);
        SCConLocTableScroll.setViewportView(SCConLocTable);
        if (SCConLocTable.getColumnModel().getColumnCount() > 0) {
            SCConLocTable.getColumnModel().getColumn(0).setResizable(false);
            SCConLocTable.getColumnModel().getColumn(1).setResizable(false);
        }

        SCPanel.add(SCConLocTableScroll);
        SCConLocTableScroll.setBounds(390, 40, 180, 630);
        SCPanel.add(SCLastNameSearchInput);
        SCLastNameSearchInput.setBounds(100, 40, 240, 30);

        javax.swing.GroupLayout SCWindowLayout = new javax.swing.GroupLayout(SCWindow.getContentPane());
        SCWindow.getContentPane().setLayout(SCWindowLayout);
        SCWindowLayout.setHorizontalGroup(
            SCWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(SCPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 663, Short.MAX_VALUE)
        );
        SCWindowLayout.setVerticalGroup(
            SCWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(SCPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 736, Short.MAX_VALUE)
        );

        RCWindow.setMinimumSize(new java.awt.Dimension(542, 709));
        RCWindow.setResizable(false);

        RCPanel.setBackground(new java.awt.Color(255, 255, 255));
        RCPanel.setMinimumSize(new java.awt.Dimension(542, 709));
        RCPanel.setLayout(null);
        RCPanel.add(RCNameInput);
        RCNameInput.setBounds(80, 70, 137, 32);

        RCPanel.add(RCComboBox);
        RCComboBox.setBounds(240, 70, 223, 32);

        RCDetailsOutput.setEditable(false);
        RCDetailsOutput.setColumns(20);
        RCDetailsOutput.setRows(5);
        RCDetailsScroll.setViewportView(RCDetailsOutput);

        RCPanel.add(RCDetailsScroll);
        RCDetailsScroll.setBounds(80, 160, 378, 420);

        RCNameLabel.setText("NAME:");
        RCPanel.add(RCNameLabel);
        RCNameLabel.setBounds(80, 50, 80, 14);

        RCDetailsLabel.setText("DETAILS:");
        RCPanel.add(RCDetailsLabel);
        RCDetailsLabel.setBounds(80, 140, 80, 14);

        RCRemoveBTN.setText("REMOVE");
        RCRemoveBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RCRemoveBTNActionPerformed(evt);
            }
        });
        RCPanel.add(RCRemoveBTN);
        RCRemoveBTN.setBounds(370, 590, 90, 30);

        javax.swing.GroupLayout RCWindowLayout = new javax.swing.GroupLayout(RCWindow.getContentPane());
        RCWindow.getContentPane().setLayout(RCWindowLayout);
        RCWindowLayout.setHorizontalGroup(
            RCWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(RCPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 542, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        RCWindowLayout.setVerticalGroup(
            RCWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RCWindowLayout.createSequentialGroup()
                .addComponent(RCPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 709, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        ANCONWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        ANCONWindow.setTitle("Add New Container");
        ANCONWindow.setMinimumSize(new java.awt.Dimension(391, 169));
        ANCONWindow.setResizable(false);

        ANCONPanel.setBackground(new java.awt.Color(255, 255, 255));
        ANCONPanel.setLayout(null);
        ANCONPanel.add(ANCONConInput);
        ANCONConInput.setBounds(100, 41, 136, 34);

        ANCONWareCusToggle.setText("Add To Customer");
        ANCONPanel.add(ANCONWareCusToggle);
        ANCONWareCusToggle.setBounds(100, 86, 136, 23);
        ANCONPanel.add(ANCONNameInput);
        ANCONNameInput.setBounds(100, 170, 241, 34);

        ANCONPanel.add(ANCONComboBox);
        ANCONComboBox.setBounds(100, 210, 241, 30);

        ANCONAddBTN.setText("ADD");
        ANCONAddBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ANCONAddBTNActionPerformed(evt);
            }
        });
        ANCONPanel.add(ANCONAddBTN);
        ANCONAddBTN.setBounds(250, 40, 95, 30);

        ANCONDetailsOutput.setEditable(false);
        ANCONDetailsOutput.setColumns(20);
        ANCONDetailsOutput.setRows(5);
        ANCONDetailsScroll.setViewportView(ANCONDetailsOutput);

        ANCONPanel.add(ANCONDetailsScroll);
        ANCONDetailsScroll.setBounds(100, 250, 241, 200);
        ANCONPanel.add(ANCONLocInput);
        ANCONLocInput.setBounds(370, 170, 100, 30);

        ANCONPanel.add(ANCONLocComboBox);
        ANCONLocComboBox.setBounds(370, 210, 100, 30);

        ANCONLocationLabel.setText("Location:");
        ANCONPanel.add(ANCONLocationLabel);
        ANCONLocationLabel.setBounds(370, 150, 90, 20);

        javax.swing.GroupLayout ANCONWindowLayout = new javax.swing.GroupLayout(ANCONWindow.getContentPane());
        ANCONWindow.getContentPane().setLayout(ANCONWindowLayout);
        ANCONWindowLayout.setHorizontalGroup(
            ANCONWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ANCONPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
        );
        ANCONWindowLayout.setVerticalGroup(
            ANCONWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ANCONPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
        );

        MCWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        MCWindow.setTitle("Move Containers");
        MCWindow.setMinimumSize(new java.awt.Dimension(1448, 900));

        javax.swing.GroupLayout MCWindowLayout = new javax.swing.GroupLayout(MCWindow.getContentPane());
        MCWindow.getContentPane().setLayout(MCWindowLayout);
        MCWindowLayout.setHorizontalGroup(
            MCWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1448, Short.MAX_VALUE)
        );
        MCWindowLayout.setVerticalGroup(
            MCWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 900, Short.MAX_VALUE)
        );

        SConWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        SConWindow.setTitle("Search Container");
        SConWindow.setMinimumSize(new java.awt.Dimension(465, 296));
        SConWindow.setResizable(false);

        SConPanel.setBackground(new java.awt.Color(255, 255, 255));
        SConPanel.setMinimumSize(new java.awt.Dimension(465, 296));

        SConLocationLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        SConLocationLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        SCConContainerInput.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        SConNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout SConPanelLayout = new javax.swing.GroupLayout(SConPanel);
        SConPanel.setLayout(SConPanelLayout);
        SConPanelLayout.setHorizontalGroup(
            SConPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SConPanelLayout.createSequentialGroup()
                .addGroup(SConPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SConPanelLayout.createSequentialGroup()
                        .addGap(170, 170, 170)
                        .addGroup(SConPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(SCConContainerInput, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SConLocationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(SConPanelLayout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addComponent(SConNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(55, 55, 55))
        );
        SConPanelLayout.setVerticalGroup(
            SConPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SConPanelLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(SCConContainerInput, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(62, 62, 62)
                .addComponent(SConLocationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(SConNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout SConWindowLayout = new javax.swing.GroupLayout(SConWindow.getContentPane());
        SConWindow.getContentPane().setLayout(SConWindowLayout);
        SConWindowLayout.setHorizontalGroup(
            SConWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(SConPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
        );
        SConWindowLayout.setVerticalGroup(
            SConWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(SConPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
        );

        ECWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        ECWindow.setTitle("Empty Container");
        ECWindow.setMinimumSize(new java.awt.Dimension(423, 255));
        ECWindow.setResizable(false);

        ECPanel.setBackground(new java.awt.Color(255, 255, 255));
        ECPanel.setMinimumSize(new java.awt.Dimension(423, 255));
        ECPanel.setName(""); // NOI18N

        ECContainerInput.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        javax.swing.GroupLayout ECPanelLayout = new javax.swing.GroupLayout(ECPanel);
        ECPanel.setLayout(ECPanelLayout);
        ECPanelLayout.setHorizontalGroup(
            ECPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ECPanelLayout.createSequentialGroup()
                .addGap(152, 152, 152)
                .addComponent(ECContainerInput, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(163, Short.MAX_VALUE))
        );
        ECPanelLayout.setVerticalGroup(
            ECPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ECPanelLayout.createSequentialGroup()
                .addGap(92, 92, 92)
                .addComponent(ECContainerInput, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(116, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout ECWindowLayout = new javax.swing.GroupLayout(ECWindow.getContentPane());
        ECWindow.getContentPane().setLayout(ECWindowLayout);
        ECWindowLayout.setHorizontalGroup(
            ECWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ECPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        ECWindowLayout.setVerticalGroup(
            ECWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ECPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        PLWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        PLWindow.setTitle("Pick List");
        PLWindow.setMinimumSize(new java.awt.Dimension(1279, 859));
        PLWindow.setResizable(false);

        PLPanel.setBackground(new java.awt.Color(255, 255, 255));
        PLPanel.setMinimumSize(new java.awt.Dimension(1279, 859));
        PLPanel.setLayout(null);

        PLPickedTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Customer", "Container", "Location", "Item"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        PLPickedTable.getTableHeader().setReorderingAllowed(false);
        PLTableScroll.setViewportView(PLPickedTable);
        if (PLPickedTable.getColumnModel().getColumnCount() > 0) {
            PLPickedTable.getColumnModel().getColumn(1).setResizable(false);
        }

        PLPanel.add(PLTableScroll);
        PLTableScroll.setBounds(66, 311, 1170, 430);
        PLPanel.add(PLNameInput);
        PLNameInput.setBounds(66, 27, 136, 28);

        PLPanel.add(PLNameComboBox);
        PLNameComboBox.setBounds(66, 61, 136, 28);

        PLAddAllBTN.setText("ADD *");
        PLAddAllBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PLAddAllBTNActionPerformed(evt);
            }
        });
        PLPanel.add(PLAddAllBTN);
        PLAddAllBTN.setBounds(212, 27, 63, 28);

        PLPanel.add(PLConComboBox);
        PLConComboBox.setBounds(291, 27, 60, 28);

        PLAddComBTN.setText("ADD");
        PLAddComBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PLAddComBTNActionPerformed(evt);
            }
        });
        PLPanel.add(PLAddComBTN);
        PLAddComBTN.setBounds(366, 27, 53, 28);

        PLDetailsOutput.setColumns(20);
        PLDetailsOutput.setRows(5);
        jScrollPane4.setViewportView(PLDetailsOutput);

        PLPanel.add(jScrollPane4);
        jScrollPane4.setBounds(60, 120, 297, 110);

        PLInventoryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "#", "Item", "Description", "Condition", "PhotoFile"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        PLInventoryTable.getTableHeader().setReorderingAllowed(false);
        PLInventoryScroll.setViewportView(PLInventoryTable);
        if (PLInventoryTable.getColumnModel().getColumnCount() > 0) {
            PLInventoryTable.getColumnModel().getColumn(0).setPreferredWidth(30);
            PLInventoryTable.getColumnModel().getColumn(1).setMinWidth(200);
            PLInventoryTable.getColumnModel().getColumn(2).setMinWidth(200);
            PLInventoryTable.getColumnModel().getColumn(3).setMinWidth(200);
        }

        PLPanel.add(PLInventoryScroll);
        PLInventoryScroll.setBounds(440, 30, 790, 200);

        PLAddItemBTN.setText("ADD ITEM");
        PLAddItemBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PLAddItemBTNActionPerformed(evt);
            }
        });
        PLPanel.add(PLAddItemBTN);
        PLAddItemBTN.setBounds(1140, 240, 90, 30);

        PLFinalizeBTN.setText("FINALIZE");
        PLFinalizeBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PLFinalizeBTNActionPerformed(evt);
            }
        });
        PLPanel.add(PLFinalizeBTN);
        PLFinalizeBTN.setBounds(1160, 760, 80, 30);

        PLRemoveBTN.setText("REMOVE");
        PLRemoveBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PLRemoveBTNActionPerformed(evt);
            }
        });
        PLPanel.add(PLRemoveBTN);
        PLRemoveBTN.setBounds(70, 760, 90, 30);

        PLIDLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        PLPanel.add(PLIDLabel);
        PLIDLabel.setBounds(210, 60, 90, 30);

        PLPrintBTN.setText("PRINT");
        PLPrintBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PLPrintBTNActionPerformed(evt);
            }
        });
        PLPanel.add(PLPrintBTN);
        PLPrintBTN.setBounds(1040, 760, 90, 30);

        javax.swing.GroupLayout PLWindowLayout = new javax.swing.GroupLayout(PLWindow.getContentPane());
        PLWindow.getContentPane().setLayout(PLWindowLayout);
        PLWindowLayout.setHorizontalGroup(
            PLWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PLPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        PLWindowLayout.setVerticalGroup(
            PLWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PLPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        SIWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        SIWindow.setTitle("Search Inventory");
        SIWindow.setMinimumSize(new java.awt.Dimension(1367, 658));

        SIPanel.setBackground(new java.awt.Color(255, 255, 255));
        SIPanel.setMinimumSize(new java.awt.Dimension(1367, 658));

        SIInventoryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Container", "#", "Item", "Description", "Condition", "Photos"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        SIInventoryScroll.setViewportView(SIInventoryTable);
        if (SIInventoryTable.getColumnModel().getColumnCount() > 0) {
            SIInventoryTable.getColumnModel().getColumn(1).setPreferredWidth(50);
            SIInventoryTable.getColumnModel().getColumn(1).setMaxWidth(50);
            SIInventoryTable.getColumnModel().getColumn(2).setPreferredWidth(200);
            SIInventoryTable.getColumnModel().getColumn(2).setMaxWidth(200);
        }

        SIContainerLabel.setText("Container:");

        SIItemLabel.setText("Item:");

        SIDescriptionLabel.setText("Description:");

        SISearchBTN.setText("SEARCH");
        SISearchBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SISearchBTNActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout SIPanelLayout = new javax.swing.GroupLayout(SIPanel);
        SIPanel.setLayout(SIPanelLayout);
        SIPanelLayout.setHorizontalGroup(
            SIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SIPanelLayout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(SIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(SIPanelLayout.createSequentialGroup()
                        .addGroup(SIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(SIContainerLabel)
                            .addComponent(SIItemLabel)
                            .addComponent(SIDescriptionLabel))
                        .addGap(18, 18, 18)
                        .addGroup(SIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(SIDescInput, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                            .addComponent(SIItemInput)
                            .addComponent(SIConInput, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(SISearchBTN, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                .addComponent(SIInventoryScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 1054, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34))
        );
        SIPanelLayout.setVerticalGroup(
            SIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SIPanelLayout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addGroup(SIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SIPanelLayout.createSequentialGroup()
                        .addGroup(SIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(SIContainerLabel)
                            .addComponent(SIConInput, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(SIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(SIPanelLayout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addComponent(SIItemLabel))
                            .addGroup(SIPanelLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(SIItemInput, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(SIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(SIDescriptionLabel)
                            .addComponent(SIDescInput, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(SISearchBTN, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(SIInventoryScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 520, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(102, Short.MAX_VALUE))
        );

        SIPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {SIConInput, SIDescInput, SIItemInput});

        javax.swing.GroupLayout SIWindowLayout = new javax.swing.GroupLayout(SIWindow.getContentPane());
        SIWindow.getContentPane().setLayout(SIWindowLayout);
        SIWindowLayout.setHorizontalGroup(
            SIWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(SIPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        SIWindowLayout.setVerticalGroup(
            SIWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(SIPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        CNIWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        CNIWindow.setTitle("Create New Inventory");
        CNIWindow.setMinimumSize(new java.awt.Dimension(1319, 689));
        CNIWindow.setResizable(false);

        CNIPanel.setBackground(new java.awt.Color(255, 255, 255));
        CNIPanel.setMinimumSize(new java.awt.Dimension(1319, 689));
        CNIPanel.setLayout(null);

        CNIInventoryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "#Items", "Item", "Description", "Condition", "Photo File"
            }
        ));
        CNIInventoryTable.getTableHeader().setReorderingAllowed(false);
        CNIInventoryTableScroll.setViewportView(CNIInventoryTable);
        if (CNIInventoryTable.getColumnModel().getColumnCount() > 0) {
            CNIInventoryTable.getColumnModel().getColumn(0).setResizable(false);
            CNIInventoryTable.getColumnModel().getColumn(1).setResizable(false);
            CNIInventoryTable.getColumnModel().getColumn(2).setResizable(false);
            CNIInventoryTable.getColumnModel().getColumn(3).setResizable(false);
            CNIInventoryTable.getColumnModel().getColumn(4).setResizable(false);
        }

        CNIPanel.add(CNIInventoryTableScroll);
        CNIInventoryTableScroll.setBounds(39, 193, 1216, 370);

        CNIContainerComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CNIContainerComboBoxActionPerformed(evt);
            }
        });
        CNIPanel.add(CNIContainerComboBox);
        CNIContainerComboBox.setBounds(39, 43, 122, 22);

        CNIContainersLabel.setText("Containers:");
        CNIPanel.add(CNIContainersLabel);
        CNIContainersLabel.setBounds(39, 23, 122, 14);

        CNIIdOutput.setEditable(false);
        CNIPanel.add(CNIIdOutput);
        CNIIdOutput.setBounds(280, 50, 110, 30);

        CNINameOutput.setEditable(false);
        CNIPanel.add(CNINameOutput);
        CNINameOutput.setBounds(280, 110, 270, 30);

        CNITelephoneOutput.setEditable(false);
        CNIPanel.add(CNITelephoneOutput);
        CNITelephoneOutput.setBounds(590, 50, 220, 30);

        CNIAddressOutput.setEditable(false);
        CNIAddressOutput.setColumns(20);
        CNIAddressOutput.setRows(5);
        CNIAddressOutputFieldScroll.setViewportView(CNIAddressOutput);

        CNIPanel.add(CNIAddressOutputFieldScroll);
        CNIAddressOutputFieldScroll.setBounds(830, 50, 310, 110);

        CNIIdLabel.setText("ID:");
        CNIPanel.add(CNIIdLabel);
        CNIIdLabel.setBounds(280, 30, 150, 14);

        CNINameLabel.setText("Name:");
        CNIPanel.add(CNINameLabel);
        CNINameLabel.setBounds(280, 90, 150, 14);

        CNIEmailLabel.setText("E-Mail:");
        CNIPanel.add(CNIEmailLabel);
        CNIEmailLabel.setBounds(590, 90, 150, 14);

        CNITelephoneLabel.setText("Telephone:");
        CNIPanel.add(CNITelephoneLabel);
        CNITelephoneLabel.setBounds(590, 30, 150, 14);

        CNIAddressLabel.setText("Address:");
        CNIPanel.add(CNIAddressLabel);
        CNIAddressLabel.setBounds(830, 30, 140, 14);

        CNIEmailOutput.setEditable(false);
        CNIPanel.add(CNIEmailOutput);
        CNIEmailOutput.setBounds(590, 110, 220, 30);

        CNIFinishBTN.setText("FINISH");
        CNIFinishBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CNIFinishBTNActionPerformed(evt);
            }
        });
        CNIPanel.add(CNIFinishBTN);
        CNIFinishBTN.setBounds(1160, 580, 90, 23);

        CNIAddNewItemBTN.setText("Add New Item");
        CNIAddNewItemBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CNIAddNewItemBTNActionPerformed(evt);
            }
        });
        CNIPanel.add(CNIAddNewItemBTN);
        CNIAddNewItemBTN.setBounds(40, 580, 120, 23);

        CNIRemoveItemBTN.setText("Remove Item");
        CNIRemoveItemBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CNIRemoveItemBTNActionPerformed(evt);
            }
        });
        CNIPanel.add(CNIRemoveItemBTN);
        CNIRemoveItemBTN.setBounds(190, 580, 130, 23);

        CNISaveBTN.setText("SAVE");
        CNISaveBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CNISaveBTNActionPerformed(evt);
            }
        });
        CNIPanel.add(CNISaveBTN);
        CNISaveBTN.setBounds(340, 580, 90, 23);

        CNIContainerLabel.setText("Container:");
        CNIPanel.add(CNIContainerLabel);
        CNIContainerLabel.setBounds(40, 160, 60, 14);

        CNIChosenContainerLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        CNIPanel.add(CNIChosenContainerLabel);
        CNIChosenContainerLabel.setBounds(100, 160, 60, 20);

        javax.swing.GroupLayout CNIWindowLayout = new javax.swing.GroupLayout(CNIWindow.getContentPane());
        CNIWindow.getContentPane().setLayout(CNIWindowLayout);
        CNIWindowLayout.setHorizontalGroup(
            CNIWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(CNIPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 1322, Short.MAX_VALUE)
        );
        CNIWindowLayout.setVerticalGroup(
            CNIWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(CNIPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 689, Short.MAX_VALUE)
        );

        EIWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        EIWindow.setTitle("Edit Inventory");
        EIWindow.setMinimumSize(new java.awt.Dimension(814, 716));
        EIWindow.getContentPane().setLayout(null);

        EI1Panel.setBackground(new java.awt.Color(255, 255, 255));
        EI1Panel.setMinimumSize(new java.awt.Dimension(814, 716));

        EIConTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "#", "Item", "Description", "Condition", "PhotoFile"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        EIConTableScroll1.setViewportView(EIConTable1);

        EIMoveBTN.setText("MOVE");
        EIMoveBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EIMoveBTNActionPerformed(evt);
            }
        });

        EIUndoBTN.setText("UNDO");
        EIUndoBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EIUndoBTNActionPerformed(evt);
            }
        });

        EISaveBTN2.setText("SAVE");
        EISaveBTN2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EISaveBTN2ActionPerformed(evt);
            }
        });

        EIConTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "#", "Item", "Description", "Condition", "PhotoFile"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        EIConTableScroll2.setViewportView(EIConTable2);

        EIRemoveBTN.setText("REMOVE");
        EIRemoveBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EIRemoveBTNActionPerformed(evt);
            }
        });

        EIConLabel1.setText("Container:");

        EIConLabel2.setText("Container:");

        EIRelocatRadioBTN.setText("Relocate To Container");

        EILockedLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        EILockedLabel.setForeground(new java.awt.Color(102, 255, 51));
        EILockedLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        EIAddBTN.setText("ADD");
        EIAddBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EIAddBTNActionPerformed(evt);
            }
        });

        EISaveBTN.setText("SAVE");

        EIConTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null}
            },
            new String [] {
                "#", "Item", "Description", "Condition", "PhotoFile"
            }
        ));
        EIConTable3.setColumnSelectionAllowed(true);
        EIConTableScroll3.setViewportView(EIConTable3);
        EIConTable3.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        javax.swing.GroupLayout EI1PanelLayout = new javax.swing.GroupLayout(EI1Panel);
        EI1Panel.setLayout(EI1PanelLayout);
        EI1PanelLayout.setHorizontalGroup(
            EI1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(EI1PanelLayout.createSequentialGroup()
                .addGroup(EI1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(EI1PanelLayout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(EI1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(EIConLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(EIConInput1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addComponent(EIConTableScroll1, javax.swing.GroupLayout.PREFERRED_SIZE, 634, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(EI1PanelLayout.createSequentialGroup()
                        .addGap(100, 100, 100)
                        .addGroup(EI1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(EIConTableScroll3, javax.swing.GroupLayout.PREFERRED_SIZE, 634, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(EI1PanelLayout.createSequentialGroup()
                                .addComponent(EIRemoveBTN, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(EIAddBTN)
                                .addGap(18, 18, 18)
                                .addComponent(EISaveBTN)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(EIRelocatRadioBTN, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(EIMoveBTN, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(107, 107, 107)
                .addGroup(EI1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(EIConLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                    .addComponent(EIConInput2)
                    .addComponent(EILockedLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(11, 11, 11)
                .addGroup(EI1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(EI1PanelLayout.createSequentialGroup()
                        .addComponent(EIUndoBTN, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(EISaveBTN2, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(EIConTableScroll2, javax.swing.GroupLayout.PREFERRED_SIZE, 634, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        EI1PanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {EIAddBTN, EIRemoveBTN, EISaveBTN});

        EI1PanelLayout.setVerticalGroup(
            EI1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(EI1PanelLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(EI1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, EI1PanelLayout.createSequentialGroup()
                        .addComponent(EIConTableScroll1)
                        .addGap(18, 18, 18)
                        .addComponent(EIConTableScroll3, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(EI1PanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(EIConLabel1)
                        .addGap(6, 6, 6)
                        .addComponent(EIConInput1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(EI1PanelLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(EIConLabel2)
                        .addGap(6, 6, 6)
                        .addComponent(EIConInput2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(EILockedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(EIConTableScroll2, javax.swing.GroupLayout.PREFERRED_SIZE, 565, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(EI1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(EI1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(EIRemoveBTN, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(EIAddBTN)
                        .addComponent(EISaveBTN)
                        .addComponent(EIRelocatRadioBTN, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(EIMoveBTN, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(EIUndoBTN, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(EISaveBTN2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(75, Short.MAX_VALUE))
        );

        EI1PanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {EIAddBTN, EIRemoveBTN, EISaveBTN});

        EIWindow.getContentPane().add(EI1Panel);
        EI1Panel.setBounds(0, 0, 1610, 716);

        VCHWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        VCHWindow.setTitle("View Customer History");
        VCHWindow.setMinimumSize(new java.awt.Dimension(764, 731));
        VCHWindow.setResizable(false);

        VCHPanel.setBackground(new java.awt.Color(255, 255, 255));
        VCHPanel.setMinimumSize(new java.awt.Dimension(764, 731));
        VCHPanel.setLayout(null);

        VCHLogArea.setEditable(false);
        VCHLogArea.setColumns(20);
        VCHLogArea.setRows(5);
        jScrollPane3.setViewportView(VCHLogArea);

        VCHPanel.add(jScrollPane3);
        jScrollPane3.setBounds(225, 31, 492, 591);
        VCHPanel.add(VCHCustomerInput);
        VCHCustomerInput.setBounds(41, 58, 163, 32);

        VCHPanel.add(VCHComboBox);
        VCHComboBox.setBounds(41, 101, 163, 29);

        VCHCustomerLabel.setText("Customer:");
        VCHPanel.add(VCHCustomerLabel);
        VCHCustomerLabel.setBounds(41, 38, 135, 14);

        VCHPrintBTN.setText("PRINT");
        VCHPrintBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                VCHPrintBTNActionPerformed(evt);
            }
        });
        VCHPanel.add(VCHPrintBTN);
        VCHPrintBTN.setBounds(647, 633, 70, 30);

        VCHDetailsOutput.setColumns(20);
        VCHDetailsOutput.setRows(5);
        jScrollPane5.setViewportView(VCHDetailsOutput);

        VCHPanel.add(jScrollPane5);
        jScrollPane5.setBounds(41, 149, 166, 273);

        javax.swing.GroupLayout VCHWindowLayout = new javax.swing.GroupLayout(VCHWindow.getContentPane());
        VCHWindow.getContentPane().setLayout(VCHWindowLayout);
        VCHWindowLayout.setHorizontalGroup(
            VCHWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(VCHPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 764, Short.MAX_VALUE)
        );
        VCHWindowLayout.setVerticalGroup(
            VCHWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(VCHPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 731, Short.MAX_VALUE)
        );

        VCONHWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        VCONHWindow.setTitle("View Container History");
        VCONHWindow.setMinimumSize(new java.awt.Dimension(1109, 638));

        VCONHPanel.setBackground(new java.awt.Color(255, 255, 255));
        VCONHPanel.setMinimumSize(new java.awt.Dimension(1109, 638));

        VCONHTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Owner", "Action", "TimeStamp"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        VCONHTable.getTableHeader().setReorderingAllowed(false);
        VCONHScroll.setViewportView(VCONHTable);

        VCONHConLabel.setText("Container:");

        javax.swing.GroupLayout VCONHPanelLayout = new javax.swing.GroupLayout(VCONHPanel);
        VCONHPanel.setLayout(VCONHPanelLayout);
        VCONHPanelLayout.setHorizontalGroup(
            VCONHPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, VCONHPanelLayout.createSequentialGroup()
                .addContainerGap(42, Short.MAX_VALUE)
                .addGroup(VCONHPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(VCONHComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(VCONHConLabel)
                    .addComponent(VCONHScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 1031, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36))
        );
        VCONHPanelLayout.setVerticalGroup(
            VCONHPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VCONHPanelLayout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(VCONHScroll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(VCONHConLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(VCONHComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(90, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout VCONHWindowLayout = new javax.swing.GroupLayout(VCONHWindow.getContentPane());
        VCONHWindow.getContentPane().setLayout(VCONHWindowLayout);
        VCONHWindowLayout.setHorizontalGroup(
            VCONHWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(VCONHPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        VCONHWindowLayout.setVerticalGroup(
            VCONHWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(VCONHPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        VAHWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        VAHWindow.setTitle("View Action History");
        VAHWindow.setMinimumSize(new java.awt.Dimension(1488, 732));
        VAHWindow.setResizable(false);

        VAHPanel.setBackground(new java.awt.Color(255, 255, 255));
        VAHPanel.setMinimumSize(new java.awt.Dimension(1488, 732));
        VAHPanel.setLayout(null);

        VAHTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "User", "Customer", "Id", "Action", "Details", "TimeStamp"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        VAHScroll.setViewportView(VAHTable);

        VAHPanel.add(VAHScroll);
        VAHScroll.setBounds(64, 49, 1367, 462);

        VAHUserTog.setText("USER");
        VAHPanel.add(VAHUserTog);
        VAHUserTog.setBounds(64, 529, 104, 32);

        VAHActionTog.setText("ACTION");
        VAHPanel.add(VAHActionTog);
        VAHActionTog.setBounds(186, 529, 104, 32);

        VAHDateTog.setText("DATE");
        VAHPanel.add(VAHDateTog);
        VAHDateTog.setBounds(390, 530, 104, 32);

        VAHPanel.add(VAHUserCombo);
        VAHUserCombo.setBounds(64, 567, 104, 30);

        VAHPanel.add(VAHActionCombo);
        VAHActionCombo.setBounds(186, 567, 180, 30);

        VAHPrintBTN.setText("PRINT");
        VAHPrintBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                VAHPrintBTNActionPerformed(evt);
            }
        });
        VAHPanel.add(VAHPrintBTN);
        VAHPrintBTN.setBounds(1332, 529, 99, 32);

        VAHSearchBTN.setText("SEARCH");
        VAHSearchBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                VAHSearchBTNActionPerformed(evt);
            }
        });
        VAHPanel.add(VAHSearchBTN);
        VAHSearchBTN.setBounds(670, 530, 104, 32);

        VAHPanel.add(VAHDateCombo);
        VAHDateCombo.setBounds(390, 567, 180, 30);

        javax.swing.GroupLayout VAHWindowLayout = new javax.swing.GroupLayout(VAHWindow.getContentPane());
        VAHWindow.getContentPane().setLayout(VAHWindowLayout);
        VAHWindowLayout.setHorizontalGroup(
            VAHWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(VAHPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        VAHWindowLayout.setVerticalGroup(
            VAHWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(VAHPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        VAWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        VAWindow.setTitle("View Analytics");
        VAWindow.setMinimumSize(new java.awt.Dimension(886, 639));
        VAWindow.setResizable(false);

        VAPanel.setBackground(new java.awt.Color(255, 255, 255));
        VAPanel.setMinimumSize(new java.awt.Dimension(886, 639));
        VAPanel.setLayout(null);

        VALongestCustomerBTN.setText("Longest Customer");
        VALongestCustomerBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                VALongestCustomerBTNActionPerformed(evt);
            }
        });
        VAPanel.add(VALongestCustomerBTN);
        VALongestCustomerBTN.setBounds(686, 33, 155, 39);

        VAMostContainersBTN.setText("Most Containers");
        VAMostContainersBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                VAMostContainersBTNActionPerformed(evt);
            }
        });
        VAPanel.add(VAMostContainersBTN);
        VAMostContainersBTN.setBounds(686, 90, 155, 39);

        VAOldestContainerBTN.setText("Oldest Container");
        VAOldestContainerBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                VAOldestContainerBTNActionPerformed(evt);
            }
        });
        VAPanel.add(VAOldestContainerBTN);
        VAOldestContainerBTN.setBounds(686, 147, 155, 39);

        VAContainerAccoladesBTN.setText("ContainerAccolades");
        VAContainerAccoladesBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                VAContainerAccoladesBTNActionPerformed(evt);
            }
        });
        VAPanel.add(VAContainerAccoladesBTN);
        VAContainerAccoladesBTN.setBounds(686, 204, 155, 39);

        VAMostActiveUserrBTN.setText("Most Active User");
        VAMostActiveUserrBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                VAMostActiveUserrBTNActionPerformed(evt);
            }
        });
        VAPanel.add(VAMostActiveUserrBTN);
        VAMostActiveUserrBTN.setBounds(686, 261, 155, 39);

        VATable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        VAScroll.setViewportView(VATable);

        VAPanel.add(VAScroll);
        VAScroll.setBounds(60, 30, 590, 520);

        javax.swing.GroupLayout VAWindowLayout = new javax.swing.GroupLayout(VAWindow.getContentPane());
        VAWindow.getContentPane().setLayout(VAWindowLayout);
        VAWindowLayout.setHorizontalGroup(
            VAWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(VAPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 886, Short.MAX_VALUE)
        );
        VAWindowLayout.setVerticalGroup(
            VAWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(VAPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 639, Short.MAX_VALUE)
        );

        FTLIWindow.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        FTLIWindow.setTitle("First Time Log In");
        FTLIWindow.setMinimumSize(new java.awt.Dimension(508, 408));
        FTLIWindow.setResizable(false);

        FTLIPanel.setBackground(new java.awt.Color(255, 255, 255));
        FTLIPanel.setMinimumSize(new java.awt.Dimension(508, 408));
        FTLIPanel.setLayout(null);

        FTLIUsernameInput.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        FTLIPanel.add(FTLIUsernameInput);
        FTLIUsernameInput.setBounds(154, 89, 156, 39);

        FTLIPasswordInput.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        FTLIPanel.add(FTLIPasswordInput);
        FTLIPasswordInput.setBounds(154, 164, 156, 39);

        FTLIRePasswordInput.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        FTLIPanel.add(FTLIRePasswordInput);
        FTLIRePasswordInput.setBounds(154, 234, 156, 39);

        FTLIUserNameLabel.setText("Username:");
        FTLIPanel.add(FTLIUserNameLabel);
        FTLIUserNameLabel.setBounds(205, 64, 64, 14);

        FTLIPasswordLabel.setText("Password:");
        FTLIPanel.add(FTLIPasswordLabel);
        FTLIPasswordLabel.setBounds(206, 139, 80, 14);

        FTLIRePasswordLabel.setText("Re-Enter Password");
        FTLIPanel.add(FTLIRePasswordLabel);
        FTLIRePasswordLabel.setBounds(189, 214, 109, 14);

        FTLISubmitBTN.setText("SUBMIT");
        FTLISubmitBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FTLISubmitBTNActionPerformed(evt);
            }
        });
        FTLIPanel.add(FTLISubmitBTN);
        FTLISubmitBTN.setBounds(344, 291, 90, 37);

        javax.swing.GroupLayout FTLIWindowLayout = new javax.swing.GroupLayout(FTLIWindow.getContentPane());
        FTLIWindow.getContentPane().setLayout(FTLIWindowLayout);
        FTLIWindowLayout.setHorizontalGroup(
            FTLIWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(FTLIPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
        );
        FTLIWindowLayout.setVerticalGroup(
            FTLIWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(FTLIPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
        );

        LIWindow.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        LIWindow.setTitle("Log In");
        LIWindow.setMinimumSize(new java.awt.Dimension(400, 340));
        LIWindow.setResizable(false);

        LIPanel.setBackground(new java.awt.Color(255, 255, 255));
        LIPanel.setMinimumSize(new java.awt.Dimension(400, 340));

        LIUserNameInput.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        LILogInBTN.setText("LOG IN");
        LILogInBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LILogInBTNActionPerformed(evt);
            }
        });

        LIusernameLabel.setText("Username:");

        LIPasswordLabel.setText("Password:");

        LIPasswordInput.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        javax.swing.GroupLayout LIPanelLayout = new javax.swing.GroupLayout(LIPanel);
        LIPanel.setLayout(LIPanelLayout);
        LIPanelLayout.setHorizontalGroup(
            LIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LIPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(LILogInBTN)
                .addGap(39, 39, 39))
            .addGroup(LIPanelLayout.createSequentialGroup()
                .addGroup(LIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(LIPanelLayout.createSequentialGroup()
                        .addGap(177, 177, 177)
                        .addComponent(LIusernameLabel))
                    .addGroup(LIPanelLayout.createSequentialGroup()
                        .addGap(177, 177, 177)
                        .addComponent(LIPasswordLabel))
                    .addGroup(LIPanelLayout.createSequentialGroup()
                        .addGap(120, 120, 120)
                        .addGroup(LIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(LIUserNameInput)
                            .addComponent(LIPasswordInput, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE))))
                .addContainerGap(124, Short.MAX_VALUE))
        );
        LIPanelLayout.setVerticalGroup(
            LIPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LIPanelLayout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addComponent(LIusernameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(LIUserNameInput, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(LIPasswordLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(LIPasswordInput, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21)
                .addComponent(LILogInBTN)
                .addContainerGap(82, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout LIWindowLayout = new javax.swing.GroupLayout(LIWindow.getContentPane());
        LIWindow.getContentPane().setLayout(LIWindowLayout);
        LIWindowLayout.setHorizontalGroup(
            LIWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(LIPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        LIWindowLayout.setVerticalGroup(
            LIWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(LIPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        RUWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        RUWindow.setTitle("Remove User");
        RUWindow.setMinimumSize(new java.awt.Dimension(526, 268));
        RUWindow.setResizable(false);

        RUPanel.setBackground(new java.awt.Color(255, 255, 255));
        RUPanel.setMinimumSize(new java.awt.Dimension(526, 268));

        RUPasswordInput.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        RURemoveBTN.setText("REMOVE");
        RURemoveBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RURemoveBTNActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout RUPanelLayout = new javax.swing.GroupLayout(RUPanel);
        RUPanel.setLayout(RUPanelLayout);
        RUPanelLayout.setHorizontalGroup(
            RUPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RUPanelLayout.createSequentialGroup()
                .addGap(52, 52, 52)
                .addComponent(RUUserComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(RUPasswordInput, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(RURemoveBTN, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(48, Short.MAX_VALUE))
        );
        RUPanelLayout.setVerticalGroup(
            RUPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RUPanelLayout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(RUPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(RURemoveBTN, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                    .addComponent(RUPasswordInput)
                    .addComponent(RUUserComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE))
                .addContainerGap(191, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout RUWindowLayout = new javax.swing.GroupLayout(RUWindow.getContentPane());
        RUWindow.getContentPane().setLayout(RUWindowLayout);
        RUWindowLayout.setHorizontalGroup(
            RUWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(RUPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        RUWindowLayout.setVerticalGroup(
            RUWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(RUPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        CPWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        CPWindow.setTitle("Change Password");
        CPWindow.setMinimumSize(new java.awt.Dimension(462, 428));
        CPWindow.setResizable(false);

        CPPanel.setBackground(new java.awt.Color(255, 255, 255));
        CPPanel.setMinimumSize(new java.awt.Dimension(462, 428));
        CPPanel.setLayout(null);

        CPUsernameInput.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        CPPanel.add(CPUsernameInput);
        CPUsernameInput.setBounds(130, 80, 205, 40);

        CPPasswordInput.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        CPPanel.add(CPPasswordInput);
        CPPasswordInput.setBounds(130, 170, 205, 41);

        CPRePasswordInput.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        CPPanel.add(CPRePasswordInput);
        CPRePasswordInput.setBounds(130, 260, 205, 44);

        CPUsernameLabel.setText("Username:");
        CPPanel.add(CPUsernameLabel);
        CPUsernameLabel.setBounds(210, 60, 113, 14);

        CPPasswordLabel.setText("Password:");
        CPPanel.add(CPPasswordLabel);
        CPPasswordLabel.setBounds(210, 150, 113, 14);

        CPRePasswordLabel.setText("Re-Enter Password:");
        CPPanel.add(CPRePasswordLabel);
        CPRePasswordLabel.setBounds(180, 240, 113, 14);

        CPChangeBTN.setText("CHANGE");
        CPChangeBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CPChangeBTNActionPerformed(evt);
            }
        });
        CPPanel.add(CPChangeBTN);
        CPChangeBTN.setBounds(130, 330, 200, 34);

        changePasswordLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        changePasswordLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        changePasswordLabel.setText("CHANGE PASSWORD");
        CPPanel.add(changePasswordLabel);
        changePasswordLabel.setBounds(140, 10, 200, 30);

        javax.swing.GroupLayout CPWindowLayout = new javax.swing.GroupLayout(CPWindow.getContentPane());
        CPWindow.getContentPane().setLayout(CPWindowLayout);
        CPWindowLayout.setHorizontalGroup(
            CPWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(CPPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)
        );
        CPWindowLayout.setVerticalGroup(
            CPWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(CPPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
        );

        RCONWindow.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        RCONWindow.setTitle("Remove Container");
        RCONWindow.setMinimumSize(new java.awt.Dimension(400, 300));
        RCONWindow.setResizable(false);

        RCONPanel.setBackground(new java.awt.Color(255, 255, 255));
        RCONPanel.setMinimumSize(new java.awt.Dimension(400, 300));

        RCONContainerInput.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        RCONContainerInput.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        RCONRemoveBTN.setText("REMOVE");
        RCONRemoveBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RCONRemoveBTNActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout RCONPanelLayout = new javax.swing.GroupLayout(RCONPanel);
        RCONPanel.setLayout(RCONPanelLayout);
        RCONPanelLayout.setHorizontalGroup(
            RCONPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RCONPanelLayout.createSequentialGroup()
                .addGap(128, 128, 128)
                .addGroup(RCONPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(RCONRemoveBTN, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                    .addComponent(RCONContainerInput))
                .addContainerGap(138, Short.MAX_VALUE))
        );
        RCONPanelLayout.setVerticalGroup(
            RCONPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RCONPanelLayout.createSequentialGroup()
                .addGap(68, 68, 68)
                .addComponent(RCONContainerInput, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(RCONRemoveBTN, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(129, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout RCONWindowLayout = new javax.swing.GroupLayout(RCONWindow.getContentPane());
        RCONWindow.getContentPane().setLayout(RCONWindowLayout);
        RCONWindowLayout.setHorizontalGroup(
            RCONWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(RCONPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        RCONWindowLayout.setVerticalGroup(
            RCONWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(RCONPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        WMWindow.setMinimumSize(new java.awt.Dimension(589, 697));
        WMWindow.setResizable(false);

        WMPanel.setBackground(new java.awt.Color(255, 255, 255));
        WMPanel.setMinimumSize(new java.awt.Dimension(589, 697));
        WMPanel.setLayout(null);
        WMPanel.add(WMConFromInput);
        WMConFromInput.setBounds(70, 80, 50, 30);

        WMConTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Container Numbers"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        WMConTable.getTableHeader().setReorderingAllowed(false);
        WMConScroll.setViewportView(WMConTable);
        if (WMConTable.getColumnModel().getColumnCount() > 0) {
            WMConTable.getColumnModel().getColumn(0).setResizable(false);
        }

        WMPanel.add(WMConScroll);
        WMConScroll.setBounds(70, 150, 138, 360);

        WMGenerateConBTN.setText("Generate");
        WMGenerateConBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WMGenerateConBTNActionPerformed(evt);
            }
        });
        WMPanel.add(WMGenerateConBTN);
        WMGenerateConBTN.setBounds(70, 120, 138, 23);

        WMLocationsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Locations"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        WMLocationsTable.getTableHeader().setReorderingAllowed(false);
        WMLocScroll.setViewportView(WMLocationsTable);
        if (WMLocationsTable.getColumnModel().getColumnCount() > 0) {
            WMLocationsTable.getColumnModel().getColumn(0).setResizable(false);
        }

        WMPanel.add(WMLocScroll);
        WMLocScroll.setBounds(370, 190, 160, 320);
        WMPanel.add(WMRowsInput);
        WMRowsInput.setBounds(370, 90, 48, 29);
        WMPanel.add(WMHeightInput);
        WMHeightInput.setBounds(480, 90, 49, 32);
        WMPanel.add(WMConToInput);
        WMConToInput.setBounds(160, 80, 50, 30);

        WMToLabel.setText("    To");
        WMPanel.add(WMToLabel);
        WMToLabel.setBounds(120, 80, 40, 30);

        WMRowsLabel.setText("Rows:");
        WMPanel.add(WMRowsLabel);
        WMRowsLabel.setBounds(320, 100, 40, 14);

        WMHeightLabel.setText("Height:");
        WMPanel.add(WMHeightLabel);
        WMHeightLabel.setBounds(440, 100, 40, 14);

        WMLocationGenBTN.setText("Generate");
        WMLocationGenBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WMLocationGenBTNActionPerformed(evt);
            }
        });
        WMPanel.add(WMLocationGenBTN);
        WMLocationGenBTN.setBounds(430, 140, 100, 30);
        WMPanel.add(WMColumnsInput);
        WMColumnsInput.setBounds(370, 140, 50, 30);

        WMColumnsLabel.setText("Columns:");
        WMPanel.add(WMColumnsLabel);
        WMColumnsLabel.setBounds(310, 150, 60, 14);

        WMCompleteBTN.setText("COMPLETE");
        WMCompleteBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WMCompleteBTNActionPerformed(evt);
            }
        });
        WMPanel.add(WMCompleteBTN);
        WMCompleteBTN.setBounds(370, 540, 160, 23);

        WMContainerGenLabel.setText("Container Generator");
        WMPanel.add(WMContainerGenLabel);
        WMContainerGenLabel.setBounds(90, 10, 130, 20);

        WMLocationGenLabel.setText("Location Generator");
        WMPanel.add(WMLocationGenLabel);
        WMLocationGenLabel.setBounds(400, 10, 110, 20);
        WMPanel.add(WMLocProgressBar);
        WMLocProgressBar.setBounds(370, 520, 160, 14);
        WMPanel.add(WMConProgressBar);
        WMConProgressBar.setBounds(70, 520, 140, 14);

        MWAddToExistingRadio.setText("Add To Existing");
        WMPanel.add(MWAddToExistingRadio);
        MWAddToExistingRadio.setBounds(370, 50, 140, 23);

        javax.swing.GroupLayout WMWindowLayout = new javax.swing.GroupLayout(WMWindow.getContentPane());
        WMWindow.getContentPane().setLayout(WMWindowLayout);
        WMWindowLayout.setHorizontalGroup(
            WMWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(WMPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
        );
        WMWindowLayout.setVerticalGroup(
            WMWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(WMPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 697, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Container Store");
        setMinimumSize(new java.awt.Dimension(1478, 931));
        setResizable(false);

        MainPanel.setBackground(new java.awt.Color(255, 255, 255));
        MainPanel.setMaximumSize(null);
        MainPanel.setMinimumSize(new java.awt.Dimension(1478, 931));
        MainPanel.setPreferredSize(new java.awt.Dimension(1478, 931));
        MainPanel.setLayout(null);

        MainLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ContainerStore/dennis-removals-logo.png"))); // NOI18N
        MainLogo.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        MainPanel.add(MainLogo);
        MainLogo.setBounds(50, 20, 190, 133);

        SideMainBTNPanel.setBackground(new java.awt.Color(81, 184, 255));
        SideMainBTNPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        SideMainBTNPanel.setMaximumSize(null);
        SideMainBTNPanel.setMinimumSize(new java.awt.Dimension(261, 631));
        SideMainBTNPanel.setLayout(null);

        addNewCustomerBTN.setText("Add New Customer");
        addNewCustomerBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewCustomerBTNActionPerformed(evt);
            }
        });
        SideMainBTNPanel.add(addNewCustomerBTN);
        addNewCustomerBTN.setBounds(20, 20, 235, 23);

        editCustomerDetailsBTN.setText("Edit Customer Details");
        editCustomerDetailsBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editCustomerDetailsBTNActionPerformed(evt);
            }
        });
        SideMainBTNPanel.add(editCustomerDetailsBTN);
        editCustomerDetailsBTN.setBounds(20, 50, 235, 23);

        searchCustomerBTN.setText("Search Customer");
        searchCustomerBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchCustomerBTNActionPerformed(evt);
            }
        });
        SideMainBTNPanel.add(searchCustomerBTN);
        searchCustomerBTN.setBounds(20, 80, 235, 23);

        addNewContainerBTN.setText("Add New Container");
        addNewContainerBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewContainerBTNActionPerformed(evt);
            }
        });
        SideMainBTNPanel.add(addNewContainerBTN);
        addNewContainerBTN.setBounds(20, 160, 235, 23);

        moveContainersBTN.setText("Move Containers");
        moveContainersBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveContainersBTNActionPerformed(evt);
            }
        });
        SideMainBTNPanel.add(moveContainersBTN);
        moveContainersBTN.setBounds(20, 190, 235, 23);

        searchContainerBTN.setText("Search Container");
        searchContainerBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchContainerBTNActionPerformed(evt);
            }
        });
        SideMainBTNPanel.add(searchContainerBTN);
        searchContainerBTN.setBounds(20, 220, 235, 23);

        emptyContainerBTN.setText("Empty Container");
        emptyContainerBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emptyContainerBTNActionPerformed(evt);
            }
        });
        SideMainBTNPanel.add(emptyContainerBTN);
        emptyContainerBTN.setBounds(20, 250, 235, 23);

        createNewInventoryBTN.setText("Create New Inventory");
        createNewInventoryBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createNewInventoryBTNActionPerformed(evt);
            }
        });
        SideMainBTNPanel.add(createNewInventoryBTN);
        createNewInventoryBTN.setBounds(20, 340, 235, 23);

        searchInventoryBTN.setText("Search Inventory");
        searchInventoryBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchInventoryBTNActionPerformed(evt);
            }
        });
        SideMainBTNPanel.add(searchInventoryBTN);
        searchInventoryBTN.setBounds(20, 370, 235, 23);

        editInventoryBTN.setText("Edit Inventory");
        editInventoryBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editInventoryBTNActionPerformed(evt);
            }
        });
        SideMainBTNPanel.add(editInventoryBTN);
        editInventoryBTN.setBounds(20, 400, 235, 23);

        viewCustomerHistoryBTN.setText("View Customer History");
        viewCustomerHistoryBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewCustomerHistoryBTNActionPerformed(evt);
            }
        });
        SideMainBTNPanel.add(viewCustomerHistoryBTN);
        viewCustomerHistoryBTN.setBounds(20, 460, 235, 23);

        viewContainerHistoryBTN.setText("View Container History");
        viewContainerHistoryBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewContainerHistoryBTNActionPerformed(evt);
            }
        });
        SideMainBTNPanel.add(viewContainerHistoryBTN);
        viewContainerHistoryBTN.setBounds(20, 490, 235, 23);

        viewActivityHistoryBTN.setText("View Activity History");
        viewActivityHistoryBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewActivityHistoryBTNActionPerformed(evt);
            }
        });
        SideMainBTNPanel.add(viewActivityHistoryBTN);
        viewActivityHistoryBTN.setBounds(20, 520, 235, 23);

        viewAnalyticsBTN.setText("View Analytics");
        viewAnalyticsBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewAnalyticsBTNActionPerformed(evt);
            }
        });
        SideMainBTNPanel.add(viewAnalyticsBTN);
        viewAnalyticsBTN.setBounds(20, 550, 235, 23);

        createPickListBTN.setText("Create PickList");
        createPickListBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createPickListBTNActionPerformed(evt);
            }
        });
        SideMainBTNPanel.add(createPickListBTN);
        createPickListBTN.setBounds(20, 280, 235, 23);

        removeCustomerBTN.setText("Remove Customer");
        removeCustomerBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeCustomerBTNActionPerformed(evt);
            }
        });
        SideMainBTNPanel.add(removeCustomerBTN);
        removeCustomerBTN.setBounds(20, 110, 235, 23);

        MainPanel.add(SideMainBTNPanel);
        SideMainBTNPanel.setBounds(10, 170, 270, 650);

        TopMainBTNPanel.setBackground(new java.awt.Color(81, 184, 255));

        addNewUserBTN.setText("Add New User");
        addNewUserBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewUserBTNActionPerformed(evt);
            }
        });

        removeUserBTN.setText("Remove User");
        removeUserBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeUserBTNActionPerformed(evt);
            }
        });

        changePasswordsBTN.setText("Change Passwords");
        changePasswordsBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changePasswordsBTNActionPerformed(evt);
            }
        });

        removeContainerBTN.setText("Remove Container");
        removeContainerBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeContainerBTNActionPerformed(evt);
            }
        });

        manageWarehouseBTN.setText("Manage Warehouse");
        manageWarehouseBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageWarehouseBTNActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout TopMainBTNPanelLayout = new javax.swing.GroupLayout(TopMainBTNPanel);
        TopMainBTNPanel.setLayout(TopMainBTNPanelLayout);
        TopMainBTNPanelLayout.setHorizontalGroup(
            TopMainBTNPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopMainBTNPanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(addNewUserBTN, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(removeUserBTN, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(changePasswordsBTN, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(removeContainerBTN, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(manageWarehouseBTN, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(108, Short.MAX_VALUE))
        );

        TopMainBTNPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {manageWarehouseBTN, removeContainerBTN});

        TopMainBTNPanelLayout.setVerticalGroup(
            TopMainBTNPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopMainBTNPanelLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(TopMainBTNPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addNewUserBTN)
                    .addGroup(TopMainBTNPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(removeUserBTN)
                        .addComponent(changePasswordsBTN)
                        .addComponent(removeContainerBTN)
                        .addComponent(manageWarehouseBTN)))
                .addGap(23, 23, 23))
        );

        TopMainBTNPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {manageWarehouseBTN, removeContainerBTN});

        MainPanel.add(TopMainBTNPanel);
        TopMainBTNPanel.setBounds(271, 19, 1100, 50);

        mainCustomersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Title", "First Name", "Last Name", "Telephone", "E-Mail", "Address"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        mainCustomersTable.getTableHeader().setReorderingAllowed(false);
        mainCustomersScroll.setViewportView(mainCustomersTable);

        MainPanel.add(mainCustomersScroll);
        mainCustomersScroll.setBounds(289, 200, 941, 375);

        mainInventoryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "#", "Item", "Descrption", "Condition", "Photos"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        mainInventoryTable.getTableHeader().setReorderingAllowed(false);
        mainInventoryScroll.setViewportView(mainInventoryTable);

        MainPanel.add(mainInventoryScroll);
        mainInventoryScroll.setBounds(289, 613, 1084, 200);

        mainCustomersTableLable.setText("Customers:");
        MainPanel.add(mainCustomersTableLable);
        mainCustomersTableLable.setBounds(289, 180, 80, 14);

        mainInventoryTableLabel.setText("Inventory:");
        MainPanel.add(mainInventoryTableLabel);
        mainInventoryTableLabel.setBounds(289, 593, 52, 14);

        mainContainersLabel.setText("Containers:");
        MainPanel.add(mainContainersLabel);
        mainContainersLabel.setBounds(1248, 180, 80, 14);

        mainContainersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Number", "Location"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        mainContainersTable.getTableHeader().setReorderingAllowed(false);
        mainContainersScroll.setViewportView(mainContainersTable);
        if (mainContainersTable.getColumnModel().getColumnCount() > 0) {
            mainContainersTable.getColumnModel().getColumn(0).setResizable(false);
            mainContainersTable.getColumnModel().getColumn(1).setResizable(false);
        }

        MainPanel.add(mainContainersScroll);
        mainContainersScroll.setBounds(1248, 200, 125, 375);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(MainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(MainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    /**
    * opening the add new customer window and add listeners to location field and combo box
    * @param evt 
    */   
    
    private void addNewCustomerBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewCustomerBTNActionPerformed

        ANCWindow.setVisible(true);
        ANCWindow.setLocationRelativeTo(null);
        
        ANCLocationField.addKeyListener(new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {
                
            }

            @Override
            public void keyPressed(KeyEvent e) {
                
            }

            @Override
            public void keyReleased(KeyEvent e) {
                
                ANCLocComboBox.removeAllItems();                     //clears combo box for new find
                
                String preFix = ANCLocationField.getText();
                
                ArrayList<String> found = wh.getLocationsWithPrefix(preFix);        //gets all free locations and populates combo box
                
                for(String f : found){
                    
                    ANCLocComboBox.addItem(f);
                }
                
                preFix = "";
            }
        
        
        });
        
        ANCLocComboBox.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e) {
                
                if(e.getStateChange() == ItemEvent.SELECTED){
                    
                    ANCLocationField.setText(String.valueOf(e.getItem()));         //sets location filed to chosen location
                }
            }   
        });       
        
        
        ANCLocationField.addFocusListener(new java.awt.event.FocusAdapter() {      //focus listener to select all text when clicked as item listerner disrupts deletion
            public void focusGained(java.awt.event.FocusEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ANCLocationField.selectAll();
                    }
                });
            }
        });                
    }//GEN-LAST:event_addNewCustomerBTNActionPerformed

    /**
    * clears the customer fields in the add new customer window
    * @param evt 
    */   
    
    private void ANCClearBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ANCClearBTNActionPerformed
        
        int choice = JOptionPane.showConfirmDialog(null, "Are You Sure You Want To Clear The Customer Details?" , "Clear Details", JOptionPane.YES_NO_OPTION);
        
        if(choice == JOptionPane.YES_OPTION) {
        
            ANCTitleComboBox.setSelectedItem("");
            ANCFirstNameField.setText("");
            ANCLastNameField.setText("");
            ANCTelephoneField.setText("");
            ANCEmailField.setText("");
            ANCAddressField.setText("");
        }       
    }//GEN-LAST:event_ANCClearBTNActionPerformed

    /**
    * adds a container and location into the ANCcnLocTable
    * @param evt 
    */   
    
    private void ANCAddBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ANCAddBTNActionPerformed
        
        DefaultTableModel tm = (DefaultTableModel)ANCConLocTable.getModel();
        
        try{
            if(!ANCContainerField.getText().isBlank() && !ANCLocationField.getText().isBlank()){   //checks fields are not blank

                container = Integer.parseInt(ANCContainerField.getText().trim());
                location = ANCLocationField.getText().trim();
                
                if(wh.checkContainer(container) != true){      //checks the container isnt already being used                                           
                    
                    JOptionPane.showMessageDialog(null, "Container: '" + container + "' Is already In Use By \n '" + cs.getCustomerNameAndIdByContainer(container));
                    
                }else if(wh.checkLocation(location) != true){      //checks the location isnt already being used 
                        
                        JOptionPane.showMessageDialog(null, "Location Is already In Use By \n '" + cs.getCustomerNameAndIdByContainer(container) + "\n" + " Container: " + wh.getContainerByLocation(location));
                        
                    }else if(checkTable(container, location, ANCConLocTable) != true){     //checks if the container or location has already been entered in the table
                        
                            tm.addRow(new Object[]{container, location});         //all things checked it adds to the table

                            ANCContainerField.setText("");              //resets fields
                            ANCLocationField.setText("");
                            ANCLocComboBox.removeAllItems();
                        }
                                             
            }else{

                JOptionPane.showMessageDialog(null, "Both Container And Location Fields Must Completed!");
            }
        }catch(NumberFormatException ex){
            
            JOptionPane.showMessageDialog(null, "Container Field Can Only Contain Numbers!");
        }
    }//GEN-LAST:event_ANCAddBTNActionPerformed

    /**
    * removes a container and location either the last or selected
    * @param evt 
    */ 
    
    private void ANCRemoveBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ANCRemoveBTNActionPerformed
        
        DefaultTableModel tm = (DefaultTableModel)ANCConLocTable.getModel();
        
        int lastRow = ANCConLocTable.getRowCount() -1;
        int selected = ANCConLocTable.getSelectedRow();
        
        if(selected != -1){
            
            tm.removeRow(selected);
            
        }else{
            
            tm.removeRow(lastRow);
        }    
    }//GEN-LAST:event_ANCRemoveBTNActionPerformed

    /**
    * saves customer details, containers and locations and opens up the inventory window
    * @param evt 
    */ 
    
    private void ANCNextBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ANCNextBTNActionPerformed
        
        DefaultTableModel tm = (DefaultTableModel)ANCConLocTable.getModel();
        
        int rowCount = ANCConLocTable.getRowCount();
        containers = new int[rowCount];
        locations = new String[rowCount];
        
        if(rowCount > 0){           //checks if containers have been entered
        
            for(int i = 0; i < rowCount; i++){          //adds the containers and locations to array

                containers[i] = (int) tm.getValueAt(i, 0);
                locations[i] = String.valueOf(tm.getValueAt(i, 1));           
            }  

            title = String.valueOf(ANCTitleComboBox.getSelectedItem());         //sets the variables
            firstName = ANCFirstNameField.getText().substring(0, 1).toUpperCase() + ANCFirstNameField.getText().substring(1).trim();         ///capitalizes
            lastName = ANCLastNameField.getText().substring(0, 1).toUpperCase() + ANCLastNameField.getText().substring(1).trim();           ///capitalizes
            eMail = ANCEmailField.getText().trim();
            address = ANCAddressField.getText();
            telephone = ANCTelephoneField.getText();

            if(String.valueOf(ANCTitleComboBox.getSelectedItem()).isBlank() || firstName.isBlank() || lastName.isBlank() || title == null || eMail.isBlank() || address.isBlank() || telephone.isBlank()){           //checks the variables

                JOptionPane.showMessageDialog(null, "All Fields Must Be Completed!");

            }else{

                ID = cs.createID();         //creates the Id
                 
                cs.addNewCustomer(ID, title, firstName, lastName, telephone, eMail, address, rowCount, dateStamp());          //Enters the customer into the database
                
                String status = "ENTERED";              //variable assigning status as entered 
                
                wh.assignContainers(ID, containers, locations, status, false);            //updates the containers, locations and status into the database
                
                String conGroup = "Containers: ";
                
                for(int j = 0; j < containers.length; j++){             //putting the containes into one string for joptionpane output
                
                    String conStr = String.valueOf(containers[j] + ", ");
                
                    conGroup += conStr;
                }
                
                String fullName = title + " " + firstName + " " + lastName; 
            
                JOptionPane.showMessageDialog(null, conGroup + " Assigned To " + fullName);
                
                
                CNIWindow.setVisible(true);             //opens the create new inventory window
                CNIWindow.setLocationRelativeTo(null);

                for(int i = 0 ; i < containers.length; i++){            //populates the combo box

                    CNIContainerComboBox.addItem(String.valueOf(containers[i]));

                }
                            
                CNIIdOutput.setText(String.valueOf(ID));             //populates fields
                CNINameOutput.setText(fullName);
                CNITelephoneOutput.setText(String.valueOf(telephone));
                CNIEmailOutput.setText(eMail);
                CNIAddressOutput.setText(address);
                
                
                ANCTitleComboBox.setSelectedItem("");   //clears the fields in add new customer
                ANCFirstNameField.setText("");
                ANCLastNameField.setText("");
                ANCTelephoneField.setText("");
                ANCEmailField.setText("");
                ANCAddressField.setText("");
                
                tm.setRowCount(0);                      //clears the add new customer container and location table
                
                containers = new int[0];                //resets the container and locations arrays
                locations = new String[0];
                
                clearVariables();           //clears global variables
                
                ANCWindow.dispose();                //disposes of add new customer window
                
                populateMainTable();            //repopulates main customer table
            }   
        }else{
            
            JOptionPane.showMessageDialog(null, "Please Enter Containers");             //all will fail if no containers added
        }
    }//GEN-LAST:event_ANCNextBTNActionPerformed

    /**
    * combo box setting the chosen container label to the selected container
    * @param evt 
    */
    
    private void CNIContainerComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CNIContainerComboBoxActionPerformed
        
        String con = String.valueOf(CNIContainerComboBox.getSelectedItem()); 
        
        if(con != null){

            CNIChosenContainerLabel.setText(con);
        }       
    }//GEN-LAST:event_CNIContainerComboBoxActionPerformed

    /**
    * adds new row into the inventory table
    * @param evt 
    */   
    
    private void CNIAddNewItemBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CNIAddNewItemBTNActionPerformed
        
        DefaultTableModel tm = (DefaultTableModel)CNIInventoryTable.getModel();
        
        if(CNIContainerComboBox.getSelectedItem() != null) {            //only to add new row if container present in combo box
        
            tm.addRow(new Object[]{"","","","",""});
            
        }
    }//GEN-LAST:event_CNIAddNewItemBTNActionPerformed

    /**
    * removes the selected row in the inventory table
    * @param evt 
    */    
    
    private void CNIRemoveItemBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CNIRemoveItemBTNActionPerformed
        
        DefaultTableModel tm = (DefaultTableModel)CNIInventoryTable.getModel();
        
        int selected  = CNIInventoryTable.getSelectedRow();
       
        if(CNIContainerComboBox.getSelectedItem() != null) { 
            
            if(selected == -1){

                JOptionPane.showMessageDialog(null, "Please selcted Item To Remove");

            }else{

                tm.removeRow(selected);
            }
        }
    }//GEN-LAST:event_CNIRemoveItemBTNActionPerformed

    /**
    * saves the completed inventory from the inventory table
    * @param evt 
    */  
    
    private void CNISaveBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CNISaveBTNActionPerformed
        
        DefaultTableModel tm = (DefaultTableModel)CNIInventoryTable.getModel();
  
        int rowCount = CNIInventoryTable.getRowCount();
        int rowConcern = 0;
        
        if(CNIContainerComboBox.getItemCount() != 0 && rowCount > 0) { 
              
            ID  = Integer.valueOf(CNIIdOutput.getText());
            
            container = Integer.valueOf(CNIContainerComboBox.getSelectedItem().toString());

            int[] numOfItems = new int[rowCount];               
            String[] items = new String[rowCount];
            String[] descriptions = new String[rowCount];
            String[] conditions = new String[rowCount];
            String[] photofiles = new String[rowCount];

            for(int i = 0; i < rowCount; i++){              //inventory details get stored to arrays

                try{
                    
                    numOfItems[i] = Integer.parseInt(tm.getValueAt(i, 0).toString().trim());
                    items[i] = String.valueOf(tm.getValueAt(i, 1));
                    descriptions[i] = String.valueOf(tm.getValueAt(i, 2));
                    conditions[i] = String.valueOf(tm.getValueAt(i, 3));
                    photofiles[i] = String.valueOf(tm.getValueAt(i, 4));
                    
                    if(items[i].isBlank() || descriptions[i].isBlank() || conditions[i].isBlank()){
                        
                        throw new Exception();
                    }
                             
                }catch(Exception e){
                    
                    rowConcern++;           //collects number of rows with issues
                }
            }
            
            if (rowConcern > 0){            //checks to see if all rows have been completed
                
                JOptionPane.showMessageDialog(null, "'" + rowConcern + "' Row(s) Need To Be Fully Completed, Or Removed.");
                
            }else if(tm.getRowCount() == 0){
                                           
                CNIContainerComboBox.removeItem(CNIContainerComboBox.getSelectedItem());        //saved container gets removed from list and table gets emptied
                
                tm.setRowCount(0);                  //deletes all rows from table
                
            }else{
                
                String status = "Full";
                
                wh.setContainerStatus(container, status);            //updates the container status to show inventory complete
                
                in.addToInventory(ID, container, numOfItems, items, descriptions, conditions, photofiles);      //adds items to inventory
                
                                 
                String[][] logAction = new String[tm.getRowCount()][tm.getColumnCount()];       //collects inventory into multiarray to write to inventory text file
                
                for(int i = tm.getRowCount() -1; i >= 0; i--){

                    for(int j = tm.getColumnCount() -1; j >= 0; j--){
                        
                        logAction[i][j] = String.valueOf(tm.getValueAt(i, j));
                    }
                }
                
                log.logInventoryConTxt(ID, container, logAction);           //logs inventory into container text file
                
                CNIContainerComboBox.removeItem(CNIContainerComboBox.getSelectedItem());        //saved container gets removed from list and table gets emptied
                
                tm.setRowCount(0);                  //deletes all rows from table
                
                if(CNIContainerComboBox.getItemCount() == 0) { 
            
                    CNIIdOutput.setText("");             //resets fields
                    CNINameOutput.setText("");  
                    CNITelephoneOutput.setText("");  
                    CNIEmailOutput.setText("");  
                    CNIAddressOutput.setText("");
                    CNIChosenContainerLabel.setText("");
            
                }
            }
                
        }else{
             
            if(CNIContainerComboBox.getSelectedItem() == null){
                
                //blank to not do anything if all containers are completed
                 
            }else {
                 
                 if(JOptionPane.showConfirmDialog(null, "No Items Have Been Added To The Inventory" + "\n" + "Are You Sure You Want To Contiune?", "Warning",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
                
                    CNIContainerComboBox.removeItem(CNIContainerComboBox.getSelectedItem());            // to save inventory with no items
                
                    if(CNIContainerComboBox.getItemCount() == 0) { 
            
                    CNIIdOutput.setText("");             //resets fields
                    CNINameOutput.setText("");  
                    CNITelephoneOutput.setText("");  
                    CNIEmailOutput.setText("");  
                    CNIAddressOutput.setText("");
                    CNIChosenContainerLabel.setText("");
            
                    }                
                }
            }
        }
    }//GEN-LAST:event_CNISaveBTNActionPerformed

    /**
    * finishes and closes the create new inventory window
    * @param evt 
    */
    
    private void CNIFinishBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CNIFinishBTNActionPerformed
        
       if(CNIContainerComboBox.getItemCount() == 0) { 
            
            CNIIdOutput.setText("");             //resets fields
            CNINameOutput.setText("");  
            CNITelephoneOutput.setText("");  
            CNIEmailOutput.setText("");  
            CNIAddressOutput.setText("");
            CNIChosenContainerLabel.setText("");
                 
            CNIWindow.dispose();
            
            findUnInventoried();
            
        }else{
            
            JOptionPane.showMessageDialog(null, "Please Save All Inventories");
        }
    }//GEN-LAST:event_CNIFinishBTNActionPerformed

    /**
    * opens the edit customer window
    * @param evt 
    */
    
    private void editCustomerDetailsBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editCustomerDetailsBTNActionPerformed
        
        ECDWindow.setVisible(true);
        ECDWindow.setLocationRelativeTo(null);
        
        ECDLastNameInput.addKeyListener(new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {
               
            }

            @Override
            public void keyPressed(KeyEvent e) {
                
            }

            @Override
            public void keyReleased(KeyEvent e) {
                
                if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE && ECDLastNameInput.getText().isBlank()){
                    
                    ECDiDInput.setText("");                        //populates the labels with the information
                    ECDNameOutput.setText("");                            
                    ECDTelephoneOutput.setText("");  
                    ECDEmailOutput.setText("");  
                    ECDAddressOutput.setText("");  
                    
                    ECDFoundComboBox.removeAllItems();
                }
            }
        
        }); 
            
    }//GEN-LAST:event_editCustomerDetailsBTNActionPerformed

    /**
    * takes part of name entered and finds matching putting names in the found combo box
    * also adding item listener to combo box to fill in details on selection
    * @param evt 
    */
    
    private void ECDFindBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ECDFindBTNActionPerformed
        
        String prefix = "";
        
        ECDFoundComboBox.removeAllItems();           //resets the combo box
        
        if(!ECDLastNameInput.getText().isBlank()){          //checks to see if any letters entered if it is blank it retrieves every name
        
            prefix = ECDLastNameInput.getText().substring(0, 1).toUpperCase() + ECDLastNameInput.getText().substring(1);   //capitalizes
        }
        
        ArrayList<String> foundNames = cs.findCustomers(prefix);
        
        for(String name : foundNames){          //populates combo box with the found custmomers           
            
            ECDFoundComboBox.addItem(name);
        }
        
    //populates details with first result, if only one result items would not show due to the item listener not firing
        
        if(ECDFoundComboBox.getItemCount() > 0){
                   
            String firstItem = String.valueOf(ECDFoundComboBox.getSelectedItem());

            String[] nameBreak = firstItem.split(", ");          //splits the name into first and last
            lastName = nameBreak[0];
            firstName = nameBreak[1];

            Object[] details = cs.getCustomerDetailsByName(firstName, lastName);              //retrieves the information from the database

            String name = String.valueOf(details[1] + " " + details[2] + " " + details[3]);

            ECDiDInput.setText(String.valueOf(details[0]));                        //populates the labels with the information
            ECDNameOutput.setText(name);                            
            ECDTelephoneOutput.setText(String.valueOf(details[4]));
            ECDEmailOutput.setText(String.valueOf(details[5]));
            ECDAddressOutput.setText(String.valueOf(details[6]));

        }else{
            
            ECDiDInput.setText("");                     //removes information of previously found customer if next find comes up empty
            ECDNameOutput.setText("");                           
            ECDTelephoneOutput.setText(""); 
            ECDEmailOutput.setText(""); 
            ECDAddressOutput.setText(""); 
            
        }
        
    //item listener for multiple results

        ECDFoundComboBox.addItemListener(new ItemListener(){            //adds itemlistner to combo box to change details on selection
      
            @Override
            public void itemStateChanged(ItemEvent e) {

                if(e.getStateChange() == ItemEvent.SELECTED){

                    String fullName = String.valueOf(e.getItem());

                    String[] nameBreak = fullName.split(", ");          //splits the name into first and last
                    lastName = nameBreak[0];
                    firstName = nameBreak[1];

                    Object[] details = cs.getCustomerDetailsByName(firstName, lastName);              //retrieves the information from the database

                    String name = String.valueOf(details[1] + " " + details[2] + " " + details[3]);

                    ECDiDInput.setText(String.valueOf(details[0]));                     //populates the labels with the information
                    ECDTitleComboBox.setSelectedItem(details[1]);
                    ECDNameOutput.setText(name);                            
                    ECDTelephoneOutput.setText(String.valueOf(details[4]));
                    ECDEmailOutput.setText(String.valueOf(details[5]));
                    ECDAddressOutput.setText(String.valueOf(details[6]));

                }
            }
        });
    }//GEN-LAST:event_ECDFindBTNActionPerformed

    /**
    * saves the edited customer information
    * @param evt 
    */
    
    private void ECDSaveBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ECDSaveBTNActionPerformed
        
        String[] nameSplit = ECDNameOutput.getText().split(" ");            //splits name to get original title
         
        try{

            ID = Integer.parseInt(ECDiDInput.getText());

            if(cs.nameIDCheck(ID, nameSplit[2]) == true){                   //checks the name and id of the customer match in the databse 
                
                boolean justTitleChange = false;               //needed for just a title change as new name array has to set differently              

            //ternarys to see if any change has been entered if not to use the original from the retrieved data base information

                String titleChange = String.valueOf(ECDTitleComboBox.getSelectedItem());      
                String nameChange = !ECDNameChangeInput.getText().isBlank() ? ECDNameChangeInput.getText() : ECDNameOutput.getText();   
                String emailChange = !ECDEmailChangeInput.getText().isBlank() ? ECDEmailChangeInput.getText() : ECDEmailOutput.getText();
                String addressChange = !ECDAddressChangeInput.getText().isBlank() ? ECDAddressChangeInput.getText() : ECDAddressOutput.getText();
                String telePhoneChange = !ECDTelephoneChangeInput.getText().isBlank() ? ECDTelephoneChangeInput.getText() :ECDTelephoneOutput.getText();

                nameSplit = nameChange.split(" ");

            //if more than two lastnames entered each name after first is appended together with hyphen

                if(nameSplit.length > 1 && justTitleChange == true){

                    String hyPhen = "";

                    for(int i = 1; i < nameSplit.length; i++){

                        hyPhen += nameSplit[i].trim() +  "-";
                    }
                    nameSplit[1] = hyPhen.substring(0, hyPhen.length() -1);
                }

                 //splits the name into title, firstname and last name to go to the method

                String[] newName = new String[3];

                if(!ECDNameChangeInput.getText().isBlank()){

                    newName[0] = titleChange;
                    newName[1] = nameSplit[0].substring(0, 1).toUpperCase() + nameSplit[0].substring(1);        //capitalize

                }else{

                    justTitleChange = true;

                    newName[0] = titleChange;
                    newName[1] = nameSplit[1].substring(0, 1).toUpperCase() + nameSplit[1].substring(1);        //capitalize
                }
            
                try{            //catches if only the first and not last name was entered in the change name field

                    if(justTitleChange == false){

                        newName[2] = nameSplit[1].substring(0, 1).toUpperCase() + nameSplit[1].substring(1);        //capitalize

                    }else{                        //if just a title change name stored in array will be different so adjustment to be made otherwise title will be stored as first name

                        newName[2] = nameSplit[2].substring(0, 1).toUpperCase() + nameSplit[2].substring(1);        //capitalize
                    }
                    
                    Object[][] logChange = new Object[5][2];                 //arrages what has been changed into array for sorting in editCustomersMethod
                    
                    logChange[0][0] = !titleChange.matches(nameSplit[0]);
                    logChange[0][1] = "Title Changed To: " + titleChange;

                    logChange[1][0] = !ECDNameChangeInput.getText().isBlank();
                    logChange[1][1] = "Name Changed To: " + nameChange;

                    logChange[2][0] = !ECDEmailChangeInput.getText().isBlank();
                    logChange[2][1] = "Email Changed To: " + emailChange;

                    logChange[3][0] = !ECDAddressChangeInput.getText().isBlank();
                    logChange[3][1] =  "Address Changed To: " + addressChange;    

                    logChange[4][0] = !ECDTelephoneChangeInput.getText().isBlank();
                    logChange[4][1] = "Telephone Changed To: " + telePhoneChange; 
                                        
                    cs.editCustomerDetails(ID, newName, telePhoneChange, emailChange, addressChange, logChange);           //saves changes

                    if(!ECDNameChangeInput.getText().isBlank() || justTitleChange == true){                 //if name change the customer folder name is changed to the new name
                        
                        cs.changeOfNameFolderReName(ID, newName, ECDNameOutput.getText());
                    }
                    
            //resets all the labels, textfields and combo boxes

                    ECDLastNameInput.setText(""); 
                    ECDiDInput.setText("");    

                    ECDFoundComboBox.removeAllItems();
                    ECDTitleComboBox.setSelectedIndex(0);

                    ECDNameOutput.setText("");                              
                    ECDTelephoneOutput.setText("");  
                    ECDEmailOutput.setText("");  
                    ECDAddressOutput.setText("");  

                    ECDNameChangeInput.setText("");
                    ECDTelephoneChangeInput.setText("");
                    ECDEmailChangeInput.setText("");
                    ECDAddressChangeInput.setText("");
                    
                    clearVariables();            //clears all global variables
                    
                    populateMainTable();        //updates main customer table

                }catch(ArrayIndexOutOfBoundsException e){

                    JOptionPane.showMessageDialog(null, "Please Enter First And Last Name(S)");             //if only one name has been entered to change

                }catch(NumberFormatException ex){

                    JOptionPane.showMessageDialog(null, "Telephone Number Can Only Contain Numbers");           //checks telephone input
                } 
            }
        }catch(NumberFormatException ex){

            JOptionPane.showMessageDialog(null, "Please Ensure Id Numer has been Entered And Only container Numbers");      //checks Id Input before anything
        }     
    }//GEN-LAST:event_ECDSaveBTNActionPerformed

    /**
    * searches for the customer details by id number
    * @param evt 
    */
    
    private void ECDSearchBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ECDSearchBTNActionPerformed
        
        try{
            
            int id = Integer.parseInt(ECDiDInput.getText());
            
            Object[] details = cs.getCustomerDetailsByID(id);              //retrieves the information from the database by id

            String name = String.valueOf(details[1] + " " + details[2] + " " + details[3]);
                                                            
            ECDNameOutput.setText(name);                                    //populates the labels with the information
            ECDTelephoneOutput.setText(String.valueOf(details[4]));
            ECDEmailOutput.setText(String.valueOf(details[5]));
            ECDAddressOutput.setText(fixAddressFromDB(String.valueOf(details[6])));
            
            
        }catch(NumberFormatException ex){
            
            JOptionPane.showMessageDialog(null, "ID Can Only Contain Numbers");
        } 
    }//GEN-LAST:event_ECDSearchBTNActionPerformed

    /**
    * opens the SCWindow and adds key listeners to input field and item listener to Search customer combo box
    * @param evt 
    */
    
    private void searchCustomerBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchCustomerBTNActionPerformed
        
        SCWindow.setVisible(true);
        SCWindow.setLocationRelativeTo(null);
        
        DefaultTableModel tm = (DefaultTableModel)SCConLocTable.getModel();
         
        SCLastNameSearchInput.requestFocus();
               
        SCLastNameSearchInput.addKeyListener(new KeyListener(){
        
            @Override
            public void keyTyped(KeyEvent e) {          
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {                       //as keys are released the text is logged and searched for straight away
                
                if(!SCLastNameSearchInput.getText().isBlank()){                  
                        
                    SCFoundComboBox.removeAllItems();

                    String prefix = SCLastNameSearchInput.getText().substring(0,1).toUpperCase() + SCLastNameSearchInput.getText().substring(1);   //capitalize

                    ArrayList<String> foundNames = cs.findCustomers(prefix);                //searches database for names by prefix

                    for(String names : foundNames){

                        SCFoundComboBox.addItem(names);                     //populates combo box with results
                    }

                    if(foundNames.isEmpty()){                       //checks for null returned results

                        SCFoundComboBox.addItem("None Matching");
                        
                        SCIdSearchInput.setText("");                        //resets the labels of information
                        SCDateInOutput.setText(""); 
                        SCNameOutput.setText("");                            
                        SCTelephoneOutput.setText(""); 
                        SCEmailOutput.setText(""); 
                        SCAddressOutput.setText(""); 
                        
                        tm.setRowCount(0); 
                    }

            //populates details with first result, if only one result items would not show due to the item listener not firing

                    String firstItem = String.valueOf(SCFoundComboBox.getSelectedItem());

                    if(!String.valueOf(SCFoundComboBox.getSelectedItem()).matches("None Matching")){

                        String[] nameBreak = firstItem.split(", ");          //splits the name into first and last
                        lastName = nameBreak[0];
                        firstName = nameBreak[1];

                        Object[] details = cs.getCustomerDetailsByName(firstName, lastName);              //retrieves the information from the database

                        String name = String.valueOf(details[1] + " " + details[2] + " " + details[3]);

                        SCIdSearchInput.setText(String.valueOf(details[0]));    //populates the labels with the information
                        SCDateInOutput.setText(String.valueOf(details[8]));
                        SCNameOutput.setText(name);                            
                        SCTelephoneOutput.setText(String.valueOf(details[4]));
                        SCEmailOutput.setText(String.valueOf(details[5]));
                        SCAddressOutput.setText(fixAddressFromDB(String.valueOf(details[6])));

                        ID = Integer.valueOf(String.valueOf(details[0]));
                                                
                        Object[][] conLocs = wh.getCustomerContainersAndLocations(ID);              //retreievse the customers containers and populates table
                        
                        tm.setRowCount(0);
                       
                        for(int i = 0; i <conLocs.length; i++){

                            tm.addRow(new Object[]{conLocs[i][0], conLocs[i][1]});          //populates table with results
                        }
                    }

                //item listener for multiple results

                    SCFoundComboBox.addItemListener(new ItemListener(){            //adds itemlistner to combo box to change details on selection

                        @Override
                        public void itemStateChanged(ItemEvent e) {

                            if(e.getStateChange() == ItemEvent.SELECTED){

                                if(!String.valueOf(SCFoundComboBox.getSelectedItem()).matches("None Matching")){

                                    String fullName = String.valueOf(e.getItem());

                                    String[] nameBreak = fullName.split(", ");          //splits the name into first and last
                                    lastName = nameBreak[0];
                                    firstName = nameBreak[1];

                                    Object[] details = cs.getCustomerDetailsByName(firstName, lastName);              //retrieves the information from the database

                                    String name = String.valueOf(details[1] + " " + details[2] + " " + details[3]);

                                    SCIdSearchInput.setText(String.valueOf(details[0]));                        //populates the labels with the information
                                    SCDateInOutput.setText(String.valueOf(details[8]));
                                    SCNameOutput.setText(name);                            
                                    SCTelephoneOutput.setText(String.valueOf(details[4]));
                                    SCEmailOutput.setText(String.valueOf(details[5]));
                                    SCAddressOutput.setText(fixAddressFromDB(String.valueOf(details[6])));

                                    ID = Integer.valueOf(String.valueOf(details[0]));
                                    
                                    Object[][] conLocs = wh.getCustomerContainersAndLocations(ID);              //retreievse the customers containers and populates table
                                    
                                    tm.setRowCount(0);

                                    for(int i = 0; i <conLocs.length; i++){

                                        tm.addRow(new Object[]{conLocs[i][0], conLocs[i][1]});
                                        
                                    }
                                } 
                            }
                        }
                    });
                    
                }else if(e.getID() == KeyEvent.VK_BACK_SPACE){
                    
                    SCFoundComboBox.removeAllItems();
                        
                    SCIdSearchInput.setText("");                        //resets the labels of information
                    SCDateInOutput.setText(""); 
                    SCNameOutput.setText("");                            
                    SCTelephoneOutput.setText(""); 
                    SCEmailOutput.setText(""); 
                    SCAddressOutput.setText(""); 

                    tm.setRowCount(0); 
                    
                }else{
                    
                    SCFoundComboBox.removeAllItems();
                        
                    SCIdSearchInput.setText("");                        //resets the labels of information
                    SCDateInOutput.setText(""); 
                    SCNameOutput.setText("");                            
                    SCTelephoneOutput.setText(""); 
                    SCEmailOutput.setText(""); 
                    SCAddressOutput.setText(""); 

                    tm.setRowCount(0);
                }
            }      
        });                
    }//GEN-LAST:event_searchCustomerBTNActionPerformed

    /**
    * opens the Remove Customer Window
    * @param evt 
    */
    
    private void removeCustomerBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeCustomerBTNActionPerformed
        
        RCWindow.setVisible(true);
        RCWindow.setLocationRelativeTo(null);
        
        RCNameInput.requestFocus();
               
        RCNameInput.addKeyListener(new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {
                
            }

            @Override
            public void keyPressed(KeyEvent e) {
                
            }

            @Override
            public void keyReleased(KeyEvent e) {
            
                if(!RCNameInput.getText().isBlank()){
                    
                    RCComboBox.removeAllItems();                //clears previous search results
                    
                    String preFix = RCNameInput.getText().substring(0,1).toUpperCase() + RCNameInput.getText().substring(1);   //capitalize 
                
                    ArrayList<String> found = cs.findCustomers(preFix);         //retrieves found names
                    
                    for(String f : found){                  //adds found names to combo box
                        
                        RCComboBox.addItem(f);
                    }
                    
                    if(RCComboBox.getItemCount() == 0){             //deletes previous text area details if no customer found
                        
                        RCDetailsOutput.selectAll();
                        RCDetailsOutput.replaceSelection("");
                                               
                    }else{
                        
                    //finds the customer who is first in the combo box and populates fields with info, as item listener will not fire for one result
                        
                        String foundCustomer = String.valueOf(RCComboBox.getSelectedItem());
                        
                        String[] nameSplit = foundCustomer.split(", ");                         
                        lastName = nameSplit[0];
                        firstName = nameSplit[1];
                        
                        Object[] details = cs.getCustomerDetailsByName(firstName, lastName);            //gets customer details
                        
                        String name = String.valueOf(details[1] + " " + details[2] + " " + details[3]);     //formats for full name   
                        
                        ID = Integer.valueOf(String.valueOf(details[0]));
                                
                        int cons = wh.getNumberOfContainers(ID);
                        
                        RCDetailsOutput.setText("Name: '" + name + "' \n"                       //populates details into text area
                                                    + "ID: '" + details[0] + "' \n"
                                                        + "Telephone: '" + details[4] + "' \n"
                                                            + "E-Mail: '" + details[5] + "' \n"
                                                                    + "Address: \n'" + details[6] + "' \n"
                                                                            + "Date Entered: '" + details[8] + "' \n"
                                                                                + "Number Of Containers: '" + cons + "'");
                        
                    }
                        
                    RCComboBox.addItemListener(new ItemListener(){
                            
                        @Override
                        public void itemStateChanged(ItemEvent e) {

                            if(!RCNameInput.getText().isBlank() || RCComboBox.getItemCount() > 0){
                                
                                RCDetailsOutput.selectAll();                    //removes information ready for next selection
                                RCDetailsOutput.replaceSelection("");

                                String customer = String.valueOf(RCComboBox.getSelectedItem());

                                try{
                                    
                                    String[] nameSplit = customer.split(", ");                         
                                    lastName = nameSplit[0];
                                    firstName = nameSplit[1];

                                    Object[] details = cs.getCustomerDetailsByName(firstName, lastName);            //gets customer details

                                    String name = String.valueOf(details[1] + " " + details[2] + " " + details[3]);     //formats for full name   

                                    ID = Integer.valueOf(String.valueOf(details[0]));

                                    int cons = wh.getNumberOfContainers(ID);

                                    RCDetailsOutput.setText("Name: '" + name + "' \n\n"                       //populates details into text area
                                                                + "ID: '" + details[0] + "' \n\n"
                                                                    + "Telephone: '" + details[4] + "' \n\n"
                                                                        + "E-Mail: '" + details[5] + "' \n\n"
                                                                                + "Address: \n'" + details[6] + "' \n\n"
                                                                                        + "Date Entered: '" + details[8] + "' \n\n"
                                                                                            + "Number Of Containers: '" + cons + "'");
                                    
                                }catch(ArrayIndexOutOfBoundsException ex){
                                    
                                        //no need for action
                                }
                            }
                        }
                    });
                    
                }else{
                    
                    RCComboBox.removeAllItems();
                    RCDetailsOutput.selectAll();                    //if name input is blank it removes information
                    RCDetailsOutput.replaceSelection("");
                         
                }
            } 
        });       
    }//GEN-LAST:event_removeCustomerBTNActionPerformed

    /**
    * removes customer 
    * @param evt 
    */
    
    private void RCRemoveBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RCRemoveBTNActionPerformed
        
        if(JOptionPane.showConfirmDialog(null, "Are You Sure You Want To Remove This Customer From The Database?", "Remove Customer", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
            
            containers = wh.getCustomerContainers(ID);
            cs.removeCustomer(ID, containers);
            
            RCComboBox.removeAllItems();
            RCDetailsOutput.selectAll();                    //removes details
            RCDetailsOutput.replaceSelection("");
            RCNameInput.setText("");
            
            populateMainTable();        //updates main customer table
        }
    }//GEN-LAST:event_RCRemoveBTNActionPerformed

    /**
     * opens up the add new Container window and adds listeners to fields, combo box and toggle
     * @param evt 
     */
    
    private void addNewContainerBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewContainerBTNActionPerformed
        
        ANCONWindow.setVisible(true);
        ANCONWindow.setLocationRelativeTo(null);
        ANCONConInput.setFocusable(true);           //to set focusable edit inventory can unset it to add container
        ANCONWareCusToggle.setEnabled(true);        //same situation
        ANCONNameInput.setFocusable(true);          //
        
        ANCONWareCusToggle.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e) {
                
          
                if(ANCONWareCusToggle.isSelected()){                //alters window size to accomodate new options
                    
                    ANCONWareCusToggle.setText("Add To Warehouse");
                    
                    ANCONWindow.setSize(538, 550);
                    
                }else{
                    
                    ANCONWareCusToggle.setText("Add To Customer");
                    
                    ANCONWindow.setSize(391, 169);
                }
                
            }
        });
        
        ANCONNameInput.addKeyListener(new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {
                
            }

            @Override
            public void keyPressed(KeyEvent e) {
                
            }

            @Override
            public void keyReleased(KeyEvent e) {
                
                String preFix = "";
                
                if(!ANCONNameInput.getText().isBlank()){
                    
                    ANCONComboBox.removeAllItems();                //clears previous search results
                    
                    if(ANCONNameInput.getText().length() > 1){          //catches length of string
                    
                        preFix = ANCONNameInput.getText().substring(0,1).toUpperCase() + ANCONNameInput.getText().substring(1);   //capitalize 
                    
                    }else{
                        
                        preFix = ANCONNameInput.getText().substring(0,1).toUpperCase();                  //capitalize 
                        
                    }
                
                    ArrayList<String> found = cs.findCustomers(preFix);         //retrieves found names
                    
                    for(String f : found){                  //adds found names to combo box
                        
                        ANCONComboBox.addItem(f);
                    }
                    
                    if(ANCONComboBox.getItemCount() == 0){             //deletes previous text area details if no customer found
                        
                        ANCONDetailsOutput.selectAll();
                        ANCONDetailsOutput.replaceSelection("");
                                               
                    }else{
                        
                    //finds the customer who is first in the combo box and populates fields with info, as item listener will not fire for one result
                        
                        String foundCustomer = String.valueOf(ANCONComboBox.getSelectedItem());
                        
                        String[] nameSplit = foundCustomer.split(", ");                         
                        lastName = nameSplit[0];
                        firstName = nameSplit[1];
                        
                        Object[] details = cs.getCustomerDetailsByName(firstName, lastName);            //gets customer details
                        
                        String name = String.valueOf(details[1] + " " + details[2] + " " + details[3]);     //formats for full name   
                        
                        ID = Integer.valueOf(String.valueOf(details[0]));
                                
                        int cons = wh.getNumberOfContainers(ID);
                        
                        ANCONDetailsOutput.setText("Name: '" + name + "' \n"                       //populates details into text area
                                                    + "ID: '" + details[0] + "' \n"
                                                        + "Telephone: '" + details[4] + "' \n"
                                                            + "E-Mail: '" + details[5] + "' \n"
                                                                    + "Address: \n'" + details[6] + "' \n"
                                                                            + "Date Entered: '" + details[8] + "' \n"
                                                                                + "Number Of Containers: '" + cons + "'");
                        
                    }
                        
                    ANCONComboBox.addItemListener(new ItemListener(){
                            
                        @Override
                        public void itemStateChanged(ItemEvent e) {

                            if(!ANCONNameInput.getText().isBlank() || ANCONComboBox.getItemCount() > 0){
                                
                                ANCONDetailsOutput.selectAll();                    //removes information ready for next selection
                                ANCONDetailsOutput.replaceSelection("");

                                String customer = String.valueOf(ANCONComboBox.getSelectedItem());

                                try{
                                    
                                    String[] nameSplit = customer.split(", ");                         
                                    lastName = nameSplit[0];
                                    firstName = nameSplit[1];

                                    Object[] details = cs.getCustomerDetailsByName(firstName, lastName);            //gets customer details

                                    String name = String.valueOf(details[1] + " " + details[2] + " " + details[3]);     //formats for full name   

                                    ID = Integer.valueOf(String.valueOf(details[0]));

                                    int cons = wh.getNumberOfContainers(ID);

                                    ANCONDetailsOutput.setText("Name: '" + name + "' \n\n"                       //populates details into text area
                                                                + "ID: '" + details[0] + "' \n\n"
                                                                    + "Telephone: '" + details[4] + "' \n\n"
                                                                        + "E-Mail: '" + details[5] + "' \n\n"
                                                                                + "Address: \n'" + details[6] + "' \n\n"
                                                                                        + "Date Entered: '" + details[8] + "' \n\n"
                                                                                            + "Number Of Containers: '" + cons + "'");
                                    
                                }catch(ArrayIndexOutOfBoundsException ex){
                                    
                                        //no need for action
                                }
                            }
                        }
                    });
                    
                }else{
                    
                    ANCONComboBox.removeAllItems();
                    ANCONDetailsOutput.selectAll();                    //if name input is blank it removes information
                    ANCONDetailsOutput.replaceSelection("");
                         
                }               
            }      
        });
        
        ANCONLocInput.addKeyListener(new KeyListener(){
            
            @Override
            public void keyTyped(KeyEvent e) {
                
            }

            @Override
            public void keyPressed(KeyEvent e) {
                
            }

            @Override
            public void keyReleased(KeyEvent e) {
                
                ANCONLocComboBox.removeAllItems();                     //clears combo box for new find
                
                String preFix = ANCONLocInput.getText();
                
                ArrayList<String> found = wh.getLocationsWithPrefix(preFix);        //gets all free locations and populates combo box
                
                for(String f : found){
                    
                    ANCONLocComboBox.addItem(f);
                    System.out.println(f);
                }
                
                preFix = "";
            }  
        });
        
        ANCONLocComboBox.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e) {
                
                if(e.getStateChange() == ItemEvent.SELECTED){
                    
                    ANCONLocInput.setText(String.valueOf(e.getItem()));         //sets location filed to chosen location
                }
            }   
        });       
                
         ANCONLocInput.addFocusListener(new java.awt.event.FocusAdapter() {      //focus listener to select all text when clicked as item listerner disrupts deletion
        public void focusGained(java.awt.event.FocusEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ANCONLocInput.selectAll();
                    }
                });
            }
        });      
    }//GEN-LAST:event_addNewContainerBTNActionPerformed

    /**
     * adds new container to either the empties table or to an existing customer
     * @param evt 
     */
    
    private void ANCONAddBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ANCONAddBTNActionPerformed
        
        try{
            
            container = Integer.valueOf(String.valueOf(ANCONConInput.getText()));
            
            location = ANCONLocInput.getText();
            
            if(ANCONWareCusToggle.isSelected()){              
                   
                if(wh.checkContainer(container) && wh.checkEmptiesContainer(container)){        //checks container it is brand new
                    
                    if(!ANCONLocInput.getText().isBlank()){
                            
                        wh.assignNewContainer(ID, container, location, "ENTERED");          //assigns container to customer

                        cs.upDateCustomerContainerCount(ID, 1);                 //updates customer container count

                        CNIWindow.setVisible(true);                         //opens create new inventory window
                        CNIWindow.setLocationRelativeTo(null);

                        Object[] details = cs.getCustomerDetailsByID(ID);           //gets customer details

                        String fullName = String.valueOf(details[1] + " " + details[2] + " " + details[3]);     //formats for full name   

                        CNIIdOutput.setText(String.valueOf(details[0]));             //populates fields
                        CNINameOutput.setText(fullName);
                        CNITelephoneOutput.setText(String.valueOf(details[4]));
                        CNIEmailOutput.setText(String.valueOf(details[5]));
                        CNIAddressOutput.setText(String.valueOf(details[6]));

                        CNIContainerComboBox.removeAllItems();                  //removes all items from combobox, just to be sure

                        CNIContainerComboBox.addItem(String.valueOf(container));        //adds container to combo box

                        ANCONConInput.setText("");          //resets fields
                        ANCONNameInput.setText("");            
                        ANCONLocInput.setText("");                   
                        ANCONDetailsOutput.selectAll();
                        ANCONDetailsOutput.replaceSelection("");
                        ANCONComboBox.removeAllItems();
                        ANCONLocComboBox.removeAllItems();

                        ANCONWindow.dispose();                  //disposes of add new container window
                    }else{
                        
                        JOptionPane.showMessageDialog(null, "You Must Allocate A Location!");
                    }
                                                  
                }else if(wh.checkContainer(container) && !wh.checkEmptiesContainer(container)){     //checks if container is in empties then assigns if true
                    
                    if(!ANCONLocInput.getText().isBlank()){    
                        
                        containers = new int[1];
                        locations = new String[1];

                        containers[0] = container;
                        locations[0] = location;

                        wh.assignContainers(ID, containers, locations, "ENTERED", true);          //assigns the empty to the customer

                        Object[] details = cs.getCustomerDetailsByID(ID);           //gets customer details

                        String fullName = String.valueOf(details[1] + " " + details[2] + " " + details[3]);     //formats for full name  

                        JOptionPane.showMessageDialog(null, "Container : " + container + " Has Been Added To " + fullName);

                        ANCONConInput.setText("");          //resets fields
                        ANCONNameInput.setText("");            
                        ANCONLocInput.setText("");                   
                        ANCONDetailsOutput.selectAll();
                        ANCONDetailsOutput.replaceSelection("");
                        ANCONComboBox.removeAllItems();
                        ANCONLocComboBox.removeAllItems();

                        CNIWindow.setVisible(true);                         //opens create new inventory window
                        CNIWindow.setLocationRelativeTo(null);

                        CNIIdOutput.setText(String.valueOf(details[0]));             //populates fields
                        CNINameOutput.setText(fullName);
                        CNITelephoneOutput.setText(String.valueOf(details[4]));
                        CNIEmailOutput.setText(String.valueOf(details[5]));
                        CNIAddressOutput.setText(String.valueOf(details[6]));

                        CNIContainerComboBox.removeAllItems();                  //removes all items from combobox, just to be sure

                        CNIContainerComboBox.addItem(String.valueOf(container));        //adds container to combo box

                        ANCONWindow.dispose();                  //disposes of add new container window
                        
                    }else{
                        
                        JOptionPane.showMessageDialog(null, "You Must Allocate A Location!");
                    }
                    
                }else{
                    
                    JOptionPane.showMessageDialog(null, "Container Number Is Already In Use!");         //else container is in use by another customer
                }
                
            }else{
                
                if(wh.checkContainer(container) && wh.checkEmptiesContainer(container)){            //checks container not already in system
                    
                    wh.addNewContainerToEmpties(container);              //adds container straight to the empties table
                    
                    JOptionPane.showMessageDialog(null, "" + container + " Has Been Added To The System");
                    
                }else{
                    
                    JOptionPane.showMessageDialog(null, "Container Number Is Already In System");
                }
            }
        }catch(NumberFormatException ex){
            
            JOptionPane.showMessageDialog(null, "Only Numbers Can Be Used For Containers!");
        }       
    }//GEN-LAST:event_ANCONAddBTNActionPerformed

    /*
    opens the move container window and populates the location combo box
    */
    private void moveContainersBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveContainersBTNActionPerformed
     
        LinkedHashSet<String> aisles = wh.getAisleLetters();                //gets all aisle starting letter
   
        mcp.conMove.clear();            //clears old aisles (messes with the mouse click event if not cleared)
        mcp.MCLocComboBox.removeAllItems();     //clears combo box
        
                 
        MCWindow.setVisible(true);
        MCWindow.setLocationRelativeTo(null);
           
        for(String a : aisles){                     //populates combo box with found aisle letters
            
            mcp.MCLocComboBox.addItem(a);
        }
        
        mcp.MCLocComboBox.setSelectedItem(0);
 
        mcp.MCLocComboBox.addItemListener(new ItemListener(){
            
            @Override
            public void itemStateChanged(ItemEvent e) {
                
                mcp.MCRemoveBTN.setVisible(false);      // for when an aisle change occurs before container deselected
                mcp.MCUnPickAllBTN.setVisible(false);
                
                if(!String.valueOf(mcp.MCLocComboBox.getSelectedItem()).matches("Aisle") && mcp.MCLocComboBox.getItemCount() > 0){      //ignore the aisle item
                    
                    mcp.conMove.clear();            //clears old aisles (messes with the mouse click event if not cleared)

                    String letter = String.valueOf(mcp.MCLocComboBox.getSelectedItem());                //gets ailse letter chosen
               
                    int aisleSize = wh.getNumberOfContainersInAisleByLetter(letter);                //gets the total number of locations in aisle
                    int rowHeight = wh.getRowHeightByAisleLetter(letter);                   //gets the height of the rows
                    
                    int rowLength = aisleSize / rowHeight;                          //work out how many containers per row

                    aisleDetails = wh.getLocationDetailsByAisleLetter(letter);       //gets the status of the location

                    int x = 100;
                    int y = 120;

                    for(int i = 0; i < aisleSize; i++){

                        if(i > 0 && i % rowHeight == 0){            //starts new row when row height reached

                           y = 120;
                           x += 120;

                        }

                        int empty = Integer.parseInt(String.valueOf(aisleDetails[i][0]));       //checks if id entered if not location empty

                        container = Integer.parseInt(String.valueOf(aisleDetails[i][1]));       //gets container number if there is one

                        mcp.conMove.add(new Container(x, y, empty == 0 ? "FREE" : String.valueOf(container), false, false));      //adds container number or free if location not used

                        y += 120;               //places container in height position

                    }

                    x = 100;
                    y += 20;

                    mcp.floatToWare.clear();

                    floats = wh.getFloatingContainers();             //gets floating containers from floating table (already removed containers)

                    for(int i = 0; i < floats.length; i++){

                        mcp.floatToWare.add(new Container(x, y, String.valueOf(floats[i][1]), false, true));

                        x += 120;
                    }

                    mcp.repaint();                        

                }else{
                    
                    mcp.conMove.clear();            //clears containers from screen if aisle selected
                    mcp.repaint();
                   
                }                   
            }
        });      
    }//GEN-LAST:event_moveContainersBTNActionPerformed

    /**
     * opens the search container window and adds key listener to search container input
     * @param evt 
     */
    
    private void searchContainerBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchContainerBTNActionPerformed
       
        SConWindow.setVisible(true);
        SConWindow.setLocationRelativeTo(null);
        
        SCConContainerInput.addKeyListener(new KeyListener(){
            
            @Override
            public void keyTyped(KeyEvent e) {
                
            }

            @Override
            public void keyPressed(KeyEvent e) {
                
                if(e.getKeyCode() ==  KeyEvent.VK_ENTER){               //enter button to search for location
                    
                    try{
                        
                        container = Integer.valueOf(SCConContainerInput.getText());
                        
                        location = wh.findLocation(container);
                        
                        SConLocationLabel.setText(location);
                        
                        String name = cs.getCustomerNameAndIdByContainer(container);
                        
                        SConNameLabel.setText(name);
                        
                    }catch(NumberFormatException ex){
                        
                        JOptionPane.showMessageDialog(null, "Container Can Only Contain Numbers");
                    }
                    
                }
                
                if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE){               //backspace will set labels empty
                    
                    SConLocationLabel.setText("");
                    SConNameLabel.setText("");
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                
                
            }            
        });       
    }//GEN-LAST:event_searchContainerBTNActionPerformed

    /**
     * opens the empty container window
     * @param evt 
     */
    
    private void emptyContainerBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emptyContainerBTNActionPerformed
       
        ECWindow.setVisible(true);
        ECWindow.setLocationRelativeTo(null);
        
        ECContainerInput.addKeyListener(new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {
               
            }

            @Override
            public void keyPressed(KeyEvent e) {
              
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    
                    try{
                        
                        container = Integer.valueOf(ECContainerInput.getText());
                        
                        int choice  = JOptionPane.showConfirmDialog(null, "Are You Sure You Want To Empty Container : '" + container + "' ?", "Empty Container", JOptionPane.YES_NO_OPTION);
                        
                        if(choice == JOptionPane.YES_OPTION){
                            
                            wh.emptySingleContainer(container, false);
                            ECContainerInput.setText("");
                            populateMainTable();    //update main customer table
                            
                        }else{
                            
                            ECContainerInput.setText("");
                        }
                        
                    }catch(NumberFormatException ex){
                        
                        JOptionPane.showMessageDialog(null, "Container Can Only Contain Numbers");
                    }
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                
            }        
        });       
    }//GEN-LAST:event_emptyContainerBTNActionPerformed

    /**
     * opens up pick list window and adds listeners
     * @param evt 
     */
    
    private void createPickListBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createPickListBTNActionPerformed
        
        PLWindow.setVisible(true);
        PLWindow.setLocationRelativeTo(null);
        
        DefaultTableModel tmi = (DefaultTableModel) PLInventoryTable.getModel();
        DefaultTableModel tmp = (DefaultTableModel) PLPickedTable.getModel();
        
        PLNameInput.addKeyListener(new KeyListener(){
            
            @Override
            public void keyTyped(KeyEvent e) {
                
            }

            @Override
            public void keyPressed(KeyEvent e) {
                
            }

            @Override
            public void keyReleased(KeyEvent e) {
                
                String preFix = "";
                
                if(!PLNameInput.getText().isBlank()){
                    
                    PLNameComboBox.removeAllItems();                //clears previous search results
                    
                    if(PLNameInput.getText().length() > 1){          //catches length of string
                    
                        preFix = PLNameInput.getText().substring(0,1).toUpperCase() + PLNameInput.getText().substring(1);   //capitalize 
                    
                    }else{
                        
                        preFix = PLNameInput.getText().substring(0,1).toUpperCase();                  //capitalize 
                        
                    }
                
                    ArrayList<String> found = cs.findCustomers(preFix);         //retrieves found names
                    
                    for(String f : found){                  //adds found names to combo box
                        
                        PLNameComboBox.addItem(f);
                    }
                    
                    if(PLNameComboBox.getItemCount() == 0){             //deletes previous text area details if no customer found
                        
                        PLDetailsOutput.selectAll();
                        PLDetailsOutput.replaceSelection("");
                                               
                    }else{
                        
                    //finds the customer who is first in the combo box and populates fields with info, as item listener will not fire for one result
                        
                        String foundCustomer = String.valueOf(PLNameComboBox.getSelectedItem());
                        
                        String[] nameSplit = foundCustomer.split(", ");                         
                        lastName = nameSplit[0];
                        firstName = nameSplit[1];
                        
                        Object[] details = cs.getCustomerDetailsByName(firstName, lastName);            //gets customer details
                        
                        String name = String.valueOf(details[1] + " " + details[2] + " " + details[3]);     //formats for full name   
                        
                        ID = Integer.valueOf(String.valueOf(details[0]));
                        
                        PLIDLabel.setText("ID: " + String.valueOf(details[0]));
                                
                        int cons = wh.getNumberOfContainers(ID);
                        
                        PLDetailsOutput.setText("Name: '" + name + "' \n"                       //populates details into text area
                                                    + "Telephone: '" + details[4] + "' \n"
                                                        + "E-Mail: '" + details[5] + "' \n"
                                                            + "Address: \n'" + details[6] + "' \n"
                                                                + "Date Entered: '" + details[8] + "'");
                        
                        
                        containers = wh.getCustomerContainers(ID);                  //populates the container combo box with the customers containers
                        
                        for(int c : containers){
                            
                            PLConComboBox.addItem(String.valueOf(c));
                        }

                        tmi.setRowCount(0);                     //reset table

                        container = Integer.valueOf(String.valueOf(PLConComboBox.getSelectedItem()));

                        Object[][] inventory = in.getInventory(container);          //return inventory of container

                        for(Object[] i : inventory){                            //populate pick list inventory table

                            tmi.addRow(new Object[]{i[0],i[1],i[2],i[3],i[4]});

                        }                    
                    }   
                    
                }else{
                    
                    PLNameComboBox.removeAllItems();
                    PLDetailsOutput.selectAll();                    //if name input is blank it removes information
                    PLDetailsOutput.replaceSelection("");
                    PLConComboBox.removeAllItems();
                } 
                            
            } 
        });
        
        PLNameComboBox.addItemListener(new ItemListener(){

            @Override
            public void itemStateChanged(ItemEvent e) {

                PLConComboBox.removeAllItems();         //resets combo box

                if(!PLNameInput.getText().isBlank() || PLNameComboBox.getItemCount() > 0){

                   PLDetailsOutput.selectAll();                    //removes information ready for next selection
                    PLDetailsOutput.replaceSelection("");

                    String customer = String.valueOf(ANCONComboBox.getSelectedItem());

                    try{

                        String[] nameSplit = customer.split(", ");                         
                        lastName = nameSplit[0];
                        firstName = nameSplit[1];

                        Object[] details = cs.getCustomerDetailsByName(firstName, lastName);            //gets customer details

                        String name = String.valueOf(details[1] + " " + details[2] + " " + details[3]);     //formats for full name   

                        ID = Integer.valueOf(String.valueOf(details[0]));
                        
                        PLIDLabel.setText("ID: " + String.valueOf(details[0]));

                        int cons = wh.getNumberOfContainers(ID);

                        PLDetailsOutput.setText("Name: '" + name + "' \n"                       //populates details into text area                                       
                                                    + "Telephone: '" + details[4] + "' \n"
                                                        + "E-Mail: '" + details[5] + "' \n"
                                                            + "Address: \n'" + details[6] + "' \n"
                                                                + "Date Entered: '" + details[8] + "'");

                        containers = wh.getCustomerContainers(ID);                  //populates the container combo box with the customers containers

                        for(int c : containers){

                            PLConComboBox.addItem(String.valueOf(c));
                        }
                        
                        
                        tmi.setRowCount(0);                     //reset table

                        container = Integer.valueOf(String.valueOf(PLConComboBox.getSelectedItem()));

                        Object[][] inventory = in.getInventory(container);          //return inventory of container

                        for(Object[] i : inventory){                            //populate pick list inventory table

                            tmi.addRow(new Object[]{i[0],i[1],i[2],i[3],i[4]});

                        }
                                    
                    }catch(ArrayIndexOutOfBoundsException ex){

                            //no need for action
                    }
                }
            }
        });     
        
        PLConComboBox.addItemListener(new ItemListener(){
            
            @Override
            public void itemStateChanged(ItemEvent e) {
                
                tmi.setRowCount(0);                     //reset table
                
                try{
                    

                    container = Integer.valueOf(String.valueOf(PLConComboBox.getSelectedItem()));

                    Object[][] inventory = in.getInventory(container);          //return inventory of container

                    for(Object[] i : inventory){                            //populate pick list inventory table

                        tmi.addRow(new Object[]{i[0],i[1],i[2],i[3],i[4]});

                    }
               
                }catch(NumberFormatException ex){
                        
                    //when name input is deleted this will fire when it is emptied no issues 
                }    
            }
        });       
    }//GEN-LAST:event_createPickListBTNActionPerformed

    /**
     * add button to add all Customers containers to the pick list
     * @param evt 
     */
    
    private void PLAddAllBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PLAddAllBTNActionPerformed
        
        DefaultTableModel tm = (DefaultTableModel) PLPickedTable.getModel();
        
        ID = Integer.valueOf(String.valueOf(PLIDLabel.getText().substring(4)));  // 4 because of ID: 
        
        String fullName = cs.getCustomerNameById(ID);
        
        Object[][] conLocs = wh.getCustomerContainersAndLocations(ID);
        
        for(Object[] p : conLocs){
            
            tm.addRow(new Object[]{fullName, p[0], p[1], "ALL"});       //ALL means all items (whole container)
        }
        
    }//GEN-LAST:event_PLAddAllBTNActionPerformed

    /**
     * adds single container to pick list
     * @param evt 
     */
    
    private void PLAddComBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PLAddComBTNActionPerformed
       
        DefaultTableModel tm = (DefaultTableModel) PLPickedTable.getModel();
        
        ID = Integer.valueOf(String.valueOf(PLIDLabel.getText().substring(4)));  // 4 because of ID: 
        
        String fullName = cs.getCustomerNameById(ID);
        
        container = Integer.valueOf(String.valueOf(PLConComboBox.getSelectedItem()));
        
        location = wh.findLocation(container);
        
        tm.addRow(new Object[]{fullName, container, location, "ALL"});       //ALL means all items (whole container)
        
    }//GEN-LAST:event_PLAddComBTNActionPerformed

    /**
     * removes chosen item from inventory list in pick list window
     * @param evt 
     */
    
    private void PLAddItemBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PLAddItemBTNActionPerformed
           
        DefaultTableModel tmi = (DefaultTableModel) PLInventoryTable.getModel();
        DefaultTableModel tmp = (DefaultTableModel) PLPickedTable.getModel();
        
        ID = Integer.valueOf(String.valueOf(PLIDLabel.getText().substring(4)));  // 4 because of ID: 
        
        String fullName = cs.getCustomerNameById(ID);
        
        container = Integer.valueOf(String.valueOf(PLConComboBox.getSelectedItem()));
        
        location = wh.findLocation(container); 
               
        int getAmount = 0;
        
        int selection = PLInventoryTable.getSelectedRow();
        
        if(selection != -1){                //checks item selected
        
            int amount = Integer.valueOf(String.valueOf(tmi.getValueAt(selection, 0)));         //gets the number of items

            if(amount > 1){                         //give choice to take certain amount

                try{

                    getAmount = Integer.valueOf(JOptionPane.showInputDialog(null, "Please Choose The Number Of Items To Remove", "Remove Item", JOptionPane.OK_CANCEL_OPTION));

                    if(getAmount > amount){

                        JOptionPane.showMessageDialog(null, "You Can Not Take More Than Is There!");

                    }else{                          //adds amount you entered to take away
                        
                        String item = String.valueOf(tmi.getValueAt(selection, 1));
                        String description = String.valueOf(tmi.getValueAt(selection, 2));
                        String condition = String.valueOf(tmi.getValueAt(selection, 3));
                        String photoFile = String.valueOf(tmi.getValueAt(selection, 4));
                        
                        String fullItem = "" + getAmount + " - " + item + " - " + description + " - " + condition + " - "  + photoFile;

                        tmp.addRow(new Object[]{fullName, container, location, fullItem});
                    }

                }catch(NumberFormatException ex){

                        JOptionPane.showMessageDialog(null, "Please Only Enter Numbers");

                } 
            }else{                      //if there is only one item to take
                               
                String item = String.valueOf(tmi.getValueAt(selection, 1));
                String description = String.valueOf(tmi.getValueAt(selection, 2));
                String condition = String.valueOf(tmi.getValueAt(selection, 3));
                String photoFile = String.valueOf(tmi.getValueAt(selection, 4));

                String fullItem = "" + amount + " - " + item + " - " + description + " - " + condition + " - "  + photoFile;

                tmp.addRow(new Object[]{fullName, container, location, fullItem});
                
            }
        }else{
            
            JOptionPane.showMessageDialog(null, "Please Select Item To Remove");
        }
    }//GEN-LAST:event_PLAddItemBTNActionPerformed

    /**
     * removes entry from the pick list table
     * @param evt 
     */
    
    private void PLRemoveBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PLRemoveBTNActionPerformed
       
        DefaultTableModel tm = (DefaultTableModel) PLPickedTable.getModel();
        
        int selected = PLPickedTable.getSelectedRow();
        
        if(selected == -1){
            
            try{
                
                tm.removeRow(tm.getRowCount() -1);              //removes last item in table
                
            }catch(ArrayIndexOutOfBoundsException ex){
                
                JOptionPane.showMessageDialog(null, "No Items To Remove");
            }
        }else{
            
            try{
                
                tm.removeRow(selected);         //removes selected row
                
            }catch(ArrayIndexOutOfBoundsException ex){
                
                JOptionPane.showMessageDialog(null, "Please Select Item To Remove");
            }
        }      
    }//GEN-LAST:event_PLRemoveBTNActionPerformed

    /**
     * prints pick list
     * @param evt 
     */
    
    private void PLPrintBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PLPrintBTNActionPerformed
       
        try {
            
            PLPickedTable.print();
                    
        } catch (PrinterException ex) {
            
            Logger.getLogger(ContainerMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_PLPrintBTNActionPerformed

    /**
     * finalizes pick list table containers removed and items removed or updated in container
     * @param evt 
     */
    
    private void PLFinalizeBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PLFinalizeBTNActionPerformed
       
        DefaultTableModel tm = (DefaultTableModel) PLPickedTable.getModel();
               
        if(printed){
            
            for(int i = 0; i < tm.getRowCount(); i++){

                container = Integer.valueOf(String.valueOf(tm.getValueAt(i, 1)));
                location = String.valueOf(tm.getValueAt(i, 2));

                String[] split = String.valueOf(tm.getValueAt(i, 3)).split(" - ");

                int numOfItems = Integer.valueOf(split[0]);
                String item = split[1];
                String description = split[2];
                String condition = split[3];
                String photoFile = split[4];

                in.removeItems(ID, container, numOfItems, item, description, condition, photoFile);

            }  

            tm.setRowCount(0);                      //resets pick list window inputs

            PLNameComboBox.removeAllItems();
            PLDetailsOutput.selectAll();                   
            PLDetailsOutput.replaceSelection("");
            PLConComboBox.removeAllItems();
            PLNameInput.setText("");
            
        }else{
            
            int choice = JOptionPane.showConfirmDialog(null, "You Have Not Printed The Pick List Yet, Do You Want To Do It Now?", "Print Pick List?", JOptionPane.OK_CANCEL_OPTION);
            
            if(choice == JOptionPane.OK_OPTION){
                
                PLPrintBTN.doClick();
                
            }else{
                
                printed = true;
                PLFinalizeBTN.doClick();
            }
        }       
    }//GEN-LAST:event_PLFinalizeBTNActionPerformed

    /**
     * opens the create new inventory window and adds non inventoried containers to the combo box
     * @param evt 
     */
    
    private void createNewInventoryBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createNewInventoryBTNActionPerformed

        findUnInventoried();    //rare case uninventorized container gets removed resets the count
        
        CNIWindow.setVisible(true);
        CNIWindow.setLocationRelativeTo(null);
        CNIContainerComboBox.removeAllItems();

        ArrayList<String> unInventorized = in.getUnInventoried();
        
        for(String un : unInventorized){
            
            CNIContainerComboBox.addItem(un);
        }
        
        //select first item in combobox and get details because itemlistener will not fire without change
    
        try{
            container = Integer.valueOf(String.valueOf(CNIContainerComboBox.getSelectedItem()));

            ID = wh.getCustomerIdByContainer(container);

            Object[] details = cs.getCustomerDetailsByID(ID);           //gets customer details

            String fullName = String.valueOf(details[1] + " " + details[2] + " " + details[3]);     //formats for full name   

            CNIIdOutput.setText(String.valueOf(details[0]));             //populates fields
            CNINameOutput.setText(fullName);
            CNITelephoneOutput.setText(String.valueOf(details[4]));
            CNIEmailOutput.setText(String.valueOf(details[5]));
            CNIAddressOutput.setText(String.valueOf(details[6]));

            CNIContainerComboBox.addItemListener(new ItemListener(){            //item listener to get container change

                @Override
                public void itemStateChanged(ItemEvent e) {

                    if(!String.valueOf(CNIContainerComboBox.getSelectedItem()).matches("null")){

                        container = Integer.valueOf(String.valueOf(CNIContainerComboBox.getSelectedItem()));

                        ID = wh.getCustomerIdByContainer(container);

                        Object[] details = cs.getCustomerDetailsByID(ID);           //gets customer details

                        String fullName = String.valueOf(details[1] + " " + details[2] + " " + details[3]);     //formats for full name   

                        CNIIdOutput.setText(String.valueOf(details[0]));             //populates fields
                        CNINameOutput.setText(fullName);
                        CNITelephoneOutput.setText(String.valueOf(details[4]));
                        CNIEmailOutput.setText(String.valueOf(details[5]));
                        CNIAddressOutput.setText(String.valueOf(details[6]));
                    }
                }
            });
        }catch(NumberFormatException e){
            
            //does nothing to stop throwing when window opened with no inventories to fill
        }
    }//GEN-LAST:event_createNewInventoryBTNActionPerformed

    /**
     * opens up the search inventory window
     * @param evt 
     */
    
    private void searchInventoryBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchInventoryBTNActionPerformed
       
        SIWindow.setVisible(true);
        SIWindow.setLocationRelativeTo(null);
        
    }//GEN-LAST:event_searchInventoryBTNActionPerformed

    /**
     * searches for specified item in search inventory window
     * @param evt 
     */
    
    private void SISearchBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SISearchBTNActionPerformed
        
        DefaultTableModel tm = (DefaultTableModel)SIInventoryTable.getModel();
        tm.setRowCount(0); 
        
        try{ 
         
            container = SIConInput.getText().isBlank() ? 0 : Integer.valueOf(String.valueOf(SIConInput.getText()));
            String item = SIItemInput.getText();
            String description = SIDescInput.getText();

            if(container > 0 && item.isBlank() && description.isBlank()){           //search just by container
            
                Object[][] inventory = in.getInventory(container);
                
                for(Object[] in : inventory){
                    
                    tm.addRow(new Object[]{container, in[0], in[1], in[2], in[3], in[4]});
                }
                
            }else if(!item.isBlank() && description.isBlank()){         //search by item
                
                ArrayList<String> items = in.searchInventoryByItem(item);
                
                for(String i : items){
                    
                    String[] split = i.split("-");
                    
                    System.out.println(Arrays.toString(split));
                    
                    tm.addRow(new Object[]{split[0], split[1], split[2], split[3], split[4], split[5]}); 
                    
                }
                
            }else if(!description.isBlank() && !item.isBlank()){            //search by item and description
                
                ArrayList<String> items = in.searchInventoryByItemAndDescription(item, description);
                
                for(String i : items){
                    
                    String[] split = i.split("-");
                    
                    tm.addRow(new Object[]{split[0], split[1], split[2], split[3], split[4], split[5]}); 
                
                }
            }
        }catch(NumberFormatException ex){
         
            JOptionPane.showMessageDialog(null, "Container Can Only Contain Numbers!");
        }        
    }//GEN-LAST:event_SISearchBTNActionPerformed

    /**
     * opens the edit inventory window adds listeners
     * @param evt 
     */
    
    private void editInventoryBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editInventoryBTNActionPerformed
        
        EIWindow.setVisible(true);
        EIWindow.setLocationRelativeTo(null);
        EIMoveBTN.setVisible(false);
                
        EIWindow.addWindowListener(new WindowAdapter() {
            
            @Override
            public void windowClosing(WindowEvent e) {
               
                if(e.getID() == WindowEvent.WINDOW_CLOSING && saved == true){
                    
                    EIWindow.setDefaultCloseOperation(DISPOSE_ON_CLOSE);                   
                    
                }else{
                    
                    EIWindow.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
                    JOptionPane.showMessageDialog(null, "Please Save Before Exiting");
                }
            }

        });

        DefaultTableModel tm = (DefaultTableModel)EIConTable1.getModel();   
        DefaultTableModel tm2 = (DefaultTableModel)EIConTable2.getModel();
        DefaultTableModel tm3 = (DefaultTableModel)EIConTable3.getModel();
        
        movedItems = new ArrayList<>();
        
        EIConInput1.addKeyListener(new KeyListener(){                   //action listener for first container input
            @Override
            public void keyTyped(KeyEvent e) {               
            }

            @Override
            public void keyPressed(KeyEvent e) {
                
                if(e.getKeyCode() == KeyEvent.VK_ENTER && saved == true){
                    
                    try{
                                 
                        chosen = true;
                        
                        container = Integer.valueOf(String.valueOf(EIConInput1.getText()));
                        
                        if(container != container2){     //catches opening same container as move into container
                            
                            ID = wh.getCustomerIdByContainer(container);

                            Object[][] inventory = in.getInventory(container);

                            tm.setRowCount(0);

                            for(Object[] i : inventory){

                                tm.addRow(new Object[]{i[0], i[1], i[2], i[3], i[4]});
                            }
                        }else{
                            JOptionPane.showMessageDialog(null, "Cannot choose same container");
                            
                            EIConInput1.setText("");
                        
                        }
                    }catch(NumberFormatException ex){
                        
                        JOptionPane.showMessageDialog(null, "Container Can Only Contain Numbers!");
                        EIConInput1.setText("");
                    }
                    
                }else if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE && saved == true){
                    
                    tm.setRowCount(0);
                    EIConInput1.setText("");
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {     
                
                if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
                    
                    EIConInput1.setText("");
                    tm.setRowCount(0);
                    chosen = false;
                    
                }
            }       
        });
        
        EIRelocatRadioBTN.addItemListener(new ItemListener(){           //item listener for radio button to add to another container
            @Override
            public void itemStateChanged(ItemEvent e) {
                
                if(e.getStateChange() == ItemEvent.SELECTED){
                    
                    EIWindow.setSize(1617, 716);
                    EIMoveBTN.setVisible(true);
                    EIRemoveBTN.setVisible(false);
                    EIAddBTN.setVisible(false);
                    EISaveBTN.setVisible(false);
                    EIWindow.setLocationRelativeTo(null);
                    
                }else if(e.getStateChange() == ItemEvent.DESELECTED  && saved == true){
                    
                    EIWindow.setSize(814, 716);
                    EIMoveBTN.setVisible(false);
                    EIRemoveBTN.setVisible(true);
                    EIAddBTN.setVisible(true);
                    EISaveBTN.setVisible(true);
                    EIWindow.setLocationRelativeTo(null);
                }
            }       
        }); 
        
        EIConInput2.addKeyListener(new KeyListener(){           //action listener for second container input
            
            @Override
            public void keyTyped(KeyEvent e) {               
            }

            @Override
            public void keyPressed(KeyEvent e) {
                
                if(e.getKeyCode() == KeyEvent.VK_ENTER && saved == true){
                    
                    try{
                        
                        container2 = Integer.valueOf(String.valueOf(EIConInput2.getText()));
                         
                        if(container != container2){
                            
                            boolean isCustomers = ID == wh.getCustomerIdByContainer(container2);
                            
                            if(!isCustomers){
                           
                                boolean conCheck = wh.checkContainer(container2);       //checks if container is already in use by the another customer

                                if(conCheck){           //if true container is not assigned in warehouse

                                    conCheck = wh.checkEmptiesContainer(container2);            //checks if container is in empties

                                    if(conCheck){           //if true container not in system

                                        int choice = JOptionPane.showConfirmDialog(null, "Container Is Not In System, Would You Like To Create A New Container And Add It To The Customer?", "Not In System", JOptionPane.YES_NO_OPTION);

                                        if(choice == JOptionPane.YES_OPTION){

                                            addNewContainerBTN.doClick();
                                            ANCONConInput.setText(EIConInput2.getText()); 
                                            ANCONConInput.setFocusable(false);
                                            ANCONNameInput.setFocusable(false);
                                            ANCONWareCusToggle.doClick();

                                            String name = cs.getCustomerNameByIdLastToFirst(ID);
                                            ANCONNameInput.setText(name);

                                            Object[] details = cs.getCustomerDetailsByID(ID);            //gets customer details

                                            name = String.valueOf(details[1] + " " + details[2] + " " + details[3]);     //formats for full name 

                                            int cons = wh.getNumberOfContainers(ID);

                                            ANCONDetailsOutput.setText("Name: '" + name + "' \n"                       //populates details into text area
                                                                        + "ID: '" + details[0] + "' \n"
                                                                            + "Telephone: '" + details[4] + "' \n"
                                                                                + "E-Mail: '" + details[5] + "' \n"
                                                                                        + "Address: \n'" + details[6] + "' \n"
                                                                                                + "Date Entered: '" + details[8] + "' \n"
                                                                                                    + "Number Of Containers: '" + cons + "'");
                                            movable = true;
                                        }
                                    }else{

                                        int choice = JOptionPane.showConfirmDialog(null, "Container Is Marked As Empty, Would You Like To Assign It To The Customer?", "Not Assigned", JOptionPane.YES_NO_OPTION);

                                        if(choice == JOptionPane.YES_OPTION){

                                            addNewContainerBTN.doClick();
                                            ANCONConInput.setText(EIConInput2.getText()); 
                                            ANCONConInput.setFocusable(false);
                                            ANCONNameInput.setFocusable(false);
                                            ANCONWareCusToggle.doClick();

                                            String name = cs.getCustomerNameByIdLastToFirst(ID);
                                            ANCONNameInput.setText(name);

                                            Object[] details = cs.getCustomerDetailsByID(ID);            //gets customer details

                                            name = String.valueOf(details[1] + " " + details[2] + " " + details[3]);     //formats for full name 

                                            int cons = wh.getNumberOfContainers(ID);

                                            ANCONDetailsOutput.setText("Name: '" + name + "' \n"                       //populates details into text area
                                                                        + "ID: '" + details[0] + "' \n"
                                                                            + "Telephone: '" + details[4] + "' \n"
                                                                                + "E-Mail: '" + details[5] + "' \n"
                                                                                        + "Address: \n'" + details[6] + "' \n"
                                                                                                + "Date Entered: '" + details[8] + "' \n"
                                                                                                    + "Number Of Containers: '" + cons + "'");

                                            movable = true;
                                        }                                
                                    }
                                }else{

                                    JOptionPane.showMessageDialog(null, "Container: '" + container2 + "'  Does Not Belong To Customer!");
                                    EIConInput2.setText("");
                                    EILockedLabel.setText("");
                                    movable = false;

                                }
                            }else{          //adds container inventory to table

                                Object[][] inventory = in.getInventory(container2);

                                tm2.setRowCount(0);

                                for(Object[] inv : inventory){

                                    tm2.addRow(new Object[]{inv[0], inv[1], inv[2], inv[3], inv[4]});

                                }

                                EILockedLabel.setText("Retrieved");
                                movable = true;

                                 
                                }
                            
                        }else{
                            
                            JOptionPane.showMessageDialog(null, "Cannot choose same container");
                            EIConInput2.setText("");      
                        }
                        
                    }catch(NumberFormatException ex){
                        
                        JOptionPane.showMessageDialog(null, "Container Can Only Contain Numbers!");
                        EIConInput2.setText("");
                    }
                    
                }else if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE && saved == true){
                    
                    tm2.setRowCount(0);
                    EIConInput2.setText("");
                    EILockedLabel.setText("");
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {    
                
            }       
        });
        
        EIAddBTN.addActionListener(new ActionListener(){            //adds entered item into container
            @Override
            public void actionPerformed(ActionEvent e) {
                
                if(!String.valueOf(tm3.getValueAt(0, 0)).isBlank() && chosen == true){
                    
                    try{

                        int numOfItems = Integer.valueOf(String.valueOf(tm3.getValueAt(0, 0)));         //stores entries
                        String item = String.valueOf(tm3.getValueAt(0, 1));
                        String description = String.valueOf(tm3.getValueAt(0, 2));
                        String condition = String.valueOf(tm3.getValueAt(0, 3));
                        String photoFile = String.valueOf(tm3.getValueAt(0, 4));

                        tm.addRow(new Object[]{numOfItems,item,description,condition,photoFile});           //adds to container table

                        in.addToInventory(ID, container, new int[]{numOfItems}, new String[]{item}, new String[]{description}, new String[]{condition}, new String[]{photoFile});   //adds to inventory

                        tm3.setRowCount(0);     //resets the add item table
                        tm3.setRowCount(1);
                        
                    }catch(NumberFormatException ex){
                        
                        JOptionPane.showMessageDialog(null, "Please Enter Item To Details To Add / Make Sure Item Number Contains Only Numbers");
                    }
                }
            }        
        });       
    }//GEN-LAST:event_editInventoryBTNActionPerformed

    /**
     * button to remove chosen item from inventory in edit inventory window
     * @param evt 
     */
    
    private void EIRemoveBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EIRemoveBTNActionPerformed
        
        DefaultTableModel tm = (DefaultTableModel)EIConTable1.getModel(); 
        
        int selected = EIConTable1.getSelectedRow();
        
        try{
            
            container = Integer.valueOf(String.valueOf(EIConInput1.getText()));
            ID = wh.getCustomerIdByContainer(container);

            if(selected != -1){

                int amount = Integer.valueOf(String.valueOf(tm.getValueAt(selected, 0)));

                if(amount > 1){

                    int getAmount = Integer.valueOf(JOptionPane.showInputDialog(null,"Please Select Amount To Remove", "Remove Item", JOptionPane.OK_CANCEL_OPTION));

                    if(getAmount > amount){

                        JOptionPane.showMessageDialog(null, "You Can Not Take More Than Is There!");

                    }else{
                        
                        String item = String.valueOf(tm.getValueAt(selected, 1));
                        String description = String.valueOf(tm.getValueAt(selected, 2));
                        String condition = String.valueOf(tm.getValueAt(selected, 3));
                        String photoFile = String.valueOf(tm.getValueAt(selected, 4));

                        int choice = JOptionPane.showConfirmDialog(null, "Are You Sure You Want To Remove '"+ getAmount + "' '" + item + "'","Remove Item", JOptionPane.YES_NO_OPTION);

                        if(choice == JOptionPane.YES_OPTION){  

                            in.removeItems(ID, container, getAmount, item, description, condition, photoFile);         //removes item then refreshes table
                            
                            Object[][] inventory = in.getInventory(container);
                        
                            tm.setRowCount(0);

                            for(Object[] i : inventory){

                            tm.addRow(new Object[]{i[0], i[1], i[2], i[3], i[4]});
                            
                            }                          
                        }
                    }
                }
            }else{
                
                JOptionPane.showMessageDialog(null, "Please Select Item To Remove");
            }
        }catch(NumberFormatException ex){
         
            JOptionPane.showMessageDialog(null, "Container Can Only Contain Numbers!");
        }
    }//GEN-LAST:event_EIRemoveBTNActionPerformed

    /**
     * button to move items to another container in edit inventory window
     * @param evt 
     */
    
    private void EIMoveBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EIMoveBTNActionPerformed
                       
        DefaultTableModel tm = (DefaultTableModel)EIConTable1.getModel(); 
        DefaultTableModel tm2 = (DefaultTableModel)EIConTable2.getModel();
        
        int selected = EIConTable1.getSelectedRow();
        
        boolean found = false;
        
        if(movable){
            try{

                container2 = Integer.valueOf(String.valueOf(EIConInput2.getText().trim()));

                ID = wh.getCustomerIdByContainer(container2);

                if(selected != -1){

                    int amount = Integer.valueOf(String.valueOf(tm.getValueAt(selected, 0)));
                    String item = String.valueOf(tm.getValueAt(selected, 1));
                    String description = String.valueOf(tm.getValueAt(selected, 2));
                    String condition = String.valueOf(tm.getValueAt(selected, 3));
                    String photoFile = String.valueOf(tm.getValueAt(selected, 4));
                                                       
                    if(amount > 1){                 //more than one item

                        int getAmount = Integer.valueOf(JOptionPane.showInputDialog(null,"Please Select Amount To Move", "Move Item", JOptionPane.OK_CANCEL_OPTION));

                        if(getAmount > amount || getAmount < 0){         

                            JOptionPane.showMessageDialog(null, "You Can Not Take More Or Less Than Is There!");

                        }else{

                            int choice = JOptionPane.showConfirmDialog(null, "Are You Sure You Want To Move '"+ getAmount + "' '" + item + "'","Move Item", JOptionPane.YES_NO_OPTION);

                            try{
                                
                                if(choice == JOptionPane.YES_OPTION){  

                                    if(getAmount != amount){            //changes table values in first container

                                        tm.setValueAt(amount - getAmount, selected, 0);     //updates amount in container 1

                                        for(int i = tm2.getRowCount() -1; i > -1; i--){            //searches if the item is already in container 2

                                            boolean rItem = item.matches(String.valueOf(tm2.getValueAt(i, 1)));
                                            boolean rDesc = description.matches(String.valueOf(tm2.getValueAt(i, 2)));
                                            boolean rCond = condition.matches(String.valueOf(tm2.getValueAt(i, 3)));
                                            boolean rPhoto = photoFile.matches(String.valueOf(tm2.getValueAt(i, 4)));

                                            if(rItem && rDesc && rCond && rPhoto){

                                                int addAmount = Integer.valueOf(String.valueOf(tm2.getValueAt(i, 0))) + getAmount;
                                                tm2.setValueAt(addAmount, i, 0);        //updates item count if found
                                                found = true;
                                            }                                                    
                                        }

                                        if(found == false){

                                            tm2.addRow(new Object[]{getAmount, item, description, condition, photoFile});      //adds item to new container  
                                        } 

                                        EILock();           //locks variables to prevent change before save
                                        movedItems.add(new Object[]{getAmount, item, description, condition, photoFile});           //adds item to list for action

                                    }else{      //entered all items to move

                                        for(int i = tm2.getRowCount() -1; i > -1; i--){            //searches if the item is already in container 2

                                            boolean rItem = item.matches(String.valueOf(tm2.getValueAt(i, 1)));
                                            boolean rDesc = description.matches(String.valueOf(tm2.getValueAt(i, 2)));
                                            boolean rCond = condition.matches(String.valueOf(tm2.getValueAt(i, 3)));
                                            boolean rPhoto = photoFile.matches(String.valueOf(tm2.getValueAt(i, 4)));

                                            if(rItem && rDesc && rCond && rPhoto){

                                                int addAmount = Integer.valueOf(String.valueOf(tm2.getValueAt(i, 0))) + getAmount;

                                                tm2.setValueAt(addAmount, i, 0);        //updates item count if found
                                                tm.removeRow(selected);
                                                found = true;                                           
                                            }                                                      
                                        }
                                        if(found == false){

                                             tm2.addRow(new Object[]{getAmount, item, description, condition, photoFile});      //adds item to new container 
                                             tm.removeRow(selected);
                                         } 

                                        EILock();           //locks variables to prevent change before save
                                        movedItems.add(new Object[]{getAmount, item, description, condition, photoFile});           //adds item to list for action
                                    }
                                }
                            }catch(NullPointerException ex){
                                
                                //catches cancel button
                            }
                        }
                        
                    }else{          //only one item in container
                        
                        for(int i = tm2.getRowCount() -1; i > -1; i--){            //searches if the item is already in container 2

                            boolean rItem = item.matches(String.valueOf(tm2.getValueAt(i, 1)));
                            boolean rDesc = description.matches(String.valueOf(tm2.getValueAt(i, 2)));
                            boolean rCond = condition.matches(String.valueOf(tm2.getValueAt(i, 3)));
                            boolean rPhoto = photoFile.matches(String.valueOf(tm2.getValueAt(i, 4)));

                            if(rItem && rDesc && rCond && rPhoto){

                                int addAmount = Integer.valueOf(String.valueOf(tm2.getValueAt(i, 0))) + amount;

                                tm2.setValueAt(addAmount, i, 0);        //updates item count if found
                                tm.removeRow(selected);
                                found = true;
                            }                                                      
                        }
                        if(found == false){

                            tm2.addRow(new Object[]{amount, item, description, condition, photoFile});      //adds item to new container 
                            tm.removeRow(selected);
                         }   
                        
                        EILock();           //locks variables to prevent change before save
                        movedItems.add(new Object[]{amount, item, description, condition, photoFile});           //adds item to list for action
                    }
                }else{

                    JOptionPane.showMessageDialog(null, "Please Select Item To Remove");
                }                
            }catch(NumberFormatException ex){

                JOptionPane.showMessageDialog(null, "Container Can Only Contain Numbers!");
            }
        }    
    }//GEN-LAST:event_EIMoveBTNActionPerformed

    /**
     * button to undo/remove the item added to the second container
     * @param evt 
     */
    
    private void EIUndoBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EIUndoBTNActionPerformed
               
        DefaultTableModel tm = (DefaultTableModel)EIConTable1.getModel();
        DefaultTableModel tm2 = (DefaultTableModel)EIConTable2.getModel();
        
        boolean found = false;
        int amount = 0;
        int selected = EIConTable2.getSelectedRow();
        int alreadyStored = in.getinventoryRowCount(container2);     //stops removing items already stored
        
     
        if(selected != -1){

            for(int i = tm.getRowCount() -1; i > -1; i--){            //searches if the item is already in there

                amount = Integer.valueOf(String.valueOf(tm.getValueAt(i, 0)));
                boolean rItem = String.valueOf(tm.getValueAt(i, 1)).matches(String.valueOf(tm2.getValueAt(selected, 1)));
                boolean rDesc = String.valueOf(tm.getValueAt(i, 2)).matches(String.valueOf(tm2.getValueAt(selected, 2)));
                boolean rCond = String.valueOf(tm.getValueAt(i, 3)).matches(String.valueOf(tm2.getValueAt(selected, 3)));
                boolean rPhoto = String.valueOf(tm.getValueAt(i, 4)).matches(String.valueOf(tm2.getValueAt(i, 4)));

                if(rItem && rDesc && rCond && rPhoto){

                    int addAmount = Integer.valueOf(String.valueOf(tm2.getValueAt(selected, 0))) + amount;
                    tm.setValueAt(addAmount, i, 0);                                                             //updates item count if found
                    found = true;
                    
                    for(int m = 0; m < movedItems.size(); m++){         //removes selected item from the list of moved items
                        
                        if(Arrays.equals(movedItems.get(m), new Object[]{tm2.getValueAt(selected, 0), tm2.getValueAt(selected, 1), tm2.getValueAt(selected, 2), tm2.getValueAt(selected, 3), tm2.getValueAt(selected, 4)})){

                            movedItems.remove(movedItems.size() -1);        
                        }
                    }
                }                   
            }

            if(found == false){             //adds all item information if item not already in container 1 table

                amount = Integer.valueOf(String.valueOf(tm2.getValueAt(selected, 0)));
                String item = String.valueOf(tm2.getValueAt(selected, 1));
                String description = String.valueOf(tm2.getValueAt(selected, 2));
                String condition = String.valueOf(tm2.getValueAt(selected, 3));
                String photoFile = String.valueOf(tm2.getValueAt(selected, 4));

                tm.addRow(new Object[]{amount, item, description, condition, photoFile});
                
                for(int m = 0; m < movedItems.size(); m++){                 //removes selected item from the list of moved items

                    if(Arrays.equals(movedItems.get(m), new Object[]{tm2.getValueAt(selected, 0), tm2.getValueAt(selected, 1), tm2.getValueAt(selected, 2), tm2.getValueAt(selected, 3), tm2.getValueAt(selected, 4)})){

                        movedItems.remove(movedItems.size() -1);        
                    }
                }                
            }

            tm2.removeRow(selected);

        }else{

            try{
                
                Object[] itemReturn = movedItems.get(movedItems.size()-1);          //gets last entered item

                for(int j = tm.getRowCount() -1; j > -1; j--){            //searches if the item is already in container 1

                    amount = Integer.valueOf(String.valueOf(itemReturn[0]));
                    boolean rItem = String.valueOf(itemReturn[1]).matches(String.valueOf(tm.getValueAt(j, 1)));
                    boolean rDesc = String.valueOf(itemReturn[2]).matches(String.valueOf(tm.getValueAt(j, 2)));
                    boolean rCond = String.valueOf(itemReturn[3]).matches(String.valueOf(tm.getValueAt(j, 3)));
                    boolean rPhoto = String.valueOf(itemReturn[4]).matches(String.valueOf(tm.getValueAt(j, 4)).replaceAll("\\\\", ""));

                    if(rItem && rDesc && rCond && rPhoto){

                        int addAmount = Integer.valueOf(String.valueOf(tm.getValueAt(j, 0))) + amount;
                        tm.setValueAt(addAmount, j, 0);                                                             //updates item count if found
                        found = true;

                        for(int k = tm2.getRowCount() -1; k > -1; k--){                      //removes amount from container 2

                            amount = Integer.valueOf(String.valueOf(itemReturn[0]));
                            rItem = String.valueOf(itemReturn[1]).matches(String.valueOf(tm2.getValueAt(k, 1)));
                            rDesc = String.valueOf(itemReturn[2]).matches(String.valueOf(tm2.getValueAt(k, 2)));
                            rCond = String.valueOf(itemReturn[3]).matches(String.valueOf(tm2.getValueAt(k, 3)));
                            rPhoto = String.valueOf(itemReturn[4]).matches(String.valueOf(tm2.getValueAt(k, 4)));

                            if(rItem && rDesc && rCond && rPhoto){

                                int takeAmount = Integer.valueOf(String.valueOf(tm2.getValueAt(k, 0))) - amount;
                                
                                if(takeAmount == 0){
                                    
                                    tm2.removeRow(k);           //if none left removes row
                                    
                                }else{
                                    
                                    tm2.setValueAt(takeAmount, k, 0);
                                }                                                              
                            }
                        }
                        
                        movedItems.remove(itemReturn);      //takes item out of array
                    }                   
                }
                if(found == false){             //adds all item information if item not already in container 1 table

                    amount = Integer.valueOf(String.valueOf(itemReturn[0]));
                    String item = String.valueOf(itemReturn[1]);
                    String description = String.valueOf(itemReturn[2]);
                    String condition = String.valueOf(itemReturn[3]);
                    String photoFile = String.valueOf(itemReturn[4]);
                    
                    for(int k = tm2.getRowCount() -1; k > -1; k--){                      //removes amount from container 2

                        amount = Integer.valueOf(String.valueOf(itemReturn[0]));
                        boolean rItem = item.matches(String.valueOf(tm2.getValueAt(k, 1)));
                        boolean rDesc = description.matches(String.valueOf(tm2.getValueAt(k, 2)));
                        boolean rCond = condition.matches(String.valueOf(tm2.getValueAt(k, 3)));
                        boolean rPhoto = photoFile.matches(String.valueOf(tm2.getValueAt(k, 4)));

                        if(rItem && rDesc && rCond && rPhoto){

                            int takeAmount = Integer.valueOf(String.valueOf(tm2.getValueAt(k, 0))) - amount;

                            if(takeAmount == 0){

                                tm2.removeRow(k);           //if none left removes row

                            }else{

                                tm2.setValueAt(takeAmount, k, 0);
                            }                                                              
                        }
                    }

                    tm.addRow(new Object[]{amount, item, description, condition, photoFile});
                    movedItems.remove(itemReturn);
                    
                }               
            }catch(IndexOutOfBoundsException ex){
                
                JOptionPane.showMessageDialog(null, "No Items Left To Undo");
            }
        }
      
        if(movedItems.isEmpty()){
            
             EIUnLock();
             movable = true;
        }
    }//GEN-LAST:event_EIUndoBTNActionPerformed

    /**
     * button to save changes made during item move to new container
     * @param evt 
     */
    
    private void EISaveBTN2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EISaveBTN2ActionPerformed

        if(movedItems.size() > 0){    
            
            ID = wh.getCustomerIdByContainer(container2);

            int[] numOfItems = new int[movedItems.size()];
            String[] items = new String[movedItems.size()];
            String[] descriptions = new String[movedItems.size()];
            String[] conditions = new String[movedItems.size()];
            String[] photoFiles = new String[movedItems.size()];
          
            for(int i = 0 ; i < movedItems.size(); i++){
                
                Object[] itemReturn = movedItems.get(i);
                
                int amount = Integer.valueOf(String.valueOf(itemReturn[0]));
                String item = String.valueOf(itemReturn[1]);
                String description = String.valueOf(itemReturn[2]);
                String condition = String.valueOf(itemReturn[3]);
                String photoFile = String.valueOf(itemReturn[4]);
                
                numOfItems[i] = amount;
                items[i] = item;
                descriptions[i] = description;
                conditions[i] = condition;
                photoFiles[i] = photoFile;
                
            }
            
            in.addToInventory(ID, container2, numOfItems, items, descriptions, conditions, photoFiles);            
            
            for(int i = 0; i < movedItems.size(); i++){

                Object[] itemReturn = movedItems.get(i);

                int amount = Integer.valueOf(String.valueOf(itemReturn[0]));
                String item = String.valueOf(itemReturn[1]);
                String description = String.valueOf(itemReturn[2]);
                String condition = String.valueOf(itemReturn[3]);
                String photoFile = String.valueOf(itemReturn[4]);

                in.removeItems(ID, container, amount, item, description, condition, photoFile);

           
            }

            wh.setContainerStatus(container2, "FULL");          //incase of new assigned container
            
            EIUnLock();   //unlocks variables to enable change of containers
            findUnInventoried();            //updates button on main screen to show number of containers with no items
            movedItems.clear();
            
        }else{
            
            JOptionPane.showMessageDialog(null, "No Items Have Been Moved!");
        }
    }//GEN-LAST:event_EISaveBTN2ActionPerformed

    /**
     * button view the container history 
    */
    
    private void viewCustomerHistoryBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewCustomerHistoryBTNActionPerformed
       
        VCHWindow.setVisible(true);
        VCHWindow.setLocationRelativeTo(null);
        
        VCHCustomerInput.addKeyListener(new KeyListener(){ 
            @Override
            public void keyTyped(KeyEvent e) {
                
            }

            @Override
            public void keyPressed(KeyEvent e) {
                
            }

            @Override
            public void keyReleased(KeyEvent e) {
                
     
                VCHComboBox.removeAllItems();

                String preFix = VCHCustomerInput.getText();

                ArrayList<String> foundNames = cs.findCustomers(preFix);

                foundNames.forEach(f -> VCHComboBox.addItem(f));

                String customer = String.valueOf(VCHComboBox.getSelectedItem());

                String[] nameSplit = customer.split(", ");                         
                        lastName = nameSplit[0];
                        firstName = nameSplit[1];

                        Object[] details = cs.getCustomerDetailsByName(firstName, lastName);            //gets customer details

                        String name = String.valueOf(details[1] + " " + details[2] + " " + details[3]);     //formats for full name   

                        ID = Integer.valueOf(String.valueOf(details[0]));

                        int cons = wh.getNumberOfContainers(ID);

                        VCHDetailsOutput.setText("Name: '" + name + "' \n"                       //populates details into text area
                                                    + "ID: '" + details[0] + "' \n"
                                                        + "Telephone: '" + details[4] + "' \n"
                                                            + "E-Mail: '" + details[5] + "' \n"
                                                                + "Address: \n'" + details[6] + "' \n"
                                                                    + "Date Entered: '" + details[8] + "'");


                        String customerActionLog = cs.getCustomerFileId(ID) + "\\actionLog.txt";

                try(BufferedReader br = new BufferedReader(new FileReader(customerActionLog))){         //shows log data of first customer result

                    String line = "";

                    while((line = br.readLine()) != null){

                        VCHLogArea.append(line + "\n");

                    }

                }catch (FileNotFoundException ex) {

                    Logger.getLogger(ContainerMain.class.getName()).log(Level.SEVERE, null, ex);

                }catch (IOException ex) {

                    Logger.getLogger(ContainerMain.class.getName()).log(Level.SEVERE, null, ex);
                }      
                
                if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE){                //resets entries when name input is deleted

                    VCHComboBox.removeAllItems();
                    VCHDetailsOutput.selectAll();
                    VCHDetailsOutput.replaceSelection("");
                    VCHLogArea.selectAll();
                    VCHLogArea.replaceSelection(""); 

                }

            }               
        });
           
        VCHComboBox.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e) {
                
                String customer = String.valueOf(VCHComboBox.getSelectedItem());
                
                if(!customer.isBlank()){
                
                    try{
                        String[] nameSplit = customer.split(", ");                         
                                lastName = nameSplit[0];
                                firstName = nameSplit[1];

                                Object[] details = cs.getCustomerDetailsByName(firstName, lastName);            //gets customer details

                                String name = String.valueOf(details[1] + " " + details[2] + " " + details[3]);     //formats for full name   

                                ID = Integer.valueOf(String.valueOf(details[0]));

                                int cons = wh.getNumberOfContainers(ID);

                                VCHDetailsOutput.setText("Name: '" + name + "' \n"                       //populates details into text area
                                                            + "ID: '" + details[0] + "' \n"
                                                                + "Telephone: '" + details[4] + "' \n"
                                                                    + "E-Mail: '" + details[5] + "' \n"
                                                                        + "Address: \n'" + details[6] + "' \n"
                                                                            + "Date Entered: '" + details[8] + "'");
                    }catch(ArrayIndexOutOfBoundsException ex){
                        
                        //do nothing 
                    }
                }
            }       
        });
    }//GEN-LAST:event_viewCustomerHistoryBTNActionPerformed

    /**
     * prints customer history in view customer history window
     * @param evt 
     */
    
    private void VCHPrintBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_VCHPrintBTNActionPerformed
        
        try {
            
            VCHLogArea.print();
            
        } catch (PrinterException ex) {
            
            Logger.getLogger(ContainerMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_VCHPrintBTNActionPerformed

    /**
     * opens the view container history window populates combo box and adds item listener
     * @param evt 
     */
    
    private void viewContainerHistoryBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewContainerHistoryBTNActionPerformed
        
        VCONHWindow.setVisible(true);
        VCONHWindow.setLocationRelativeTo(null);

        DefaultTableModel tm = (DefaultTableModel)VCONHTable.getModel();
        
        VCONHComboBox.removeAllItems();
        tm.setRowCount(0);          
        
        int[] cons = wh.getContainers();            //gets containers
        
        for(int c : cons){
            
            VCONHComboBox.addItem(String.valueOf(c));       ///adds to combo box
        }
        
        VCONHComboBox.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e) {
                
                try{
                    tm.setRowCount(0);

                    container = Integer.valueOf(String.valueOf(VCONHComboBox.getSelectedItem()));

                    Object[][]conHis = log.getContainerHistory(container);

                    for(Object[] ch : conHis){                      //populates tables

                        tm.addRow(new Object[]{ch[0], ch[1], ch[2]});
                    }    
                }catch(NumberFormatException ex){
                    
                    //do nothing as just a glitch when opening up window for second time
                }
            }       
        });
    }//GEN-LAST:event_viewContainerHistoryBTNActionPerformed

    /**
     * opens the view activity history window and populates combo boxes
     * @param evt 
     */
    
    private void viewActivityHistoryBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewActivityHistoryBTNActionPerformed
        
        VAHWindow.setVisible(true);
        VAHWindow.setLocationRelativeTo(null);
        
        DefaultTableModel tm = (DefaultTableModel)VAHTable.getModel();
        
        tm.setRowCount(0);
        
        VAHUserCombo.removeAllItems();
        VAHActionCombo.removeAllItems();
        VAHDateCombo.removeAllItems();
        
        ArrayList<String> users = log.getUsers();
        ArrayList<String> actions = log.getAction();
        ArrayList<String> dates =log.getDates();
        
        users.forEach(u -> VAHUserCombo.addItem(u));
        actions.forEach(a -> VAHActionCombo.addItem(a));
        dates.forEach(d -> VAHDateCombo.addItem(d));
        
        Object[][] actHis = log.getActionLog();
        
        for(Object[] ah : actHis){
            
            tm.addRow(new Object[]{ah[0], ah[1], ah[2], ah[3], ah[4], ah[5]});
        }
        
        
    }//GEN-LAST:event_viewActivityHistoryBTNActionPerformed

    /**
     * search button to search by chosen parameters in the view action history window
     * @param evt 
     */
    
    private void VAHSearchBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_VAHSearchBTNActionPerformed
       
        DefaultTableModel tm = (DefaultTableModel)VAHTable.getModel();
        
        boolean userTog = VAHUserTog.isSelected();
        boolean actionTog = VAHActionTog.isSelected();
        boolean dateTog = VAHDateTog.isSelected();
        
        if(userTog && actionTog && dateTog){            //search by all
            
            String getUser = String.valueOf(VAHUserCombo.getSelectedItem());
            String getAction = String.valueOf(VAHActionCombo.getSelectedItem());
            String getDate  = String.valueOf(VAHDateCombo.getSelectedItem());
            
            tm.setRowCount(0);
            
            Object[][] actHis = log.getActionHistoryByUserActionDate(getUser, getAction, getDate);
        
            for(Object[] ah : actHis){

                tm.addRow(new Object[]{ah[0], ah[1], ah[2], ah[3], ah[4], ah[5]});
            }  
            
        }else if(userTog && !actionTog && !dateTog){                                //search by user
                                    
            String getUser = String.valueOf(VAHUserCombo.getSelectedItem());
            
            tm.setRowCount(0);
            
            Object[][] actHis = log.getActionHistoryByUser(user);
        
            for(Object[] ah : actHis){

                tm.addRow(new Object[]{ah[0], ah[1], ah[2], ah[3], ah[4], ah[5]});
            } 
            
        }else if(userTog && actionTog && !dateTog){                                 //search by user and action
                        
            String getUser = String.valueOf(VAHUserCombo.getSelectedItem());
            String getAction = String.valueOf(VAHActionCombo.getSelectedItem());
            
            tm.setRowCount(0);
            
            Object[][] actHis = log.getActionHistoryByUserAction(getUser, getAction);
        
            for(Object[] ah : actHis){

                tm.addRow(new Object[]{ah[0], ah[1], ah[2], ah[3], ah[4], ah[5]});
            }  
                   
        }else if(userTog && !actionTog && dateTog){                                     //search by user and date
                        
            String getUser = String.valueOf(VAHUserCombo.getSelectedItem());
            String getDate  = String.valueOf(VAHDateCombo.getSelectedItem());
            
            tm.setRowCount(0);
            
            Object[][] actHis = log.getActionHistoryByUserDate(user, getDate);
        
            for(Object[] ah : actHis){

                tm.addRow(new Object[]{ah[0], ah[1], ah[2], ah[3], ah[4], ah[5]});
            }  
                    
        }else if(!userTog && actionTog && !dateTog){                                    //search by action
            
            String getAction = String.valueOf(VAHActionCombo.getSelectedItem());
            
            tm.setRowCount(0);
            
            Object[][] actHis = log.getActionHistoryByAction(getAction);
        
            for(Object[] ah : actHis){

                tm.addRow(new Object[]{ah[0], ah[1], ah[2], ah[3], ah[4], ah[5]});
            }  
                       
        }else if(!userTog && actionTog && dateTog){                                     //search by action and date
            
            String getAction = String.valueOf(VAHActionCombo.getSelectedItem());
            String getDate  = String.valueOf(VAHDateCombo.getSelectedItem());
            
            tm.setRowCount(0);
            
            Object[][] actHis = log.getActionHistoryByActionDate(getAction, getDate);
        
            for(Object[] ah : actHis){

                tm.addRow(new Object[]{ah[0], ah[1], ah[2], ah[3], ah[4], ah[5]});
            }  
        
        }else if(!userTog && !actionTog && !dateTog){                                   //get all data
            
            tm.setRowCount(0);
            
            Object[][] actHis = log.getActionLog();
        
            for(Object[] ah : actHis){

                tm.addRow(new Object[]{ah[0], ah[1], ah[2], ah[3], ah[4], ah[5]});
            }       
        }
    }//GEN-LAST:event_VAHSearchBTNActionPerformed

    /**
     * prints table for the view action history table
     * @param evt 
     */
    
    private void VAHPrintBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_VAHPrintBTNActionPerformed
        
        try {
            
            VAHTable.print();
            
        } catch (PrinterException ex) {
            
            Logger.getLogger(ContainerMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_VAHPrintBTNActionPerformed

    /**
     * opens the view analytic window
     * @param evt 
     */
    
    private void viewAnalyticsBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewAnalyticsBTNActionPerformed
        
        VAWindow.setVisible(true);
        VAWindow.setLocationRelativeTo(null);

    }//GEN-LAST:event_viewAnalyticsBTNActionPerformed

    /**
     * button to view longest customer in database in view analytic window
     * @param evt 
     */
    
    private void VALongestCustomerBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_VALongestCustomerBTNActionPerformed

        DefaultTableModel tm = (DefaultTableModel)VATable.getModel();
        String[] columns  = cs.getCustomerDBColumnNames(); 
        Object[][]cusDetails = cs.getLongestCustomer();
        
        tm.setColumnCount(0);
        tm.setRowCount(0);
            
  
        if(!VATableMax){

            VAWindow.setSize(1200, 639);
            VAScroll.setSize(VAScroll.getWidth() + 300, VAScroll.getHeight());

            VALongestCustomerBTN.setBounds(VAWindow.getWidth() - (45 + VALongestCustomerBTN.getWidth()), VALongestCustomerBTN.getY(), VALongestCustomerBTN.getWidth(), VALongestCustomerBTN.getHeight());
            VAMostContainersBTN.setBounds(VAWindow.getWidth() - (45 + VAMostContainersBTN.getWidth()), VAMostContainersBTN.getY(), VAMostContainersBTN.getWidth(), VAMostContainersBTN.getHeight());
            VAOldestContainerBTN.setBounds(VAWindow.getWidth() - (45 + VAOldestContainerBTN.getWidth()), VAOldestContainerBTN.getY(), VAOldestContainerBTN.getWidth(), VAOldestContainerBTN.getHeight());
            VAContainerAccoladesBTN.setBounds(VAWindow.getWidth() - (45 + VAContainerAccoladesBTN.getWidth()), VAContainerAccoladesBTN.getY(), VAContainerAccoladesBTN.getWidth(), VAContainerAccoladesBTN.getHeight());
            VAMostActiveUserrBTN.setBounds(VAWindow.getWidth() - (45 + VAMostActiveUserrBTN.getWidth()), VAMostActiveUserrBTN.getY(), VAMostActiveUserrBTN.getWidth(), VAMostActiveUserrBTN.getHeight());

        }
            for(String c : columns){

                tm.addColumn(c);
            }

            for(Object[] cus : cusDetails){

                tm.addRow(new Object[]{cus[0], cus[1], cus[2], cus[3], cus[4], cus[5], cus[6], cus[7], cus[8]}); 
            }

            VAWindow.setLocationRelativeTo(null);
            
            VATableMax = true;          //stops table expanding more than once
        
    }//GEN-LAST:event_VALongestCustomerBTNActionPerformed

    /**
     * button to view customer with most containers  in database in view analytic window
     * @param evt 
     */
    
    private void VAMostContainersBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_VAMostContainersBTNActionPerformed
        
        DefaultTableModel tm = (DefaultTableModel)VATable.getModel();
        String[] columns  = cs.getCustomerDBColumnNames(); 
        Object[][]cusDetails = log.getMostContainers();
  
        tm.setColumnCount(0);
        tm.setRowCount(0);
            
        
        if(!VATableMax){

            VAWindow.setSize(1200, 639);
            VAScroll.setSize(VAScroll.getWidth() + 300, VAScroll.getHeight());

            VALongestCustomerBTN.setBounds(VAWindow.getWidth() - (45 + VALongestCustomerBTN.getWidth()), VALongestCustomerBTN.getY(), VALongestCustomerBTN.getWidth(), VALongestCustomerBTN.getHeight());
            VAMostContainersBTN.setBounds(VAWindow.getWidth() - (45 + VAMostContainersBTN.getWidth()), VAMostContainersBTN.getY(), VAMostContainersBTN.getWidth(), VAMostContainersBTN.getHeight());
            VAOldestContainerBTN.setBounds(VAWindow.getWidth() - (45 + VAOldestContainerBTN.getWidth()), VAOldestContainerBTN.getY(), VAOldestContainerBTN.getWidth(), VAOldestContainerBTN.getHeight());
            VAContainerAccoladesBTN.setBounds(VAWindow.getWidth() - (45 + VAContainerAccoladesBTN.getWidth()), VAContainerAccoladesBTN.getY(), VAContainerAccoladesBTN.getWidth(), VAContainerAccoladesBTN.getHeight());
            VAMostActiveUserrBTN.setBounds(VAWindow.getWidth() - (45 + VAMostActiveUserrBTN.getWidth()), VAMostActiveUserrBTN.getY(), VAMostActiveUserrBTN.getWidth(), VAMostActiveUserrBTN.getHeight());

        }
            for(String c : columns){

                tm.addColumn(c);
            }

            for(Object[] cus : cusDetails){

                tm.addRow(new Object[]{cus[0], cus[1], cus[2], cus[3], cus[4], cus[5], cus[6], cus[7], cus[8]}); 
            }

            VAWindow.setLocationRelativeTo(null);
            
            VATableMax = true;          //stops table expanding more than once
        
    }//GEN-LAST:event_VAMostContainersBTNActionPerformed

    /**
     * button to get the oldest container in database in view analytic window
     * @param evt 
     */
    
    private void VAOldestContainerBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_VAOldestContainerBTNActionPerformed
                
        DefaultTableModel tm = (DefaultTableModel)VATable.getModel();
        String[] columns  = new String[]{"Container", "Action", "Created"};
        Object[][]conDetails = wh.getOldestContainer();
        
        tm.setColumnCount(0);
        tm.setRowCount(0);
            
  
        if(VATableMax){

            VAWindow.setSize(886, 639);
            VAScroll.setSize(VAScroll.getWidth() - 300, VAScroll.getHeight());

            VALongestCustomerBTN.setBounds(VAWindow.getWidth() - (45 + VALongestCustomerBTN.getWidth()), VALongestCustomerBTN.getY(), VALongestCustomerBTN.getWidth(), VALongestCustomerBTN.getHeight());
            VAMostContainersBTN.setBounds(VAWindow.getWidth() - (45 + VAMostContainersBTN.getWidth()), VAMostContainersBTN.getY(), VAMostContainersBTN.getWidth(), VAMostContainersBTN.getHeight());
            VAOldestContainerBTN.setBounds(VAWindow.getWidth() - (45 + VAOldestContainerBTN.getWidth()), VAOldestContainerBTN.getY(), VAOldestContainerBTN.getWidth(), VAOldestContainerBTN.getHeight());
            VAContainerAccoladesBTN.setBounds(VAWindow.getWidth() - (45 + VAContainerAccoladesBTN.getWidth()), VAContainerAccoladesBTN.getY(), VAContainerAccoladesBTN.getWidth(), VAContainerAccoladesBTN.getHeight());
            VAMostActiveUserrBTN.setBounds(VAWindow.getWidth() - (45 + VAMostActiveUserrBTN.getWidth()), VAMostActiveUserrBTN.getY(), VAMostActiveUserrBTN.getWidth(), VAMostActiveUserrBTN.getHeight());
        
        }
            for(String c : columns){

                tm.addColumn(c);
            }

            for(Object[] cus : conDetails){

                tm.addRow(new Object[]{cus[0], cus[1], cus[2]}); 
            }
            
            VAWindow.setLocationRelativeTo(null);

            VATableMax = false;          //stops table shrinking more than once
     
    }//GEN-LAST:event_VAOldestContainerBTNActionPerformed

    /**
     * button to view container accolades in view analytic window
     * @param evt 
     */
    
    private void VAContainerAccoladesBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_VAContainerAccoladesBTNActionPerformed
                       
        DefaultTableModel tm = (DefaultTableModel)VATable.getModel();
        String[] columns  = {"Container", "Owners", "Moved"};
        Object[][]conDetails = wh.getContainerAccolades();
        
        tm.setColumnCount(0);
        tm.setRowCount(0);
            
  
        if(VATableMax){

            VAWindow.setSize(886, 639);
            VAScroll.setSize(VAScroll.getWidth() - 300, VAScroll.getHeight());

            VALongestCustomerBTN.setBounds(VAWindow.getWidth() - (45 + VALongestCustomerBTN.getWidth()), VALongestCustomerBTN.getY(), VALongestCustomerBTN.getWidth(), VALongestCustomerBTN.getHeight());
            VAMostContainersBTN.setBounds(VAWindow.getWidth() - (45 + VAMostContainersBTN.getWidth()), VAMostContainersBTN.getY(), VAMostContainersBTN.getWidth(), VAMostContainersBTN.getHeight());
            VAOldestContainerBTN.setBounds(VAWindow.getWidth() - (45 + VAOldestContainerBTN.getWidth()), VAOldestContainerBTN.getY(), VAOldestContainerBTN.getWidth(), VAOldestContainerBTN.getHeight());
            VAContainerAccoladesBTN.setBounds(VAWindow.getWidth() - (45 + VAContainerAccoladesBTN.getWidth()), VAContainerAccoladesBTN.getY(), VAContainerAccoladesBTN.getWidth(), VAContainerAccoladesBTN.getHeight());
            VAMostActiveUserrBTN.setBounds(VAWindow.getWidth() - (45 + VAMostActiveUserrBTN.getWidth()), VAMostActiveUserrBTN.getY(), VAMostActiveUserrBTN.getWidth(), VAMostActiveUserrBTN.getHeight());
        
        }
            for(String c : columns){

                tm.addColumn(c);
            }

            for(Object[] cus : conDetails){

                tm.addRow(new Object[]{cus[0], cus[1], cus[2]}); 
            }
            
            VAWindow.setLocationRelativeTo(null);

            VATableMax = false;          //stops table shrinking more than once
     
    }//GEN-LAST:event_VAContainerAccoladesBTNActionPerformed

    /**
     * button to get the counts of user actions (view analytic window)
     * @param evt 
     */
    
    private void VAMostActiveUserrBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_VAMostActiveUserrBTNActionPerformed
       
        DefaultTableModel tm = (DefaultTableModel)VATable.getModel();
        String[] columns  = {"User", "Entries", "Customers Added", "Containers Created", "Containers Moved", "Container Emptied", "Inventories Added", "Items Updated", "Items Removed", "Customers Removed"};
        
        tm.setColumnCount(0);
        tm.setRowCount(0);
            
  
        if(!VATableMax){

            VAWindow.setSize(1200, 639);
            VAScroll.setSize(VAScroll.getWidth() + 300, VAScroll.getHeight());

            VALongestCustomerBTN.setBounds(VAWindow.getWidth() - (45 + VALongestCustomerBTN.getWidth()), VALongestCustomerBTN.getY(), VALongestCustomerBTN.getWidth(), VALongestCustomerBTN.getHeight());
            VAMostContainersBTN.setBounds(VAWindow.getWidth() - (45 + VAMostContainersBTN.getWidth()), VAMostContainersBTN.getY(), VAMostContainersBTN.getWidth(), VAMostContainersBTN.getHeight());
            VAOldestContainerBTN.setBounds(VAWindow.getWidth() - (45 + VAOldestContainerBTN.getWidth()), VAOldestContainerBTN.getY(), VAOldestContainerBTN.getWidth(), VAOldestContainerBTN.getHeight());
            VAContainerAccoladesBTN.setBounds(VAWindow.getWidth() - (45 + VAContainerAccoladesBTN.getWidth()), VAContainerAccoladesBTN.getY(), VAContainerAccoladesBTN.getWidth(), VAContainerAccoladesBTN.getHeight());
            VAMostActiveUserrBTN.setBounds(VAWindow.getWidth() - (45 + VAMostActiveUserrBTN.getWidth()), VAMostActiveUserrBTN.getY(), VAMostActiveUserrBTN.getWidth(), VAMostActiveUserrBTN.getHeight());

        }
            for(String c : columns){

                tm.addColumn(c);
            }

            ArrayList<String> users = log.getUsers();
            
            for(int i = 0; i < users.size(); i++){
                
                Object[][] userData = log.getUserData(users.get(i));
                
                for(Object[] u : userData){
                    
                    tm.addRow(new Object[]{u[0], u[1], u[2], u[3], u[4], u[5], u[6], u[7], u[8], u[9]});
                }
            }
            
            VAWindow.setLocationRelativeTo(null);
            
            VATableMax = true;           //stops table expanding more than once
            
    }//GEN-LAST:event_VAMostActiveUserrBTNActionPerformed

    /**
     * first time log in for when the program is first ran
     * @param evt 
     */
    
    private void FTLISubmitBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FTLISubmitBTNActionPerformed

        
        String userName = FTLIUsernameInput.getText();
        
        char[] password = FTLIPasswordInput.getPassword();
        
        char[] rePassword = FTLIRePasswordInput.getPassword();

        if(Arrays.equals(password, rePassword)){
            
            if(!userName.isBlank()){
                
                li.addNewUser(userName, password, "ADMIN");
                
                LIWindow.setVisible(true);
                LIWindow.setLocationRelativeTo(null);
                
                for(int i= 0; i < password.length; i++){
                
                    password[i] = '*';
                    rePassword[i] = '*';
                }
                 
                 FTLIWindow.setFocusable(false);
                
            }else{
                
                JOptionPane.showMessageDialog(null, "UserName Cannot Be Blank");
            }
            
        }else{
            
            JOptionPane.showMessageDialog(null, "Passwords Do Not Match!");
        }
        
    }//GEN-LAST:event_FTLISubmitBTNActionPerformed

    /**
     * log in window for program
     * @param evt 
     */
    
    private void LILogInBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LILogInBTNActionPerformed
        
        String userName = LIUserNameInput.getText();
        
        char[] password = LIPasswordInput.getPassword();
        
        if(li.checkuser(userName, password)){
            
            ContainerMain cm = new ContainerMain();
 
            user = userName;
                   
            admin = li.checkAdmin(userName);
            
            if(admin == false){
            
                cm.TopMainBTNPanel.setVisible(false);    //restricts activity for admin only
                
            }
            
            for(int i= 0; i < password.length; i++){
                
                password[i] = '*';
            }
                       
            LIWindow.dispose(); 
            FTLIWindow.dispose();
            
            if(log.isWarehouseManaged()){           //checks if warehouse has been set up 
                
                cm.setVisible(true);
                
            }else{
                
                manageWarehouseBTN.doClick();
                
                WMWindow.addWindowListener(new WindowAdapter(){
                    
                    @Override
                    public void windowClosing(WindowEvent e) {          //stops window from closing if it has not been completed
               
                        if(e.getID() == WindowEvent.WINDOW_CLOSING){

                            WMWindow.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

                        } 
                    }                
                });
            }
        }else{
            
            JOptionPane.showMessageDialog(null, "Password Or Username Incorrect!");
        }       
    }//GEN-LAST:event_LILogInBTNActionPerformed

    /**
     * opens up first time log in window for the add new user button
     * @param evt 
     */
    
    private void addNewUserBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewUserBTNActionPerformed
        
        FTLIWindow.setVisible(true);
        FTLIWindow.setLocationRelativeTo(null);
        
        FTLIWindow.setDefaultCloseOperation(DISPOSE_ON_CLOSE);      //stops exiting of program
        LIWindow.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
    }//GEN-LAST:event_addNewUserBTNActionPerformed

    /**
     * opens the remove user window
     * @param evt 
     */
    
    private void removeUserBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeUserBTNActionPerformed
        
        RUWindow.setVisible(true);
        RUWindow.setLocationRelativeTo(null);
        
        RUUserComboBox.removeAllItems();
        RUPasswordInput.setText("");
        
        ArrayList<String> users = li.getUsers();
        
        users.forEach(u -> RUUserComboBox.addItem(u));
        
    }//GEN-LAST:event_removeUserBTNActionPerformed

    /**
     * button to remove selected user
     * @param evt 
     */
    
    private void RURemoveBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RURemoveBTNActionPerformed
                
        String rUser = String.valueOf(RUUserComboBox.getSelectedItem());
        char[] password = RUPasswordInput.getPassword();
        
        if(li.checkuser(user, password)){
            
            int choice = JOptionPane.showConfirmDialog(null, "Are You Sure You Want To Remove '" + rUser + "' This cannot Be Undone!","Remove User", JOptionPane.YES_NO_OPTION);
            
            if(choice == JOptionPane.YES_OPTION){
                
                li.removeUser(rUser);
            }
        }else{
            
            JOptionPane.showMessageDialog(null, "Please Enter Correct Password!");
            RUPasswordInput.setText("");
            RUPasswordInput.requestFocus();
        }
        
    }//GEN-LAST:event_RURemoveBTNActionPerformed

    /**
     * opens change password window
     * @param evt 
     */
    
    private void changePasswordsBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changePasswordsBTNActionPerformed
        
        CPWindow.setVisible(true);
        CPWindow.setLocationRelativeTo(null);
        
    }//GEN-LAST:event_changePasswordsBTNActionPerformed

    /**
     * button to change the password of the entered user
     * @param evt 
     */
    
    private void CPChangeBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CPChangeBTNActionPerformed
        
        changePasswordLabel.setVisible(false);
        String cpUser = CPUsernameInput.getText();
        char[] password = CPPasswordInput.getPassword();
        char[] rePassword = CPRePasswordInput.getPassword();
   
        if(!changePassword){
            if(Arrays.equals(password, rePassword)){

                if(li.checkuser(cpUser, password)){

                    int choice = JOptionPane.showConfirmDialog(null, "Are You Sure You Want To Change '" + cpUser + "''s Password This cannot Be Undone!", "Change Password", JOptionPane.YES_NO_OPTION);

                    if(choice == JOptionPane.YES_OPTION){
         
                        changePasswordLabel.setVisible(true);
                        CPPasswordInput.setText("");
                        CPRePasswordInput.setText("");
                        CPUsernameInput.setEnabled(false);
                        changePassword = true;
                        
                    }
                }else{

                    JOptionPane.showMessageDialog(null, "Username Does Not Match Password!");
                }
            }else{

                JOptionPane.showMessageDialog(null, "Passwords do Not Match!");
            }
        }else{
            
            char[] newPassword = CPPasswordInput.getPassword();
            char[] newRePassword = CPRePasswordInput.getPassword();

            String access = li.checkAdmin(cpUser) ? "ADMIN" : "STANDARD";
            
            if(Arrays.equals(newPassword , newRePassword)){
            
                li.removeUser(cpUser);
                li.addNewUser(cpUser, newPassword, access);
                JOptionPane.showMessageDialog(null, "Password Changed");
                CPWindow.dispose();
                changePassword = false;
                
            }else{

                JOptionPane.showMessageDialog(null, "Passwords do Not Match!");
            }
        } 
    }//GEN-LAST:event_CPChangeBTNActionPerformed

    /**
     * button to open up the remove container window
     * @param evt 
     */
    
    private void removeContainerBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeContainerBTNActionPerformed
        
        RCONWindow.setVisible(true);
        RCONWindow.setLocationRelativeTo(null);
        
        
    }//GEN-LAST:event_removeContainerBTNActionPerformed

    /**
     * button to remove selected container
     * @param evt 
     */
    
    private void RCONRemoveBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RCONRemoveBTNActionPerformed
       
        container = Integer.valueOf(String.valueOf(RCONContainerInput.getText()));
        
        try {
           
            int choice = JOptionPane.showConfirmDialog(null, "Are You Sure You Want To Remove Container: '" + container + "' This cannot Be Undone!", "Remove Container", JOptionPane.YES_NO_OPTION);

                if(choice == JOptionPane.YES_OPTION){

                    wh.removeContainer(container);
                }
                
        }catch(NumberFormatException ex){
            
            JOptionPane.showMessageDialog(null, "Please Enter Only Numbers");
        }
        
    }//GEN-LAST:event_RCONRemoveBTNActionPerformed

    /**
     * button to open up the manage warehouse window
     * @param evt 
     */
    
    private void manageWarehouseBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageWarehouseBTNActionPerformed
        
        WMWindow.setVisible(true);
        WMWindow.setLocationRelativeTo(null);
        
        DefaultTableModel tm = (DefaultTableModel)WMConTable.getModel();
        DefaultTableModel tm2 = (DefaultTableModel)WMLocationsTable.getModel();
        
        if(log.isWarehouseManaged()){               
            
            MWAddToExistingRadio.setVisible(true);
            MWAddToExistingRadio.setSelected(true);
            MWAddToExistingRadio.setEnabled(false);
            
        }else{
            
            MWAddToExistingRadio.setVisible(false);         //removes add to existing toggle if the warehouse has not already been set
        }
        
        WMGenerateConBTN.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                
                tm.setRowCount(0);                                                  //generates container numbers
                int from = Integer.valueOf(String.valueOf(WMConFromInput.getText()));
                int to = Integer.valueOf(String.valueOf(WMConToInput.getText()));
                
                ArrayList<Integer> existingCons  = (ArrayList<Integer>) Arrays.stream(wh.getContainers()).boxed().collect(Collectors.toList());
                
                boolean inSystem = false;
                
                for(int ex = from ; ex < to ; ex++){        //checks entered number against all containers in syystem
                    
                    if(existingCons.contains(ex)){
                        
                        inSystem = true;
                        break;
                    }
                }
                
                if(inSystem == false){
                    
                    for(int i = from; i <= to; i++){

                        tm.addRow(new Object[]{i});
                    }
                    
                }else{
                    
                    JOptionPane.showMessageDialog(null, "Container Numbers(s) Already Asigned");
                }
            }   
        });
        
        WMLocationGenBTN.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                
                tm2.setRowCount(0); 
                
                int rows = Integer.valueOf(String.valueOf(WMRowsInput.getText()));
                
                if(rows > 26 || rows < 1){
                    
                    JOptionPane.showMessageDialog(null, "You Have Chosen More Rows Than Letters Available Or Entered Zero Or Minus Rows");
                    WMRowsInput.setText("");
                    
                }else{
                    
                    int height = Integer.valueOf(String.valueOf(WMHeightInput.getText()));
                    int columns  = Integer.valueOf(String.valueOf(WMColumnsInput.getText()));

                    //if adding to existing warehouse
                    
                    
                    char letterFrom = MWAddToExistingRadio.isSelected() ? wh.getLastAisleLetter() : 'A';       //gets next letter after last letter entered if selected
                    
                    if(MWAddToExistingRadio.isSelected()){          //   goes to next letter only if adding extra aisles
                        
                        letterFrom += 1;
                    }
                    
                    char letterTo = MWAddToExistingRadio.isSelected() ? (char)(letterFrom + rows) : (char)(rows + 'A');     //adjusts letter to from last letter if selected
                    int limit = MWAddToExistingRadio.isSelected() ? (int)(letterTo - 'A') : 26;         //stops going over number of letters available

                    if(limit < 27){                   //appends letter as aisle id

                        for(char a = letterFrom; a < letterTo; a++){

                            for(int c = 1; c <= columns; c++){

                                for(int h = 1; h <= height; h++){

                                    String newLocation = ""+a+"-"+c+"-("+h+")";
                                    tm2.addRow(new Object[]{newLocation});
                                }
                            }
                        }

                    }else{

                       JOptionPane.showMessageDialog(null, "You Have Chosen More Rows Than Letters Available");

                    }                
                }  
            }
        }); 
        
        WMCompleteBTN.addActionListener(new ActionListener(){
                        
            @Override
            public void actionPerformed(ActionEvent e) {
                
                WMWindow.addWindowListener(new WindowAdapter(){
                    
                    @Override
                    public void windowClosing(WindowEvent e) {          //stops window from closing if it has not been completed

                        if(e.getID() == WindowEvent.WINDOW_CLOSING){

                            WMWindow.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

                        } 
                    }                
                });
                
                int conCount = WMConTable.getRowCount();
                int locCount = WMLocationsTable.getRowCount() ;
                
                if(locCount > 0){
                                       
                    WMConProgressBar.setMaximum(conCount);
                                     
                    new Timer(10, new ActionListener(){
                        
                        int count = 0;
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            
                            if(count < conCount){
                                
                                wh.addNewContainerToEmpties(Integer.valueOf(String.valueOf(tm.getValueAt(count, 0))));

                                count++;
                                WMConProgressBar.setValue(count);
                                
                            }else{
                                                               
                                ((Timer)e.getSource()).stop();
                            }
                        }
                   }).start();
                }
                                
                if(locCount > 0){
                                       
                    WMLocProgressBar.setMaximum(locCount);
                                     
                    new Timer(10, new ActionListener(){
                        
                        int count = 0;
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            
                            if(count < locCount){
                                
                                wh.addLocation(String.valueOf(tm2.getValueAt(count, 0)));

                                count++;
                                WMLocProgressBar.setValue(count);
                                
                                
                            }else{
                                                               
                                ((Timer)e.getSource()).stop();

                                            
                                boolean selected = MWAddToExistingRadio.isSelected();           //variable to change action history log action

                                if(selected){

                                    log.logAction(user, "Company", 0, "WAREHOUSE UPDATED", "NEW AILSE(S) ADDED", timeStamp());

                                }else{

                                    log.logAction(user, "Company", 0, "WAREHOUSE SET UP", "COMPLETE", timeStamp());
                                }

                                WMWindow.dispose();

                                if(!MWAddToExistingRadio.isVisible()){              //only opens up new window if its the first time using warehouse manager
                                    
                                    ContainerMain cm = new ContainerMain();
                                    cm.setVisible(true);
                                    cm.setLocationRelativeTo(null);
                                }

                            }
                        }
                   }).start();
                }               
            }           
        });         
    }//GEN-LAST:event_manageWarehouseBTNActionPerformed

    private void WMCompleteBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WMCompleteBTNActionPerformed
       //SEE ABOVE
    }//GEN-LAST:event_WMCompleteBTNActionPerformed

    private void WMLocationGenBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WMLocationGenBTNActionPerformed
       //SEE ABOVE
    }//GEN-LAST:event_WMLocationGenBTNActionPerformed

    private void WMGenerateConBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WMGenerateConBTNActionPerformed
       //SEE ABOVE
    }//GEN-LAST:event_WMGenerateConBTNActionPerformed

    private void EIAddBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EIAddBTNActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_EIAddBTNActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ContainerMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ContainerMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ContainerMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ContainerMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                
                LogIn li = new LogIn();
                ContainerMain cm = new ContainerMain();
                   
                if(li.checkFirstTime()){                    //checks for the first time running the program
                    
                    cm.FTLIWindow.setVisible(true);
                    cm.FTLIWindow.setLocationRelativeTo(null);

                }else{
                    
                    cm.LIWindow.setVisible(true);
                    cm.LIWindow.setLocationRelativeTo(null);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ANCAddBTN;
    private javax.swing.JTextArea ANCAddressField;
    private javax.swing.JLabel ANCAddressLabel;
    private javax.swing.JScrollPane ANCAddressScroll;
    private javax.swing.JButton ANCClearBTN;
    private javax.swing.JTable ANCConLocTable;
    private javax.swing.JTextField ANCContainerField;
    private javax.swing.JLabel ANCContainerLabel;
    private javax.swing.JScrollPane ANCContainerScroll;
    private javax.swing.JTextField ANCEmailField;
    private javax.swing.JLabel ANCEmailLabel;
    private javax.swing.JTextField ANCFirstNameField;
    private javax.swing.JLabel ANCFirstNameLabel;
    private javax.swing.JTextField ANCLastNameField;
    private javax.swing.JLabel ANCLastNameLabel;
    private javax.swing.JComboBox<String> ANCLocComboBox;
    private javax.swing.JTextField ANCLocationField;
    private javax.swing.JLabel ANCLocationLabel;
    private javax.swing.JButton ANCNextBTN;
    private javax.swing.JButton ANCONAddBTN;
    private javax.swing.JComboBox<String> ANCONComboBox;
    private javax.swing.JTextField ANCONConInput;
    private javax.swing.JTextArea ANCONDetailsOutput;
    private javax.swing.JScrollPane ANCONDetailsScroll;
    private javax.swing.JComboBox<String> ANCONLocComboBox;
    private javax.swing.JTextField ANCONLocInput;
    private javax.swing.JLabel ANCONLocationLabel;
    private javax.swing.JTextField ANCONNameInput;
    private javax.swing.JPanel ANCONPanel;
    private javax.swing.JToggleButton ANCONWareCusToggle;
    private javax.swing.JFrame ANCONWindow;
    private javax.swing.JPanel ANCPanel;
    private javax.swing.JButton ANCRemoveBTN;
    private javax.swing.JTextField ANCTelephoneField;
    private javax.swing.JLabel ANCTelephoneLabel;
    private javax.swing.JComboBox<String> ANCTitleComboBox;
    private javax.swing.JLabel ANCTitleLabel;
    private javax.swing.JFrame ANCWindow;
    private javax.swing.JButton CNIAddNewItemBTN;
    private javax.swing.JLabel CNIAddressLabel;
    private javax.swing.JTextArea CNIAddressOutput;
    private javax.swing.JScrollPane CNIAddressOutputFieldScroll;
    private javax.swing.JLabel CNIChosenContainerLabel;
    private javax.swing.JComboBox<String> CNIContainerComboBox;
    private javax.swing.JLabel CNIContainerLabel;
    private javax.swing.JLabel CNIContainersLabel;
    private javax.swing.JLabel CNIEmailLabel;
    private javax.swing.JTextField CNIEmailOutput;
    private javax.swing.JButton CNIFinishBTN;
    private javax.swing.JLabel CNIIdLabel;
    private javax.swing.JTextField CNIIdOutput;
    private javax.swing.JTable CNIInventoryTable;
    private javax.swing.JScrollPane CNIInventoryTableScroll;
    private javax.swing.JLabel CNINameLabel;
    private javax.swing.JTextField CNINameOutput;
    private javax.swing.JPanel CNIPanel;
    private javax.swing.JButton CNIRemoveItemBTN;
    private javax.swing.JButton CNISaveBTN;
    private javax.swing.JLabel CNITelephoneLabel;
    private javax.swing.JTextField CNITelephoneOutput;
    private javax.swing.JFrame CNIWindow;
    private javax.swing.JButton CPChangeBTN;
    private javax.swing.JPanel CPPanel;
    private javax.swing.JPasswordField CPPasswordInput;
    private javax.swing.JLabel CPPasswordLabel;
    private javax.swing.JPasswordField CPRePasswordInput;
    private javax.swing.JLabel CPRePasswordLabel;
    private javax.swing.JTextField CPUsernameInput;
    private javax.swing.JLabel CPUsernameLabel;
    private javax.swing.JFrame CPWindow;
    private javax.swing.JTextField ECContainerInput;
    private javax.swing.JTextArea ECDAddressChangeInput;
    private javax.swing.JScrollPane ECDAddressChangeScroll;
    private javax.swing.JLabel ECDAddressLabel;
    private javax.swing.JTextArea ECDAddressOutput;
    private javax.swing.JScrollPane ECDAddressOutputScroll;
    private javax.swing.JTextField ECDEmailChangeInput;
    private javax.swing.JLabel ECDEmailLabel;
    private javax.swing.JLabel ECDEmailOutput;
    private javax.swing.JButton ECDFindBTN;
    private javax.swing.JLabel ECDFindIDLabel;
    private javax.swing.JLabel ECDFindLastNameLabel;
    private javax.swing.JComboBox<String> ECDFoundComboBox;
    private javax.swing.JTextField ECDLastNameInput;
    private javax.swing.JTextField ECDNameChangeInput;
    private javax.swing.JLabel ECDNameLabel;
    private javax.swing.JLabel ECDNameOutput;
    private javax.swing.JPanel ECDPanel;
    private javax.swing.JButton ECDSaveBTN;
    private javax.swing.JButton ECDSearchBTN;
    private javax.swing.JTextField ECDTelephoneChangeInput;
    private javax.swing.JLabel ECDTelephoneLabel;
    private javax.swing.JLabel ECDTelephoneOutput;
    private javax.swing.JComboBox<String> ECDTitleComboBox;
    private javax.swing.JLabel ECDTitleLabel;
    private javax.swing.JFrame ECDWindow;
    private javax.swing.JTextField ECDiDInput;
    private javax.swing.JPanel ECPanel;
    private javax.swing.JFrame ECWindow;
    private javax.swing.JPanel EI1Panel;
    private javax.swing.JButton EIAddBTN;
    private javax.swing.JTextField EIConInput1;
    private javax.swing.JTextField EIConInput2;
    private javax.swing.JLabel EIConLabel1;
    private javax.swing.JLabel EIConLabel2;
    private javax.swing.JTable EIConTable1;
    private javax.swing.JTable EIConTable2;
    private javax.swing.JTable EIConTable3;
    private javax.swing.JScrollPane EIConTableScroll1;
    private javax.swing.JScrollPane EIConTableScroll2;
    private javax.swing.JScrollPane EIConTableScroll3;
    private javax.swing.JLabel EILockedLabel;
    private javax.swing.JButton EIMoveBTN;
    private javax.swing.JRadioButton EIRelocatRadioBTN;
    private javax.swing.JButton EIRemoveBTN;
    private javax.swing.JButton EISaveBTN;
    private javax.swing.JButton EISaveBTN2;
    private javax.swing.JButton EIUndoBTN;
    private javax.swing.JFrame EIWindow;
    private javax.swing.JPanel FTLIPanel;
    private javax.swing.JPasswordField FTLIPasswordInput;
    private javax.swing.JLabel FTLIPasswordLabel;
    private javax.swing.JPasswordField FTLIRePasswordInput;
    private javax.swing.JLabel FTLIRePasswordLabel;
    private javax.swing.JButton FTLISubmitBTN;
    private javax.swing.JLabel FTLIUserNameLabel;
    private javax.swing.JTextField FTLIUsernameInput;
    private javax.swing.JFrame FTLIWindow;
    private javax.swing.JButton LILogInBTN;
    private javax.swing.JPanel LIPanel;
    private javax.swing.JPasswordField LIPasswordInput;
    private javax.swing.JLabel LIPasswordLabel;
    private javax.swing.JTextField LIUserNameInput;
    private javax.swing.JFrame LIWindow;
    private javax.swing.JLabel LIusernameLabel;
    private javax.swing.JFrame MCWindow;
    private javax.swing.JRadioButton MWAddToExistingRadio;
    private javax.swing.JLabel MainLogo;
    private javax.swing.JPanel MainPanel;
    private javax.swing.JButton PLAddAllBTN;
    private javax.swing.JButton PLAddComBTN;
    private javax.swing.JButton PLAddItemBTN;
    private javax.swing.JComboBox<String> PLConComboBox;
    private javax.swing.JTextArea PLDetailsOutput;
    private javax.swing.JButton PLFinalizeBTN;
    private javax.swing.JLabel PLIDLabel;
    private javax.swing.JScrollPane PLInventoryScroll;
    private javax.swing.JTable PLInventoryTable;
    private javax.swing.JComboBox<String> PLNameComboBox;
    private javax.swing.JTextField PLNameInput;
    private javax.swing.JPanel PLPanel;
    private javax.swing.JTable PLPickedTable;
    private javax.swing.JButton PLPrintBTN;
    private javax.swing.JButton PLRemoveBTN;
    private javax.swing.JScrollPane PLTableScroll;
    private javax.swing.JFrame PLWindow;
    private javax.swing.JComboBox<String> RCComboBox;
    private javax.swing.JLabel RCDetailsLabel;
    private javax.swing.JTextArea RCDetailsOutput;
    private javax.swing.JScrollPane RCDetailsScroll;
    private javax.swing.JTextField RCNameInput;
    private javax.swing.JLabel RCNameLabel;
    private javax.swing.JTextField RCONContainerInput;
    private javax.swing.JPanel RCONPanel;
    private javax.swing.JButton RCONRemoveBTN;
    private javax.swing.JFrame RCONWindow;
    private javax.swing.JPanel RCPanel;
    private javax.swing.JButton RCRemoveBTN;
    private javax.swing.JFrame RCWindow;
    private javax.swing.JPanel RUPanel;
    private javax.swing.JPasswordField RUPasswordInput;
    private javax.swing.JButton RURemoveBTN;
    private javax.swing.JComboBox<String> RUUserComboBox;
    private javax.swing.JFrame RUWindow;
    private javax.swing.JLabel SCAddressLabel;
    private javax.swing.JTextArea SCAddressOutput;
    private javax.swing.JScrollPane SCAddressScroll;
    private javax.swing.JTextField SCConContainerInput;
    private javax.swing.JTable SCConLocTable;
    private javax.swing.JScrollPane SCConLocTableScroll;
    private javax.swing.JLabel SCDateInLabel;
    private javax.swing.JLabel SCDateInOutput;
    private javax.swing.JLabel SCEmailLabel;
    private javax.swing.JLabel SCEmailOutput;
    private javax.swing.JComboBox<String> SCFoundComboBox;
    private javax.swing.JTextField SCIdSearchInput;
    private javax.swing.JLabel SCIdSearchLabel;
    private javax.swing.JTextField SCLastNameSearchInput;
    private javax.swing.JLabel SCNameLabel;
    private javax.swing.JLabel SCNameOutput;
    private javax.swing.JLabel SCNameSearchLabel;
    private javax.swing.JPanel SCPanel;
    private javax.swing.JLabel SCTelephoneLabel;
    private javax.swing.JLabel SCTelephoneOutput;
    private javax.swing.JFrame SCWindow;
    private javax.swing.JLabel SConLocationLabel;
    private javax.swing.JLabel SConNameLabel;
    private javax.swing.JPanel SConPanel;
    private javax.swing.JFrame SConWindow;
    private javax.swing.JTextField SIConInput;
    private javax.swing.JLabel SIContainerLabel;
    private javax.swing.JTextField SIDescInput;
    private javax.swing.JLabel SIDescriptionLabel;
    private javax.swing.JScrollPane SIInventoryScroll;
    private javax.swing.JTable SIInventoryTable;
    private javax.swing.JTextField SIItemInput;
    private javax.swing.JLabel SIItemLabel;
    private javax.swing.JPanel SIPanel;
    private javax.swing.JButton SISearchBTN;
    private javax.swing.JFrame SIWindow;
    private javax.swing.JPanel SideMainBTNPanel;
    private javax.swing.JPanel TopMainBTNPanel;
    private javax.swing.JButton VAContainerAccoladesBTN;
    private javax.swing.JComboBox<String> VAHActionCombo;
    private javax.swing.JToggleButton VAHActionTog;
    private javax.swing.JComboBox<String> VAHDateCombo;
    private javax.swing.JToggleButton VAHDateTog;
    private javax.swing.JPanel VAHPanel;
    private javax.swing.JButton VAHPrintBTN;
    private javax.swing.JScrollPane VAHScroll;
    private javax.swing.JButton VAHSearchBTN;
    private javax.swing.JTable VAHTable;
    private javax.swing.JComboBox<String> VAHUserCombo;
    private javax.swing.JToggleButton VAHUserTog;
    private javax.swing.JFrame VAHWindow;
    private javax.swing.JButton VALongestCustomerBTN;
    private javax.swing.JButton VAMostActiveUserrBTN;
    private javax.swing.JButton VAMostContainersBTN;
    private javax.swing.JButton VAOldestContainerBTN;
    private javax.swing.JPanel VAPanel;
    private javax.swing.JScrollPane VAScroll;
    private javax.swing.JTable VATable;
    private javax.swing.JFrame VAWindow;
    private javax.swing.JComboBox<String> VCHComboBox;
    private javax.swing.JTextField VCHCustomerInput;
    private javax.swing.JLabel VCHCustomerLabel;
    private javax.swing.JTextArea VCHDetailsOutput;
    private javax.swing.JTextArea VCHLogArea;
    private javax.swing.JPanel VCHPanel;
    private javax.swing.JButton VCHPrintBTN;
    private javax.swing.JFrame VCHWindow;
    private javax.swing.JComboBox<String> VCONHComboBox;
    private javax.swing.JLabel VCONHConLabel;
    private javax.swing.JPanel VCONHPanel;
    private javax.swing.JScrollPane VCONHScroll;
    private javax.swing.JTable VCONHTable;
    private javax.swing.JFrame VCONHWindow;
    private javax.swing.JTextField WMColumnsInput;
    private javax.swing.JLabel WMColumnsLabel;
    private javax.swing.JButton WMCompleteBTN;
    private javax.swing.JTextField WMConFromInput;
    private javax.swing.JProgressBar WMConProgressBar;
    private javax.swing.JScrollPane WMConScroll;
    private javax.swing.JTable WMConTable;
    private javax.swing.JTextField WMConToInput;
    private javax.swing.JLabel WMContainerGenLabel;
    private javax.swing.JButton WMGenerateConBTN;
    private javax.swing.JTextField WMHeightInput;
    private javax.swing.JLabel WMHeightLabel;
    private javax.swing.JProgressBar WMLocProgressBar;
    private javax.swing.JScrollPane WMLocScroll;
    private javax.swing.JButton WMLocationGenBTN;
    private javax.swing.JLabel WMLocationGenLabel;
    private javax.swing.JTable WMLocationsTable;
    private javax.swing.JPanel WMPanel;
    private javax.swing.JTextField WMRowsInput;
    private javax.swing.JLabel WMRowsLabel;
    private javax.swing.JLabel WMToLabel;
    private javax.swing.JFrame WMWindow;
    private javax.swing.JButton addNewContainerBTN;
    private javax.swing.JButton addNewCustomerBTN;
    private javax.swing.JButton addNewUserBTN;
    private javax.swing.JLabel changePasswordLabel;
    private javax.swing.JButton changePasswordsBTN;
    private javax.swing.JButton createNewInventoryBTN;
    private javax.swing.JButton createPickListBTN;
    private javax.swing.JButton editCustomerDetailsBTN;
    private javax.swing.JButton editInventoryBTN;
    private javax.swing.JButton emptyContainerBTN;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JLabel mainContainersLabel;
    private javax.swing.JScrollPane mainContainersScroll;
    private javax.swing.JTable mainContainersTable;
    private javax.swing.JScrollPane mainCustomersScroll;
    private javax.swing.JTable mainCustomersTable;
    private javax.swing.JLabel mainCustomersTableLable;
    private javax.swing.JScrollPane mainInventoryScroll;
    private javax.swing.JTable mainInventoryTable;
    private javax.swing.JLabel mainInventoryTableLabel;
    private javax.swing.JButton manageWarehouseBTN;
    private javax.swing.JButton moveContainersBTN;
    private javax.swing.JButton removeContainerBTN;
    private javax.swing.JButton removeCustomerBTN;
    private javax.swing.JButton removeUserBTN;
    private javax.swing.JButton searchContainerBTN;
    private javax.swing.JButton searchCustomerBTN;
    private javax.swing.JButton searchInventoryBTN;
    private javax.swing.JButton viewActivityHistoryBTN;
    private javax.swing.JButton viewAnalyticsBTN;
    private javax.swing.JButton viewContainerHistoryBTN;
    private javax.swing.JButton viewCustomerHistoryBTN;
    // End of variables declaration//GEN-END:variables

    /**
     * clears all the class variables for the customer
     */
    
    public void clearVariables(){
        
        title = "";
        firstName = "";
        lastName = "";
        telephone = "";
        eMail = "";
        address = "";
        container = 0;
        location = "";
        
    }
    
    /**
     * checks if the table already contains given fields
     * @param container
     * @param location
     * @param table
     * @return 
     */
    
    public boolean checkTable(int container, String location, javax.swing.JTable table){
        
        boolean alreadyEntered = false;
        
        int rows = table.getRowCount();

        DefaultTableModel tm = (DefaultTableModel)table.getModel();
        
        for(int  i = 0; i < rows; i++){
            
            int enteredContainer = (int) tm.getValueAt(i, 0);
            String enteredLocation = String.valueOf(tm.getValueAt(i, 1));
            
            if(container == enteredContainer){
                
                JOptionPane.showMessageDialog(null, "Container Already entered In The Table!");
                alreadyEntered = true;
                break;
            }
            
            if(location.contains(enteredLocation)){
                
                JOptionPane.showMessageDialog(null, "Location Already entered In The Table!");
                 alreadyEntered = true;
                break;
                
            }        
        }
        
        return alreadyEntered;
    }
    
    /**
     * class for adding a file chooser button to table
     */
    
    class FileChooserCellEditor extends DefaultCellEditor implements TableCellEditor {

        /** Number of clicks to start editing */
        private static final int CLICK_COUNT_TO_START = 2;
        /** Editor component */
        private JButton button;
        /** File chooser */
        private JFileChooser fileChooser;
        /** Selected file */
        private String file = "";

        /**
         * Constructor.
         */
        public FileChooserCellEditor() {
            super(new JTextField());
            setClickCountToStart(CLICK_COUNT_TO_START);

            // Using a JButton as the editor component
            button = new JButton();
            button.setBackground(Color.white);
            button.setFont(button.getFont().deriveFont(Font.PLAIN));
            button.setBorder(null);

            // Dialog which will do the actual editing
            
            fileChooser = new JFileChooser();
        }

        @Override
        public Object getCellEditorValue() {
            return file;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//            file = value.toString();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                
                    fileChooser.setCurrentDirectory(new File(cs.getCustomerFileId(ID))); 
                    
                    if (fileChooser.showOpenDialog(button) == JFileChooser.APPROVE_OPTION) {
                        file = fileChooser.getSelectedFile().getAbsolutePath().replaceAll("\\\\", "/");
                    }
                    fireEditingStopped();
                }
            });
            button.setText(file);
            return button;
        }
    }
    
    /**
     * alters text to go to database
     * @param address
     * @return 
     */
    public String fixAddressToDB(String address){
        
        String fixed = address.replaceAll("\n", " ");
           
        return fixed;
    }
    
      /**
     * alters text to get from database
     * @param address
     * @return 
     */
        public String fixAddressFromDB(String address){
        
        String fixed = address.replaceAll(" ", "\n");
           
        return fixed;
    }
        
    /**
     * locks the variables in edit inventory to stop changing before saving
     */
        
    private void EILock(){

        saved = false;
        locked = true;                  //locks container into window to stop changes mid action
        movable = true;
        
        EIConInput1.setFocusable(false);            //stops user inputing another container before save is completed
        EIConInput2.setFocusable(false);
        EILockedLabel.setText("Locked");
        EIRelocatRadioBTN.setEnabled(false);
    }
    
     /**
     * unlocks the variables in edit inventory to stop changing before saving
     */
    
    private void EIUnLock(){
        
        saved = true;                                       //resets variables keeping from changes
        locked = false;        
        movable = false;
        
        EIConInput1.setFocusable(true);           
        EIConInput2.setFocusable(true);
        EILockedLabel.setText("Done");
        EIRelocatRadioBTN.setEnabled(true);
    }
}
