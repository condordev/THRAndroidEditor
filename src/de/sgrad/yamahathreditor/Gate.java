package de.sgrad.yamahathreditor;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import de.sgrad.yamahathreditor.SysExCommands.SwitchableDevices;

public class Gate {
	public static String TAG = "THR";
	
	private MainActivity activity;
	
	Button btnGateOnOff;
	MarkerSeekBar sbGateThreshold;
	MarkerSeekBar sbGateRelease;
	private SysExCommands sysExCommands;
	
	public boolean initialized;
	
	public Gate(MainActivity activity) {
		this.activity = activity;
		sysExCommands = new SysExCommands();
		setupUI();
		initialized = true;
	}
	
	public void setupUI() {
		final GateData gd = activity.getGateData();
		btnGateOnOff = (Button) activity.findViewById(R.id.btnGateOnOff); 
		
		sbGateRelease = (MarkerSeekBar) activity.findViewById(R.id.sbGateRelease); 
		sbGateThreshold = (MarkerSeekBar) activity.findViewById(R.id.sbGateThreshold); 
		
		sbGateRelease.setMax(0x64); 
		sbGateRelease.setProgress(gd.release);
		
		sbGateThreshold.setMax(0x64); 
		sbGateThreshold.setProgress(gd.threshold);

		updateUI();
		
		btnGateOnOff.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (gd.gateOnOff){
					btnGateOnOff.setText("is Off");
					gd.gateOnOff = false;
					activity.onDataChangeUI(sysExCommands.getDeviceOnOffSysEx(SwitchableDevices.GATE, gd.gateOnOff));
				}else{
					btnGateOnOff.setText("is On");
					gd.gateOnOff = true;
					activity.onDataChangeUI(sysExCommands.getDeviceOnOffSysEx(SwitchableDevices.GATE, gd.gateOnOff));
				}
				updateUI();
			}
		});
		
		sbGateRelease.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					gd.release = (byte) progress;
					activity.onDataChangeUI(sysExCommands.getGateValuesSysEx(progress,"Release"));
					((TextView)activity.findViewById(R.id.sbGateReleaseText)).setText("Release "+ Integer.toString(progress));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbGateRelease.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
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
		
		sbGateThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					gd.threshold = (byte) progress;
					activity.onDataChangeUI(sysExCommands.getGateValuesSysEx(progress,"Threshold"));
					((TextView)activity.findViewById(R.id.sbGateThresholdText)).setText("Threshold "+ Integer.toString(progress));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbGateThreshold.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
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
		final GateData gd = activity.getGateData();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
        		sbGateRelease.setProgress(gd.release);
        		sbGateRelease.refreshDrawableState();
        		
        		sbGateThreshold.setProgress(gd.threshold);
        		sbGateThreshold.refreshDrawableState();	
        		
        		if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            		if(gd.gateOnOff){
            			btnGateOnOff.setText("is On");
            		    activity.btnGate.setText(Html.fromHtml("Gate " +  "<br />" +  "<small>" + "On" + "</small>" ));
            		}
            		else{
            			btnGateOnOff.setText("is Off");
            			activity.btnGate.setText(Html.fromHtml("Gate " +  "<br />" +  "<small>" + "Off" + "</small>" ));
            		}        			
        		}else{
            		if(gd.gateOnOff){
            			btnGateOnOff.setText("is On");
            		    activity.btnGate.setText(Html.fromHtml("Gate" +  "<br />" +  "<small><small>" + "Off" + "</small></small>"));
            		}
            		else{
            			btnGateOnOff.setText("is Off");
            			activity.btnGate.setText(Html.fromHtml("Gate" +  "<br />" +  "<small><small>" + "Off" + "</small></small>"));
            		}
        		}
        		
        		
        		((TextView)activity.findViewById(R.id.sbGateReleaseText)).setText("Release "+ Integer.toString(gd.release));
        		((TextView)activity.findViewById(R.id.sbGateThresholdText)).setText("Threshold "+ Integer.toString(gd.threshold));
            }
        });

	}

}
