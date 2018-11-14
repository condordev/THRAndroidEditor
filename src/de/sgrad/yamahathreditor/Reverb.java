package de.sgrad.yamahathreditor;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import de.sgrad.yamahathreditor.SysExCommands.SwitchableDevices;

public class Reverb implements OnItemSelectedListener {
	public static String TAG = "THR";
	
	private MainActivity activity;
	
	Button btnReverbOnOff;
	Spinner spinnerReverbType;
	MarkerSeekBar sbReverbTime;
	MarkerSeekBar sbReverbPre;
	MarkerSeekBar sbReverbLowCut;
	MarkerSeekBar sbReverbHighCut;
	MarkerSeekBar sbReverbHighRatio;
	MarkerSeekBar sbReverbLowRatio;
	MarkerSeekBar sbReverbLevel;
	MarkerSeekBar sbSpringReverb;
	MarkerSeekBar sbSpringReverbFilter;
//	String [] compressorType = getResources().getStringArray(R.array.compressor_types);
	private SysExCommands sysExCommands;
	
	int TIME_MAX = 0x0c8;     // 0.3s - 20 sec, 0xC8 = 200
	int TIME_MIN = 0x03;
	
	int PRE_MAX = 0x07d0;     // 0.1 ms - 200ms, 2000
	int PRE_MIN = 0x01; 
	
	int HIGHCUT_MAX = 0x3e81; // 16001Hz -> 16001 = Thru 
	int HIGHCUT_MIN = 0x3e8;  // 1000Hz
	
	int LOWCUT_MAX = 0x1f40;  // 8000Hz
	int LOWCUT_MIN = 0x15;    // 22Hz, 21 = Thru 
	
	int HIGHRATIO_MAX = 0x0a; 
	int HIGHRATIO_MIN = 0x01;
	
	int LOWRATIO_MAX = 0x0e; 
	int LOWRATIO_MIN = 0x01;
	
	public boolean initialized;
	
	public Reverb(MainActivity activity) {
		this.activity = activity;
		sysExCommands = new SysExCommands();
		setupUI();
		initialized = true;
	}
	
