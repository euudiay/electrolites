package org.microlites;

import org.microlites.bluetooth.BluetoothManager;
import org.microlites.data.Data;
import org.microlites.view.AnimationThread;
import org.microlites.view.ECGView;
import org.microlites.view.FullDynamicThread;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class MicrolitesActivity extends Activity implements OnGestureListener {
	BluetoothManager bluetoothManager;
	GestureDetector gestureScanner;
	
	public static MicrolitesActivity instance = null;
	
	AnimationThread currentViewThread;
	BluetoothManager currentManager;
	ECGView currentView;
	View menuView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Store references
        MicrolitesActivity.instance = this;
        gestureScanner = new GestureDetector(this);
        currentView = null;
        
        // Create handlers
        Button start = (Button) findViewById(R.id.startButton);
        start.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MicrolitesActivity.instance.initBluetoothVisualization(0, null);
			}
		});
        
        Button end = (Button) findViewById(R.id.stopButton);
        end.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (currentView != null) {
					bluetoothManager.stopRunning();
					MicrolitesActivity.instance.destroyECGView();
				}
			}
		});
    }
    
    public void initBluetoothVisualization(int phase, ECGView v) {
    	switch (phase) {
    	case 0: // Phase 0 - Init
    		System.out.println("Init Bluetooth Phase 0");
	    	// Create Bluetooth Manager
	    	bluetoothManager = new BluetoothManager();
	    	// Create ECGView
	    	// 1. Get reference to main content panel
	    	LinearLayout content = (LinearLayout) findViewById(R.id.contentPanel);
	    	menuView = content.getChildAt(0);
	    	// 2. Clear it
			content.removeAllViews();
			// 3. Add new Dynamic Surface Holder
			currentView = new ECGView(getApplicationContext(), null, this);
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
			bluetoothManager.startRunning("FireFly-3781", Data.getInstance().dynamicThread);
			break;
    	}
    }
    
    public void destroyECGView() {
    	bluetoothManager.stopRunning();
    	LinearLayout content = (LinearLayout) findViewById(R.id.contentPanel);
		content.removeAllViews();
		content.addView(menuView);
		currentView = null;
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent me)
    {
        return gestureScanner.onTouchEvent(me);
    }
    
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
    	//ECGView v = (ECGView) findViewById(R.id.ecgView);
    	//System.out.println("SCROLLSHIORE! [" + distanceX + ", " + distanceY + "]");
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
		// System.out.println("FLIGHSOR! [" + velocityX + ", " + velocityY + "]");
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
		return false;
	}
}