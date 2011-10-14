package com.electrolites.ecg;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ElectrolitesActivity extends Activity {
	Button raf = (Button) findViewById(R.id.button1);
    Button pfe = (Button) findViewById(R.id.button2);
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        OnClickListener listener = new OnClickListener() {
			public void onClick(View v) {
				//String text1 = (String) raf.getText();
	    		String text2 = (String) pfe.getText();
	    		raf.setText(text2);
	    		//pfe.setText(text1);
			}
		};
        
        raf.setOnClickListener(listener);
        //pfe.setOnClickListener(listener);
        
    }
    
    class TempClickListener implements OnClickListener {

		public void onClick(View v) {
    		raf.setText(pfe.getText());
		}
    }
}