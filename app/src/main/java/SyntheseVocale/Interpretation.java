package SyntheseVocale;

import android.app.Activity;
import java.util.List;

import Constants.AudioMatchs;

public class Interpretation {

    private Activity activity;

    public Interpretation(Activity activity) {
        this.activity = activity;
    }

    public String findMatch(String text) {
        for (int i=0; i< AudioMatchs.matchsList.size(); i++) {
            List<String> list = AudioMatchs.matchsList.get(i);
            int listSize = list.size();
            for (int j=0; j<listSize; j++)
                if (text.contains(list.get(j)))
                    return list.get(0); //always the first one, for standards implementations in Activities
        }

        //no Matchs
        return null;
    }

}
