package com.example.galidog2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
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

/**
 * Activité qui permet l'enregistrement d'un nouveau trajet
 */
public class AjoutTrajetActivity extends AppCompatActivity implements MapEventsReceiver {

    private static final double rayon = 6371; // km
    /**
     * Attributs
     */
    private VoiceOut voiceOut = null;
    private boolean demarrerPosition = false;
    private CheckBox bouton_pause;
    private Button bouton_arret;
    private Button bouton_cercle;//Bouton pour dessiner un cercle
    //    private int numero_marker = 1;
    MapView map = null; // La vue de la map
    private MyLocationNewOverlay myLocationNewOverlay;
    private Switch switchMyLocation; // permet d'activer ou de désactiver l'affichage de la position
    private int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION;
    private int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
    private String nomFichier;
    private GeoPoint dernierPoint;
    private ArrayList<Marker> listeMarqueurs = new ArrayList<>();

    private String MY_USERAGENT = "Galidog2";
    private Address adresse;
    private String st_adresse;
    private List<Address> listeAdresses = null;

    private ArrayList<CirclePlottingOverlay> listCircleEveil = new ArrayList<>();
    private ArrayList<CirclePlottingOverlay> listCircleValidation = new ArrayList<>();
    private int nombreCercle = 0;//Cet entier permet de suivre l'avancée dans les cercles

    private Polyline polyline;
    KmlDocument kmlDocument = new KmlDocument();
    private static final String TAG = "AjoutTrajetActivity";

    private ArrayList<Double> distanceSup = new ArrayList<>();//Nécessaire pour le calcul
    private ArrayList<Double> distance = new ArrayList<>();
    private ArrayList<Double> information = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        voiceOut = new VoiceOut(this);
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
        bouton_cercle = findViewById(R.id.bt_cercle);
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

                //Calcul et entrée des infos sur les markers
                ajoutInfoMarker();

                //On enregistre polyline et marqueurs
                enregistrerTrajet();

