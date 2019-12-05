package com.example.galidog2;

import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;

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

    double rayonm=3;
    GeoPoint pt;

    public CirclePlottingOverlay(GeoPoint pt) {
        super();
        this.pt = pt;
    }

    public double getLatitude(){
        return pt.getLatitude();
    }

    public double getLongitude(){
        return pt.getLongitude();
    }


    public CirclePlottingOverlay(GeoPoint pt, double rayonm) {
        super();
        this.rayonm=rayonm;
        this.pt = pt;
    }

    public double getRayon(){
        return rayonm;
    }


    public void drawCircle(final MapView mapView, final int color){
        List<GeoPoint> circle = Polygon.pointsAsCircle(pt, rayonm);
        Polygon p = new Polygon(mapView);
        p.setPoints(circle);
        p.setFillColor(color);
        p.setTitle("A circle");
        mapView.getOverlayManager().add(p);
        mapView.invalidate();
    }

    public void changeColor(MapView mapView, final int color){
        drawCircle(mapView, color);
    }
}
