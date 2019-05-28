package com.example.galidog2;

public class Point {
    /** CONSTANTES UTILISEES ET A POTENTIELLEMENT MODIFIER*/

    private static final double LATITUDE_MIN=50.602794;/** quadriage de la cité scientifique*/
    private static final double LATITUDE_MAX=50.613183;
    private static final double LONGITUDE_MIN=3.128780;
    private static final double LONGITUDE_MAX=3.152891;


    /** ATTRIBUTS DE LA CLASSE Point*/
    static int dernierIdPoint=0; /**liste les différents points (indice dans la liste cf Trajet) */


    /** ATTRIBUTS */
    /** AJOUTER VIRAGES HORAIRES !!!!! */
    private int idPoint;
    private double latitude;
    private double longitude;
    private double distancePointSuivant;
    private double cap;
    private String direction;



    /** CONSTRUCTEURS */

    public Point() {/**constructeur point localisé*/
        this.idPoint=dernierIdPoint++;
        this.latitude=0;
        this.longitude=0;
        this.distancePointSuivant=0;
        this.cap=0;
        this.direction="En avant";
        Localisation();
    }


    public Point(double longitude, double latitude) {/**constructeur coordonnees INUTILE*/
        this.latitude = latitude;
        this.longitude = longitude;
    }


    /** GETTERS */

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getDistancePointSuivant() {
        return distancePointSuivant;
    }

    public double getCap() {
        return cap;
    }

    public String getDirection() {
        return direction;
    }



    /** SETTERS */

    public void setDistancePointSuivant(double distancePointPrecedent) {
        this.distancePointSuivant = distancePointPrecedent;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setCap(double cap) {
        this.cap = cap;
    }

    /** TOSTRING */
    @Override
    public String toString() {
        return "Point{" + "idPoint=" + idPoint + ", latitude=" + latitude + ", longitude=" + longitude + ", distancePointSuivant=" + distancePointSuivant + ", cap=" + cap + ", direction=" + direction + '}';
    }

    /** METHODES */

    public void Localisation(){/**localise un point dans un carré défini plus haut */
        this.latitude = Math.random()*(LATITUDE_MAX-LATITUDE_MIN)+LATITUDE_MIN; /** A MODIFIER SOUS ANDROID */
        this.longitude= Math.random()*(LONGITUDE_MAX-LONGITUDE_MIN)+LONGITUDE_MIN;;
    }


    public static void main(String[] args) { /** TEST NE SERT A RIEN DANS LE PROGRAMME */
        Point p1=new Point();
        Point p2=new Point();
        System.out.println(p1);
        System.out.println(p2);
        double lat1=Math.toRadians(p1.getLatitude());
        double lon1=Math.toRadians(p1.getLongitude());
        double lat2=Math.toRadians(p2.getLatitude());
        double lon2=Math.toRadians(p2.getLongitude());
        double Cap = 90-180*Math.atan2(lat2-lat1, (lon2-lon1)*Math.cos(lat1))/Math.PI;
        System.out.println(Cap);
    }
}
