package SyntheseVocale;

import android.app.Activity;
import android.content.Intent;

import com.example.galidog2.ChoixMemorisationActivity;

import java.util.ArrayList;

public class Interpretation {

    private Activity activity;
    private VoiceOut voiceOut;
    private ArrayList<String> matchList = new ArrayList<String>();

    public Interpretation(Activity activity) {
        this.activity = activity;
        voiceOut = new VoiceOut(activity.getApplicationContext());
    }

    public String findMatch(String text) {
        addMatchs();
        for (int i=0; i< matchList.size(); i++)
            if (text.contains(matchList.get(i)))
                return matchList.get(i);
        return null;
    }

    private void addMatchs(){
        matchList.add("mémorisation");
        matchList.add("ajouter trajet");
    }

}
