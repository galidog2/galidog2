package com.example.galidog2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class AjoutTrajetActivity extends AppCompatActivity {

    private static final String TAG = "AjoutTrajetActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajout_trajet);
        //récupération du nom du trajet :

        if (getIntent().hasExtra("nouveaufichier")){
            String nomFichier = getIntent().getStringExtra("nouveaufichier");
            Log.d(TAG, "onCreate: "+nomFichier);
        }
    }
}
