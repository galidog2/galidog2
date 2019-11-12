package com.example.galidog2;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AjoutTrajetActivity extends AppCompatActivity {

    ArrayList<String> listeFichiers = new ArrayList<>();
    private Polyline polyline;
    KmlDocument kmlDocument = new KmlDocument();
    MapView map = null;
    private static final String TAG = "AjoutTrajetActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajout_trajet);


        //récupération du nom du trajet :
        if (getIntent().hasExtra("nouveaufichier")) {
            String nomFichier = getIntent().getStringExtra("nouveaufichier");
            Log.d(TAG, "onCreate: " + nomFichier);
        }

//        AlertDialogDemarrer();

        tracerPolyline();
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            GeoPoint geoPoint = new GeoPoint(latitude, longitude);

            String msg = "New Latitude: " + latitude + "\n New Longitude: " + longitude;
            Toast.makeText(AjoutTrajetActivity.this, msg, Toast.LENGTH_LONG).show();

            polyline.addPoint(geoPoint);
            map.getOverlays().add(polyline);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void arreterTrajet() {
        long delay = 20 * 1000;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                kmlDocument.mKmlRoot.addOverlay(polyline, kmlDocument);
                File localFile = kmlDocument.getDefaultPathForAndroid("my_route.kml");
                kmlDocument.saveAsKML(localFile);
                Toast.makeText(AjoutTrajetActivity.this, "Enregistré", Toast.LENGTH_SHORT).show();
            }
        }, delay);
    }

    private void tracerPolyline() {
        polyline = new Polyline(map);
        polyline.setWidth(8f);

        int minTime = 4000;
        int minDistance = 2;

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Nécessaire pour pas d'erreur mais degueulasse !
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener);
    }

    private void AlertDialogDemarrer() {

        // Un AlertDialog fonctionne comme une «mini-activité».
        // Il demande à l'utisateur une valeur, la renvoie à l'activité et s'éteint.
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Appuyez sur 'Démarrer' lorsque vous êstes prêt ?");
        // Cet AlertDialog comporte un bouton pour démarrer…
        alertDialogBuilder.setPositiveButton("Démarrer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Mettre en route l'enregistrement et afficher la carte aussi
                Intent intent = new Intent(AjoutTrajetActivity.this, LectureActivity.class);
                startActivity(intent);
            }
        });
        // … et un bouton pour annuler, qui arrête l'AlertDialog.
        alertDialogBuilder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(AjoutTrajetActivity.this, ChoixMemorisationActivity.class);
                //On retourne au choix des activités si on annule.
                startActivity(intent);
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
