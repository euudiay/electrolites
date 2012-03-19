package org.microlites;

import org.microlites.data.Data;
import org.microlites.data.DataManager;
import org.microlites.data.bluetooth.BluetoothManager;
import org.microlites.view.AnimationThread;
import org.microlites.view.ECGView;
import org.microlites.view.FullDynamicThread;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MicrolitesActivity extends Activity implements OnGestureListener {
	GestureDetector gestureScanner;				// Gesture Detector
	
	public static MicrolitesActivity instance;	// Reference to this Activity
	ECGView currentView;						// Reference to current View
	AnimationThread currentViewThread;			// Reference to View Thread
	DataManager currentManager;					// Reference to Data Manager
	View menuView;								// Refernece to initial Menu
	
    /** Called when the activity is first created,
     * 	when the screen is rotated or when the
     *  application is paused and resumed by the system
     *  or the user.
     * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set main layout
    	setContentView(R.layout.main);
    	
    	// Store reference to this instance
        MicrolitesActivity.instance = this;
        // Instantiate Gesture Detector
        gestureScanner = new GestureDetector(this);
        // Initialize references
        currentView = null;
        currentViewThread = null;
        currentManager = null;
        menuView = null;
        
        // Restore view if application has been restored
    	if (savedInstanceState != null) {
    		if (savedInstanceState.containsKey("currentView")) {
    			String view = savedInstanceState.getString("currentView");
    			if (view == "ECGView") {
    				initBluetoothVisualization(0, null);
    				// TODO: Restore Thread Status
    				// 1. Restore View
    				// 2. Restore Thread
    			}
    		}
        }
        
        // Create Main Menu Button Handlers
    	if (currentView == null) {
    		SeekBar bar = (SeekBar) findViewById(R.id.seekBar1);
    		bar.setProgress(50);
    		
    		bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					int max = seekBar.getMax();
					Float w = 1 + progress/((float) max)*2;
					if (w - Math.floor(w.doubleValue()) > 0.5)
						w = (float) (Math.floor(w.doubleValue()) + 0.5);
					else
						w = (float) (Math.floor(w.doubleValue()));
						
					((TextView) findViewById(R.id.textView1)).setText("Ancho en segundos: "+w);
					Data.getInstance().viewWidth = w;
				}
			});
    		
	        Button start = (Button) findViewById(R.id.startBluetoothButton);
	        start.setOnClickListener(new OnClickListener() {
	        	// Start button inits visualization
				public void onClick(View v) {
					MicrolitesActivity.instance.initBluetoothVisualization(0, null);
				}
			});
    	}
    }
    
    public void initBluetoothVisualization(int phase, ECGView v) {
    	switch (phase) {
    	case 0: // Phase 0 - Init
    		System.out.println("Init Bluetooth Phase 0");
    		
	    	// Create Bluetooth Manager
	    	currentManager = new BluetoothManager();
	    	
	    	// Create ECGView
	    	// 1. Get reference to main content panel
	    	LinearLayout content = (LinearLayout) findViewById(R.id.contentPanel);
	    	menuView = content.getChildAt(0);
	    	
	    	// 2. Clear it
			content.removeAllViews();
			
			// 3. Add new Dynamic Surface Holder
			currentView = new ECGView(getApplicationContext(), null, this);
			currentView.notifyAboutCreation = true;
	        content.addView(currentView);
	        
	        // 4. Wait for dynamicHolder to call this again
	        break;
    	case 1: // Phase 1 - Surface available, start the magic!
    		System.out.println("Init Bluetooth Phase 1");
    		Data d = Data.getInstance();
    		
    		// 1. Instantiate viewthread
    		d.dynamicThread = new FullDynamicThread(d.currentViewHolder, currentView);
    		currentView.setThread(d.dynamicThread);
    		
    		// 2. Start reception thread
    		currentManager.configure(Data.getInstance().dynamicThread);
			currentManager.start();
			break;
    	}
    }
    
    public void destroyECGView() {
    	if (currentManager != null) {
    		currentManager.stop();
    		LinearLayout content = (LinearLayout) findViewById(R.id.contentPanel);
    		content.removeAllViews();
    		content.addView(menuView);
    		currentView = null;
    	}
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	if (currentView instanceof ECGView) {
    		outState.putString("currentView", "ECGView");
    		if (currentViewThread != null)
    			currentViewThread.saveYourData(outState);
    	}
    	else
    		outState.remove("currentView");
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	if (currentManager != null)
    		currentManager.stop();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent me)
    {
        return gestureScanner.onTouchEvent(me);
    }
    
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
    	if (currentView != null)
    		currentView.handleScroll(distanceX,distanceY);
        return true;
    }

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		Data.getInstance().pause = !Data.getInstance().pause;
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Detener");
		menu.add("Más Cosas");
		menu.add("Salir");
		menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		//menu.getItem(0).setEnabled(false);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    if (item.getTitle().equals("Detener")) {
            destroyECGView();
	    } else if (item.getTitle().equals("Salir")){
	    	finish();
	    } else {
	    	return super.onOptionsItemSelected(item);
	    }
	    
        return true;
	}
}