package edu.shell.freegee.device;


import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Action implements Serializable, Comparable<Action> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 628757196911001590L;

	private String name;

    private String description;
    
    private String category;

    private String version;

    private String zipfile;

    private String zipfilelocation;

    private String md5sum;

    private boolean stockOnly;

    private boolean hidden;

    private int priority = 10;

    private ArrayList<Action> dependencies;

    public Action(String name, String description, String category, String version, String zipfile, String zipfilelocation, String md5sum, boolean stockOnly, boolean hidden, int priority, ArrayList<Action> dependencies){
    	this.name = name;
    	this.description = description;
    	this.category = category;
    	this.version = version;
    	this.zipfile = zipfile;
    	this.zipfilelocation = zipfilelocation;
    	this.md5sum = md5sum;
    	this.stockOnly = stockOnly;
    	this.hidden = hidden;
    	this.priority = priority;
    	this.dependencies = dependencies;
    }
    
    public Action(){}
    
    public Action(JSONObject jAction) throws JSONException {
    	this.name = jAction.getString("name");
    	this.description = jAction.getString("description");
    	this.category = jAction.getString("category");
    	//this.version = jAction.getString("version");
    	this.zipfile = jAction.getString("zipfile");
    	this.zipfilelocation = jAction.getString("zipfilelocation");
    	this.md5sum = jAction.getString("md5sum");
    	this.stockOnly = jAction.getInt("stockonly") == 1 ? true: false;
    	this.hidden = jAction.getInt("hidden") == 1 ? true: false;
    	this.priority = jAction.getInt("priority");
    	if(jAction.has("dependencies") && jAction.get("dependencies") instanceof JSONArray){
	    	JSONArray jdepends = jAction.getJSONArray("dependencies");
	    	for(int index = 0; index < jdepends.length(); index++){
				this.dependencies.add(new Action((JSONObject)jdepends.get(index)));
			}
    	}
	}

	public String getName(){
    	return name;
    }
    
    public String getDescription(){
    	return description;
    }
    
    public String getCategory(){
    	return category;
    }
    
    public String getVersion() {
        return version;
    }
    
    public String getZipFile() {
        return zipfile;
    }
    
    public String getZipFileLocation() {
        return zipfilelocation;
    }
    
    public String getMd5sum() {
        return md5sum;
    }
    
    public boolean getStockOnly(){
    	return stockOnly;
    }
    
    public boolean getHidden(){
    	return hidden;
    }
    
    public int getPriority() {
		return priority;
	}
    
    public ArrayList<Action> getDependencies() {
        return dependencies;
    }

    public void setName(String name){
    	this.name = name;
    }

    public void setDescription(String description){
    	this.description = description;
    }
    
    public void setCategory(String category){
    	this.category = category;
    }

    public void setVersion( String version ) {
        this.version = version;
    }
    
    public void setZipFile( String zipfile ) {
        this.zipfile = zipfile;
    }
    
    public void setZipFileLocation( String zipfilelocation ) {
        this.zipfilelocation = zipfilelocation;
    }
    
    public void setMd5sum( String md5sum ) {
        this.md5sum = md5sum;
    }
    
    public void setStockOnly( boolean stockOnly ){
    	this.stockOnly = stockOnly;
    }
    
    public void setHidden( boolean hidden ){
    	this.hidden = hidden;
    }

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setDependencies( ArrayList<Action> dependencies ) {
        this.dependencies = dependencies;
    }

	@Override
	public int compareTo(Action b) {
		return b.priority - this.priority;
	}
}