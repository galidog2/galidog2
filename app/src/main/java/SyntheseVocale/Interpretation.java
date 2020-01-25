package SyntheseVocale;

import android.app.Activity;
import android.content.Intent;

import com.example.galidog2.ChoixMemorisationActivity;

public class Interpretation {

    private Activity activity;
    private VoiceOut voiceOut;

    public Interpretation(Activity activity) {
        this.activity = activity;
        voiceOut = new VoiceOut(activity.getApplicationContext());
    }

    public void interpreterText(String text) {
        if (text.contains("mémorisation")) {
            Intent intent = new Intent(this.activity, ChoixMemorisationActivity.class);
            this.activity.startActivity(intent);

            this.voiceOut.speak("Mode mémorisation");
        }
    }

}
