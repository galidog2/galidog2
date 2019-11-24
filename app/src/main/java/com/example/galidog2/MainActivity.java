package com.example.galidog2;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView txtOutput;
    Button speakButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ici vient une nlle branche carto
        //ça n'a pas marché

        txtOutput = findViewById(R.id.text);
        speakButton = findViewById(R.id.speakButton);

        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),
                        "click",
                        Toast.LENGTH_SHORT).show();
                startSpeechToText();
            }
        });
    }


    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speak something...");
        try {
            startActivityForResult(intent, 666);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Speech recognition is not supported in this device.",
                    Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Callback for speech recognition activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 666: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String text = result.get(0);
                    txtOutput.setText(text);
                    interpreterAction(text);
                }
                break;
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void interpreterAction (String speech) {
        String[] words = speech.split(" ");
        List<String> wordsList = Arrays.asList(words);

        String action = "";
        if (speech.contains("continue")) action = "continuer";
        else if (speech.contains("tourne")) action = "tourner";
        else if (speech.contains("transverse")) action = "transverser";
        else if (speech.contains("monte")) action = "monter";
        else if (speech.contains("descend")) action = "descendre";
        else if (speech.contains("alle")) action = "aller";
        else if (speech.contains("demi-tour")) action = "demi-tour";


        String direction = "";
        if (speech.contains("gauche")) direction = "gauche";
        else if (speech.contains("droite")) direction = "droite";

        String distance = "";
        String distanceMesure = "";

        for(int i = 0; i < wordsList.size(); i++){
            String word = wordsList.get(i);
            if (word.equals("m") || word.contains("mètre")) {
                distanceMesure = "metres";
                if (i > 0) distance = wordsList.get(i-1);
            }
        }

        txtOutput.setText("Speech : " + speech + "\nAction : " + action + "\nDirection : " + direction + "\nDistance : " + distance + " " + distanceMesure);
    }

}
