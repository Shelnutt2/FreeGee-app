package edu.shell.freegee;


import java.util.ArrayList;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class Devices {
	@ElementList
    private ArrayList<Device> devices = new ArrayList<Device>();
 
    public Devices() {}
 
    public Devices(ArrayList<Device> devices) {
        this.devices = devices;
    }
 
    public ArrayList<Device> getDevices() {
        return devices;
    }
 
    public void setDevices(ArrayList<Device> devices) {
        this.devices = devices;
    }
}
