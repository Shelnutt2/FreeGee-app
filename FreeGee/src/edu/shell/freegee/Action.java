package edu.shell.freegee;

import java.util.ArrayList;

public class Action {
	private String name;
    private String version;
    private String zipfile;
    private String zipfilelocation;
    private String md5sum;
    private ArrayList<Action> dependencies;

    public String getName(){
    	return name;
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
    
    public ArrayList<Action> getDependencies() {
        return dependencies;
    }

    public void setName(String name){
    	this.name = name;
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
    
    public void setDependencies( ArrayList<Action> dependencies ) {
        this.dependencies = dependencies;
    }
}

