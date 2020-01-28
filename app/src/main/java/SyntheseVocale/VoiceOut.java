package SyntheseVocale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

public class VoiceOut {

    private TextToSpeech textToSpeech;
    private Context context;

    public VoiceOut(Context context){
        this.context = context;

        textToSpeech=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.FRANCE);
                }
            }
        });
    }

    public void speak(String toSpeak) {
        Log.i("VOICE OUT", "SPEAAAAAAAAAAAAAAAAAK " + toSpeak);
        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }

}
