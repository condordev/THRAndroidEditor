package de.sgrad.yamahathreditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

import de.sgrad.yamahathreditor.SysExCommands.THRModel;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Patch  {
	public static String TAG = "THR";
	// header (18 byte)
	 //F0 43 7D 00 02 0C 44 54 41 31 41 6C 6C 50 00 00 7F 7F
	
	// YDL file starts with:
	// 44 54 41 42 30 31 64 00 00 00 00 00 00 (0 - 0xc) zero based index, ASCII = DTAB0 than 0x64 = 100 num patches??
	// than first patch name starts at pos 0xD (MODERN BACKING...)
	// until and it has 0x105 (261) bytes, now the next patch name starts
	
	final static byte [] MESSAGE_START = new byte [] {(byte)0xf0, 0x43, 0x7d, 0x00};
	/* Seems to be the same for all patches */
	//"DTA1AllP\\x00\\x00\\x7f\\x7f";
	final static byte[] PATCH_MAGIC =  new byte [] {0x44, 0x54, 0x41, 0x31, 0x41, 0x6C, 0x6C, 0x50, 0x00, 0x00, 0x7f, 0x7f } ;

	final static int HEADER_SIZE = 6;   // MESSAGE_START + 2 byte
	final static int DATA_SIZE = 0x10c; // DATA_SIZE = 12 + 128 + 96 + 32 = 268	 (PMAGIC + Name + Data + Tail w/o crc)
	final static int TRAILER_SIZE = 2;
	final static int SYSEX_SIZE = (HEADER_SIZE + DATA_SIZE + TRAILER_SIZE); // = 276

	final static int PATCH_SIZE = 0x100; // PATCH_SIZE= 128 + 96 + 32      = 256  (Name + Data + Tail w/o crc)
	
	private MainActivity activity;
	private File libraryFile;
	private RandomAccessFile fp = null;
	private byte[] sysExDump;
	int pid = 0;
	String currentPatchName = "Patch";
	ListView lv = null;
	
	//HashMap<String, byte[]> namedPatches =  new HashMap<String, byte[]>();
	ArrayList<byte[]> patchList = null;
	
	TextView textViewSelectedPatchName;

	ArrayList<String> listData = null;
	ArrayAdapter<String> listAdapter = null;
	
	public Patch(final MainActivity activity, ListView patchlistview) {
		this.activity = activity;
		lv = patchlistview;
    	patchList = new ArrayList<byte[]>(100);
    	listData = new ArrayList<String>(); 
		listAdapter = new ArrayAdapter<String>(activity, R.layout.patch_list, listData);
		/*{
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                //textView.setTextColor({YourColorHere});
                textView.setTextSize(1.2f);
                return textView;
            }
		};*/
		
		listAdapter.setNotifyOnChange(true);
		lv.setAdapter(listAdapter);
		lv.setLongClickable(true);
		//loadLibraryContent();
		//setupListener();
		
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
            	final int longPressPos = pos;
            	Log.d( TAG, "long press position clicked " + pos);
            	AlertDialog.Builder alert = new AlertDialog.Builder(activity); 
            	
            	final EditText edittext = new EditText(activity);
            	byte[] preset = patchList.get(pos);
            	currentPatchName = bytesToString(preset);
            	
            	alert.setMessage("Enter patch name. Will overwrite " + currentPatchName);
            	alert.setTitle("Store User Preset");
            	
            	edittext.setText(textViewSelectedPatchName.getText());
            	alert.setView(edittext);
            	
            	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            	    public void onClick(DialogInterface dialog, int whichButton) {
            	       
            	    }
            	});

            	alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            	    public void onClick(DialogInterface dialog, int whichButton) {
            	    	String userPatchName = edittext.getText().toString();
            	    	Log.d( TAG, "Entered " + userPatchName);
            	    	
            	    	//byte [] toFile = new byte[PATCH_SIZE + 5]; // 128 (0x80) byte patch name, 96  (0x60) byte patch data, 37  (0x25) byte tail until end = 261
            	    	
            	    	byte [] toFile = Arrays.copyOf(userPatchName.getBytes(), PATCH_SIZE + 5);
            	    	
            	    	// set amp type after patch name block
            	    	toFile[128] = activity.getAmpData().getPatchValue();
            	    	
            	    	byte [] ctrlData = activity.getPatchValues();
            	    	toFile[129] = ctrlData[0];
            	    	toFile[130] = ctrlData[1];
            	    	toFile[131] = ctrlData[2];
            	    	toFile[132] = ctrlData[3];
            	    	toFile[133] = ctrlData[4];
            	    	toFile[134] = activity.getCabinetData().getPatchValue();
            	    	
            	    	byte [] compData = activity.getCompressorData().getPatchValues();
            	    	System.arraycopy(compData, 0, toFile, 144, compData.length); 
            	    	
            	    	byte [] modData = activity.getModulationData().getPatchValues();
            	    	System.arraycopy(modData, 0, toFile, 160, modData.length);
            	    	
            	    	byte [] delayData = activity.getDelayData().getPatchValues();
            	    	System.arraycopy(delayData, 0, toFile, 176, delayData.length);
            	    	
            	    	byte [] reverbData = activity.getReverbData().getPatchValues();
            	    	System.arraycopy(reverbData, 0, toFile, 192, reverbData.length);

            	    	byte [] gateData = activity.getGateData().getPatchValues();
            	    	System.arraycopy(gateData, 0, toFile, 208, gateData.length);
            	    	
            	    	//Log.d( TAG, "ToFile:" + userPatchName + " Data: " + SysExCommands.byteToHex(toFile) + " Lenght: " + toFile.length + " byte(s)");
            	    	
            	    	//libraryFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + "THR" + "/" + "THR5_10.YDL");
            	    	libraryFile = getLibraryFile(activity.model);
            			if (libraryFile.exists()) {
            		    	try {
            					fp = new RandomAccessFile(libraryFile, "rw");
            					Log.d( TAG, "Load library file in 'read/write' mode: " + libraryFile.getAbsolutePath() + " Lenght: " + libraryFile.length() + " byte(s)");
            				} catch (FileNotFoundException e2) {
            					e2.printStackTrace();
            				}
            			}
            			
            			//long pos = 0; 
            			try {
            				fp.seek(0xd + (longPressPos * (PATCH_SIZE + 5)) );
            				fp.write(toFile);
            				Log.d( TAG, "Overwriting: " + listData.get(longPressPos) + " with " + userPatchName + " at pos " + longPressPos);
            				final String str = userPatchName;
            				activity.runOnUiThread(new Runnable() {
            					@Override
            					public void run() {
            						Toast.makeText(activity,  "Overwriting: " + listData.get(longPressPos) + " with " + str + " at pos " + longPressPos, Toast.LENGTH_LONG).show();
            					}
            				});
            				
            				listData.set(longPressPos, userPatchName);
            				listAdapter.notifyDataSetChanged();
            				activity.btnPatch.setText(userPatchName);
            				activity.btnPatch.refreshDrawableState();
            				textViewSelectedPatchName.setText(userPatchName);
            				textViewSelectedPatchName.refreshDrawableState();
            				currentPatchName = userPatchName;
            				
            			} catch (IOException e) {
            				e.printStackTrace();
            			}
            			
            			try {
            				fp.close();
            				libraryFile = null;
            			} catch (IOException e) {
            				e.printStackTrace();
            			}
            			
            			loadLibraryContent();
            			lv.refreshDrawableState();
            	    }
            	});

            	alert.show();

                return true;
            }
        }); 
		
		// listening to single list item click
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final byte[] preset = patchList.get(position);
				currentPatchName = bytesToString(preset);
				
				AlertDialog.Builder alert = new AlertDialog.Builder(activity); 
            	alert.setMessage("Your current settings will be lost. Do you want to load " + currentPatchName + " ?");
            	alert.setTitle("Load Preset");
            	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            	    public void onClick(DialogInterface dialog, int whichButton) {
            	       
            	    }
            	});
            	alert.setPositiveButton("Load", new DialogInterface.OnClickListener() {
            	    public void onClick(DialogInterface dialog, int whichButton) {
        				textViewSelectedPatchName.setText(currentPatchName);
        				activity.btnPatch.setText(currentPatchName);
        				// skip patch name field
        				byte[] payload = Arrays.copyOfRange(preset, 0x80 , preset.length);
        				
        				Log.d( TAG, "Name:" + currentPatchName + " Data: " + SysExCommands.byteToHex(preset) + " Lenght: " + preset.length + " byte(s)");
        				//Log.d( TAG, "Payload:" + SysExCommands.byteToHex(payload) + " Lenght: " + payload.length + " byte(s)");
        				
        				// amp type 
        				activity.getAmpData().setPatchValue(payload[0]);
        				if(activity.amp.initialized)
        					activity.amp.updateUI();
        				
        				// ctrl values 
        				byte[] ctrls = Arrays.copyOfRange(payload, 1 , 6);
        				//Log.d( TAG, "ctrls:" + SysExCommands.byteToHex(ctrls) + " Lenght: " + ctrls.length + " byte(s)");
        				activity.setPatchValues(ctrls);
        				
        				//cabinet type 
        				activity.getCabinetData().setPatchValue(payload[6]);
        				if(activity.cabinets.initialized)
        					activity.cabinets.updateUI();
        				
        				//compressor
        				byte[] compressor = Arrays.copyOfRange(payload, 16 , 32);
        				//Log.d( TAG, "compressor:" + SysExCommands.byteToHex(compressor) + " Lenght: " + compressor.length + " byte(s)");
        				activity.getCompressorData().setPatchValues(compressor);
        				if(activity.compressor.initialized)
        					activity.compressor.updateUI();
        				
        				//modulation
        				byte[] modulation = Arrays.copyOfRange(payload, 32 , 48);
        				//Log.d( TAG, "modulation:" + SysExCommands.byteToHex(modulation) + " Lenght: " + modulation.length + " byte(s)");
        				activity.getModulationData().setPatchValues(modulation);
        				if(activity.modulation.initialized)
        					activity.modulation.updateUI();
        				
        				//delay
        				byte[] delay = Arrays.copyOfRange(payload, 48 , 64);
        				//Log.d( TAG, "delay:" + SysExCommands.byteToHex(delay) + " Lenght: " + delay.length + " byte(s)");
        				activity.getDelayData().setPatchValues(delay);
        				if(activity.delay.initialized)
        					activity.delay.updateUI();
        				
        				//reverb
        				byte[] reverb = Arrays.copyOfRange(payload, 64 , 80);
        				//Log.d( TAG, "reverb:" + SysExCommands.byteToHex(reverb) + " Lenght: " + reverb.length + " byte(s)");
        				activity.getReverbData().setPatchValues(reverb);
        				if(activity.reverb.initialized)
        					activity.reverb.updateUI();
        				
        				
        				//gate
        				byte[] gate = Arrays.copyOfRange(payload, 80 , 96);
        				//Log.d( TAG, "gate:" + SysExCommands.byteToHex(gate) + " Lenght: " + gate.length + " byte(s)");
        				activity.getGateData().setPatchValues(gate);
        				if(activity.gate.initialized)
        					activity.gate.updateUI();
        				
        				// sysexdump 
        				/* Header */
        				sysExDump = new byte[SYSEX_SIZE];
        				
        				System.arraycopy(MESSAGE_START, 0, sysExDump, 0, MESSAGE_START.length);
        				sysExDump[4] = DATA_SIZE >> 7;
        				sysExDump[5] = DATA_SIZE & 0x7f;
        				
        				/* Assemble data section */
        				System.arraycopy(PATCH_MAGIC, 0, sysExDump, HEADER_SIZE, PATCH_MAGIC.length);  // until here sysEx size = 18 byte
        				//Log.d( TAG, "sysExDump: " + SysExCommands.byteToHex( (Arrays.copyOfRange(sysExDump, 0 , HEADER_SIZE +  PATCH_MAGIC.length))) );
        				//Log.d( TAG, "Preset Len: " + preset.length + " PATCH_MAGIC Len: " + PATCH_MAGIC.length + " SYSEX_SIZE: " + SYSEX_SIZE + " DATA_SIZE: " + DATA_SIZE);
        				// SysEx 18 + 257 = 275 (SYSEX_SIZE = 276)
        				System.arraycopy(preset, 0, sysExDump, HEADER_SIZE + PATCH_MAGIC.length, preset.length - 4);
        				
        		        /* Last byte of patch has to be zero. No idea why. */
        				sysExDump[HEADER_SIZE + DATA_SIZE - 1] = 0;

        		        /* Calculate checksum */
        		        int cs = 0;
        		        for (int i = 0; i < DATA_SIZE; i++){
        		            cs += sysExDump[HEADER_SIZE + i];
        		        }
        		        
        		        sysExDump[HEADER_SIZE + DATA_SIZE] = (byte) ((~cs + 1) & 0x7f); // sysExDump[HEADER_SIZE + DATA_SIZE] same as pi += DATA_SIZE;
        		        
        		        /* End sysex */
        		        sysExDump[HEADER_SIZE + DATA_SIZE + 1] = (byte)0xf7;  // write end sign to 275th pos in array (0 .. 275 = 276 byts = SYSEXSize )
        		        
        		        activity.sendSysExToDevice(sysExDump);
        		        
        		       // Log.d( TAG, "sysExDump: " + SysExCommands.byteToHex(sysExDump) + " Lenght: " + sysExDump.length + " byte(s)");
            	    }
            	});
				
            	alert.show();
			}
		});	
	}

	public byte[] getCurrentPresetValues(String userPatchName){
    	byte [] preset = Arrays.copyOf(userPatchName.getBytes(), PATCH_SIZE + 5);
    	// set amp type after patch name block
    	preset[128] = activity.getAmpData().getPatchValue();
    	
    	byte [] ctrlData = activity.getPatchValues();
    	preset[129] = ctrlData[0];
    	preset[130] = ctrlData[1];
    	preset[131] = ctrlData[2];
    	preset[132] = ctrlData[3];
    	preset[133] = ctrlData[4];
    	preset[134] = activity.getCabinetData().getPatchValue();
    	
    	byte [] compData = activity.getCompressorData().getPatchValues();
    	System.arraycopy(compData, 0, preset, 144, compData.length); 
    	
    	byte [] modData = activity.getModulationData().getPatchValues();
    	System.arraycopy(modData, 0, preset, 160, modData.length);
    	
    	byte [] delayData = activity.getDelayData().getPatchValues();
    	System.arraycopy(delayData, 0, preset, 176, delayData.length);
    	
    	byte [] reverbData = activity.getReverbData().getPatchValues();
    	System.arraycopy(reverbData, 0, preset, 192, reverbData.length);

    	byte [] gateData = activity.getGateData().getPatchValues();
    	System.arraycopy(gateData, 0, preset, 208, gateData.length);
        
        return preset;
	}
	
	public byte[] getPresetSysExDump(byte [] preset){
		// sysexdump 
		/* Header */
		sysExDump = new byte[SYSEX_SIZE];
		
		System.arraycopy(MESSAGE_START, 0, sysExDump, 0, MESSAGE_START.length);
		sysExDump[4] = DATA_SIZE >> 7;
		sysExDump[5] = DATA_SIZE & 0x7f;
		
		/* Assemble data section */
		System.arraycopy(PATCH_MAGIC, 0, sysExDump, HEADER_SIZE, PATCH_MAGIC.length);  // until here sysEx size = 18 byte
		//Log.d( TAG, "sysExDump: " + SysExCommands.byteToHex( (Arrays.copyOfRange(sysExDump, 0 , HEADER_SIZE +  PATCH_MAGIC.length))) );
		//Log.d( TAG, "Preset Len: " + preset.length + " PATCH_MAGIC Len: " + PATCH_MAGIC.length + " SYSEX_SIZE: " + SYSEX_SIZE + " DATA_SIZE: " + DATA_SIZE);
		// SysEx 18 + 257 = 275 (SYSEX_SIZE = 276)
		System.arraycopy(preset, 0, sysExDump, HEADER_SIZE + PATCH_MAGIC.length, preset.length - 4);
		
        /* Last byte of patch has to be zero. No idea why. */
		sysExDump[HEADER_SIZE + DATA_SIZE - 1] = 0;

        /* Calculate checksum */
        int cs = 0;
        for (int i = 0; i < DATA_SIZE; i++){
            cs += sysExDump[HEADER_SIZE + i];
        }
        
        sysExDump[HEADER_SIZE + DATA_SIZE] = (byte) ((~cs + 1) & 0x7f); // sysExDump[HEADER_SIZE + DATA_SIZE] same as pi += DATA_SIZE;
        
        /* End sysex */
        sysExDump[HEADER_SIZE + DATA_SIZE + 1] = (byte)0xf7;  // write end sign to 275th pos in array (0 .. 275 = 276 byts = SYSEXSize )
        
        return sysExDump;
	}
	
	public void updatePatchComponentValues(byte [] preset){
		// skip patch name field
		byte[] payload = Arrays.copyOfRange(preset, 0x80 , preset.length);
		
		String name = Patch.bytesToString(preset);
		
		Log.d( TAG, "Name:" + name + " Data: " + SysExCommands.byteToHex(preset) + " Lenght: " + preset.length + " byte(s)");
		//Log.d( TAG, "Payload:" + SysExCommands.byteToHex(payload) + " Lenght: " + payload.length + " byte(s)");
		
		// amp type 
		activity.getAmpData().setPatchValue(payload[0]);
		if(activity.amp.initialized)
			activity.amp.updateUI();
		
		// ctrl values 
		byte[] ctrls = Arrays.copyOfRange(payload, 1 , 6);
		//Log.d( TAG, "ctrls:" + SysExCommands.byteToHex(ctrls) + " Lenght: " + ctrls.length + " byte(s)");
		activity.setPatchValues(ctrls);
		
		//cabinet type 
		activity.getCabinetData().setPatchValue(payload[6]);
		if(activity.cabinets.initialized)
			activity.cabinets.updateUI();
		
		//compressor
		byte[] compressor = Arrays.copyOfRange(payload, 16 , 32);
		//Log.d( TAG, "compressor:" + SysExCommands.byteToHex(compressor) + " Lenght: " + compressor.length + " byte(s)");
		activity.getCompressorData().setPatchValues(compressor);
		if(activity.compressor.initialized)
			activity.compressor.updateUI();
		
		//modulation
		byte[] modulation = Arrays.copyOfRange(payload, 32 , 48);
		//Log.d( TAG, "modulation:" + SysExCommands.byteToHex(modulation) + " Lenght: " + modulation.length + " byte(s)");
		activity.getModulationData().setPatchValues(modulation);
		if(activity.modulation.initialized)
			activity.modulation.updateUI();
		
		//delay
		byte[] delay = Arrays.copyOfRange(payload, 48 , 64);
		//Log.d( TAG, "delay:" + SysExCommands.byteToHex(delay) + " Lenght: " + delay.length + " byte(s)");
		activity.getDelayData().setPatchValues(delay);
		if(activity.delay.initialized)
			activity.delay.updateUI();
		
		//reverb
		byte[] reverb = Arrays.copyOfRange(payload, 64 , 80);
		//Log.d( TAG, "reverb:" + SysExCommands.byteToHex(reverb) + " Lenght: " + reverb.length + " byte(s)");
		activity.getReverbData().setPatchValues(reverb);
		if(activity.reverb.initialized)
			activity.reverb.updateUI();
		
		
		//gate
		byte[] gate = Arrays.copyOfRange(payload, 80 , 96);
		//Log.d( TAG, "gate:" + SysExCommands.byteToHex(gate) + " Lenght: " + gate.length + " byte(s)");
		activity.getGateData().setPatchValues(gate);
		if(activity.gate.initialized)
			activity.gate.updateUI();

	}
	
    public boolean loadLibraryContent() {
    	listAdapter.clear();
    	listData.clear();
    	patchList.clear();
    	
    	textViewSelectedPatchName = (TextView) activity.findViewById(R.id.patchName); 

    	//libraryFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + "THR" + "/" + "THR5_10.YDL");
    	libraryFile = getLibraryFile(activity.model);
    	//Log.d( TAG, "getExternalStoragePublicDirectory " + libraryFile.getAbsolutePath() + " Lenght: " + libraryFile.length());
		if (libraryFile != null && libraryFile.exists() ) {
			Log.d( TAG, "Load library file in 'read' mode: " + libraryFile.getAbsolutePath() + " Lenght: " + libraryFile.length() + " byte(s)");
	    	try {
				fp = new RandomAccessFile(libraryFile, "r");
			} catch (FileNotFoundException e2) {
				e2.printStackTrace();
			}
		}else{
			return false;
		}
		/*
        if (fp.toString().startsWith("DTAB01d")){
        	Log.d( TAG, "ERROR: file %s is not a YDL preset file.");
        }else{
        	Log.d( TAG, "File starts with DTAB01d. OK.");
        }
        */
		
		long pos = 0; 
		try {
			// skip offset until first preset (MODERN...)
			fp.seek(0xd);
			while( (pos = fp.getFilePointer()) <= fp.length() - PATCH_SIZE){
				byte[] rawPatchData = new byte[PATCH_SIZE + 5];
				fp.read(rawPatchData, 0, PATCH_SIZE + 5);
				//namedPatches.put(bytesToString(rawPatchData), rawPatchData);
				//byte[]  = Arrays.copyOfRange(rawPatchData, str.length(), byteArr.length);
				patchList.add(rawPatchData);
				pos += (PATCH_SIZE + 5);
				pid++;
				fp.seek(pos);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			fp.close();
			libraryFile = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(byte[] patch : patchList){
			String patchName = bytesToString(patch);
			listData.add(patchName);
		}
		listAdapter.notifyDataSetChanged();
		return true;
    }
    
	public static String bytesToString(byte[] data) {
	    String dataOut = "";
	    for (int i = 0; i < 48; i++) {  // name filed is 128 but 48 should be enough
	        if ( data[i] >= 32 && data[i] <= 127 && data[i] != 0x00 )
	            dataOut += (char)data[i];
	        else
	        	break;
	    }
	    return dataOut;
	}
	
	public byte[] getBytes (File file){
	    FileInputStream input = null;
	    if (file.exists()) try
	    {
	        input = new FileInputStream (file);
	        int len = (int) file.length();
	        byte[] data = new byte[len];
	        int count, total = 0;
	        while ((count = input.read (data, total, len - total)) > 0) total += count;
	        return data;
	    }
	    catch (Exception ex)
	    {
	        ex.printStackTrace();
	    }
	    finally
	    {
	        if (input != null) try
	        {
	            input.close();
	        }
	        catch (Exception ex)
	        {
	            ex.printStackTrace();
	        }
	    }
	    return null;
	}
	
	public static File getLibraryFile(THRModel model){
		switch (model) {
			case THR10:
				return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + "THR" + "/" + "THR5_10.YDL");
			case THR10C:
				return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + "THR" + "/" + "THR10C.YDL");
			case THR10X:
				return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + "THR" + "/" + "THR10X.YDL");
			case THR5:
				return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + "THR" + "/" + "THR5_10.YDL");
			case THR5A:
				return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + "THR" + "/" + "THR5A.YDL");
			default:
				return null;
		}
	}

}
