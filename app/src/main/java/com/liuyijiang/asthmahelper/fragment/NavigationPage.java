package com.liuyijiang.asthmahelper.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.liuyijiang.asthmahelper.R;
import com.liuyijiang.asthmahelper.tool.MultiLinesRadioGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.content.Context.MODE_MULTI_PROCESS;

public class NavigationPage extends Fragment implements OnMapReadyCallback {

    private SharedPreferences config;
    private SharedPreferences data;
    private SharedPreferences.Editor editor;

    private View navigationView;

    private TextView textStartPoint;
    private TextView textDestination;
    private ImageView imageSearch;

    private MapFragment mapFragment;
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private FusedLocationProviderClient mFusedLocationClient;
    private LatLng myLocation;

    private LatLng startPoint;
    private LatLng destination;
    private boolean navigate = false;

    private ImageView buttonCancelStart;
    private ImageView buttonCancelDestination;
    private ImageView mapSettingSwitchOn;
    private ImageView mapSettingSwitchOff;

    private RelativeLayout mapSetting;

    private RadioGroup groupTravelMode;
    private MultiLinesRadioGroup groupGuideMode;
    private ProgressBar progressBar;
//    private RadioButton optionDriving;
//    private RadioButton optionWalking;
//    private RadioButton optionBicycling;
//    private RadioButton optionShortest;
//    private RadioButton optionLessAirPollution;
//    private RadioButton optionLessPollenCount;
//    private RadioButton optionCombination;

