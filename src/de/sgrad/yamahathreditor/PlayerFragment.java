package de.sgrad.yamahathreditor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
 
public class PlayerFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
	public static final String TAG = "THR";
 
    private ImageButton btnPlay;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ImageButton btnPlaylist;
    private ImageButton btnRepeat;
   // private ImageButton btnShuffle;
    private ImageButton btnLinkPatchOne;
    private ImageButton btnLinkPatchTwo;
    private ImageButton btnEQ;
    private Button btnA;
    private Button btnB;
    private Button btnPatchA;
    private Button btnPatchB;
    private ToggleButton btnAB;
    private SeekBar songProgressBar;
    private TextView songTitleLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    private TextView linkedPatchNameOne;
    private TextView linkedPatchNameTwo;
    private EditText editPatch1;
    private EditText editPatch2;
    KnobView knobVolume = null;
    KnobView knobBassBoost;
    KnobView knobBalance;
    LinearLayout ABLayout;
    LinearLayout patchABLayout;
    LinearLayout timerDisplayLayout;
    LinearLayout player_footer_bg;
    LinearLayout soundControl;
    LinearLayout player_link;
    short bassRoundedStrength = 0;
    RangeSeekBar<Integer> sbLoop;
    
    // Media Player
    public  MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();;
    public static SongManager songManager = null;
    private Utilities utils;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private boolean isRepeat = false;
    public String m_SongPath = null;
    public String m_SongTitle = null;
        
    public MainActivity activity = null;
	public HashMap<String, SongPatchData> songPatch = null;
    private GraphicsEqualizer equalizerFragment = null;
    private AudioManager audioManager = null;
    private BassBoost bb = null;
    private boolean mediaPlayerIsPrepared = false;
    private long currentDuration = 0;
    private long minValueMilliSeconds = 0;
    private long maxValueMilliSeconds = 0; 
    private int minValueMemory = 0;
    private boolean ABOnOff = true;
    private boolean firstPatchRecalled = false;
    private boolean secondPatchRecalled = false;
    //private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    
    PlayerFragment(){
    	Log.d(TAG, "PlayerFragment::PlayerFragment() ");
    	 songManager = new SongManager();
         mp = new MediaPlayer();
         utils = new Utilities();   
         equalizerFragment = new GraphicsEqualizer();
        
         /*
         final Descriptor[] effects = AudioEffect.queryEffects();
		 for (final Descriptor effect : effects) {
		     Log.d(TAG, effect.name.toString() + ", type: " + effect.type.toString());
		 } 
		 */
         
         bb = new BassBoost(0, mp.getAudioSessionId());
 		 bb.setEnabled(true);
 		 //mp.attachAuxEffect(bb.getId());
 		 mp.setAuxEffectSendLevel(1.0f);
 		 
 		if (bb.getStrengthSupported()){
 		    bassRoundedStrength = bb.getRoundedStrength();
 		    Log.d(TAG, "PlayerFragment::bassRoundedStrength: " + bassRoundedStrength);
 		    bb.setStrength(bassRoundedStrength);
 		}

    }
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	Log.d(TAG, "PlayerFragment::onAttach ");
    	
        if (activity instanceof MainActivity) {
        	this.activity = (MainActivity) getActivity();
        	songManager.setContentResolver(activity.getContentResolver());
        }
        audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        deSerializeMap();
    }
    
    public GraphicsEqualizer getEqualizerFragmentReference(){
    	if (equalizerFragment != null)
    		return equalizerFragment;
    	else
    		return null;
    }
    
    public void BassBoostOnOff(boolean on){
    	if(on){
    		bb.setEnabled(true);
    	}else{
    		bb.setEnabled(false);
    	}
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	Log.d(TAG, "PlayerFragment::onActivityCreated ");
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	Log.d(TAG, "PlayerFragment::onCreateView ");
    	View rootView = inflater.inflate(de.sgrad.yamahathreditor.R.layout.player, container, false);
    	
        // All player buttons
        btnPlay = (ImageButton) rootView.findViewById(R.id.btnPlay);
        btnForward = (ImageButton) rootView.findViewById(R.id.btnForward);
        btnBackward = (ImageButton) rootView.findViewById(R.id.btnBackward);
        btnNext = (ImageButton) rootView.findViewById(R.id.btnNext);
        btnPrevious = (ImageButton) rootView.findViewById(R.id.btnPrevious);
        btnPlaylist = (ImageButton) rootView.findViewById(R.id.btnPlaylist);
        btnRepeat = (ImageButton) rootView.findViewById(R.id.btnRepeat);
       // btnShuffle = (ImageButton) rootView.findViewById(R.id.btnShuffle);
        btnLinkPatchOne = (ImageButton) rootView.findViewById(R.id.btnLinkPatchOne);
        btnLinkPatchTwo = (ImageButton) rootView.findViewById(R.id.btnLinkPatchTwo);
        btnEQ = (ImageButton) rootView.findViewById(R.id.btnEQ);
        songProgressBar = (SeekBar) rootView.findViewById(R.id.songProgressBar);
        songTitleLabel = (TextView) rootView.findViewById(R.id.songTitle);
        songCurrentDurationLabel = (TextView) rootView.findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) rootView.findViewById(R.id.songTotalDurationLabel);
        linkedPatchNameOne = (TextView) rootView.findViewById(R.id.linkedPatchNameOne);
        linkedPatchNameTwo = (TextView) rootView.findViewById(R.id.linkedPatchNameTwo);
        knobVolume = (KnobView) rootView.findViewById(R.id.knobVolume);
        knobBassBoost = (KnobView) rootView.findViewById(R.id.knobBassBoost);
        knobBalance = (KnobView) rootView.findViewById(R.id.knobBalance);
        sbLoop = (RangeSeekBar<Integer>) rootView.findViewById(R.id.sbLoop); 
        btnA = (Button) rootView.findViewById(R.id.btnA); 
        btnB = (Button) rootView.findViewById(R.id.btnB); 
        btnPatchA = (Button) rootView.findViewById(R.id.btnPatchA); 
        btnPatchB = (Button) rootView.findViewById(R.id.btnPatchB); 
        editPatch1 = (EditText) rootView.findViewById(R.id.editPatch1); 
        editPatch2 = (EditText) rootView.findViewById(R.id.editPatch2); 
        btnAB = (ToggleButton) rootView.findViewById(R.id.btnAB); 
        ABLayout = (LinearLayout)rootView.findViewById(R.id.ABLayout);
        patchABLayout = (LinearLayout)rootView.findViewById(R.id.PatchABLayout);
        timerDisplayLayout = (LinearLayout)rootView.findViewById(R.id.timerDisplay);
        player_footer_bg = (LinearLayout)rootView.findViewById(R.id.player_footer_bg);
        soundControl = (LinearLayout)rootView.findViewById(R.id.soundControl);
        player_link = (LinearLayout)rootView.findViewById(R.id.player_link);
        
        sbLoop.setUserValuesAsString(true);
      //  sbVolume = (MarkerSeekBar) rootView.findViewById(R.id.sbVolume);
        
        btnPlaylist.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnForward.setOnClickListener(this);
        btnBackward.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        btnRepeat.setOnClickListener(this);
        //btnShuffle.setOnClickListener(this);
        btnLinkPatchOne.setOnClickListener(this);
        btnLinkPatchTwo.setOnClickListener(this);
        btnEQ.setOnClickListener(this);
        btnB.setOnClickListener(this);
        btnA.setOnClickListener(this);
        btnPatchB.setOnClickListener(this);
        btnPatchA.setOnClickListener(this); 
        btnAB.setOnClickListener(this);
        btnAB.setChecked(true);
       // btnAB.setText(Html.fromHtml("A/B" +  "<br />" +  "<small><small>" + "On" + "</small></small>" ));
        
        // Listeners
        songProgressBar.setOnSeekBarChangeListener(this); // Important
        //sbLoop.setOnRangeSeekBarChangeListener(this);
        //mp.setOnCompletionListener(this); // Important
        
        
   //     sbVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        
		knobVolume.setMin(0.0);
		knobVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		knobVolume.setValue(9.0);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 9, 0);
		knobVolume.setAdditionalText("Volume ");
		knobVolume.setKnobListener(new KnobListener() {
			int volumeMemory = 0;
			public void onKnobChanged(double newValue) {
				if(volumeMemory == (int)newValue){
				}else{
					//Log.d(TAG,"Knob Volume Value" + newValue + " Int: " + (int)newValue);  
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)newValue, 0);
				}
				volumeMemory = (int)newValue;
			}
	    });
		
		knobBassBoost.setMin(0.0);
		knobBassBoost.setMax(1000.0);
		knobBassBoost.setValue(500);
		bb.setStrength((short) 500);
		knobBassBoost.setAdditionalText("Bass ");
		knobBassBoost.setNumberFormat("SHORT");
		knobBassBoost.setKnobListener(new KnobListener() {
			public void onKnobChanged(double newValue) {
				//Log.d(TAG,"Knob BassBoost Value" + newValue + " Short: " + (short)newValue);  
				//bb.setStrength((short) newValue);
			}
	    });
		
		knobBassBoost.setKnobListenerUp(new KnobListener() {
			public void onKnobChanged(double newValue) {
				Log.d(TAG,"Knob BassBoost Value" + newValue + " Short: " + (short)newValue);  
				bb.setStrength((short) newValue);
			}
	    });
		
		knobBalance.setMin(0.0);
		knobBalance.setMax(1000.0);
		knobBalance.setValue(500);
		knobBalance.setAdditionalText("  ");
		knobBalance.setNumberFormat("SHORT");
		knobBalance.setKnobListener(new KnobListener() {
			public void onKnobChanged(double newValue) {
				//Log.d(TAG,"Knob BassBoost Value" + newValue + " Short: " + (short)newValue);  
				//bb.setStrength((short) newValue);
			}
	    });
		
	    mp.setOnPreparedListener(new OnPreparedListener() {
	        @Override
	        public void onPrepared(MediaPlayer mp) {
	        	Log.d( TAG, "PlayerFragment::MediaPlayer::setOnPreparedListener()");
	        	maxValueMilliSeconds = mp.getDuration();
	        	sbLoop.setRangeValues(0, (int)maxValueMilliSeconds / 1000);
				sbLoop.setSelectedMaxValue((int)maxValueMilliSeconds / 1000);
				sbLoop.setSelectedMinValue(0);
				Log.d( TAG, "PlayerFragment::setOnPreparedListener() Sec: " + mp.getDuration() + " Minutes: " + utils.milliSecondsToTimer(mp.getDuration()) + 
						" AMin: " + sbLoop.getAbsoluteMinValue() + " AMax: " + sbLoop.getAbsoluteMaxValue() + " Min: " + sbLoop.getSelectedMinValue() +  " Max: " + sbLoop.getSelectedMaxValue());
				mediaPlayerIsPrepared = true;
				playSong(m_SongPath);
	        }
	    });
	    
 		sbLoop.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>(){
 			@Override
 			public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
 				//Log.d(TAG, "PlayerFragment::onRangeSeekBarValuesChanged " + minValue + " " + maxValue);
 				minValueMilliSeconds = minValue * 1000;
 				maxValueMilliSeconds = maxValue * 1000;
 				
 				// handle only min ValueChange
 				if(minValue != minValueMemory){
 					minValueMemory = minValue;
 					if(ABOnOff && mediaPlayerIsPrepared && mp.isPlaying()){
 						mp.seekTo((int)minValueMilliSeconds);
 					}
 				}
 			}
 			
 		});
 		
 		
        editPatch1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ( (actionId == EditorInfo.IME_ACTION_DONE)){
                	Log.d( TAG, "PlayerFragment::IME_ACTION_DONE() " + editPatch1.getText());
                	if(songPatch.containsKey(m_SongPath)){
                		songPatch.get(m_SongPath).setFirstPatchTime(Utilities.timerToSeconds(editPatch1.getText().toString()));
                	}
                }
                return false;
            }
        });
        
        editPatch1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                	editPatch1.getText().clear();
                }
            }
        });
        
        editPatch2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ( (actionId == EditorInfo.IME_ACTION_DONE)){
                	if(songPatch.containsKey(m_SongPath)){
                		songPatch.get(m_SongPath).setSecondPatchTime(Utilities.timerToSeconds(editPatch2.getText().toString()));
                	}
                }
                return false;
            }
        });
        
        editPatch2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                	editPatch2.getText().clear();
                }
            }
        });        
        
	    /*
	    mp.setOnSeekCompleteListener(new OnSeekCompleteListener() {
			@Override
			public void onSeekComplete(MediaPlayer mp) {
				Log.d( TAG, "PlayerFragment::MediaPlayer::setOnSeekCompleteListener()");
			}
		});
	    */
        
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                // check for repeat is ON or OFF
            	if(isRepeat){
                    // repeat is on play same song again
                    playSong(m_SongPath);
                }else{
                	//mHandler.removeCallbacks(mUpdateTimeTask);
                	mp.stop();
                	//mp.release();
                	minValueMemory = 0;
                	mediaPlayerIsPrepared = false;
                	mp.reset();
    				try {
						mp.setDataSource(m_SongPath);
						mp.prepare();                	
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
                	btnPlay.setImageResource(R.drawable.btn_play);
                }
            }
        });
        
    	return rootView;
    }
    
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.player);
        //Log.d( TAG, "PlayerFragment::onCreate()");

    }
    
	/**
	 * Receiving song data from playlist view and play the song
	 * */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 100){
         	m_SongPath = data.getExtras().getString("songPath");
         	m_SongTitle = data.getExtras().getString("songTitle");
         	songTitleLabel.setText(m_SongTitle);
         	activity.findViewById(R.id.btnPlay).setEnabled(true);
         	
         	//Log.d( TAG, "PlayerFragment::onActivityResult() " + m_SongPath);
         	
         	
         	if(songPatch.containsKey(m_SongPath)){
         		editPatch1.setText(Utilities.secondsToTimer(songPatch.get(m_SongPath).getFirstPatchTime()));
         		editPatch2.setText(Utilities.secondsToTimer(songPatch.get(m_SongPath).getSecondPatchTime()));
         		
	         	String name = songPatch.get(m_SongPath).getFirstPatchName();
	         	if(!name.isEmpty()){
	         		linkedPatchNameOne.setText("P1: " + name);
	         	}
         		
	         	name = songPatch.get(m_SongPath).getSecondPatchName();
	         	if(!name.isEmpty()){
	         		linkedPatchNameTwo.setText("P2: " + name);
	         	}
	         	
	         	updateSongFirstPresetValues();

         	}else{
         		editPatch1.setText("00:00");
         		editPatch2.setText("00:00");
         		linkedPatchNameOne.setText("no patch");
         		linkedPatchNameTwo.setText("no patch");
         	}
         	
         	// print all songPatch
         	for (Entry<String, SongPatchData> entry : songPatch.entrySet()) {
         	    String key = entry.getKey();
         	    SongPatchData value = entry.getValue();
         	    Log.d( TAG, "PlayerFragment::onActivityResult() songPatch: " + key + " Patch 1: " + value.getFirstPatchName() + " Patch 2: " + value.getSecondPatchName());
         	}
         	
            try {
            	minValueMemory = 0;
            	mediaPlayerIsPrepared = false;
            	mp.reset();
				mp.setDataSource(m_SongPath);
				mp.prepare();
				
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
    
 
    /**
     * Function to play a song
     * @param songIndex - index of song
     * */
    public void  playSong(String songPath){
        // Play song
        try {
          //  mp.reset();
          //  mp.setDataSource(songPath);
          //  mp.prepare();
            mp.start();
            // Displaying Song title
            //String songTitle = songsList.get(songIndex).get("songTitle");
            //songTitleLabel.setText(m_SongTitle);
 
            // Changing Button Image to pause image
            btnPlay.setImageResource(R.drawable.btn_pause);
 
            // set Progress bar values
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);
 
            // Updating progress bar
            updateProgressBar();
            
            if(!isRepeat){
            	mp.pause();
            	btnPlay.setImageResource(R.drawable.btn_play);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
 
    /**
     * Update timer on seekbar
     * */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }   
 
    /**
     * Background Runnable thread
     * */
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			if (mp != null) {
				long totalDuration = mp.getDuration();
				currentDuration = mp.getCurrentPosition();

				if(ABOnOff && currentDuration >= maxValueMilliSeconds) {
					mp.seekTo((int) minValueMilliSeconds);
				}

				if(songPatch.containsKey(m_SongPath)) {
					if(mp.getCurrentPosition() / 1000 >= songPatch.get(m_SongPath).getFirstPatchTime() && !songPatch.get(m_SongPath).getFirstPatch().isEmpty()){
						if(!firstPatchRecalled){
							firstPatchRecalled = true;
							updateSongFirstPresetValues();
						}
					}else{
						firstPatchRecalled = false;
					}
					
					if(mp.getCurrentPosition() / 1000 >= songPatch.get(m_SongPath).getSecondPatchTime() && !songPatch.get(m_SongPath).getSecondPatch().isEmpty()){
						if(!secondPatchRecalled){
							updateSongSecondPresetValues();
							secondPatchRecalled = true;
						}
					}else{
						secondPatchRecalled = false;
					}
					
				}

				// Displaying Total Duration time
				songTotalDurationLabel.setText("" + utils.milliSecondsToTimer(totalDuration));
				// Displaying time completed playing
				songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(currentDuration));

				// Updating progress bar
				int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
				
				// Log.d("Progress", ""+progress);
				songProgressBar.setProgress(progress);

				// Running this thread after 100 milliseconds
				mHandler.postDelayed(this, 100);
			}
		}
	};
 
    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }
 
    /**
     * When user starts moving the progress handler
     * */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }
 
    /**
     * When user stops moving the progress hanlder
     * */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    	if(mediaPlayerIsPrepared){
	        mHandler.removeCallbacks(mUpdateTimeTask);
	        int totalDuration = mp.getDuration();
	        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
	 
	        // forward or backward to certain seconds
	        mp.seekTo(currentPosition);
	 
	        // update timer progress again
	        updateProgressBar();
    	}
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	//Log.d( TAG, "PlayerFragment::onPause()");
    }
    
    @Override
    public void onStop(){
    	super.onStop();
    	//Log.d( TAG, "PlayerFragment::onStop()");
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	//Log.d( TAG, "PlayerFragment::onResume()");
    }

    @Override
     public void onDestroy(){
     super.onDestroy();
     	//Log.d( TAG, "PlayerFragment::onDestroy()");
     	serializeMap(songPatch);
        mp.release();
        mp = null;
     }
    
	public void updateSongFirstPresetValues(){
		if(songPatch.containsKey(m_SongPath)){
			byte [] preset = songPatch.get(m_SongPath).getFirstPatch().getBytes();
			byte [] sysExDump = activity.patch.getPresetSysExDump(preset);
			//String name = Patch.bytesToString(preset);
			String name = songPatch.get(m_SongPath).getFirstPatchName();
			linkedPatchNameOne.setText("P1: " + name);
			activity.btnPatch.setText(name);
			activity.patch.updatePatchComponentValues(preset);
			activity.sendSysExToDevice(sysExDump);
			Log.d(TAG, "PlayerFragment::updateSongFirstPresetValues() " + m_SongPath + " Patch: " + name);
	        btnLinkPatchTwo.setEnabled(true);
	        linkedPatchNameTwo.setEnabled(true);
		}else{
			Log.d(TAG, "PlayerFragment::updateSongFirstPresetValues() m_SongPath not in songPatch" );
			linkedPatchNameOne.setText("no patch");
	        btnLinkPatchTwo.setEnabled(false);
	        linkedPatchNameTwo.setEnabled(false);
		}
	}
	
	public void updateSongSecondPresetValues(){
		if(songPatch.containsKey(m_SongPath)){
			if(songPatch.get(m_SongPath).getSecondPatch() != null){
				byte [] preset = songPatch.get(m_SongPath).getSecondPatch().getBytes();
				byte [] sysExDump = activity.patch.getPresetSysExDump(preset);
				//String name = Patch.bytesToString(preset);
				String name = songPatch.get(m_SongPath).getSecondPatchName();
				linkedPatchNameTwo.setText("P2: " + name);
				activity.btnPatch.setText(name);
				activity.patch.updatePatchComponentValues(preset);
				activity.sendSysExToDevice(sysExDump);
				Log.d(TAG, "PlayerFragment::updateSongSecondPresetValues() " + m_SongPath + " Patch: " + name);
			}
		}else{
			Log.d(TAG, "PlayerFragment::updateSongSecondPresetValues() m_SongPath not in songPatch" );
			linkedPatchNameTwo.setText("no patch");
		}
	}

	@Override
	public void onClick(View v) {
        switch (v.getId()) {
        
            case R.id.btnPlaylist:
                Intent i = new Intent(activity.getApplicationContext(), PlayListActivity.class);
                startActivityForResult(i, 100);
                break;
                
            case R.id.btnPlay:
                // check for already playing
            	if(mp!=null){
            		Log.d( TAG, "PlayerFragment::onClick() mp.isPlaying(): " + mp.isPlaying());
	                if(mp.isPlaying()){
                        mp.pause();
                        // Changing button image to play button
                        btnPlay.setImageResource(R.drawable.btn_play);
	                }else{
	                    // Resume song
                        mp.start();
                        // Changing button image to pause button
                        btnPlay.setImageResource(R.drawable.btn_pause);
                        //playSong(m_SongPath);
	                }
            	}else{
            		
            	}
                break;
              
            /*
            case R.id.btnShuffle:
                if(isShuffle){
                    isShuffle = false;
                    Toast.makeText(activity.getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
                }else{
                    // make repeat to true
                    isShuffle= true;
                    Toast.makeText(activity.getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isRepeat = false;
                    btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                }            	
                break;
             */   
            case R.id.btnForward:
                // get current song position
                int currentPosition = mp.getCurrentPosition();
                // check if seekForward time is lesser than song duration
                if(currentPosition + seekForwardTime <= mp.getDuration()){
                    // forward song
                    mp.seekTo(currentPosition + seekForwardTime);
                }else{
                    // forward to end position
                    mp.seekTo(mp.getDuration());
                }            	
                break;

            case R.id.btnBackward:
                // get current song position
                currentPosition = mp.getCurrentPosition();
                // check if seekBackward time is greater than 0 sec
                if(currentPosition - seekBackwardTime >= 0){
                    // forward song
                    mp.seekTo(currentPosition - seekBackwardTime);
                }else{
                    // backward to starting position
                    mp.seekTo(0);
                }
             	
                break;

            case R.id.btnLinkPatchOne:
             	if(m_SongPath != null){
                	AlertDialog.Builder alert = new AlertDialog.Builder(activity); 
                	final EditText edittext = new EditText(activity);
                	alert.setMessage("Enter patch name or click 'Unlink' to remove assigned patch from song. Press 'Back' to cancel dialog.");
                	alert.setTitle("Song Preset Linking");
                	
                	
                	String patchBtnText = (String) activity.btnPatch.getText();
                	if(patchBtnText.equals("Load Library"))
                		edittext.setText("");
                	else
                		edittext.setText(patchBtnText);
                	
                	alert.setView(edittext);
                	
                	alert.setNegativeButton("Unlink", new DialogInterface.OnClickListener() {
                	    public void onClick(DialogInterface dialog, int whichButton) {
                	    	songPatch.remove(m_SongPath);
                     		editPatch1.setText(Integer.toString(0));
                     		Log.d(TAG, "PlayerFragment::UNLINK First: " + songPatch.size());
                	    	linkedPatchNameOne.setText("no patch");
                            btnLinkPatchTwo.setEnabled(false);
                            linkedPatchNameTwo.setEnabled(false);
                	    }
                	});	
                	
                	alert.setPositiveButton("Link", new DialogInterface.OnClickListener() {
                	    public void onClick(DialogInterface dialog, int whichButton) {
                	    	String userPatchName = edittext.getText().toString();
                    		byte [] presetData = activity.patch.getCurrentPresetValues(userPatchName);
                    		
                    		SongPatchData songPatchData = new SongPatchData();
                    		songPatchData.setFirstPatch(new String(presetData));
                    		songPatchData.setFirstPatchName(userPatchName);
                    		
                    		songPatch.put(m_SongPath, songPatchData);
                    		linkedPatchNameOne.setText("P1: " + userPatchName);  
                    		activity.btnPatch.setText(userPatchName);
                    		editPatch1.setText(Integer.toString(0));
                    		
                    		Log.d(TAG, "PlayerFragment::btnLinkPatchOne Click songPatch size" + songPatch.size());
                    		
                    		
                    		// enable second patch if first one is set (SongPatchData object allocated)
                            btnLinkPatchTwo.setEnabled(true);
                            linkedPatchNameTwo.setEnabled(true);
                	    }
                	});
                	
                	alert.show();
            	}            	
                break;
                
            case R.id.btnLinkPatchTwo:
             	if(m_SongPath != null){
                	AlertDialog.Builder alert = new AlertDialog.Builder(activity); 
                	final EditText edittext = new EditText(activity);
                	alert.setMessage("Enter patch name or click 'Unlink' to remove assigned patch from song. Press 'Back' to cancel dialog.");
                	alert.setTitle("Song Preset Linking");
                	
                	
                	String patchBtnText = (String) activity.btnPatch.getText();
                	if(patchBtnText.equals("Load Library"))
                		edittext.setText("");
                	else
                		edittext.setText(patchBtnText);
                	
                	alert.setView(edittext);
                	
                	alert.setNegativeButton("unlink", new DialogInterface.OnClickListener() {
                	    public void onClick(DialogInterface dialog, int whichButton) {
                	    	//songPatch.remove(m_SongPath);
                	    	songPatch.get(m_SongPath).setSecondPatch("");
                	    	songPatch.get(m_SongPath).setSecondPatchTime(0);
                	    	songPatch.get(m_SongPath).setSecondPatchName("");
                	    	editPatch2.setText(Integer.toString(0));
                	    	linkedPatchNameTwo.setText("no patch");
                	    }
                	});	
                	
                	alert.setPositiveButton("Link", new DialogInterface.OnClickListener() {
                	    public void onClick(DialogInterface dialog, int whichButton) {
                	    	String userPatchName = edittext.getText().toString();
                    		byte [] presetData = activity.patch.getCurrentPresetValues(userPatchName);

                    		songPatch.get(m_SongPath).setSecondPatch(new String(presetData));
                    		songPatch.get(m_SongPath).setSecondPatchName(userPatchName);
                    		linkedPatchNameTwo.setText("P2: " + userPatchName); 
                    		editPatch2.setText(Integer.toString(0));
                    		activity.btnPatch.setText(userPatchName);
                	    }
                	});
                	
                	alert.show();
            	}            	
                break;                

            case R.id.btnEQ:
            	if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                	if(ABLayout.getVisibility() == LinearLayout.GONE){
                		ABLayout.setVisibility(LinearLayout.VISIBLE);
                		patchABLayout.setVisibility(LinearLayout.VISIBLE);
                		timerDisplayLayout.setVisibility(LinearLayout.VISIBLE);
                		soundControl.setVisibility(LinearLayout.VISIBLE);
                		player_link.setVisibility(LinearLayout.VISIBLE);
                	}else{
                		ABLayout.setVisibility(LinearLayout.GONE);
                		patchABLayout.setVisibility(LinearLayout.GONE);
                		timerDisplayLayout.setVisibility(LinearLayout.GONE);
                		soundControl.setVisibility(LinearLayout.GONE);
                		player_link.setVisibility(LinearLayout.GONE);
                	}
            	}else{
                	if(ABLayout.getVisibility() == LinearLayout.GONE){
                		ABLayout.setVisibility(LinearLayout.VISIBLE);
                		patchABLayout.setVisibility(LinearLayout.VISIBLE);
                		timerDisplayLayout.setVisibility(LinearLayout.VISIBLE);
                		soundControl.setVisibility(LinearLayout.VISIBLE);
                		songProgressBar.setVisibility(LinearLayout.VISIBLE);
                		player_footer_bg.setVisibility(LinearLayout.VISIBLE);
                	}else{
                		player_footer_bg.setVisibility(LinearLayout.GONE);
                		patchABLayout.setVisibility(LinearLayout.GONE);
                		ABLayout.setVisibility(LinearLayout.GONE);
                		timerDisplayLayout.setVisibility(LinearLayout.GONE);
                		soundControl.setVisibility(LinearLayout.GONE);
                		songProgressBar.setVisibility(LinearLayout.GONE);
                	}
            	}
            	
            	activity.runFragmentHandler(equalizerFragment);
                break;

            case R.id.btnPrevious:
            	mp.seekTo(0);
            	//btnPlay.setImageResource(R.drawable.btn_play);
                break;
                
            case R.id.btnNext:
            	mp.seekTo(mp.getDuration() - 10);
            	//btnPlay.setImageResource(R.drawable.btn_play);
            	break;
            	
            case R.id.btnRepeat:
            	
                if(isRepeat){
                    isRepeat = false;
                    Toast.makeText(activity.getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                }else{
                    // make repeat to true
                    isRepeat = true;
                    Toast.makeText(activity.getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
                } 
                
            	break;
                
            case R.id.btnA:
            	if(mediaPlayerIsPrepared){
            		minValueMilliSeconds = mp.getCurrentPosition();
            		sbLoop.setSelectedMinValue((int)minValueMilliSeconds / 1000);
            	}
                break;            	
            case R.id.btnB:
            	if(mediaPlayerIsPrepared){
            		maxValueMilliSeconds = mp.getCurrentPosition();
            		sbLoop.setSelectedMaxValue((int)maxValueMilliSeconds / 1000);
            	}
                break;  
                
            case R.id.btnPatchA:
            	if(mediaPlayerIsPrepared){
            		if(songPatch.containsKey(m_SongPath)){
            			int time = mp.getCurrentPosition() / 1000;
            			songPatch.get(m_SongPath).setFirstPatchTime(time);
            			editPatch1.setText(Utilities.secondsToTimer(time));
            		}
            	}
                break;            	
            case R.id.btnPatchB:
            	if(mediaPlayerIsPrepared){
            		if(songPatch.containsKey(m_SongPath)){
            			int time = mp.getCurrentPosition() / 1000;
            			songPatch.get(m_SongPath).setSecondPatchTime(time);
            			editPatch2.setText(Utilities.secondsToTimer(time));
            		}            		
            	}
                break;  
                
            case R.id.btnAB:
            	if(mediaPlayerIsPrepared){
            		boolean on = ((ToggleButton) v).isChecked();
            		
            		if(!on){
            			ABOnOff = false; 
            			//btnAB.setText(Html.fromHtml("A/B" +  "<br />" +  "<small><small>" + "Off" + "</small></small>" ));
            			sbLoop.setEnabled(false);
            			//sbLoop.setVisibility(View.GONE);
            			sbLoop.setAlpha(0.3f);
            			btnA.setEnabled(false);
            			btnB.setEnabled(false);
            		}else{
            			ABOnOff = true; 
            			//btnAB.setText(Html.fromHtml("A/B" +  "<br />" +  "<small><small>" + "On" + "</small></small>" ));
            			sbLoop.setEnabled(true);
            			sbLoop.setAlpha(1.0f);
            			//sbLoop.setVisibility(View.VISIBLE);
            			btnA.setEnabled(true);
            			btnB.setEnabled(true);
            		}
            	}
            	break;                
            default:
                break;
        }
		
	}
	
    public void serializeMap(HashMap<String,SongPatchData> hm) {
        try {
            FileOutputStream fStream = activity.openFileOutput("songmap.ser", Context.MODE_PRIVATE) ;
            ObjectOutputStream oStream = new ObjectOutputStream(fStream);

            oStream.writeObject(hm);        
            oStream.flush();
            oStream.close();

            Log.d(TAG, "Serialization success");
        } catch (Exception e) {
        	Log.d(TAG, "serializeMap IO Exception " + e.getMessage());
        	e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
	public void deSerializeMap() {
    	FileInputStream fStream = null;
		try {
			fStream = activity.openFileInput("songmap.ser");
	        try {
	            ObjectInputStream iStream = new ObjectInputStream(fStream);

	            songPatch = (HashMap<String, SongPatchData>)iStream.readObject();   
	            if(songPatch == null){
	            	Log.d(TAG, "songPatch is NULL");
	            	songPatch = new HashMap<String, SongPatchData>();
	            }

	            Log.d(TAG, "Deserialization success ");
	        } catch (Exception e) {
	        	Log.d(TAG, "Deserialization IO Exception " + e.getMessage());
	        	songPatch = new HashMap<String, SongPatchData>();
	        }			
		} catch (FileNotFoundException e1) {
			songPatch = new HashMap<String, SongPatchData>();
			Log.d(TAG, "Deserialization file not found");
		}

    }
    
    public void handleUserHardkeyVolumeChange(){
    	if(knobVolume != null && audioManager != null)
    		knobVolume.setValue(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }
	

 
}
