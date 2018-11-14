package de.sgrad.yamahathreditor;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class GraphicsEqualizer extends Fragment{
	public static String TAG = "GraphicsEqualizer";
	
	private Equalizer mEqualizer;
	private Spinner equalizerPresetSpinner;
	private MainActivity activity;
	private LinearLayout mLinearLayout;
	private LinearLayout mLinearLayoutFragmentBase;
	private Button btnOnOff;
	
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	
        if (activity instanceof MainActivity) {
        	this.activity = (MainActivity) getActivity();
        	mEqualizer = new Equalizer(0, this.activity.getMediaPlayerReference().getAudioSessionId());
        }
    }
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	Log.d("TimeStretchActivity", "onCreateView ");
    	View rootView = inflater.inflate(de.sgrad.yamahathreditor.R.layout.equalizer, container, false);
    	mLinearLayoutFragmentBase = (LinearLayout)rootView.findViewById(R.id.fragment_main_layout);
    	
    	setupEqualizerFxAndUI();
    	
    	return rootView;
    	//return inflater.inflate(de.sgrad.timestretch.R.layout.eq_fragment, container, false);
    }
    
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
		//mEqualizer.setEnabled(true);
    }
	
	/*
	 * displays the SeekBar sliders for the supported equalizer frequency bands
	 * user can move sliders to change the frequency of the bands
	 */
	private void setupEqualizerFxAndUI() {
		// get reference to linear layout for the seekBars
		mLinearLayout = (LinearLayout) mLinearLayoutFragmentBase.findViewById(de.sgrad.yamahathreditor.R.id.linearLayoutEqual);
		Log.d("TimeStretchActivity", "setupEqualizerFxAndUI " + mLinearLayout);
		
		equalizerPresetSpinner = (Spinner) mLinearLayoutFragmentBase.findViewById(de.sgrad.yamahathreditor.R.id.spinner); 
		btnOnOff = (Button) mLinearLayoutFragmentBase.findViewById(de.sgrad.yamahathreditor.R.id.btnEQOnOff); 
		
		btnOnOff.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mEqualizer.getEnabled()){
					mEqualizer.setEnabled(false);
					activity.playerFragment.BassBoostOnOff(false);
					btnOnOff.setText("is Off");
				}else{
					mEqualizer.setEnabled(true);
					activity.playerFragment.BassBoostOnOff(true);
					btnOnOff.setText("is On");
				}
			}
		});
		
		/*
		// equalizer heading
		TextView equalizerHeading = new TextView(activity);
		equalizerHeading.setText("Equalizer");
		equalizerHeading.setTextSize(15);
		equalizerHeading.setGravity(Gravity.CENTER_HORIZONTAL);
		equalizerHeading.setTextColor(Color.WHITE);
		mLinearLayout.addView(equalizerHeading);
		 */	
		// get number frequency bands supported by the equalizer engine
		short numberFrequencyBands = mEqualizer.getNumberOfBands();

		// get the level ranges to be used in setting the band level
		// get lower limit of the range in milliBels
		final short lowerEqualizerBandLevel = mEqualizer.getBandLevelRange()[0];
		// get the upper limit of the range in millibels
		final short upperEqualizerBandLevel = mEqualizer.getBandLevelRange()[1];

		// loop through all the equalizer bands to display the band headings, lower & upper levels and the seek bars
		for (short i = 0; i < numberFrequencyBands; i++) {
			final short equalizerBandIndex = i;

			// frequency header for each seekBar
			TextView frequencyHeaderTextview = new TextView(activity);
			frequencyHeaderTextview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			frequencyHeaderTextview.setGravity(Gravity.CENTER_HORIZONTAL);
			frequencyHeaderTextview.setText((mEqualizer.getCenterFreq(equalizerBandIndex) / 1000) + " Hz");
			//frequencyHeaderTextview.setBackgroundColor(Color.GRAY);
			frequencyHeaderTextview.setTextColor(Color.WHITE);
			mLinearLayout.addView(frequencyHeaderTextview);

			// set up linear layout to contain each seekBar
			LinearLayout seekBarRowLayout = new LinearLayout(activity);
			seekBarRowLayout.setOrientation(LinearLayout.HORIZONTAL);

			// set up lower level textview for activity seekBar
			TextView lowerEqualizerBandLevelTextview = new TextView(activity);
			lowerEqualizerBandLevelTextview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
			lowerEqualizerBandLevelTextview.setText((lowerEqualizerBandLevel / 100) + " dB");
			//lowerEqualizerBandLevelTextview.setBackgroundColor(Color.GRAY);
			lowerEqualizerBandLevelTextview.setTextColor(Color.WHITE);
			
			// set up upper level textview for activity seekBar
			TextView upperEqualizerBandLevelTextview = new TextView(activity);
			upperEqualizerBandLevelTextview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
			upperEqualizerBandLevelTextview.setText((upperEqualizerBandLevel / 100) + " dB");
			//upperEqualizerBandLevelTextview.setBackgroundColor(Color.GRAY);
			upperEqualizerBandLevelTextview.setTextColor(Color.WHITE);

			// ********** the seekBar **************
			// set the layout parameters for the seekbar
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.weight = 1;

			// create a new seekBar
			SeekBar seekBar = new SeekBar(activity);
			// give the seekBar an ID
			seekBar.setId(i);

			seekBar.setLayoutParams(layoutParams);
			seekBar.setMax(upperEqualizerBandLevel - lowerEqualizerBandLevel);
			// set the progress for activity seekBar
			seekBar.setProgress(mEqualizer.getBandLevel(equalizerBandIndex));

			// change progress as its changed by moving the sliders
			seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					mEqualizer.setBandLevel(equalizerBandIndex, (short) (progress + lowerEqualizerBandLevel));
				}

				public void onStartTrackingTouch(SeekBar seekBar) {
					// not used
				}

				public void onStopTrackingTouch(SeekBar seekBar) {
					// not used
				}
			});

			// add the lower and upper band level textviews and the seekBar to the row layout
			seekBarRowLayout.addView(lowerEqualizerBandLevelTextview);
			seekBarRowLayout.addView(seekBar);
			seekBarRowLayout.addView(upperEqualizerBandLevelTextview);

			mLinearLayout.addView(seekBarRowLayout);

		}
		// show the spinner
		equalizeSound();
	}
	/*
	 * shows spinner with list of equalizer presets to choose from - updates the
	 * seekBar progress and gain levels according to those of the selected preset
	 */
	
	private void equalizeSound() {
		// set up the spinner
		ArrayList<String> equalizerPresetNames = new ArrayList<String>();
		ArrayAdapter<String> equalizerPresetSpinnerAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, equalizerPresetNames);
		equalizerPresetSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// get list of the device's equalizer presets
		for (short i = 0; i < mEqualizer.getNumberOfPresets(); i++) {
			equalizerPresetNames.add(mEqualizer.getPresetName(i));
		}
		
		equalizerPresetSpinner.setAdapter(equalizerPresetSpinnerAdapter);

		// handle the spinner item selections
		equalizerPresetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// first list item selected by default and sets the preset accordingly
				mEqualizer.usePreset((short) position);
				// get the number of frequency bands for activity equalizer
				// engine
				short numberFrequencyBands = mEqualizer.getNumberOfBands();
				
				// get the lower gain setting for activity equalizer band
				final short lowerEqualizerBandLevel = mEqualizer.getBandLevelRange()[0];

				// set seekBar indicators according to selected preset
				for (short i = 0; i < numberFrequencyBands; i++) {
					short equalizerBandIndex = i;
					SeekBar seekBar = (SeekBar) mLinearLayout.findViewById(equalizerBandIndex);
					// get current gain setting for activity equalizer band set the progress indicator of activity seekBar to indicate the current gain value
					seekBar.setProgress(mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// not used
			}
		});
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	
    @Override
    public void onResume() {
        super.onResume();
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    }
    

}
