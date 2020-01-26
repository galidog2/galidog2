package Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class AudioMatchs {

    //Matchs Lists
    //The first String of each list is the standard one to be implemented in activities
    public static final List<String> matchsMemorisation = Collections.unmodifiableList(Arrays.asList("mémorisation", "mémoriser"));
    public static final List<String> matchsAccueil = Collections.unmodifiableList(Arrays.asList("accueil","home","menu", "retourner"));
    public static final List<String> matchsAjouterTrajet = Collections.unmodifiableList(Arrays.asList("ajouter trajet","ajouter","trajet", "neaveau trajet", "nouvelle route"));
    public static final List<String> matchsSupprimerTrajet = Collections.unmodifiableList(Arrays.asList("supprimer trajet","supprimer","enlever"));

    //Ajouter toutes les lists ci-dessous dans la variable matchList
    public static final List<List<String>> matchsList =
            Collections.unmodifiableList(Arrays.asList(matchsMemorisation,matchsAccueil, matchsAjouterTrajet, matchsSupprimerTrajet));
}