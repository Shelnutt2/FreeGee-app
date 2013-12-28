package edu.shell.freegee.device;
import org.simpleframework.xml.Element;


public class Partition {
	@Element
    private String name;
	@Element
    private String partitionNumber;
	@Element
    private boolean requiredBackup;

	public Partition(String name, String partitionNumber, boolean requiredBackup){
		this.name = name;
		this.partitionNumber = partitionNumber;
		this.requiredBackup = requiredBackup;
	}
	
	public String getName(){
		return name;
	}

	public String getPartitionNumber(){
		return partitionNumber;
	}

	public boolean getRequiredBackup(){
		return requiredBackup;
	}

	public void setName(String name){
		this.name = name;
	}

	public void getPartitionNumber(String partitionNumber){
		this.partitionNumber = partitionNumber;
	}

	public void setRequiredBackup(boolean requiredBackup){
		this.requiredBackup = requiredBackup;
	}
}
