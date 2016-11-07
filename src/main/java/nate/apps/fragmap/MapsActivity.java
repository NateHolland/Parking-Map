package nate.apps.fragmap;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException; 
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

import nate.apps.ServerAccess;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener, View.OnClickListener {

    // reciever identifier
    private static final String MAP_RECIEVER = "map_reciever";
    //map object
    private GoogleMap mMap;
    //positional marker
    private Marker marker;
    private Context context;
    //zones list
    private ArrayList<Zone> parking_zones = new ArrayList<Zone>();
    //current zone
    private int current_zone = -1;
    // fragment to store data
    private DataStoreFragment dataFragment;

    //json tags
    private static final String CURRENT_LOCATION = "current_location";
    private static final String LOCATION_DATA = "location_data";
    private static final String VIEWPORT = "bounds";
    private static final String NORTH = "north";
    private static final String SOUTH = "south";
    private static final String EAST = "east";
    private static final String WEST = "west";
    private static final String ZONES = "zones";
    private static final String POLYGON = "polygon";
    public static final String PAYMENT_ALLOWED = "payment_is_allowed";
    public static final String PRICE = "service_price";
    public static final String CURRENCY = "currency";
    private static final String MAX_DURATION = "max_duration";
    private static final String PROVIDER = "provider_name";
    private static final String NAME = "name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.fragmap);
        FragmentManager fm = getFragmentManager();
        dataFragment = (DataStoreFragment) fm.findFragmentByTag("data");

        // create the fragment and data the first time
        if (dataFragment == null) {
            // add the fragment
            dataFragment = new DataStoreFragment();
            fm.beginTransaction().add(dataFragment,"data").commit();
            // load the data from the web
        }
        //hide start parking button and zone info view until needed
        findViewById(R.id.sign).setVisibility(View.GONE);
        findViewById(R.id.startpark).setVisibility(View.GONE);
        //set listener for start parking button
        findViewById(R.id.startpark).setOnClickListener(this);
        // Obtain the MapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // set listener for when map is ready
        mapFragment.getMapAsync(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intent_filter = new IntentFilter(MAP_RECIEVER);
        intent_filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(baseReciever, intent_filter);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(baseReciever);
    }
    //reciever for json object
    private BroadcastReceiver baseReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                JSONObject outcome = new JSONObject(intent.getStringExtra(ServerAccess.RESULT));
                //handle data in json
                handle_outcome(outcome);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //method to handle data in json
    private void handle_outcome(JSONObject json) throws JSONException {
        // Add a marker at location from json
        LatLng position = latlng_fromString(json.getString(CURRENT_LOCATION),",");
        marker = mMap.addMarker(new MarkerOptions().position(position));
        JSONObject location_data = json.getJSONObject(LOCATION_DATA);
        // If we have a saved camera position, such as after device orientation change then we go back to it
        LatLng existingtarget = dataFragment.getTarget();
        if(existingtarget!=null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(existingtarget,dataFragment.getZoom()));
        }else{
            // otherwise we move camera to viewport from json
            JSONObject viewport = location_data.getJSONObject(VIEWPORT);
            // Get southwest coord
            LatLng southwest = new LatLng(viewport.getDouble(SOUTH),viewport.getDouble(WEST));
            // Get Northeast coord
            LatLng northeast = new LatLng(viewport.getDouble(NORTH),viewport.getDouble(EAST));
            // Get Bounds
            LatLngBounds bounds = new LatLngBounds(southwest,northeast);
            // Move camera
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,0));
        }


        //iterate through zones
        JSONArray zones = location_data.getJSONArray(ZONES);
        for(int i = 0; i < zones.length(); i++){
            //add zone
            add_zone(zones.getJSONObject(i));
        }
    }

    //method to add zone to map
    private void add_zone(JSONObject zone) throws JSONException {

        String poly = zone.getString(POLYGON);
        //seperate each point
        String[] coords = poly.split(",");
        ArrayList<LatLng> latlngs = new ArrayList<>();
        for (String coord : coords) {
            //add points to list
            latlngs.add(latlng_fromString(coord," "));
        }
        //add polygon to map and to zone object
        Zone current_zone = new Zone(zone,mMap.addPolygon(new PolygonOptions().addAll(latlngs).strokeWidth(3)),context);
        //add zone to list
        parking_zones.add(current_zone);
    }

    //method to get LatLng value from string
    private LatLng latlng_fromString(String string, String divider) {
        String[] latlong =  string.trim().split(divider);
        double latitude = Double.parseDouble(latlong[0]);
        double longitude = Double.parseDouble(latlong[1]);
        return new LatLng(latitude,longitude);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // set our camera movement listeners
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraIdleListener(this);
        // start service to get json data
        Intent intent = new Intent(context, ServerAccess.class);
        intent.putExtra(ServerAccess.RECIEVER, MAP_RECIEVER);
        context.startService(intent);
    }

    @Override
    public void onCameraMoveStarted(int i) {
        //hide marker while moving map
        marker.setVisible(false);
    }

    @Override
    public void onCameraIdle() {
        //move marker to new position in center of view
        marker.setPosition(mMap.getCameraPosition().target);
        //save camera position
        dataFragment.setCamera(mMap.getCameraPosition().zoom,mMap.getCameraPosition().target);
        current_zone = -1;
        //iterate through parking zones
        for (Zone zone:parking_zones) {
            //if marker is in zone then display relevant information
            if(PolyUtil.containsLocation(marker.getPosition(),zone.getPolygon().getPoints(),true)){
                // get marker bitmap and set it
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(zone.getMarker()));
                //set zone colour to show we are in it
                zone.highlight();
                //store current zone index
                current_zone = parking_zones.indexOf(zone);
                try {
                    // write data to sign
                    int maxstay = (int) zone.getDouble(MAX_DURATION);
                    ((TextView)findViewById(R.id.maxstay)).setText(getResources().getString(R.string.maxstay)+ String.format(" %02d h %02d m",maxstay/60,maxstay%60));
                    ((TextView)findViewById(R.id.price)).setText(new DecimalFormat(zone.getString(CURRENCY)+"##,##0.00").format(zone.getDouble(PRICE)));
                    ((TextView)findViewById(R.id.name)).setText(zone.getString(NAME));
                    ((TextView)findViewById(R.id.provider)).setText(zone.getString(PROVIDER));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else{
                //set zone colour to default
                try {
                    zone.reset();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        //set marker to default if not in zone
        if(current_zone<0)marker.setIcon(BitmapDescriptorFactory.defaultMarker());
        // show start parking and zone info if in zone
        findViewById(R.id.sign).setVisibility(current_zone>-1?View.VISIBLE:View.GONE);
        findViewById(R.id.startpark).setVisibility(current_zone>-1?View.VISIBLE:View.GONE);
        //make marker visible again
        marker.setVisible(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.startpark:
                Log.v("parkig",""+current_zone);
                // tell user which parking area is being parked
                try {
                    Toast.makeText(context,String.format(getResources().getString(R.string.now_parking),parking_zones.get(current_zone).getString(NAME)),Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
