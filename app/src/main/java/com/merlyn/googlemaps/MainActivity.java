package com.merlyn.googlemaps;

import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private GoogleMap mGoogleMap;
    private SupportMapFragment mMapFragment;
    private List<Kota> listKota= new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setMaps();
    }

    private void setMaps() {
        mMapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap=googleMap;
                while(listKota.size()==0) {
                    try {
                        listKota=new MapCallback().execute().get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                boolean runonce=true;
                for (int i=0;i<listKota.size();i++) {
                    Kota k = listKota.get(i);
                    LatLng mLatLng = new LatLng(k.getLatitude(),k.getLongitude());
                    mGoogleMap.addMarker(new MarkerOptions().position(mLatLng).title(k.getName()));

                    if (runonce) {
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng,10));
                        runonce=false;
                    }
                }
            }
        });
    }

    private static class MapCallback extends AsyncTask<Void, Void, List<Kota>> {
        private List<Kota> listKota= new ArrayList<>();

        @Override
        protected List<Kota> doInBackground(Void... voids) {
            String url="https://dev.projectlab.co.id/mit/1317003/get_location.php";
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = jsonParser.makeHttpRequest(url,"GET", new ArrayList<NameValuePair>());
            if (jsonObject!= null) {
                try {
                    int success=jsonObject.getInt("success");
                    if (success==1){
                        JSONArray arrayKota =jsonObject.getJSONArray("locations");
                        for (int i=0;i<arrayKota.length();i++) {
                            JSONObject k = arrayKota.getJSONObject(i);
                            Kota kota = new Kota(k.getString("name"), k.getDouble("latitude"), k.getDouble("longitude"));
                            listKota.add(kota);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            return listKota;
        }
    }

}
