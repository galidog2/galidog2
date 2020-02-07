package com.example.galidog2;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

import Constants.Audictionary;
import SyntheseVocale.VoiceOut;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChoixMemorisationActivity extends SpeechRecognizerActivity implements RecyclerViewAdapter.OnTrajetListener {
    private VoiceOut voiceOut = null;
    private RecyclerViewAdapter adapter;
    ArrayList<String> listeFichiers = new ArrayList<>();
    private String nomTrajets = "";
    private static final String TAG = "ChoixMemorisationActivity";
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;
    private CheckBox cb_supprimer;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    private EditText editText;
    private AlertDialog alertDialog;
    private AlertDialog alertDialogSupprimer;
    private boolean ajouterTrajetDialogShown = false;
    private boolean supprimerTrajetDialogShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choix_memorisation);
        voiceOut = new VoiceOut(this);
        // Utilisation du RecyclerView
        recyclerView = findViewById(R.id.recycler_view);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        cb_supprimer = findViewById(R.id.cb_supprimer);
        ArrayList<String> listeVide = new ArrayList<>();
        // Création de l'adapter qui va organiser les ItemHolders
        adapter = new RecyclerViewAdapter(listeVide, this);
        recyclerView.setAdapter(adapter);
        // On implémente un RecyclerViewAdapter basique au RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        boolean demandeL = demandePermissionStockageLecture();
        if (demandeL) {

            listeFichiers = recupererListeKML();
            if (!listeFichiers.isEmpty())
                adapter.show(listeFichiers);
        }

        // On crée le bouton flottant qui permet d'ajouter des listes
        floatingActionButton = findViewById(R.id.fab);
        // Les variables ont besoin d'être déclarées en final car on les utilise dans un cast local.
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                demandePermissionStockageEcriture();
                boolean demandeE = demandePermissionStockageEcriture();
                if (demandeE) {
                    CreerAlertDialog();
                }
            }
        });
    }

    /**
     * Remarque : le chemin est (sans doute) à redéfinir.
     * Récupération des titres des itinéraires dans le dossier /storage/emulated/0/osmdroid/kml
     *
     * @return la liste des titres des trajets
     */
    private ArrayList<String> recupererListeKML() {
        String path = Environment.getExternalStorageDirectory().toString() + "/osmdroid/kml";
        File directory = new File(path);
        File[] files = directory.listFiles();
        String nomFichier;
        if (files != null)
            for (File file : files) {
                nomFichier = file.getName().substring(0, file.getName().lastIndexOf('.'));
                listeFichiers.add(nomFichier);
                voiceOut.speak(nomFichier);
                nomTrajets += nomFichier + ", ";
            }
        return listeFichiers;
    }

    /**
     * Permet de supprimer un trajet
     *
     * @param position
     */
    private void supprimerTrajet(int position) {
        String path = Environment.getExternalStorageDirectory().toString() + "/osmdroid/kml";
        File directory = new File(path);
        File[] files = directory.listFiles();
        files[position].delete();
        listeFichiers = recupererListeKML();
    }

    /**
     * La méthode alerteDialogSupprimer crée une fenêtre où l'utisateur peut
     * valider ou non la suppression du trajet.
     */
    private void alerteDialogSupprimer(final int position) {
        // Un AlertDialog fonctionne comme une «mini-activité».
        // Il demande à l'utisateur une valeur, la renvoie à l'activité et s'éteint.
        supprimerTrajetDialogShown = true;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Voulez-vous supprimer le trajet " + listeFichiers.get(position) + " ?");
        voiceOut.speak("Voulez-vous supprimer le trajet " + listeFichiers.get(position) + " ?");
        // Cet AlertDialog comporte un bouton pour valider…
        alertDialogBuilder.setPositiveButton("Supprimer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                supprimerTrajet(position);
                //On met à jour l'affichage
                Intent intent = new Intent(ChoixMemorisationActivity.this, ChoixMemorisationActivity.class);
                startActivity(intent);
                supprimerTrajetDialogShown = false;
                voiceOut.speak("Trajet supprimé");
            }
        });
        // … et un bouton pour annuler, qui arrête l'AlertDialog.
        alertDialogBuilder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                supprimerTrajetDialogShown = false;
            }
        });
        alertDialogSupprimer = alertDialogBuilder.create();
        alertDialogSupprimer.show();
    }

    /**
     * Méthode pour demander la permission de lecture sur le stockage
     *
     * @return un booléen qui indique si on demande la permission
     */
    private boolean demandePermissionStockageLecture() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            //on indique au-dessus avec la variable MY_PERMISSIONS le numéro de requête.
            //ce numéro est réutilisé dans onRequestPermissionsResult
            return false;
        }
        return true;
    }

    /**
     * Méthode pour demander la permission d'écriture sur le stockage
     *
     * @return un booléen qui indique si on demande la permission
     */
    private boolean demandePermissionStockageEcriture() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            return false;
        }
        return true;
    }

    /**
     * Méthode appelée lors de la demande à l'utilisateur des permissions
     * Si l'on a une réponse favorable, on peut continuer les actions.
     * Si l'on a une réponse négative, on redemande la permission car elle est nécessaire à l'application.
     *
     * @param requestCode  le n° de requête
     * @param permissions  les permissions demandées
     * @param grantResults la réponse de l'utilisateur
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ArrayList<String> listeFichiers = recupererListeKML();
                    if (!listeFichiers.isEmpty())
                        adapter.show(listeFichiers);
                } else {
                    // Permission non autorisée
                    Toast.makeText(this, "Cette permission est nécessaire pour lire les fichiers", Toast.LENGTH_SHORT).show();
                    demandePermissionStockageLecture();
                }
            }

            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "Cette permission est nécessaire pour sauvegarder des fichiers", Toast.LENGTH_SHORT).show();
                    demandePermissionStockageEcriture();
                }
            }
        }
    }

    @Override
    public void doCommandeVocal(String command) {
        if (ajouterTrajetDialogShown)   //nommer trajet
            editText.setText(command);
        else {          //sélection d'un trajet. Command = Nom du trajet demmandé, peut-être (on verifie)
            if (!listeFichiers.isEmpty())
                for (int i = 0; i < listeFichiers.size(); i++)
                    if (listeFichiers.get(i).equalsIgnoreCase(command)) //nom du trajet était dit, du coup on le demarre
                        onTrajetClick(i);
        }
    }

    @Override
    public void doMatch(String match) {
        if (match.equals(Audictionary.matchsAccueil.get(0))) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            voiceOut.speak("Accueil");
        } else if (match.equals(Audictionary.matchsAjouterTrajet.get(0)))
            floatingActionButton.callOnClick();
        else if (match.equals(Audictionary.matchsSupprimerTrajet.get(0))) { //"Supprimer appellé"
            if (supprimerTrajetDialogShown)
                alertDialogSupprimer.getButton(AlertDialog.BUTTON_POSITIVE).callOnClick();
            else
                cb_supprimer.setChecked(!cb_supprimer.isChecked());
        } else if (match.equals(Audictionary.matchsAnnulerDialog.get(0))) {
            if (supprimerTrajetDialogShown)
                alertDialogSupprimer.getButton(AlertDialog.BUTTON_NEGATIVE).callOnClick();
            else if (ajouterTrajetDialogShown)
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).callOnClick();
        } else if (match.equals(Audictionary.matchsValiderAjouterTrajetDialog.get(0)))
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).callOnClick();
        else if (match.equals(Audictionary.matchsDireTrajet.get(0))) {
            voiceOut.speak("Vos trajets sont :" + nomTrajets);
        }else if (match.equals(Audictionary.matchsEcran.get(0))) {
            voiceOut.speak("Vous êtes dans le mode mémorisation");
        }
    }

    /**
     * La méthode CreerAlertDialog crée une fenêtre où l'utisateur peut
     * rentrer le nom de la nouvelle liste.
     */
    private void CreerAlertDialog() {
        ajouterTrajetDialogShown = true;
        editText = new EditText(this);
        // Un AlertDialog fonctionne comme une «mini-activité».
        // Il demande à l'utisateur une valeur, la renvoie à l'activité et s'éteint.
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Entrez le nom du trajet");
        alertDialogBuilder.setView(editText);
        voiceOut.speak("Donnez le nom du trajet");//TODO: voiceIn
        // Cet AlertDialog comporte un bouton pour valider…
        alertDialogBuilder.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Intent intent = new Intent(ChoixMemorisationActivity.this, AjoutTrajetActivity.class);
                intent.putExtra("nouveaufichier", editText.getText().toString());
                voiceOut.speak("Vous créez le trajet :" + editText.getText().toString());
                startActivity(intent);
            }
        });
        // … et un bouton pour annuler, qui arrête l'AlertDialog.
        alertDialogBuilder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ajouterTrajetDialogShown = false;
                dialog.cancel();
            }
        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Cette méthode permet de démarrer la LectureActivity.
     * Elle lui envoie le nom du trajet à lire.
     *
     * @param position le numéro de l'élément cliqué
     */
    @Override
    public void onTrajetClick(int position) {
        if (!cb_supprimer.isChecked()) {
            Intent intent = new Intent(ChoixMemorisationActivity.this, LectureActivity.class);
            intent.putExtra("nomfichier", listeFichiers.get(position));
            voiceOut.speak("Vous suivez le trajet :" + listeFichiers.get(position));
            startActivity(intent);
        } else {
            alerteDialogSupprimer(position);
        }
    }

}
