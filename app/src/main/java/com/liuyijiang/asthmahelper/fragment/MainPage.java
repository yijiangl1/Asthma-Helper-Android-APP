package com.liuyijiang.asthmahelper.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.liuyijiang.asthmahelper.R;
import com.liuyijiang.asthmahelper.tool.MultiLinesRadioGroup;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_MULTI_PROCESS;

public class MainPage extends Fragment implements OnMapReadyCallback {

    private SharedPreferences config;
    private SharedPreferences data;
    private SharedPreferences.Editor editor;

    private View mainView;
    private MapFragment mapFragment;
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private TileOverlay aqiOverlay = null;
    private TileOverlay pollenCountOverlay = null;
    private MultiLinesRadioGroup groupMode;
    private Marker marker1;
    private Marker marker2;
    private Marker marker3;
    private Marker marker;
    private TextView addressSelector;
    private TextView tipTitle;
    private TextView tipDescription;
    private ImageView cancelButton;
    private int informationIndex = 0;

    private int[] aqiColors = {
            Color.rgb(0, 153, 102),
            Color.rgb(255, 222, 51),
            Color.rgb(255, 153, 51),
            Color.rgb(204, 0, 51),
            Color.rgb(102, 0, 153),
            Color.rgb(126, 0, 35)
    };
    private int[] pollenCountColors = {
            Color.rgb(0, 153, 102),
            Color.rgb(255, 222, 51),
            Color.rgb(255, 153, 51),
            Color.rgb(204, 0, 51)
    };

    private static final String GOOD = "Good";
    private static final String MODERATE = "Moderate";
    private static final String POOR = "Poor";
    private static final String UNHEALTHY = "Unhealthy";
    private static final String VERY_UNHEALTHY = "Very Unhealthy";
    private static final String LOW = "Low";
    private static final String MEDIUM = "Medium";
    private static final String HIGH = "High";
    private static final String IS_AQI = "isAQI";
    private static final int ADDRESS = 0;
    private static final int LATITUDE = 1;
    private static final int LONGITUDE = 2;
    private static final String[] SELECTED = {"Selected Address", "Selected Latitude", "Selected Longitude"};
    private static final String[] ADDRESS1 = {"Address1", "Address1 Latitude", "Address1 Longitude"};
    private static final String[] ADDRESS2 = {"Address2", "Address2 Latitude", "Address2 Longitude"};
    private static final String[] ADDRESS3 = {"Address3", "Address3 Latitude", "Address3 Longitude"};
    private static final String AQI = "AQI";
    private static final String POLLEN_COUNT = "Pollen Count";
    private static final int TITLE = 0;
    private static final int DESCRIPTION = 1;
    private static final String[] TIP1 = {"Use Allergy-Proof Covers on Pillows and Mattresses", "Wash bedding weekly in hot water (above 130 degrees F) to get rid of dust mites and use a dehumidifier to reduce excess moisture and help prevent mold in your home."};
    private static final String[] TIP2 = {"Do not Allow Pets in Bedrooms or on Furniture", "Pet dander - a common asthma trigger - is often difficult to avoid entirely because for many of us, our pets are just like members of the family."};
    private static final String[] TIP3 = {"Remove Carpets and Stuffed Toys from Bedrooms", "If carpeting cannot be removed, vacuum at least twice a week with a cleaner equipped with a HEPA air filter. Ask your doctor about which cleaning products are best to use."};
    private static final String[] TIP4 = {"Fix Leaky Faucets", "Mold is a common asthma trigger. To reduce mold in your home, remove household plants and keep bathrooms clean and dry by opening a window or using a bathroom fan during showers or baths."};
    private static final String[] TIP5 = {"Avoid Areas where People Smoke", "Breathing smoke - even secondhand smoke and smoke on clothing, furniture or drapes - can trigger an asthma attack. Be sure to ask for a smoke-free hotel room when traveling."};
    private static final String[] TIP6 = {"Avoid Harsh Cleaning Products and Chemicals", "Fumes from household cleaners can trigger asthma. Avoid inhaling fumes at home and prevent exposure away from home as much as possible."};
    private static final String[] TIP7 = {"Reduce Stress", "Intense emotions and worry often worsen asthma symptoms so take steps to relieve stress in your life. Make time for things you enjoy doing - and for relaxation."};
    private static final String[] TIP8 = {"Pay Attention to Air Quality", "Extremely hot and humid weather and poor air quality can exacerbate asthma symptoms for many people. Limit outdoor activity when these conditions exist or a pollution alert has been issued."};
    private static final String[] TIP9 = {"Exercise Indoors", "Physical activity is important - even for people with asthma. Reduce the risk for exercise-induced asthma attacks by working out inside on very cold or very warm days. Talk to your doctor about asthma and exercise."};
    private static final String[] TIP10 = {"Take Control of Your Seasonal Allergies", "Allergies and asthma are closely related, so talk to your doctor if you have hay fever. Use medications as directed and stay inside as much as possible when pollen counts are high."};
    private static final String[] TIP11 = {"Let People Around You Know You Have Asthma", "It's important for family members, friends, co-workers, teachers, and coaches to be able to recognize symptoms of an asthma attack - and know what to do if one occurs."};
    private static final String[] TIP12 = {"Keep Quick-Relief Asthma Medicines Available", "Follow policies at your child's school to make sure he or she is allowed to carry an inhaler and any other emergency rescue medications that may be necessary. Make sure the school nurse knows your child has asthma."};
    private static final String[] TIP13 = {"Talk to Your child's Teachers and Coaches", "Chalk dust can trigger an asthma attack - so it may be helpful for your child to sit away from chalkboards in class. His or her coaches and/or physical education teacher can provide important information about asthma symptoms during exercise."};
    private static final String[] TIP14 = {"Be Prepared - Just in Case", "Know the location of the nearest hospital - to your home, your job and your child's school. When you are traveling, locate the nearest emergency facility beforehand, in case of an asthma attack."};
    private static final int ADDRESS_REQUEST_CODE = 0;
    private static final String ADDRESS_TIP = "Please click and select an address";

