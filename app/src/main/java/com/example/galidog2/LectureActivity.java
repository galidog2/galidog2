package com.example.galidog2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

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
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

//TODO : gérer la navigation (se baser sur l'exemple OSMNavigator).

/**
 * Activity générant la carte pour se diriger
 */
public class LectureActivity<ListCircleEveil> extends AppCompatActivity implements MapEventsReceiver {

    private static final String TAG = "LectureActivity";

    MapView map = null; // La vue de la map
    private MyLocationNewOverlay myLocationNewOverlay;
    private Switch switchMyLocation; // permet d'activer ou de désactiver l'affichage de la position
    private int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION;
    private int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
    private String nomFichier;
    //Liste des points à marquer
    List<IGeoPoint> points = new ArrayList<>();

    //Boutton pour dessiner un cercle
    private Button Circle;
    //Boutton pour checker si on est dans un cercle
    private Button CheckPoint;

    //Liste les CirclePlottingOverlay (les cercles)
    private ArrayList<CirclePlottingOverlay> ListCircleEveil = new ArrayList<>();
    private ArrayList<CirclePlottingOverlay> ListCircleValidation = new ArrayList<>();


    //Se valide si on est dans le cercle de validation
    private Switch CheckValidation;
    //Se valide si on est dans le cercle d'éveil
    private Switch CheckEveil;


    //Cet entier permet de suivre l'avancée dans les cercles.
    private int NombreCercle=0;



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

        setContentView(R.layout.activity_map);
        switchMyLocation = findViewById(R.id.switchMyLocation);
        miseEnPlaceCarte();

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
        map.getOverlays().add(0,mapEventsOverlay);
        myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);

        Circle = findViewById(R.id.DrawCircle);
        Circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCircle();
            }
        });


        CheckPoint = findViewById(R.id.CheckPoint);
        CheckPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NombreCercle<ListCircleValidation.size()) {
                    CheckCircleEveil(myLocationNewOverlay.getMyLocation(), NombreCercle);
                    CheckCircleValidation(myLocationNewOverlay.getMyLocation(), NombreCercle);
                }
            }
        });

    }


    private void navigation(){
    }


    //Fonction pour créer les cercles d'Eveil et de Validation

    private void createCircle(){
                //Cercle d'Eveil
                CirclePlottingOverlay cercle_eveil = new CirclePlottingOverlay(myLocationNewOverlay.getMyLocation(), 8, NombreCercle);
                cercle_eveil.drawCircle(map, Color.RED);
        ListCircleEveil.add(cercle_eveil);
                map.getOverlays().add(cercle_eveil);

                //Cercle de Validation
                CirclePlottingOverlay cercle_validation = new CirclePlottingOverlay(myLocationNewOverlay.getMyLocation(), 3, NombreCercle);
                cercle_validation.drawCircle(map, Color.RED);
                map.getOverlays().add(cercle_validation);
                ListCircleValidation.add(cercle_validation);

            }


    //Fonction pour changer la couleur du cercle d'Eveil
    private void ModifColorEveil(int n){ ListCircleEveil.get(n).changeColor(map, Color.GREEN); }

    //Fonction pour changer la couleur du cercle de validation
    private void ModifColorValidation(int n){ ListCircleValidation.get(n).changeColor(map, Color.GREEN); }


    //On check si on est dans le cercle d'éveil du point n


            public void CheckCircleEveil(GeoPoint p, int n) {
                double latitude = p.getLatitude();
                double longitude = p.getLongitude();
                Log.d(TAG, "onLocationChanged: test fonction changement couleur");
                if (Math.pow((latitude - ListCircleEveil.get(n).getLatitude())*111.11, 2) + Math.pow((longitude - ListCircleEveil.get(n).getLongitude())*111.11*Math.cos(Math.toRadians(latitude)), 2) - Math.pow(ListCircleEveil.get(n).getRayon()/1000, 2) < 0) {
                    //On modifie la couleur
                    ModifColorEveil(n);
                }
            }


    //On check si on est dans le cercle de validation du point n


            public void CheckCircleValidation(GeoPoint p, int n) {
                double latitude = p.getLatitude();
                double longitude = p.getLongitude();
                if (Math.pow((latitude - ListCircleValidation.get(n).getLatitude())*111.11, 2) + Math.pow((longitude - ListCircleValidation.get(n).getLongitude())*111.11*Math.cos(Math.toRadians(latitude)), 2) - Math.pow(ListCircleValidation.get(n).getRayon()/1000, 2) < 0) {
                    //On modifie la couleur
                    ModifColorValidation(n);
                    if (ListCircleValidation.size()>NombreCercle){
                        NombreCercle+=1;
                    }
                }
            }




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

        /**
         * Ajout de marqueurs
         * Ce sont des exemples, mais ca fonctionne
         */
        /*ajoutMarqueur(50.637687, 3.064494, "Beffroi");//Beffroi
        ajoutMarqueur(50.605965, 3.137047, "Centrale");//Centrale
        ajoutMarqueur(50.636895, 3.063444, "Grand'Place");//Grand'Place
        ajoutMarqueur(50.605476, 3.139046, "4 Cantons");//4Cantons*/
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
        FolderOverlay kmlOverlay = (FolderOverlay)kmlToRead.mKmlRoot.buildOverlay(map, null, null, kmlToRead);
        overlays.add(kmlOverlay);
        map.invalidate();

        IMapController mapController = map.getController();
        mapController.setZoom((double) 15); //valeur à adapter en fonction de l'itinéraire
        BoundingBox bb = kmlToRead.mKmlRoot.getBoundingBox();
        mapController.setCenter(bb.getCenterWithDateLine());
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

    public void ajoutMarqueur(double latitude, double longitude, String text) {

        //création de plein de points jusqu'a 100k easy
        points.add(new LabelledGeoPoint(latitude, longitude, text));
        // wrap them in a theme
        SimplePointTheme pt = new SimplePointTheme(points, true);

        // create label style
        Paint textStyle = new Paint();
        textStyle.setStyle(Paint.Style.FILL);//Que l'intérieur du texte
        textStyle.setColor(Color.parseColor("#0000ff"));//Bleu
        textStyle.setTextAlign(Paint.Align.CENTER);
        textStyle.setTextSize(20);

        // set some visual options for the overlay
        // we use here MAXIMUM_OPTIMIZATION algorithm, which works well with >100k points
        SimpleFastPointOverlayOptions opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
                .setRadius(7).setIsClickable(true).setCellSize(15).setTextStyle(textStyle);

        // create the overlay with the theme
        final SimpleFastPointOverlay sfpo = new SimpleFastPointOverlay(pt, opt);

        // onClick callback
        sfpo.setOnClickListener(new SimpleFastPointOverlay.OnClickListener() {
            @Override
            public void onClick(SimpleFastPointOverlay.PointAdapter points, Integer point) {
                Toast.makeText(map.getContext()
                        , "You clicked " + ((LabelledGeoPoint) points.get(point)).getLabel()
                        , Toast.LENGTH_SHORT).show();
            }
        });

        map.getOverlays().add(sfpo);
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
