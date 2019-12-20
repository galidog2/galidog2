package com.example.galidog2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;


/**
 * Activity générant la carte pour se diriger
 */
public class LectureActivity extends AppCompatActivity implements MapEventsReceiver {

    private static final String TAG = "LectureActivity";

    private Toast toast;
    MapView map = null; // La vue de la map
    private MyLocationNewOverlay myLocationNewOverlay;
    private Switch switchMyLocation; // permet d'activer ou de désactiver l'affichage de la position
    private Location location;

    private int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION;
    private int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

    private String nomFichier;
    private FolderOverlay kmlOverlay;
    private List<GeoPoint> mGeoPoints;
    private Polyline trajet;
    private Marker depart;
    private ArrayList<Marker> indications = new ArrayList<>();

    private boolean onGoing = false;
    private int distanceEveilMeter = 20;
    private int compteur=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //nécessaire pour osmdroid :
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        //récupération du nom du trajet :
        if (getIntent().hasExtra("nomfichier")){
            nomFichier = getIntent().getStringExtra("nomfichier");
            Log.i("PMR",nomFichier);
        }

        //initialisation du toast :
        toast = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);

        //vérification que la localisation a été activée :
        checkIfLocalisation(this);

        setContentView(R.layout.activity_map);
        switchMyLocation = findViewById(R.id.switchMyLocation);
        switchMyLocation.setChecked(true);
        miseEnPlaceCarte();

        List<Overlay> overlays = kmlOverlay.getItems();
        trajet = new Polyline();
        int i;
        for (i= 0 ; i<overlays.size(); i++)
        {
            if(overlays.get(i) instanceof Polyline){
                trajet = (Polyline)overlays.get(i);
            }
            if (overlays.get(i) instanceof Marker){
                Marker marker = (Marker)overlays.get(i);
                if (marker.getTitle().equals("Départ")){
                    depart = marker;
                }
                else{
                    indications.add(marker);
                }
            }
        }
        mGeoPoints = trajet.getPoints();
        map.post(new Runnable() {
            @Override
            public void run() {
                final BoundingBox boundingBox = BoundingBox.fromGeoPoints(mGeoPoints);
                map.zoomToBoundingBox(boundingBox, false, 30);
            }
        });

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
        map.getOverlays().add(0,mapEventsOverlay);

        setLocalisationManager();

    }

    private void checkIfLocalisation(Context context) {
        if (isLocationEnabled(context)) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Veuillez activer la localisation");
            alertDialogBuilder.setMessage("La localisation est nécessaire pour démarrer le guidage").setCancelable(false);
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

    private void setLocalisationManager(){
        int minTime = 4000;
        int minDistance = 4;

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Nécessaire pour pas d'erreur mais degueulasse !
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener);
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {

            // Projection pour convertir la précision (reçue en mètres) en pixels.
            Projection projection = map.getProjection();
            float accuracyMeters = location.getAccuracy();
            float accuracyPixels = projection.metersToPixels(accuracyMeters);
            float distanceEveilPixel = projection.metersToPixels(distanceEveilMeter);

            GeoPoint locationGeo = new GeoPoint(location.getLatitude(), location.getLongitude());

            if (accuracyMeters>distanceEveilMeter){
                toast.setText("Acquisition de la position en cours.");
                toast.show();
                return;
            }

            // Bug au niveau des toasts. Le départ s'affiche un nombre incalculable de fois.
            if(!onGoing) {
                double distance = depart.getPosition().distanceToAsDouble(locationGeo);
                if(distance<accuracyMeters){
                    onGoing = true;
                    toast.setText("Vous êtes sur le point de départ. Démarrage du trajet.");
                    toast.show();
                }

                else{
                    toast.setText("Placez vous sur le point de départ s'il vous plaît.");
                    toast.show();
                }
            }

            else{
                if(!trajet.isCloseTo(locationGeo,accuracyPixels, map)){
                    toast.setText("Revenez sur vos pas, vous vous éloignez du trajet.");
                    toast.show();
                }
                else{
                    double distance = indications.get(compteur).getPosition().distanceToAsDouble(locationGeo);
                    if (distance<accuracyMeters+distanceEveilPixel){
                        if (indications.get(compteur).getTitle().equals("Arrivée")) {
                            toast.setText("Vous arrivez dans 20m !");
                            toast.show();
                        }
                        toast.setText("Éveil du " + indications.get(compteur).getTitle());
                        toast.show();
                    }
                    else if (distance<accuracyMeters){
                        if (indications.get(compteur).getTitle().equals("Arrivée")) {
                            toast.setText("Vous êtes arrivés !");
                            toast.show();
                        }
                        else{
                            toast.setText("Indication : ");
                            toast.show();
                            compteur++;
                        }
                    }
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
     * Mise en place de la carte du monde
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

        miseEnPlaceKmlOverlay(overlays);
    }

    /**
     * Méthode pour afficher un trajet
     * @param overlays la liste des overlays
     */
    private void miseEnPlaceKmlOverlay(List<Overlay> overlays) {
        KmlDocument kmlToRead = new KmlDocument();
        String path = Environment.getExternalStorageDirectory().toString()+ "/osmdroid/kml/"+nomFichier+".kml";
        File fichier = new File(path);
        kmlToRead.parseKMLFile(fichier);
        kmlOverlay = (FolderOverlay)kmlToRead.mKmlRoot.buildOverlay(map, null, null, kmlToRead);
        overlays.add(kmlOverlay);
        map.invalidate();
    }

    /**
     * Mise en place de l'overlay avec la position de l'utilisateur,
     * Mise en place de l'interrupteur pour activer ou non l'overlay
     */
    private void miseEnPlaceMyLocationOverlay() {
        myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        myLocationNewOverlay.disableMyLocation();
        map.getOverlays().add(myLocationNewOverlay);
        //Bouton 'Ma Localisation' on/off
        switchMyLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    demandePermissionsLocalisation();
                    myLocationNewOverlay.enableMyLocation();
                    myLocationNewOverlay.enableFollowLocation();
                    map.getController().animateTo(myLocationNewOverlay.getMyLocation());
                } else {
                    myLocationNewOverlay.disableMyLocation();
                    myLocationNewOverlay.disableFollowLocation();
                }
            }
        });
        map.getOverlays().add(myLocationNewOverlay);
    }

    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    /**
     * Permet de récupérer un bitmap à partir d'un drawable.
     * Sert à convertir l'icon pour placer l'utilisateur
     *
     * @param drawableRes : drawable à convertir
     * @return : le bitmap corespondant
     */
    private Bitmap recupererBitmap(int drawableRes) {
        Drawable drawable = getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        assert drawable != null;
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
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
