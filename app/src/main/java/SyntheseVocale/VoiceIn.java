package SyntheseVocale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class VoiceIn {

    private Activity activity;

    private String phrase;
    private String action;
    private String direction;
    private String distance;
    private String distanceMesure;

    private boolean interpreted;

    public VoiceIn(Activity activity){
        this.activity = activity;
        this.interpreted = false;
    }

    public String listen(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speak something...");
        try {
            activity.startActivityForResult(intent, 666);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(activity.getApplicationContext(),
                    "Sorry! Speech recognition is not supported in this device.",
                    Toast.LENGTH_SHORT).show();
        }
        return phrase;
    }



    @SuppressLint("SetTextI18n")
    public void interpreterAction (String phrase) {
        interpreted = true;
        this.phrase = phrase;

        String[] words = phrase.split(" ");
        List<String> wordsList = Arrays.asList(words);

        //Interprétation de l'action
        action = "";
        if (phrase.contains("continue")) action = "continuer";
        else if (phrase.contains("tourne")) action = "tourner";
        else if (phrase.contains("transverse")) action = "transverser";
        else if (phrase.contains("monte")) action = "monter";
        else if (phrase.contains("descend")) action = "descendre";
        else if (phrase.contains("alle")) action = "aller";
        else if (phrase.contains("demi-tour")) action = "demi-tour";
        else if (phrase.contains("memoris")) action = "memoriser";


        //Interprétation de la direction
        direction = "";
        if (phrase.contains("gauche")) direction = "gauche";
        else if (phrase.contains("droite")) direction = "droite";
        else if (phrase.contains("avant")) direction = "avant";


        //Interprétation de la distance et aussi de la mesure de la distance
        distance = "";
        distanceMesure = "";
        for(int i = 0; i < wordsList.size(); i++){
            String word = wordsList.get(i);
            if (word.equals("m") || word.contains("mètre")) {
                distanceMesure = "metres";
                if (i > 0) distance = wordsList.get(i-1);
                break;
            } else if (word.equals("km") || word.contains("kilomètre")) {
                distanceMesure = "kilomètres";
                if (i > 0) distance = wordsList.get(i-1);
                break;
            }
        }
     }

    public String getPhrase(){
        return interpreted ? phrase : "";
    }

    public String getAction(){
        return interpreted ? action : "";
    }

    public String getDirection(){
        return interpreted ? direction : "";
    }

    public int getDistance(){
        return interpreted ? Integer.parseInt(distance) : 0;
    }

    public String getDistanceMesure(){
        return interpreted ? distanceMesure : "";
    }

    public String toString(){
        if (interpreted){
           return "Speech : " + phrase + "\nAction : " + action + "\nDirection : " + direction + "\nDistance : " + distance + " " + distanceMesure;
        }
        return "Not interpreted";
    }

}
