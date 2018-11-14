package de.sgrad.yamahathreditor;

import java.math.BigInteger;
import java.util.Formatter;

import android.util.Log;

public class SysExCommands {
	private static final String TAG = "THR";

	final private byte [] sysExMsgFrame = new byte [] {(byte)0xf0, 0x43, 0x7d, 0x10, 0x41, 0x30, 0x01, 0x00, 0x00, 0x00, (byte)0xf7};
	               //       byte [] msg = new byte [] {(byte)0xf0, 0x43, 0x7d, 0x30, 0x41, 0x30, 0x00, 0x00, (byte)0xf7}; // WIDE on
	public enum THRModel {
		THR5((byte)0x30),
		THR10((byte)0x31),
		THR10X((byte)0x32),
		THR10C((byte)0x33),
		THR5A((byte)0x34),
		INVALID((byte)0x00);
		
		byte value;
		THRModel(byte value)  { this.value = value; }
		void set(byte value) { this.value = value; }

		@Override
		public String toString() {
			switch(this) {
			case THR5: return "THR5";
			case THR10: return "THR10";
			case THR10X: return "THR10X";
			case THR10C: return "THR10C";
			case THR5A: return "THR5A";
			case INVALID: return "Invalid Model";
			default: throw new IllegalArgumentException();
			}
		}
	    
	    public static THRModel fromByte(byte b) {
	        for (THRModel op : values()) {
	            if (op.value == b) {
	            	//Log.d( TAG, "AmpType Found: " + op.toString() + " Byte: " + SysExCommands.byteToHex(b));
	            	return op;
	            }
	        }
	        throw new IllegalArgumentException();
	    }
	}
		
	public enum SwitchableDevices {
		GATE((byte)0x5f),
		COMPRESSOR((byte)0x1f),
		MODULATION((byte)0x2f),
		DELAY((byte)0x3f),
		REVERB((byte)0x4f);

		byte value;
		SwitchableDevices(byte value) { this.value = value; }

		@Override
		public String toString() {
			switch(this) {
			case GATE: return "Gate";
			case COMPRESSOR: return "Compressor";
			case MODULATION: return "Modulation";
			case DELAY: return "Delay";
			case REVERB: return "Reverb";
			default: throw new IllegalArgumentException();
			}
		}
	}
	
	
	public enum AmpType {
		CLEAN((byte)0x00),
		CRUNCH((byte)0x01),
		LEAD((byte)0x02),
		BRITHI((byte)0x03),
		MODERN((byte)0x04),
		BASS((byte)0x05),
		ACO((byte)0x06),
		FLAT((byte)0x07);

		byte value;
		AmpType(byte value)  { this.value = value; }
		void set(byte value) { this.value = value; }

		//@Override
		public String toString(THRModel model) {
			if(model == THRModel.THR10){
				switch(this) {
					case CLEAN: return "Clean";
					case CRUNCH: return "Crunch";
					case LEAD: return "Lead";
					case BRITHI: return "BritHi";
					case MODERN: return "Modern";
					case BASS: return "Bass";
					case ACO: return "Aco";
					case FLAT: return "Flat";
					default: throw new IllegalArgumentException();
				}
			}else if (model == THRModel.THR10C){
				switch(this) {
					case CLEAN: return "Deluxe";
					case CRUNCH: return "Class A";
					case LEAD: return "US Blues";
					case BRITHI: return "Brit Blues";
					case MODERN: return "Mini";
					case BASS: return "Bass";
					case ACO: return "Aco";
					case FLAT: return "Flat";
					default: throw new IllegalArgumentException();
				}
			}
			return "";
		}
	    
	    public static AmpType fromByte(byte b) {
	    	//Log.d( TAG, "AmpType Enter: " + values().length + " Byte: " + SysExCommands.byteToHex(b));
	        for (AmpType op : values()) {
	        	//Log.d( TAG, "AmpType: " + op.toString() + " Byte: " + SysExCommands.byteToHex(b));
	            if (op.value == b) {
	            	//Log.d( TAG, "AmpType Found: " + op.toString() + " Byte: " + SysExCommands.byteToHex(b));
	            	return op;
	            }
	        }
	        throw new IllegalArgumentException();
	    }
	}
	
