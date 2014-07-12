package edu.shell.freegee.device;

import java.util.ArrayList;


public class Device {

    private String name;

    private ArrayList<String> carrier;

    private ArrayList<String> model;

    private ArrayList<String> firmware;

    private String prop_id;

    private String sw_prop_id;

    private int bootloaderExploit = 1;

    private ArrayList< Action > actions;

    private String deviceDetailsLocation;

    public Device(String name, ArrayList<String> carrier, ArrayList<String> model, ArrayList<String> firmware, String prop_id, String sw_prop_id, int bootloaderExploit, ArrayList< Action > actions, String deviceDetailsLocation){
    	this.name = name;
    	this.carrier = carrier;
    	this.model = model;
    	this.firmware = firmware;
    	this.prop_id = prop_id;
    	this.sw_prop_id = sw_prop_id;
    	this.bootloaderExploit = bootloaderExploit;
    	this.actions = actions;
    	this.deviceDetailsLocation = deviceDetailsLocation;
    }

    public Device() {}

	public String getName(){
    	return name;
    }

    public ArrayList<String> getCarrier(){
    	return carrier;
    }

    public ArrayList<String> getModel(){
    	return model;
    }

    public ArrayList<String> getFirmware(){
    	return firmware;
    }

    public String getProp_id(){
    	return prop_id;
    }

    public String getSW_Prop_id(){
    	return sw_prop_id;
    }

    public int getBootloaderExploit(){
    	return bootloaderExploit;
    }

    public ArrayList< Action > getActions() {
        return actions;
    }
    
    public String getDeviceDetailsLocation(){
    	return deviceDetailsLocation;
    }

    public void setName(String name){
    	this.name = name;
    }

    public void setCarrier(ArrayList<String> carrier){
    	this.carrier = carrier;
    }

    public void setModel(ArrayList<String> model){
    	this.model = model;
    }

    public void setFirmware(ArrayList<String> firmware){
    	this.firmware = firmware;
    }

    public void setProp_id(String prop_id){
    	this.prop_id = prop_id;
    }

    public void setSW_Prop_id(String sw_prop_id){
    	this.sw_prop_id = sw_prop_id;
    }

    public void setBootloaderExploit(int bootloaderExploit){
    	this.bootloaderExploit = bootloaderExploit;
    }

    public void setActions( ArrayList< Action > actions ) {
        this.actions = actions;
    }
    
    public void setDeviceDetailsLocation(String deviceDetailsLocation){
    	this.deviceDetailsLocation = deviceDetailsLocation;
    }

    public String toString(){
		return "Name: "+name+" "+"Model: "+model+" "+"Carrier: " +carrier;
    }
}