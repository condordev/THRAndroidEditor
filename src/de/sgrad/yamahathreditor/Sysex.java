package de.sgrad.yamahathreditor;

import android.graphics.Color;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class Sysex{
	public static String TAG = "THR";
	
	final private byte [] sysExMsgFrame = new byte [] {(byte)0xf0, 0x43, 0x7d, 0x10, 0x41, 0x30, 0x01, 0x00, 0x00, 0x00, (byte)0xf7};
	
	private MainActivity activity;
	private Button btnSendSysex;
	private Button btnFirstUp;
	private Button btnFirstDown;
	private Button btnSecondUp;
	private Button btnSecondDown;
	private Button btnThirdUp;
	private Button btnThirdDown;
	private TextView sysexEdit;
	public ListView midiEventListView2;
	private EditText num1;
	private EditText num2;
	private EditText num3;
	private CheckBox c1;
	private CheckBox c2;
	private CheckBox c3;
	
	public boolean initialized;
	
	public Sysex(MainActivity activity) {
		this.activity = activity;
		setupUI();
		initialized = true;
	}
	
	private void setupUI() {
		btnSendSysex = (Button) activity.findViewById(R.id.btnSendSysex); 
		btnFirstUp = (Button) activity.findViewById(R.id.btnFirstUp); 
		btnFirstDown = (Button) activity.findViewById(R.id.btnFirstDown); 
		btnSecondUp = (Button) activity.findViewById(R.id.btnSecondUp); 
		btnSecondDown = (Button) activity.findViewById(R.id.btnSecondDown); 
		btnThirdUp = (Button) activity.findViewById(R.id.btnThirdUp); 
		btnThirdDown = (Button) activity.findViewById(R.id.btnThirdDown); 
		sysexEdit = (TextView) activity.findViewById(R.id.sysexEdit); 
		midiEventListView2 = (ListView) activity.findViewById(R.id.midiEventListView2);
		num1 = (EditText) activity.findViewById(R.id.inputnumber1);
		num2 = (EditText) activity.findViewById(R.id.inputnumber2);
		num3 = (EditText) activity.findViewById(R.id.inputnumber3);
		c1 = (CheckBox) activity.findViewById(R.id.c1);
		c2 = (CheckBox) activity.findViewById(R.id.c2);
		c3 = (CheckBox) activity.findViewById(R.id.c3);
		

		Spannable wordtoSpan = new SpannableString("f0 43 7d 10 41 30 01 xx xx xx f7");        
		wordtoSpan.setSpan(new ForegroundColorSpan(Color.GREEN), 21, 29, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		sysexEdit.setText(wordtoSpan);
		
		c1.setChecked(false);
		c2.setChecked(false);
		c3.setChecked(false);
		
		//updateUI();
		
		btnSendSysex.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onDataChangeUI(sysExMsgFrame);
				updateUI();
			}
		});
		
		btnFirstUp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sysExMsgFrame[7] += 1; 
				if(c1.isChecked()){
					activity.onDataChangeUI(sysExMsgFrame);
				}
				updateUI();
			}
		});
		
		btnFirstDown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(sysExMsgFrame[7] > 0)
					sysExMsgFrame[7] -= 1;
				if(c1.isChecked()){
					activity.onDataChangeUI(sysExMsgFrame);
				}
				updateUI();
			}
		});
		
		btnSecondUp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sysExMsgFrame[8] += 1;
				if(c2.isChecked()){
					activity.onDataChangeUI(sysExMsgFrame);
				}
				updateUI();
			}
		});
		
		btnSecondDown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(sysExMsgFrame[8] > 0)
					sysExMsgFrame[8] -= 1;
				if(c2.isChecked()){
					activity.onDataChangeUI(sysExMsgFrame);
				}
				updateUI();
			}
		});
		
		btnThirdUp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sysExMsgFrame[9] += 1;
				if(c3.isChecked()){
					activity.onDataChangeUI(sysExMsgFrame);
				}
				updateUI();
			}
		});
		btnThirdDown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(sysExMsgFrame[9] > 0)
					sysExMsgFrame[9] -= 1;
				if(c3.isChecked()){
					activity.onDataChangeUI(sysExMsgFrame);
				}
				updateUI();
			}
		});	
		
		num1.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	 if (s.length() > 0 && s.length() <=2) {
                 	sysExMsgFrame[7] = Integer.valueOf(s.toString(), 16).byteValue();
                	updateUI();
            	 }else{
            		 num1.getText().clear(); 
            	 }
            }
         });
		
		num2.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	 if (s.length() > 0 && s.length() <=2) {
                 	sysExMsgFrame[8] = Integer.valueOf(s.toString(), 16).byteValue();
                	updateUI();
            	 }else{
            		 num2.getText().clear(); 
            	 }
            }
         });
		num3.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	 if (s.length() > 0 && s.length() <=2) {
                 	sysExMsgFrame[9] = Integer.valueOf(s.toString(), 16).byteValue();
                	updateUI();
            	 }else{
            		 num3.getText().clear(); 
            	 }
            }
         });
		
	    c1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	        @Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					
				}
			}

	    });
	    
	    c2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					
				}
			}
	    });
	    
	    c3.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					
				}
			}
	    });
	}
	
	public void updateUI(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
        		Spannable wordtoSpan = new SpannableString(SysExCommands.byteToHex(sysExMsgFrame));        
        		wordtoSpan.setSpan(new ForegroundColorSpan(Color.GREEN), 21, 29, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        	    sysexEdit.setText(wordtoSpan);
            }
        });
	}
}
