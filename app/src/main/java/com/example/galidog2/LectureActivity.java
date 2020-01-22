package com.example.galidog2;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


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
    private Polyline trajet;
    private ArrayList<Marker> listeMarqueurs = new ArrayList<>();

    private ArrayList<CirclePlottingOverlay> listCircleEveil = new ArrayList<>();
    private ArrayList<CirclePlottingOverlay> listCircleValidation = new ArrayList<>();
    private int nombreCercle = 0;//Cet entier permet de suivre l'avancée dans les cercles
    private Button bt_check;
    private List<GeoPoint> mGeoPoints;
    private Marker depart;
    private ArrayList<Marker> indications = new ArrayList<>();

    private boolean displayedBefore = false;
    private boolean onGoing = false;
    private int distanceEveilMeters = 10;
    private int distanceTrajetMeters = 5;
    private int compteur = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //nécessaire pour osmdroid :
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        //récupération du nom du trajet :
        if (getIntent().hasExtra("nomfichier")) {
            nomFichier = getIntent().getStringExtra("nomfichier");
            Log.i("PMR", nomFichier);
        }

        //initialisation du toast :
        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        //vérification que la localisation a été activée :
        checkIfLocalisation(this);

        setContentView(R.layout.activity_map);
        switchMyLocation = findViewById(R.id.switchMyLocation);
        bt_check = findViewById(R.id.bt_check);
        switchMyLocation.setChecked(true);

        miseEnPlaceCarte();

        List<Overlay> overlays = kmlOverlay.getItems();
        trajet = new Polyline();
        int i;
        for (i = 0; i < overlays.size(); i++) {
            if (overlays.get(i) instanceof Polyline) {
                trajet = (Polyline) overlays.get(i);
            }
            if (overlays.get(i) instanceof Marker) {
                Marker marker = (Marker) overlays.get(i);
                if (marker.getTitle().equals("Départ")) {
                    depart = marker;
                } else {
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
        map.getOverlays().add(0, mapEventsOverlay);

        recupererFichier();

        tracerCercle();
        map.getOverlays().set(map.getOverlays().size() - 1, myLocationNewOverlay); //Localisation par dessus les cercles

        bt_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nombreCercle < listCircleValidation.size()) {
                    CheckCircleEveil(myLocationNewOverlay.getMyLocation(), nombreCercle);
                    CheckCircleValidation(myLocationNewOverlay.getMyLocation(), nombreCercle);
                    map.getOverlays().set(map.getOverlays().size() - 1, myLocationNewOverlay);
                }
            }
        });
        setLocalisationManager();
    }

    /**
     * Méthode tracant les cercles rouges d'éveil et de validation
     */
    private void tracerCercle() {
        for (int i = 0; i < listeMarqueurs.size(); i++) {
            createCircle(listeMarqueurs.get(i).getPosition());
        }
    }

    /**
     * Méthode pour récupérer les fichiers sauvegardés
     */
    private void recupererFichier() {
        List<Overlay> overlays = kmlOverlay.getItems();
        trajet = new Polyline();
        for (int i = 0; i < overlays.size(); i++) {
            if (overlays.get(i) instanceof Polyline) {
                trajet = (Polyline) overlays.get(i);
            } else if (overlays.get(i) instanceof Marker) {
                Marker marker = (Marker) overlays.get(i);
                listeMarqueurs.add(marker);
            }
        }
    }

    /**
     * Méthode pour créer les cercles d'Eveil et de Validation
     */

    private void createCircle(GeoPoint geoPoint) {
        //Cercle d'Eveil
        CirclePlottingOverlay cercle_eveil = new CirclePlottingOverlay(geoPoint, 8, listCircleEveil.size() + nombreCercle);
        cercle_eveil.drawCircle(map, Color.RED);
        listCircleEveil.add(cercle_eveil);
        map.getOverlays().add(0,cercle_eveil);

        //Cercle de Validation
        CirclePlottingOverlay cercle_validation = new CirclePlottingOverlay(geoPoint, 3, listCircleValidation.size() + nombreCercle);
        cercle_validation.drawCircle(map, Color.RED);
        listCircleValidation.add(cercle_validation);
        map.getOverlays().add(cercle_validation);
    }

    /**
     * Méthodes pour les cercles d'éveil et de validation
     */

    //Fonction pour changer la couleur du cercle d'Eveil
    private void ModifColorEveil(int n) {
        listCircleEveil.get(n).changeColor(map, Color.GREEN);
        map.getOverlays().set(0, listCircleEveil.get(n));
//        map.getOverlays().set(map.getOverlays().size() - 1, myLocationNewOverlay);
    }

    //Fonction pour changer la couleur du cercle de validation
    private void ModifColorValidation(int n) {
        listCircleValidation.get(n).changeColor(map, Color.GREEN);
        map.getOverlays().set(1, listCircleValidation.get(n));
//        map.getOverlays().set(map.getOverlays().size() - 1, myLocationNewOverlay);
    }

    //On check si on est dans le cercle d'éveil du point numéro n
    public void CheckCircleEveil(GeoPoint p, int numero) {
        double latitude = p.getLatitude();
        double longitude = p.getLongitude();
        Log.d(TAG, "onLocationChanged: test fonction changement couleur");
        if (Math.pow((latitude - listCircleEveil.get(numero).getLatitude()) * 111.11, 2) + Math.pow((longitude - listCircleEveil.get(numero).getLongitude()) * 111.11 * Math.cos(Math.toRadians(latitude)), 2) - Math.pow(listCircleEveil.get(numero).getRayon() / 1000, 2) < 0) {
            //On modifie la couleur
            ModifColorEveil(numero);
        }
    }

    //On check si on est dans le cercle de validation du point numéro n
    public void CheckCircleValidation(GeoPoint p, int numero) {
        double latitude = p.getLatitude();
        double longitude = p.getLongitude();
        if (Math.pow((latitude - listCircleValidation.get(numero).getLatitude()) * 111.11, 2) + Math.pow((longitude - listCircleValidation.get(numero).getLongitude()) * 111.11 * Math.cos(Math.toRadians(latitude)), 2) - Math.pow(listCircleValidation.get(numero).getRayon() / 1000, 2) < 0) {
            //On modifie la couleur
            ModifColorValidation(numero);
            if (listCircleValidation.size() > nombreCercle) {
                nombreCercle += 1;
            }
        }
    }

    private void checkIfLocalisation(final Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(context)
                    .setMessage("Veuillez activer la localisation pour démarrer le guidage.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Annuler", null).show();
        }
    }

    private void setLocalisationManager() {
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
            float distanceTrajetPixels = projection.metersToPixels(distanceTrajetMeters);
            float distanceEveilPixels = projection.metersToPixels(distanceEveilMeters);

            GeoPoint locationGeo = new GeoPoint(location.getLatitude(), location.getLongitude());

            if (accuracyMeters > distanceEveilMeters + distanceTrajetMeters) {
                toast.setText("Acquisition de la position en cours.");
                toast.show();
                return;
            }

            if (!onGoing) {
                double distance = depart.getPosition().distanceToAsDouble(locationGeo);
                if (distance < accuracyMeters) {
                    onGoing = true;
                    toast.setText("Vous êtes sur le point de départ. Démarrage du trajet." + depart.getSnippet());
                    toast.show();
                    ModifColorValidation(compteur);
                } else {
                    toast.setText("Placez vous sur le point de départ s'il vous plaît.");
                    ModifColorEveil(compteur);
                    toast.show();
                }
            } else {
                if (!trajet.isCloseTo(locationGeo, accuracyPixels + distanceTrajetPixels, map)) {
                    toast.setText("Revenez sur vos pas, vous vous éloignez du trajet.");
                    toast.show();
                } else {
                    double distance = indications.get(compteur).getPosition().distanceToAsDouble(locationGeo);

                    if (distance < accuracyMeters + 6) {
                        if (indications.get(compteur).getTitle().equals("Arrivée")) {
                            toast.setText("Vous arrivez dans 20m !");
                            toast.show();
                            ModifColorEveil(compteur+1);
                        }
                        if (!displayedBefore) {
                            toast.setText("Éveil du " + indications.get(compteur).getTitle());
                            toast.show();
                            ModifColorEveil(compteur+1);
                            displayedBefore = true;
                        }
                    }
                    if (distance < accuracyMeters + 2) {
                        if (indications.get(compteur).getTitle().equals("Arrivée")) {
                            toast.setText("Vous êtes arrivés !");
                            toast.show();
                            ModifColorValidation(compteur+1);
                        } else {
                            toast.setText(indications.get(compteur).getSnippet());
                            toast.show();
                            ModifColorValidation(compteur+1);
                            compteur++;
                            displayedBefore = false;
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
     *
     * @param overlays la liste des overlays
     */
    private void miseEnPlaceKmlOverlay(List<Overlay> overlays) {
        KmlDocument kmlToRead = new KmlDocument();
        String path = Environment.getExternalStorageDirectory().toString() + "/osmdroid/kml/" + nomFichier + ".kml";
        File fichier = new File(path);
        kmlToRead.parseKMLFile(fichier);
        kmlOverlay = (FolderOverlay) kmlToRead.mKmlRoot.buildOverlay(map, null, null, kmlToRead);
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