	public enum CabinetType {
		US4x12((byte)0),
		US2x12((byte)1),
		Brit4x12((byte)2),
		Brit2x12((byte)3),
		ONEx12((byte)4),
		FOURx10((byte)5),
		NONE((byte)6);

		byte value;
		CabinetType(byte value) { this.value = value; }

		//@Override
		public String toString(THRModel model) {
			if(model == THRModel.THR10){
				switch(this) {
				case US4x12: return "US4x12";
				case US2x12: return "US2x12";
				case Brit4x12: return "Brit4x12";
				case Brit2x12: return "Brit2x12";
				case ONEx12: return "1x12";
				case FOURx10: return "4x10";
				case NONE: return "None";
					default: throw new IllegalArgumentException();
				}
			}else if (model == THRModel.THR10C){
				switch(this) {
				case US4x12: return "BritBlues 2x12";
				case US2x12: return "US2x12";
				case Brit4x12: return "California 1x12";
				case Brit2x12: return "American 1x12";
				case ONEx12: return "Boutique 2x12";
				case FOURx10: return "Yamaha 2x12";
				case NONE: return "None";
					default: throw new IllegalArgumentException();
				}
			}
			return "";
		}
		
	    public static CabinetType fromByte(byte b) {
	        for (CabinetType op : values()) {
	            if (op.value == b) {
	            	//Log.d( TAG, "CabinetType Found: " + op.toString() + " Byte: " + byteToHex(b));
	            	return op;
	            }
	        }
	        throw new IllegalArgumentException();
	    }
	}
	
	public enum ReverbType {
		HALL((byte)0),
		ROOM((byte)1),
		PLATE((byte)2),
		SPRING((byte)3);

		byte value;
		ReverbType(byte value) { this.value = value; }

		@Override
		public String toString() {
			switch(this) {
			case HALL: return "Hall";
			case ROOM: return "Room";
			case PLATE: return "Plate";
			case SPRING: return "Spring";
			default: throw new IllegalArgumentException();
			}
		}
		
	    public static ReverbType fromByte(byte b) {
	        for (ReverbType op : values()) {
	            if (op.value == b) {
	            	//Log.d( TAG, "ReverbType Found: " + op.toString() + " Byte: " + byteToHex(b));
	            	return op;
	            }
	        }
	        throw new IllegalArgumentException();
	    }
	}	
	
	public enum ModulationType {
		CHORUS((byte)0),
		FLANGER((byte)1),
		TREMOLO((byte)2),
		PHASER((byte)3);

		byte value;
		ModulationType(byte value) { this.value = value; }

		@Override
		public String toString() {
			switch(this) {
			case CHORUS: return "Chorus";
			case FLANGER: return "Flanger";
			case TREMOLO: return "Tremolo";
			case PHASER: return "Phaser";
			default: throw new IllegalArgumentException();
			}
		}
		
	    public static ModulationType fromByte(byte b) {
	        for (ModulationType op : values()) {
	            if (op.value == b) {
	            	//Log.d( TAG, "ModulationType Found: " + op.toString() + " Byte: " + byteToHex(b));
	            	return op;
	            }
	        }
	        throw new IllegalArgumentException();
	    }
	}	
	
	public enum Controls {
		GAIN((byte)1),
		MASTER((byte)2),
		BASS((byte)3),
		MIDDLE((byte)4),
		TREBLE((byte)5),
		PRESENCE((byte)6);

		byte value;
		Controls(byte value) { this.value = value; }

        public byte getValue() {
            return this.value;
        }
		
		@Override
		public String toString() {
			switch(this) {
			case GAIN: return "Gain";
			case MASTER: return "Master";
			case BASS: return "Bass";
			case MIDDLE: return "Middle";
			case TREBLE: return "Treble";
			case PRESENCE: return "Presence";
			default: throw new IllegalArgumentException();
			}
		}
		
	    public static Controls fromByte(byte b) {
	        for (Controls op : values()) {
	            if (op.value == b) {
	            	//Log.d( TAG, "Controls Found: " + op.toString() + " Byte: " + byteToHex(b));
	            	return op;
	            }
	        }
	        throw new IllegalArgumentException();
	    }
	}
	
	SysExCommands(){
		//System.out.println(sysExMsgFrame[10]&0xFF);
		//getAmpTypeSysEx(AmpType.CRUNCH);
	}
	
