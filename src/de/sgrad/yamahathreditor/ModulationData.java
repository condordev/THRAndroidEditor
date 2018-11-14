package de.sgrad.yamahathreditor;

import android.util.Log;

public class ModulationData {
	public static String TAG = "THR";
	public static final String modTypeChorus = "Chorus";
	public static final String modTypeFlanger = "Flanger";
	public static final String modTypeTremolo = "Tremolo";
	public static final String modTypePhaser = "Phaser";
	
	String  modType;
	boolean modOnOff;
	
	byte speed;
	byte depth;
	byte chorusMix;
	byte manual;
	byte feedback;
	byte tremFreq;
	byte flangerSpread;
	
	ModulationData(){
		speed = 0;
		depth = 0;
		chorusMix = 0;
		manual = 0;
		feedback = 0;
		tremFreq = 0;
		flangerSpread = 0;
		modType = modTypeChorus;
		modOnOff = false;
	}
	
	/*
	 * Read the current control values from this module and convert them to byte-format to write into patch file 
	 */
	public byte[] getPatchValues(){
		byte [] msg = new byte [] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
		if(modType.equals(modTypeChorus)){
			msg[0] = 0x00;
			msg[1] = speed;
			msg[2] = depth;
			msg[3] = chorusMix;
		}else if (modType.equals(modTypeFlanger)) {
			msg[0] = 0x01;
			msg[1] = speed;
			msg[2] = manual;
			msg[3] = depth;
			msg[4] = feedback;
			msg[5] = flangerSpread;
		}else if (modType.equals(modTypeTremolo)) {
			msg[0] = 0x02;
			msg[1] = tremFreq;
			msg[2] = depth;
		}else if (modType.equals(modTypePhaser)) {
			msg[0] = 0x03;
			msg[1] = speed;
			msg[2] = manual;
			msg[3] = depth;
			msg[4] = feedback;			
		}
		
		// On = 0x00, Off = 0x7f
		if(modOnOff == false)
			msg[15] = 0x7f;
		return msg;
	}
	
	/*
	 * set the current control values in this module. They has been read from patch file
	 */
	public void setPatchValues(byte[] msg){
		if(msg[0] == 0x00){
			modType = modTypeChorus;
			speed = msg[1];
			depth = msg[2];
			chorusMix = msg[3];
		}else if(msg[0] == 0x01){
			modType = modTypeFlanger;
			speed = msg[1];
			manual = msg[2];
			depth = msg[3];
			feedback = msg[4];
			flangerSpread = msg[5];
		}else if(msg[0] == 0x02){
			modType = modTypeTremolo;
			tremFreq = msg[1];
			depth = msg[2];
		}else if(msg[0] == 0x03){
			modType = modTypePhaser;
			speed = msg[1];
			manual = msg[2];
			depth = msg[3];
			feedback = msg[4];
		}
		
		if(msg[15] == 0x7f)
			modOnOff = false;
		else
			modOnOff = true;
		
		Log.d( TAG, "ModulationData::setPatchValues() Type " + modType + " Speed " + speed + " Depth " + depth + " Feedback " + feedback + " Frequency " + tremFreq + " Manual " + manual + " Mix " + chorusMix + " Spread " + flangerSpread + " On " + Boolean.toString(modOnOff));
	}

}
