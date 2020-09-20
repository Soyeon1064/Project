package com.example.project;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import androidx.core.content.ContextCompat;

import com.skt.Tmap.TMapView;

//github test

public class GpsTracker implements LocationListener {
    private final Context mContext;
    Location location;
    double latitude;
    double longitude;
    TMapView tMapView;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    //    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 2 * 1;  // 2 seconds
    protected LocationManager locationManager;

    //GpsTracker에서 위치를 받아서 TMapView 이미지에 위치 변경을 위해서 추가
    public GpsTracker(Context context, TMapView tMapView) {
        this.mContext = context;
        this.tMapView = tMapView;
        getLocation();
    }


    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.i("GpsTracker", "GPS is not available");
            } else {
                int hasFineLocationPermission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
                int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION);

                if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                        hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
                    ;
                }
                else
                    return null;

                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null)
                    {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null)
                        {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }


                if (isGPSEnabled)
                {
                    if (location == null)
                    {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null)
                        {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null)
                            {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.d("@@@", ""+e.toString());
        }

        return location;
    }

    public double getLatitude()
    {
        if(location != null)
        {
            latitude = location.getLatitude();
        }

        return latitude;
    }

    public double getLongitude()
    {
        if(location != null)
        {
            longitude = location.getLongitude();
        }

        return longitude;
    }

    @Override
    //기기의 gps가 바뀔 때마다 gps가 호출해주는 리스너
    public void onLocationChanged(Location location)
    {
        //현재위치와 지도의 중심점을 바꿔주기
        this.location = location;
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        tMapView.setCenterPoint(longitude, latitude, true);
        tMapView.setLocationPoint(longitude, latitude);
    }

    @Override
    public void onProviderDisabled(String provider)
    {
    }

    @Override
    public void onProviderEnabled(String provider)
    {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }

    public void stopUsingGPS()
    {
        if(locationManager != null)
        {
            locationManager.removeUpdates(GpsTracker.this);
        }
    }
}