	public byte [] getCabinetTypeSysEx(CabinetType type){
		byte[] sysExMsgCab = sysExMsgFrame.clone();
		sysExMsgCab[7] = 0x06;
		sysExMsgCab[9] = type.value;
		//Log.d( TAG, "getCabTypeSysEx() Cabinet type: " + type.toString() + " SysEx: " + byteToHex( sysExMsgCab ));
		return sysExMsgCab;
	}
	
	public byte [] getControlSysEx(Controls type, byte ctrlValue){
		byte[] sysExMsgControl = sysExMsgFrame.clone();
		sysExMsgControl[7] = type.value;
		sysExMsgControl[9] = ctrlValue;
		//Log.d( TAG, "getControlSysEx() Control type: " + type.toString() + " SysEx: " + byteToHex( sysExMsgControl ));
		return sysExMsgControl;
	}
	
	public byte [] getAmpTypeSysEx(AmpType type){
		byte[] sysExMsgAmp = sysExMsgFrame.clone();
		sysExMsgAmp[9] = type.value;
		//Log.d( TAG, "getAmpTypeSysEx() Amp type: " + type.toString() + " SysEx: " + byteToHex( sysExMsgAmp ));
		return sysExMsgAmp;
	}
	
	public byte [] getDeviceOnOffSysEx(SwitchableDevices device, boolean onOff){
		byte[] sysExMsg = sysExMsgFrame.clone();
		sysExMsg[7] = device.value;
		
		// On = 0x00, Off = 0x7f
		if(onOff == false)
			sysExMsg[9] = 0x7f;
			
		//Log.d( TAG, "getDeviceOnOffSysEx() " + device.toString() + " SysEx: " + byteToHex( sysExMsg ));
		return sysExMsg;
	}
	
	public byte [] getCompressorTypeSelectSysEx(String compressorType){
		byte[] sysExMsg = sysExMsgFrame.clone();
		sysExMsg[7] = 0x10;
		
		// Stomp 10 00 00  ; Rack 10 00 01
		if(compressorType.equals(CompressorData.compressorTypeRack))
			sysExMsg[9] = 0x01;
			
		//Log.d( TAG, "getCompressorTypeSelectSysEx() " + compressorType + " SysEx: " + byteToHex( sysExMsg ));
		return sysExMsg;
	}
	
	public byte [] getReverbTypeSelectSysEx(String reverbType){
		byte[] sysExMsg = sysExMsgFrame.clone();
		sysExMsg[7] = 0x40;
		
		if(reverbType.equals(ReverbData.reverbTypeHall))
			sysExMsg[9] = 0x00;
		else if(reverbType.equals(ReverbData.reverbTypeRoom))
			sysExMsg[9] = 0x01;
		else if(reverbType.equals(ReverbData.reverbTypePlate))
			sysExMsg[9] = 0x02;	
		else if(reverbType.equals(ReverbData.reverbTypeSpring))
			sysExMsg[9] = 0x03;	
			
		//Log.d( TAG, "getReverbTypeSelectSysEx() " + reverbType + " SysEx: " + byteToHex( sysExMsg ));
		return sysExMsg;
	}
	
	public byte [] getModulationTypeSelectSysEx(String modType){
		byte[] sysExMsg = sysExMsgFrame.clone();
		sysExMsg[7] = 0x20;
		
		if(modType.equals(ModulationData.modTypeChorus))
			sysExMsg[9] = 0x00;
		else if(modType.equals(ModulationData.modTypeFlanger))
			sysExMsg[9] = 0x01;
		else if(modType.equals(ModulationData.modTypePhaser))
			sysExMsg[9] = 0x03;	
		else if(modType.equals(ModulationData.modTypeTremolo))
			sysExMsg[9] = 0x02;	
			
		//Log.d( TAG, "getModulationTypeSelectSysEx() " + modType + " SysEx: " + byteToHex( sysExMsg ));
		return sysExMsg;
	}
	
	public byte [] getLEDOnOffSysEx(boolean on){
		byte [] msg = new byte [] {(byte)0xf0, 0x43, 0x7d, 0x30, 0x41, 0x30, 0x01, 0x00, (byte)0xf7}; // LED on 
		if(!on){
			msg[7] = 0x01;
		}
			
		//Log.d( TAG, "getLEDOnOffSysEx() " + byteToHex( msg ));
		return msg;
	}
	
