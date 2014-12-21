package edu.shell.freegee.device;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class Device {

    private String name;

    private String carrier;

    private String model;
    
    private String description;

    private String firmware;

    private ArrayList< Action > actions;
    
    private Device parentDevice;

    public Device(String name, String carrier, String model, String description, String firmware, ArrayList< Action > actions, Device ParentDeviec){
    	this.name = name;
    	this.carrier = carrier;
    	this.model = model;
    	this.description = description;
    	this.firmware = firmware;
    	this.actions = actions;
    	this.parentDevice = parentDevice;
    }

    public Device() {}

	public Device(JSONObject jDevice) throws JSONException {
		this.name = jDevice.getString("name");
		this.carrier = jDevice.getString("carrier");
		this.model = jDevice.getString("model");
		this.description = jDevice.getString("description");
		this.firmware = jDevice.getString("firmware");
		if(jDevice.has("actions") && jDevice.get("actions") instanceof JSONArray){
			actions = new ArrayList<Action>();
			JSONArray jActions = jDevice.getJSONArray("actions");
			for(int index = 0; index < jActions.length(); index++){
				this.actions.add(new Action(jActions.getJSONObject(index)));
			}
		}
	}

	public String getName(){
    	return name;
    }

    public String getCarrier(){
    	return carrier;
    }

    public String getModel(){
    	return model;
    }
    
    public String getDescription(){
    	return description;
    }

    public String getFirmware(){
    	return firmware;
    }
    
    public Device getParentDevice(){
    	return parentDevice;
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
    
    public void setDescription(String description){
    	this.description = description;
    }

    public void setFirmware(String firmware){
    	this.firmware = firmware;
    }

    public void setActions( ArrayList< Action > actions ) {
        this.actions = actions;
    }
    
    public void setParentDevice(Device parentDevice){
    	this.parentDevice = parentDevice;
    }

    public String toString(){
		return "Name: "+name+" "+"Model: "+model+" "+"Carrier: " +carrier;
    }
}