    private static final LatLng MELBOURNE = new LatLng(-37.8136, 144.9631);
    private static final LatLngBounds MELBOURNE_BOUNDS = new LatLngBounds(new LatLng(-38.1, 144.7), new LatLng(-37.5, 145.45));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_main, container, false);
        init();
        return mainView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        config = getActivity().getSharedPreferences("config", MODE_MULTI_PROCESS);
        data = getActivity().getSharedPreferences("data", MODE_MULTI_PROCESS);
        groupMode.clearCheck();
        boolean isAQI = config.getBoolean(IS_AQI, true);
        if (isAQI) {
            groupMode.check(R.id.air_pollution_radio_button);
        } else {
            groupMode.check(R.id.pollen_count_radio_button);
        }
        groupMode.setOnCheckedChangeListener(mainCheckedChangeListener);
        addressSelector.setOnClickListener(clickListener);
        cancelButton.setOnClickListener(clickListener);
    }

    public void initView() {
        groupMode.clearCheck();
        boolean isAQI = config.getBoolean(IS_AQI, true);
        if (isAQI) {
            groupMode.check(R.id.air_pollution_radio_button);
            updateAQIMap();
        } else {
            groupMode.check(R.id.pollen_count_radio_button);
            updatePollenCountMap();
        }
        addMarker();
        if (isAddressEmpty(SELECTED)) {
            addressSelector.setText(ADDRESS_TIP);
            cancelButton.setVisibility(View.INVISIBLE);
            String[] tip = getTips();
            if (tip != null) {
                tipTitle.setText(tip[TITLE]);
                tipDescription.setText(tip[DESCRIPTION]);
            }
            if (marker != null) {
                marker.remove();
            }
        } else {
            String address = config.getString(SELECTED[ADDRESS], null);
            addressSelector.setText(address);
            cancelButton.setVisibility(View.VISIBLE);
            addOneMarker();
        }
    }

    private void init() {
        GoogleMapOptions options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_NORMAL)
                .compassEnabled(true)
                .rotateGesturesEnabled(false)
                .tiltGesturesEnabled(false);
        mapFragment = MapFragment.newInstance();
        mapFragment.getMapAsync(this);

        this.getChildFragmentManager()
                .beginTransaction()
                .add(R.id.half_map_container, mapFragment)
                .commit();

        groupMode = mainView.findViewById(R.id.show_mode);
        addressSelector = mainView.findViewById(R.id.select_address);
        cancelButton = mainView.findViewById(R.id.cancel_button);
        tipTitle = mainView.findViewById(R.id.tip_title);
        tipDescription = mainView.findViewById(R.id.tip_description);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.select_address:
                    try {
                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setBoundsBias(MELBOURNE_BOUNDS).build(getActivity());
                        startActivityForResult(intent, ADDRESS_REQUEST_CODE);
                    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                        Toast.makeText(getActivity(), "Google Play Services is not available", Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.cancel_button:
                    addressSelector.setText(ADDRESS_TIP);
                    cancelButton.setVisibility(View.INVISIBLE);
                    String[] tip = getTips();
                    if (tip != null) {
                        tipTitle.setText(tip[TITLE]);
                        tipDescription.setText(tip[DESCRIPTION]);
                    }
                    removeAddress(SELECTED);
                    if (marker != null) {
                        marker.remove();
                    }
                    break;
            }
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        mMap.setLatLngBoundsForCameraTarget(MELBOURNE_BOUNDS);
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels * 2 / 3;
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(MELBOURNE_BOUNDS, width, height, 0));
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        boolean isAQI = config.getBoolean("isAQI", true);
        if (isAQI) {
            updateAQIMap();
        } else {
            updatePollenCountMap();
        }
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomGesturesEnabled(false);
        mUiSettings.setScrollGesturesEnabled(false);
        mUiSettings.setTiltGesturesEnabled(false);
        mUiSettings.setRotateGesturesEnabled(false);
        mMap.setOnMarkerClickListener(markerClickListener);
        mMap.setOnMapClickListener(mapClickListener);
