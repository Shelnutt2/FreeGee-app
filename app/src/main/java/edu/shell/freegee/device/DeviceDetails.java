package edu.shell.freegee.device;
import java.util.ArrayList;

public class DeviceDetails {

    private ArrayList<Partition> partitions;

	public DeviceDetails(ArrayList<Partition> partitions){
		this.partitions = partitions;
	}

    public DeviceDetails(){}

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