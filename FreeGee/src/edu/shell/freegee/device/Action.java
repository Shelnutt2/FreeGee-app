package edu.shell.freegee.device;


import java.io.Serializable;
import java.util.ArrayList;

public class Action implements Serializable, Comparable<Action> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 628757196911001590L;

	private String name;

    private String description;

    private String version;

    private String zipfile;

    private String zipfilelocation;

    private String md5sum;

    private boolean stockOnly;

    private boolean hidden;

    private int priority = 10;

    private ArrayList<Action> dependencies;

    public Action(String name, String description, String version, String zipfile, String zipfilelocation, String md5sum, boolean stockOnly, boolean hidden, int priority, ArrayList<Action> dependencies){
    	this.name = name;
    	this.description = description;
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
    
    public String getName(){
    	return name;
    }
    
    public String getDescription(){
    	return description;
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