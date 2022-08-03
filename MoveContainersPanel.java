/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContainerStore;

import static ContainerStore.ContainerMain.floats;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 *
 * @author hutch
 */
public class MoveContainersPanel extends JPanel implements MouseListener, MouseMotionListener{
    
    private Warehouse wh;
    private Container empty;
    
    protected JComboBox MCLocComboBox;
    protected JButton MCRemoveBTN;
    protected JButton MCUnPickAllBTN;
    
    
    ArrayList<Container> conMove = new ArrayList<>();
    ArrayList<String> conToFloat = new ArrayList<>();
    ArrayList<Container> floatToWare = new ArrayList<>();
    ArrayList<String> floatToMove = new ArrayList<>();
    
    int getX;
    int getY;
    int originalX;
    int originalY;
    
    int dragNum;
    
    
    public MoveContainersPanel(){
        
        wh = new Warehouse();
        
        setLayout(null);
        setBounds(0,0,1448, 900);
        
        MCLocComboBox = new JComboBox();
        MCRemoveBTN = new JButton("Remove");
        MCUnPickAllBTN = new JButton("UnPick");
        
        MCLocComboBox.setBounds(50, 20, 60, 30);
        MCRemoveBTN.setBounds(130, 20, 90, 30);
        MCUnPickAllBTN.setBounds(230, 20, 90, 30);
        
        add(MCLocComboBox);
        add(MCRemoveBTN);
        add(MCUnPickAllBTN);
               
        setBackground(Color.WHITE);
        setOpaque(true);
        
        MCRemoveBTN.setVisible(false);
        MCUnPickAllBTN.setVisible(false);
        
        empty = new Container(getWidth() - 150, 10, "EMPTY", false, false);
        
        addBTNAction();
    }
    
    /**
     * adds listeners to buttons
     */
    
    private void addBTNAction(){
        
        MCUnPickAllBTN.addActionListener(new ActionListener(){      //unpick button
            
               
            @Override
            public void actionPerformed(ActionEvent e) {

                for(int i = 0; i < conMove.size(); i++){            //finds all containers picked and un picks them

                    if(conMove.get(i).getPicked()){

                        conMove.get(i).setPicked(false);
                        conToFloat.remove(conMove.get(i).getConString());       //removes unpicked container from the floating table arraylist       
                    }
                }
                
                MCRemoveBTN.setVisible(false);                  //removes buttons when all containers unpicked
                MCUnPickAllBTN.setVisible(false);
                
                repaint();
            }
        }); 
        
        MCRemoveBTN.addActionListener(new ActionListener(){
            
            @Override
            public void actionPerformed(ActionEvent e) {
                
                for(String c : conToFloat){                     //adds all picked containers to the floating table
                    
                    int con = Integer.valueOf(c);
                    int id = wh.getCustomerIdByContainer(con);
                    
                    String status = wh.getStatus(con);
                    wh.addToFloationgTable(id, con, status);
                    
                }
                
                MCRemoveBTN.setVisible(false);                  //removes buttons when all picked containers removed
                MCUnPickAllBTN.setVisible(false);
                
                conToFloat.clear();         //clears contofloat array when done
                
                wh.sortAisle(String.valueOf(MCLocComboBox.getSelectedItem()));
                
                int aisle = MCLocComboBox.getSelectedIndex();           //essentialy refreshes the page
                MCLocComboBox.setSelectedIndex(0);
                MCLocComboBox.setSelectedIndex(aisle);
                        
                repaint();
            }
        });
    }
    
    /*
    returns the conMove arraylist
    */
    
    public ArrayList<Container> getCons(){
        
        return conMove;
    }
     
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        
        if(floatToWare.size() > 0){
            
            empty.draw(g);
        }
        
        for(int i = 0; i < conMove.size(); i++){
            
            conMove.get(i).draw(g);
        }
        
          for(int i = 0; i < floatToWare.size(); i++){
            
            floatToWare.get(i).draw(g);
        }    
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        
       Point click = e.getPoint();              //gets mouse click point
           
       for(int i = 0; i < conMove.size(); i++){             //check clicks of warehouse containers
           
           if(conMove.get(i).getBounds().contains(click) && !conMove.get(i).getConString().matches("FREE")){          //checks which container clicked on ignoring free space

                boolean picked  = conMove.get(i).getPicked();       //gets if the container has already been picked

                conMove.get(i).setPicked(!picked);                  //sets boolean picked to opposite of what it is

                if(conMove.get(i).getPicked()){                 //adds picked container to the floating arraylist ready to be added to the floating table
                    
                    conToFloat.add(conMove.get(i).getConString());
                 
                }else{
                    
                    conToFloat.remove(conMove.get(i).getConString());          //removes unpicked container from the floating table arraylist
                }                     
           }
       }

