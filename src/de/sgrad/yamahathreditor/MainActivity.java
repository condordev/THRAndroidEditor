package de.sgrad.yamahathreditor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

import de.sgrad.yamahathreditor.SysExCommands.Controls;
import de.sgrad.yamahathreditor.SysExCommands.THRModel;
import jp.kshoji.driver.midi.device.MidiInputDevice;
import jp.kshoji.driver.midi.device.MidiOutputDevice;
import jp.kshoji.driver.midi.util.UsbMidiDriver;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.hardware.usb.UsbDevice;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

public class MainActivity extends Activity implements OnGestureListener{
	
//	button1.setText(Html.fromHtml("<b><big>" + "Title" + "</big></b>" +  "<br />" + 
//	        "<small>" + "subtitle" + "</small>" + "<br />"));
	
	public static final String TAG = "THR";
	public static final String REPOSITORY_NAME = "APP_SETTING";
	
    private static final int SWIPE_DISTANCE_THRESHOLD = 300;
    private static final int SWIPE_VELOCITY_THRESHOLD = 700;
    
	String versionName = null;
	final private byte [] sysExMsgEditorStart = new byte [] {(byte)0xf0, 0x43, 0x7d, 0x20, 0x44, 0x54, 0x41, 0x31, 0x41, 0x6c, 0x6c, 0x50, (byte)0xf7};

	// ************  Dropbox  related **********************
	final static private String APP_KEY = "---------------";
	final static private String APP_SECRET = "---------------";
    // You don't need to change these, leave them alone.
    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCESS_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    private static final boolean USE_OAUTH1 = false;
    private DropboxAPI<AndroidAuthSession> mDBApi = null;
    public boolean mLoggedIn;
	// dropbox on patch view
	Button btnDropboxDownload;
	Button btnDropboxUpload;
	Button btnDropboxAuth;
	// ************  END Dropbox  related **********************
	
	ArrayAdapter<UsbDevice> connectedDevicesAdapter;
	private SysExCommands sysExCommands;
	public SysExParser sysExParser;
	private UsbMidiDriver usbMidiDriver;
	private PresetFileManager presetFileManager = null;
	MidiOutputDevice midiOutputDeviceFromSpinner = null;
	
	
	Spinner deviceSpinner;
	Button btnAmps;
	Button btnCabinets;
	Button btnGate;
	Button btnModulation;
	Button btnDelay;
	Button btnReverb;
	Button btnCompressor;
	public Button btnPatch;
	public Button btnSysEx;
	ToggleButton btnLED;
	ToggleButton btnWide;
	Button btnSimulation;
	
    public PlayerFragment playerFragment = null;
	
	KnobView knobGain;
	KnobView knobMaster;
	KnobView knobBass;
	KnobView knobMiddle;
	KnobView knobTreble;
	
	public THRModel model = THRModel.THR10;
	public boolean validModelDetected = false;
	public int cable = 0;
	public ReverbData rd;
	public DelayData dd;
	public ModulationData md;
	public GateData gd;
	public CabinetData cabd;
	public AmpData ad;
	public CompressorData cd;
	public Compressor compressor;
	public Reverb reverb;
	public Delay delay;
	public Cabinets cabinets;
	public Gate gate;
	public Amps amp;
	public Modulation modulation;
	public Patch patch;
	public Automation simulation;
	private ListView patchlistview;
	private ListView presetListView;
	private GestureDetectorCompat gestureDetectorCompat;
	public Sysex sysex;
	
	ViewFlipper viewFlipper = null;
	ArrayAdapter<String> midiEventAdapter;
	ListView midiEventListView;
	LinearLayout ampLayout;
	LinearLayout cabinetLayout;
	LinearLayout gateLayout;
	LinearLayout reverbLayout;
	LinearLayout compressorLayout;
	LinearLayout delayLayout;
	LinearLayout modulationLayout;
	LinearLayout patchLayout;
	LinearLayout simulationLayout;
	LinearLayout messageLayout;
	LinearLayout presetLayout;
	LinearLayout playerLayout;
	LinearLayout sysexLayout;
	
	LinearLayout linearLayoutControls;
	
	File appDirectory = new File( Environment.getExternalStorageDirectory() + "/MyPersonalAppFolder" );
	File logDirectory = new File( appDirectory + "/log" );
	File logFile = new File( logDirectory, "runtime" + System.currentTimeMillis() + ".txt" );
	
	public int value = 0;
	public int btnBackgroundColor = 0;
	private boolean showHint = true;
	private boolean canExit = false;
	
	byte gain = 0;
	byte master = 0;
	byte bass = 0;
	byte middle = 0;
	byte treble = 0;
	
	public byte getGain() {
		return gain;
	}


	public void setGain(byte gain) {
		this.gain = gain;
	}


	public byte getMaster() {
		return master;
	}


	public void setMaster(byte master) {
		this.master = master;
	}


	public byte getBass() {
		return bass;
	}


	public void setBass(byte bass) {
		this.bass = bass;
	}


	public byte getMiddle() {
		return middle;
	}


	public void setMiddle(byte middle) {
		this.middle = middle;
	}


	public byte getTreble() {
		return treble;
	}


	public void setTreble(byte treble) {
		this.treble = treble;
	}
	
	public DropboxAPI<AndroidAuthSession> getDropboxDBApi(){
		return mDBApi;
	}
	
	public MediaPlayer getMediaPlayerReference(){
		return playerFragment.mp;
	}

	
	public boolean inSysExHandlingIsOn;
	
