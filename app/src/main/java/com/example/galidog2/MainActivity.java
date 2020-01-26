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
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

import org.osmdroid.config.Configuration;

public class MainActivity extends SpeechRecognizerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        // On rend les boutons non cliquables (modes non pris en charge par Galidog2)
        findViewById(R.id.navigation).setEnabled(false);
        findViewById(R.id.description).setEnabled(false);


        Button memorisationB = (Button)findViewById(R.id.mémorisation);
        memorisationB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChoixMemorisationActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void doMatch(String match) {
        if (match.equals("mémorisation")){
            Intent intent = new Intent(this, ChoixMemorisationActivity.class);
            this.startActivity(intent);
            //this.voiceOut.speak("Mode mémorisation");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appbar, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //audioModeStart();
        SharedPreferences sharedpreferences = getSharedPreferences("Mode",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("audio", "true");
        editor.apply();
        return true;
    }

}