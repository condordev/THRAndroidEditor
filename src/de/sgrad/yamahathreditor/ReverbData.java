package de.sgrad.yamahathreditor;

import android.util.Log;

public class ReverbData {
	public static String TAG = "THR";
	public static final String reverbTypeRoom = "Room";
	public static final String reverbTypeHall = "Hall";
	public static final String reverbTypePlate = "Plate";
	public static final String reverbTypeSpring = "Spring";
	public static final String reverbTypeRoomPlateHall = "RoomPlateHall";
	
	private String  reverbType;
	private boolean reverbOnOff;
	private int time;
	private int pre;
	private int highcut;
	private int lowcut;
	private byte highratio;
	private byte lowratio;
	private byte level;
	private byte reverb;
	private byte filter;
	
	public ReverbData() {
		time = 0;
		pre = 0;
		highcut = 0;
		lowcut = 0;
		highratio = 0;
		lowratio = 0;
		level = 0;
		reverb = 0;
		filter = 0;
		reverbType = "Hall";
		reverbOnOff = false;
	}
	
	public String getReverbType() {
		return reverbType;
	}

	public void setReverbType(String reverbType) {
		this.reverbType = reverbType;
	}

	public boolean isReverbOnOff() {
		return reverbOnOff;
	}

	public void setReverbOnOff(boolean reverbOnOff) {
		this.reverbOnOff = reverbOnOff;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getPre() {
		return pre;
	}

	public void setPre(int pre) {
		this.pre = pre;
	}

	public int getHighcut() {
		return highcut;
	}

	public void setHighcut(int highcut) {
		this.highcut = highcut;
	}

	public int getLowcut() {
		return lowcut;
	}

	public void setLowcut(int lowcut) {
		this.lowcut = lowcut;
	}

	public byte getHighratio() {
		return highratio;
	}

	public void setHighratio(byte highratio) {
		this.highratio = highratio;
	}

	public byte getLowratio() {
		return lowratio;
	}

	public void setLowratio(byte lowratio) {
		this.lowratio = lowratio;
	}

	public byte getLevel() {
		return level;
	}

	public void setLevel(byte level) {
		this.level = level;
	}

	public byte getReverb() {
		return reverb;
	}

	public void setReverb(byte reverb) {
		this.reverb = reverb;
	}

	public byte getFilter() {
		return filter;
	}

	public void setFilter(byte filter) {
		this.filter = filter;
	}
	
	/*
	 * set the current control values in this module. They has been read from patch file
	 */
	public void setPatchValues(byte[] msg){
		if(msg[0] == 0x00){
			reverbType = reverbTypeHall;
		}else if(msg[0] == 0x01){
			reverbType = reverbTypeRoom;
		}else if(msg[0] == 0x02){
			reverbType = reverbTypePlate;
		}else if(msg[0] == 0x03){
			reverbType = reverbTypeSpring;
			reverb = msg[1];
			filter = msg[2];
			time = 0;
			lowcut = 0;
			highcut = 0;
			lowratio = 0;
			highratio = 0;
			level = 0;
		}else{
			Log.d( TAG, "ERROR: ReverbData::setPatchValues() Invalid ReverbType");	
		}
		
		if(!(reverbType.equals(reverbTypeSpring))){
			byte [] temp = new byte [] {0x00, 0x00};
			temp[0] = msg[1];
			temp[1] = msg[2];
			time = SysExCommands.decode7BitByteToInt(temp);
			
			temp[0] = msg[3];
			temp[1] = msg[4];
			pre = SysExCommands.decode7BitByteToInt(temp);
			
			temp[0] = msg[5];
			temp[1] = msg[6];
			lowcut = SysExCommands.decode7BitByteToInt(temp);
			
			temp[0] = msg[7];
			temp[1] = msg[8];
			highcut = SysExCommands.decode7BitByteToInt(temp);

			highratio = msg[9];
			lowratio = msg[10];
			level = msg[11];
		}
		
		// On = 0x00, Off = 0x7f
		if(msg[15] == 0x7f)
			reverbOnOff = false;
		else
			reverbOnOff = true;
		
		Log.d( TAG, "ReverbData::setPatchValues() Reverb Type "+ reverbType + " Time " + time + " Pre " + pre + " Reverb " + reverb + " Filter " + filter + " LowCut " + lowcut +  
				" HighCut " + highcut + " HighRatio" + highratio + " LowRatio" + lowratio + " Level " + level + " On " + Boolean.toString(reverbOnOff));

	}
	
	/*
	 * Read the current control values from this module and convert them to byte-format to write into patch file 
	 */
	public byte[] getPatchValues(){
		byte [] msg = new byte [] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,   0x00, 0x00, 0x00, 0x00};
		
		byte [] temp = SysExCommands.encodeIntegerTo7Bit(time);
		msg[1] = temp[0];
		msg[2] = temp[1];
		
		if(reverbType.equals(reverbTypeHall)){
			msg[0] = 0x00;
		}else if (reverbType.equals(reverbTypeRoom)) {
			msg[0] = 0x01;
		}else if (reverbType.equals(reverbTypePlate)) {
			msg[0] = 0x02;
		}else if (reverbType.equals(reverbTypeSpring)) {
			msg[0] = 0x03;
			msg[1] = reverb;
			msg[2] = filter;
		}

		if(!reverbType.equals(reverbTypeSpring)){
			temp = SysExCommands.encodeIntegerTo7Bit(pre);
			msg[3] = temp[0];
			msg[4] = temp[1];
			
			temp = SysExCommands.encodeIntegerTo7Bit(lowcut);
			msg[5] = temp[0];
			msg[6] = temp[1];
			
			temp = SysExCommands.encodeIntegerTo7Bit(highcut);
			msg[7] = temp[0];
			msg[8] = temp[1];
			
			msg[9] = highratio;
			msg[10] = lowratio;
			msg[11] = level;
		}
		
		// On = 0x00, Off = 0x7f
		if(reverbOnOff == false)
			msg[15] = 0x7f;
		return msg;
	}
	
}
