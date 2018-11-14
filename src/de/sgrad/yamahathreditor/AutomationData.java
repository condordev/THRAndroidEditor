package de.sgrad.yamahathreditor;

import android.widget.TextView;

public class AutomationData extends SimpleTimer{
	
	public enum DataType {
		INT,
		BYTE;
	}
	
	int value = 0;
	int minValue = 0;
	int maxValue = 100;
	String function = "";
	
	TextView rangeSeekBarText = null;
	byte[] sysEx = null;
	String deviceType = "";
	
	DataType type = DataType.INT;
	boolean isAssigned = false;
	MainActivity activity = null;
	
	public AutomationData(TextView rangeSeekBarText, MainActivity activity ) {
		
	    this.activity = activity;
		this.rangeSeekBarText = rangeSeekBarText;
	}
	
	public void assignFunction(String function){
		this.function = function;
	}
	
	public void assignDeviceType(String devType){
		this.deviceType = devType;
	}
	
	public void updateSysEx(){
		if(type == DataType.BYTE){
			sysEx[9] = (byte)value;
		}else if(type == DataType.INT){
			byte [] temp = SysExCommands.encodeIntegerTo7Bit(value);
			sysEx[8] = temp[0];
			sysEx[9] = temp[1];
		}
	}

	@Override
	void onTimer() {
		activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //	Log.d( TAG, "Timer Function: " + data.function + " Assigned: " + Boolean.toString(data.isAssigned) + " rangeSeekBarText "
                //	             + data.rangeSeekBarText.getText() + " minValue: " + data.minValue + " maxValue: " + data.maxValue + " value: "+ data.value);
            	if(isAssigned && value < maxValue ){
        			value++;
        			updateSysEx();
        			activity.onDataChangeUI(sysEx);
        			
        			if(deviceType.equals(Automation.deviceTypeControl)){
        				if(function.equals(Automation.controlGain)){
        					rangeSeekBarText.setText(Automation.controlGain + " " + String.valueOf(value));
        					activity.setGain((byte)value);
        					
        				}else if (function.equals(Automation.controlMaster)){
        					rangeSeekBarText.setText(Automation.controlMaster + " " + String.valueOf(value));
        					activity.setMaster((byte)value);
        					
        				}else if (function.equals(Automation.controlBass)){
        					rangeSeekBarText.setText(Automation.controlBass + " " + String.valueOf(value));
        					activity.setBass((byte)value);			
        					
        				}else if (function.equals(Automation.controlMiddle)){
        					rangeSeekBarText.setText(Automation.controlMiddle + " " + String.valueOf(value));
        					activity.setMiddle((byte)value);				
        					
        				}else if (function.equals(Automation.controlTreble)){
        					rangeSeekBarText.setText(Automation.controlTreble + " " + String.valueOf(value));
        					activity.setTreble((byte)value);		
        				}
        				activity.refreshContolUI();
        			}else if (deviceType.equals(Automation.deviceTypeDelay)){
        				if(function.equals(Automation.delayTime)){
        					rangeSeekBarText.setText(Automation.delayTime + " " + value/10.0 + " ms");
        					activity.getDelayData().time = value;
        					
        				}else if (function.equals(Automation.delayFeedback)){
        					rangeSeekBarText.setText(Automation.delayFeedback + " " + String.valueOf(value));
        					activity.getDelayData().feedback = (byte) value;        					
        					
        				}else if (function.equals(Automation.delayHighCut)){
        					rangeSeekBarText.setText(Automation.delayHighCut + " " + String.valueOf(value));
        					activity.getDelayData().highcut = value;        					
        					
        				}else if (function.equals(Automation.delayLowCut)){
        					rangeSeekBarText.setText(Automation.delayLowCut + " " + String.valueOf(value));
        					activity.getDelayData().lowcut = value;        					
        					
        				}else if (function.equals(Automation.delayLevel)){
        					rangeSeekBarText.setText(Automation.delayLevel + " " + String.valueOf(value));
        					activity.getDelayData().level = (byte) value;        					
        				}
        				activity.delay.updateUI();
        			}
            	}
            }
        });
	}

}
