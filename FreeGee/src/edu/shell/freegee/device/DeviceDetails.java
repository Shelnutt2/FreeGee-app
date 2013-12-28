package edu.shell.freegee.device;
import java.util.ArrayList;

import org.simpleframework.xml.ElementList;


public class DeviceDetails {
	@ElementList(required=false)
    private ArrayList<Partition> partitions;

	public DeviceDetails(ArrayList<Partition> partitions){
		this.partitions = partitions;
	}
	public ArrayList<Partition> getPartitions(){
		return partitions;
	}

	public void setPartitions(ArrayList<Partition> partitions){
		this.partitions = partitions;
	}

	public boolean addPartition(Partition partition){
		return partitions.add(partition);
	}
}