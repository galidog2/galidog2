package com.example.galidog2;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

import SyntheseVocale.VoiceIn;
import SyntheseVocale.VoiceOut;

public class MainActivity extends AppCompatActivity {

    TextView txtOutput;
    Button speakButton;

    VoiceIn voice;
    VoiceOut voiceOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtOutput = findViewById(R.id.text);
        speakButton = findViewById(R.id.speakButton);

        voiceOut = new VoiceOut(getApplicationContext());

        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSpeechToText();
            }
        });
    }


    private void startSpeechToText() {
        voice = new VoiceIn(this);
        voice.listen();
    }

    private void createToast(String text){
        Toast.makeText(getApplicationContext(),
                text,
                Toast.LENGTH_SHORT).show();
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
                    String phrase = result.get(0);
                    voice.interpreterAction(phrase);
                    Toast.makeText(getApplicationContext(),
                            voice.toString(),
                            Toast.LENGTH_SHORT).show();

                    voiceOut.speak(voice.getPhrase());
                }
                break;
            }
        }
    }




}
