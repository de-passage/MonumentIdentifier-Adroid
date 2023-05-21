package com.example.watchapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private LocationManager locationManager;

    private double currentLatitude;
    private double currentLongitude;

    private double searchRadius = 200;

    private TextView radiusTextView;

    private Button searchButton;

    private LinearLayout monumentList;

    final LinearLayout.LayoutParams monumentListItemLayoutParam = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);


    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Radius of the earth in meters
        final int R = 6371000;

        // Convert degrees to radians
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        // Haversine formula
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // return distance in meters
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = getSystemService(LocationManager.class);
        radiusTextView = findViewById(R.id.radius_textview);
        monumentList = findViewById(R.id.monument_list);
        SeekBar radiusSeekBar = findViewById(R.id.radius_seek_bar);
        searchButton = findViewById(R.id.search_button);
        searchButton.setEnabled(false);
        getLocationPermission();

        searchButton.setOnClickListener(v -> getLocation());

        updateRadiusTextView();

        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int radius, boolean fromUser) {
                searchRadius = radius + 1;
                updateRadiusTextView();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        Wearable.getMessageClient(this).addListener(this);
    }

    private void updateRadiusTextView() {
        radiusTextView.setText(getString(R.string.search_radius, String.valueOf(searchRadius)));
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                                              Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    currentLongitude = location.getLongitude();
                    currentLatitude = location.getLatitude();
                }
                public void onProviderEnabled(String provider) {}
                public void onProviderDisabled(String provider) {}
            };

            // Register the listener with the Location Manager to receive location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            getLocation();
        } else {
            // Request the permission
            ActivityCompat.requestPermissions(this,
                                              new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                              LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                                              Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                this.currentLongitude = location.getLongitude();
                this.currentLatitude = location.getLatitude();
                getMonuments();
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            toastPermissionDenied();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                toastPermissionDenied();
            }
        }
    }

    private void toastPermissionDenied() {
        Toast.makeText(this, "Geolocation denied. Application won't work properly.",
                       Toast.LENGTH_SHORT).show();

    }

    private void getMonuments() {
        searchButton.setEnabled(false);
        monumentList.removeAllViews();
        addItemToMonumentList(getString(R.string.waiting_for_data));
        Log.i("MainActivity",
              "Getting monuments, lat: " + currentLatitude + ", long: " + currentLongitude);
        MonumentService monumentService = new MonumentService();
        monumentService.getMonuments(new Coordinate(currentLatitude, currentLongitude),
                                     searchRadius).onResponse((call, response) -> {
            searchButton.setEnabled(true);
            if (response.isSuccessful()) {
                MonumentResponse monumentResponse = response.body();
                List<MonumentInfo> monumentInfoList = new ArrayList<>();
                if (monumentResponse != null && monumentResponse.monuments.length > 0) {
                    monumentList.removeAllViews();
                    Arrays.stream(monumentResponse.monuments).map(MonumentInfo::new)
                            .sorted(Comparator.comparing(m -> m.distance))
                            .forEach(info -> {
                                monumentInfoList.add(info);
                                addItemToMonumentList(info.toString());
                            });

                    sendMonumentListToWear(monumentInfoList);
                } else {
                    noMonumentFount();
                }
            }
        }).onFailure((call, t) -> {
            searchButton.setEnabled(true);
            noMonumentFount();
        }).request();
    }

    private void sendMonumentListToWear(List<MonumentInfo> monumentInfoList) {
        CapabilityClient capabilityClient = Wearable.getCapabilityClient(this);
        Task<Map<String, CapabilityInfo>> capabilityTask =
                capabilityClient.getAllCapabilities(CapabilityClient.FILTER_REACHABLE);

        capabilityTask.addOnSuccessListener(capabilityInfoMap -> {
            if (capabilityInfoMap.isEmpty()) {
                Log.i("MainActivity", "No wear devices found");
                return;
            }

            CapabilityInfo capacityInfo = capabilityInfoMap.get("receive_monument_list");
            if (capacityInfo == null) {
                Log.i("MainActivity",
                      "No wear devices with the required capability found");
                return;
            }

            Set<Node> nodes = capacityInfo.getNodes();
            if (!nodes.isEmpty()) {
                Node node = nodes.iterator().next();
                Log.i("MainActivity", "Node found: " + node.getDisplayName());
                MessageClient messageClient = Wearable.getMessageClient(this);
                messageClient.sendMessage(node.getId(), "/monument_list",
                                          monumentInfoList.stream().map(MonumentInfo::toString).collect(
                                                  Collectors.joining("\n")).getBytes());
            } else {
                Log.i("MainActivity", "No wear devices with the required capability found: " + capacityInfo);
            }
        });
    }

    private void addItemToMonumentList(String s) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(monumentListItemLayoutParam);
        textView.setText(s);
        monumentList.addView(textView);
    }

    public String processString(String input) {
        StringBuilder output = new StringBuilder();
        int lastIndex = 0;
        int bracketStart;
        while ((bracketStart = input.indexOf("[[", lastIndex)) != -1) {
            // Append the part of the string before the brackets
            output.append(input.substring(lastIndex, bracketStart));
            int bracketEnd = input.indexOf("]]", bracketStart);
            if (bracketEnd != -1) {
                String insideBrackets = input.substring(bracketStart + 2, bracketEnd);
                int pipeIndex = insideBrackets.indexOf("|");
                if (pipeIndex != -1) {
                    // Append the part after '|'
                    output.append(insideBrackets.substring(pipeIndex + 1));
                } else {
                    // Append the string inside the brackets
                    output.append(insideBrackets);
                }
                lastIndex = bracketEnd + 2;
            } else {
                // If there's no matching end bracket, append the rest of the string and break
                output.append(input.substring(bracketStart));
                break;
            }
        }
        // Append the remainder of the string after the last end bracket
        output.append(input.substring(lastIndex));

        return output.toString();
    }


    private void noMonumentFount() {
        Toast.makeText(this, "No monument found around the current location", Toast.LENGTH_SHORT)
                .show();
        monumentList.removeAllViews();
        addItemToMonumentList(getString(R.string.no_monument_around));
        sendMonumentListToWear(Collections.emptyList());
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Log.i("MainActivity", "Message received: " + messageEvent.getPath());
        getMonuments();
    }

    public class MonumentInfo {
        public final String name;
        public final double latitude;
        public final double longitude;
        public final double distance;

        public MonumentInfo(Monument monument) {
            this.name = processString(monument.name);
            this.latitude = monument.lat;
            this.longitude = monument.lon;
            this.distance =
                    calculateDistance(currentLatitude, currentLongitude, latitude, longitude);
        }

        @NonNull
        public String toString() {
            if (distance < 1000) {
                int distanceInMeters = (int) Math.round(distance);
                return name + " " + distanceInMeters + "m";
            } else {
                double distanceInKm = distance / 1000.0;
                DecimalFormat df = new DecimalFormat("#.##");
                return name + " " + df.format(distanceInKm) + "km";
            }
        }
    }
}