	private void setupUI() {
		final ReverbData rd = activity.getReverbData();
		spinnerReverbType = (Spinner) activity.findViewById(R.id.spinnerReverbType); 
		btnReverbOnOff = (Button) activity.findViewById(R.id.btnReverbOnOff); 
		
		sbReverbTime = (MarkerSeekBar) activity.findViewById(R.id.sbReverbTime); 
		sbReverbPre = (MarkerSeekBar) activity.findViewById(R.id.sbReverbPre); 
		sbReverbHighCut = (MarkerSeekBar) activity.findViewById(R.id.sbReverbHighCut); 
		sbReverbLowCut = (MarkerSeekBar) activity.findViewById(R.id.sbReverbLowCut); 
		sbReverbHighRatio = (MarkerSeekBar) activity.findViewById(R.id.sbReverbHighRatio); 
		sbReverbLowRatio = (MarkerSeekBar) activity.findViewById(R.id.sbReverbLowRatio); 
		sbReverbLevel = (MarkerSeekBar) activity.findViewById(R.id.sbReverbLevel); 
		sbSpringReverb = (MarkerSeekBar) activity.findViewById(R.id.sbSpringReverb); 
		sbSpringReverbFilter = (MarkerSeekBar) activity.findViewById(R.id.sbSpringReverbFilter); 
		
		sbReverbTime.setMax(TIME_MAX); // 3 - 0x0148
		sbReverbTime.setProgress(rd.getTime());
		
		sbReverbPre.setMax(PRE_MAX);  // 1 - 0x0f50
		sbReverbPre.setProgress(rd.getPre());

		sbReverbHighCut.setMax(HIGHCUT_MAX);  // 0768-7d01
		sbReverbHighCut.setProgress(rd.getHighcut());
		
		sbReverbLowCut.setMax(LOWCUT_MAX);    // 0015-3e40
		sbReverbLowCut.setProgress(rd.getLowcut());
		
		sbReverbHighRatio.setMax(HIGHRATIO_MAX);      // 01-0a
		sbReverbHighRatio.setProgress(rd.getHighratio());
		
		sbReverbLowRatio.setMax(LOWRATIO_MAX);        //01-0e
		sbReverbLowRatio.setProgress(rd.getLowratio());
		
		sbReverbLevel.setMax(0x64);  // 0x0000 - 0x64
		sbReverbLevel.setProgress(rd.getLevel());
		
		sbSpringReverb.setMax(0x64); 
		sbSpringReverb.setProgress(rd.getReverb());
		
		sbSpringReverbFilter.setMax(0x64); 
		sbSpringReverbFilter.setProgress(rd.getFilter());
		
		spinnerReverbType.setOnItemSelectedListener(this);
		updateUI();
		
		btnReverbOnOff.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (activity.getReverbData().isReverbOnOff()){
					btnReverbOnOff.setText("is Off");
					activity.getReverbData().setReverbOnOff(false);
					spinnerReverbType.setEnabled(false);
					activity.onDataChangeUI(sysExCommands.getDeviceOnOffSysEx(SwitchableDevices.REVERB, activity.getReverbData().isReverbOnOff()));
				}else{
					btnReverbOnOff.setText("is On");
					spinnerReverbType.setEnabled(true);
					activity.getReverbData().setReverbOnOff(true);
					//activity.onDataChangeUI(sysExCommands.getReverbTypeSelectSysEx(rd.getReverbType()));
					activity.onDataChangeUI(sysExCommands.getDeviceOnOffSysEx(SwitchableDevices.REVERB, activity.getReverbData().isReverbOnOff()));
				}
				updateUI();
			}
		});
		
		sbReverbTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	            // only change value and text if user touch initiated change. anything else (sysex) will be updated by updateUI()
				//Log.d( TAG, "!!!!!!! TIME REVERB setOnSeekBarChangeListener() called!!!!  FROM USER : " + Boolean.toString(fromUser));
	            if(fromUser){
	            	if(progress <= TIME_MIN){
	            		progress = TIME_MIN + progress;
	            	}	
	            	activity.getReverbData().setTime(progress);	
	            	activity.onDataChangeUI(sysExCommands.getReverbValuesSysEx(ReverbData.reverbTypeRoomPlateHall, progress,"Time"));
	            	((TextView)activity.findViewById(R.id.sbReverbTimeText)).setText("Time " + progress/10.0 + " sec");
	            }
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbReverbTime.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
            	return progress/10.0 + " sec";
            }
            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });		
		
		sbReverbPre.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				//Log.d( TAG, "!!!!!!! PRE REVERB setOnSeekBarChangeListener() called!!!! FROM USER : " + Boolean.toString(fromUser));
	            if(fromUser){
	            	if(progress <= PRE_MIN){
	            		progress = PRE_MIN + progress;
	            	}
	            	activity.getReverbData().setPre(progress);	
	            	activity.onDataChangeUI(sysExCommands.getReverbValuesSysEx(ReverbData.reverbTypeRoomPlateHall, progress,"Pre"));
					((TextView)activity.findViewById(R.id.sbReverbPreText)).setText("Pre "+  progress/10.0 + " ms");
	            }
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbReverbPre.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
            	return progress/10.0 + " ms";
            }
            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });			
		
		sbReverbLowCut.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	            if(fromUser){
	            	if(progress <= LOWCUT_MIN){
	            		progress = LOWCUT_MIN + progress;
	            	}
		            activity.getReverbData().setLowcut(progress);	
		            activity.onDataChangeUI(sysExCommands.getReverbValuesSysEx(ReverbData.reverbTypeRoomPlateHall, progress,"LowCut"));
					if (progress < 22)
						((TextView)activity.findViewById(R.id.sbReverbLowCutText)).setText("Low Cut Thru ");
					else
						((TextView)activity.findViewById(R.id.sbReverbLowCutText)).setText("Low Cut "+ Integer.toString(progress) + " Hz");
	            }
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbReverbLowCut.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
				if (progress < 22)
					return "Thru";
				else
					return Integer.toString(progress) + " Hz";
            }
            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });		
		
		sbReverbHighCut.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	            if(fromUser){
	            	if(progress <= HIGHCUT_MIN){
	            		progress = HIGHCUT_MIN + progress;
	            	}	
		            activity.getReverbData().setHighcut(progress);
		            activity.onDataChangeUI(sysExCommands.getReverbValuesSysEx(ReverbData.reverbTypeRoomPlateHall, progress,"HighCut"));
					if (progress > 16000)
						((TextView)activity.findViewById(R.id.sbReverbHighCutText)).setText("High Cut Thru");
					else
						((TextView)activity.findViewById(R.id.sbReverbHighCutText)).setText("High Cut "+ Integer.toString(progress) + " Hz");
	            }
					
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbReverbHighCut.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
            	if (progress > 16000)
					return "Thru";
				else
					return Integer.toString(progress) + " Hz";
            }
            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });			
		
		sbReverbLowRatio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	            if(fromUser){
	            	if(progress <= LOWRATIO_MIN){
	            		progress = LOWRATIO_MIN + progress;
	            	}
		            activity.getReverbData().setLowratio((byte)progress);
					activity.onDataChangeUI(sysExCommands.getReverbValuesSysEx(ReverbData.reverbTypeRoomPlateHall, progress,"LowRatio"));
					((TextView)activity.findViewById(R.id.sbReverbLowRatioText)).setText("Low Ratio "+ Integer.toString(progress));
	            }
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbReverbLowRatio.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
            	return Integer.toString(progress);
            }
            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });			
		
		sbReverbHighRatio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	            if(fromUser){
	            	if(progress <= HIGHRATIO_MIN){
	            		progress = HIGHRATIO_MIN + progress;
	            	}
		            activity.getReverbData().setHighratio((byte)progress);
		            activity.onDataChangeUI(sysExCommands.getReverbValuesSysEx(ReverbData.reverbTypeRoomPlateHall, progress,"HighRatio"));
					((TextView)activity.findViewById(R.id.sbReverbHighRatioText)).setText("High Ratio "+ Integer.toString(progress));
	            }
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbReverbHighRatio.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
            	return Integer.toString(progress);
            }
            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });		
		
		sbReverbLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					activity.onDataChangeUI(sysExCommands.getReverbValuesSysEx(ReverbData.reverbTypeRoomPlateHall, progress,"Level"));
					activity.getReverbData().setLevel((byte)progress);
					((TextView)activity.findViewById(R.id.sbReverbLevelText)).setText("Level "+ Integer.toString(progress));
				}
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbReverbLevel.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
            	return Integer.toString(progress);
            }
            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });			
		
		sbSpringReverb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					activity.onDataChangeUI(sysExCommands.getReverbValuesSysEx(ReverbData.reverbTypeSpring, progress,"Reverb"));
					activity.getReverbData().setReverb((byte)progress);
					((TextView)activity.findViewById(R.id.sbSpringReverbText)).setText("Reverb "+ Integer.toString(progress));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbSpringReverb.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
            	return Integer.toString(progress);
            }
            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });			
		
		sbSpringReverbFilter.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				activity.onDataChangeUI(sysExCommands.getReverbValuesSysEx(ReverbData.reverbTypeSpring, progress,"Filter"));
				activity.getReverbData().setFilter((byte)progress);
				((TextView)activity.findViewById(R.id.sbSpringReverbFilterText)).setText("Filter "+ Integer.toString(progress));
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbSpringReverbFilter.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
            	return Integer.toString(progress);
            }
            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });			

	}
	
	public void updateUI(){
		ReverbData rd = activity.getReverbData();
		String reverbStatus = rd.getReverbType();
		if(rd.getReverbType().equals(ReverbData.reverbTypeHall)){
			spinnerReverbType.setSelection(getIndex(spinnerReverbType,ReverbData.reverbTypeHall));
		}else if ( rd.getReverbType().equals(ReverbData.reverbTypeRoom)){
			spinnerReverbType.setSelection(getIndex(spinnerReverbType,ReverbData.reverbTypeRoom));
		}else if ( rd.getReverbType().equals(ReverbData.reverbTypePlate)){
			spinnerReverbType.setSelection(getIndex(spinnerReverbType,ReverbData.reverbTypePlate));
		}else if ( rd.getReverbType().equals(ReverbData.reverbTypeSpring)){
			spinnerReverbType.setSelection(getIndex(spinnerReverbType,ReverbData.reverbTypeSpring));
		}
		
		if(rd.isReverbOnOff()){
			btnReverbOnOff.setText("is On");
			spinnerReverbType.setEnabled(true);
			reverbStatus = reverbStatus.concat(" On");
		}
		else{
			btnReverbOnOff.setText("is Off");
			spinnerReverbType.setEnabled(false);
			reverbStatus = reverbStatus.concat(" Off");
		}
		
		if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			activity.btnReverb.setText(Html.fromHtml("Reverb " +  "<br />" +  "<small>" + reverbStatus + "</small>" ));
		}else{
			activity.btnReverb.setText(Html.fromHtml("Rev " +  "<br />" +  "<small>" + reverbStatus + "</small>" ));
		}
		
		
		sbReverbTime.setProgress(rd.getTime());
		sbReverbTime.refreshDrawableState();
		
		sbReverbPre.setProgress(rd.getPre());
		sbReverbPre.refreshDrawableState();
		
		sbReverbHighCut.setProgress(rd.getHighcut());
		sbReverbHighCut.refreshDrawableState();
		
		sbReverbLowCut.setProgress(rd.getLowcut());
		sbReverbLowCut.refreshDrawableState();
		
		sbReverbHighRatio.setProgress(rd.getHighratio());
		sbReverbHighRatio.refreshDrawableState();
		
		sbReverbLowRatio.setProgress(rd.getLowratio());
		sbReverbLowRatio.refreshDrawableState();
		
		sbReverbLevel.setProgress(rd.getLevel());
		sbReverbLevel.refreshDrawableState();
		
		sbSpringReverb.setProgress(rd.getReverb());
		sbSpringReverb.refreshDrawableState();
		
		sbSpringReverbFilter.setProgress(rd.getFilter());
		sbSpringReverbFilter.refreshDrawableState();
		
		((TextView)activity.findViewById(R.id.sbReverbTimeText)).setText("Time " + rd.getTime()/10.0 + " sec");
		((TextView)activity.findViewById(R.id.sbReverbPreText)).setText("Pre "+ + rd.getPre()/10.0 + " ms");
		
		if (rd.getLowcut() < 22)
			((TextView)activity.findViewById(R.id.sbReverbLowCutText)).setText("Low Cut Thru ");
		else
			((TextView)activity.findViewById(R.id.sbReverbLowCutText)).setText("Low Cut "+ Integer.toString(rd.getLowcut()) + " Hz");
		
		if (rd.getHighcut() > 16000)
			((TextView)activity.findViewById(R.id.sbReverbHighCutText)).setText("High Cut Thru");
		else
			((TextView)activity.findViewById(R.id.sbReverbHighCutText)).setText("High Cut "+ Integer.toString(rd.getHighcut()) + " Hz");	
		
		((TextView)activity.findViewById(R.id.sbReverbLowRatioText)).setText("Low Ratio "+ Integer.toString(rd.getLowratio()));
		((TextView)activity.findViewById(R.id.sbReverbHighRatioText)).setText("High Ratio "+ Integer.toString(rd.getHighratio()));
		((TextView)activity.findViewById(R.id.sbReverbLevelText)).setText("Level "+ Integer.toString(rd.getLevel()));
		((TextView)activity.findViewById(R.id.sbSpringReverbText)).setText("Reverb "+ Integer.toString(rd.getReverb()));
		((TextView)activity.findViewById(R.id.sbSpringReverbFilterText)).setText("Filter "+ Integer.toString(rd.getFilter()));
		
		Log.d( TAG, "Reverb::updateUI() Type "+ rd.getReverbType() + " Time " + rd.getTime() + " Pre " + rd.getPre() + " Reverb "
		+ rd.getReverb() + " Filter " + rd.getFilter() + " LowCut " + rd.getLowcut() +  
		" HighCut " + rd.getHighcut() + " HighRatio" + rd.getHighratio() + " LowRatio" + 
		rd.getLowratio() + " Level " + rd.getLevel() + " On " + Boolean.toString(rd.isReverbOnOff()));
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
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		ReverbData rd = activity.getReverbData();
		String selReverbType = parent.getItemAtPosition(position).toString();
		
		if(!rd.getReverbType().equals(selReverbType)){
			activity.onDataChangeUI(sysExCommands.getReverbTypeSelectSysEx(selReverbType));
			rd.setReverbType(selReverbType);
		}
		
		if(rd.getReverbType().equals(ReverbData.reverbTypeHall) || rd.getReverbType().equals(ReverbData.reverbTypeRoom) || rd.getReverbType().equals(ReverbData.reverbTypePlate)){
			
			// not visible
			activity.findViewById(R.id.sbSpringReverbText).setVisibility(View.GONE);
			sbSpringReverb.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbSpringReverbFilterText).setVisibility(View.GONE);
			sbSpringReverbFilter.setVisibility(View.GONE);
			
			// visible
			activity.findViewById(R.id.sbReverbTimeText).setVisibility(View.VISIBLE);
			sbReverbTime.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbReverbPreText).setVisibility(View.VISIBLE);
			sbReverbPre.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbReverbHighCutText).setVisibility(View.VISIBLE);
			sbReverbHighCut.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbReverbLowCutText).setVisibility(View.VISIBLE);
			sbReverbLowCut.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbReverbHighRatioText).setVisibility(View.VISIBLE);
			sbReverbHighRatio.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbReverbLowRatioText).setVisibility(View.VISIBLE);
			sbReverbLowRatio.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbReverbLevelText).setVisibility(View.VISIBLE);
			sbReverbLevel.setVisibility(View.VISIBLE);
			
		}else if(rd.getReverbType().equals(ReverbData.reverbTypeSpring)) {
			
			// visible
			activity.findViewById(R.id.sbSpringReverbText).setVisibility(View.VISIBLE);
			sbSpringReverb.setVisibility(View.VISIBLE);
			
			activity.findViewById(R.id.sbSpringReverbFilterText).setVisibility(View.VISIBLE);
			sbSpringReverbFilter.setVisibility(View.VISIBLE);
			
			// not visible
			activity.findViewById(R.id.sbReverbTimeText).setVisibility(View.GONE);
			sbReverbTime.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbReverbPreText).setVisibility(View.GONE);
			sbReverbPre.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbReverbHighCutText).setVisibility(View.GONE);
			sbReverbHighCut.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbReverbLowCutText).setVisibility(View.GONE);
			sbReverbLowCut.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbReverbHighRatioText).setVisibility(View.GONE);
			sbReverbHighRatio.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbReverbLowRatioText).setVisibility(View.GONE);
			sbReverbLowRatio.setVisibility(View.GONE);
			
			activity.findViewById(R.id.sbReverbLevelText).setVisibility(View.GONE);
			sbReverbLevel.setVisibility(View.GONE);			
		}		
	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
    

}
