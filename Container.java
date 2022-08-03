/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContainerStore;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 *
 * @author hutch
 */
public class Container extends Rectangle {
    
    int x;
    int y;
    int width = 100;
    int height = 100;
    
    boolean picked;
    boolean floating;
    
    Color chosen = Color.GREEN;
    Color floatChosen = Color.BLUE;
    Color unChosen = new Color(255,210,85);
    
    String number;
    
    public Container(int x, int y, String number, boolean picked, boolean floating){
        
        this.x = x;
        this.y = y;
        this.number = number;
        this.picked = picked;
        this.floating = floating;
    }
    
    @Override
    public String toString(){
        
        return number + " - "+picked;
    }
    
    public void draw(Graphics g){
        
        if(!getConString().matches("FREE")){            //checks if container present in location
            
            if(getPicked()){     //check if picked
                
                if(getFloating()){      //check if picked a flosting container
                    
                     g.setColor(floatChosen);
                     
                }else{
                    
                    g.setColor(chosen);         //changes color to green when clicked on
                }
            }else{
                
                g.setColor(unChosen);       //changes color back to normal if clicked on while green
            }
            
            if(getConString().matches("EMPTY")){        //for the empty container to drag containers to to empty them
                
                
                g.setColor(Color.RED);
            }
                 
            g.fillRect(x, y, 100, 100);

            g.setColor(Color.BLACK);

            g.drawString(number, x + 10, y + 30);
            
        }else{                              //no container empty black bordered box 
            
            g.setColor(Color.BLACK);

            g.drawRect(x, y, 100, 100);

            g.setColor(Color.BLACK);

            g.drawString(number, x + 10, y + 30);
        }
    }

    /**
     * returns the container number or FREE location
     * @return 
     */
    
    public String getConString(){
        
        return number;
    }
    
    /**
     * returns rectangle of container location
     * @return 
     */
    
    @Override
    public Rectangle getBounds(){
        
        return new Rectangle(x, y, width, height);
    }

    /**
     * returns picked 
     * @return 
     */
    
    public boolean getPicked(){
        
        return picked;
    }
   
    /**
     * sets picked
     * @param chosen 
     */
    
    public void setPicked(boolean chosen){
      
        picked = chosen;
        
    }

    /**
     * returns picked 
     * @return 
     */
    
    public boolean getFloating(){
        
        return floating;
    }
   
    public void setX(int setX){
        
        x = setX;
    }
    
    public void setY(int setY){
        
        y = setY;
    }

    public int returnX(){
        
        return x;
    }
    
    public int returnY(){
        
        return y;
    }
}

