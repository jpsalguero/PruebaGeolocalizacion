package com.example.mapa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button mBtnDireccion, mBtnRuta;
    public static String lcCoordenadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            locationStart();
        }

        mBtnDireccion = findViewById(R.id.button);
        mBtnRuta = findViewById(R.id.btnRuta);

        mBtnDireccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDireccion(lcCoordenadas);
            }
        });

        mBtnRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            //Se envían las coordenadas al Api específico (Google, Waze, Navegador).
            public void onClick(View view) {

                double latitude = 3.4747184; //lcCoordenadas.getLatitude();
                double longitude = -76.5884877; //lcCoordenadas.getLongitude();

                String url = "waze://?ll=" + latitude + ", " + longitude + "&navigate=yes";
                Intent intentWaze = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intentWaze.setPackage("com.waze");

                String uriGoogle = "google.navigation:q=" + latitude + "," + longitude;
                Intent intentGoogleNav = new Intent(Intent.ACTION_VIEW, Uri.parse(uriGoogle));
                intentGoogleNav.setPackage("com.google.android.apps.maps");

                String uriGoogle2 = "https://www.google.com/";
                Intent intentGoogleNav2 = new Intent(Intent.ACTION_VIEW, Uri.parse(uriGoogle2));

                String title = "Visualice la ruta con: ";
                Intent chooserIntent = Intent.createChooser(intentGoogleNav, title);
                Intent[] arr = {intentWaze, intentGoogleNav2};
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arr);
                startActivity(chooserIntent);
            }
        });
    }

    //Habilitar permiso de localización GPS.
    private void locationStart() {
        /*TextView mensaje1 = (TextView) findViewById(R.id.mensaje_id);
        TextView mensaje2 = (TextView) findViewById(R.id.mensaje_id2);*/

        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        Local.setMainActivity(this);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) Local);
        /*mensaje1.setText("Localización agregada");
        mensaje2.setText("");*/
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart();
                return;
            }
        }
    }

    //Se recibe el objeto loc para obtener la dirección.
    public String setLocation(Location loc) {

        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    lcCoordenadas = DirCalle.getAddressLine(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lcCoordenadas;
    }

    public void setDireccion(String sbDireccion) {
        TextView mensaje2 = findViewById(R.id.mensaje_id2);
        mensaje2.setText(sbDireccion);
    }

    /* Aqui empieza la Clase Localizacion */
    public class Localizacion implements LocationListener {
        MainActivity mainActivity;

        public MainActivity getMainActivity() {
            return mainActivity;
        }

        public void setMainActivity(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        /*Este método se ejecuta cada vez que el GPS recibe nuevas coordenadas
        debido a la detección de un cambio de ubicacion*/
        public void onLocationChanged(Location loc) {
            /*TextView mensaje1 = (TextView) findViewById(R.id.mensaje_id);
            loc.getLatitude();
            loc.getLongitude();
            lcCoordenadas = loc;
            mensaje1.setText(Text);
            */

            this.mainActivity.setLocation(loc);
        }

        @Override
        // Este metodo se ejecuta cuando el GPS es desactivado
        public void onProviderDisabled(String provider) {
            TextView mensaje1 = (TextView) findViewById(R.id.mensaje_id);
            mensaje1.setText("GPS Desactivado");
        }

        @Override
        // Este metodo se ejecuta cuando el GPS es activado
        public void onProviderEnabled(String provider) {
            TextView mensaje1 = (TextView) findViewById(R.id.mensaje_id);
            mensaje1.setText("GPS Activado");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }
}