	public byte [] getWideModeOnOffSysEx(boolean on){
		byte [] msg = new byte [] {(byte)0xf0, 0x43, 0x7d, 0x30, 0x41, 0x30, 0x00, 0x00, (byte)0xf7}; // WIDE on 
		if(!on){
			msg[7] = 0x01;
		}
			
		//Log.d( TAG, "getLEDOnOffSysEx() " + byteToHex( msg ));
		return msg;
	}
	
	public byte [] getCompressorValuesSysEx(String compressorType, byte ctrlValue, int intCtrlValue, String parameter){
		byte[] sysExMsg = sysExMsgFrame.clone();
		
		byte[] converted = encodeIntegerTo7Bit(intCtrlValue);
		sysExMsg[8] = converted[0];
		sysExMsg[9] = converted[1];
		
		if(compressorType.equals(CompressorData.compressorTypeStomp)){
			if(parameter.equals("Sustain")){
				sysExMsg[7] = 0x11;
			}else if(parameter.equals("Output")){
				sysExMsg[7] = 0x12;
			}
			sysExMsg[9] = ctrlValue;
		}else if(compressorType.equals(CompressorData.compressorTypeRack)){
			if(parameter.equals("Output")){
				sysExMsg[7] = 0x17;
			//	sysExMsg[8] = (byte) ((intCtrlValue >> 8) & 0xFF);	
			//	sysExMsg[9] = (byte) (intCtrlValue & 0xFF);	
			}else if(parameter.equals("Threshold")){
				sysExMsg[7] = 0x11;
			//	sysExMsg[8] = (byte) ((intCtrlValue >> 8) & 0xFF);	
			//	sysExMsg[9] = (byte) (intCtrlValue & 0xFF);
				//Log.d( TAG, "getCompressorValuesSysEx() sysExMsg[8]: " + byteToHex ((byte) (intCtrlValue & 0xFF)) + " sysExMsg[9]: " + byteToHex ((byte) ((intCtrlValue >> 8) & 0xFF)));
			}else if(parameter.equals("Attack")){
				sysExMsg[7] = 0x13;
				sysExMsg[9] = ctrlValue;
			}else if(parameter.equals("Release")){
				sysExMsg[7] = 0x14;
				sysExMsg[9] = ctrlValue;
			}else if(parameter.equals("Ratio")){
				sysExMsg[7] = 0x15;
				sysExMsg[9] = ctrlValue;
			}else if(parameter.equals("Knee")){
				sysExMsg[7] = 0x16;
				sysExMsg[9] = ctrlValue;
			}
		}
			
		//Log.d( TAG, "getCompressorValuesSysEx() Type: " + compressorType + " Param: " + parameter + " SysEx: " + byteToHex( sysExMsg ));
		return sysExMsg;
	}
	
	public byte [] getDelayValuesSysEx(int intCtrlValue, String parameter){
		byte[] sysExMsg = sysExMsgFrame.clone();
		byte[] converted = encodeIntegerTo7Bit(intCtrlValue);
		sysExMsg[8] = converted[0];
		sysExMsg[9] = converted[1];
		if(parameter.equals("Time")){
			sysExMsg[7] = 0x31;
			//sysExMsg[8] = (byte) ((intCtrlValue >> 8) & 0xFF);	
			//sysExMsg[9] = (byte) (intCtrlValue & 0xFF);	
		}else if(parameter.equals("HighCut")){
			sysExMsg[7] = 0x34;
			//sysExMsg[8] = (byte) ((intCtrlValue >> 8) & 0xFF);	
			//sysExMsg[9] = (byte) (intCtrlValue & 0xFF);
		}else if(parameter.equals("LowCut")){
			sysExMsg[7] = 0x36;
			//sysExMsg[8] = (byte) ((intCtrlValue >> 8) & 0xFF);	
			//sysExMsg[9] = (byte) (intCtrlValue & 0xFF);			
		}else if(parameter.equals("Level")){
			sysExMsg[7] = 0x38;
			sysExMsg[9] = (byte)intCtrlValue;
		}else if(parameter.equals("Feedback")){
			sysExMsg[7] = 0x33;
			sysExMsg[9] = (byte)intCtrlValue;
		}
			
		//Log.d(TAG, "getDelayValuesSysEx(): Param: " + parameter + " SysEx: " + byteToHex( sysExMsg ));
		return sysExMsg;
	}
	
