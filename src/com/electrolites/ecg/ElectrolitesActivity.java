package com.electrolites.ecg;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ElectrolitesActivity extends Activity {
	private Button rafButton;
	private Button pfervButton;
	private YetAnotherListener listener;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        rafButton = (Button) findViewById(R.id.button1);
        pfervButton = (Button) findViewById(R.id.button2);
        
        listener = new YetAnotherListener();
        
        rafButton.setOnClickListener(listener);
        pfervButton.setOnClickListener(listener);
    }
    
    class YetAnotherListener implements OnClickListener {

		public void onClick(View v) {
			String text1 = (String) rafButton.getText();
			String text2 = (String) pfervButton.getText();
			
			rafButton.setText(text2);
			pfervButton.setText(text1);
		}
    	
    }
    
}