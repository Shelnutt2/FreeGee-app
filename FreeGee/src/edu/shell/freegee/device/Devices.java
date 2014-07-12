package edu.shell.freegee.device;


import java.util.ArrayList;


public class Devices {

	private int protocolVersion = 2;

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
