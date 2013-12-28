package edu.shell.freegee.device;

import java.util.ArrayList;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class Device {
	@Element
    private String name;
	@ElementList
    private ArrayList<String> carrier;
	@Element
    private String model;
	@ElementList
    private ArrayList<String> firmware;
	@Element
    private String prop_id;
	@Element
    private String sw_prop_id;
	@Element // 0 = mako aboot, 1 = loki
    private int bootloaderExploit;
    @ElementList
    private ArrayList< Action > actions;
    @Element(required=false)
    private String deviceDetailsLocation;

    public Device(String name, ArrayList<String> carrier, String model, ArrayList<String> firmware, String prop_id, String sw_prop_id, int bootloaderExploit, ArrayList< Action > actions, String deviceDetailsLocation){
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
    
    public String getModel(){
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
    
    public int getbootloaderExploit(){
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
    
    public void setModel(String model){
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
    
    public void setbootloaderExploit(int bootloaderExploit){
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