package de.sgrad.yamahathreditor;


public class DelayData {
	public static String TAG = "THR";
	int time;
	byte feedback;
	int highcut;
	int lowcut;
	byte level;
	boolean delayOnOff;
	
	public DelayData() {
		time = 0;
		feedback = 0;
		highcut = 0;
		lowcut = 0;
		level = 0;
		delayOnOff = false;
	}
	
	/*
	 * Read the current control values from this module and convert them to byte-format to write into patch file 
	 */
	public byte[] getPatchValues(){
		byte [] msg = new byte [] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
		byte [] temp = SysExCommands.encodeIntegerTo7Bit(time);
		msg[1] = temp[0];
		msg[2] = temp[1];
		msg[3] = feedback;
		temp = SysExCommands.encodeIntegerTo7Bit(highcut);
		msg[4] = temp[0];
		msg[5] = temp[1];
		temp = SysExCommands.encodeIntegerTo7Bit(lowcut);
		msg[6] = temp[0];
		msg[7] = temp[1];
		msg[8] = level;
		
		// On = 0x00, Off = 0x7f
		if(delayOnOff == false)
			msg[15] = 0x7f;
		return msg;
	}
	
	/*
	 * set the current control values in this module. They has been read from patch file
	 */
	public void setPatchValues(byte[] msg){
		byte [] temp = new byte [] {0x00, 0x00};
		temp[0] = msg[1];
		temp[1] = msg[2];
		time = SysExCommands.decode7BitByteToInt(temp);
		feedback = msg[3];
		
		temp[0] = msg[4];
		temp[1] = msg[5];
		highcut = SysExCommands.decode7BitByteToInt(temp);
		
		temp[0] = msg[6];
		temp[1] = msg[7];
		lowcut = SysExCommands.decode7BitByteToInt(temp);
		level = msg[8];
		
		if(msg[15] == 0x7f)
			delayOnOff = false;
		else
			delayOnOff = true;
		
		//Log.d( TAG, "DELAY: Time " + time + " Feedback " + feedback + " HighCut " + highcut + " LowCut " + lowcut + " Level " + level + " On " + Boolean.toString(delayOnOff));
	}
}
