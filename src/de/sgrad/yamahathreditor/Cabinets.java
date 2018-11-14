package de.sgrad.yamahathreditor;

import de.sgrad.yamahathreditor.SysExCommands.CabinetType;
import de.sgrad.yamahathreditor.SysExCommands.THRModel;
import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

public class Cabinets{
	public static String TAG = "THR";
	
	private MainActivity activity;
	private Button btn1x12;
	private Button btn4x10;
	private Button btnBrit2x12;
	private Button btnBrit4x12;
	private Button btnUS2x12;
	private Button btnUS4x12;
	private Button btnNone;
	private SysExCommands sysExCommands;
	
	public boolean initialized;
	
	public Cabinets(MainActivity activity) {
		this.activity = activity;
		sysExCommands = new SysExCommands();
		setupUI();
		initialized = true;
	}
	
	public void changeCabinetButtonText(THRModel model){
		if(model == THRModel.THR10C){
			btn1x12.setText("Boutique 2x12");
			btn4x10.setText("Yamaha 2x12");
			btnBrit2x12.setText("American 1x12");
			btnBrit4x12.setText("California 1x12");
			btnUS2x12.setText("Boutique 2x12");
			btnUS4x12.setText("BritBlues 2x12");
		}else if(model == THRModel.THR10X){
			
		}
	}
	
	private void setupUI() {
		btn1x12 = (Button) activity.findViewById(R.id.btn1x12); 
		btn4x10 = (Button) activity.findViewById(R.id.btn4x10); 
		btnBrit2x12 = (Button) activity.findViewById(R.id.btnBrit2x12); 
		btnBrit4x12 = (Button) activity.findViewById(R.id.btnBrit4x12); 
		btnUS2x12 = (Button) activity.findViewById(R.id.btnUS2x12); 
		btnUS4x12 = (Button) activity.findViewById(R.id.btnUS4x12); 
		btnNone = (Button) activity.findViewById(R.id.btnNone); 
		
		updateUI();
		
		btn1x12.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onDataChangeUI(sysExCommands.getCabinetTypeSysEx(CabinetType.ONEx12));
				activity.getCabinetData().currentBox = CabinetType.ONEx12;
				updateUI();
			}
		});
		
		btn4x10.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onDataChangeUI(sysExCommands.getCabinetTypeSysEx(CabinetType.FOURx10));
				activity.getCabinetData().currentBox = CabinetType.FOURx10;
				updateUI();
			}
		});
		
		btnBrit2x12.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onDataChangeUI(sysExCommands.getCabinetTypeSysEx(CabinetType.Brit2x12));
				activity.getCabinetData().currentBox = CabinetType.Brit2x12;
				updateUI();
			}
		});
		
		btnBrit4x12.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onDataChangeUI(sysExCommands.getCabinetTypeSysEx(CabinetType.Brit4x12));
				activity.getCabinetData().currentBox = CabinetType.Brit4x12;
				updateUI();
			}
		});
		
		btnUS4x12.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onDataChangeUI(sysExCommands.getCabinetTypeSysEx(CabinetType.US4x12));
				activity.getCabinetData().currentBox = CabinetType.US4x12;
				updateUI();
			}
		});
		
		btnUS2x12.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onDataChangeUI(sysExCommands.getCabinetTypeSysEx(CabinetType.US2x12));
				activity.getCabinetData().currentBox = CabinetType.US2x12;
				updateUI();
			}
		});
		btnNone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onDataChangeUI(sysExCommands.getCabinetTypeSysEx(CabinetType.NONE));
				activity.getCabinetData().currentBox = CabinetType.NONE;
				updateUI();
			}
		});	
	}
	
	public void updateUI(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
        		
        		btn1x12.getBackground().setColorFilter(activity.btnBackgroundColor,Mode.MULTIPLY);
        		btn4x10.getBackground().setColorFilter(activity.btnBackgroundColor,Mode.MULTIPLY);
        		btnBrit2x12.getBackground().setColorFilter(activity.btnBackgroundColor,Mode.MULTIPLY);
        		btnBrit4x12.getBackground().setColorFilter(activity.btnBackgroundColor,Mode.MULTIPLY);
        		btnNone.getBackground().setColorFilter(activity.btnBackgroundColor,Mode.MULTIPLY);
        		btnUS2x12.getBackground().setColorFilter(activity.btnBackgroundColor,Mode.MULTIPLY);
        		btnUS4x12.getBackground().setColorFilter(activity.btnBackgroundColor,Mode.MULTIPLY);
        		
        		switch(activity.getCabinetData().currentBox){
        		case Brit2x12:
        			btnBrit2x12.getBackground().setColorFilter(Color.CYAN,Mode.MULTIPLY);
        			break;
        		case Brit4x12:
        			btnBrit4x12.getBackground().setColorFilter(Color.CYAN,Mode.MULTIPLY);
        			break;
        		case FOURx10:
        			btn4x10.getBackground().setColorFilter(Color.CYAN,Mode.MULTIPLY);
        			break;
        		case NONE:
        			btnNone.getBackground().setColorFilter(Color.CYAN,Mode.MULTIPLY);
        			break;
        		case ONEx12:
        			btn1x12.getBackground().setColorFilter(Color.CYAN,Mode.MULTIPLY);
        			break;
        		case US2x12:
        			btnUS2x12.getBackground().setColorFilter(Color.CYAN,Mode.MULTIPLY);
        			break;
        		case US4x12:
        			btnUS4x12.getBackground().setColorFilter(Color.CYAN,Mode.MULTIPLY);
        			break;
        		default:
        			break;
        		}
        		
        		String cabType = activity.getCabinetData().currentBox.toString(activity.model);
        		if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            		activity.btnCabinets.setText(Html.fromHtml("Cabinet" +  "<br />" +  "<small>" + cabType + "</small>" ));
        		}else{
            		activity.btnCabinets.setText(Html.fromHtml("Cab " +  "<br />" +  "<small><small>" + "None" + "</small></small>"));
        		}
            }
        });
	}

}
