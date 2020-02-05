package Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Audictionary {


    //Phrases que seront dits par nous
    public static final String SPEAK_ENTRER_MODE_MEMORISATION = "Mode mémorisation";


    //Matchs Lists
    //The first String of each list is the standard one to be implemented in activities

    //HOME
    public static final List<String> matchsMemorisation = Collections.unmodifiableList(Arrays.asList("mémorisation", "mémoriser"));
    //MEMORISATION
    public static final List<String> matchsAccueil = Collections.unmodifiableList(Arrays.asList("accueil", "home", "menu", "retour"));
    public static final List<String> matchsAjouterTrajet = Collections.unmodifiableList(Arrays.asList("ajouter trajet", "ajouter", "trajet", "nouveau trajet", "nouvelle route", "nouveau"));
    public static final List<String> matchsDireTrajet = Collections.unmodifiableList(Arrays.asList("écouter", "dire", "nombre"));
    public static final List<String> matchsSupprimerTrajet = Collections.unmodifiableList(Arrays.asList("supprimer trajet", "supprimer", "enlever"));
    public static final List<String> matchsAnnulerDialog = Collections.unmodifiableList(Arrays.asList("annuler trajet", "annuler", "fermer", "annulé", "annule"));
    public static final List<String> matchsValiderAjouterTrajetDialog = Collections.unmodifiableList(Arrays.asList("valider trajet", "valider", "confirmer"));
    //AJOUTER TRAJET
    public static final List<String> matchsPlayTrajet = Collections.unmodifiableList(Arrays.asList("play", "jouer", "commencer", "démarrer"));
    public static final List<String> matchsPauseTrajet = Collections.unmodifiableList(Arrays.asList("pause", "pauser", "attendre"));
    public static final List<String> matchsReprendreTrajet = Collections.unmodifiableList(Arrays.asList("reprendre", "continuer"));
    public static final List<String> matchsArretTrajet = Collections.unmodifiableList(Arrays.asList("arrêt", "arreter", "finaliser", "stop", "arrête"));
    public static final List<String> matchsSuivreTrajet = Collections.unmodifiableList(Arrays.asList("suivre"));
    public static final List<String> matchsCercletTrajet = Collections.unmodifiableList(Arrays.asList("cercle"));
    //TRAJET SELECTIONNÉ
    public static final List<String> matchsCheckTrajet = Collections.unmodifiableList(Arrays.asList("check"));

    //Ajouter toutes les lists ci-dessous dans la variable matchList
    public static final List<List<String>> matchsList =
            Collections.unmodifiableList(Arrays.asList(matchsMemorisation, matchsAccueil, matchsAjouterTrajet,
                    matchsSupprimerTrajet, matchsAnnulerDialog, matchsValiderAjouterTrajetDialog, matchsPlayTrajet, matchsPauseTrajet,
                    matchsArretTrajet, matchsSuivreTrajet, matchsCercletTrajet, matchsCheckTrajet,matchsReprendreTrajet,
                    matchsDireTrajet));

}
