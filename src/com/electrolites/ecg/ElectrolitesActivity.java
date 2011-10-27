package com.electrolites.ecg;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.EditText;

import com.electrolites.data.*;

public class ElectrolitesActivity extends Activity {
	private Data data;
	
	private Button start;
	private StartListener startListener;
	private Button up;
	private UpListener upListener;
	private Button down;
	private DownListener downListener;
	private Button plus;
	private PlusListener plusListener;
	private Button less;
	private LessListener lessListener;
	
	private EditText e_id;
	private EditText e_name;
	
	private LinearLayout lSuperior;
	private LinearLayout lInferior;
	
	
	private ECGView ecgView;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        data = Data.getInstance();
        
        start = (Button) findViewById(R.id.b_start);
        start.setEnabled(true);
        startListener = new StartListener();
        start.setOnClickListener(startListener);
        
        up = (Button) findViewById(R.id.b_up);
        up.setEnabled(true);
        upListener = new UpListener();
        up.setOnClickListener(upListener);
        
        down = (Button) findViewById(R.id.b_down);
        down.setEnabled(true);
        downListener = new DownListener();
        down.setOnClickListener(downListener);
        
        plus = (Button) findViewById(R.id.b_plus);
        plus.setEnabled(true);
        plusListener = new PlusListener();
        plus.setOnClickListener(plusListener);
        
        less = (Button) findViewById(R.id.b_less);
        less.setEnabled(true);
        lessListener = new LessListener();
        less.setOnClickListener(lessListener);
        
        ecgView = (ECGView) findViewById(R.id.v_eCGView);
        ecgView.setVisibility(View.INVISIBLE);
        

        lSuperior = (LinearLayout) findViewById(R.id.l_superior);
        lSuperior.setBackgroundColor(Color.DKGRAY);
        
        lInferior = (LinearLayout) findViewById(R.id.l_inferior);
        lInferior.setBackgroundColor(Color.DKGRAY);
        
        e_id = (EditText) findViewById(R.id.e_id);
        e_id.setTextColor(Color.GRAY);
        e_name = (EditText) findViewById(R.id.e_name);
        e_name.setTextColor(Color.GRAY);
        
    }
    
    class StartListener implements OnClickListener {

		public void onClick(View v) {
			start.setEnabled(false);
			ecgView.setVisibility(View.VISIBLE);
		}
    }
    
    class UpListener implements OnClickListener {

		public void onClick(View v) {
			if (data.getDrawBaseHeight() < 0.1)
				up.setEnabled(false);
			else 
			{
				data.setDrawBaseHeight(data.getDrawBaseHeight()-0.1f);
				down.setEnabled(true);
			}	
		}
    }
    
    
    class DownListener implements OnClickListener {
		public void onClick(View v) {			
			if (data.getDrawBaseHeight() >= 1)
				down.setEnabled(false);
			else
			{
				data.setDrawBaseHeight(data.getDrawBaseHeight()+0.1f);
				up.setEnabled(true);
			}			
		}    	
    }
    
    class PlusListener implements OnClickListener {

		public void onClick(View v) {
			data.setWidhtScale(data.getWidhtScale()+ 0.5f);
			less.setEnabled(true);	
		}
    }
    
    
    class LessListener implements OnClickListener {
		public void onClick(View v) {			
			if (data.getWidhtScale() < 0.5f)
				less.setEnabled(false);
			else
			{
				data.setWidhtScale(data.getWidhtScale() - 0.5f);
			}			
		}    	
    }  
    
    
    
    
    
    
}