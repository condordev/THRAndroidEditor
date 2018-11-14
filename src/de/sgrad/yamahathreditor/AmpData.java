package de.sgrad.yamahathreditor;

import de.sgrad.yamahathreditor.SysExCommands.AmpType;
import android.util.Log;

public class AmpData {
	public AmpType currentAmp;
	
	public AmpData() {
		currentAmp = AmpType.ACO;
	}
	
	public byte getPatchValue(){
		return currentAmp.value;
	}
	
	public void setPatchValue(byte v){
		currentAmp = AmpType.fromByte(v);
		//Log.d( "THR", "AMP type: " + currentAmp.toString());
	}
}