//        mMap.setOnMarkerDragListener(markerDragListener);
        addMarker();
        if (isAddressEmpty(SELECTED)) {
            addressSelector.setText(ADDRESS_TIP);
            cancelButton.setVisibility(View.INVISIBLE);
            String[] tip = getTips();
            if (tip != null) {
                tipTitle.setText(tip[TITLE]);
                tipDescription.setText(tip[DESCRIPTION]);
            }
            if (marker != null) {
                marker.remove();
            }
        } else {
            String address = config.getString(SELECTED[ADDRESS], null);
            addressSelector.setText(address);
            cancelButton.setVisibility(View.VISIBLE);
            addOneMarker();
        }
    }

//    GoogleMap.OnMarkerDragListener markerDragListener = new GoogleMap.OnMarkerDragListener() {
//        @Override
//        public void onMarkerDragStart(Marker marker) {}
//
//        @Override
//        public void onMarkerDrag(Marker marker) {}
//
//        @Override
//        public void onMarkerDragEnd(final Marker marker) {
//            mMap.setOnMapClickListener(null);
//            marker.setDraggable(false);
//            final double lat = marker.getPosition().latitude;
//            final double lng = marker.getPosition().longitude;
//            String address = String.format(Locale.ENGLISH, "(%f), (%f)", lat, lng);
//            addressSelector.setText(address);
//            cancelButton.setVisibility(View.VISIBLE);
//            long latitude = Double.doubleToRawLongBits(lat);
//            long longitude = Double.doubleToRawLongBits(lng);
//            putAddress(SELECTED, address, latitude, longitude);
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    getAQIData(lat, lng);
//                    getPollenCountData(lat, lng);
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            addOneMarker();
//                            marker.setDraggable(true);
//                            mMap.setOnMapClickListener(mapClickListener);
//                        }
//                    });
//                }
//            }).start();
//        }
//    };

    GoogleMap.OnMapClickListener mapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            if (isAddressEmpty(SELECTED)) {
                mMap.setOnMapClickListener(null);
                final double lat = latLng.latitude;
                final double lng = latLng.longitude;
                String address = String.format(Locale.ENGLISH, "(%f), (%f)", lat, lng);
                addressSelector.setText(address);
                cancelButton.setVisibility(View.VISIBLE);
                long latitude = Double.doubleToRawLongBits(lat);
                long longitude = Double.doubleToRawLongBits(lng);
                putAddress(SELECTED, address, latitude, longitude);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getAQIData(lat, lng);
                        getPollenCountData(lat, lng);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addOneMarker();
                                mMap.setOnMapClickListener(mapClickListener);
                            }
                        });
                    }
                }).start();
            } else {
                addressSelector.setText(ADDRESS_TIP);
                cancelButton.setVisibility(View.INVISIBLE);
                String[] tip = getTips();
                if (tip != null) {
                    tipTitle.setText(tip[TITLE]);
                    tipDescription.setText(tip[DESCRIPTION]);
                }
                removeAddress(SELECTED);
                if (marker != null) {
                    marker.remove();
                }
            }
        }
    };

    private void addMarker() {
        LatLng address1 = getLatLngFromLog(ADDRESS1);
        LatLng address2 = getLatLngFromLog(ADDRESS2);
        LatLng address3 = getLatLngFromLog(ADDRESS3);
        if (address1 != null) {
            String address = config.getString(ADDRESS1[0], null);
            String aqi = getData(AQI, 0);
            String pollen = getData(POLLEN_COUNT, 0);
            String description = String.format(Locale.ENGLISH, "AQI: %s, Pollen: %s", aqi, pollen);
            if (marker1 != null) {
                marker1.remove();
            }
            marker1 = mMap.addMarker(new MarkerOptions().position(address1).title(address).alpha(0.8f).snippet(description));
        } else {
            if (marker1 != null) {
                marker1.remove();
            }
        }
        if (address2 != null) {
            String address = config.getString(ADDRESS2[0], null);
            String aqi = getData(AQI, 1);
            String pollen = getData(POLLEN_COUNT, 1);
            String description = String.format(Locale.ENGLISH, "AQI: %s, Pollen: %s", aqi, pollen);
            if (marker2 != null) {
                marker2.remove();
            }
            marker2 = mMap.addMarker(new MarkerOptions().position(address2).title(address).alpha(0.8f).snippet(description));
        } else {
            if (marker2 != null) {
                marker2.remove();
            }
        }
        if (address3 != null) {
            String address = config.getString(ADDRESS3[0], null);
            String aqi = getData(AQI, 2);
            String pollen = getData(POLLEN_COUNT, 2);
            String description = String.format(Locale.ENGLISH, "AQI: %s, Pollen: %s", aqi, pollen);
            if (marker3 != null) {
                marker3.remove();
            }
            marker3 = mMap.addMarker(new MarkerOptions().position(address3).title(address).alpha(0.8f).snippet(description));
        } else {
            if (marker3 != null) {
                marker3.remove();
            }
        }
    }

    private String getData(String arg, int id) {
        int good = config.getInt(GOOD, 25) + 25;
        int moderate = config.getInt(MODERATE, 25) + 75;
        int poor = config.getInt(POOR, 25) + 125;
        int unhealthy = config.getInt(UNHEALTHY, 25) + 175;
        int veryUnhealthy = config.getInt(VERY_UNHEALTHY, 25) + 275;
        int low = config.getInt(LOW, 25) + 25;
        int medium = config.getInt(MEDIUM, 25) + 75;
        int high = config.getInt(HIGH, 25) + 155;

        String name = String.format(Locale.ENGLISH, "%s %d", arg, id);
        int argData = data.getInt(name, -1);

        if (argData == -1) {
            informationIndex = 0;
            return "No data";
        } else {
            if (arg.equals(AQI)) {
                if (argData <= good) {
                    informationIndex = 1;
                    return String.format(Locale.ENGLISH, "%d (Good)", argData);
                } else if (argData <= moderate) {
                    informationIndex = 2;
                    return String.format(Locale.ENGLISH, "%d (Moderate)", argData);
                } else if (argData <= poor) {
                    informationIndex = 3;
                    return String.format(Locale.ENGLISH, "%d (Poor)", argData);
                } else if (argData <= unhealthy) {
                    informationIndex = 4;
                    return String.format(Locale.ENGLISH, "%d (Unhealthy)", argData);
                } else if (argData <= veryUnhealthy) {
                    informationIndex = 5;
                    return String.format(Locale.ENGLISH, "%d (Very Unhealthy)", argData);
                } else {
                    informationIndex = 6;
                    return String.format(Locale.ENGLISH, "%d (Hazardous)", argData);
                }
            } else {
                if (argData <= low) {
                    informationIndex = 7;
                    return String.format(Locale.ENGLISH, "%d (Low)", argData);
                } else if (argData <= medium) {
                    informationIndex = 8;
                    return String.format(Locale.ENGLISH, "%d (Medium)", argData);
                } else if (argData <= high) {
                    informationIndex = 9;
                    return String.format(Locale.ENGLISH, "%d (High)", argData);
                } else {
                    informationIndex = 10;
                    return String.format(Locale.ENGLISH, "%d (Extreme)", argData);
                }
            }
        }
    }

    private GoogleMap.OnMarkerClickListener markerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            marker.showInfoWindow();
            return true;
        }
    };

    private RadioGroup.OnCheckedChangeListener mainCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            switch (checkedId) {
                case R.id.air_pollution_radio_button:
                    editor = config.edit();
                    editor.putBoolean(IS_AQI, true)
                            .apply();
                    updateAQIMap();
                    break;
                case R.id.pollen_count_radio_button:
                    editor = config.edit();
                    editor.putBoolean(IS_AQI, false)
                            .apply();
                    updatePollenCountMap();
                    break;
            }
        }
    };

    public void updateAQIMap() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                float num0 = 0f;
                float num1 = config.getInt(GOOD, 25) + 25;
                float num2 = config.getInt(MODERATE, 25) + 75;
                float num3 = config.getInt(POOR, 25) + 125;
                float num4 = config.getInt(UNHEALTHY, 25) + 175;
                float num5 = config.getInt(VERY_UNHEALTHY, 25) + 275;
                float num6 = 500f;
                num0 = ((num0 + num1) / 2f) / num6;
                num1 = ((num1 + num2) / 2f) / num6;
                num2 = ((num2 + num3) / 2f) / num6;
                num3 = ((num3 + num4) / 2f) / num6;
                num4 = ((num4 + num5) / 2f) / num6;
                num5 = ((num5 + num6) / 2f) / num6;
