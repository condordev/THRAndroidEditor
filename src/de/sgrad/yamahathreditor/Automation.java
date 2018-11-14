package de.sgrad.yamahathreditor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import de.sgrad.yamahathreditor.AutomationData.DataType;
import de.sgrad.yamahathreditor.SysExCommands.Controls;
import de.sgrad.yamahathreditor.SysExCommands.THRModel;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Automation {
	public static String TAG = "THR";
	
	public static final String deviceTypeControl = "Controls";
	public static final String deviceTypeReverb = "Reverb";
	public static final String deviceTypeModulation = "Modulation";
	public static final String deviceTypeCompressor = "Compressor";
	public static final String deviceTypeGate = "Gate";
	public static final String deviceTypeDelay = "Delay";
	public static final String deviceTypeCabinets = "Cabinets";
	
	public static final String controlGain = "Gain";
	public static final String controlMaster = "Master";
	public static final String controlBass = "Bass";
	public static final String controlMiddle = "Middle";
	public static final String controlTreble = "Treble";
	
	public static final String delayTime = "Time";
	public static final String delayFeedback = "Feedback";
	public static final String delayHighCut = "HighCut";
	public static final String delayLowCut = "LowCut";
	public static final String delayLevel = "Level";
	
	public static final String None = "None";
	public static final int TIMER_MS = 100; 

	
	private MainActivity activity;
	private Button btnStartAutomation;
	private Button btnStopSysExParsing;
	private Button btnMisc;
	
	private Button btnAssignSeekBar1;
	private Button btnAssignSeekBar2;
	private Button btnAssignSeekBar3;
	
	
	//private Handler handler;
	//private Simulator worker;
	public Thread workthread = null;
	RangeSeekBar<Integer> rangeSeekBar1 = null;
	RangeSeekBar<Integer> rangeSeekBar2 = null;
	RangeSeekBar<Integer> rangeSeekBar3 = null;
	TextView rangeSeekBar1Text = null;
	TextView rangeSeekBar2Text = null;
	TextView rangeSeekBar3Text = null;
	Spinner deviceSpinner = null;
	Spinner functionSpinner = null;
	Spinner timerSpinner = null;
	String selDeviceType = deviceTypeControl;
	
	String selectedFunction = "";
	int timerValue = 0;
	
	HashMap<RangeSeekBar<Integer>, AutomationData> funcMap = new HashMap<RangeSeekBar<Integer>, AutomationData>();
	
	List<String> functionList = new ArrayList<String>();
	
	private SysExCommands sysExCommands;
	public boolean initialized;
	private boolean timerIsOn = false;
	
	
	public Automation(MainActivity activity){
		this.activity = activity;
		sysExCommands = new SysExCommands();
		setupUI();
		initialized = true;

		/*
	    handler = new Handler();
	    worker = new Simulator(handler,activity, this);
	    workthread = new Thread(worker);
	    worker.setPause(true);
	    */
	}

	@SuppressWarnings("unchecked")
	private void setupUI() {
		btnStartAutomation = (Button) activity.findViewById(R.id.btnStartAutomation); 
		btnStopSysExParsing = (Button) activity.findViewById(R.id.btnStopSysExParsing); 
		btnMisc = (Button) activity.findViewById(R.id.btnMisc);
		btnAssignSeekBar1 = (Button) activity.findViewById(R.id.btnAssignSeekBar1);
		btnAssignSeekBar2 = (Button) activity.findViewById(R.id.btnAssignSeekBar2);
		btnAssignSeekBar3 = (Button) activity.findViewById(R.id.btnAssignSeekBar3);
		
		rangeSeekBar1 = (RangeSeekBar<Integer>)activity.findViewById(R.id.rangeSeekBar1);
		rangeSeekBar2 = (RangeSeekBar<Integer>)activity.findViewById(R.id.rangeSeekBar2);
		rangeSeekBar3 = (RangeSeekBar<Integer>)activity.findViewById(R.id.rangeSeekBar3);
		rangeSeekBar1Text = (TextView)activity.findViewById(R.id.rangeSeekBar1Text);
		rangeSeekBar2Text = (TextView)activity.findViewById(R.id.rangeSeekBar2Text);
		rangeSeekBar3Text = (TextView)activity.findViewById(R.id.rangeSeekBar3Text);
		
		deviceSpinner = (Spinner)activity.findViewById(R.id.spinnerDeviceType);
		functionSpinner = (Spinner)activity.findViewById(R.id.spinnerFunction);
		timerSpinner = (Spinner)activity.findViewById(R.id.spinnerTimer);
		
		funcMap.put(rangeSeekBar1, new AutomationData(rangeSeekBar1Text, activity));
		funcMap.put(rangeSeekBar2, new AutomationData(rangeSeekBar2Text, activity));
		funcMap.put(rangeSeekBar3, new AutomationData(rangeSeekBar3Text, activity));
		
		final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, functionList);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		functionSpinner.setAdapter(dataAdapter);
		
		//String[] deviceArray = activity.getResources().getStringArray(R.array.device_types);
		deviceSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					functionList.clear();
					selDeviceType = parent.getItemAtPosition(position).toString();
					if (selDeviceType.equals(deviceTypeControl)) {
						functionList.add(controlGain);
						functionList.add(controlMaster);
						functionList.add(controlBass);
						functionList.add(controlMiddle);
						functionList.add(controlTreble);
						selectedFunction = controlGain;
						
					} else if (selDeviceType.equals(deviceTypeCabinets)) {
						
					} else if (selDeviceType.equals(deviceTypeCompressor)) {
						
					} else if (selDeviceType.equals(deviceTypeDelay)) {
						functionList.add(delayTime);
						functionList.add(delayFeedback);
						functionList.add(delayHighCut);
						functionList.add(delayLowCut);
						functionList.add(delayLevel);	
						selectedFunction = delayTime;
						
					} else if (selDeviceType.equals(deviceTypeGate)) {
						
					} else if (selDeviceType.equals(deviceTypeReverb)) {
						
					} else if (selDeviceType.equals(deviceTypeModulation)) {
						
					}
					functionSpinner.refreshDrawableState();
					dataAdapter.notifyDataSetChanged();
					functionSpinner.setSelection(0);
					//functionSpinner.getAdapter().getView(position, null, null).performClick();
					//functionSpinner.performItemClick(functionSpinner, 0 , 0);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					
				}
		});
		
		functionSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				selectedFunction = parent.getItemAtPosition(position).toString();
				Log.d( TAG, "setOnItemSelectedListener selected function " + selectedFunction );
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		timerSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String timer = parent.getItemAtPosition(position).toString();
				timer = timer.replace("ms", "");
				timer = timer.replace(" ", "");
				timerValue = Integer.parseInt(timer);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		btnAssignSeekBar1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AutomationData auto = funcMap.get(rangeSeekBar1);
				String sb1Assign = auto.function;
				String sb2Assign = funcMap.get(rangeSeekBar2).function;
				String sb3Assign = funcMap.get(rangeSeekBar3).function;
				
				
				if(btnAssignSeekBar1.getText().equals("-")){
					auto.function = "";
					rangeSeekBar1Text.setText("Empty");
					btnAssignSeekBar1.setText("+");
					auto.isAssigned = false;
					auto.intervalMS = 0;
					auto.value = auto.minValue;
				}else{				
					if(sb1Assign.equals(selectedFunction)){
	         			activity.runOnUiThread(new Runnable() {
	        				@Override
	        				public void run() {
	        					Toast.makeText(activity, "ERROR: Function " + selectedFunction + " is already assigned to this slider", Toast.LENGTH_LONG).show();
	        				}
	        			});						
					}else if (sb2Assign.equals(selectedFunction) || sb3Assign.equals(selectedFunction)){
	         			activity.runOnUiThread(new Runnable() {
	        				@Override
	        				public void run() {
	        					Toast.makeText(activity, "ERROR: Function " + selectedFunction + " is assigned to another slider", Toast.LENGTH_LONG).show();
	        				}
	        			});	
					}else{
						auto.assignFunction(selectedFunction);
						auto.assignDeviceType(selDeviceType);
						auto.isAssigned = true;
						auto.intervalMS = timerValue;
						rangeSeekBar1Text.setText(selectedFunction);
						btnAssignSeekBar1.setText("-");
						preapareAutomation(rangeSeekBar1);
					}
				}
			}
		});
		
		
		btnAssignSeekBar2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AutomationData auto = funcMap.get(rangeSeekBar2);
				String sb2Assign = auto.function;
				String sb1Assign = funcMap.get(rangeSeekBar1).function;
				String sb3Assign = funcMap.get(rangeSeekBar3).function;
				
				if(btnAssignSeekBar2.getText().equals("-")){
					auto.function = "";
					rangeSeekBar2Text.setText("Empty");
					btnAssignSeekBar2.setText("+");
					auto.isAssigned = false;
					auto.intervalMS = 0;
					auto.value = auto.minValue;
				}else{
					if(sb2Assign.equals(selectedFunction)){
	         			activity.runOnUiThread(new Runnable() {
	        				@Override
	        				public void run() {
	        					Toast.makeText(activity, "ERROR: Function " + selectedFunction + " is already assigned to this slider", Toast.LENGTH_LONG).show();
	        				}
	        			});						
					}else if (sb1Assign.equals(selectedFunction) || sb3Assign.equals(selectedFunction)){
	         			activity.runOnUiThread(new Runnable() {
	        				@Override
	        				public void run() {
	        					Toast.makeText(activity, "ERROR: Function " + selectedFunction + " is assigned to another slider", Toast.LENGTH_LONG).show();
	        				}
	        			});	
					}else{
						auto.assignFunction(selectedFunction);
						auto.assignDeviceType(selDeviceType);
						auto.isAssigned = true;
						auto.intervalMS = timerValue;
						rangeSeekBar2Text.setText(selectedFunction);
						btnAssignSeekBar2.setText("-");
						preapareAutomation(rangeSeekBar2);
					}
				}
			}
		});
		
		btnAssignSeekBar3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AutomationData auto = funcMap.get(rangeSeekBar3);
				String sb3Assign = auto.function;
				String sb1Assign = funcMap.get(rangeSeekBar1).function;
				String sb2Assign = funcMap.get(rangeSeekBar2).function;
				
				if(btnAssignSeekBar3.getText().equals("-")){
					auto.function = "";
					rangeSeekBar3Text.setText("Empty");
					btnAssignSeekBar3.setText("+");
					auto.isAssigned = false;
					auto.intervalMS = 0;
					auto.value = auto.minValue;
				}else{					
					if(sb3Assign.equals(selectedFunction)){
	         			activity.runOnUiThread(new Runnable() {
	        				@Override
	        				public void run() {
	        					Toast.makeText(activity, "ERROR: Function " + selectedFunction + " is already assigned to this slider", Toast.LENGTH_LONG).show();
	        				}
	        			});						
					}else if (sb1Assign.equals(selectedFunction) || sb2Assign.equals(selectedFunction)){
	         			activity.runOnUiThread(new Runnable() {
	        				@Override
	        				public void run() {
	        					Toast.makeText(activity, "ERROR: Function " + selectedFunction + " is assigned to another slider", Toast.LENGTH_LONG).show();
	        				}
	        			});	
					}else{
						auto.assignFunction(selectedFunction);
						auto.assignDeviceType(selDeviceType);
						auto.isAssigned = true;
						auto.intervalMS = timerValue;
						rangeSeekBar3Text.setText(selectedFunction);
						btnAssignSeekBar3.setText("-");
						preapareAutomation(rangeSeekBar3);
					}
				}
			}
		});
		
		
		btnStartAutomation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(timerIsOn){
					funcMap.get(rangeSeekBar1).stop();
					funcMap.get(rangeSeekBar2).stop();
					funcMap.get(rangeSeekBar3).stop();
					timerIsOn = false;
					btnStartAutomation.setText("Stopped");
				}else{
					funcMap.get(rangeSeekBar1).start();
					funcMap.get(rangeSeekBar2).start();
					funcMap.get(rangeSeekBar3).start();	
					timerIsOn = true;
					btnStartAutomation.setText("Running");
				}

				/*
	            Thread.State status = workthread.getState();
	            Log.d(TAG, "Automation thread  status: " + status);
	            if (Thread.State.NEW.equals(status)) {
	                // first start
	                Log.d(TAG, "Automation thread start");
	                workthread.start();
	            } else {
	                if (worker.isPause()) {
	                    Log.d(TAG, "Automation thread  go on");
	                    worker.setPause(false);
	                } else {
	                    Log.d(TAG, "Automation thread  pause");
	                    worker.setPause(true);
	                }
	            }
	            */				
			}
		});
		
		btnStopSysExParsing.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// toggle sysex handling in onMidiSystemExclusive (wont parse anything)
				if(activity.inSysExHandlingIsOn){
					activity.inSysExHandlingIsOn = false;
				}else{
					activity.inSysExHandlingIsOn = true;
				}
			}
		});
		
		btnMisc.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
     			activity.runOnUiThread(new Runnable() {
    				@Override
    				public void run() {
    					Toast.makeText(activity, "App version: " + activity.versionName, Toast.LENGTH_LONG).show();
    				}
    			});					
				/*
				byte [] test = {0x7d, 0x01};
				int val = SysExCommands.decode7BitByteToInt(test);
				Log.d(TAG, "decode7BitByteToInt():  Integer: " + val + " 0x" + Integer.toHexString(val));
				*/
			}
		});	
		
		rangeSeekBar1.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>(){
			@Override
			public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
				funcMap.get(rangeSeekBar1).minValue = minValue;
				funcMap.get(rangeSeekBar1).maxValue = maxValue;
				funcMap.get(rangeSeekBar1).value = minValue;
			}
			
		});
		
		rangeSeekBar2.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>(){
			@Override
			public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
				//rangeSeekBar2Text.setText(minValue.toString());
				funcMap.get(rangeSeekBar2).minValue = minValue;
				funcMap.get(rangeSeekBar2).maxValue = maxValue;		
				funcMap.get(rangeSeekBar2).value = minValue;
			}
			
		});
		
		rangeSeekBar3.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>(){
			@Override
			public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
				funcMap.get(rangeSeekBar3).minValue = minValue;
				funcMap.get(rangeSeekBar3).maxValue = maxValue;	
				funcMap.get(rangeSeekBar3).value = minValue;
			}
			
		});

	}

	public void preapareAutomation(RangeSeekBar<Integer> seekBar){
		if(selDeviceType.equals(deviceTypeControl)){
			prepareControlAutomation(seekBar);
		}else if(selDeviceType.equals(deviceTypeDelay)){
			prepareDelayAutomation(seekBar);
		}
	}
	
	private void prepareControlAutomation(RangeSeekBar<Integer> seekBar){
		AutomationData data = funcMap.get(seekBar);
		data.type = DataType.BYTE;
		seekBar.setRangeValues(0, 100);
		
		if(data.function.equals(controlGain)){
			data.rangeSeekBarText.setText(controlGain + " " + String.valueOf(data.value));
			data.sysEx = sysExCommands.getControlSysEx(Controls.GAIN, (byte)data.value);
			activity.setGain((byte)data.value);
			
		}else if (data.function.equals(controlMaster)){
			data.rangeSeekBarText.setText(controlMaster + " " + String.valueOf(data.value));
			data.sysEx = sysExCommands.getControlSysEx(Controls.MASTER, (byte)data.value);
			activity.setMaster((byte)data.value);
			
		}else if (data.function.equals(controlBass)){
			data.rangeSeekBarText.setText(controlBass + " " + String.valueOf(data.value));
			data.sysEx = sysExCommands.getControlSysEx(Controls.BASS, (byte)data.value);
			activity.setBass((byte)data.value);			
			
		}else if (data.function.equals(controlMiddle)){
			data.rangeSeekBarText.setText(controlMiddle + " " + String.valueOf(data.value));
			data.sysEx = sysExCommands.getControlSysEx(Controls.MIDDLE, (byte)data.value);
			activity.setMiddle((byte)data.value);				
			
		}else if (data.function.equals(controlTreble)){
			data.rangeSeekBarText.setText(controlTreble + " " + String.valueOf(data.value));
			data.sysEx = sysExCommands.getControlSysEx(Controls.TREBLE, (byte)data.value);
			activity.setTreble((byte)data.value);		
		}
		activity.refreshContolUI();
	}
	
	private void prepareDelayAutomation(RangeSeekBar<Integer> seekBar){
		AutomationData data = funcMap.get(seekBar);
		
		if(data.function.equals(delayTime)){
			data.type = DataType.INT;
			data.value = activity.getDelayData().time;
			seekBar.setRangeValues(Delay.TIME_MIN , Delay.TIME_MAX );  // 1 .. 9999  (0.1 ms .... 999.0 ms)
			data.rangeSeekBarText.setText(delayTime + " " + data.value/10.0 + " ms");  
			data.sysEx = sysExCommands.getDelayValuesSysEx(data.value, "Time");
			
		}else if (data.function.equals(delayFeedback)){
			data.type = DataType.BYTE;
			seekBar.setRangeValues(0, 100);
			data.rangeSeekBarText.setText(delayFeedback + " " + String.valueOf(data.value));
			data.sysEx = sysExCommands.getDelayValuesSysEx(data.value, "Feedback");
			activity.getDelayData().feedback = (byte) data.value;
			
		}else if (data.function.equals(delayHighCut)){
			data.type = DataType.INT;
			seekBar.setRangeValues((Integer)Delay.HIGHCUT_MIN, (Integer)Delay.HIGHCUT_MAX);
			data.rangeSeekBarText.setText(delayHighCut + " " + String.valueOf(data.value));
			data.sysEx = sysExCommands.getDelayValuesSysEx(data.value, "HighCut");
			activity.getDelayData().highcut = data.value;
			
		}else if (data.function.equals(delayLowCut)){
			data.type = DataType.INT;
			seekBar.setRangeValues((Integer)Delay.LOWCUT_MIN, (Integer)Delay.LOWCUT_MAX);
			data.rangeSeekBarText.setText(delayLowCut + " " + String.valueOf(data.value));
			data.sysEx = sysExCommands.getDelayValuesSysEx(data.value, "LowCut");
			activity.getDelayData().lowcut = data.value;
			
		}else if (data.function.equals(delayLevel)){
			seekBar.setRangeValues(0, 100);
			data.rangeSeekBarText.setText(delayLevel + " " + String.valueOf(data.value));
			data.sysEx = sysExCommands.getDelayValuesSysEx(data.value, "Level");
			activity.getDelayData().level = (byte) data.value;
		}
		activity.delay.updateUI();	
		
	}
}
