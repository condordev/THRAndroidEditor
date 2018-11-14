package de.sgrad.yamahathreditor;

import java.util.Arrays;

import android.util.Log;

public class SysExParser {
	private static final String TAG = "THR";
	private MainActivity activity;

	//final private byte [] sysExMsg = new byte [] {(byte)0xf0, 0x43, 0x7d, 0x10, 0x41, 0x30, 0x01, 0x00, 0x00, 0x00, (byte)0xf7};
	
	SysExCommands.Controls ctrl;
	
	SysExParser(MainActivity activity){
        this.activity = activity;
	}
	
	public void read(byte[] in) throws RuntimeException{
		//Log.d( TAG, "read() hex data: " + SysExCommands.byteToHex(in) + " Length " + in.length);
		// single sysex messages
		if(in.length == 11 && in[0] == (byte)0xf0 && in[10] == (byte)0xf7){
			
			// ctrl data separate handling			
			if(in[7] == 0x01 || in[7] == 0x02 || in[7] == 0x03 || in[7] == 0x04 || in[7] == 0x05){
				controlMsg(in[7], in[9]);
				
			// cabinet data separate handling
			}else if (in[7] == 0x06){
				activity.getCabinetData().setPatchValue(in[9]);
				if(activity.cabinets.initialized)
					activity.cabinets.updateUI();
			}else{
				byte typeMasked = (byte) (in[7] & 0xF0);
				byte b1 = in[7];
				byte b2 = in[8];
				byte b3 = in[9];
				
				switch (DeviceIdentifier.fromByte(typeMasked)) {
				case AMP:
					activity.getAmpData().setPatchValue(in[9]);
					
					if(activity.amp.initialized)
						activity.amp.updateUI();
					
					break;
				case COMPRESSOR: // not switchable from unit
					
					CompressorData compData = activity.getCompressorData();
					byte [] tempComp = new byte [] {0x00, 0x00};
					tempComp[0] = b2;
					tempComp[1] = b3;
					
					if(b1 == 0x10){  
						if(b3 == 0x00){
							compData.compressorType = CompressorData.compressorTypeStomp;
						}else if (b3 == 0x01){
							compData.compressorType = CompressorData.compressorTypeRack;
						}
					}else if(b1 == 0x11){  
						if(compData.compressorType.equals(CompressorData.compressorTypeStomp)){
							compData.stompSustain = b3;
						}else{
							compData.rackThreshold = SysExCommands.decode7BitByteToInt(tempComp);
						}
					}else if(b1 == 0x12){ 
						compData.stompOutput = b3;
					}else if(b1 == 0x13){ 
						compData.rackAttack = b3;
					}else if(b1 == 0x14){ 
						compData.rackRelease = b3;
					}else if(b1 == 0x15){  
						compData.rackRatio = b3;
					}else if(b1 == 0x16){  
						compData.rackKnee = b3;
					}else if(b1 == 0x17){ 
						compData.rackOutput = SysExCommands.decode7BitByteToInt(tempComp);
					}
					
					// On/Off
					if(b1 == 0x1f){
						if(b3 == 0x00){
							compData.compressorOnOff = true;
						}else{
							compData.compressorOnOff = false;
						}
					}
					
					if(activity.compressor.initialized)
						activity.compressor.updateUI();
					
					break;
				case MODULATION:
					
					ModulationData modData = activity.getModulationData();
					if(b1 == 0x20){  // mod type select
						if(b3 == 0){
							modData.modType = ModulationData.modTypeChorus;
						}else if(b3 == 1){
							modData.modType = ModulationData.modTypeFlanger;
						}else if(b3 == 2){
							modData.modType = ModulationData.modTypeTremolo;
						}else if (b3 == 3){
							modData.modType = ModulationData.modTypePhaser;
						}
					}else if(b1 == 0x21){  // mod speed or tremFreq
						if(modData.modType.equals(ModulationData.modTypeTremolo)){
							modData.tremFreq = b3;
						}else{
							modData.speed = b3;
						}
					}else if(b1 == 0x22){  
						if(modData.modType.equals(ModulationData.modTypePhaser) || modData.modType.equals(ModulationData.modTypeFlanger)){
							modData.manual = b3;
						}else{
							modData.depth = b3;
						}
					}else if(b1 == 0x23){  
						if(modData.modType.equals(ModulationData.modTypePhaser) || modData.modType.equals(ModulationData.modTypeFlanger)){
							modData.depth = b3;
						}else{
							modData.chorusMix = b3;
						}
					}else if(b1 == 0x24){ 
						modData.feedback = b3;
					}else if(b1 == 0x25){  
						modData.flangerSpread = b3;
					}
					
					// On/Off
					if(b1 == 0x2f){
						if(b3 == 0x00){
							modData.modOnOff = true;
						}else{
							modData.modOnOff = false;
						}
					}
					
					if(activity.modulation.initialized)
						activity.modulation.updateUI();
					
					break;
				case DELAY:
					
					DelayData dlyData = activity.getDelayData();
					byte [] temp = new byte [] {0x00, 0x00};
					temp[0] = b2;
					temp[1] = b3;
					
					if(b1 == 0x31){  
						dlyData.time = SysExCommands.decode7BitByteToInt(temp);
					}else if(b1 == 0x33){  
						dlyData.feedback = b3;
					}else if(b1 == 0x34){  
						dlyData.highcut = SysExCommands.decode7BitByteToInt(temp);
					}else if(b1 == 0x36){  
						dlyData.lowcut = SysExCommands.decode7BitByteToInt(temp);
					}else if(b1 == 0x38){  
						dlyData.level = b3;
					}
					
					// On/Off
					if(b1 == 0x3f){
						if(b3 == 0x00){
							dlyData.delayOnOff = true;
						}else{
							dlyData.delayOnOff  = false;
						}
					}
					
					if(activity.delay.initialized)
						activity.delay.updateUI();
					
					break;
				case REVERB:
					
					ReverbData revData = activity.getReverbData();
					byte [] tempRev = new byte [] {0x00, 0x00};
					tempRev[0] = b2;
					tempRev[1] = b3;
					
					if(b1 == 0x40){  // reverb type select
						if(b3 == 0){
							revData.setReverbType(ReverbData.reverbTypeHall); 
						}else if(b3 == 1){
							revData.setReverbType(ReverbData.reverbTypeRoom); 
						}else if(b3 == 2){
							revData.setReverbType(ReverbData.reverbTypePlate); 
						}else if (b3 == 3){
							revData.setReverbType(ReverbData.reverbTypeSpring); 
						}
					}else if(b1 == 0x41){  
						if(revData.getReverbType().equals(ReverbData.reverbTypeSpring)){
							revData.setReverb(b3);
						}else{
							revData.setTime(SysExCommands.decode7BitByteToInt(tempRev));
						}

					}else if(b1 == 0x43){  
						revData.setPre(SysExCommands.decode7BitByteToInt(tempRev));
					}else if(b1 == 0x45){  
						revData.setLowcut(SysExCommands.decode7BitByteToInt(tempRev));
					}else if(b1 == 0x47){  
						revData.setHighcut(SysExCommands.decode7BitByteToInt(tempRev));
					}else if(b1 == 0x49){  
						revData.setHighratio(b3);
					}else if(b1 == 0x4a){  
						revData.setLowratio(b3);
					}else if(b1 == 0x4b){  
						revData.setLevel(b3);
					}else if(b1 == 0x42){  
						revData.setFilter(b3);
					}
					
					// On/Off
					if(b1 == 0x4f){
						if(b3 == 0x00){
							revData.setReverbOnOff(true);
						}else{
							revData.setReverbOnOff(false);
						}
					}
					//Log.d( TAG, "SysExParser: " + revData.getPre());
					
					if(activity.reverb.initialized)
						activity.reverb.updateUI();
					
					break;
				case GATE: // not switchable from unit
					GateData gateData = activity.getGateData();
					
					if(b1 == 0x51){  
						gateData.threshold = b3;
					}else if(b1 == 0x52){  
						gateData.release = b3;
					}
					
					if(b1 == 0x5f){
						if(b3 == 0x00){
							gateData.gateOnOff = true;
						}else{
							gateData.gateOnOff = false;
						}
					}
					
					if(activity.gate.initialized)
						activity.gate.updateUI();
					
					break;
				default:
					break;
				}
			}
		}else if(in.length == 276){ // sysex dump
			byte[] name = Arrays.copyOfRange(in, 18, 146);
			//Log.d( TAG, "Dump Name: " + Patch.bytesToString(name) + " Data: " + SysExCommands.byteToHex(name) + " Lenght: " + name.length + " byte(s)");
			
			String strName = Patch.bytesToString(name);
			if(strName.equals("") || strName.equals(" ")){
				strName = "nameless patch";
			}
			
			activity.btnPatch.setText(strName);
			
			// skip header and name 
			byte[] payload = Arrays.copyOfRange(in, 146 , 276);   // 18 + 128 = 146 zero based index -> 145
			//Log.d( TAG, "Payload: " + SysExCommands.byteToHex(payload) + " Lenght: " + payload.length + " byte(s)");
			
			// amp type 
			activity.getAmpData().setPatchValue(payload[0]);
			
			// ctrl values 
			byte[] ctrls = Arrays.copyOfRange(payload, 1 , 6);
			//Log.d( TAG, "ctrls:" + SysExCommands.byteToHex(ctrls) + " Lenght: " + ctrls.length + " byte(s)");
			activity.setPatchValues(ctrls);
			
			//cabinet type 
			activity.getCabinetData().setPatchValue(payload[6]);
			
			//compressor
			byte[] compressor = Arrays.copyOfRange(payload, 16 , 32);
			//Log.d( TAG, "compressor:" + SysExCommands.byteToHex(compressor) + " Lenght: " + compressor.length + " byte(s)");
			activity.getCompressorData().setPatchValues(compressor);
			
			//modulation
			byte[] modulation = Arrays.copyOfRange(payload, 32 , 48);
			//Log.d( TAG, "modulation:" + SysExCommands.byteToHex(modulation) + " Lenght: " + modulation.length + " byte(s)");
			activity.getModulationData().setPatchValues(modulation);
			
			//delay
			byte[] delay = Arrays.copyOfRange(payload, 48 , 64);
			//Log.d( TAG, "delay:" + SysExCommands.byteToHex(delay) + " Lenght: " + delay.length + " byte(s)");
			activity.getDelayData().setPatchValues(delay);
			
			//reverb
			byte[] reverb = Arrays.copyOfRange(payload, 64 , 80);
			//Log.d( TAG, "reverb:" + SysExCommands.byteToHex(reverb) + " Lenght: " + reverb.length + " byte(s)");
			activity.getReverbData().setPatchValues(reverb);
			
			//gate
			byte[] gate = Arrays.copyOfRange(payload, 80 , 96);
			//Log.d( TAG, "gate:" + SysExCommands.byteToHex(gate) + " Lenght: " + gate.length + " byte(s)");
			activity.getGateData().setPatchValues(gate);	

			
			if(activity.reverb.initialized)
				activity.reverb.updateUI();
			if(activity.gate.initialized)
				activity.gate.updateUI();
			if(activity.delay.initialized)
				activity.delay.updateUI();
			if(activity.modulation.initialized)
				activity.modulation.updateUI();
			if(activity.compressor.initialized)
				activity.compressor.updateUI();
			if(activity.amp.initialized)
				activity.amp.updateUI();
			if(activity.cabinets.initialized)
				activity.cabinets.updateUI();
			
		}else if(in.length == 13){
			
		}
	}
	
