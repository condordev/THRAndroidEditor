package de.sgrad.yamahathreditor;

import de.sgrad.yamahathreditor.SysExCommands.SwitchableDevices;
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

public class Compressor implements OnItemSelectedListener {
	public static String TAG = "THR";
	
	MainActivity activity;
	
	Button btnCompressorOnOff;
	Spinner spinnerCompressorType;
	MarkerSeekBar sbCompStompSustain;
	MarkerSeekBar sbCompStompOutput;
	MarkerSeekBar sbCompRackOutput;
	MarkerSeekBar sbCompRackThreshold;
	MarkerSeekBar sbCompRackAttack;
	MarkerSeekBar sbCompRackRelease;
	MarkerSeekBar sbCompRackRatio;
	MarkerSeekBar sbCompRackKnee;
//	String [] compressorType = getResources().getStringArray(R.array.compressor_types);
	
	private SysExCommands sysExCommands;
	public boolean initialized;
	
	Compressor(MainActivity activity){
		this.activity = activity;
		sysExCommands = new SysExCommands();
		setupUI();
		initialized = true;
	}
	
	/*
	 * displays the SeekBar sliders for the supported equalizer frequency bands
	 * user can move sliders to change the frequency of the bands
	 */
	private void setupUI() {
		spinnerCompressorType = (Spinner) activity.findViewById(R.id.spinnerCompressorType); 
		btnCompressorOnOff = (Button) activity.findViewById(R.id.btnCompressorOnOff); 
		
		sbCompStompSustain = (MarkerSeekBar) activity.findViewById(R.id.sbCompStompSustain); 
		sbCompStompOutput = (MarkerSeekBar) activity.findViewById(R.id.sbCompStompOutput); 
		sbCompRackOutput = (MarkerSeekBar) activity.findViewById(R.id.sbCompRackOutput); 
		sbCompRackThreshold = (MarkerSeekBar) activity.findViewById(R.id.sbCompRackThreshold); 
		sbCompRackAttack = (MarkerSeekBar) activity.findViewById(R.id.sbCompRackAttack); 
		sbCompRackRelease = (MarkerSeekBar) activity.findViewById(R.id.sbCompRackRelease); 
		sbCompRackRatio = (MarkerSeekBar) activity.findViewById(R.id.sbCompRackRatio); 
		sbCompRackKnee = (MarkerSeekBar) activity.findViewById(R.id.sbCompRackKnee);
		
		sbCompStompSustain.setMax(100);
		sbCompStompSustain.setProgress(activity.getCompressorData().stompSustain);
		
		sbCompStompOutput.setMax(100);
		sbCompStompOutput.setProgress(activity.getCompressorData().stompOutput);

		sbCompRackAttack.setMax(100);
		sbCompRackAttack.setProgress(activity.getCompressorData().rackAttack);
		
		sbCompRackRelease.setMax(100);
		sbCompRackRelease.setProgress(activity.getCompressorData().rackRelease);
		
		sbCompRackRatio.setMax(5);
		sbCompRackRatio.setProgress(activity.getCompressorData().rackRatio);
		
		sbCompRackKnee.setMax(2);
		sbCompRackKnee.setProgress(activity.getCompressorData().rackKnee);
		
		sbCompRackThreshold.setMax(0x258); // 600 , 0x0000 - 0x0458 (-60db : 0)
		sbCompRackThreshold.setProgress(activity.getCompressorData().rackThreshold);
		
		sbCompRackOutput.setMax(0x258); // 600 ,0x0000 - 0x0458 (-20db : 40db)
		sbCompRackOutput.setProgress(activity.getCompressorData().rackOutput);
		
		
		spinnerCompressorType.setOnItemSelectedListener(this);
		
		updateUI();
		
		btnCompressorOnOff.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (activity.getCompressorData().compressorOnOff){
					btnCompressorOnOff.setText("is Off");
					activity.getCompressorData().compressorOnOff = false;
					spinnerCompressorType.setEnabled(false);
					activity.onDataChangeUI(sysExCommands.getDeviceOnOffSysEx(SwitchableDevices.COMPRESSOR, false));
				}else{
					btnCompressorOnOff.setText("is On");
					activity.getCompressorData().compressorOnOff = true;
					spinnerCompressorType.setEnabled(true);
					// if switched on send also current type
					activity.onDataChangeUI(sysExCommands.getDeviceOnOffSysEx(SwitchableDevices.COMPRESSOR, true));
					//activity.onDataChangeUI(sysExCommands.getCompressorTypeSelectSysEx(activity.getCompressorData().compressorType));
				}
				updateUI();
			}
		});
		
		sbCompStompSustain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					activity.getCompressorData().stompSustain = (byte) progress;
					activity.onDataChangeUI(sysExCommands.getCompressorValuesSysEx(CompressorData.compressorTypeStomp, activity.getCompressorData().stompSustain,0,"Sustain"));
					((TextView)activity.findViewById(R.id.sbCompStompSustainText)).setText("Sustain "+ Integer.toString(progress));
				}
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbCompStompSustain.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
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
		
		sbCompStompOutput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					activity.getCompressorData().stompOutput = (byte) progress;
					activity.onDataChangeUI(sysExCommands.getCompressorValuesSysEx(CompressorData.compressorTypeStomp, activity.getCompressorData().stompOutput,0, "Output"));
					((TextView)activity.findViewById(R.id.sbCompStompOutputText)).setText("Output "+ Integer.toString(progress));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbCompStompOutput.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
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
		
		sbCompRackAttack.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					activity.getCompressorData().rackAttack = (byte)progress;
					activity.onDataChangeUI(sysExCommands.getCompressorValuesSysEx(CompressorData.compressorTypeRack, activity.getCompressorData().rackAttack,0, "Attack"));
					((TextView)activity.findViewById(R.id.sbCompRackAttackText)).setText("Attack "+ Integer.toString(progress));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbCompRackAttack.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
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
		
		sbCompRackRatio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					activity.getCompressorData().rackRatio = (byte)progress;
					activity.onDataChangeUI(sysExCommands.getCompressorValuesSysEx(CompressorData.compressorTypeRack, activity.getCompressorData().rackRatio,0, "Ratio"));
					String value = "1:1";
					if(progress == 0){
						value = "1:1";
					}else if(progress == 1){
						value = "1:4";
					}else if(progress == 2){
						value = "1:8";
					}else if(progress == 3){
						value = "1:12";
					}else if(progress == 4){
						value = "1:20";
					}else if(progress == 5){
						value = "1:inf";
					}
					
					((TextView)activity.findViewById(R.id.sbCompRackRatioText)).setText("Ratio "+ value);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbCompRackRatio.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
				String value = "1:1";
				if(progress == 0){
					value = "1:1";
				}else if(progress == 1){
					value = "1:4";
				}else if(progress == 2){
					value = "1:8";
				}else if(progress == 3){
					value = "1:12";
				}else if(progress == 4){
					value = "1:20";
				}else if(progress == 5){
					value = "1:inf";
				}
				return value;
            }

            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });
		
		sbCompRackKnee.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					activity.getCompressorData().rackKnee = (byte) progress;
					activity.onDataChangeUI(sysExCommands.getCompressorValuesSysEx(CompressorData.compressorTypeRack, activity.getCompressorData().rackKnee,0, "Knee"));
					String value = "soft";
					if(progress == 0){
						value = "soft";
					}else if(progress == 1){
						value = "medium";
					}else if(progress == 2){
						value = "hard";
					}
						
					((TextView)activity.findViewById(R.id.sbCompRackKneeText)).setText("Knee "+ value);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});	
		sbCompRackKnee.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
				String value = "soft";
				if(progress == 0){
					value = "soft";
				}else if(progress == 1){
					value = "medium";
				}else if(progress == 2){
					value = "hard";
				}
				return value;
            }

            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });
		
		sbCompRackThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					activity.getCompressorData().rackThreshold = (short) progress;
					activity.onDataChangeUI(sysExCommands.getCompressorValuesSysEx(CompressorData.compressorTypeRack,(byte)0, progress, "Threshold"));
					((TextView)activity.findViewById(R.id.sbCompRackThresholdText)).setText("Threshold "+ Integer.toString((progress - 0x258) / 10) + " dB");
					}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbCompRackThreshold.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
            	return Integer.toString((progress - 0x258) / 10) + " dB";
            }

            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });
		
		sbCompRackOutput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					activity.getCompressorData().rackOutput = (short) progress;
					activity.onDataChangeUI(sysExCommands.getCompressorValuesSysEx(CompressorData.compressorTypeRack,(byte)0,progress, "Output"));
					((TextView)activity.findViewById(R.id.sbCompRackOutputText)).setText("Output "+ Integer.toString((progress - 200) / 10) + " dB");
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});	
		sbCompRackOutput.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
              //  return String.format("%s", progress / 10.0 + " ms");
            	return Integer.toString((progress - 200) / 10) + " dB";
            }

            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });
	}
	

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		String selCompType = parent.getItemAtPosition(position).toString();
		CompressorData cd = activity.getCompressorData();
		
		if(!cd.compressorType.equals(selCompType)){
			cd.compressorType = selCompType;
			if(cd.compressorOnOff){
				activity.onDataChangeUI(sysExCommands.getCompressorTypeSelectSysEx(selCompType));
			}
		}
		
		if(activity.getCompressorData().compressorType.equals(CompressorData.compressorTypeStomp)){
			// not visible
			activity.findViewById(R.id.sbCompRackOutputText).setVisibility(View.GONE);
			sbCompRackOutput.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbCompRackThresholdText).setVisibility(View.GONE);
			sbCompRackThreshold.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbCompRackAttackText).setVisibility(View.GONE);
			sbCompRackAttack.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbCompRackReleaseText).setVisibility(View.GONE);
			sbCompRackRelease.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbCompRackRatioText).setVisibility(View.GONE);
			sbCompRackRatio.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbCompRackKneeText).setVisibility(View.GONE);
			sbCompRackKnee.setVisibility(View.GONE);	
			
			// visible
			activity.findViewById(R.id.sbCompStompSustainText).setVisibility(View.VISIBLE);
			sbCompStompSustain.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbCompStompOutputText).setVisibility(View.VISIBLE);
			sbCompStompOutput.setVisibility(View.VISIBLE);
			
		}else if(activity.getCompressorData().compressorType.equals(CompressorData.compressorTypeRack)) {
			// visible
			activity.findViewById(R.id.sbCompStompSustainText).setVisibility(View.GONE);
			sbCompStompSustain.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbCompStompOutputText).setVisibility(View.GONE);
			sbCompStompOutput.setVisibility(View.GONE);
			
			// not visible
			activity.findViewById(R.id.sbCompRackOutputText).setVisibility(View.VISIBLE);
			sbCompRackOutput.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbCompRackThresholdText).setVisibility(View.VISIBLE);
			sbCompRackThreshold.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbCompRackAttackText).setVisibility(View.VISIBLE);
			sbCompRackAttack.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbCompRackReleaseText).setVisibility(View.VISIBLE);
			sbCompRackRelease.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbCompRackRatioText).setVisibility(View.VISIBLE);
			sbCompRackRatio.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbCompRackKneeText).setVisibility(View.VISIBLE);
			sbCompRackKnee.setVisibility(View.VISIBLE);				
		}
	}
	
	public void updateUI(){
		final CompressorData cd = activity.getCompressorData();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	spinnerCompressorType.setSelection(getIndex(spinnerCompressorType,cd.compressorType));
            	
            	String compStatus = cd.compressorType;
        		
        		if(cd.compressorOnOff){
        			btnCompressorOnOff.setText("is On");
        			spinnerCompressorType.setEnabled(true);
        			compStatus = compStatus.concat(" On");
        		}
        		else{
        			spinnerCompressorType.setEnabled(false);
        			btnCompressorOnOff.setText("is Off");
        			compStatus = compStatus.concat(" Off");
            	}
        		
        		if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
        			activity.btnCompressor.setText(Html.fromHtml("Compressor " +  "<br />" +  "<small>" + compStatus + "</small>" ));
        		}else{
        			activity.btnCompressor.setText(Html.fromHtml("Comp" +  "<br />" +  "<small><small>" + compStatus + "</small></small>"));
        		}
        		
        		sbCompStompSustain.setProgress(cd.stompSustain);
        		sbCompStompSustain.refreshDrawableState();
        		
        		sbCompStompOutput.setProgress(cd.stompOutput);
        		sbCompStompOutput.refreshDrawableState();
        		
        		sbCompRackAttack.setProgress(cd.rackAttack);
        		sbCompRackAttack.refreshDrawableState();
        		
        		sbCompRackKnee.setProgress(cd.rackKnee);
        		sbCompRackKnee.refreshDrawableState();
        		
        		sbCompRackOutput.setProgress(cd.rackOutput);
        		sbCompRackOutput.refreshDrawableState();
        		
        		sbCompRackRatio.setProgress(cd.rackRatio);
        		sbCompRackRatio.refreshDrawableState();
        		
        		sbCompRackRelease.setProgress(cd.rackRelease);
        		sbCompRackRelease.refreshDrawableState();
        		
        		sbCompRackThreshold.setProgress(cd.rackThreshold);
        		sbCompRackThreshold.refreshDrawableState();
        		
        		((TextView)activity.findViewById(R.id.sbCompStompSustainText)).setText("Sustain "+ Integer.toString(cd.stompSustain));
        		((TextView)activity.findViewById(R.id.sbCompStompOutputText)).setText("Output "+ Integer.toString(cd.stompOutput));
        		((TextView)activity.findViewById(R.id.sbCompRackAttackText)).setText("Attack "+ Integer.toString(cd.rackAttack));
        		((TextView)activity.findViewById(R.id.sbCompRackReleaseText)).setText("Release "+ Integer.toString(cd.rackRelease));
        		
        		String value = "1:1";
        		if(cd.rackRatio == 0){
        			value = "1:1";
        		}else if(cd.rackRatio == 1){
        			value = "1:4";
        		}else if(cd.rackRatio == 2){
        			value = "1:8";
        		}else if(cd.rackRatio == 3){
        			value = "1:12";
        		}else if(cd.rackRatio == 4){
        			value = "1:20";
        		}else if(cd.rackRatio == 5){
        			value = "1:inf";
        		}
        		((TextView)activity.findViewById(R.id.sbCompRackRatioText)).setText("Ratio "+ value);
        		
        		value = "soft";
        		if(cd.rackKnee == 0){
        			value = "soft";
        		}else if(cd.rackKnee == 1){
        			value = "medium";
        		}else if(cd.rackKnee == 2){
        			value = "hard";
        		}
        		((TextView)activity.findViewById(R.id.sbCompRackKneeText)).setText("Knee "+ value);
        		
        		((TextView)activity.findViewById(R.id.sbCompRackThresholdText)).setText("Threshold "+ Integer.toString((cd.rackThreshold - 0x258) / 10) + " dB");
        		((TextView)activity.findViewById(R.id.sbCompRackOutputText)).setText("Output "+ Integer.toString((cd.rackOutput - 200) / 10) + " dB");
        		
            }
        });
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
		// TODO Auto-generated method stub
		
	}
    

}
