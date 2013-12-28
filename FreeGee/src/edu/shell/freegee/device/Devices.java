package edu.shell.freegee.device;


import java.util.ArrayList;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class Devices {
	@Element
	private int protocolVersion = 2;
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
    
    public int getProtocolVersion() {
        return protocolVersion;
    }
 
    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
}