    private static final int ADDRESS = 0;
    private static final int LATITUDE = 1;
    private static final int LONGITUDE = 2;
    private static final String[] START = {"Start Address", "Start Latitude", "Start Longitude"};
    private static final String[] DESTINATION = {"Destination", "Destination Latitude", "Destination Longitude"};
    private static final String MY_LOCATION = "My Location";
    private static final int DRIVING = 1;
    private static final int WALKING = 2;
    private static final int BICYCLING = 3;
    private static final int SHORTEST = 1;
    private static final int LESS_AIR_POLLUTION = 2;
    private static final int LESS_POLLEN = 3;
    private static final int COMBINATION = 4;
    private static final String TRAVEL_MODE = "Travel Mode";
    private static final String GUIDE_MODE = "Guide Mode";
    private static final int PERMISSIONS_ACCESS_FINE_LOCATION = 1;
    private static final int START_POINT_ADDRESS_REQUEST_CODE = 1;
    private static final int DESTINATION_ADDRESS_REQUEST_CODE = 2;
    private static final LatLng MELBOURNE = new LatLng(-37.8136, 144.9631);
    private static final String API_KEY = "AIzaSyARE3munzwK3yI5uPcsweFbf52-101m_2Y";
    private static final LatLngBounds MELBOURNE_BOUNDS = new LatLngBounds(new LatLng(-38.1, 144.7), new LatLng(-37.5, 145.45));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        navigationView = inflater.inflate(R.layout.fragment_navigation, container, false);
        init();
        return navigationView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initListener();
        initView();
    }

    public void initView() {
        if (mMap != null) {
            mMap.clear();
        }
        String startLocation = config.getString(START[ADDRESS], MY_LOCATION);
        String destinationLocation = config.getString(DESTINATION[ADDRESS], MY_LOCATION);
        int travelMode = config.getInt(TRAVEL_MODE, DRIVING);
        int guideMode = config.getInt(GUIDE_MODE, COMBINATION);

        textStartPoint.setText(startLocation);
        textDestination.setText(destinationLocation);
        if (startLocation.equals(MY_LOCATION)) {
            buttonCancelStart.setVisibility(View.INVISIBLE);
        } else {
            buttonCancelStart.setVisibility(View.VISIBLE);
        }
        if (destinationLocation.equals(MY_LOCATION)) {
            buttonCancelDestination.setVisibility(View.INVISIBLE);
        } else {
            buttonCancelDestination.setVisibility(View.VISIBLE);
        }
        groupTravelMode.clearCheck();
        groupGuideMode.clearCheck();
        switch (travelMode) {
            case DRIVING:
                groupTravelMode.check(R.id.radio_driving);
                break;
            case WALKING:
                groupTravelMode.check(R.id.radio_walking);
                break;
            case BICYCLING:
                groupTravelMode.check(R.id.radio_bicycling);
                break;
        }
        switch (guideMode) {
            case SHORTEST:
                groupGuideMode.check(R.id.radio_shortest);
                break;
            case LESS_AIR_POLLUTION:
                groupGuideMode.check(R.id.radio_less_air_pollution);
                break;
            case LESS_POLLEN:
                groupGuideMode.check(R.id.radio_less_pollen_count);
                break;
            case COMBINATION:
                groupGuideMode.check(R.id.radio_combination);
                break;
        }
    }

    public void updateRoute() {
        if (navigate) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Location Permission Needed")
                            .setMessage("The function you are using needs Location Permission, please accept.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Prompt the user once explanation has been shown
                                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_ACCESS_FINE_LOCATION );
                                }
                            })
                            .create()
                            .show();
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_ACCESS_FINE_LOCATION);
                }
            } else {
                mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            guideTheWay(startPoint, destination, myLocation);
                        } else {
                            Toast.makeText(getActivity(), "Cannot get your current location", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
    }

    private void initListener() {
        config = getActivity().getSharedPreferences("config", MODE_MULTI_PROCESS);
        data = getActivity().getSharedPreferences("data", MODE_MULTI_PROCESS);
        textStartPoint.setOnClickListener(navigationPageClickListener);
        textDestination.setOnClickListener(navigationPageClickListener);
        buttonCancelStart.setOnClickListener(navigationPageClickListener);
        buttonCancelDestination.setOnClickListener(navigationPageClickListener);
        imageSearch.setOnClickListener(navigationPageClickListener);
        mapSettingSwitchOn.setOnClickListener(navigationPageClickListener);
        mapSettingSwitchOff.setOnClickListener(navigationPageClickListener);
        groupTravelMode.setOnCheckedChangeListener(navigationCheckedChangeListener);
        groupGuideMode.setOnCheckedChangeListener(navigationCheckedChangeListener);
    }

    private void init() {
        GoogleMapOptions options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_NORMAL)
                .compassEnabled(true)
                .rotateGesturesEnabled(false)
                .tiltGesturesEnabled(false);
        mapFragment = MapFragment.newInstance();
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        this.getChildFragmentManager()
                .beginTransaction()
                .add(R.id.map_container, mapFragment)
                .commit();

        textStartPoint = navigationView.findViewById(R.id.start_point_address);
        textDestination = navigationView.findViewById(R.id.destination_address);
        buttonCancelStart = navigationView.findViewById(R.id.cancel_button_start);
        buttonCancelDestination = navigationView.findViewById(R.id.cancel_button_destination);
        imageSearch = navigationView.findViewById(R.id.search_button);

        mapSettingSwitchOn = navigationView.findViewById(R.id.map_setting_switch);
        mapSettingSwitchOff = navigationView.findViewById(R.id.back_arrow);
        mapSetting = navigationView.findViewById(R.id.map_setting);

        groupTravelMode = navigationView.findViewById(R.id.travel_mode);
        groupGuideMode = navigationView.findViewById(R.id.guide_mode);
        progressBar = navigationView.findViewById(R.id.progress_bar);
//        optionDriving = navigationView.findViewById(R.id.radio_driving);
//        optionWalking = navigationView.findViewById(R.id.radio_walking);
//        optionBicycling = navigationView.findViewById(R.id.radio_bicycling);
//        optionShortest = navigationView.findViewById(R.id.radio_shortest);
//        optionLessAirPollution = navigationView.findViewById(R.id.radio_less_air_pollution);
//        optionLessPollenCount = navigationView.findViewById(R.id.radio_less_pollen_count);
//        optionCombination = navigationView.findViewById(R.id.radio_combination);
    }

    private View.OnClickListener navigationPageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.start_point_address:
                    try {
                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setBoundsBias(MELBOURNE_BOUNDS).build(getActivity());
                        startActivityForResult(intent, START_POINT_ADDRESS_REQUEST_CODE);
                    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                        Toast.makeText(getActivity(), "Google Play Services is not available", Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.destination_address:
                    try {
                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setBoundsBias(MELBOURNE_BOUNDS).build(getActivity());
                        startActivityForResult(intent, DESTINATION_ADDRESS_REQUEST_CODE);
                    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                        Toast.makeText(getActivity(), "Google Play Services is not available", Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.cancel_button_start:
                    textStartPoint.setText(MY_LOCATION);
                    buttonCancelStart.setVisibility(View.INVISIBLE);
                    removeAddress(START);
                    break;
                case R.id.cancel_button_destination:
                    textDestination.setText(MY_LOCATION);
                    buttonCancelDestination.setVisibility(View.INVISIBLE);
                    removeAddress(DESTINATION);
                    break;
                case R.id.search_button:
                    progressBar.setVisibility(View.VISIBLE);
                    imageSearch.setOnClickListener(null);
                    textStartPoint.setOnClickListener(null);
                    textDestination.setOnClickListener(null);
                    startPoint = getLatLngFromLog(START);
                    destination = getLatLngFromLog(DESTINATION);
                    if (startPoint == null || destination == null) {
                        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle("Location Permission Needed")
                                        .setMessage("The function you are using needs Location Permission, please accept.")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                //Prompt the user once explanation has been shown
                                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_ACCESS_FINE_LOCATION );
                                            }
                                        })
                                        .create()
                                        .show();
                            } else {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_ACCESS_FINE_LOCATION);
                            }
                        } else {
                            mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                        if (startPoint == null) {
                                            startPoint = myLocation;
                                        }
                                        if (destination == null) {
                                            destination = myLocation;
                                        }
                                        guideTheWay(startPoint, destination);
                                    } else {
                                        Toast.makeText(getActivity(), "Cannot get your current location", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    } else {
                        guideTheWay(startPoint, destination);
                    }
                    break;
                case R.id.map_setting_switch:
                    mapSetting.setVisibility(View.VISIBLE);
                    mapSettingSwitchOn.setVisibility(View.INVISIBLE);
                    break;
                case R.id.back_arrow:
                    mapSetting.setVisibility(View.INVISIBLE);
                    mapSettingSwitchOn.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener navigationCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            switch (checkedId) {
                case R.id.radio_driving:
                    putTravelMode(DRIVING);
                    break;
                case R.id.radio_walking:
                    putTravelMode(WALKING);
                    break;
                case R.id.radio_bicycling:
                    putTravelMode(BICYCLING);
                    break;
                case R.id.radio_shortest:
                    putGuideMode(SHORTEST);
                    break;
                case R.id.radio_less_air_pollution:
                    putGuideMode(LESS_AIR_POLLUTION);
                    break;
                case R.id.radio_less_pollen_count:
                    putGuideMode(LESS_POLLEN);
                    break;
                case R.id.radio_combination:
                    putGuideMode(COMBINATION);
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mUiSettings.setMyLocationButtonEnabled(true);
                        mMap.setMyLocationEnabled(true);
                        View locationButton = ((View) Objects.requireNonNull(mapFragment.getView()).findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                        rlp.setMargins(0, 0, 0, 30);
                        mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
                                }
                            }
                        });
                    }
                } else {
                    mUiSettings.setMyLocationButtonEnabled(false);
                    mMap.setMyLocationEnabled(false);
                    Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MELBOURNE, 15));
        mUiSettings = mMap.getUiSettings();

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Location Permission Needed")
                        .setMessage("The function you are using needs Location Permission, please accept.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_ACCESS_FINE_LOCATION );
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_ACCESS_FINE_LOCATION);
            }
        } else {
            mUiSettings.setMyLocationButtonEnabled(true);
            mMap.setMyLocationEnabled(true);
            View locationButton = ((View) Objects.requireNonNull(mapFragment.getView()).findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            rlp.setMargins(0, 0, 0, 30);
            mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
                        }
                    }
                });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case START_POINT_ADDRESS_REQUEST_CODE:
                if (resultCode == -1) {
                    Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                    textStartPoint.setText(place.getName());
                    buttonCancelStart.setVisibility(View.VISIBLE);
                    String address = (String) place.getName();
                    long latitude = Double.doubleToRawLongBits(place.getLatLng().latitude);
                    long longitude = Double.doubleToRawLongBits(place.getLatLng().longitude);
                    putAddress(START, address, latitude, longitude);
                }
                break;
            case DESTINATION_ADDRESS_REQUEST_CODE:
                if (resultCode == -1) {
                    Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                    textDestination.setText(place.getName());
                    buttonCancelDestination.setVisibility(View.VISIBLE);
                    String address = (String) place.getName();
                    long latitude = Double.doubleToRawLongBits(place.getLatLng().latitude);
                    long longitude = Double.doubleToRawLongBits(place.getLatLng().longitude);
                    putAddress(DESTINATION, address, latitude, longitude);
                }
                break;
        }
    }

    private void putAddress(String[] target, String address, long latitude, long longitude) {
        editor = config.edit();
        editor.putString(target[ADDRESS], address)
                .putLong(target[LATITUDE], latitude)
                .putLong(target[LONGITUDE], longitude)
                .apply();
    }

    private void removeAddress(String[] target) {
        editor = config.edit();
        editor.remove(target[ADDRESS])
                .remove(target[LATITUDE])
                .remove(target[LONGITUDE])
                .apply();
    }

    private void putTravelMode(int mode) {
        editor = config.edit();
        editor.putInt(TRAVEL_MODE, mode)
                .apply();
    }

    private void putGuideMode(int mode) {
        editor = config.edit();
        editor.putInt(GUIDE_MODE, mode)
                .apply();
    }

    private LatLng getLatLngFromLog(String[] target) {
        if (config.contains(target[ADDRESS])) {
            long defaultLat = Double.doubleToRawLongBits(-37.8136);
            long defaultLng = Double.doubleToRawLongBits(144.9631);
            double latitude = Double.longBitsToDouble(config.getLong(target[LATITUDE], defaultLat));
            double longitude = Double.longBitsToDouble(config.getLong(target[LONGITUDE], defaultLng));
            return new LatLng(latitude, longitude);
        }
        return null;
    }

    private String getAddressFromLog(String[] target) {
        if (config.contains(target[ADDRESS])) {
            return config.getString(target[ADDRESS], null);
        }
        return null;
    }

    private void guideTheWay(LatLng start, LatLng end) {
//        LatLngBounds bounds = getBounds(start, end);
        navigate = true;
        final String urlString = getRequestUrl(start, end);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String data = downloadJson(urlString);
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    final List<List<HashMap<String, String>>> routes = parseJson(jsonObject);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            drawOnMap(routes);
                            imageSearch.setOnClickListener(navigationPageClickListener);
                            textStartPoint.setOnClickListener(navigationPageClickListener);
                            textDestination.setOnClickListener(navigationPageClickListener);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
//        String info = String.format(Locale.ENGLISH, "From (%f, %f) to (%f, %f)", start.latitude, start.longitude, end.latitude, end.longitude);
//        Toast.makeText(getActivity(), info, Toast.LENGTH_LONG).show();
    }

    private void guideTheWay(LatLng start, LatLng end, LatLng wayPoint) {
        final String urlString = getRequestUrl(start, end, wayPoint);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String data = downloadJson(urlString);
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    final List<List<HashMap<String, String>>> routes = parseJson(jsonObject);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            drawOnMap(routes);
                            imageSearch.setOnClickListener(navigationPageClickListener);
                            textStartPoint.setOnClickListener(navigationPageClickListener);
                            textDestination.setOnClickListener(navigationPageClickListener);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void drawOnMap(List<List<HashMap<String, String>>> result) {
        mMap.clear();
        ArrayList<LatLng> points = null;
//        ArrayList<LatLng> finalPoints = null;
        PolylineOptions lineOptions = new PolylineOptions();
        String startAddress = getAddressFromLog(START);
        String endAddress = getAddressFromLog(DESTINATION);
        LatLngBounds bounds;
        LatLng start = getLatLngFromLog(START);
        LatLng end = getLatLngFromLog(DESTINATION);
        double[] latlngs;
//        int maxAQI = 1000;
//        int maxPollenCount = 1000;
//        int mode = config.getInt(GUIDE_MODE, COMBINATION);

        if (start == null) {
            startAddress = "Your Location";
            start = myLocation;
        }
        if (end == null) {
            endAddress = "Your Location";
            end = myLocation;
        }
        latlngs = getBounds(start, end);
        bounds = new LatLngBounds(new LatLng(latlngs[0], latlngs[1]), new LatLng(latlngs[2], latlngs[3]));

        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();
//            int maxPointAQI = 0, maxPointPollenCount = 0;
            double minLat = latlngs[0], minLng = latlngs[1], maxLat = latlngs[2], maxLng = latlngs[3];

            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                if (lat < minLat) {
                    minLat = lat;
                }
                if (maxLat < lat) {
                    maxLat = lat;
                }
                double lng = Double.parseDouble(point.get("lng"));
                if (lng < minLng) {
                    minLng = lng;
                }
                if (maxLng < lng) {
                    maxLng = lng;
                }
                LatLng position = new LatLng(lat, lng);
//                if (mode == LESS_AIR_POLLUTION || mode == COMBINATION) {
//                    int aqi = 25;
//                    if (aqi > maxPointAQI) {
//                        maxPointAQI = aqi;
//                    }
//                }
//                if (mode == LESS_POLLEN || mode == COMBINATION) {
//                    int pollen = 25;
//                    if (pollen > maxPointPollenCount) {
//                        maxPointPollenCount = pollen;
//                    }
//                }
                points.add(position);
            }
//            if (mode == SHORTEST) {
//                finalPoints = points;
//                bounds = new LatLngBounds(new LatLng(minLat, minLng), new LatLng(maxLat, maxLng));
//            } else if (mode == LESS_AIR_POLLUTION) {
//                if (maxPointAQI < maxAQI) {
//                    maxAQI = maxPointAQI;
//                    finalPoints = points;
//                    bounds = new LatLngBounds(new LatLng(minLat, minLng), new LatLng(maxLat, maxLng));
//                }
//            } else if (mode == LESS_POLLEN) {
//                if (maxPointPollenCount < maxPollenCount) {
//                    maxPollenCount = maxPointPollenCount;
//                    finalPoints = points;
//                    bounds = new LatLngBounds(new LatLng(minLat, minLng), new LatLng(maxLat, maxLng));
//                }
//            } else {
//                if ((maxPointAQI + maxPointPollenCount) < (maxAQI + maxPollenCount)) {
//                    maxAQI = maxPointAQI;
//                    maxPollenCount = maxPointPollenCount;
//                    finalPoints = points;
//                    bounds = new LatLngBounds(new LatLng(minLat, minLng), new LatLng(maxLat, maxLng));
//                }
//            }
        }

        // Drawing polyline in the Google Map for the i-th route
        mMap.clear();
        if (points == null || points.size() == 0) {
            String tip = String.format(Locale.ENGLISH, "There is no way from %s to %s", startAddress, endAddress);
            Toast.makeText(getActivity(), tip, Toast.LENGTH_LONG).show();
        } else {
            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(10);
            // Changing the color polyline according to the mode
            lineOptions.color(Color.BLUE);
            mMap.addPolyline(lineOptions);
        }
        mMap.addMarker(new MarkerOptions().position(start).title(startAddress).alpha(0.8f));
        mMap.addMarker(new MarkerOptions().position(end).title(endAddress).alpha(0.8f));
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 300));
    }

    private List<List<HashMap<String, String>>> parseJson(JSONObject jsonObject) {
        List<List<HashMap<String, String>>> routes = new ArrayList<>();
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;
        int minAqi = 1000, minPollen = 1000;
        int mode = config.getInt(GUIDE_MODE, COMBINATION);

        try {
            jRoutes = jsonObject.getJSONArray("routes");
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List<HashMap<String, String>> path = new ArrayList<>();

                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                    int aqiMax = 0, pollenMax = 0;

                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<>();
                            double lat = (list.get(l)).latitude;
                            double lng = (list.get(l)).longitude;
                            hm.put("lat", Double.toString(lat));
                            hm.put("lng", Double.toString(lng));
                            path.add(hm);
                            if ((l == 0) || (l == (list.size() - 1))) {
                                int aqi = 0, pollen = 0;
                                if (mode == LESS_AIR_POLLUTION || mode == COMBINATION) {
                                    aqi = getAQIData(lat, lng);
                                }
                                if (aqi > aqiMax) {
                                    aqiMax = aqi;
                                }
                                if (mode == LESS_POLLEN || mode == COMBINATION) {
                                    pollen = getPollenCountData(lat, lng);
                                }
                                if (pollen > pollenMax) {
                                    pollenMax = pollen;
                                }
                            }
                        }
                    }
                    if (mode == LESS_AIR_POLLUTION) {
                        if (aqiMax < minAqi) {
                            routes.clear();
                            routes.add(path);
                        }
                    } else if (mode == LESS_POLLEN) {
                        if (pollenMax < minPollen) {
                            routes.clear();
                            routes.add(path);
                        }
                    } else if (mode == COMBINATION) {
                        if ((aqiMax + pollenMax) < (minAqi + minPollen)) {
                            routes.clear();
                            routes.add(path);
                        }
                    } else {
                        routes.clear();
                        routes.add(path);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return routes;
    }

    /**
     * Method to decode polyline points Courtesy :
     * jeffreysambells.com/2010/05/27
     * /decoding-polylines-from-google-maps-direction-api-with-java
     */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    private String downloadJson(String urlString) {
        HttpURLConnection connection = null;
        String data = "";
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            data = response.toString();
            reader.close();
            in.close();
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return data;
    }

    private String getRequestUrl(LatLng start, LatLng end) {
        String origin = "origin=" + start.latitude + "," + start.longitude;
        String dest = "destination=" + end.latitude + "," + end.longitude;
        String mode = "mode=driving";
        String alternatives;
        int travelMode = config.getInt(TRAVEL_MODE, DRIVING);
        switch (travelMode) {
            case DRIVING:
                mode = "mode=driving";
                break;
            case WALKING:
                mode = "mode=walking";
                break;
            case BICYCLING:
                mode = "mode=bicycling";
                break;
        }
        if (config.getInt(GUIDE_MODE, COMBINATION) == 1) {
            alternatives = "alternatives=false";
        } else {
            alternatives = "alternatives=true";
        }
        String key = String.format(Locale.ENGLISH, "key=%s", API_KEY);
        String parameters = origin + "&" + dest + "&" + mode + "&" + alternatives + "&" + key;
        String outputFormat = "json";
        String api = "https://maps.googleapis.com/maps/api/directions/";
        return String.format(Locale.ENGLISH, "%s%s?%s", api, outputFormat, parameters);
    }

    private String getRequestUrl(LatLng start, LatLng end, LatLng wayPoint) {
        String origin = "origin=" + start.latitude + "," + start.longitude;
        String dest = "destination=" + end.latitude + "," + end.longitude;
        String waypoints = "waypoints=" + wayPoint.latitude + "," + wayPoint.longitude;
        String mode = "mode=driving";
        String alternatives;
        int travelMode = config.getInt(TRAVEL_MODE, DRIVING);
        switch (travelMode) {
            case DRIVING:
                mode = "mode=driving";
                break;
            case WALKING:
                mode = "mode=walking";
                break;
            case BICYCLING:
                mode = "mode=bicycling";
                break;
        }
        if (config.getInt(GUIDE_MODE, COMBINATION) == 1) {
            alternatives = "alternatives=false";
        } else {
            alternatives = "alternatives=true";
        }
        String key = String.format(Locale.ENGLISH, "key=%s", API_KEY);
        String parameters = origin + "&" + dest + "&" + waypoints + "&" + mode + "&" + alternatives + "&" + key;
        String outputFormat = "json";
        String api = "https://maps.googleapis.com/maps/api/directions/";
        return String.format(Locale.ENGLISH, "%s%s?%s", api, outputFormat, parameters);
    }

    private double[] getBounds(LatLng start, LatLng end) {
        double lat1 = start.latitude;
        double lat2 = end.latitude;
        double lng1 = start.longitude;
        double lng2 = end.longitude;
        double minLat, maxLat, minLng, maxLng;
        if (lat1 <= lat2) {
            minLat = lat1;
            maxLat = lat2;
        } else {
            minLat = lat2;
            maxLat = lat1;
        }
        if (lng1 <= lng2) {
            minLng = lng1;
            maxLng = lng2;
        } else {
            minLng = lng2;
            maxLng = lng1;
        }
        return new double[]{minLat, minLng, maxLat, maxLng};
    }

    private int getAQIData(double latitude, double longitude) {
        if ((-38.1 <= latitude) && (latitude <= -37.5) && (144.7 <= longitude) && (longitude <= 145.45)) {
            List<Integer> aqiList = new ArrayList<>();
            int aqi, rowNum = 0, columnNum = 0;
            double ratioY = 0d, ratioX = 0d;
            for (int i = 3; i < 33; i++) {
                String name = String.format(Locale.ENGLISH, "AQI %d", i);
                aqi = data.getInt(name, -1);
                aqiList.add(aqi);
            }
            for (double lat = -37.5; lat > -38.11; lat -= 0.15) {
                if (latitude >= lat) {
                    ratioY = (latitude - lat) / 0.15;
                    break;
                }
                rowNum++;
            }
            for (double lng = 144.7; lng < 145.46; lng += 0.15) {
                if (longitude <= lng) {
                    ratioX = (lng - longitude) / 0.15;
                    break;
                }
                columnNum++;
            }
            int line1 = rowNum - 1;
            int line2 = rowNum;
            int column1 = columnNum - 1;
            int column2 = columnNum;
            if (line1 < 0) {
                line1 = 0;
            }
            if (column1 < 0) {
                column1 = 0;
            }
            int index1 = line1 * 6 + column1;
            int index2 = line1 * 6 + column2;
            int index3 = line2 * 6 + column1;
            int index4 = line2 * 6 + column2;
            int aqi1 = aqiList.get(index1);
            int aqi2 = aqiList.get(index2);
            int aqi3 = aqiList.get(index3);
            int aqi4 = aqiList.get(index4);
            double aqi5 = aqi2 + (aqi1 - aqi2) * ratioX;
            double aqi6 = aqi4 + (aqi3 - aqi4) * ratioX;
            return (int) Math.round(aqi6 + (aqi5 - aqi6) * ratioY);
        } else {
            HttpURLConnection connection = null;
            int aqi = -1;
            String urlString = String.format(Locale.ENGLISH, "https://api.waqi.info/feed/geo:%f;%f/?token=ae9d2b8b19da4591becde868dfa5078432feb2b5", latitude, longitude);
            try {
                URL url = new URL(urlString);
                int times = 0;
                boolean status = false;
                while (times < 5 && !status) {
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    aqi = parseJSON(response.toString());
                    if (aqi != -1) {
                        status = true;
                    }
                    reader.close();
                    in.close();
                    connection.disconnect();
                    times++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return aqi;
        }
    }

    private int getPollenCountData(double latitude, double longitude) {
        int pollenCount;
        if ((-38.1 <= latitude) && (latitude <= -37.5) && (144.7 <= longitude) && (longitude <= 145.45)) {
            List<Integer> pollenCountList = new ArrayList<>();
            int pollen, rowNum = 0, columnNum = 0;
            double ratioY = 0d, ratioX = 0d;
            for (int i = 3; i < 33; i++) {
                String name = String.format(Locale.ENGLISH, "Pollen Count %d", i);
                pollen = data.getInt(name, -1);
                pollenCountList.add(pollen);
            }
            for (double lat = -37.5; lat > -38.11; lat -= 0.15) {
                if (latitude >= lat) {
                    ratioY = (latitude - lat) / 0.15;
                    break;
                }
                rowNum++;
            }
            for (double lng = 144.7; lng < 145.46; lng += 0.15) {
                if (longitude <= lng) {
                    ratioX = (lng - longitude) / 0.15;
                    break;
                }
                columnNum++;
            }
            int line1 = rowNum - 1;
            int line2 = rowNum;
            int column1 = columnNum - 1;
            int column2 = columnNum;
            if (line1 < 0) {
                line1 = 0;
            }
            if (column1 < 0) {
                column1 = 0;
            }
            int index1 = line1 * 6 + column1;
            int index2 = line1 * 6 + column2;
            int index3 = line2 * 6 + column1;
            int index4 = line2 * 6 + column2;
            int pollen1 = pollenCountList.get(index1);
            int pollen2 = pollenCountList.get(index2);
            int pollen3 = pollenCountList.get(index3);
            int pollen4 = pollenCountList.get(index4);
            double pollen5 = pollen2 + (pollen1 - pollen2) * ratioX;
            double pollen6 = pollen4 + (pollen3 - pollen4) * ratioX;
            pollenCount = (int) Math.round(pollen6 + (pollen5 - pollen6) * ratioY);
        } else {
            pollenCount = getPoissonVariable(25);
        }
        return pollenCount;
    }

    private static int getPoissonVariable(double lambda) {
        int x = 0;
        double y = Math.random(), cdf = getPoissonProbability(x, lambda);
        while (cdf < y) {
            x++;
            cdf += getPoissonProbability(x, lambda);
        }
        double rate = Math.random() * 4;
        if (x > 25) {
            x = (int) ((x - 25) * rate) + 25;
        }
        return x;
    }

    private static double getPoissonProbability(int k, double lambda) {
        double c = Math.exp(-lambda), sum = 1;
        for (int i = 1; i <= k; i++) {
            sum *= lambda / i;
        }
        return sum * c;
    }

    private int parseJSON(String jsonString) {
        try {
            JSONObject jsonObj = new JSONObject(jsonString);
            JSONObject data = jsonObj.getJSONObject("data");
            return Integer.parseInt(data.getString("aqi"));
        } catch (Exception e) {
            return -1;
        }
    }
}
