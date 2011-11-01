package com.electrolites.ecg;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Layout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.TextView;

import com.electrolites.data.*;

public class ElectrolitesActivity extends Activity {
	
	static final int DIALOG_EXIT_ID = 0;
	static final int DIALOG_ABOUT_ID = 1;
	
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
	
	private TextView hRate;
	private TextView display;
	
	private EditText id;
	private EditText name;
	
	private LinearLayout lSuperior;
	private LinearLayout lInferior;
	
	
	private ECGView ecgView;
    
	@Override
	public void onSaveInstanceState(Bundle saveInstanceState) {
		saveInstanceState.putBoolean("ecgVisible", ecgView.getVisibility() == View.VISIBLE);
		super.onSaveInstanceState(saveInstanceState);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState.getBoolean("ecgVisible"))
        	ecgView.setVisibility(View.VISIBLE);
	}
	
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
        
        id = (EditText) findViewById(R.id.e_id);
        id.setTextColor(Color.GRAY);
        name = (EditText) findViewById(R.id.e_name);
        name.setTextColor(Color.GRAY);
        
        hRate = (TextView) findViewById(R.id.t_hRate);
        hRate.setBackgroundColor(Color.DKGRAY);    
        hRate.setGravity(Gravity.CENTER);
        hRate.setTextSize(30f);
        hRate.setTextColor(Color.RED);
        hRate.setText("60 bpr");
        
        display = (TextView) findViewById(R.id.t_display);
        display.setBackgroundColor(Color.DKGRAY);    
        display.setGravity(Gravity.CENTER);
        display.setTextSize(30f);
        display.setTextColor(Color.GRAY);
        display.setText("NOT ARRITMIA DETECTED");
    }
    
    //Menu Items 
    
    //Called when a dialog is first created
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
        case DIALOG_EXIT_ID:
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage("Are you sure you want to exit?")
        	       .setCancelable(false)
        	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	                ElectrolitesActivity.this.finish();
        	           }
        	       })
        	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	                dialog.cancel();
        	           }
        	       });
        	dialog = builder.create();
            break;
        case(DIALOG_ABOUT_ID):
        	dialog = new Dialog(this);
        	dialog.setContentView(R.layout.about_dialog);
        	dialog.setTitle("About");
        	
        	LinearLayout labout = (LinearLayout) dialog.findViewById(R.id.layout_about);
        	labout.setOnClickListener(new AboutListener());
        	
        	TextView text = (TextView) dialog.findViewById(R.id.text);
        	text.setGravity(Gravity.CENTER);
        	text.setText("Electrolites V0.0");
        	ImageView image = (ImageView) dialog.findViewById(R.id.im_android);

        	image.setImageResource(R.drawable.android1);
   	
        	break;
        default:
            dialog = null;
        }
        return dialog;
    } 
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
        
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.start:
			start.setEnabled(false);
			ecgView.setVisibility(View.VISIBLE);
            return true;
        case R.id.about:
            showDialog(DIALOG_ABOUT_ID);
            return true;
        case R.id.exit:
            showDialog(DIALOG_EXIT_ID);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
   
   //AboutDialog Listener
    
    class AboutListener implements OnClickListener {
    	
    	public void onClick(View v){
    		dismissDialog(DIALOG_ABOUT_ID);
    	}
    }
    
   //Main Layout Listeners  
    class StartListener implements OnClickListener {

		public void onClick(View v) {
			start.setEnabled(false);
			ecgView.setVisibility(View.VISIBLE);
		}
    }
    
    class UpListener implements OnClickListener {

		public void onClick(View v) {
			if (ecgView.getVisibility() != View.VISIBLE)
				return;
			
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
			if (ecgView.getVisibility() != View.VISIBLE)
				return;
			
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
			if (ecgView.getVisibility() != View.VISIBLE)
				return;
			
			data.setWidhtScale(data.getWidhtScale()+ 0.5f);
			less.setEnabled(true);	
		}
    }
    
    class LessListener implements OnClickListener {
		public void onClick(View v) {
			if (ecgView.getVisibility() != View.VISIBLE)
				return;
			
			if (data.getWidhtScale() < 0.5f)
				less.setEnabled(false);
			else
			{
				data.setWidhtScale(data.getWidhtScale() - 0.5f);
			}			
		}    	
    }  

    
}