//                num0 = num0 / num6;
//                num1 = num1 / num6;
//                num2 = num2 / num6;
//                num3 = num3 / num6;
//                num4 = num4 / num6;
//                num5 = num5 / num6;

                float[] startPoints = {num0, num1, num2, num3, num4, num5};
                final Gradient gradient = new Gradient(aqiColors, startPoints);

                final List<WeightedLatLng> list = getAQIWeightedLatLng(num6);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (list.size() > 0) {
                            HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                                    .weightedData(list)
                                    .gradient(gradient)
                                    .opacity(0.5f)
                                    .radius(50)
                                    .build();
                            if (pollenCountOverlay != null) {
                                pollenCountOverlay.remove();
                            }
                            if (aqiOverlay != null) {
                                aqiOverlay.remove();
                            }
                            // Add a tile overlay to the map, using the heat map tile provider.
                            aqiOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                        }
                    }
                });
            }
        }).start();
    }

    private List<WeightedLatLng> getAQIWeightedLatLng(float upBound) {
        List<WeightedLatLng> list = new ArrayList<>();
        List<Float> rateList2 = new ArrayList<>();
        List<Float> rateList = new ArrayList<>();

        int aqi;
        int count = 0;
        float sum = 0f;
        float avg, rate;

        for (int i = 3; i < 33; i++) {
            String name = String.format(Locale.ENGLISH, "AQI %d", i);
            aqi = data.getInt(name, -1);
            float aqiRate = -1f;
            if (aqi != -1) {
                aqiRate = ((float) aqi) / upBound;
                count++;
                sum += aqiRate;
            }
            rateList2.add(aqiRate);
        }
        if (count == 0) {
            return list;
        } else {
            avg = sum / (float) count;
        }
        for (int i = 0; i < 30; i++) {
            if (rateList2.get(i) < 0f) {
                rateList2.set(i, avg);
            }
        }
        for (int i = 0; i < 357; i++) {
            rateList.add(0f);
        }
        int index;
        int index2;
        for (int i = 0; i < 17; i += 2) {
            for (int j = 0; j < 21; j++) {
                index = 21 * i + j;
                index2 = 6 * (i / 4) + (j / 4);
                if (j % 4 == 0) {
                    rateList.set(index, rateList2.get(index2));
                } else  if (j % 4 == 1) {
                    rate = (rateList2.get(index2 + 1) - rateList2.get(index2)) / 4f + rateList2.get(index2 );
                    rateList.set(index, rate);
                } else if (j % 4 == 2) {
                    rate = (rateList2.get(index2) + rateList2.get(index2 + 1)) / 2f;
                    rateList.set(index, rate);
                } else if (j % 4 == 3) {
                    rate = (rateList2.get(index2 + 1) - rateList2.get(index2)) * 3f / 4f + rateList2.get(index2);
                    rateList.set(index, rate);
                }
            }
        }
        for (int i = 2; i < 17; i += 4) {
            for (int j = 0; j < 21; j++) {
                index = 21 * i + j;
                rate = (rateList.get(index - 42) + rateList.get(index + 42)) / 2f;
                rateList.set(index, rate);
            }
        }
        for (int i = 1; i < 17; i += 4) {
            for (int j = 0; j < 21; j++) {
                index = 21 * i + j;
                rate = (rateList.get(index - 21) + rateList.get(index + 21)) / 2f;
                rateList.set(index, rate);
            }
        }
        for (int i = 3; i < 17; i += 4) {
            for (int j = 0; j < 21; j++) {
                index = 21 * i + j;
                rate = (rateList.get(index - 21) + rateList.get(index + 21)) / 2f;
                rateList.set(index, rate);
            }
        }
        double step1 = (-38.1d + 37.5d) / 16d;
        double step2 = (145.45 - 144.7) / 20d;
        count = 0;
        for (double lat = -37.5; lat > -38.1001; lat += step1) {
            for (double lng = 144.7; lng < 145.4501; lng += step2) {
                float newRate = rateList.get(count);
                count++;
                LatLng latLng = new LatLng(lat, lng);
                list.add(new WeightedLatLng(latLng, newRate));
            }
        }
        for (double lat = -35.5; lat > -36.1001; lat += step1) {
            for (double lng = 144.7; lng < 145.4501; lng += step2) {
                float newRate = 0;
                LatLng latLng = new LatLng(lat, lng);
                list.add(new WeightedLatLng(latLng, newRate));
            }
        }
        count = 0;
        for (double lat = -34.5; lat > -35.1001; lat += step1) {
            for (double lng = 144.7; lng < 145.4501; lng += step2) {
                float newRate = rateList.get(count) / avg;
                count++;
                LatLng latLng = new LatLng(lat, lng);
                list.add(new WeightedLatLng(latLng, newRate));
            }
        }
        return list;
    }

    public void updatePollenCountMap() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                float num0 = 5f;
                float num1 = config.getInt(LOW, 25) + 25;
                float num2 = config.getInt(MEDIUM, 25) + 75;
                float num3 = config.getInt(HIGH, 25) + 155;
                float num4 = 300f;
                num0 = ((num0 + num1) / 2f) / num4;
                num1 = ((num1 + num2) / 2f) /num4;
                num2 = ((num2 + num3) / 2f) / num4;
                num3 = ((num3 + num4) / 2f) / num4;
