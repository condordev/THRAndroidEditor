package de.sgrad.yamahathreditor;

import android.util.Log;

public class GateData {
	public static String TAG = "THR";
	
	boolean gateOnOff;
	byte threshold;
	byte release;
	
	public GateData() {
		gateOnOff = false;
		threshold = 0;
		release = 0;
	}
	
	/*
	 * Read the current control values from this module and convert them to byte-format to write into patch file 
	 */
	public byte[] getPatchValues(){
		byte [] msg = new byte [] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
		
		msg[1] = threshold;
		msg[2] = release;
			
		// On = 0x00, Off = 0x7f
		if(gateOnOff == false)
			msg[15] = 0x7f;
		return msg;
	}	
	
	/*
	 * set the current control values in this module. They has been read from patch file
	 */
	public void setPatchValues(byte[] msg){
		threshold = msg[1];
		release = msg[2];
		
		if(msg[15] == 0x7f)
			gateOnOff = false;
		else
			gateOnOff = true;
		
		//Log.d( TAG, "GATE: Threshold " + threshold + " Release " + release  + " On " + Boolean.toString(gateOnOff));
	}

}
