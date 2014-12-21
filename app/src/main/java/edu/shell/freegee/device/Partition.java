package edu.shell.freegee.device;


public class Partition {

    private String name;

    private String partitionNumber;

    private boolean requiredBackup;

	public Partition(String name, String partitionNumber, boolean requiredBackup){
		this.name = name;
		this.partitionNumber = partitionNumber;
		this.requiredBackup = requiredBackup;
	}

    public Partition(){}
	
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
