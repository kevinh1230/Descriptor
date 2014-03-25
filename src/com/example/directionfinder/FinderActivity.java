package com.example.directionfinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@SuppressLint("DefaultLocale")
public class FinderActivity extends ListActivity implements LocationListener {
    
	static private final String LOG_TAG = "FinderActivity";
	static private final String SERVER_URL = "http://crowdlab.soe.ucsc.edu/tagstore/default/";
	static private final int MAX_SETUP_DOWNLOAD_TRIES = 2;
	
	//declare variables
	Context context;
    Button button1,button2,button3,button4,button5; //declare buttons
    ListView myListView; //declare listview
    LocationListener listener;
    LocationManager locationmanager;
    String provider;
    static Location currentLocation;
    	
    //custom arrayadapter
	private static final ArrayList<String> ListviewContent = new ArrayList<String>();
	
    private static class ListViewAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
     

        public ListViewAdapter(Context context) {
        	mInflater = LayoutInflater.from(context); //declare inflater
        }
    
        public int getCount() {
        	return ListviewContent.size(); 
        }
    
        public Object getItem(int position) {
        	return position;
        }
    
        public long getItemId(int position) {
        	return position;
        }
    
        @SuppressLint("DefaultLocale")
        public View getView(int position, View convertView, ViewGroup parent) {
        	ListContent content;
        	if (convertView == null) {
        		convertView = mInflater.inflate(R.layout.listview, null);
        		content = new ListContent();
        		content.text = (TextView) convertView.findViewById(R.id.TextView01);
        		content.text.setCompoundDrawables(convertView.getResources().getDrawable(R.drawable.ic_launcher), null, null, null);
        		convertView.setTag(content);
        	} else {   
        		content = (ListContent) convertView.getTag();
        	}
        	content.text.setText(ListviewContent.get(position));
        	return convertView;
        }

