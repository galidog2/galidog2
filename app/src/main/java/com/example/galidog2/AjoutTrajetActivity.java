package com.example.galidog2;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AjoutTrajetActivity extends AppCompatActivity {

    ArrayList<String> listeFichiers = new ArrayList<>();
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
