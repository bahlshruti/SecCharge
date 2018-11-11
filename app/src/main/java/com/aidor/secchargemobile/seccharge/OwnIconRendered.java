package com.aidor.secchargemobile.seccharge;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import com.aidor.projects.seccharge.R;
import com.aidor.secchargemobile.custom.SecCharge;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

class OwnIconRendered extends DefaultClusterRenderer<MyMapItem> {
     Context context;
    SecCharge myApp;
    IconGenerator mClusterIconGenerator;
    public OwnIconRendered(Context context, GoogleMap map,
                           ClusterManager<MyMapItem> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
        mClusterIconGenerator = new IconGenerator(context);
    }
    //IconGenerator mClusterIconGenerator = new IconGenerator(context);
        @Override
        protected void onBeforeClusterItemRendered (MyMapItem item, MarkerOptions markerOptions){
           // markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_thunder));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_thunder)));

            super.onBeforeClusterItemRendered(item, markerOptions);
        }

        @Override
        protected void onBeforeClusterRendered (Cluster <MyMapItem> cluster, MarkerOptions
        markerOptions){
            super.onBeforeClusterRendered(cluster, markerOptions);
            mClusterIconGenerator.setTextAppearance(context, android.R.style.TextAppearance_Medium);
           // mClusterIconGenerator.setTextAppearance(context, android.R.style.TextAppearance_Large);

            if (cluster.getSize() < 10) {
                final Drawable clusterIcon = context.getResources().getDrawable(R.drawable.m1);
                mClusterIconGenerator.setBackground(clusterIcon);
                mClusterIconGenerator.setContentPadding(45, 25, 0, 0);

                Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));

            } else if (cluster.getSize() > 9 && cluster.getSize() < 60) {
                final Drawable clusterIcon = context.getResources().getDrawable(R.drawable.m2);
                mClusterIconGenerator.setBackground(clusterIcon);
                mClusterIconGenerator.setContentPadding(35, 31, 0, 0);
                Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
            } else {
                final Drawable clusterIcon = context.getResources().getDrawable(R.drawable.m3);
                mClusterIconGenerator.setBackground(clusterIcon);
                mClusterIconGenerator.setContentPadding(45, 37, 0, 0);
                Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
            }

        }

}