	public byte [] getGateValuesSysEx(int intCtrlValue, String parameter){
		byte[] sysExMsg = sysExMsgFrame.clone();
		
		if(parameter.equals("Release")){
			sysExMsg[7] = 0x52;
			sysExMsg[9] = (byte)intCtrlValue;
		}else if(parameter.equals("Threshold")){
			sysExMsg[7] = 0x51;
			sysExMsg[9] = (byte)intCtrlValue;
		}
			
		//Log.d(TAG, "getGateValuesSysEx(): Param: " + parameter + " SysEx: " + byteToHex( sysExMsg ));
		return sysExMsg;
	}
	
	public byte [] getReverbValuesSysEx(String reverbType, int intCtrlValue, String parameter){
		byte[] sysExMsg = sysExMsgFrame.clone();
		byte[] converted = encodeIntegerTo7Bit(intCtrlValue);
		sysExMsg[8] = converted[0];
		sysExMsg[9] = converted[1];
		
		if(reverbType.equals(ReverbData.reverbTypeRoomPlateHall)){
			if(parameter.equals("Time")){
				sysExMsg[7] = 0x41;
				//sysExMsg[8] = (byte) ((intCtrlValue >> 8) & 0xFF);	
				//sysExMsg[9] = (byte) (intCtrlValue & 0xFF);	
			}else if(parameter.equals("Pre")){
				sysExMsg[7] = 0x43;
				//sysExMsg[8] = (byte) ((intCtrlValue >> 8) & 0xFF);	
				//sysExMsg[9] = (byte) (intCtrlValue & 0xFF);
			}else if(parameter.equals("HighCut")){
				sysExMsg[7] = 0x47;
				//sysExMsg[8] = (byte) ((intCtrlValue >> 8) & 0xFF);	
				//sysExMsg[9] = (byte) (intCtrlValue & 0xFF);			
			}else if(parameter.equals("LowCut")){
				sysExMsg[7] = 0x45;
				//sysExMsg[8] = (byte) ((intCtrlValue >> 8) & 0xFF);	
				//sysExMsg[9] = (byte) (intCtrlValue & 0xFF);		
			}else if(parameter.equals("HighRatio")){
				sysExMsg[7] = 0x49;
				sysExMsg[9] = (byte)intCtrlValue;
			}else if(parameter.equals("LowRatio")){
				sysExMsg[7] = 0x4a;
				sysExMsg[9] = (byte)intCtrlValue;
			}else if(parameter.equals("Level")){
				sysExMsg[7] = 0x4b;
				sysExMsg[9] = (byte)intCtrlValue;
			}
		}else if(reverbType.equals(ReverbData.reverbTypeSpring)){
			if(parameter.equals("Reverb")){
				sysExMsg[7] = 0x41;
				sysExMsg[9] = (byte)intCtrlValue;
			}else if (parameter.equals("Filter")){
				sysExMsg[7] = 0x42;
				sysExMsg[9] = (byte)intCtrlValue;
			}
		}
			
		//Log.d(TAG, "getReverbValuesSysEx(): Param: " + parameter + " SysEx: " + byteToHex( sysExMsg ));
		return sysExMsg;
	}
	