	final Handler midiEventHandler = new Handler(new Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			if (midiEventAdapter != null) {
				midiEventAdapter.add((String)msg.obj);
			}
			// message handled successfully
			return true;
		}
	});
	
    /**
     * Choose device from spinner
     *
     * @return the MidiOutputDevice from spinner
     */
	@Nullable MidiOutputDevice getMidiOutputDeviceFromSpinner() {
		if (deviceSpinner != null && deviceSpinner.getSelectedItemPosition() >= 0 && connectedDevicesAdapter != null && !connectedDevicesAdapter.isEmpty()) {
			UsbDevice device = connectedDevicesAdapter.getItem(deviceSpinner.getSelectedItemPosition());
			if (device != null) {
				Set<MidiOutputDevice> midiOutputDevices = usbMidiDriver.getMidiOutputDevices(device);
				
				if (midiOutputDevices.size() > 0) {
					// returns the first one.
					return (MidiOutputDevice) midiOutputDevices.toArray()[0];
				}
			}
		}
		return null;
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// show version info
		Context context = getApplicationContext(); // or activity.getApplicationContext()
		PackageManager packageManager = context.getPackageManager();
		String packageName = context.getPackageName();
		String build = null;
		
		try {
			ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
			ZipFile zf = new ZipFile(ai.sourceDir);
			ZipEntry ze = zf.getEntry("META-INF/MANIFEST.MF");
			long time = ze.getTime();
			SimpleDateFormat formatter = (SimpleDateFormat) SimpleDateFormat.getInstance();
			formatter.setTimeZone(TimeZone.getTimeZone("gmt"));
			build = formatter.format(new java.util.Date(time));
			zf.close();
		} catch (Exception e) {
		}

		versionName = "not available"; // initialize String
		
		gestureDetectorCompat = new GestureDetectorCompat(this,this);

		try {
		    versionName = packageManager.getPackageInfo(packageName, 0).versionName + ". Build: " + build;
		} catch (PackageManager.NameNotFoundException e) {
		    e.printStackTrace();
		}
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
		        setContentView(R.layout.activity_main);
		        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		  } else {
		        setContentView(R.layout.activity_main);
		        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		
		String path = Environment.getExternalStorageDirectory() + "/MyPersonalAppFolder";
		//songManager.setContentResolver(getContentResolver());
		
		playerFragment = new PlayerFragment();
		
        // create app folder
        if ( !appDirectory.exists() ) {
            appDirectory.mkdir();
        }
		
		if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
		    Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(path , ""));
		}
		
		//deSerializeMap();
		
		sysExCommands = new SysExCommands();
		inSysExHandlingIsOn = true;
		sysExParser = new SysExParser(this);
		

		//mp = new MediaPlayer();
		
		//final ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.myViewFlipper);
		viewFlipper = (ViewFlipper) findViewById(R.id.myViewFlipper);
		midiEventListView = (ListView) findViewById(R.id.midiEventListView);
		ampLayout =	(LinearLayout)findViewById(R.id.amplayout);
		gateLayout = (LinearLayout)findViewById(R.id.gateLayout);
		reverbLayout = (LinearLayout)findViewById(R.id.reverbLayout);
		compressorLayout =	(LinearLayout)findViewById(R.id.compressorLayout);
		modulationLayout =	(LinearLayout)findViewById(R.id.modulationLayout);
		delayLayout = (LinearLayout)findViewById(R.id.delayLayout);
		cabinetLayout =	(LinearLayout)findViewById(R.id.cabinetLayout);
		patchLayout =	(LinearLayout)findViewById(R.id.patchLayout);
		simulationLayout =	(LinearLayout)findViewById(R.id.simulationLayout);
		messageLayout =	(LinearLayout)findViewById(R.id.messagelayout);
		presetLayout =	(LinearLayout)findViewById(R.id.presetLayout);
		linearLayoutControls =	(LinearLayout)findViewById(R.id.linearLayoutControls);
		sysexLayout =	(LinearLayout)findViewById(R.id.sysexLayout);
		
		deviceSpinner = (Spinner) findViewById(R.id.deviceNameSpinner);
		btnAmps = (Button) findViewById(R.id.btnAmps);
		btnReverb = (Button) findViewById(R.id.btnReverb);
		btnDelay = (Button) findViewById(R.id.btnDelay);
		btnCabinets = (Button) findViewById(R.id.btnCabinets);
		btnGate = (Button) findViewById(R.id.btnGate);
		btnModulation = (Button) findViewById(R.id.btnModulation);
		btnCompressor = (Button) findViewById(R.id.btnCompressor);
		btnPatch = (Button) findViewById(R.id.btnPatch);
		btnLED = (ToggleButton) findViewById(R.id.tbtnLED);
		btnWide = (ToggleButton) findViewById(R.id.tbtnWide);
		btnSimulation = (Button) findViewById(R.id.btnSimulation);
		patchlistview = (ListView) findViewById(R.id.patchlistview);
		presetListView = (ListView) findViewById(R.id.presetlistview);
		btnSysEx = (Button) findViewById(R.id.btnSysEx);
		
		btnAmps.setTextSize(16);
		btnReverb.setTextSize(16);
		btnDelay.setTextSize(16);
		btnCabinets.setTextSize(16);
		btnGate.setTextSize(16);
		btnModulation.setTextSize(16);
		btnCompressor.setTextSize(16);
		btnSimulation.setTextSize(16);
		btnSysEx.setTextSize(16);
		
		/*
		Log.d( TAG, "bntTextSize " + btnAmps.getTextSize());
		Log.d( TAG, "bntTextSize " + btnAmps.getTextSize());
		*/
		
		btnDropboxDownload = (Button) findViewById(R.id.btnDropboxDownload);
		btnDropboxUpload = (Button) findViewById(R.id.btnDropboxUpload);
		btnDropboxAuth = (Button) findViewById(R.id.btnDropboxAuth);
		btnDropboxDownload.setVisibility(View.GONE);
		btnDropboxUpload.setVisibility(View.GONE);
		
		rd = new ReverbData();
		dd = new DelayData();
		md = new ModulationData();
		gd = new GateData();
		cabd = new CabinetData();
		ad = new AmpData();
		cd = new CompressorData();
		
		reverb = new Reverb(this);
		delay = new Delay(this);
		cabinets = new Cabinets(this);
		compressor = new Compressor(this);
		gate = new Gate(this);
		amp = new Amps(this);
		modulation = new Modulation(this);
		simulation = new Automation(this);
		sysex = new Sysex(this);
		
        // We create a new AuthSession so that we can use the Dropbox API.
        AndroidAuthSession session = buildSession();
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
		
		patch = new Patch(this, patchlistview);
		presetFileManager = new PresetFileManager(this, presetListView);
		
		connectedDevicesAdapter = new ArrayAdapter<UsbDevice>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, new ArrayList<UsbDevice>());
		deviceSpinner.setAdapter(connectedDevicesAdapter);
		
		btnBackgroundColor = getResources().getColor(android.R.color.background_light);
		
		this.copyFilesFromAsset();
		
		setButtonTextLines();
		
		 usbMidiDriver = new UsbMidiDriver(getApplicationContext()) {
	            @Override
	            public void onDeviceAttached(@NonNull UsbDevice usbDevice) {
	                // deprecated method.
	                // do nothing
	            	Log.d( TAG, "onDeviceAttached " + usbDevice.getDeviceName());
	            }

	            @Override
	            public void onMidiInputDeviceAttached(@NonNull MidiInputDevice midiInputDevice) {
	            	Log.d( TAG, "onMidiInputDeviceAttached " + midiInputDevice.getUsbDevice().getDeviceName());
	            }

	            @Override
	            public void onMidiOutputDeviceAttached(@NonNull final MidiOutputDevice midiOutputDevice) {
	            	Log.d( TAG, "onMidiOutputDeviceAttached " + midiOutputDevice.getUsbDevice().getDeviceName());
	                runOnUiThread(new Runnable() {
	                    @Override
	                    public void run() {
	                        if (connectedDevicesAdapter != null) {
	                            connectedDevicesAdapter.remove(midiOutputDevice.getUsbDevice());
	                            connectedDevicesAdapter.add(midiOutputDevice.getUsbDevice());
	                            connectedDevicesAdapter.notifyDataSetChanged();
	                            midiOutputDeviceFromSpinner = getMidiOutputDeviceFromSpinner();
	                        }
	                        Toast.makeText(MainActivity.this, "USB MIDI Device " + midiOutputDevice.getUsbDevice().getDeviceName() + " has been attached.", Toast.LENGTH_SHORT).show();
	                       // if(midiOutputDeviceFromSpinner != null)
	                       // 	sendSysExToDevice(sysExMsgEditorStart);	
	                    }
	                });
	            }

	            @Override
	            public void onDeviceDetached(@NonNull UsbDevice usbDevice) {
	                // deprecated method.
	                // do nothing
	            	Log.d( TAG, "onDeviceDetached " + usbDevice.getDeviceName());
	            }

	            @Override
	            public void onMidiInputDeviceDetached(@NonNull MidiInputDevice midiInputDevice) {
	            	Log.d( TAG, "onMidiInputDeviceDetached " + midiInputDevice.getUsbDevice().getDeviceName());
	            }

	            @Override
	            public void onMidiOutputDeviceDetached(@NonNull final MidiOutputDevice midiOutputDevice) {
	            	Log.d( TAG, "onMidiInputDeviceDetached " + midiOutputDevice.getUsbDevice().getDeviceName());
	                runOnUiThread(new Runnable() {
	                    @Override
	                    public void run() {
	                        if (connectedDevicesAdapter != null) {
	                            connectedDevicesAdapter.remove(midiOutputDevice.getUsbDevice());
	                            connectedDevicesAdapter.notifyDataSetChanged();
	                        }
	                        validModelDetected = false;
	                        Toast.makeText(MainActivity.this, "USB MIDI Device " + midiOutputDevice.getUsbDevice().getDeviceName() + " has been detached.", Toast.LENGTH_LONG).show();
	                    }
	                });
	            }

				@Override
				public void onMidiActiveSensing(MidiInputDevice arg0, int arg1) {}

				@Override
				public void onMidiCableEvents(MidiInputDevice arg0, int arg1, int arg2, int arg3, int arg4) {}

				@Override
				public void onMidiChannelAftertouch(MidiInputDevice arg0, int arg1, int arg2, int arg3) {}

				@Override
				public void onMidiContinue(MidiInputDevice arg0, int arg1) {}

				@Override
				public void onMidiControlChange(MidiInputDevice arg0, int arg1,int arg2, int arg3, int arg4) {}

				@Override
				public void onMidiMiscellaneousFunctionCodes(MidiInputDevice arg0, int arg1, int arg2, int arg3, int arg4) {}

				@Override
				public void onMidiNoteOff(MidiInputDevice arg0, int arg1, int arg2,int arg3, int arg4) {}

				@Override
				public void onMidiNoteOn(MidiInputDevice arg0, int arg1, int arg2, int arg3, int arg4) {}

				@Override
				public void onMidiPitchWheel(MidiInputDevice arg0, int arg1, int arg2, int arg3) {}

				@Override
				public void onMidiPolyphonicAftertouch(MidiInputDevice arg0,int arg1, int arg2, int arg3, int arg4) {}

				@Override
				public void onMidiProgramChange(MidiInputDevice arg0, int arg1,int arg2, int arg3) {}

				@Override
				public void onMidiReset(MidiInputDevice arg0, int arg1) {}

				@Override
				public void onMidiSingleByte(MidiInputDevice arg0, int arg1,int arg2) {}

				@Override
				public void onMidiSongPositionPointer(MidiInputDevice arg0, int arg1, int arg2) {}

				@Override
				public void onMidiSongSelect(MidiInputDevice arg0, int arg1, int arg2) {}

				@Override
				public void onMidiStart(MidiInputDevice arg0, int arg1) {}

				@Override
				public void onMidiStop(MidiInputDevice arg0, int arg1) {}

				@Override
				public void onMidiTimeCodeQuarterFrame(MidiInputDevice arg0, int arg1, int arg2) {}

				@Override
				public void onMidiTimingClock(MidiInputDevice arg0, int arg1) {}

				@Override
				public void onMidiTuneRequest(MidiInputDevice arg0, int arg1) {}

				@Override
				public void onMidiSystemCommonMessage(MidiInputDevice arg0,int arg1, byte[] arg2) {}
				
	            @Override
	            public void onMidiSystemExclusive(@NonNull final MidiInputDevice sender, int cable, final byte[] systemExclusive) {
	            	// rcv from THR:     0xf0 0x43 0x7d 0x60 0x44 0x54 0x41  (0x30 = THR 5, 0x31 = 10, 0x32 = 10x, 0x33 = 10c)  0xf7
	            	// tx from editor:   0xf0 0x43 0x7d 0x20 0x44 0x54 0x41  0x31 0x41 0x6c 0x6c 0x50 0xf7};
	            	if((systemExclusive.length == 9) && (systemExclusive[1] == 0x43) && (systemExclusive[2] == (byte)0x7d) && 
	            			(systemExclusive[3] == 0x60) && (systemExclusive[4] == 0x44) && (systemExclusive[5] == 0x54) && 
	            			(systemExclusive[6] == 0x41)){
	            		if (false == validModelDetected){
	            			switch (THRModel.fromByte(systemExclusive[7])) {
							case THR10:
								validModelDetected = true;
								model = THRModel.THR10;
								break;
							case THR10C:
								validModelDetected = true;
								model = THRModel.THR10C;
								break;
							case THR10X:
								validModelDetected = true;
								model = THRModel.THR10X;
								break;
							case THR5:
								validModelDetected = true;
								model = THRModel.THR5;
								break;
							case THR5A:
								validModelDetected = true;
								model = THRModel.THR5A;
								break;
							default:
								break;
							}
		            		
		            		if(validModelDetected){
		            			runOnUiThread(new Runnable() {
		            				@Override
		            				public void run() {
		            					Toast.makeText(MainActivity.this, "Model " + model.toString() + " is connected.", Toast.LENGTH_LONG).show();
		            				}
		            			});
		            			
		            			if(model != THRModel.THR10){
		            				amp.changeAmpButtonText(model);
		            				amp.updateUI();
		            				cabinets.changeCabinetButtonText(model);
		            				cabinets.updateUI();
		            			}
		            			/*
		            			if(model == THRModel.THR10C){
		            				sysExMsgEditorStart[7] = 0x33;
		            			}
		            			*/
		            			sendSysExToDevice(sysExMsgEditorStart);	
		            			
		            		}else{
		             			runOnUiThread(new Runnable() {
		            				@Override
		            				public void run() {
		            					Toast.makeText(MainActivity.this, "Could not recognize valid THR model ", Toast.LENGTH_LONG).show();
		            				}
		            			});		            			
		            		}
	            		 }
	            	  } else{
		                runOnUiThread(new Runnable() {
		                    @Override
		                    public void run() {
		                    	midiEventHandler.sendMessage(Message.obtain(midiEventHandler, 0, " FROM: " + SysExCommands.byteToHex(systemExclusive) + " Length: " + systemExclusive.length));
		                    	if(inSysExHandlingIsOn == true){
		                    		try{
		                    			sysExParser.read(systemExclusive);
		                    		}catch (RuntimeException re){
		                    			try {
		                    				PrintStream ps = new PrintStream(logFile);
		                    				re.printStackTrace(ps);
		                    				ps.close();
		                    				midiEventHandler.sendMessage(Message.obtain(midiEventHandler, 0, " Ex: " + Log.getStackTraceString(re)));
		                    			} catch (FileNotFoundException e) {
		                    				e.printStackTrace();
		                    			}
		                    		}
		                    	}
		                    }
		                });
	            	}
	            }
	        };
	        
        usbMidiDriver.open();
		
		midiEventAdapter = new ArrayAdapter<String>(this, R.layout.midi_event, R.id.midiEventDescriptionTextView);
		midiEventListView.setAdapter(midiEventAdapter);
		sysex.midiEventListView2.setAdapter(midiEventAdapter);
		
		//viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(findViewById(R.id.messagelayout)));
		
		btnLED.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
				boolean on = ((ToggleButton) arg0).isChecked();
				if (on){
					sendSysExToDevice(sysExCommands.getLEDOnOffSysEx(true));
				}else{
					sendSysExToDevice(sysExCommands.getLEDOnOffSysEx(false));
				}
			}
		});
		
		btnWide.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				boolean on = ((ToggleButton) arg0).isChecked();
				if (on){
					sendSysExToDevice(sysExCommands.getWideModeOnOffSysEx(true));
				}else{
					sendSysExToDevice(sysExCommands.getWideModeOnOffSysEx(false));
				}
				// TO DO only for testing, remove later
				midiEventAdapter.clear();
			}
		});

		
		btnAmps.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				resetControlButtonColor();
				if (viewFlipper.getCurrentView().equals(ampLayout)){
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(messageLayout));
				}else{
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(ampLayout));
					btnAmps.getBackground().setColorFilter(Color.rgb(14, 116, 127),Mode.DST_OVER);
				}
				viewFlipper.refreshDrawableState();
				viewFlipper.invalidate();
			}
		});
		
		btnGate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				resetControlButtonColor();
				if (viewFlipper.getCurrentView().equals(gateLayout)){
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(messageLayout));
				}else{
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(gateLayout));
					btnGate.getBackground().setColorFilter(Color.rgb(14, 116, 127),Mode.DST_OVER);
				}	
				viewFlipper.refreshDrawableState();
				viewFlipper.invalidate();
			}
		});
		
		btnCompressor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				resetControlButtonColor();
				if (viewFlipper.getCurrentView().equals(compressorLayout)){
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(messageLayout));
				}else{
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(compressorLayout));
					btnCompressor.getBackground().setColorFilter(Color.rgb(14, 116, 127),Mode.DST_OVER);
				}
				viewFlipper.refreshDrawableState();
				viewFlipper.invalidate();
			}
		});
		
		btnDelay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				resetControlButtonColor();
				if (viewFlipper.getCurrentView().equals(delayLayout)){
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(messageLayout));
				}else{
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(delayLayout));
					btnDelay.getBackground().setColorFilter(Color.rgb(14, 116, 127),Mode.DST_OVER);
				}	
				viewFlipper.refreshDrawableState();
				viewFlipper.invalidate();
			}
		});
		
		btnModulation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				resetControlButtonColor();
				if (viewFlipper.getCurrentView().equals(modulationLayout)){
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(messageLayout));
				}else{
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(modulationLayout));
					btnModulation.getBackground().setColorFilter(Color.rgb(14, 116, 127),Mode.DST_OVER);
				}
				viewFlipper.refreshDrawableState();
				viewFlipper.invalidate();
			}
		});
		
		btnReverb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				resetControlButtonColor();
				if (viewFlipper.getCurrentView().equals(reverbLayout)){
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(messageLayout));
				}else{
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(reverbLayout));
					btnReverb.getBackground().setColorFilter(Color.rgb(14, 116, 127),Mode.DST_OVER);
				}
				viewFlipper.refreshDrawableState();
				viewFlipper.invalidate();
			}
		});
		
		btnCabinets.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				resetControlButtonColor();
				if (viewFlipper.getCurrentView().equals(cabinetLayout)){
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(messageLayout));
				}else{
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(cabinetLayout));
					btnCabinets.getBackground().setColorFilter(Color.rgb(14, 116, 127),Mode.DST_OVER);
				}	
				viewFlipper.refreshDrawableState();
				viewFlipper.invalidate();
			}
		});
		
		btnPatch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				resetControlButtonColor();
				if (viewFlipper.getCurrentView().equals(patchLayout)){
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(messageLayout));
				}else{
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(patchLayout));
					btnPatch.getBackground().setColorFilter(Color.rgb(14, 116, 127),Mode.DST_OVER);
					if (!patch.loadLibraryContent()){
            			runOnUiThread(new Runnable() {
            				@Override
            				public void run() {
            					Toast.makeText(MainActivity.this, "Library file not found for " + model.toString() + ". Make sure it exists in sdcard/download/THR folder", Toast.LENGTH_LONG).show();
            				}
            			});
					}
					
					if(showHint){
	         			runOnUiThread(new Runnable() {
	        				@Override
	        				public void run() {
	        					Toast.makeText(MainActivity.this, "Hint: Long press on a slot to store preset", Toast.LENGTH_LONG).show();
	        				}
	        			});	
	         			showHint = false;
					}
				}	
				viewFlipper.refreshDrawableState();
				viewFlipper.invalidate();
			}
		});
		
		btnPatch.setOnLongClickListener(new OnLongClickListener() { 
	        @Override
	        public boolean onLongClick(View v) {
				resetControlButtonColor();
				if (viewFlipper.getCurrentView().equals(presetLayout)){
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(messageLayout));
				}else{
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(presetLayout));
					btnPatch.getBackground().setColorFilter(Color.rgb(14, 116, 127),Mode.DST_OVER);
				}	
				viewFlipper.refreshDrawableState();
				viewFlipper.invalidate();	        	
	            return true;
	        }
	    });
		
		btnSysEx.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				resetControlButtonColor();
				if (viewFlipper.getCurrentView().equals(sysexLayout)){
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(messageLayout));
				}else{
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(sysexLayout));
					btnSysEx.getBackground().setColorFilter(Color.rgb(14, 116, 127),Mode.DST_OVER);
				}	
				viewFlipper.refreshDrawableState();
				viewFlipper.invalidate();
			}
	    });
		
		btnSimulation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				resetControlButtonColor();
				runFragmentHandler(playerFragment);
				viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(messageLayout));
				/*
				resetControlButtonColor();
				if (viewFlipper.getCurrentView().equals(simulationLayout)){
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(messageLayout));
				}else{
					viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(simulationLayout));
					btnSimulation.getBackground().setColorFilter(Color.rgb(14, 116, 127),Mode.DST_OVER);
					//patch.loadLibraryContent();
				}	
				viewFlipper.refreshDrawableState();
				viewFlipper.invalidate();
				*/
			}
		});
		
		
		btnDropboxDownload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showConformationDialogDropboxDownload();
			}
		});
		
		btnDropboxUpload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showConfirmationDialogDropboxUpload();
			}
		});
		
		btnDropboxAuth.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				/*
				AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
				AndroidAuthSession session = new AndroidAuthSession(appKeys);
				mDBApi = new DropboxAPI<AndroidAuthSession>(session);
				
				mDBApi.getSession().startOAuth2Authentication(MainActivity.this);
				*/
                // This logs you out if you're logged in, or vice versa
                if (mLoggedIn) {
                    logOut();
                } else {
                    // Start the remote authentication
                    if (USE_OAUTH1) {
                        mDBApi.getSession().startAuthentication(MainActivity.this);
                    } else {
                        mDBApi.getSession().startOAuth2Authentication(MainActivity.this);
                    }
                    showToast("Starting dropbox authentication");
                }
			}
		});
		
		knobGain = (KnobView) this.findViewById(R.id.knobGain);
		knobGain.setMin(0.0);
		knobGain.setMax(100.0);
		knobGain.setValue(0.0);
		knobGain.setAdditionalText("Gain ");
		knobGain.setKnobListener(new KnobListener() {
			byte gainMemory = 0;
			public void onKnobChanged(double newValue) {
				//Log.d(TAG,"Knob Value" + newValue + " Byte: " + (byte)newValue);  
				if(gainMemory == (byte)newValue){
				}else{
					gain = (byte)newValue;
					sendSysExToDevice(sysExCommands.getControlSysEx(Controls.GAIN,(byte)newValue));	
				}
				gainMemory = (byte)newValue;
			}
	    });
		
		knobMaster = (KnobView) this.findViewById(R.id.knobMaster);
		knobMaster.setMin(0.0);
		knobMaster.setMax(100.0);
		knobMaster.setValue(0.0);
		knobMaster.setAdditionalText("Master ");
		knobMaster.setKnobListener(new KnobListener() {
			byte masterMemory = 0;
			public void onKnobChanged(double newValue) {
				//Log.d(TAG,"Knob Value" + newValue + " Byte: " + (byte)newValue);  
				if(masterMemory == (byte)newValue){
				}else{
					master = (byte)newValue;
					sendSysExToDevice(sysExCommands.getControlSysEx(Controls.MASTER,(byte)newValue));				
				}
				masterMemory = (byte)newValue;
			}
	    });
		
		knobBass = (KnobView) this.findViewById(R.id.knobBass);
		knobBass.setMin(0.0);
		knobBass.setMax(100.0);
		knobBass.setValue(0.0);
		knobBass.setAdditionalText("Bass ");
		knobBass.setKnobListener(new KnobListener() {
			byte bassMemory = 0;
			public void onKnobChanged(double newValue) {
				//Log.d(TAG,"Knob Value" + newValue + " Byte: " + (byte)newValue);  
				if(bassMemory == (byte)newValue){
				}else{
					bass = (byte)newValue;
					sendSysExToDevice(sysExCommands.getControlSysEx(Controls.BASS,(byte)newValue));	
				}
				bassMemory = (byte)newValue;
			}
	    });
		
		knobMiddle = (KnobView) this.findViewById(R.id.knobMiddle);
		knobMiddle.setMin(0.0);
		knobMiddle.setMax(100.0);
		knobMiddle.setValue(0.1);
		knobMiddle.setAdditionalText("Middle ");
		knobMiddle.setKnobListener(new KnobListener() {
			byte middleMemory = 0;
			public void onKnobChanged(double newValue) {
				//Log.d(TAG,"Knob Value" + newValue + " Byte: " + (byte)newValue);  
				if(middleMemory == (byte)newValue){
				}else{
					middle = (byte)newValue;
					sendSysExToDevice(sysExCommands.getControlSysEx(Controls.MIDDLE,(byte)newValue));	
				}
				middleMemory = (byte)newValue;
			}
	    });
		
		knobTreble = (KnobView) this.findViewById(R.id.knobTreble);
		knobTreble.setMin(0.0);
		knobTreble.setMax(100.0);
		knobTreble.setValue(0.0);
		knobTreble.setAdditionalText("Treble ");
		knobTreble.setKnobListener(new KnobListener() {
			byte trebleMemory = 0;
			public void onKnobChanged(double newValue) {
				//Log.d(TAG,"Knob Value" + newValue + " Byte: " + (byte)newValue);  
				if(trebleMemory == (byte)newValue){
				}else{
					treble = (byte)newValue;
					sendSysExToDevice(sysExCommands.getControlSysEx(Controls.TREBLE,(byte)newValue));	
				}
				trebleMemory = (byte)newValue;
			}
	    });

	}
	
	private void showConformationDialogDropboxDownload(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setMessage("This will download your remote patches from Dropbox and overwrite your current patch list. Are you sure?");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
                DropboxDownloadTask download = new DropboxDownloadTask(MainActivity.this);
                download.execute();	
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();		
	}
	
	private void showConfirmationDialogDropboxUpload(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setMessage("This will upload your custom patches to Dropbox and overwrite your remote patches. Are you sure?");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
                DropboxUploadTask upload = new DropboxUploadTask(MainActivity.this);
                upload.execute();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();		
	}
	
	public ReverbData getReverbData(){
		return rd;
	}
	
	public DelayData getDelayData(){
		return dd;
	}
	
	public ModulationData getModulationData(){
		return md;
	}
	
	public GateData getGateData(){
		return gd;
	}
	
	public CabinetData getCabinetData(){
		return cabd;
	}
	
	public AmpData getAmpData(){
		return ad;
	}
	
	public CompressorData getCompressorData(){
		return cd;
	}
	
	public byte[] getPatchValues(){
		byte [] ctrlValues = new byte [] {0x00, 0x0, 0x00, 0x00, 0x00};
		ctrlValues[0] = gain;
		ctrlValues[1] = master;
		ctrlValues[2] = bass;
		ctrlValues[3] = middle;
		ctrlValues[4] = treble;
		return ctrlValues;
	}
	
	public void setPatchValues(final byte[] ctrlValues){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
        		gain = ctrlValues[0];
        		master = ctrlValues[1];
        		bass = ctrlValues[2];
        		middle = ctrlValues[3];
        		treble = ctrlValues[4];
        		
        		/*
        		knobGain.setValue(gain);
        		knobMaster.setValue(master);
        		knobBass.setValue(bass);
        		knobMiddle.setValue(middle);
        		knobTreble.setValue(treble);
        		*/
        		refreshContolUI();
            }
        });
	}
	
	public void refreshContolUI(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
        		knobGain.setValue(gain);
        		knobMaster.setValue(master);
        		knobBass.setValue(bass);
        		knobMiddle.setValue(middle);
        		knobTreble.setValue(treble);
            }
        });		
	}
	
	public void onDataChangeUI(byte [] sysEx){
		//if(sendSysExToDevice)
			sendSysExToDevice(sysEx);
	}
	
	public void sendSysExToDevice(final byte [] sysEx){
		final MidiOutputDevice midiOutputDevice = getMidiOutputDeviceFromSpinner();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
        		if(midiOutputDevice == null){
        			//Log.d(TAG, "Cabinet midiOutputDevice invalid");
        			// only for testing
        			midiEventHandler.sendMessage(Message.obtain(midiEventHandler, 0, "NoN TO: " + SysExCommands.byteToHex(sysEx)));			
        		}else{
        			midiOutputDevice.sendMidiSystemExclusive(cable, sysEx);
        			midiEventHandler.sendMessage(Message.obtain(midiEventHandler, 0, "TO: " + SysExCommands.byteToHex(sysEx)));			
        		}
            }
        });
	}
	
	public void onSysExControlChange(final SysExCommands.Controls control, final int value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
        		switch(control){
	    			case GAIN:{
	    				gain = (byte) value;
	    				knobGain.setValue(value);
	    				break;
	    			}
	    			case MASTER:{
	    				master = (byte) value;
	    				knobMaster.setValue(value);
	    				break;
	    			}
	    			case BASS:{
	    				bass = (byte) value;
	    				knobBass.setValue(value);
	    				break;
	    			}
	    			case MIDDLE:{
	    				middle = (byte) value;
	    				knobMiddle.setValue(value);
	    				break;
	    			}
	    			case TREBLE:{
	    				treble = (byte) value;
	    				knobTreble.setValue(value);
	    				break;
	    			}
	    			default:
	    				break;
        		}            	
            }
        });
	}
	
	public void copyFilesFromAsset(){
		 File outDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
		 File dir = new File(outDir + "/" + "THR");
		 // copy only once if app is started first time
		 if (!dir.exists()){
			 copyAssets("THR", outDir.toString());
		 }else{
			 Log.d(TAG, "Directory " + dir.getAbsolutePath() + " already exists");
		 }
	}
	
	
	// src/main/assets add folder with name fold
	// File outDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
	// copyAssets("fold",outDir.toString());
	private void copyAssets(String path, String outPath) {
	    AssetManager assetManager = this.getAssets();
	    String assets[];
	    try {
	        assets = assetManager.list(path);
	       // Log.d(TAG, "copyAssets() Num assets: " + assets.length);
	        if (assets.length == 0) {
	            copyFile(path, outPath);
	        } else {
	            String fullPath = outPath + "/" + path;
	            File dir = new File(fullPath);
	            if (!dir.exists())
	                if (!dir.mkdir()) Log.e(TAG, "No create external directory: " + dir );
	            for (String asset : assets) {
	            	Log.d(TAG, "copyAssets() recursive " + asset);
	                copyAssets(path + "/" + asset, outPath);
	            }
	        }
	    } catch (IOException ex) {
	        Log.e(TAG, "I/O Exception", ex);
	    }
	}

	private void copyFile(String filename, String outPath) {
	    AssetManager assetManager = this.getAssets();

	    InputStream in;
	    OutputStream out;
	    try {
	        in = assetManager.open(filename);
	        String newFileName = outPath + "/" + filename;
	        out = new FileOutputStream(newFileName);

	        byte[] buffer = new byte[1024];
	        int read;
	        while ((read = in.read(buffer)) != -1) {
	            out.write(buffer, 0, read);
	        }
	        in.close();
	        out.flush();
	        out.close();
	    } catch (Exception e) {
	        Log.e(TAG, e.getMessage());
	    }

	}
	
   /* Checks if external storage is available for read and write */
   public boolean isExternalStorageWritable() {
       String state = Environment.getExternalStorageState();
       if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
           return true;
       }
       return false;
   }

   /* Checks if external storage is available to at least read */
   public boolean isExternalStorageReadable() {
       String state = Environment.getExternalStorageState();
       if ( Environment.MEDIA_MOUNTED.equals( state ) ||
               Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {
           return true;
       }
       return false;
   }
   
   public void resetControlButtonColor(){
       btnCabinets.getBackground().setColorFilter(btnBackgroundColor,Mode.MULTIPLY);
       btnReverb.getBackground().setColorFilter(btnBackgroundColor,Mode.MULTIPLY);
       btnDelay.getBackground().setColorFilter(btnBackgroundColor,Mode.MULTIPLY);
       btnCompressor.getBackground().setColorFilter(btnBackgroundColor,Mode.MULTIPLY);
       btnGate.getBackground().setColorFilter(btnBackgroundColor,Mode.MULTIPLY);
       btnModulation.getBackground().setColorFilter(btnBackgroundColor,Mode.MULTIPLY);
       btnAmps.getBackground().setColorFilter(btnBackgroundColor,Mode.MULTIPLY);
       btnPatch.getBackground().setColorFilter(btnBackgroundColor,Mode.MULTIPLY);
       btnSimulation.getBackground().setColorFilter(btnBackgroundColor,Mode.MULTIPLY);
   }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
        usbMidiDriver.close();
       // mp.release();
       // mp = null;
	}
	
	// Within the activity which receives these changes
	// Checks the current device orientation, and toasts accordingly
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
		    LayoutInflater inflater = (LayoutInflater) this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		    inflater.inflate(R.layout.activity_main, null);
		    Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
		    LayoutInflater inflater = (LayoutInflater) this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		    inflater.inflate(R.layout.activity_main, null);
		    Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
		}

	}
	
	protected void onResume() {
	    super.onResume();

	    if(mDBApi != null){
		    AndroidAuthSession session = mDBApi.getSession();
	        // The next part must be inserted in the onResume() method of the
	        // activity from which session.startAuthentication() was called, so
	        // that Dropbox authentication completes properly.
	        if (session.authenticationSuccessful()) {
	            try {
	                // Mandatory call to complete the auth
	                session.finishAuthentication();
	
	                // Store it locally in our app for later use
	                storeAuth(session);
	                setLoggedIn(true);
	            } catch (IllegalStateException e) {
	                showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
	                Log.d(TAG, "Error authenticating", e);
	            }
	        }
	    }
	}
	
	@Override
	public void onBackPressed() {
	    if(canExit){
	        super.onBackPressed();
	        finish();
	    }else{
	    	
	    	if(playerFragment.isVisible()){
	    		runFragmentHandler(playerFragment);
	    	}else if(playerFragment.getEqualizerFragmentReference() != null && playerFragment.getEqualizerFragmentReference().isVisible()){
	    		runFragmentHandler(playerFragment.getEqualizerFragmentReference());
	    	}
	    	
	        canExit = true;
			resetControlButtonColor();
	    	viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(messageLayout));
	        Toast.makeText(getApplicationContext(), "Press 'back' again to exit app", Toast.LENGTH_SHORT).show();
	    }
	    // if not pressed within 2seconds then will be setted(canExit) as false
	    mHandler.sendEmptyMessageDelayed(1, 2000/*time interval to next press in milli second*/);
	}


	public Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case 1:
                canExit = false;
                break;
            default:
                break;
            }
        }
    };
	
	
	/*********************** DROPBOX Related *******************************/
	
    /**
     * Convenience function to change UI state based on being logged in
     */
    private void setLoggedIn(boolean loggedIn) {
    	mLoggedIn = loggedIn;
    	if (loggedIn) {
    		btnDropboxAuth.setText("Logout");
    		btnDropboxDownload.setVisibility(View.VISIBLE);
    		btnDropboxUpload.setVisibility(View.VISIBLE);
    	} else {
    		btnDropboxAuth.setText("Login");
    		btnDropboxDownload.setVisibility(View.GONE);
    		btnDropboxUpload.setVisibility(View.GONE);
    		
    	}
    }


    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    private void loadAuth(AndroidAuthSession session) {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;

        if (key.equals("oauth2:")) {
            // If the key is set to "oauth2:", then we can assume the token is for OAuth 2.
            session.setOAuth2AccessToken(secret);
            setLoggedIn(true);
        } else {
            // Still support using old OAuth 1 tokens.
            session.setAccessTokenPair(new AccessTokenPair(key, secret));
        }
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    private void storeAuth(AndroidAuthSession session) {
        // Store the OAuth 2 access token, if there is one.
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.commit();
            return;
        }
        // Store the OAuth 1 access token, if there is one.  This is only necessary if
        // you're still using OAuth 1.
        AccessTokenPair oauth1AccessToken = session.getAccessTokenPair();
        if (oauth1AccessToken != null) {
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, oauth1AccessToken.key);
            edit.putString(ACCESS_SECRET_NAME, oauth1AccessToken.secret);
            edit.commit();
            return;
        }
    }

    private void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }
    
    private void logOut() {
    	Log.d(TAG, "Dropbox logout performed");
    	showToast("Dropbox logout performed");
    	
        // Remove credentials from the session
        mDBApi.getSession().unlink();

        // Clear our stored keys
        clearKeys();
        // Change UI state to display logged out version
        setLoggedIn(false);
    }
    
	public void runFragmentHandler(Fragment fragment)  {
		FragmentManager man = this.getFragmentManager();
		FragmentTransaction ft = man.beginTransaction();
		
		//ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
		
		//prevent unintended control knob changes if swipe
		if(fragment instanceof PlayerFragment){
			if(!fragment.isVisible()){
				linearLayoutControls.setVisibility(LinearLayout.GONE);
			}else{
				linearLayoutControls.setVisibility(LinearLayout.VISIBLE);
			}
		}
		
	    if (!fragment.isVisible()) {
	        ft.show(fragment);
	    } else {
	        ft.hide(fragment);
	    }
	    
	    
	    if(!fragment.isAdded())
	    	if(fragment instanceof GraphicsEqualizer){
	    		ft.add(de.sgrad.yamahathreditor.R.id.fragment_eq_container, fragment);
	    	}else{
	    		ft.add(de.sgrad.yamahathreditor.R.id.fragment_container, fragment);
	    	}
	    
	    ft.commit();
	}


	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (viewFlipper.getCurrentView().equals(messageLayout) || playerFragment.isVisible()){
	        float distanceX = e2.getX() - e1.getX();
	        float distanceY = e2.getY() - e1.getY();
	        if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
	            if (distanceX > 0){
	            	// swipe right
	            	if(playerFragment.isVisible()){
	            		runFragmentHandler(playerFragment);
	            	}
	            }else{
	            	// swipe left
	            	/*
	            	if(playerFragment.isVisible()){
	            		linearLayoutControls.setVisibility(LinearLayout.VISIBLE);
	            	}else{
	            		linearLayoutControls.setVisibility(LinearLayout.GONE);
	            	}
	            	*/
	            	runFragmentHandler(playerFragment);
	            }
	            return true;
	        }
		}
        return false;
	}
	
	@Override
    public boolean dispatchTouchEvent(MotionEvent ev){
		this.gestureDetectorCompat.onTouchEvent(ev);
            return super.dispatchTouchEvent(ev);   
    }
	
	public void setButtonTextLines(){
		
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			btnAmps.setText(Html.fromHtml("Amp" +  "<br />" +  "<small>" + "Acoustic" + "</small>" ));
			btnCabinets.setText(Html.fromHtml("Cabinet" +  "<br />" +  "<small>" + "None" + "</small>"));
			btnGate.setText(Html.fromHtml("Gate" +  "<br />" +  "<small>" + "Off" + "</small>"));
			btnModulation.setText(Html.fromHtml("Modulation" +  "<br />" +  "<small>" + "Chorus Off" + "</small>"));
			btnCompressor.setText(Html.fromHtml("Compressor" +  "<br />" +  "<small>" + "Stomp Off" + "</small>"));
			btnReverb.setText(Html.fromHtml("Reverb" +  "<br />" +  "<small>" + "Hall Off" + "</small>"));
			btnDelay.setText(Html.fromHtml("Delay" +  "<br />" +  "<small>" + "Off" + "</small>"));
			btnSimulation.setText(Html.fromHtml("Player" +  "<br />" +  "<small>" + "Idle" + "</small>"));			
		}else{
			btnAmps.setText(Html.fromHtml("Amp " +  "<br />" +  "<small><small>" + "Acoustic" + "</small></small>" ));
			btnCabinets.setText(Html.fromHtml("Cab " +  "<br />" +  "<small><small>" + "None" + "</small></small>"));
			btnGate.setText(Html.fromHtml("Gate" +  "<br />" +  "<small><small>" + "Off" + "</small></small>"));
			btnModulation.setText(Html.fromHtml("Mod " +  "<br />" +  "<small><small>" + "Chorus Off" + "</small></small>"));
			btnCompressor.setText(Html.fromHtml("Comp" +  "<br />" +  "<small><small>" + "Stomp Off" + "</small></small>"));
			btnReverb.setText(Html.fromHtml("Rev " +  "<br />" +  "<small><small>" + "Hall Off" + "</small></small>"));
			btnDelay.setText(Html.fromHtml("Dly " +  "<br />" +  "<small><small>" + "Off" + "</small></small>"));
			btnSimulation.setText(Html.fromHtml("Play" +  "<br />" +  "<small><small>" + "Idle" + "</small></small>"));				
		}
	}
	
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) 
    {
        if (event.getAction() == KeyEvent.ACTION_DOWN){
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                	playerFragment.handleUserHardkeyVolumeChange();
                    return false;
            }
        }
        return super.dispatchKeyEvent(event);
    }
	
    
    /*
    @SuppressWarnings("unchecked")
	public void LoadSettings(Context context) {
        SharedPreferences pref = context.getSharedPreferences(REPOSITORY_NAME, Context.MODE_PRIVATE);
        songPatch = (HashMap<String, byte[]>) pref.getAll();
        if(songPatch == null) {
        	songPatch = new HashMap<String, byte[]>();
        }
    }
    
    public void SaveSettings(Context context) {
        if(songPatch == null) {
        	songPatch = new HashMap<String, byte[]>();
        }

        //persist
        SharedPreferences pref = context.getSharedPreferences(REPOSITORY_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        for (String s : songPatch.keySet()) {
            editor.put(s, songPatch.get(s));
        }
        editor.commit();
    }
    */
}


