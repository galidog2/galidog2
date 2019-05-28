package com.example.galidog2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Trajet {
    /** CONSTANTES UTILISEES ET A POTENTIELLEMENT MODIFIER*/

    private static final int WAITING_TIME_NEXT_POINT=3;/** temps d'attente en secondes Prochain Point*/
    private static final int WAITING_TIME_LOC=1;/** temps d'attente en secondes Localisation*/
    private static final int ANGLE_LIMITE_VIRAGE=30;/**angle limite virage en degrés*/
    private static final int ANGLE_LIMITE_DEMI_TOUR=150;/**angle limite demi tour en degrés*/

    private static final String AVANT="En avant";
    private static final String GAUCHE="A gauche";
    private static final String DROITE="A droite";
    private static final String ARRIERE="Demi tour";


    /** ATTRIBUTS DE LA CLASSE Trajet */

    static int dernierIdTrajet=0; /**identification des différents Trajets */


    /** ATTRIBUTS */

    private int idTrajet;
    private String nom; /** Nom du trajet */
    private List listePoints = new ArrayList();/**liste qui recense tous les points du trajet */


    /** CONSTRUCTEURS */

    public Trajet(){ /**Constructeur par défaut */
        this.idTrajet=dernierIdTrajet++;
        this.nom=null;
    }

    public Trajet(String nom) {
        this();
        if (nom!=null){
            this.nom = nom;
        }
    }


    /** GETTERS */

    public int getIdTrajet() {
        return idTrajet;
    }

    public String getNom() {
        return nom;
    }

    public List getListePoints() {
        return listePoints;
    }


    /** SETTERS */

    public void setNom(String nom) {
        if (nom!=null){
            this.nom = nom;
        }
    }



    /** TOSTRING */

    @Override
    public String toString() {
        return "Trajet{" + "idTrajet=" + idTrajet + ", nom=" + nom + ", listePoints=" + listePoints + '}';
    }



    /** METHODES */

    /** méthode distance précédente et cap*/
    public double[] CalculerGrandeurs(Point p1, Point p2) {
        double lat1=Math.toRadians(p1.getLatitude());
        double lon1=Math.toRadians(p1.getLongitude());
        double lat2=Math.toRadians(p2.getLatitude());
        double lon2=Math.toRadians(p2.getLongitude());
        double distance=6367445*Math.acos(Math.sin(lat1)*Math.sin(lat2)+Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1));
        double cap = 90-180*Math.atan2(lat2-lat1, (lon2-lon1)*Math.cos(lat1))/Math.PI;
        double[] resultat = {distance,cap};
        return resultat;
    }

    /**Méthode ajout distance point précédent*/
    public void AjoutDistance(Point p1, Point p2){
        p1.setDistancePointSuivant((double)CalculerGrandeurs(p1,p2)[0]);
    }

    /**Méthode ajout cap point précédent */
    public double AjoutCap(Point p1,Point p2){
        double cap=(double) CalculerGrandeurs(p1,p2)[1];/**cap terrestre entre p1 et p2*/

        if ((double)p1.getCap()==0){
            System.out.println(cap);
            p1.setCap(cap);
        }
        return(cap);
    }


    /** Méthodes ajout direction point précédent*/
    public void AjoutDirection(Point p1){
        p1.setDirection(AVANT);/** premier point du trajet toujours en direction avant*/
    }
    public void AjoutDirection(Point p1, Point p2, Point p3){

        System.out.println("ajout direction2");
        double deltaCap=AjoutCap(p2,p3)-AjoutCap(p1,p2);

        if (Math.abs(deltaCap)<=ANGLE_LIMITE_VIRAGE){/**en avant*/
            p2.setDirection(AVANT);
        }else if(deltaCap>ANGLE_LIMITE_VIRAGE && deltaCap <=ANGLE_LIMITE_DEMI_TOUR){/**virage droit*/
            p2.setDirection(DROITE);
        }else if(deltaCap<-ANGLE_LIMITE_VIRAGE && deltaCap >=-ANGLE_LIMITE_DEMI_TOUR){/**virage droit*/
            p2.setDirection(GAUCHE);
        }else{
            p2.setDirection(ARRIERE);
        }
    }

    /**Savoir si l'appareil est localisé ou non A MODIFIER SELON ANDROID*/
    public static boolean LocalisationOK() throws InterruptedException {/**permet de savoir si l'appareil a bien été localisé dans la ville */
        System.out.println("Localisation en cours...");
        boolean pasLocalise=true;
        while (pasLocalise){
            /**Opérations de localisation */

            /**ici opperations A SUPPRIMER, juste pour tester la fonction !!!*/
            for(int i=0;i<2;i++){
                System.out.println("Localisation en cours...");
                TimeUnit.SECONDS.sleep(WAITING_TIME_LOC);
            }
            pasLocalise=false;
        }
        System.out.println("Appareil localisé");   /** localisationOK*/
        return true;

        /**System.out.println("Appareil non localisé");   /** localisation non effectuée*/
        /**return false;*/
    }

    /**Demander à l'utilisateur s'il veut continuer ou non le trajet*/
    public boolean ContinuerTrajet(){
        if (listePoints.isEmpty()){/**début du trajet */
            System.out.println("Prêt au départ ? (O/N)");/**début du trajet*/
        }else{
            System.out.println("Continuer le trajet ? (O/N)");/**suite du trajet ou non*/
        }

        Scanner sc = new Scanner(System.in);
        char response = sc.nextLine().charAt(0);


        if (response=='O'){/** réponse utilisateur */
            return true;
        }else{
            return false;
        }
    }

    /** Méthode principale qui permet de suivre le parcours et de relever les différents points */
    public void ConstructionTrajet() throws InterruptedException{
        int len=listePoints.size();
        while (ContinuerTrajet()){/** On continue le trajet*/

            if (LocalisationOK()){/**vérifie si l'appareil est bien localisé */
                Point p=new Point();/**crée un nouveau point*/
                listePoints.add(p);/**ajoute le nouveau point à la liste*/
                len=listePoints.size();

                /**AJOUT DIRECTION*/
                if(len==1){ /** un seul point dans la liste*/
                    AjoutDirection((Point) listePoints.get(0));

                } else if (len>2){
                    AjoutDirection((Point) listePoints.get(len - 3),(Point) listePoints.get(len - 2),(Point) listePoints.get(len - 1)); /**initialise l'angle du point du milieu*/
                }

                /**AJOUT DISTANCE*/
                if (len>1){/** Au moins 2 points */
                    AjoutDistance((Point) listePoints.get(len -2),(Point) listePoints.get(len-1));/**initialise la distance du premier point vers le deuxième*/
                }
                System.out.println(listePoints);
                TimeUnit.SECONDS.sleep(WAITING_TIME_NEXT_POINT) ;
            }
        }
    }
}