                Intent intent = new Intent(AjoutTrajetActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        bouton_cercle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCircle(dernierPoint);
                tracerMarqueur("Point n°: " + nombreCercle);
//                trouverAdresse(dernierPoint); //Trouver l'adresse du marker pour le mettre en description de marker
            }
        });
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            if (location.getAccuracy() > 15 && polyline.getPoints().size() == 0 && demarrerPosition == false) {
                voiceOut.speak("Acquisition de la position, veuillez patienter");

            } else {
                if ((!bouton_pause.isChecked()) && demarrerPosition == true) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    dernierPoint = new GeoPoint(latitude, longitude);

                    polyline.addPoint(dernierPoint);
                    map.getOverlays().add(polyline);
                    map.getController().animateTo(dernierPoint);

                    if (polyline.getPoints().size() == 1) {
                        tracerMarqueur("Départ");
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

    private void checkIfLocalisation(Context context) {
        if (isLocationEnabled(context)) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            //TODO:à vocaliser ?
            alertDialogBuilder.setTitle("Veuillez activer la localisation");
            voiceOut.speak("Veuillez activer la localisation");
            alertDialogBuilder.setMessage("La localisation est nécessaire pour enregistrer un trajet").setCancelable(false);
            voiceOut.speak("La localisation est nécessaire pour enregistrer un trajet");
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            alertDialog.dismiss();
        }
    }

    public static Boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            // This is Deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);

        }
    }

    /**
     * Fonction pour créer les cercles d'Eveil et de Validation
     */

    private void createCircle(GeoPoint geoPoint) {
        //Cercle d'Eveil
        CirclePlottingOverlay cercle_eveil = new CirclePlottingOverlay(geoPoint, 8, nombreCercle);
        cercle_eveil.drawCircle(map, Color.RED);
        listCircleEveil.add(cercle_eveil);
        map.getOverlays().add(cercle_eveil);

        //Cercle de Validation
        CirclePlottingOverlay cercle_validation = new CirclePlottingOverlay(geoPoint, 3, nombreCercle);
        cercle_validation.drawCircle(map, Color.RED);
        map.getOverlays().add(cercle_validation);
        listCircleValidation.add(cercle_validation);

        nombreCercle = nombreCercle + 1;
    }

    @SuppressLint("StaticFieldLeak")
    private void trouverAdresse(GeoPoint geoPoint) {
        // Retreive Geocoding data (add this code to an event click listener on a button)
        new AsyncTask<String, Void, List<Address>>() {
            @Override
            protected List<Address> doInBackground(String... strings) {
//                 Reverse Geocoding
                GeocoderNominatim geocoder = new GeocoderNominatim(MY_USERAGENT);

                try {
                    listeAdresses = geocoder.getFromLocation(50.605965, 3.137047, 1);
                    adresse = listeAdresses.get(0);

                    StringBuilder sb = new StringBuilder();
                    if (listeAdresses.size() > 0) {
                        Address address = listeAdresses.get(0);
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
                        st_adresse = sb.toString();
                        Toast.makeText(AjoutTrajetActivity.this, "Adresse : " + st_adresse, Toast.LENGTH_SHORT).show();
                    } else {
                        adresse = null;
                        Toast.makeText(AjoutTrajetActivity.this, "Echec ...", Toast.LENGTH_SHORT).show();
                    }
                    return listeAdresses;
                } catch (IOException e) {
                    adresse = null;
                }
                if (adresse != null) {
                    Log.d("Test", "Adresse: " + st_adresse);
                }
                return null;
            }
//            @Override
//            protected List<Address> doInBackground(String... strings) {
//                GeocoderNominatim geocoder = new GeocoderNominatim(MY_USERAGENT);
//                try {
//                    listeAdresses = geocoder.getFromLocation(startPoint.getLatitude(),startPoint.getLongitude(), 1);
//                    Toast.makeText(AjoutTrajetActivity.this, "Adresse : "+listeAdresses.get(0), Toast.LENGTH_SHORT).show();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Toast.makeText(AjoutTrajetActivity.this, "Geocoding error! Internet available?", Toast.LENGTH_SHORT).show();
//                }
//                return listeAdresses;
//            }

//            @Override
//            protected Void doInBackground(Void... voids) {
//                // Reverse Geocoding
//                GeocoderNominatim geocoder = new GeocoderNominatim(MY_USERAGENT);
//
//                try {
//                    List<Address> addresses = geocoder.getFromLocation(startPoint.getLatitude(), startPoint.getLongitude(), 1);
//
//                    adresse = addresses.get(0);
////                    StringBuilder sb = new StringBuilder();
////                    if (addresses.size() > 0) {
////                        Address address = addresses.get(0);
////                        int n = address.getMaxAddressLineIndex();
////                        Log.d("Test", "CountryName: " + address.getCountryName());
////                        Log.d("Test", "CountryCode: " + address.getCountryCode());
////                        Log.d("Test", "PostalCode " + address.getPostalCode());
//////                        Log.d("Test", "FeatureName " + address.getFeatureName()); //null
////                        Log.d("Test", "City: " + address.getAdminArea());
////                        Log.d("Test", "Locality: " + address.getLocality());
////                        Log.d("Test", "Premises: " + address.getPremises()); //null
////                        Log.d("Test", "SubAdminArea: " + address.getSubAdminArea());
////                        Log.d("Test", "SubLocality: " + address.getSubLocality());
//////                        Log.d("Test", "SubThoroughfare: " + address.getSubThoroughfare()); //null
//////                        Log.d("Test", "getThoroughfare: " + address.getThoroughfare()); //null
////                        Log.d("Test", "Locale: " + address.getLocale());
////                        for (int i = 0; i <= n; i++) {
////                            if (i != 0)
////                                sb.append(", ");
////                            sb.append(address.getAddressLine(i));
////                        }
////                        adresse = sb.toString();
////                    } else {
////                        adresse = null;
////                    }
//                    Toast.makeText(AjoutTrajetActivity.this, "Adresse : " + adresse, Toast.LENGTH_SHORT).show();
//                } catch (IOException e) {
//                    adresse = null;
//                }
//                if (adresse != null) {
//                    Log.d("Test", "Adresse: " + adresse);
//                }
//                return null;
//            }
        }.execute();
    }

    /**
     * Ajout de distance au marqueur
     */

    private void ajoutInfoMarker() {
        construction();//On calcule les infos de distance et d'orientation
        for (int i = 0; i < listeMarqueurs.size(); i++) {
            if (i == 0 && listeMarqueurs.size() <= 2) { //Distance + prochaine orientation
                listeMarqueurs.get(i).setSnippet("Marchez sur " + distance.get(i)
                        + " mètres");
            } else if (i == 0) { //Distance + prochaine orientation
                listeMarqueurs.get(i).setSnippet("Marchez sur " + distance.get(i)
                        + " mètres, puis tournez à " + information.get(i) + " heures");
            } else if (i < listeMarqueurs.size() - 2) {
                listeMarqueurs.get(i).setSnippet("Tournez à " + information.get(i - 1)
                        + "heures, marchez sur " + distance.get(i)
                        + " mètres, puis tournez à " + information.get(i) + " heures");
            } else if (i == listeMarqueurs.size() - 2) {
                listeMarqueurs.get(i).setSnippet("Tournez à " + information.get(i - 1)
                        + "heures, marchez sur " + distance.get(i));
            } else if (i == listeMarqueurs.size() - 1) {
                listeMarqueurs.get(i).setSnippet("Arrivée");
            }

        }
    }

    /**
     * Bouton (plus tard remplacé par une information vocale pour ordonner le calcul des informations
     * Détermination de la distance entre les points (listes d'entier qui comprendra les distances, l'indice i sera la distance entre le point i et le point i+1)
     * Détermination de l'information vers le point suivant (liste de string qui comprendra les informations horaires, ATTENTION il y aura besoin d'une liste des distance entre les points i et i+2 pour calculer les infos
     */

    private void construction() {
        Log.i(TAG, "construction: test1: avant boucle for");
        for (int i = 0; i < listeMarqueurs.size() - 1; i++) {
            Log.i(TAG, "construction: test1 : dans boucle for");
            if (i + 1 != listeMarqueurs.size()) {
                distance.add(calculDistance(i, i + 1));
            }
            if (i + 2 != listeMarqueurs.size()) {
                distanceSup.add(calculDistance(i, i + 2));
                information.add(calculInformation(i));
            }
        }
    }

    public double calculDistance(int i, int j) {
        double latitudei = listeMarqueurs.get(i).getPosition().getLatitude();
        double longitudei = listeMarqueurs.get(i).getPosition().getLongitude();
        double latitudej = listeMarqueurs.get(j).getPosition().getLatitude();
        double longitudej = listeMarqueurs.get(j).getPosition().getLongitude();
        double x = (longitudej - longitudei) * Math.cos((latitudei + latitudej) / 2);
        double y = (latitudej - latitudei);
        double distance = Math.sqrt(x * x + y * y) * rayon;
        if (distance == 0) {
            return 0;
        }
        return Math.ceil(distance * 10);
    }

    public double calculInformation(int i) {
        double d1 = calculDistance(i, i + 1);
        double d2 = calculDistance(i + 1, i + 2);
        double d3 = calculDistance(i, i + 2);

        //Calcul de l'angle entre les 2 segments
        double gamma = calculAngle(d1, d2, d3);

        if (facteurDirection(i) > 0) {
            return 6 - Math.floor(6 * gamma / Math.PI);
        }
        return 6 + Math.floor(6 * gamma / Math.PI);

    }


    //Fonction pour calculer l'angle entre 2 segments
    public double calculAngle(double d1, double d2, double d3) {
        return Math.acos((Math.pow(d1, 2) + Math.pow(d2, 2) - Math.pow(d3, 2)) / (2 * d1 * d2));
    }

    //Fonction pour calculer la pente
    public double facteurDirection(int i) {
        double latitudei = listeMarqueurs.get(i).getPosition().getLatitude();
        double longitudei = listeMarqueurs.get(i).getPosition().getLongitude();
        double latitudej = listeMarqueurs.get(i + 1).getPosition().getLatitude();
        double longitudej = listeMarqueurs.get(i + 1).getPosition().getLongitude();
        double latitudeh = listeMarqueurs.get(i + 2).getPosition().getLatitude();
        double longitudeh = listeMarqueurs.get(i + 2).getPosition().getLongitude();

        return (latitudej - latitudei) * (longitudeh - longitudei) - (longitudej - longitudei) * (latitudeh - latitudei);
    }


    /**
     * Trace un marqueur
     */

    private void tracerMarqueur(String titre) {
        Marker marker = new Marker(map);
        marker.setPosition(dernierPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(getDrawable(R.drawable.marqueur));
        marker.setSubDescription("Description possible");//TODO : Utiliser trouverAdresse() ici
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
        voiceOut.speak("Construction du trajet terminée, trajet enregistré");
//        Toast.makeText(AjoutTrajetActivity.this, "Trajet enregistré", Toast.LENGTH_SHORT).show();
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

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Appuyez sur 'Démarrer' lorsque vous êtes prêt");
        alertDialogBuilder.setPositiveButton("Démarrer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                demarrerPosition = true;
            }
        });
        alertDialogBuilder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(AjoutTrajetActivity.this, ChoixMemorisationActivity.class);
                startActivity(intent);
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

//        final LayoutInflater layoutInflater = LayoutInflater.from(this);
//        View promptView = layoutInflater.inflate(R.layout.prompt, null);
//
//        final AlertDialog alertD = new AlertDialog.Builder(this).create();
//
//        FloatingActionButton btnPlay = (FloatingActionButton) promptView.findViewById(R.id.play);
//
//        btnPlay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                alertD.dismiss();
//            }
//        });
//
//        alertD.setView(promptView);
//
//        alertD.show();
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

