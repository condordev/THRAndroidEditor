package de.sgrad.yamahathreditor;

import de.sgrad.yamahathreditor.SysExCommands.AmpType;
import de.sgrad.yamahathreditor.SysExCommands.THRModel;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class Amps {
	public static String TAG = "THR";
	
	private MainActivity activity;
	private Button btnClean;
	private Button btnLead;
	private Button btnCrunch;
	private Button btnBritHi;
	private Button btnModern;
	private Button btnBass;
	private Button btnAco;
	private Button btnFlat;
	
	private SysExCommands sysExCommands;
	public boolean initialized;
	
	public Amps(MainActivity activity){
		this.activity = activity;
		sysExCommands = new SysExCommands();
		setupUI();
		initialized = true;
	}
	
	public void changeAmpButtonText(THRModel model){
		if(model == THRModel.THR10C){
			btnClean.setText("Deluxe");
			btnCrunch.setText("Class A");
			btnLead.setText("US Blues");
			btnBritHi.setText("Brit Blues");
			btnModern.setText("Mini");
		}else if(model == THRModel.THR10X){
			
		}
	}

	private void setupUI() {
		btnClean = (Button) activity.findViewById(R.id.btnClean); 
		btnCrunch = (Button) activity.findViewById(R.id.btnCrunch); 
		btnLead = (Button) activity.findViewById(R.id.btnLead); 
		btnBritHi = (Button) activity.findViewById(R.id.btnBritHi); 
		btnModern = (Button) activity.findViewById(R.id.btnModern); 
		btnBass = (Button) activity.findViewById(R.id.btnBass); 
		btnAco= (Button) activity.findViewById(R.id.btnAco); 
		btnFlat = (Button) activity.findViewById(R.id.btnFlat); 
		
		updateUI();
		
		btnClean.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onDataChangeUI(sysExCommands.getAmpTypeSysEx(AmpType.CLEAN));
				activity.getAmpData().currentAmp = AmpType.CLEAN;
				updateUI();
			}
		});
		
		btnCrunch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onDataChangeUI(sysExCommands.getAmpTypeSysEx(AmpType.CRUNCH));
				activity.getAmpData().currentAmp = AmpType.CRUNCH;
				updateUI();
			}
		});
		
		btnLead.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onDataChangeUI(sysExCommands.getAmpTypeSysEx(AmpType.LEAD));
				activity.getAmpData().currentAmp = AmpType.LEAD;
				updateUI();
			}
		});
		
		btnBritHi.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onDataChangeUI(sysExCommands.getAmpTypeSysEx(AmpType.BRITHI));
				activity.getAmpData().currentAmp = AmpType.BRITHI;
				updateUI();
			}
		});
		
		btnModern.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onDataChangeUI(sysExCommands.getAmpTypeSysEx(AmpType.MODERN));
				activity.getAmpData().currentAmp = AmpType.MODERN;
				updateUI();
			}
		});
		
		btnBass.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onDataChangeUI(sysExCommands.getAmpTypeSysEx(AmpType.BASS));
				activity.getAmpData().currentAmp = AmpType.BASS;
				updateUI();
			}
		});		
		btnAco.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onDataChangeUI(sysExCommands.getAmpTypeSysEx(AmpType.ACO));
				activity.getAmpData().currentAmp = AmpType.ACO;
				updateUI();
			}
		});		
		btnFlat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onDataChangeUI(sysExCommands.getAmpTypeSysEx(AmpType.FLAT));
				activity.getAmpData().currentAmp = AmpType.FLAT;
				updateUI();
			}
		});		
	}
	
	public void updateUI(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
        		uncheckButtons();
        		switch(activity.getAmpData().currentAmp){
        		case ACO:
        			btnAco.getBackground().setColorFilter(Color.CYAN,Mode.MULTIPLY);
        			break;
        		case BASS:
        			btnBass.getBackground().setColorFilter(Color.CYAN,Mode.MULTIPLY);
        			break;
        		case BRITHI:
        			btnBritHi.getBackground().setColorFilter(Color.CYAN,Mode.MULTIPLY);
        			break;
        		case CLEAN:
        			btnClean.getBackground().setColorFilter(Color.CYAN,Mode.MULTIPLY);
        			break;
        		case CRUNCH:
        			btnCrunch.getBackground().setColorFilter(Color.CYAN,Mode.MULTIPLY);
        			break;
        		case FLAT:
        			btnFlat.getBackground().setColorFilter(Color.CYAN,Mode.MULTIPLY);
        			break;
        		case LEAD:
        			btnLead.getBackground().setColorFilter(Color.CYAN,Mode.MULTIPLY);
        			break;
        		case MODERN:
        			btnModern.getBackground().setColorFilter(Color.CYAN,Mode.MULTIPLY);
        			break;
        		default:
        			break;
        		}  
        		
        		String ampType = activity.getAmpData().currentAmp.toString(activity.model);
        		if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
        			activity.btnAmps.setText(Html.fromHtml("Amp" +  "<br />" +  "<small>" + ampType + "</small>" ));
        		}else{
        			activity.btnAmps.setText(Html.fromHtml("Amp " +  "<br />" +  "<small><small>" + "Acoustic" + "</small></small>" ));
        		}
        		
            }
        });
	}
	
	private void uncheckButtons(){
		btnAco.getBackground().setColorFilter(activity.btnBackgroundColor,Mode.MULTIPLY);
		btnBass.getBackground().setColorFilter(activity.btnBackgroundColor,Mode.MULTIPLY);
		btnBritHi.getBackground().setColorFilter(activity.btnBackgroundColor,Mode.MULTIPLY);
		btnClean.getBackground().setColorFilter(activity.btnBackgroundColor,Mode.MULTIPLY);
		btnCrunch.getBackground().setColorFilter(activity.btnBackgroundColor,Mode.MULTIPLY);
		btnFlat.getBackground().setColorFilter(activity.btnBackgroundColor,Mode.MULTIPLY);
		btnLead.getBackground().setColorFilter(activity.btnBackgroundColor,Mode.MULTIPLY);
		btnModern.getBackground().setColorFilter(activity.btnBackgroundColor,Mode.MULTIPLY);
	}
}
