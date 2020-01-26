package com.example.galidog2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import Constants.AudioMatchs;

public abstract class SpeechRecognizerActivity extends GenericActivity implements RecognitionListener {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "RecognitionListener";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        resetSpeechRecognizer();

        // check for permission
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }

        setRecogniserIntent();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                speech.startListening(recognizerIntent);
            } else {
                showToast("Permission Denied!");
                finish();
            }
        }
    }

    @Override
    public void onResume() {
        showLog( "RESUME");
        super.onResume();
        resetSpeechRecognizer();
        speech.startListening(recognizerIntent);
    }

    @Override
    protected void onPause() {
        showLog( "PAUSE");
        super.onPause();
        speech.stopListening();
        if (speech != null) {
            speech.destroy();
        }
    }

    @Override
    protected void onStop() {
        showLog( "STOP");
        super.onStop();

        if (speech != null) {
            speech.destroy();
        }
    }

    @Override
    public void onBeginningOfSpeech() {
        showLog( "onBeginningOfSpeech");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        showLog( "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        showLog( "onEndOfSpeech");
        speech.stopListening();
    }

    @Override
    public void onResults(Bundle results) {
        showLog( "onResults");

        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches) {
            text += result + "\n";
            findAndDoMatch(result);
        }

        showLog("Matchs: " + text);
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        showLog( "FAILED " + errorMessage);

        // rest voice recogniser
        resetSpeechRecognizer();
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        showLog( "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        showLog( "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        showLog("onReadyForSpeech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
    }

    public String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }


    private void resetSpeechRecognizer() {

        if(speech != null)
            speech.destroy();
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        if(SpeechRecognizer.isRecognitionAvailable(this))
            speech.setRecognitionListener(this);
        else
            finish();
    }

    private void setRecogniserIntent() {
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "fr");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    public void findAndDoMatch(String text) {
        for (int i = 0; i< AudioMatchs.matchsList.size(); i++) {
            List<String> list = AudioMatchs.matchsList.get(i);
            int listSize = list.size();
            for (int j=0; j<listSize; j++)
                if (text.contains(list.get(j))) //found a match
                    doMatch(list.get(0)); //always the first one, for standards implementations in Activities
        }
    }

    //do whatever we want with the matched audio
    public abstract void doMatch(String match);

    private void showToast (String text) {
        Toast.makeText(this.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void showLog(String text) {
        Log.i(LOG_TAG, text);
    }

}
