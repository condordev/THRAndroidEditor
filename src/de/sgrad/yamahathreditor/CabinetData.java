package de.sgrad.yamahathreditor;

import de.sgrad.yamahathreditor.SysExCommands.CabinetType;
import android.util.Log;

public class CabinetData {
	
	public CabinetType currentBox;
	
	public CabinetData() {
		currentBox = CabinetType.NONE;
	}
	
	public byte getPatchValue(){
		return currentBox.value;
	}
	
	public void setPatchValue(byte v){
		currentBox = CabinetType.fromByte(v);
		//Log.d( TAG, "CABINET type: " + currentBox.toString());
	}
}
