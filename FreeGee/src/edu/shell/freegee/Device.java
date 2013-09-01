package edu.shell.freegee;

import java.util.ArrayList;


public class Device {

    private String name;
    private String carrier;
    private String model;
    private String firmware;
    private String prop_id;
    private ArrayList< Action > actions;
    
   
    public String getName(){
    	return name;
    }
    
    public String getCarrier(){
    	return carrier;
    }
    
    public String getModel(){
    	return model;
    }
    
    public String getFirmware(){
    	return firmware;
    }
    
    public String getProp_id(){
    	return prop_id;
    }
    
    public ArrayList< Action > getActions() {
        return actions;
    }
    
    public void setName(String name){
    	this.name = name;
    }
    
    public void setCarrier(String carrier){
    	this.carrier = carrier;
    }
    
    public void setModel(String model){
    	this.model = model;
    }
    
    public void setFirmware(String firmware){
    	this.firmware = firmware;
    }
    
    public void setProp_id(String prop_id){
    	this.prop_id = prop_id;
    }
    
    public void setActions( ArrayList< Action > actions ) {
        this.actions = actions;
    }
}

