package com.example.galidog2;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Activité qui permet l'enregistrement d'un nouveau trajet
 */
public class AjoutTrajetActivity extends AppCompatActivity implements MapEventsReceiver {

    private CheckBox bouton_pause;
    private Button bouton_arret;
    private Button bouton_marker;
    private int numero_marker = 1;
    MapView map = null; // La vue de la map
    private MyLocationNewOverlay myLocationNewOverlay;
    private Switch switchMyLocation; // permet d'activer ou de désactiver l'affichage de la position
    private int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION;
    private int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
    private String nomFichier;
    private GeoPoint dernierPoint;
    private ArrayList<Marker> listeMarqueurs = new ArrayList<>();

    private String MY_USERAGENT = "com.beview.mygeoapp";

    private Polyline polyline;
    KmlDocument kmlDocument = new KmlDocument();
    private static final String TAG = "AjoutTrajetActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajout_trajet);

        //Garder l'écran allumé pour pouvoir enregistrer en continu
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //nécessaire pour osmdroid :
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));


        //récupération du nom du trajet :
        if (getIntent().hasExtra("nouveaufichier")) {
            nomFichier = getIntent().getStringExtra("nouveaufichier");
            Log.d(TAG, "onCreate: " + nomFichier);
        }

        bouton_pause = findViewById(R.id.cb_pause);
        bouton_arret = findViewById(R.id.bt_arret);
        bouton_marker = findViewById(R.id.bt_marker);
        switchMyLocation = findViewById(R.id.switchMyLocation);
        miseEnPlaceCarte();

        //vérification si la localisation est activée
        checkIfLocalisation(this);

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay((MapEventsReceiver) this);
        map.getOverlays().add(0, mapEventsOverlay);

        switchMyLocation.setChecked(true);
        AlertDialogDemarrer();

        tracerPolyline();

        /**
         * Bouton 'Arrêt' pour arrêter et enregistrer le  trajet :
         */
        bouton_arret.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //On trace le marqueur d'arrivée
                tracerMarqueur("Arrivée");
                //On enregistre polyline et marqueurs
                enregistrerTrajet();

                Intent intent = new Intent(AjoutTrajetActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        bouton_marker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //On trace le marqueur
                tracerMarqueur("Point " + numero_marker);
                numero_marker = numero_marker + 1;

                // Trouver l'adresse du marker pour le mettre en description de marker
                // EDIT: on ne l'utilise pas tant qu'on n'a pas résolu le problème
                //trouverAdresse(dernierPoint);
            }
        });
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            if (!bouton_pause.isChecked()) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                dernierPoint = new GeoPoint(latitude, longitude);
//                GeoPoint geoPoint = new GeoPoint(latitude, longitude);

                polyline.addPoint(dernierPoint);
                map.getOverlays().add(polyline);
                map.getController().animateTo(dernierPoint);

                if (polyline.getPoints().size() == 1) {
                    tracerMarqueur("Départ");
                }
            }
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

    private void checkIfLocalisation(Context context) {
        if (isLocationEnabled(context)) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Veuillez activer la localisation");
            alertDialogBuilder.setMessage("La localisation est nécessaire pour enregistrer un trajet").setCancelable(false);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            alertDialog.dismiss();
        }
    }

    public static Boolean isLocationEnabled(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            // This is Deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != Settings.Secure.LOCATION_MODE_OFF);

        }
    }

    private void trouverAdresse(GeoPoint geoPoint) {
        // Reverse Geocoding
        GeocoderNominatim geocoder = new GeocoderNominatim(MY_USERAGENT);
        String theAddress;

        try {
            List<Address> addresses = geocoder.getFromLocation(geoPoint.getLatitude(), geoPoint.getLongitude(), 1);
            StringBuilder sb = new StringBuilder();
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                int n = address.getMaxAddressLineIndex();
                Log.d("Test", "CountryName: " + address.getCountryName());
                Log.d("Test", "CountryCode: " + address.getCountryCode());
                Log.d("Test", "PostalCode " + address.getPostalCode());
//                        Log.d("Test", "FeatureName " + address.getFeatureName()); //null
                Log.d("Test", "City: " + address.getAdminArea());
                Log.d("Test", "Locality: " + address.getLocality());
                Log.d("Test", "Premises: " + address.getPremises()); //null
                Log.d("Test", "SubAdminArea: " + address.getSubAdminArea());
                Log.d("Test", "SubLocality: " + address.getSubLocality());
//                        Log.d("Test", "SubThoroughfare: " + address.getSubThoroughfare()); //null
//                        Log.d("Test", "getThoroughfare: " + address.getThoroughfare()); //null
                Log.d("Test", "Locale: " + address.getLocale());
                for (int i = 0; i <= n; i++) {
                    if (i != 0)
                        sb.append(", ");
                    sb.append(address.getAddressLine(i));
                }
                theAddress = sb.toString();
            } else {
                theAddress = null;
            }
            Toast.makeText(this, theAddress, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            theAddress = null;
        }
        if (theAddress != null) {
            Log.d("Test", "Address: " + theAddress);
        }
    }

    private void tracerMarqueur(String titre) {
        Marker marker = new Marker(map);
        marker.setPosition(dernierPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(getDrawable(R.drawable.marqueur));
        marker.setSubDescription("Description possible");
        marker.setSnippet("Adresse ?");
        marker.setTitle(titre);
        listeMarqueurs.add(marker);
        map.getOverlays().add(marker);
    }

    /**
     * Demande des permissions au cas où
     */
    private void demandePermissionsLocalisation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    /**
     * Mise en place de la carte
     */
    private void miseEnPlaceCarte() {
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(true);
        final List<Overlay> overlays = map.getOverlays();
        ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(map);
        overlays.add(mScaleBarOverlay);
        miseEnPlaceMyLocationOverlay();

        polyline = new Polyline(map);
    }

    /**
     * Mise en place de l'overlay avec la position de l'utilisateur,
     * Mise en place de l'interrupteur pour activer ou non l'overlay
     */
    private void miseEnPlaceMyLocationOverlay() {
        myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        myLocationNewOverlay.enableMyLocation();
        map.getOverlays().add(myLocationNewOverlay);
        //Bouton 'Ma Localisation' suivie ou non
        switchMyLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    demandePermissionsLocalisation();
                    myLocationNewOverlay.enableFollowLocation();
                    map.getController().animateTo(myLocationNewOverlay.getMyLocation());
                } else {
                    myLocationNewOverlay.disableFollowLocation();
                }
            }
        });
        map.getOverlays().add(myLocationNewOverlay);

        IMapController mapController = map.getController();
        mapController.setZoom((double) 20);
        mapController.setCenter(new GeoPoint(50.636895, 3.063444));
    }

    /**
     * Permet d'enregistrer le trajet
     */
    private void enregistrerTrajet() {
        kmlDocument.mKmlRoot.addOverlay(polyline, kmlDocument);
        kmlDocument.mKmlRoot.addOverlays(listeMarqueurs, kmlDocument);

        File localFile = cheminStockage(nomFichier + ".kml");
        kmlDocument.saveAsKML(localFile);
        Toast.makeText(AjoutTrajetActivity.this, "Enregistré", Toast.LENGTH_SHORT).show();
    }

    /**
     * Permet d'avoir le chemin jusqu'au dossier de stockage
     *
     * @param fileName
     * @return
     */
    private File cheminStockage(String fileName) {
        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/osmdroid/kml";
            File directory = new File(path);
            directory.mkdir();
            return new File(directory.getAbsolutePath(), fileName);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tracer le trajet en suivant le listener de la localisation
     */
    private void tracerPolyline() {
        polyline.setWidth(8f);

        int minTime = 2000;
        int minDistance = 2;

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Nécessaire pour pas d'erreur mais degueulasse !
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener);
    }

    /**
     * Message demandant la confirmation avant de commencer l'enregistrement
     * La localisation doit etre activée dans les paramètres ...
     */
    private void AlertDialogDemarrer() {
        final LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.prompt, null);

        final AlertDialog alertD = new AlertDialog.Builder(this).create();

        FloatingActionButton btnPlay = (FloatingActionButton) promptView.findViewById(R.id.play);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertD.dismiss();
            }
        });

        alertD.setView(promptView);

        alertD.show();
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }
}
