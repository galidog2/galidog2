package com.example.galidog2;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 *
 * @author Hugo Dufrêne
 */
public class Memorisation {/** MODE MEMORISATION */

    /** ATTRIBUTS DE LA CLASSE Mémorisation */

    int nbTrajets=0;


    /** ATTRIBUTS */

    private List listeTrajets = new ArrayList();/**liste qui recense tous les trajets */


    /** METHODES */

    public static boolean NommerTrajet(Trajet t){
        if (t.getNom()==null){/** trajet sans nom */
            Scanner sc = new Scanner(System.in);
            char response='N';
            System.out.println("Veuillez nommer ce trajet svp");/**nom du trajet*/
            String name = sc.nextLine();
            while (response=='N' || name==null){

                if (!"".equals(name) && name != null){       /** A FAIRE !!!! VERIFIER SI LE NOM EST DEJA PRIS */
                    System.out.println("Veuillez confirmer le nom :" + name+" (O/N)");/**début du trajet*/
                    response = sc.nextLine().charAt(0);
                    if (response=='O'){
                        t.setNom(name);                         /** LE NOM N'EST PAS PRIS ET CONFIRME, ON LE CHANGE */
                        return true;
                    }else{
                        System.out.println("Veuillez nommer ce trajet svp");/**nom du trajet*/
                        name = sc.nextLine();
                    }

                }else if ("".equals(name) || name == null){
                    System.out.println("Nom incorrect");/** NOM VIDE*/
                    System.out.println("Veuillez nommer ce trajet svp");/**nom du trajet*/
                    name = sc.nextLine();
                }else{
                    System.out.println("Ce nom de trajet est déjà pris"); /** NOM DEJA PRIS*/
                    System.out.println("Veuillez nommer ce trajet svp");/**nom du trajet*/
                    name = sc.nextLine();
                }

            }
        }
        return false;
    }

    public boolean AutreMemorisation(){
        System.out.println("Voulez-vous mémoriser un trajet ? (O/N)");/**suite du programme ou non*/

        Scanner sc = new Scanner(System.in);
        char response = sc.nextLine().charAt(0);

        if (response=='O'){/** réponse utilisateur */
            return true;
        }else{
            return false;
        }
    }



    /** Méthode du mode mémorisation -- STRUCTURE DU MODE MEMORISATION */
    public void main(String[] args) throws InterruptedException {
        System.out.println("Bienvenue dans le mode mémorisation !");
        while (AutreMemorisation()){
            System.out.println("Nombre de trajets mémorisés : "+nbTrajets);
            Trajet t = new Trajet();
            t.ConstructionTrajet();
            if (NommerTrajet(t)){
                listeTrajets.add(t);
            }
            System.out.println(t);
            System.out.println("listeTrajets : "+listeTrajets);
        }
        System.out.println("Fin du programme Mémorisation...");

    }
}