       for(int i = 0; i < conMove.size(); i++){
           
           if(conMove.get(i).getPicked()){
                    
                MCRemoveBTN.setVisible(true);               //shows remove and unpick button only if a container picked
                MCUnPickAllBTN.setVisible(true);
                 
                break;
                
            }else{
                    
                MCRemoveBTN.setVisible(false);
                MCUnPickAllBTN.setVisible(false);
            }
        }  
    }

    @Override
    public void mousePressed(MouseEvent e) {
          
        Point click = e.getPoint(); 
        
        for(int i = 0 ; i < floatToWare.size(); i++){
            
            if(floatToWare.get(i).getBounds().contains(click)){     //checks clicks on floating containers
  
                originalX  = floatToWare.get(i).returnX();        //gets orignal co-ordinates of container
                originalY = floatToWare.get(i).returnY();
           
                dragNum = i;
                
                boolean picked  = floatToWare.get(i).getPicked();       //gets if the container has already been picked

                floatToWare.get(i).setPicked(!picked);                  //sets boolean picked to opposite of what it is
               
                repaint();
            }      
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
           
        Point click = e.getPoint(); 
        
  scan: for(int i = 0 ; i < floatToWare.size(); i++){
            

            if(floatToWare.get(i).getBounds().contains(click)){     //checks clicks on floating containers
           
                boolean picked  = floatToWare.get(i).getPicked();       //unpicks container changing its color
                floatToWare.get(i).setPicked(!picked);
                
                
                int conNum = Integer.valueOf(String.valueOf(floatToWare.get(i).getConString()));
               
                for(int j = 0; j < conMove.size(); j++){               
        
                    if(conMove.get(j).getBounds().contains(click) && conMove.get(j).getConString().matches("FREE")){     //checks it was dropped in free space
                       
                        int id = Integer.valueOf(String.valueOf(ContainerMain.floats[i][0]));                   //finds container details
                        int container = Integer.valueOf(String.valueOf(ContainerMain.floats[i][1]));
                        String status  = String.valueOf(ContainerMain.floats[i][2]);
                        
                        String location = String.valueOf(ContainerMain.aisleDetails[j][2]);        
                        
                        wh.floatToWarehouse(id, container, status, location);        //re-enters the details back into the warehouse deletes from floating
                      
                        wh.sortAisle(String.valueOf(MCLocComboBox.getSelectedItem()));      //                
                                                                                            //
                        floats = wh.getFloatingContainers();                                //
                                                                                            //
                        int aisle = MCLocComboBox.getSelectedIndex();                       //essentialy refreshes the page
                        MCLocComboBox.setSelectedIndex(0);                                  //
                        MCLocComboBox.setSelectedIndex(aisle);      
                        
                    }else if(empty.getBounds().contains(click)){                //checks if dropped in empty
                        
                        wh.floatToEmpty(conNum);
                        wh.sortAisle(String.valueOf(MCLocComboBox.getSelectedItem()));      //                
                                                                                            //
                        floats = wh.getFloatingContainers();                                //
                                                                                            //
                        int aisle = MCLocComboBox.getSelectedIndex();                       //essentialy refreshes the page
                        MCLocComboBox.setSelectedIndex(0);                                  //
                        MCLocComboBox.setSelectedIndex(aisle);   
                                               
                        break scan;     //to stop multiple fires while in loop
                        
                    }else if(!picked && conMove.get(j).getBounds().contains(click) && conMove.get(j).getConString().matches("FREE")){
                        
                        break scan;             //do nothing if no floating container chosen and clicked on empty else the screen flickers and gets buggy
                        
                    }else{
                        
                        try{

                           floatToWare.get(i).setX(originalX) ;             //resets to original position
                           floatToWare.get(i).setY(originalY);

                            repaint();
                            
                        }catch(IndexOutOfBoundsException ex){
                            
                            //method works fine but throws this exception cant fix it causes no problems so not handling
                        }
                        
                        dragNum = 0;            //resets chosen flost container
                     
                    }
                }
            }      
        }
                                               
        dragNum = 0;            //resets chosen flost container
        
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        repaint();  
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
       
    }

    @Override
    public void mouseExited(MouseEvent e) {
       
    }  

    @Override
    public void mouseDragged(MouseEvent e) {
        
        Point click = e.getPoint(); 
       
        for(int i = 0 ; i < floatToWare.size(); i++){          //checks if it is a floating container
                
            if(floatToWare.get(i).getBounds().contains(click)){ 
                
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                getX = e.getX() - 50;
                getY = e.getY() - 50;

                floatToWare.get(dragNum).setX(getX);            //repaints in the position of cursor (while being dragged)
                floatToWare.get(dragNum).setY(getY);

                repaint();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        
        
    }
    
}
