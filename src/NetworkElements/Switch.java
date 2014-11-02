/**
 * @author Andrew Fox
 */
package NetworkElements;

import java.util.*;

import DataTypes.Packet;

public class Switch implements PacketConsumer{
	ArrayList<NIC> inputNICs=new ArrayList<NIC>();		// NICs from input side
	ArrayList<NIC> outputNICs=new ArrayList<NIC>();		// NICs on output side
	boolean inputQueue=false;							// if the switch is input queued or output queued
	
	int lastStartNIC =  0;		// the NIC that was scanned first in last round
	ArrayList<Boolean> isNICUsed = new ArrayList<Boolean>();	// track whether the destinations are occupied
	int inputQueueSize = 20;
	int outputQueueSize = 20;
	
	
	public Switch (int numComputers){
		for (int i=0;i<numComputers;i++){
			NIC nic1=new NIC(this,0);
			NIC nic2=new NIC(this,1);
			
			this.isNICUsed.add(false);
		}
	}

	/**
	 * Adds a nic to the router
	 * side - whether it is on the source or destination side
	 */
	public void addNIC(NIC nic, int side){
		if(side==0)
			inputNICs.add(nic);
		if(side==1)
			outputNICs.add(nic);
	}

	// NOT CALLED
	public void addNIC(NIC nic){}


	/**
	 * Returns an array of NICs on source side
	 * @return
	 */
	public ArrayList<NIC> getInputNICs(){
		return this.inputNICs;
	}

	/**
	 * Returns an array of NICs on destination side
	 * @return
	 */
	public ArrayList<NIC> getOutputNICs(){
		return this.outputNICs;
	}

	/**
	 * Sets the size of the buffers in the NICs on the switch
	 */
	public void setSwitchBufferSize(){
		if(inputQueue==true){
			for(NIC nic:inputNICs){
				nic.setBufferSize(inputQueueSize);
			}
			for(NIC nic:outputNICs){
				nic.setBufferSize(1);
			}
		}
		else if(inputQueue==false){
			for(NIC nic:inputNICs){
				nic.setBufferSize(1);
			}
			for(NIC nic:outputNICs){
				nic.setBufferSize(outputQueueSize);
			}
		}
	}


	/**
	 * Sends packets from the queues on the source
	 * side of the switch to the destination side
	 */
	public void sendFromBuffer(){
		if(inputQueue==true){
			for (int i = 0; i < this.isNICUsed.size(); i ++) {
				this.isNICUsed.set(i, false);
			}
			int nicID = this.lastStartNIC;
			
			// scan through all the input NICs, decide which can send packet
			while (true) {				
				if (!this.inputNICs.get(nicID).getBuffer().isEmpty()) {
					int dest = this.inputNICs.get(nicID).getBuffer().get(0).getDest();
					if (this.isNICUsed.get(dest) == false) {
						this.isNICUsed.set(dest, true);		// mark that the destination is occupied
						Packet p = this.inputNICs.get(nicID).getBuffer().get(0);
						this.outputNICs.get(dest).getBuffer().add(p);
						this.inputNICs.get(nicID).getBuffer().remove(0);
					}
				}					
				nicID ++;
				if (nicID >= this.inputNICs.size()) {
					nicID = 0;
				}				
				if (nicID == this.lastStartNIC) {
					break;
				}
			}
			this.lastStartNIC ++;
			if (this.lastStartNIC >= this.inputNICs.size()) {
				this.lastStartNIC = 0;
			}
			
		}
		else if(inputQueue==false){
			for (NIC nic : this.inputNICs) {
				if (!nic.getBuffer().isEmpty()) {
					int dest = nic.getBuffer().get(0).getDest();
					Packet p = nic.getBuffer().get(0);
					if (this.outputNICs.get(dest).getBuffer().size() < this.outputQueueSize)
						this.outputNICs.get(dest).getBuffer().add(p);
					nic.getBuffer().remove(0);
				}
			}
		}
		for (NIC nic : this.inputNICs) {
			for (Packet p: nic.getBuffer()) {
				p.addDelay(1);	// add delay to packets remaining in input queues
			}
		}
	}

	/**
	 * Sets if the switch is input queued
	 */
	public void setInputQueue(){
		inputQueue=true;
	}

	/**
	 * Sets if the switch is output queued
	 */
	public void setOutputQueue(){
		inputQueue=false;
	}


	/**
	 * Sends packets from the destination side to their final computer destination
	 */
	public void sendFromOutputs(){
		for(NIC nic:outputNICs){
			nic.sendFromBuffer();
		}
		for (NIC nic : this.outputNICs) {
			for (Packet p: nic.getBuffer()) {
				p.addDelay(1);	// add delay to packets remaining in output queues
			}
		}
	}
	
}
