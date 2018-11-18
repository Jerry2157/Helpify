package mx.itesm.agbdc.helpify;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class Mapa extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    private LocationManager gps;
    private Marker myMarker1, myMarker2, myMarker3, myMarker4, myMarker5, previousMarker;
    private static final int  PERMISO_GPS   = 200;
    double lat;
    double lon;
    ArrayList<String> names;
    ArrayList<LatLng> latLngs;
    Location myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        latLngs = new ArrayList<>();
        names = new ArrayList<>();
        Intent intent = getIntent();
        String[] myStrings = intent.getStringArrayExtra("Coordenadas");

        for(int i = 0; i < (myStrings.length/3); i++)
        {
            double a = Double.valueOf(myStrings[(3 * i)]);
            double b = Double.valueOf(myStrings[(3 * i) + 1]);
            String n = myStrings[(3 * i) + 2];
            latLngs.add(new LatLng(a, b));
            names.add(n);
        }
        configurarGPS();
    }


    private void configurarGPS() {
        // Crea el administrador del gps
        gps = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Pregunta si está prendido el GPS en el sistema
        if (!gps.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Abrir Settings para prender el GPS, no se puede hacer con código
            prenderGPS();
        }
    }

    private void prenderGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("El GPS está apagado, ¿Quieres prenderlo?")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new
                                Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        setUbicacionInicial();// Abre settings

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void crearMarkers()
    {
        Log.i("Markers", "Marcadores");

        Log.i("SIZE", Integer.toString(latLngs.size()));
        int j = 0;
        for (LatLng lt: latLngs)
        {
            Log.i("LATLON: ", lt.toString());
            mMap.addMarker(new MarkerOptions().position(lt).title(names.get(j)));
            j++;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISO_GPS);
            // Contestará con onRequestPermissionsResult...

        } else {
            gps.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, (LocationListener) this);

        }

    }

    @Override
    public void onLocationChanged(Location location) {
        double newLat = location.getLatitude();
        double newLon = location.getLongitude();
        double distance = measure(lat, lon, newLat, newLon);
        if(distance > 10)
        {
            lat = newLat;
            lon = newLon;
            LatLng sydney = new LatLng(newLat, newLon);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }
    }

    private double measure(double lat1, double lon1, double lat2, double lon2){  // generally used geo measurement function
        double R = 6378.137; // Radius of earth in KM
        double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
        double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        return d * 1000; // meters
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.equals(previousMarker))
        {
            /*Intent show = new Intent(getBaseContext(), ShowInfo.class);
            startActivity(show);*/
            return true;
        }
        else
        {
            previousMarker = marker;
            Toast.makeText(this, "Press again the marker to confirm", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED)
        {
            mMap.setMyLocationEnabled(true);
            myLocation = gps.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            setUbicacionInicial();
        }
        mMap.setOnMarkerClickListener(this);
        //mMap.setMyLocationEnabled(true);

        crearMarkers();

    }

    void setUbicacionInicial()
    {

        if ( myLocation!=null) {
            lat = myLocation.getLatitude();
            lon = myLocation.getLongitude();
        }
        else
        {
            lat = 19.594210;
            lon = -99.228167;

        }
        LatLng tec = new LatLng(lat, lon);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tec, 18));
    }
}
