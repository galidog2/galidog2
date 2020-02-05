package com.example.galidog2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

import org.osmdroid.config.Configuration;

import Constants.Audictionary;
import SyntheseVocale.VoiceOut;

public class MainActivity extends SpeechRecognizerActivity {

    Button memorisationButton;
    private static boolean annonce =false;
    VoiceOut voiceOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // On rend les boutons non cliquables (modes non pris en charge par Galidog2)
        findViewById(R.id.navigation).setEnabled(false);
        findViewById(R.id.description).setEnabled(false);


        //Test Voix
        voiceOut = new VoiceOut(this);

        memorisationButton = (Button)findViewById(R.id.mémorisation);
        memorisationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceOut.speak("Mode mémorisation");
                Intent intent = new Intent(MainActivity.this, ChoixMemorisationActivity.class);
                startActivity(intent);
            }
        });

        accueil();
    }

    private void accueil() {
        if(!annonce){
            voiceOut.speak("Bienvenue dans l'application Galidog. Choisissez un mode.");
            annonce = true;
        }
    }

    @Override
    public void doCommandeVocal(String command) {

    }

    @Override
    public void doMatch(String match) {
        if (match.equals(Audictionary.matchsMemorisation.get(0)))
            memorisationButton.callOnClick();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appbar, menu);
        return true;
    }


    @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        audioModeStart();
        SharedPreferences sharedpreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("audio", "true");
        editor.apply();
        return true;
    }
}
