package com.alcove.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.Geocoder;
import android.location.Address;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.os.Build;

public class LocationManager {
    private static final String TAG = "LocationManager";
    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    private LocationCallback callback;

    public interface LocationCallback {
        void onLocationReceived(Location location);
        void onLocationError(String error);
    }

    public interface LocationNameCallback {
        void onLocationNameReceived(String locationName);
    }

    public LocationManager(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Request current device location (one-time fetch)
     */
    public void getCurrentLocation(LocationCallback callback) {
        this.callback = callback;

        // Check permissions
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationError("Location permission not granted");
            return;
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            Log.d(TAG, "Location obtained: " + location.getLatitude() + ", " + location.getLongitude());
                            callback.onLocationReceived(location);
                        } else {
                            Log.w(TAG, "Location is null - device may not have GPS fix yet");
                            callback.onLocationError("Location not available yet. Ensure GPS is enabled.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get location", e);
                        callback.onLocationError("Error: " + e.getMessage());
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception while getting location", e);
            callback.onLocationError("Exception: " + e.getMessage());
        }
    }

    /**
     * Format location as readable string
     */
    public static String formatLocation(Location location) {
        if (location == null) return "Location unknown";
        return String.format(Locale.getDefault(), "%.4f, %.4f", location.getLatitude(), location.getLongitude());
    }

    /**
     * Get city name from location (approximate)
     */
    public static void getLocationName(Context context, Location location, LocationNameCallback callback) {
        if (location == null) {
            callback.onLocationNameReceived("Unknown location");
            return;
        }

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1, new Geocoder.GeocodeListener() {
                @Override
                public void onGeocode(List<Address> addresses) {
                    callback.onLocationNameReceived(processAddresses(addresses, location));
                }

                @Override
                public void onError(String errorMessage) {
                    callback.onLocationNameReceived(formatLocation(location));
                }
            });
        } else {
            new Thread(() -> {
                String name = getLegacyLocationName(geocoder, location);
                callback.onLocationNameReceived(name);
            }).start();
        }
    }

    @SuppressWarnings("deprecation")
    private static String getLegacyLocationName(Geocoder geocoder, Location location) {
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            return processAddresses(addresses, location);
        } catch (IOException e) {
            Log.e(TAG, "Error getting location name", e);
            return formatLocation(location);
        }
    }

    private static String processAddresses(List<Address> addresses, Location location) {
        if (addresses != null && !addresses.isEmpty()) {
            Address address = addresses.get(0);
            String city = address.getLocality();
            if (city == null) {
                city = address.getSubAdminArea(); // Fallback
            }
            if (city != null) {
                return city;
            }
        }
        return formatLocation(location);
    }
}
