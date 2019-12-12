package com.example.galidog2;

import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;

import org.apache.commons.lang3.ObjectUtils;
import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;

import java.util.List;

/**
 * created on 12/27/2017.
 *
 * @author Alex O'Ree
 */

public class CirclePlottingOverlay extends Overlay {

    private String adresse = "adresse";
    private double rayonm=3;
    private GeoPoint pt;
    private int couleur = Color.RED;
    private int numeroCercle;

    public CirclePlottingOverlay(GeoPoint pt) {
        super();
        this.pt = pt;
    }


    /**
     * GETTERS & SETTERS
     */

    public int getNumeroCercle(){
        return numeroCercle;
    }

    public void setNumeroCercle(int i){
        this.numeroCercle=i;
    }

    public void setColor(int couleur){
        this.couleur=couleur;
    }

    public int getCouleur(){
        return couleur;
    }

    public double getLatitude(){
        return pt.getLatitude();
    }

    public double getLongitude(){
        return pt.getLongitude();
    }

    public String getAdresse(){
        return adresse;
    }

    public void setAdresse(String newAdresse){
        adresse=newAdresse;
    }


    public CirclePlottingOverlay(GeoPoint pt, double rayonm, int numeroCercle) {
        super();
        this.numeroCercle=numeroCercle;
        this.rayonm=rayonm;
        this.pt = pt;
    }

    public double getRayon(){
        return rayonm;
    }

    /**
     * DrawCircle
     */


    public void drawCircle(final MapView mapView, final int color){
        List<GeoPoint> circle = Polygon.pointsAsCircle(pt, rayonm);
        Polygon p = new Polygon(mapView);
        p.setPoints(circle);
        p.setFillColor(color);
        p.setTitle("Cercle " + numeroCercle);
        p.setSnippet(adresse);
        mapView.getOverlayManager().add(p);
        mapView.invalidate();
    }

    public void changeColor(MapView mapView, final int color){
        drawCircle(mapView, color);
    }
}
