package de.sgrad.yamahathreditor;

import android.util.Log;

public class CompressorData {
	public static String TAG = "THR";
	public static final String compressorTypeStomp = "Stomp";
	public static final String compressorTypeRack = "Rack";
	
	String  compressorType;
	boolean compressorOnOff;
	
	byte stompSustain;
	byte stompOutput;
	int  rackThreshold;
	byte rackAttack;
	byte rackRelease;
	byte rackRatio;
	byte rackKnee;
	int  rackOutput;
	
	public CompressorData() {
		stompSustain = 0;
		stompOutput = 0;
		rackThreshold = 0;
		rackAttack = 0;
		rackRelease = 0;
		rackRatio = 0;
		rackKnee = 0;
		rackOutput = 0;
		compressorType = compressorTypeStomp;
		compressorOnOff = false;
	}
	
	/*
	 * Read the current control values from this module and convert them to byte-format to write into patch file 
	 */
	public byte[] getPatchValues(){
		byte [] msg = new byte [] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,   0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
		if(compressorType.equals(compressorTypeRack)){
			msg[0] = 0x01;
			byte [] threshold = SysExCommands.encodeIntegerTo7Bit(rackThreshold);
			msg[1] = threshold[0];
			msg[2] = threshold[1];
			msg[3] = rackAttack;
			msg[4] = rackRelease;
			msg[5] = rackRatio;
			msg[6] = rackKnee;
			byte [] output = SysExCommands.encodeIntegerTo7Bit(rackOutput);
			msg[7] = output[0];
			msg[8] = output[1];
		}else{
			msg[1] = stompSustain; 
			msg[2] = stompOutput; 
		}
		// On = 0x00, Off = 0x7f
		if(compressorOnOff == false)
			msg[15] = 0x7f;
		return msg;
	}
	
	/*
	 * set the current control values in this module. They has been read from patch file
	 */
	public void setPatchValues(byte[] msg){
		if(msg[0] == 0x01){
			compressorType = compressorTypeRack;
			byte [] temp = new byte [] {0x00, 0x00};
			temp[0] = msg[1];
			temp[1] = msg[2];
			rackThreshold = SysExCommands.decode7BitByteToInt(temp);	
			rackAttack = msg[3];
			rackRelease = msg[4];
			rackRatio = msg[5];
			rackKnee = msg[6];
			temp[0] = msg[7];
			temp[1] = msg[8];
			rackOutput = SysExCommands.decode7BitByteToInt(temp);	
		}else if (msg[0] == 0x00){
			compressorType = compressorTypeStomp;
			stompSustain = msg[1];
			stompOutput = msg[2];
		}
		
		if(msg[15] == 0x7f)
			compressorOnOff = false;
		else
			compressorOnOff = true;
		
		//Log.d( TAG, "COMPRESSOR: Type " + compressorType + " Sustain " + stompSustain + " Output " + stompOutput + " Threshold " + rackThreshold + " Attack " + rackAttack + " Release " + rackRelease + " Ratio " + rackRatio  + " RackOutput " + rackOutput + " On " + Boolean.toString(compressorOnOff));
	}

}