	public byte [] getModulationValuesSysEx(String modType, int intCtrlValue, String parameter){
		byte[] sysExMsg = sysExMsgFrame.clone();
		
		if(modType.equals(ModulationData.modTypeChorus)){
			if(parameter.equals("Speed")){
				sysExMsg[7] = 0x21;
				sysExMsg[9] = (byte)intCtrlValue;
			}else if(parameter.equals("Depth")){
				sysExMsg[7] = 0x22;
				sysExMsg[9] = (byte)intCtrlValue;
			}else if(parameter.equals("Mix")){
				sysExMsg[7] = 0x23;
				sysExMsg[9] = (byte)intCtrlValue;		
			}
		}else if(modType.equals(ModulationData.modTypeFlanger)){
			if(parameter.equals("Speed")){
				sysExMsg[7] = 0x21;
				sysExMsg[9] = (byte)intCtrlValue;
			}else if(parameter.equals("Manual")){
				sysExMsg[7] = 0x22;
				sysExMsg[9] = (byte)intCtrlValue;
			}else if(parameter.equals("Depth")){
				sysExMsg[7] = 0x23;
				sysExMsg[9] = (byte)intCtrlValue;		
			}else if(parameter.equals("Feedback")){
				sysExMsg[7] = 0x24;
				sysExMsg[9] = (byte)intCtrlValue;
			}else if(parameter.equals("Spread")){
				sysExMsg[7] = 0x25;
				sysExMsg[9] = (byte)intCtrlValue;		
			}
		}else if(modType.equals(ModulationData.modTypeTremolo)){
			if(parameter.equals("Freq")){
				sysExMsg[7] = 0x21;
				sysExMsg[9] = (byte)intCtrlValue;
			}else if(parameter.equals("Depth")){
				sysExMsg[7] = 0x22;
				sysExMsg[9] = (byte)intCtrlValue;
			}
		}else if(modType.equals(ModulationData.modTypePhaser)){
			if(parameter.equals("Speed")){
				sysExMsg[7] = 0x21;
				sysExMsg[9] = (byte)intCtrlValue;
			}else if(parameter.equals("Manual")){
				sysExMsg[7] = 0x22;
				sysExMsg[9] = (byte)intCtrlValue;
			}else if(parameter.equals("Depth")){
				sysExMsg[7] = 0x23;
				sysExMsg[9] = (byte)intCtrlValue;		
			}else if(parameter.equals("Feedback")){
				sysExMsg[7] = 0x24;
				sysExMsg[9] = (byte)intCtrlValue;		
			}
		}
			
		//Log.d(TAG, "getModulationValuesSysEx(): Param: " + parameter + " SysEx: " + byteToHex( sysExMsg ));
		return sysExMsg;
	}
	
	
    public static String byteToHex(final byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes){
            formatter.format("%02x ", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
    
    public static String byteToHex(final byte value) {
        Formatter formatter = new Formatter();
        formatter.format("%02x ", value);
        String result = formatter.toString();
        formatter.close();
        return result;
    }
    
    static public byte[] encodeIntegerTo7Bit(int v){
    	byte val [] = new byte [] {0x00, 0x00};
    	val[0] = (byte) ((v >> 8) & 0xFF);	
    	val[1] = (byte) (v & 0xFF);	
    	
    	// shift everything to right
    	val[0] <<= 1;
    	
        //check if  the MSB (1000 0000) bit  is set
        if((val[1] & ((byte)0x80)) != 0) {
        	// set 8th bit to 0 in second byte
        	val[1] = (byte) (val[1] & ~(1 << 7));
        	// set 0 bit in first byte
        	val[0] = (byte) (val[0] | (1 << 0));
        }
        
       // Log.d(TAG, "encodeIntegerTo7Bit(): int value: " + v + " mem " + byteToHex( val ) + " val[0] = " + byteToHex( val[0] ) + " val[1] = " + byteToHex( val[1] ));
        return val;
    }
    
   static  public int decode7BitByteToInt(byte[] val){
        //check if  the LSBit (0000 0001) bit  is set
        if((val[0] & ((byte)0x01)) != 0) {
        	// set 8th bit in second byte
        	val[1] = (byte) (val[1] | (1 << 7));
        }
        
    	// shift everything to left in first byte
    	val[0] >>= 1;
        	
        // convert byte [] to int
        int result = new BigInteger(val).intValue();
        
       // Log.d(TAG, "decode7BitByteToInt(): Byte val[0]: " + byteToHex( val[0] ) + " val[1] = " + byteToHex( val[1] ) + " Integer: " + result + " 0x" + Integer.toHexString(result));
        return result;
    }
    
/*    
     set the seventh bit to 1:
    	b = (byte) (b | (1 << 6));
    	To set the sixth bit to zero:
    	b = (byte) (b & ~(1 << 5));
    	(The bit positions are effectively 0-based, so that's why the "seventh bit" maps to 1 << 6 instead of 1 << 7.)
    	
    	b |= (1 << bitIndex); // set a bit to 1
		b &= ~(1 << bitIndex); // set a bit to 0
		
		2E=00101110 E0=11100000 (12000Hz)
		5D=01011101 60=01100000
		
		0F=00001111 46=01000110 (391,0 ms)
		1E=00011110 46=01000110
		
		27=00100111 0F=00001111 (999.9ms)
		4E=01001110 0F=00001111
*/
}