//                num0 = num0 / num4;
//                num1 = num1 /num4;
//                num2 = num2 / num4;
//                num3 = num3 / num4;
                float[] startPoints = {num0, num1, num2, num3};
                final Gradient gradient = new Gradient(pollenCountColors, startPoints);

                final List<WeightedLatLng> list = getPollenCountWeightedLatLng(num4);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (list.size() > 0) {
                            HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                                    .weightedData(list)
                                    .gradient(gradient)
                                    .opacity(0.5f)
                                    .radius(50)
                                    .build();
                            if (pollenCountOverlay != null) {
                                pollenCountOverlay.remove();
                            }
                            if (aqiOverlay != null) {
                                aqiOverlay.remove();
                            }
                            // Add a tile overlay to the map, using the heat map tile provider.
                            pollenCountOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                        }
                    }
                });
            }
        }).start();
    }

    private List<WeightedLatLng> getPollenCountWeightedLatLng(float upBound) {
        List<WeightedLatLng> list = new ArrayList<>();
        List<Float> rateList2 = new ArrayList<>();
        List<Float> rateList = new ArrayList<>();

        int pollenCount;
        int count = 0;
        float sum = 0f;
        float avg, rate;

        for (int i = 3; i < 33; i++) {
            String name = String.format(Locale.ENGLISH, "Pollen Count %d", i);
            pollenCount = data.getInt(name, -1);
            float pollenCountRate = -1f;
            if (pollenCount != -1) {
                pollenCountRate = ((float) pollenCount) / upBound;
                count++;
                sum += pollenCountRate;
            }
            rateList2.add(pollenCountRate);
        }
        if (count == 0) {
            return list;
        } else {
            avg = sum / (float) count;
        }
        for (int i = 0; i < 30; i++) {
            if (rateList2.get(i) < 0f) {
                rateList2.set(i, avg);
            }
        }
        for (int i = 0; i < 357; i++) {
            rateList.add(0f);
        }
        int index;
        int index2;
        for (int i = 0; i < 17; i += 2) {
            for (int j = 0; j < 21; j++) {
                index = 21 * i + j;
                index2 = 6 * (i / 4) + (j / 4);
                if (j % 4 == 0) {
                    rateList.set(index, rateList2.get(index2));
                } else  if (j % 4 == 1) {
                    rate = (rateList2.get(index2 + 1) - rateList2.get(index2)) / 4f + rateList2.get(index2 );
                    rateList.set(index, rate);
                } else if (j % 4 == 2) {
                    rate = (rateList2.get(index2) + rateList2.get(index2 + 1)) / 2f;
                    rateList.set(index, rate);
                } else if (j % 4 == 3) {
                    rate = (rateList2.get(index2 + 1) - rateList2.get(index2)) * 3f / 4f + rateList2.get(index2);
                    rateList.set(index, rate);
                }
            }
        }
        for (int i = 2; i < 17; i += 4) {
            for (int j = 0; j < 21; j++) {
                index = 21 * i + j;
                rate = (rateList.get(index - 42) + rateList.get(index + 42)) / 2f;
                rateList.set(index, rate);
            }
        }
        for (int i = 1; i < 17; i += 4) {
            for (int j = 0; j < 21; j++) {
                index = 21 * i + j;
                rate = (rateList.get(index - 21) + rateList.get(index + 21)) / 2f;
                rateList.set(index, rate);
            }
        }
        for (int i = 3; i < 17; i += 4) {
            for (int j = 0; j < 21; j++) {
                index = 21 * i + j;
                rate = (rateList.get(index - 21) + rateList.get(index + 21)) / 2f;
                rateList.set(index, rate);
            }
        }
        double step1 = (-38.1d + 37.5d) / 16d;
        double step2 = (145.45 - 144.7) / 20d;
        count = 0;
        for (double lat = -37.5; lat > -38.1001; lat += step1) {
            for (double lng = 144.7; lng < 145.4501; lng += step2) {
                float newRate = rateList.get(count);
                count++;
                LatLng latLng = new LatLng(lat, lng);
                list.add(new WeightedLatLng(latLng, newRate));
            }
        }
        count = 0;
        for (double lat = -36.5; lat > -37.1001; lat += step1) {
            for (double lng = 144.7; lng < 145.4501; lng += step2) {
                float newRate = rateList.get(count) / avg;
                count++;
                LatLng latLng = new LatLng(lat, lng);
                list.add(new WeightedLatLng(latLng, newRate));
            }
        }
        for (double lat = -35.5; lat > -36.1001; lat += step1) {
            for (double lng = 144.7; lng < 145.4501; lng += step2) {
                float newRate = 0;
                LatLng latLng = new LatLng(lat, lng);
                list.add(new WeightedLatLng(latLng, newRate));
            }
        }
        return list;
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

    private String[] getTips() {
        double rand = Math.random();
        int index = (int) (rand * 14) + 1;
        switch (index) {
            case 1:
                return TIP1;
            case 2:
                return TIP2;
            case 3:
                return TIP3;
            case 4:
                return TIP4;
            case 5:
                return TIP5;
            case 6:
                return TIP6;
            case 7:
                return TIP7;
            case 8:
                return TIP8;
            case 9:
                return TIP9;
            case 10:
                return TIP10;
            case 11:
                return TIP11;
            case 12:
                return TIP12;
            case 13:
                return TIP13;
            case 14:
                return TIP14;
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ADDRESS_REQUEST_CODE:
                if (resultCode == -1) {
                    Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                    addressSelector.setText(place.getName());
                    cancelButton.setVisibility(View.VISIBLE);
                    String address = (String) place.getName();
                    final double lat = place.getLatLng().latitude;
                    final double lng = place.getLatLng().longitude;
                    long latitude = Double.doubleToRawLongBits(lat);
                    long longitude = Double.doubleToRawLongBits(lng);
                    putAddress(SELECTED, address, latitude, longitude);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getAQIData(lat, lng);
                            getPollenCountData(lat, lng);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addOneMarker();
                                }
                            });
                        }
                    }).start();
                }
                break;
        }
    }

    private void getAQIData(double latitude, double longitude) {
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
            int result = (int) Math.round(aqi6 + (aqi5 - aqi6) * ratioY);
            editor = data.edit();
            editor.putInt("AQI 1000", result)
                    .apply();
        } else {
            HttpURLConnection connection = null;
            int aqi;
            String name = "AQI 1000";
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
                    System.out.println(aqi);
                    if (aqi != -1) {
                        editor = data.edit();
                        editor.putInt(name, aqi)
                                .apply();
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
        }
    }

    private void getPollenCountData(double latitude, double longitude) {
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
        String name = "Pollen Count 1000";
        editor = data.edit();
        editor.putInt(name, pollenCount)
                .apply();
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

    private void addOneMarker() {
        LatLng selectedAddress = getLatLngFromLog(SELECTED);
        if (selectedAddress != null) {
            String address = config.getString(SELECTED[0], null);
            String aqi = getData(AQI, 1000);
            int index1 = informationIndex;
            String pollen = getData(POLLEN_COUNT, 1000);
            int index2 = informationIndex;
            String description = String.format(Locale.ENGLISH, "AQI: %s, Pollen: %s", aqi, pollen);
            if (marker != null) {
                marker.remove();
            }
            marker = mMap.addMarker(new MarkerOptions().position(selectedAddress).title(address).alpha(0.8f).snippet(description));
            String information = generateInformation(index1, index2);
            tipTitle.setText(description);
            tipDescription.setText(information);
        }
    }

    private String generateInformation(int index1, int index2) {
        String description1 = null, description2 = null, result;

        switch (index1) {
            case 0:
                description1 = null;
                break;
            case 1:
                description1 = "Air quality is considered satisfactory, and air pollution poses little or no risk";
                break;
            case 2:
                description1 = "Air quality is acceptable; however, for some pollutants there may be a moderate health concern for a very small number of people who are unusually sensitive to air pollution";
                break;
            case 3:
                description1 = "Active children and adults, and people with respiratory disease, such as asthma, should limit prolonged outdoor exertion";
                break;
            case 4:
                description1 = "Active children and adults, and people with respiratory disease, such as asthma, should avoid prolonged outdoor exertion; everyone else, especially children, should limit prolonged outdoor exertion";
                break;
            case 5:
                description1 = "Active children and adults, and people with respiratory disease, such as asthma, should avoid all outdoor exertion; everyone else, especially children, should limit outdoor exertion";
                break;
            case 6:
                description1 = "Everyone should avoid all outdoor exertion";
                break;
        }

        switch (index2) {
            case 0:
                description2 = null;
                break;
            case 7:
                description2 = "Very few sufferers of pollen allergies would be expected to experience symptoms";
                break;
            case 8:
                description2 = "Some sufferers of pollen allergies will experience symptoms, but these are unlikely to be severe";
                break;
            case 9:
                description2 = "Many sufferers of pollen allergies will experience symptoms. It is better to stay away from sources of pollen";
                break;
            case 10:
                description2 = "Most sufferers of pollen allergies will experience symptoms and symptoms may be severe in some sufferers. The best precaution is to stay indoors";
                break;
        }

        if ((description1 != null) && (description2 != null)) {
            result = String.format(Locale.ENGLISH, "%s\n\n%s", description1, description2);
        } else if (description1 != null) {
            result = String.format(Locale.ENGLISH, "%s", description1);
        } else if (description2 != null) {
            result = String.format(Locale.ENGLISH, "%s", description2);
        } else {
            result = "No Data Available!";
        }
        return result;
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

    private boolean isAddressEmpty(String[] target) {
        return config.getString(target[ADDRESS], null) == null;
    }
}
