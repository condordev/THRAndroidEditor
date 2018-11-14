package de.sgrad.yamahathreditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.opengl.Visibility;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class PresetFileManager implements OnItemSelectedListener {

	public static final String TAG = "THR";
    public File sdcardTHRFolderDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + "THR" );
    private final  byte [] MESSAGE_START_PROGRAMM = new byte [] {0x44, 0x54, 0x41, 0x50, 0x00,0x02, 0x00, 0x00, 0x00};
    private final int OFFSET = 9;
	public ArrayList<String> listData = null;
	List<File> inFiles = null;
	ArrayAdapter<String> listAdapter = null;
	//byte[] presetData = null;
	ArrayList<byte[]> patchList = null;
	//File libraryFile = null;
	private RandomAccessFile fp = null;
	
    private MainActivity activity = null;
    private ListView lv = null;
    private Button btnOpen = null;
    private Button btnOpenDropbox = null;
    private Button btnSave = null;
    public TextView tvPresetName = null;
    private Spinner spinnerPresetFilter = null;
    private boolean libList = false;
    public boolean dropboxList = false;
    private String ydpFileName = "userPatch1";
    private File ydpFile = null;
    public String presetFilter = "all"; 

    public PresetFileManager(final MainActivity activity, ListView presetlistview){
    	this.activity = activity;
    	lv = presetlistview;
    	listData = new ArrayList<String>(); 
		listAdapter = new ArrayAdapter<String>(activity, R.layout.patch_list, listData);
		listAdapter.setNotifyOnChange(true);
		lv.setAdapter(listAdapter);
		
		patchList = new ArrayList<byte[]>(100);
		
		btnOpen = (Button) activity.findViewById(R.id.btnOpen);
		btnOpenDropbox = (Button) activity.findViewById(R.id.btnOpenDropbox);
		btnSave = (Button) activity.findViewById(R.id.btnSave);
		spinnerPresetFilter = (Spinner) activity.findViewById(R.id.spinnerPresetFilter);
		spinnerPresetFilter.setOnItemSelectedListener(this);
		//btnSave.setVisibility(View.GONE);
		
		tvPresetName = (TextView) activity.findViewById(R.id.presetName);
		tvPresetName.setText("no selection");
		
		btnOpen.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				updateSDCardFileList();
			}
		});
		
		btnOpenDropbox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				listData.clear();
				libList = false;
				dropboxList = false;
				patchList.clear();
				listAdapter.clear();
				DropboxGetFileListTask downloadList = new DropboxGetFileListTask(activity, PresetFileManager.this);
                downloadList.execute();						
				//listAdapter.notifyDataSetChanged();
			}
		});
		
		btnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				saveProgram();
			}
		}); 
		
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(dropboxList){
					String fileName = listData.get(position);
					DropboxDownloadArbitraryFile downloadFile = new DropboxDownloadArbitraryFile(activity, fileName, PresetFileManager.this);
					downloadFile.execute();	
				}else if(libList){
					ydpFileName = decodePresetData(patchList.get(position), true);
				}else{
					final File file = inFiles.get(position);
					Log.d( TAG, "onItemClick " + file.getAbsolutePath() + " Lenght: " + file.getName());
			        if (file.getName().endsWith(".YDP") && file.length() == 265){
			        	byte[] presetData = getBytes(inFiles.get(position));
			        	decodePresetData(presetData, false);
			        	ydpFileName = file.getName();
			        	libList = false;
			        }else if(file.getName().endsWith(".YDL") && file.length() == 0x65FC){
			        	patchList.clear();
			        	File libraryFile = inFiles.get(position);
			        	if(loadLibraryContent(libraryFile)){
			        		libList = true;
			        		tvPresetName.setText("Library File List:");
			        	}
			        }else{
		     			activity.runOnUiThread(new Runnable() {
		    				@Override
		    				public void run() {
		    					Toast.makeText(activity, "ERROR: " + file.getName() + " is not a valid YDP or YDL preset file.", Toast.LENGTH_LONG).show();
		    				}
		    			});	
			        	Log.d( TAG, "ERROR: File " + file.getName() + " is not a valid YDP or YDL preset file.");
			        	libList = false;
			        }
				}
			}
		});	
    }
    
	private void updateSDCardFileList(){
		listData.clear();
		libList = false;
		dropboxList = false;
		patchList.clear();
		listAdapter.clear();
		inFiles = getListFiles(sdcardTHRFolderDir);
		for(File file : inFiles){
			listData.add(file.getName());
		}
		listAdapter.notifyDataSetChanged();	
		tvPresetName.setText("Content of SDCard THR folder:");
	}
    
    private List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
            	if(presetFilter.equals("all")){
            		if(file.getName().endsWith(".YDP") || file.getName().endsWith(".YDL")){
            			inFiles.add(file);
            		}
            	}else if(presetFilter.equals("program")){
            		if(file.getName().endsWith(".YDP")){
            			inFiles.add(file);
            		}
            	}else if(presetFilter.equals("library")){
            		if(file.getName().endsWith(".YDL")){
            			inFiles.add(file);
            		}
            	}
            }
        }
        return inFiles;
    }
    
    public String decodePresetData(byte[] presetData, boolean isLibrary){
    	byte [] preset = null;
    	String presetName = null;
    	byte[] payload = null;
    	
    	if(isLibrary){
    		preset = presetData;
    	}else{
    		preset = Arrays.copyOfRange(presetData, 9 , presetData.length);
    	}
    	
    	presetName = Patch.bytesToString(preset);
    	tvPresetName.setText(presetName);
    	activity.btnPatch.setText(presetName);
    	payload = Arrays.copyOfRange(preset, 0x80 , preset.length);
    	
    	Log.d( TAG, "Name:" + presetName + " Data: " + SysExCommands.byteToHex(preset) + " Lenght: " + preset.length + " byte(s)");
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
		byte [] sysExDump = new byte[Patch.SYSEX_SIZE];
		
		System.arraycopy(Patch.MESSAGE_START, 0, sysExDump, 0, Patch.MESSAGE_START.length);
		sysExDump[4] = Patch.DATA_SIZE >> 7;
		sysExDump[5] = Patch.DATA_SIZE & 0x7f;
		
		/* Assemble data section */
		System.arraycopy(Patch.PATCH_MAGIC, 0, sysExDump, Patch.HEADER_SIZE, Patch.PATCH_MAGIC.length);  // until here sysEx size = 18 byte
		//Log.d( TAG, "sysExDump: " + SysExCommands.byteToHex( (Arrays.copyOfRange(sysExDump, 0 , HEADER_SIZE +  PATCH_MAGIC.length))) );
		//Log.d( TAG, "Preset Len: " + preset.length + " PATCH_MAGIC Len: " + PATCH_MAGIC.length + " SYSEX_SIZE: " + SYSEX_SIZE + " DATA_SIZE: " + DATA_SIZE);
		// SysEx 18 + 257 = 275 (SYSEX_SIZE = 276)
		System.arraycopy(preset, 0, sysExDump, Patch.HEADER_SIZE + Patch.PATCH_MAGIC.length, preset.length - 4);
		
        /* Last byte of patch has to be zero. No idea why. */
		sysExDump[Patch.HEADER_SIZE + Patch.DATA_SIZE - 1] = 0;

        /* Calculate checksum */
        int cs = 0;
        for (int i = 0; i < Patch.DATA_SIZE; i++){
            cs += sysExDump[Patch.HEADER_SIZE + i];
        }
        
        sysExDump[Patch.HEADER_SIZE + Patch.DATA_SIZE] = (byte) ((~cs + 1) & 0x7f); // sysExDump[HEADER_SIZE + DATA_SIZE] same as pi += DATA_SIZE;
        
        /* End sysex */
        sysExDump[Patch.HEADER_SIZE + Patch.DATA_SIZE + 1] = (byte)0xf7;  // write end sign to 275th pos in array (0 .. 275 = 276 byts = SYSEXSize )
        
        activity.sendSysExToDevice(sysExDump);
        
        return presetName;
	}
    
    public boolean loadLibraryContent(File libraryFile) {
    	listAdapter.clear();
    	listData.clear();
    	patchList.clear();
    	
    	//textViewSelectedPatchName = (TextView) activity.findViewById(R.id.patchName); 

    	Log.d( TAG, "loadLibraryContent " + libraryFile.getAbsolutePath() + " Lenght: " + libraryFile.length());
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
			while( (pos = fp.getFilePointer()) <= fp.length() - Patch.PATCH_SIZE){
				byte[] rawPatchData = new byte[Patch.PATCH_SIZE + 5];
				fp.read(rawPatchData, 0, Patch.PATCH_SIZE + 5);
				//namedPatches.put(bytesToString(rawPatchData), rawPatchData);
				//byte[]  = Arrays.copyOfRange(rawPatchData, str.length(), byteArr.length);
				patchList.add(rawPatchData);
				pos += (Patch.PATCH_SIZE + 5);
				fp.seek(pos);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		try {
			fp.close();
			libraryFile = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(byte[] patch : patchList){
			String patchName = Patch.bytesToString(patch);
			listData.add(patchName);
		}
		listAdapter.notifyDataSetChanged();
		return true;
    }
    
    public void saveProgram(){
    	AlertDialog.Builder alert = new AlertDialog.Builder(activity); 
    	final EditText edittext = new EditText(activity);
    	alert.setMessage("Enter program file name: ");
    	alert.setTitle("Store Single User Program on SDCard");
    	
		if(ydpFileName.endsWith(".YDP") || ydpFileName.endsWith(".ydp")){
			ydpFileName = ydpFileName.replaceAll(".YDP", "");
		}
    	
    	edittext.setText(ydpFileName);
    	alert.setView(edittext);
    	
    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int whichButton) {
    	       
    	    }
    	});

    	alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int whichButton) {
    	    	ydpFileName = edittext.getText().toString();
    	    	Log.d( TAG, "Entered " + ydpFileName);
    			
				if(ydpFileName.endsWith(".YDP") || ydpFileName.endsWith(".ydp")){
					ydpFileName = ydpFileName.replaceAll(".YDP", "");
				}
				
				ydpFile = new File(sdcardTHRFolderDir.getAbsolutePath().concat("/").concat(ydpFileName).concat(".YDP"));
				
				if(ydpFile.exists()){
					//showProgramOverwriteConfirmationDialog(ydpFile.getName());
					AlertDialog.Builder builder = new AlertDialog.Builder(activity);
					builder.setCancelable(false);
					builder.setMessage("File " + ydpFileName + " already exists. Do you want to overwrite it?");
					builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							storeProgramData(ydpFileName);
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
				}else{
					storeProgramData(ydpFileName);
				}
    	    }
    	});

    	alert.show();
    }
    
	public void storeProgramData(String userPatchName){
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(ydpFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		byte [] toFile = new byte[265];
		
		System.arraycopy(MESSAGE_START_PROGRAMM, 0, toFile, 0, MESSAGE_START_PROGRAMM.length);
		System.arraycopy(userPatchName.getBytes(), 0, toFile, OFFSET, userPatchName.getBytes().length);
		
    	// set amp type after patch name block
    	toFile[128 + OFFSET] = activity.getAmpData().getPatchValue();
    	
    	byte [] ctrlData = activity.getPatchValues();
    	toFile[129 + OFFSET] = ctrlData[0];
    	toFile[130 + OFFSET] = ctrlData[1];
    	toFile[131 + OFFSET] = ctrlData[2];
    	toFile[132 + OFFSET] = ctrlData[3];
    	toFile[133 + OFFSET] = ctrlData[4];
    	toFile[134 + OFFSET] = activity.getCabinetData().getPatchValue();
    	
    	byte [] compData = activity.getCompressorData().getPatchValues();
    	System.arraycopy(compData, 0, toFile, 144 + OFFSET, compData.length); 
    	
    	byte [] modData = activity.getModulationData().getPatchValues();
    	System.arraycopy(modData, 0, toFile, 160 + OFFSET, modData.length);
    	
    	byte [] delayData = activity.getDelayData().getPatchValues();
    	System.arraycopy(delayData, 0, toFile, 176 + OFFSET, delayData.length);
    	
    	byte [] reverbData = activity.getReverbData().getPatchValues();
    	System.arraycopy(reverbData, 0, toFile, 192 + OFFSET, reverbData.length);

    	byte [] gateData = activity.getGateData().getPatchValues();
    	System.arraycopy(gateData, 0, toFile, 208 + OFFSET, gateData.length);
    	
    	//Log.d( TAG, "ToFile:" + userPatchName + " Data: " + SysExCommands.byteToHex(toFile) + " Lenght: " + toFile.length + " byte(s)");
    	
		//long pos = 0; 
		try {
			if(outputStream != null){
				outputStream.write(toFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		updateSDCardFileList();
		
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(activity, "Program " + ydpFileName + " successfully stored in /Download/THR/ folder", Toast.LENGTH_LONG).show();
			}
		});	
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

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {
		String presetFilterType = parent.getItemAtPosition(position).toString();
		if(presetFilterType.equals("program")){
			presetFilter = "program";
		}else if(presetFilterType.equals("library")){
			presetFilter = "library";
		}else if(presetFilterType.equals("all")){ // both
			presetFilter = "all";
		}
		updateSDCardFileList();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
}
