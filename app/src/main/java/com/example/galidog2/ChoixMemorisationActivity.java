package com.example.galidog2;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

public class ChoixMemorisationActivity extends GenericActivity implements RecyclerViewAdapter.OnTrajetListener{

    private RecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    ArrayList<String> listeFichiers = new ArrayList<>();
    private static final String TAG = "ChoixMemorisationActivi";
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE=1;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choix_memorisation);

        // Utilisation du RecyclerView
        recyclerView = findViewById(R.id.recycler_view);

        ArrayList<String> listeVide = new ArrayList<>();
        // Création de l'adapter qui va organiser les ItemHolders
        adapter = new RecyclerViewAdapter(listeVide,this);
        recyclerView.setAdapter(adapter);
        // On implémente un RecyclerViewAdapter basique au RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        boolean demandeL = demandePermissionStockageLecture();
        if(demandeL){

            listeFichiers = recupererListeKML();
            if(!listeFichiers.isEmpty())
                adapter.show(listeFichiers);
        }

        // On crée le bouton flottant qui permet d'ajouter des listes
        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        // Les variables ont besoin d'être déclarées en final car on les utilise dans un cast local.
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                demandePermissionStockageEcriture();
                boolean demandeE = demandePermissionStockageEcriture();
                if(demandeE){
                    CreerAlertDialog();
                }
            }
        });
    }

    private ArrayList<String> recupererListeKML() {
        // Récupération des titres des itinéraires dans le dossier /storage/emulated/0/osmdroid/kml
        // Remarque : le chemin est (sans doute) à redéfinir.
        String path = Environment.getExternalStorageDirectory().toString()+ "/osmdroid/kml";
        File directory = new File(path);
        File[] files = directory.listFiles();
        String nomFichier;
        for (int i = 0; i < files.length; i++)
        {
            nomFichier = files[i].getName().substring(0, files[i].getName().lastIndexOf('.'));
            listeFichiers.add(nomFichier);
        }
        return listeFichiers;
    }

    private boolean demandePermissionStockageLecture() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            return false;
        }
        return true;
    }

    private boolean demandePermissionStockageEcriture() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            return false;
        }
        return true;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ArrayList<String> listeFichiers = recupererListeKML();
                    if(!listeFichiers.isEmpty())
                        adapter.show(listeFichiers);
                } else {
                    // Permission non autorisée
                    Toast.makeText(this, "Cette permission est nécessaire pour lire les fichiers", Toast.LENGTH_SHORT).show();
                    demandePermissionStockageLecture();
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CreerAlertDialog();
                } else {
                    Toast.makeText(this, "Cette permission est nécessaire pour sauvegarder des fichiers", Toast.LENGTH_SHORT).show();
                    demandePermissionStockageEcriture();
                }
                return;
            }
        }
    }

    /**
     *  La méthode CreerAlertDialog crée une fenêtre où l'utisateur peut
     *      rentrer le nom de la nouvelle liste.
     */
    private void CreerAlertDialog() {

        final EditText editText = new EditText(this);
        // Un AlertDialog fonctionne comme une «mini-activité».
        // Il demande à l'utisateur une valeur, la renvoie à l'activité et s'éteint.
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Entrez le nom du trajet");
        alertDialogBuilder.setView(editText);
        // Cet AlertDialog comporte un bouton pour valider…
        alertDialogBuilder.setPositiveButton("Valider",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

                //TODO : ajout d'un itinéraire.
                // On lance une nouvelle activité

                /* On relance l'activité pour la rafraîchir*/
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
        // … et un bouton pour annuler, qui arrête l'AlertDialog.
        alertDialogBuilder.setNegativeButton("Annuler",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onTrajetClick(int position) {
        Intent intent = new Intent(ChoixMemorisationActivity.this, MapActivity.class);
        intent.putExtra("nomfichier",listeFichiers.get(position));
        startActivity(intent);
    }
}
