package de.sgrad.yamahathreditor;

import de.sgrad.yamahathreditor.SysExCommands.SwitchableDevices;
import de.sgrad.yamahathreditor.MarkerSeekBar;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class Delay {
	public static String TAG = "THR";
	
	private MainActivity activity;
	
	Button btnDelayOnOff;
	MarkerSeekBar sbDelayTime;
	MarkerSeekBar sbDelayFeedback;
	MarkerSeekBar sbDelayHighCut;
	MarkerSeekBar sbDelayLowCut;
	MarkerSeekBar sbDelayLevel;
	private SysExCommands sysExCommands;
	
	public final static int TIME_MAX = 0x270F; // 27=00100111 0F=00001111 (999.9ms) -> transmitted 4E=01001110 0F=00001111 (7bit encoding)
	public final static int TIME_MIN = 0x01; 
	
	public final static int HIGHCUT_MAX = 0x3e81; // 16001Hz -> 16001 = Thru 
	public final static int HIGHCUT_MIN = 0x3e8;  // 1000Hz
	
	public final static int LOWCUT_MAX = 0x1f40;  // 8000Hz
	public final static int LOWCUT_MIN = 0x15;    // 22Hz, 21 = Thru 
	
	public boolean initialized;
	
	public Delay(MainActivity activity) {
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
		final DelayData dd = activity.getDelayData();
		btnDelayOnOff = (Button) activity.findViewById(R.id.btnDelayOnOff); 
		
		sbDelayTime = (MarkerSeekBar) activity.findViewById(R.id.sbDelayTime); 
		sbDelayFeedback = (MarkerSeekBar) activity.findViewById(R.id.sbDelayFeedback); 
		sbDelayHighCut = (MarkerSeekBar) activity.findViewById(R.id.sbDelayHighCut); 
		sbDelayLowCut = (MarkerSeekBar) activity.findViewById(R.id.sbDelayLowCut); 
		sbDelayLevel = (MarkerSeekBar) activity.findViewById(R.id.sbDelayLevel); 
		
		sbDelayTime.setMax(TIME_MAX); // 1 - 4e0f
		sbDelayTime.setProgress(dd.time);
		
		sbDelayFeedback.setMax(0x64); // 64
		sbDelayFeedback.setProgress(dd.feedback);

		sbDelayHighCut.setMax(HIGHCUT_MAX);  // 0768 - 7d01
		sbDelayHighCut.setProgress(dd.highcut);
		
		sbDelayLowCut.setMax(LOWCUT_MAX); // 0015 - 3e40
		sbDelayLowCut.setProgress(dd.lowcut);
		
		sbDelayLevel.setMax(0x64);		// 64
		sbDelayLevel.setProgress(dd.level);
		
		updateUI();
		
		btnDelayOnOff.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dd.delayOnOff){
					btnDelayOnOff.setText("is Off");
					dd.delayOnOff = false;
					activity.onDataChangeUI(sysExCommands.getDeviceOnOffSysEx(SwitchableDevices.DELAY, dd.delayOnOff));
				}else{
					btnDelayOnOff.setText("is On");
					dd.delayOnOff = true;
					activity.onDataChangeUI(sysExCommands.getDeviceOnOffSysEx(SwitchableDevices.DELAY, dd.delayOnOff));
				}
				updateUI();
			}
		});
		
		sbDelayTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
		            if(progress <= TIME_MIN){
		                progress = TIME_MIN + progress;
		            }				
		            dd.time = progress;
					activity.onDataChangeUI(sysExCommands.getDelayValuesSysEx(progress,"Time"));
					//DecimalFormat format = new DecimalFormat("0.0"); 
					//((TextView)activity.findViewById(R.id.sbDelayTimeText)).setText("Time "+ format.format(progress/10.0) + " ms");
					((TextView)activity.findViewById(R.id.sbDelayTimeText)).setText("Time " + progress/10.0 + " ms");
				}
			}
			
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		
		sbDelayTime.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
              //  return String.format("%s", progress / 10.0 + " ms");
            	return (progress / 10.0 + " ms");
            }

            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });
		
		sbDelayFeedback.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					dd.feedback = (byte)progress;
					activity.onDataChangeUI(sysExCommands.getDelayValuesSysEx(progress,"Feedback"));
					((TextView)activity.findViewById(R.id.sbDelayFeedbackText)).setText("Feedback "+ Integer.toString(progress));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		
		sbDelayFeedback.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String toText(int progress) {
            	return (Integer.toString(progress));
            }

            @Override
            public String onMeasureLongestText(int seekBarMax) {
                return toText(seekBarMax);
            }
        });
		
		sbDelayHighCut.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
		            if(progress <= HIGHCUT_MIN){
		                progress = HIGHCUT_MIN + progress;
		            }	
		            dd.highcut = progress;
					activity.onDataChangeUI(sysExCommands.getDelayValuesSysEx(progress,"HighCut"));
					if (progress > 16000)
						((TextView)activity.findViewById(R.id.sbDelayHighCutText)).setText("High Cut Thru");
					else
						((TextView)activity.findViewById(R.id.sbDelayHighCutText)).setText("High Cut "+ Integer.toString(progress) + " Hz");
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbDelayHighCut.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
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
		
		sbDelayLowCut.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
		            if(progress <= LOWCUT_MIN){
		                progress = LOWCUT_MIN + progress;
		            }	
		            dd.lowcut = progress;
					activity.onDataChangeUI(sysExCommands.getDelayValuesSysEx(progress,"LowCut"));
					if (progress < 22)
						((TextView)activity.findViewById(R.id.sbDelayLowCutText)).setText("Low Cut Thru ");
					else
						((TextView)activity.findViewById(R.id.sbDelayLowCutText)).setText("Low Cut "+ Integer.toString(progress) + " Hz");
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbDelayLowCut.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
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
		
		sbDelayLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					dd.level = (byte)progress;
					activity.onDataChangeUI(sysExCommands.getDelayValuesSysEx(progress,"Level"));
					((TextView)activity.findViewById(R.id.sbDelayLevelText)).setText("Level "+ Integer.toString(progress));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		sbDelayLevel.setProgressAdapter(new MarkerSeekBar.ProgressAdapter() {
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
		final DelayData dd = activity.getDelayData();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
        		
        		if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            		if(dd.delayOnOff){
            			btnDelayOnOff.setText("is On");
            			activity.btnDelay.setText(Html.fromHtml("Delay " +  "<br />" +  "<small>" + "On" + "</small>" ));
            		}
            		else{
            			btnDelayOnOff.setText("is Off");
            			activity.btnDelay.setText(Html.fromHtml("Delay " +  "<br />" +  "<small>" + "Off" + "</small>" ));
            		}        			
        		}else{
            		if(dd.delayOnOff){
            			btnDelayOnOff.setText("is On");
            			activity.btnDelay.setText(Html.fromHtml("Dly " +  "<br />" +  "<small><small>" + "On" + "</small></small>" ));
            		}
            		else{
            			btnDelayOnOff.setText("is Off");
            			activity.btnDelay.setText(Html.fromHtml("Dly " +  "<br />" +  "<small><small>" + "Off" + "</small></small>" ));
            		}        			
        		}
        		
        		sbDelayTime.setProgress(dd.time);
        		sbDelayTime.refreshDrawableState();
        		
        		sbDelayFeedback.setProgress(dd.feedback);
        		sbDelayFeedback.refreshDrawableState();
        		
        		sbDelayHighCut.setProgress(dd.highcut);
        		sbDelayHighCut.refreshDrawableState();
        		
        		sbDelayLowCut.setProgress(dd.lowcut);
        		sbDelayLowCut.refreshDrawableState();
        		
        		sbDelayLevel.setProgress(dd.level);
        		sbDelayLevel.refreshDrawableState();
        		
        		((TextView)activity.findViewById(R.id.sbDelayTimeText)).setText("Time " + dd.time/10.0 + " ms");
        		((TextView)activity.findViewById(R.id.sbDelayFeedbackText)).setText("Feedback "+ Integer.toString(dd.feedback));
        		if (dd.highcut >= HIGHCUT_MAX )
        			((TextView)activity.findViewById(R.id.sbDelayHighCutText)).setText("High Cut Thru");
        		else
        			((TextView)activity.findViewById(R.id.sbDelayHighCutText)).setText("High Cut "+ Integer.toString(dd.highcut) + " Hz");	

        		if (dd.lowcut <= LOWCUT_MIN)
        			((TextView)activity.findViewById(R.id.sbDelayLowCutText)).setText("Low Cut Thru ");
        		else
        			((TextView)activity.findViewById(R.id.sbDelayLowCutText)).setText("Low Cut "+ Integer.toString(dd.lowcut) + " Hz");
        		((TextView)activity.findViewById(R.id.sbDelayLevelText)).setText("Level "+ Integer.toString(dd.level));
            }
        });

	}

}
