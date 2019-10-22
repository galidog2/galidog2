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
import android.view.View;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

public class ChoixMemorisationActivity extends GenericActivity implements RecyclerViewAdapter.OnTrajetListener{

    private RecyclerViewAdapter adapter;
    private static final String TAG = "ChoixMemorisationActivi";
    private FloatingActionButton floatingActionButton;
    private int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;
    private int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choix_memorisation);
        demandePermissionStockageLecture();

        // Utilisation du RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        // Récupération des titres des itinéraires dans le dossier /storage/emulated/0/osmdroid/kml
        // Remarque : le chemin est (sans doute) à redéfinir.
        String path = Environment.getExternalStorageDirectory().toString()+ "/osmdroid/kml";
        File directory = new File(path);
        File[] files = directory.listFiles();
        ArrayList<String> listeFichiers = new ArrayList<>();
        String nomFichier;
        for (int i = 0; i < files.length; i++)
        {
            nomFichier = files[i].getName().substring(0, files[i].getName().lastIndexOf('.'));
            listeFichiers.add(nomFichier);
        }


        // Création de l'adapter qui va organiser les ItemHolders
        adapter = new RecyclerViewAdapter(listeFichiers,this);
        recyclerView.setAdapter(adapter);
        // On implémente un RecyclerViewAdapter basique au RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // On crée le bouton flottant qui permet d'ajouter des listes
        floatingActionButton = findViewById(R.id.fab);
        // Les variables ont besoin d'être déclarées en final car on les utilise dans un cast local.
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                demandePermissionStockageEcriture();
                CreerAlertDialog();
            }
        });
    }

    private void demandePermissionStockageLecture() {

        while (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            }
        }
    }

    private void demandePermissionStockageEcriture() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        } else {
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
        //TODO: lancer la MapActivity
    }
}