        static class ListContent {
        	TextView text;
        }
    }
    
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finder);
        locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); //initilize locationmanager
        context = this;
        Criteria criteria = new Criteria(); //create criteria
        provider = locationmanager.getBestProvider(criteria, false); //initialize provider
        List<String> loc_list = locationmanager.getAllProviders(); //get all providers 
        
        if (loc_list.size() != 0) {
        	Location location = locationmanager.getLastKnownLocation(provider);
        	if (location != null) {
        		System.out.println("Provider " + provider + " has been selected.");
        		onLocationChanged(location);
        	}
        	
        	//declare listener and buttons
        	listener = new MyLocationListener();
        	
        	locationmanager.requestLocationUpdates(provider, 120000, 50, this.listener);
		
        	button1=(Button)findViewById(R.id.Button01); 
        
        	button1.setText(button1.getText().toString());
        
        	button2=(Button)findViewById(R.id.Button02); 
        	button2.setText(button2.getText().toString());

        	button3=(Button)findViewById(R.id.Button03); 
        	button3.setText(button3.getText().toString());
        
        	button4 = (Button)findViewById(R.id.Button04);
        
        	button5 = (Button)findViewById(R.id.Button05);
        	
        }

        ListView lv = getListView();
        
        // listening to single list item on click
        lv.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int position, long id) {
 
              // selected item
              String product = ((TextView) view).getText().toString();
 
              // Launching new Activity on selecting single List Item
              Intent i = new Intent(getApplicationContext(), Compass.class);
              // sending data to new activity
              i.putExtra("product", product);
              Log.d("LOG_TAG", "You clicked item " + id + " at position " + position);
              String myitem = ((TextView) view).getText().toString();
              Log.d("LOG_TAG",myitem);
              startActivity(i);
 
          }
        });
    }
	
	public void pressButton1(View v){
		uploadTag(button1.getText().toString()); //uploads text on button 1
	}
	
	public void pressButton2(View v){
		uploadTag(button2.getText().toString()); //upload text on button 2
	}
	public void pressButton3(View v){
		uploadTag(button3.getText().toString()); //upload text on button 3
	}
	public void pressOther(View v){
		Intent intent = new Intent(this, NewLabel.class);
    	startActivityForResult(intent, 1); //goes to new activity
	}
	//get all tags in area
	public void pressGetTags(View v){
		Location location = locationmanager.getLastKnownLocation(provider);
		if (location != null) {
			onLocationChanged(location);
		}
	}
	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {

			if (resultCode == RESULT_OK) {

				String result = data.getStringExtra("result");

				//conditions to put most recently used button at top
				if (button2.getText().toString().equals(result)) {
					button2.setText(button1.getText().toString());
					button1.setText(result);
				}
				else if (button1.getText().toString().equals(result)) {
					button1.setText(result);
				} else {

					String newButton3Text = button2.getText().toString();
					button3.setText(newButton3Text);

					String newButton2Text = button1.getText().toString();
					button2.setText(newButton2Text);

					button1.setText(result);
				}
				
				uploadTag( result); //upload new label

			}
		}
	}
    
	
    @SuppressWarnings("deprecation")
	public void uploadTag(String tag_name) {
	    setListAdapter(new ListViewAdapter(context));
	    
		double lon = currentLocation.getLongitude(); //get longitude
		double lat = currentLocation.getLatitude(); //get latitude
		String tag_ue = URLEncoder.encode(tag_name);
		ServerCallParams serverParams = new ServerCallParams();
		serverParams.url = "add_tagging.json";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("token", "CMPS121_yehaa")); //add new params for tag
		params.add(new BasicNameValuePair("user", "NICKNAME"));
		params.add(new BasicNameValuePair("lat", lat + ""));
		params.add(new BasicNameValuePair("lng", lon + ""));
		params.add(new BasicNameValuePair("tag", tag_ue));
		serverParams.params = params; 
		serverParams.continuation = new ContinuationAddTag(); //upload the tag
		ContactServer contacter = new ContactServer();
		contacter.execute(serverParams);
	}
	
    class ContinuationAddTag implements Continuation {
		ContinuationAddTag() {}		
		public void useString(String s) {
			if (s == null) { //if string is null
				Log.d(LOG_TAG, "Returned an empty string."); //log it
			} else { //if not
				Log.d(LOG_TAG, "Returned: " + s); //print the string		
			}		
		}
	}
    
	
	public void getTags(View v) {
		// Let us build the parameters.
		ServerCallParams serverParams = new ServerCallParams();
		serverParams.url = "get_tags.json";
		double curr_lat = currentLocation.getLatitude();
		double curr_lon = currentLocation.getLongitude();
		
		double min_lat = curr_lat - 2.0;
		double max_lat = curr_lat + 2.0;
		double min_lon = curr_lon - 2.0;
		double max_lon = curr_lon + 2.0;
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("token", "CMPS121_yehaa"));
		params.add(new BasicNameValuePair("user", "luca"));
		params.add(new BasicNameValuePair("lat_min", min_lat + ""));
		params.add(new BasicNameValuePair("lng_min", min_lon + ""));
		params.add(new BasicNameValuePair("lat_max", max_lat + ""));
		params.add(new BasicNameValuePair("lng_max", max_lon + ""));
		params.add(new BasicNameValuePair("n_taggings", "20"));
		serverParams.params = params;
		serverParams.continuation = new ContinuationGetTagList();
		ContactServer contacter = new ContactServer();
		contacter.execute(serverParams);
	}
	
	class ContinuationGetTagList implements Continuation {
		public ContinuationGetTagList() {}

		@SuppressWarnings("deprecation")
		public void useString(String s) {
			// Dejasonize the string.
			if (s == null) {
				Log.d(LOG_TAG, "Returned an empty string.");
			} else {
				Log.d(LOG_TAG, "Returned: " + s);
			    NearbyTags newTags = decodeNearbyTags(s);
			    if (newTags != null) {
			    	Log.d(LOG_TAG, "The dejsonizing succeeded");
			    	Log.d(LOG_TAG, "N. of tags:" + newTags.tags.size());
			        Collections.sort(newTags.tags, COMPARATOR);
			    	for (TagInfo currtag : newTags.tags) {
			    		// Tag names are URLDecoded in the following line:
			    		String currtag_tag = URLDecoder.decode(currtag.tag);
			    		// String currtag_nick = currtag.nick;
			    		double currtag_lat = currtag.lat;
			    		double currtag_lng = currtag.lng;
			    	    Location l = new Location(currentLocation);
			    	    l.setLatitude(currtag_lat);
			    	    l.setLongitude(currtag_lng);
			    	    double distance = currentLocation.distanceTo(l);
			    	    int distance_rounded = (int) distance;
			    	    double bearing = currentLocation.bearingTo(l);
			    	    String compass = getBearing(bearing);
			    	    
			    	    String listelement = "" + currtag_tag + " " + distance_rounded + " " + compass;
						ListviewContent.add(listelement);
			    	}
					setListAdapter(new ListViewAdapter(context));   
			    }
			}
		}
	}
	
    private static Comparator<TagInfo> COMPARATOR = new Comparator<TagInfo>()
    {
        public int compare(TagInfo o1, TagInfo o2)
        {
    		double latitude1 = o1.lat; //get latitude
    		double longitude1 = o1.lng; //get longitude
    	    Location l1 = new Location(currentLocation); //get current location
    	    l1.setLatitude(latitude1); //set new latitude
    	    l1.setLongitude(longitude1); //set new longitude
    	    double distance1 = currentLocation.distanceTo(l1); //find distance

    		double la2 = o2.lat; //get latitude
    		double ln2 = o2.lng; //get longitude
    	    Location l2 = new Location(currentLocation); //get current location
    	    l2.setLatitude(la2); //set new latitude
    	    l2.setLongitude(ln2); //set new longitude
    	    double distance2 = currentLocation.distanceTo(l2); //get distance
    	    int result = 0;
    	    if (distance1 < distance2) {
    	    	result = -1;
    	    }
    	    else if (distance1 > distance2) {
    	    	result = 1;
    	    }
    	    else if (distance1 == distance2) {
    	    	return 0;
    	    }
    	    return result;
        }
    };
	
	interface Continuation {
		void useString(String s);
	}
	
	class ServerCallParams {
		public String url; // for example: get_tags.json
		public List<NameValuePair> params;
		public Continuation continuation;
	}
	
	class FinishInfo {
		public Continuation continuation;
		public String value;
	}
    private class ContactServer extends AsyncTask<ServerCallParams, String, FinishInfo> {

    	protected FinishInfo doInBackground(ServerCallParams... callParams) {
    		Log.d(LOG_TAG, "Starting the download.");
    		String downloadedString = null;
    		int numTries = 0;
    		ServerCallParams callInfo = callParams[0];
    		List<NameValuePair> params = callInfo.params;
			FinishInfo info = new FinishInfo();
			info.continuation = callInfo.continuation;
    		while (downloadedString == null && numTries < MAX_SETUP_DOWNLOAD_TRIES && !isCancelled()) {
    			numTries++;
    			HttpClient httpclient = new DefaultHttpClient();
    		    HttpPost httppost = new HttpPost(SERVER_URL + callInfo.url);
    	        HttpResponse response = null; 
    			try {
        	        httppost.setEntity(new UrlEncodedFormEntity(params));
        	        // Execute HTTP Post Request
    				response = httpclient.execute(httppost);
    			} catch (ClientProtocolException ex) {
    				Log.e(LOG_TAG, ex.toString());
    			} catch (IOException ex) {
    				Log.w(LOG_TAG, ex.toString());
    			}
    			if (response != null) {
    				// Checks the status code.
    				int statusCode = response.getStatusLine().getStatusCode();
    				Log.d(LOG_TAG, "Status code: " + statusCode);

    				if (statusCode == HttpURLConnection.HTTP_OK) {
    					// Correct response. Reads the real result.
    					// Extracts the string content of the response.
    					HttpEntity entity = response.getEntity();
    					InputStream iStream = null;
    					try {
    						iStream = entity.getContent();
    					} catch (IOException ex) {
    						Log.e(LOG_TAG, ex.toString());
    					}
    					if (iStream != null) {
    						downloadedString = ConvertStreamToString(iStream);
    						Log.d(LOG_TAG, "Received string: " + downloadedString);
    						// Passes the string, along with the continuation, to onPostExecute.
    						info.value = downloadedString;
    				    	return info;
    					}
    				}
    			}
    		}
    		// Returns null to indicate failure.
    		info.value = null;
    		return info;
    	}
    	
    	protected void onProgressUpdate(String... s) {}
    	
    	protected void onPostExecute(FinishInfo info) {
    		// Do something with what you get. 
    		if (info != null) {
    			info.continuation.useString(info.value);
    		} else {
    			// This is just an example: we can pass back null to the continuation
    			// to indicate that no string was in fact received.
    			info.continuation.useString(null);
    		}
    	}
    }
    
    // Here is an example of how to decode a JSON string.  We will decode the taglist.
    // First, we declare a class for the info on one tag.
    // Note that if you want to have this accessible from multiple activities, as it might be
    // a good idea to do, it might be better to define this as a public class in its own file,
    // rather than here. 
    
    class TagInfo {
    	public double lat;
    	public double lng;
    	public String nick;
    	public String tag;
    }
    // Now we create a class for the overall message.
    class NearbyTags {
    	public List<TagInfo> tags;
    }
    
    // Decoding a received tag string is then simple.
    private NearbyTags decodeNearbyTags(String s) {
    	if (s == null) {
    		// Your choice of what to do; returning null may be a simple way to 
    		// propagate the fact that the call to the server failed.
    		return null;
    	}
    	// Gets a gson object for decoding a string.
    	Gson gson = new Gson();
    	NearbyTags tags = null;
    	try {
    		tags = gson.fromJson(s, NearbyTags.class);
    	} catch (JsonSyntaxException ex) {
    		Log.w(LOG_TAG, "Error decoding json: " + s + " exception: " + ex.toString());
    	}
    	return tags;
    }
      
    public static String ConvertStreamToString(InputStream is) {   	
    	if (is == null) {
    		return null;
    	}
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    try {
	        while ((line = reader.readLine()) != null) {
	            sb.append((line + "\n"));
	        }
	    } catch (IOException e) {
	        Log.d(LOG_TAG, e.toString());
	    } finally {
	        try {
	            is.close();
	        } catch (IOException e) {
	            Log.d(LOG_TAG, e.toString());
	        }
	    }
	    return sb.toString();
	}

    /* Request updates at startup */
    @Override
    protected void onResume() {
      super.onResume();
      	Location location = locationmanager.getLastKnownLocation(provider);
  		if (location != null) {
  			System.out.println("Provider " + provider + " has been selected.");
  			onLocationChanged(location);
  		}
  		locationmanager.requestLocationUpdates(provider, 120000, 50, listener);
	}

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
      super.onPause();
      locationmanager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
      currentLocation = new Location(location); 
      ListviewContent.clear(); 
      getTags(myListView); 
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
      // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
      Toast.makeText(this, "Enabled new provider " + provider,
          Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
      Toast.makeText(this, "Disabled provider " + provider,
          Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onStop(){
       super.onStop();
    }
    
    //gets the bearing of the tag
    public String getBearing(double bearing) {

    	String result = "";
    	if (bearing >= 340.0 || bearing < 20.0) {
    		result = "N";
    	}
    	else if (bearing >= 20.0 || bearing < 70.0) {
    		result = "NE";
    	}
    	else if (bearing >= 70.0 || bearing < 110.0) {
    		result = "E";
    	}
    	else if (bearing >= 110.0 || bearing < 160.0) {
    		result = "SE";
    	}
    	else if (bearing >= 160.0 || bearing < 200.0) {
    		result = "S";
    	}
    	else if (bearing >= 200.0 || bearing < 250.0) {
    		result = "SW";
    	}
    	else if (bearing >= 250.0 || bearing < 290.0) {
    		result = "W";
    	}
    	else if (bearing >= 290.0 || bearing < 340.0) {
    		result = "NW";
    	}
    	return result;
    }
    
    
    private final class MyLocationListener implements LocationListener {	
    	 public void onLocationChanged(Location location) {
    	      currentLocation = new Location(location);
    	      ListviewContent.clear();
    	      getTags(myListView);
    	    }
    	    @Override
    	    public void onStatusChanged(String provider, int status, Bundle extras) {
    	      // TODO Auto-generated method stub
    	    }
    	    @Override
    	    public void onProviderEnabled(String provider) {
    	    }
    	    @Override
    	    public void onProviderDisabled(String provider) {
    	    }
    }
    
}