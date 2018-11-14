package de.sgrad.yamahathreditor;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import de.sgrad.yamahathreditor.SysExCommands.SwitchableDevices;

public class Modulation implements OnItemSelectedListener {
	public static String TAG = "THR";
	
	private MainActivity activity;
	
	Button btnModOnOff;
	Spinner spinnerModType;
	MarkerSeekBar sbModSpeed;
	MarkerSeekBar sbModDepth;
	MarkerSeekBar sbModFeedback;
	MarkerSeekBar sbModManual;
	MarkerSeekBar sbModFreq;
	MarkerSeekBar sbModSpread;
	MarkerSeekBar sbModMix;
	//String [] compressorType = getResources().getStringArray(R.array.compressor_types);
	private SysExCommands sysExCommands;
	boolean initialized;
	
	Modulation(MainActivity activity){
		this.activity = activity;
		sysExCommands = new SysExCommands();
		setupUI();
		initialized = true;
	}
	
	private void setupUI() {
		final ModulationData md = activity.getModulationData();
		spinnerModType = (Spinner) activity.findViewById(R.id.spinnerModulationType); 
		btnModOnOff = (Button) activity.findViewById(R.id.btnModulationOnOff); 
		
		sbModSpeed = (MarkerSeekBar) activity.findViewById(R.id.sbModSpeed); 
		sbModDepth = (MarkerSeekBar) activity.findViewById(R.id.sbModDepth); 
		sbModManual = (MarkerSeekBar) activity.findViewById(R.id.sbModManual); 
		sbModFreq = (MarkerSeekBar) activity.findViewById(R.id.sbModFreq); 
		sbModSpread = (MarkerSeekBar) activity.findViewById(R.id.sbModSpread); 
		sbModMix = (MarkerSeekBar) activity.findViewById(R.id.sbModMix); 
		sbModFeedback = (MarkerSeekBar) activity.findViewById(R.id.sbModFeedback); 
		
		sbModSpeed.setMax(0x64); 
		sbModSpeed.setProgress(md.speed);
		
		sbModDepth.setMax(0x64);  
		sbModDepth.setProgress(md.depth);

		sbModManual.setMax(0x64);  
		sbModManual.setProgress(md.manual);
		
		sbModFreq.setMax(0x64);    
		sbModFreq.setProgress(md.tremFreq);
		
		sbModSpread.setMax(0x64);     
		sbModSpread.setProgress(md.flangerSpread);
		
		sbModMix.setMax(0x64);        
		sbModMix.setProgress(md.chorusMix);
		
		sbModFeedback.setMax(0x64);        
		sbModFeedback.setProgress(md.feedback);
		
		spinnerModType.setOnItemSelectedListener(this);
		updateUI();
		
		btnModOnOff.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (md.modOnOff){
					btnModOnOff.setText("is Off");
					md.modOnOff = false;
					spinnerModType.setEnabled(false);
					activity.onDataChangeUI(sysExCommands.getDeviceOnOffSysEx(SwitchableDevices.MODULATION,md.modOnOff));
				}else{
					btnModOnOff.setText("is On");
					md.modOnOff = true;
					spinnerModType.setEnabled(true);
					activity.onDataChangeUI(sysExCommands.getDeviceOnOffSysEx(SwitchableDevices.MODULATION,md.modOnOff));
					// if switched on send also current modulation type
					//activity.onDataChangeUI(sysExCommands.getModulationTypeSelectSysEx(md.modType));
				}
				updateUI();
			}
		});
		
		sbModSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
				md.speed = (byte) progress;
					if(md.modType.equals(ModulationData.modTypeChorus)){
						activity.onDataChangeUI(sysExCommands.getModulationValuesSysEx(ModulationData.modTypeChorus, progress,"Speed"));
					}else if (md.modType.equals(ModulationData.modTypeFlanger)){
						activity.onDataChangeUI(sysExCommands.getModulationValuesSysEx(ModulationData.modTypeFlanger, progress,"Speed"));
					}else if (md.modType.equals(ModulationData.modTypePhaser)){
						activity.onDataChangeUI(sysExCommands.getModulationValuesSysEx(ModulationData.modTypePhaser, progress,"Speed"));
					}
					((TextView)activity.findViewById(R.id.sbModSpeedText)).setText("Speed "+ Integer.toString(progress));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbModSpeed.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
              //  return String.format("%s", progress / 10.0 + " ms");
            	return Integer.toString(progress);
            }

            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });			
		
		sbModDepth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					md.depth = (byte) progress;
					if(md.modType.equals(ModulationData.modTypeChorus)){
						activity.onDataChangeUI(sysExCommands.getModulationValuesSysEx(ModulationData.modTypeChorus, progress,"Depth"));
					}else if (md.modType.equals(ModulationData.modTypeFlanger)){
						activity.onDataChangeUI(sysExCommands.getModulationValuesSysEx(ModulationData.modTypeFlanger, progress,"Depth"));
					}else if (md.modType.equals(ModulationData.modTypePhaser)){
						activity.onDataChangeUI(sysExCommands.getModulationValuesSysEx(ModulationData.modTypePhaser, progress,"Depth"));
					}else if (md.modType.equals(ModulationData.modTypeTremolo)){
						activity.onDataChangeUI(sysExCommands.getModulationValuesSysEx(ModulationData.modTypeTremolo, progress,"Depth"));
					}
					((TextView)activity.findViewById(R.id.sbModDepthText)).setText("Depth "+ Integer.toString(progress));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbModDepth.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
              //  return String.format("%s", progress / 10.0 + " ms");
            	return Integer.toString(progress);
            }

            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });			
		
		sbModFeedback.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					md.feedback = (byte)progress;
					if (md.modType.equals(ModulationData.modTypeFlanger)){
						activity.onDataChangeUI(sysExCommands.getModulationValuesSysEx(ModulationData.modTypeFlanger, progress,"Feedback"));
					}else if (md.modType.equals(ModulationData.modTypePhaser)){
						activity.onDataChangeUI(sysExCommands.getModulationValuesSysEx(ModulationData.modTypePhaser, progress,"Feedback"));
					}
					((TextView)activity.findViewById(R.id.sbModFeedbackText)).setText("Feedback "+ Integer.toString(progress));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbModFeedback.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
              //  return String.format("%s", progress / 10.0 + " ms");
            	return Integer.toString(progress);
            }

            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });			
		
		sbModManual.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					md.manual = (byte)progress;
					if (md.modType.equals(ModulationData.modTypeFlanger)){
						activity.onDataChangeUI(sysExCommands.getModulationValuesSysEx(ModulationData.modTypeFlanger, progress,"Manual"));
					}else if (md.modType.equals(ModulationData.modTypePhaser)){
						activity.onDataChangeUI(sysExCommands.getModulationValuesSysEx(ModulationData.modTypePhaser, progress,"Manual"));
					}
					((TextView)activity.findViewById(R.id.sbModManualText)).setText("Manual "+ Integer.toString(progress));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbModManual.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
              //  return String.format("%s", progress / 10.0 + " ms");
            	return Integer.toString(progress);
            }

            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });			
		
		sbModFreq.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					md.tremFreq = (byte)progress;
					activity.onDataChangeUI(sysExCommands.getModulationValuesSysEx(ModulationData.modTypeTremolo, progress,"Freq"));
					((TextView)activity.findViewById(R.id.sbModFreqText)).setText("Frequency "+ Integer.toString(progress));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbModFreq.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
              //  return String.format("%s", progress / 10.0 + " ms");
            	return Integer.toString(progress);
            }

            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });			
		
		sbModMix.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					md.chorusMix = (byte)progress;
					activity.onDataChangeUI(sysExCommands.getModulationValuesSysEx(ModulationData.modTypeChorus, progress,"Mix"));
					((TextView)activity.findViewById(R.id.sbModMixText)).setText("Mix "+ Integer.toString(progress));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbModMix.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
              //  return String.format("%s", progress / 10.0 + " ms");
            	return Integer.toString(progress);
            }

            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });			
		
		sbModSpread.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					md.flangerSpread = (byte)progress;
					activity.onDataChangeUI(sysExCommands.getModulationValuesSysEx(ModulationData.modTypeFlanger, progress,"Spread"));
					((TextView)activity.findViewById(R.id.sbModSpreadText)).setText("Spread "+ Integer.toString(progress));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbModSpread.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
              //  return String.format("%s", progress / 10.0 + " ms");
            	return Integer.toString(progress);
            }

            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });			

	}
	
	public void updateUI(){
		final ModulationData md = activity.getModulationData();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
        		spinnerModType.setSelection(getIndex(spinnerModType,md.modType));
        		String modStatus = md.modType;
        		
        		if(md.modOnOff){
        			btnModOnOff.setText("is On");
        			spinnerModType.setEnabled(true);	
        			modStatus = modStatus.concat(" On");
        		}
        		else{
        			btnModOnOff.setText("is Off");
        			spinnerModType.setEnabled(false);
        			modStatus = modStatus.concat(" Off");
        		}
        		
        		if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
        			activity.btnModulation.setText(Html.fromHtml("Modulation" +  "<br />" +  "<small>" + modStatus + "</small>" ));
        		}else{
        			activity.btnModulation.setText(Html.fromHtml("Mod " +  "<br />" +  "<small><small>" + modStatus + "</small></small>" ));
        		}
        		
        		
        		sbModSpeed.setProgress(md.speed);
        		sbModSpeed.refreshDrawableState();
        		
        		sbModDepth.setProgress(md.depth);
        		sbModDepth.refreshDrawableState();
        		
        		sbModFeedback.setProgress(md.feedback);
        		sbModFeedback.refreshDrawableState();
        		
        		sbModFreq.setProgress(md.tremFreq);
        		sbModFreq.refreshDrawableState();
        		
        		sbModManual.setProgress(md.manual);
        		sbModManual.refreshDrawableState();
        		
        		sbModMix.setProgress(md.chorusMix);
        		sbModMix.refreshDrawableState();
        		
        		sbModSpread.setProgress(md.flangerSpread);
        		sbModSpread.refreshDrawableState();	
        		
        		((TextView)activity.findViewById(R.id.sbModSpeedText)).setText("Speed "+ Integer.toString(md.speed));
        		((TextView)activity.findViewById(R.id.sbModDepthText)).setText("Depth "+ Integer.toString(md.depth));	
        		((TextView)activity.findViewById(R.id.sbModFeedbackText)).setText("Feedback "+ Integer.toString(md.feedback));
        		((TextView)activity.findViewById(R.id.sbModManualText)).setText("Manual "+ Integer.toString(md.manual));
        		((TextView)activity.findViewById(R.id.sbModFreqText)).setText("Frequency "+ Integer.toString(md.tremFreq));	
        		((TextView)activity.findViewById(R.id.sbModMixText)).setText("Mix "+ Integer.toString(md.chorusMix));	
        		((TextView)activity.findViewById(R.id.sbModSpreadText)).setText("Spread "+ Integer.toString(md.flangerSpread));	
            }
        });		
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		ModulationData md = activity.getModulationData();
		
		String selModType = parent.getItemAtPosition(position).toString();
		if(!md.modType.equals(selModType)){
			md.modType = selModType;
			if(md.modOnOff){
				activity.onDataChangeUI(sysExCommands.getModulationTypeSelectSysEx(selModType));
			}
		}
		
		if(md.modType.equals(ModulationData.modTypeTremolo)){
			// visible
			activity.findViewById(R.id.sbModFreqText).setVisibility(View.VISIBLE);
			sbModFreq.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbModDepthText).setVisibility(View.VISIBLE);
			sbModDepth.setVisibility(View.VISIBLE);
			
			// not visible
			activity.findViewById(R.id.sbModSpeedText).setVisibility(View.GONE);
			sbModSpeed.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbModManualText).setVisibility(View.GONE);
			sbModManual.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbModSpreadText).setVisibility(View.GONE);
			sbModSpread.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbModMixText).setVisibility(View.GONE);
			sbModMix.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbModFeedbackText).setVisibility(View.GONE);
			sbModFeedback.setVisibility(View.GONE);
			
		}else if (md.modType.equals(ModulationData.modTypeChorus)){
			// visible
			activity.findViewById(R.id.sbModSpeedText).setVisibility(View.VISIBLE);
			sbModSpeed.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbModDepthText).setVisibility(View.VISIBLE);
			sbModDepth.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbModMixText).setVisibility(View.VISIBLE);
			sbModMix.setVisibility(View.VISIBLE);
			
			// not visible
			activity.findViewById(R.id.sbModManualText).setVisibility(View.GONE);
			sbModManual.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbModSpreadText).setVisibility(View.GONE);
			sbModSpread.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbModFeedbackText).setVisibility(View.GONE);
			sbModFeedback.setVisibility(View.GONE);	
			
			activity.findViewById(R.id.sbModFreqText).setVisibility(View.GONE);
			sbModFreq.setVisibility(View.GONE);
			
		}else if (md.modType.equals(ModulationData.modTypePhaser)){
			// visible
			activity.findViewById(R.id.sbModSpeedText).setVisibility(View.VISIBLE);
			sbModSpeed.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbModDepthText).setVisibility(View.VISIBLE);
			sbModDepth.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbModManualText).setVisibility(View.VISIBLE);
			sbModManual.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbModFeedbackText).setVisibility(View.VISIBLE);
			sbModFeedback.setVisibility(View.VISIBLE);	
			
			// not visible
			activity.findViewById(R.id.sbModFreqText).setVisibility(View.GONE);
			sbModFreq.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbModMixText).setVisibility(View.GONE);
			sbModMix.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbModSpreadText).setVisibility(View.GONE);
			sbModSpread.setVisibility(View.GONE);
		}else if (md.modType.equals(ModulationData.modTypeFlanger)){
			// visible
			activity.findViewById(R.id.sbModSpeedText).setVisibility(View.VISIBLE);
			sbModSpeed.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbModDepthText).setVisibility(View.VISIBLE);
			sbModDepth.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbModManualText).setVisibility(View.VISIBLE);
			sbModManual.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbModFeedbackText).setVisibility(View.VISIBLE);
			sbModFeedback.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbModSpreadText).setVisibility(View.VISIBLE);
			sbModSpread.setVisibility(View.VISIBLE);
			
			// not visible
			activity.findViewById(R.id.sbModFreqText).setVisibility(View.GONE);
			sbModFreq.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbModMixText).setVisibility(View.GONE);
			sbModMix.setVisibility(View.GONE);
		}
	}

	// usage:  mySpinner.setSelection(getIndex(mySpinner, myValue));
	private int getIndex(Spinner spinner, String myString) {
		int index = 0;

		for (int i = 0; i < spinner.getCount(); i++) {
			if (spinner.getItemAtPosition(i).toString() .equalsIgnoreCase(myString)) {
				index = i;
				break;
			}
		}
		return index;
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
	}
    

}
