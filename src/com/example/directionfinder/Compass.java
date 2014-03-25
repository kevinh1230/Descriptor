package com.example.directionfinder;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class Compass extends Activity implements SensorEventListener {

	//declare compass,Sensor, SensorManager
	CompassView compass;
	private SensorManager mSensorManager;
	private Sensor mCompass;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_compass);
		compass = (CompassView) findViewById(R.id.compassView); //initialize compass

		TextView txtProduct = (TextView) findViewById(R.id.textView1); //initialize textview

		Intent i = getIntent(); //get intent
		String product = i.getStringExtra("product"); //get string
		txtProduct.setText(product); //set text to activity
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE); //initialize sensormanager
	    mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	}
	//require function
	public void onAccuracyChanged(Sensor sensor, int accuracy) {    
	}
	
	public void onSensorChanged(SensorEvent event) {
	    float azimuth = Math.round(event.values[0]); //get values 
	    compass.setBearing((float) azimuth); //set bearing
	}

	public void setMyBearing(View V) {
		EditText et = (EditText) findViewById(R.id.editText1); 
		TextView tv = (TextView) findViewById(R.id.textView2);
		String bearing = et.getText().toString(); //get text in edittext
		float number = Float.valueOf(bearing); //turn text to float
		tv.setText("You set your bearing to " + bearing + " degrees!");
		compass.setBearing((float) number); //set bearing to that number
	}
	
	@Override
	protected void onPause() {
	    // Unregister the listener on the onPause() event to preserve battery life;
	    super.onPause();
	    mSensorManager.unregisterListener(this);
	}

	@Override
	protected void onResume() {
	    super.onResume();
	    mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_NORMAL);
	}
}
