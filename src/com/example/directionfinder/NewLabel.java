package com.example.directionfinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class NewLabel extends Activity {
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_new_label);
		}
		
		//brings data back to first activity. 
		public void pressedEnter (View v){
			EditText et = (EditText) findViewById(R.id.editText);
			String t = et.getText().toString();
			Intent returnIntent = new Intent(); //create intent to return data
			returnIntent.putExtra("result",t); //store data in here
			setResult(RESULT_OK,returnIntent); 
			finish();
		}
	}