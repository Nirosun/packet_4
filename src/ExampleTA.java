/**
 * @author Andrew Fox
 */

import NetworkElements.*;
import java.util.*;
import DataTypes.*;

public class ExampleTA {
	// This object will be used to move time forward on all objects
	private ArrayList<Computer> allSourceConsumers = new ArrayList<Computer>();
	private ArrayList<Computer> allDestinationConsumers = new ArrayList<Computer>();
	private int time = 0;
	Switch s;

	/**
	 * Create a network and creates connections
	 * @since 1.0
	 */
	public void go(){
		System.out.println("** SYSTEM SETUP **");

		int numComputers= 64;

		// Create Source Computers
		for (int i=0;i<numComputers;i++){
			Computer c=new Computer(i);
			NIC nic = new NIC(c);
			allSourceConsumers.add(c);
		}

		// Create Destination Computers
		for (int i=0;i<numComputers;i++){
			Computer c=new Computer(i);
			NIC nic = new NIC(c);
			allDestinationConsumers.add(c);
		}

		// Create the Switch
		s = new Switch(numComputers);
		s.setInputQueue();
		//s.setOutputQueue();

		// connect the computers to the links
		int j=0;
		for(Computer c:allSourceConsumers){
			OtoOLink l=new OtoOLink(c.getNIC(),s.getInputNICs().get(j));
			j++;
		}
		j=0;
		for(Computer c:allDestinationConsumers){
			OtoOLink l=new OtoOLink(c.getNIC(),s.getOutputNICs().get(j));
			j++;
		}

		/*
		// Send packets
		allSourceConsumers.get(0).sendPacket(1);
		allSourceConsumers.get(1).sendPacket(1);
		allSourceConsumers.get(2).sendPacket(1);
		allSourceConsumers.get(3).sendPacket(1);
		
		//allSourceConsumers.get(0).sendPacket(1);
				for(int i=1; i<12; i++)
					this.tock();
		*/
		
		
		// simulate the performance of input and output queueing
		int packetsSent = 0;	// total packets sent
		double probabilitySent = 0.8;	// probability of a computer sending a packet
		
		for(int i=0; i<100000; i++) {
			for (int k = 0; k < allSourceConsumers.size(); k ++) {
				if (Math.random() <= probabilitySent) {
					// each computer randomly select one destination
					allSourceConsumers.get(k).sendPacket((int)(Math.random() * allSourceConsumers.size()));
					packetsSent ++;
				}
			}
			this.tock();
		}
			
		double throughPut = Metrics.packetsSucceed / (double)packetsSent;
		double avgDelay = Metrics.totalDelay / (double) Metrics.packetsSucceed;
		System.out.println("Total: " + packetsSent);
		System.out.println("Received: " + Metrics.packetsSucceed);
		System.out.println("Throughput: " + throughPut);
		System.out.println("Average Delay: " + avgDelay);
				
	}

	
	/**
	 * moves time forward in all of the networks objects, so that packets take some amount of time to
	 * travel from once place to another
	 */
	public void tock(){
		//System.out.println("** TIME = " + time + " **");
		time++;


		// send packets from all input computers to the switch
		for(int i=0; i<this.allSourceConsumers.size(); i++)
			allSourceConsumers.get(i).sendFromBuffer();
		s.sendFromOutputs();		// send packets from the output of the switch to destination computer
		s.sendFromBuffer();			// send packets across the switch
		// clears the buffers of the destination routers
		for(int i=0; i<this.allDestinationConsumers.size(); i++)
			allDestinationConsumers.get(i).clearBuffer();
	}

	public static void main(String args[]){
		ExampleTA go = new ExampleTA();
		go.go();
	}
}