	private void controlMsg(byte ctrlByte, byte value){
		if (ctrlByte == SysExCommands.Controls.GAIN.getValue()){
			activity.onSysExControlChange(SysExCommands.Controls.GAIN, (int)value);
		}else if (ctrlByte == SysExCommands.Controls.MASTER.getValue()){
			activity.onSysExControlChange(SysExCommands.Controls.MASTER, (int)value);
		}else if (ctrlByte == SysExCommands.Controls.BASS.getValue()){
			activity.onSysExControlChange(SysExCommands.Controls.BASS, (int)value);
		}else if (ctrlByte == SysExCommands.Controls.MIDDLE.getValue()){
			activity.onSysExControlChange(SysExCommands.Controls.MIDDLE, (int)value);
		}else if (ctrlByte == SysExCommands.Controls.TREBLE.getValue()){
			activity.onSysExControlChange(SysExCommands.Controls.TREBLE, (int)value);
		}
	}
	
	public enum DeviceIdentifier {
		AMP((byte)0x00),
		COMPRESSOR((byte)0x10),
		MODULATION((byte)0x20),
		DELAY((byte)0x30),
		REVERB((byte)0x40),
		GATE((byte)0x50);

		byte value;
		DeviceIdentifier(byte value)  { this.value = value; }
		void set(byte value) { this.value = value; }

		@Override
		public String toString() {
			switch(this) {
			case AMP: return "Amp";
			case COMPRESSOR: return "Compressor";
			case MODULATION: return "Modulation";
			case DELAY: return "Delay";
			case REVERB: return "Reverb";
			case GATE: return "Gate";
			default: throw new IllegalArgumentException();
			}
		}
		
	    public static DeviceIdentifier fromByte(byte b) {
	        for (DeviceIdentifier op : values()) {
	            if (op.value == b) {
	            	//Log.d( TAG, "DeviceIdentifier Found: " + op.toString() + " Byte: " + SysExCommands.byteToHex(b));
	            	return op;
	            }
	        }
	        throw new IllegalArgumentException();
	    }
	}

}
