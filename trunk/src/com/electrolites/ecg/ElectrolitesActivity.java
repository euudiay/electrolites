package com.electrolites.ecg;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class ElectrolitesActivity extends Activity {
	private Button rafButton;
	private ToggleButton toggleButton;
	private LinearLayout linearLayout;
	private LinearLayout linearLayout2;
	private YetAnotherListener listener;
	private ECGView ecgView;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        rafButton = (Button) findViewById(R.id.button1);
        rafButton.setEnabled(true);
        
        ecgView = (ECGView) findViewById(R.id.eCGView1);
        ecgView.setVisibility(View.INVISIBLE);
        
        listener = new YetAnotherListener();
        
        rafButton.setOnClickListener(listener);
        
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout2);
        linearLayout.setBackgroundColor(0xff444444);
        
        linearLayout2 = (LinearLayout) findViewById(R.id.linearLayout3);
        linearLayout2.setBackgroundColor(0xff444444);
    }
    
    class YetAnotherListener implements OnClickListener {

		public void onClick(View v) {
			
			rafButton.setEnabled(false);
			ecgView.setVisibility(View.VISIBLE);
		}
    	
    }
    
}