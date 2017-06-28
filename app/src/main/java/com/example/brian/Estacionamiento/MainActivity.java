package com.example.brian.Estacionamiento;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.provider.Settings;
import android.Manifest;

import java.io.IOException;
import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity {


    LocationManager locationManager;
    double longitudGPS, latitudGPS;
    double longitudArd, latitudArd;
    TextView longitudeValueGPS, latitudeValueGPS;
    TextView longitudValorArd, latitudValorArd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        longitudeValueGPS = (TextView) findViewById(R.id.longitudeValueGPS);
        latitudeValueGPS = (TextView) findViewById(R.id.latitudeValueGPS);
        longitudValorArd= (TextView) findViewById(R.id.longArd);
        latitudValorArd= (TextView) findViewById(R.id.latArd);
    }

    private boolean checkLocation() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Su ubicación esta desactivada.\npor favor active su ubicación")
                .setPositiveButton("Configuración de ubicación", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void toggleGPSUpdates(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListenerGPS);
    }

    private final LocationListener locationListenerGPS = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudGPS = location.getLongitude();
            latitudGPS = location.getLatitude();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    longitudeValueGPS.setText(longitudGPS + "");
                    latitudeValueGPS.setText(latitudGPS + "");
                    Toast.makeText(MainActivity.this, "GPS Provider update", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };



    public void cambiar (final View view) throws IOException {

        longitudArd = (Double.parseDouble(longitudValorArd.getText().toString())); //Longitud Arduino
        latitudArd = Double.parseDouble(latitudValorArd.getText().toString());  //Latitud Arduino
        double radioTierra=6371; //KM
        double Lat = (latitudArd - latitudGPS) * (Math.PI / 180);
        double Lon = (longitudArd - longitudGPS) * (Math.PI / 180);
        double a = Math.sin(Lat / 2) * Math.sin(Lat / 2) + Math.cos(latitudGPS * (Math.PI / 180)) * Math.cos(latitudArd * (Math.PI / 180)) *  Math.sin(Lon / 2) * Math.sin(Lon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distancia = radioTierra * c;
        DecimalFormat formato= new DecimalFormat("#.##");
        distancia=Double.valueOf(formato.format(distancia));
        if(distancia>1)
            Toast.makeText(getBaseContext(),"Debe estar a no más de un kilometro, su distancia: " + distancia + " KM", Toast.LENGTH_LONG).show();
        else{
            Intent elegir = new Intent(getApplicationContext(), elegir_Lugar.class); //el intent elegir dice que desde el activ¡ty main inicia el activity elegir_lugar
            locationManager.removeUpdates(locationListenerGPS);
            startActivity(elegir); //inicia el activity
        }
    }
}
