package com.liuyijiang.asthmahelper.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.liuyijiang.asthmahelper.InitialView;
import com.liuyijiang.asthmahelper.R;
import com.liuyijiang.asthmahelper.documentation.AboutActivity;
import com.liuyijiang.asthmahelper.documentation.DescriptionAQIActivity;
import com.liuyijiang.asthmahelper.documentation.DescriptionPollenCountActivity;
import com.liuyijiang.asthmahelper.tool.ExtraSeekBar;
import com.liuyijiang.asthmahelper.tool.SettingTitleView;

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

public class SettingPage extends Fragment {

    private Handler handler = new Handler();

    private SharedPreferences config;
    private SharedPreferences data;
    private SharedPreferences.Editor editor;

    private View settingView;

    private SettingTitleView settingAQI;
    private SettingTitleView settingPollenCount;
    private SettingTitleView settingAddresses;
    private SettingTitleView settingClearData;
    private RelativeLayout startNoDisturbing;
    private RelativeLayout endNoDisturbing;
    private SettingTitleView settingAbout;

    private boolean checkedAQI = false;
    private boolean checkedPollenCount = false;
    private boolean checkedAddresses = false;

    private RelativeLayout listAQI;
    private RelativeLayout listPollenCount;
    private RelativeLayout listAddresses;
    private LinearLayout listNoDisturbingTime;

    private Button buttonDescriptionAQI;
    private Button buttonResetAQI;
    private Button buttonDescriptionPollenCount;
    private Button buttonResetPollenCount;

    private ExtraSeekBar seekBarAQIGood;
    private ExtraSeekBar seekBarAQIModerate;
    private ExtraSeekBar seekBarAQIPoor;
    private ExtraSeekBar seekBarAQIUnhealthy;
    private ExtraSeekBar seekBarAQIVeryUnhealthy;
    private ExtraSeekBar seekBarPollenCountLow;
    private ExtraSeekBar seekBarPollenCountMedium;
    private ExtraSeekBar seekBarPollenCountHigh;

    private int numberOfAddresses = 0;
    private int layerOfAddresses = 1;

    private ImageView imagePlusAddress1;
    private ImageView imagePlusAddress2;
    private ImageView imageMinusAddress1;
    private ImageView imageMinusAddress2;
    private ImageView imageMinusAddress3;

    private Switch switchNoDisturbingTime;
    private Switch switchNotification;

    private TextView textAddress1;
    private TextView textAddress2;
    private TextView textAddress3;
    private TextView separatorAddresses1;
    private TextView separatorAddresses2;
    private TextView textStartTime;
    private TextView textEndTime;

    private static final String GOOD = "Good";
    private static final String MODERATE = "Moderate";
    private static final String POOR = "Poor";
    private static final String UNHEALTHY = "Unhealthy";
    private static final String VERY_UNHEALTHY = "Very Unhealthy";
    private static final String LOW = "Low";
    private static final String MEDIUM = "Medium";
    private static final String HIGH = "High";
    private static final int ADDRESS = 0;
    private static final int LATITUDE = 1;
    private static final int LONGITUDE = 2;
    private static final String[] ADDRESS1 = {"Address1", "Address1 Latitude", "Address1 Longitude"};
    private static final String[] ADDRESS2 = {"Address2", "Address2 Latitude", "Address2 Longitude"};
    private static final String[] ADDRESS3 = {"Address3", "Address3 Latitude", "Address3 Longitude"};
    private static final String ADDRESS_TIP = "Please click and select an address";
    private static final int FIRST_ADDRESS_REQUEST_CODE = 1;
    private static final int SECOND_ADDRESS_REQUEST_CODE = 2;
    private static final int THIRD_ADDRESS_REQUEST_CODE = 3;
    private static final LatLngBounds MELBOURNE_BOUNDS = new LatLngBounds(new LatLng(-38.1, 144.7), new LatLng(-37.5, 145.45));
    private static final String NO_DISTURBING = "No Disturbing";
    private static final String START_HOUR = "Start Hour";
    private static final String START_MINUTE = "Start Minute";
    private static final String END_HOUR = "End Hour";
    private static final String END_MINUTE = "End Minute";
    private static final String NOTIFICATION = "Notification";
    private static final String WARNING_TITLE = "Warning";
    private static final String WARNING_CONTENT = "Are you sure to clear user data?";
    private static final String CONFIRM_BUTTON = "Confirm";
    private static final String CANCEL_BUTTON = "Cancel";
//    private TextView tagAQIGood;
//    private TextView tagAQIModerate;
//    private TextView tagAQIPoor;
//    private TextView tagAQIUnhealthy;
//    private TextView tagAQIVeryUnhealthy;
//    private TextView tagPollenCountLow;
//    private TextView tagPollenCountMedium;
//    private TextView tagPollenCountHigh;
//
//    private LinearLayout slideBarAQIGood;
//    private LinearLayout slideBarAQIModerate;
//    private LinearLayout slideBarAQIPoor;
//    private LinearLayout slideBarAQIUnhealthy;
//    private LinearLayout slideBarAQIVeryUnhealthy;
//    private LinearLayout slideBarPollenCountLow;
//    private LinearLayout slideBarPollenCountMedium;
//    private LinearLayout slideBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        settingView = inflater.inflate(R.layout.fragment_setting, container, false);
        initial();
        return settingView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initListener();
    }

    private void initial() {
        settingAQI = settingView.findViewById(R.id.setting_aqi);
        settingPollenCount = settingView.findViewById(R.id.setting_pollen_count);
        settingAddresses = settingView.findViewById(R.id.setting_addresses);
        settingClearData = settingView.findViewById(R.id.setting_clear_data);
        startNoDisturbing = settingView.findViewById(R.id.start_no_disturbing);
        endNoDisturbing = settingView.findViewById(R.id.end_no_disturbing);
        settingAbout = settingView.findViewById(R.id.setting_about_asthma_helper);

        listAQI = settingView.findViewById(R.id.list_air_pollution);
        listPollenCount = settingView.findViewById(R.id.list_pollen_count);
        listAddresses = settingView.findViewById(R.id.list_frequently_used_addresses);
        listNoDisturbingTime = settingView.findViewById(R.id.list_no_disturbing_time);

        buttonDescriptionAQI = settingView.findViewById(R.id.specification_aqi);
        buttonResetAQI = settingView.findViewById(R.id.reset_aqi);
        buttonDescriptionPollenCount = settingView.findViewById(R.id.specification_pollen_count);
        buttonResetPollenCount = settingView.findViewById(R.id.reset_pollen_count);

        seekBarAQIGood = settingView.findViewById(R.id.bar_aqi_good);
        seekBarAQIModerate = settingView.findViewById(R.id.bar_aqi_moderate);
        seekBarAQIPoor = settingView.findViewById(R.id.bar_aqi_poor);
        seekBarAQIUnhealthy = settingView.findViewById(R.id.bar_aqi_unhealthy);
        seekBarAQIVeryUnhealthy = settingView.findViewById(R.id.bar_aqi_very_unhealthy);
        seekBarPollenCountLow = settingView.findViewById(R.id.bar_pollen_count_low);
        seekBarPollenCountMedium = settingView.findViewById(R.id.bar_pollen_count_medium);
        seekBarPollenCountHigh = settingView.findViewById(R.id.bar_pollen_count_high);

        imagePlusAddress1 = settingView.findViewById(R.id.address1_plus);
        imageMinusAddress1 = settingView.findViewById(R.id.address1_minus);
        imagePlusAddress2 = settingView.findViewById(R.id.address2_plus);
        imageMinusAddress2 = settingView.findViewById(R.id.address2_minus);
        imageMinusAddress3 = settingView.findViewById(R.id.address3_minus);

        switchNoDisturbingTime = settingView.findViewById(R.id.switch_no_disturbing_time);
        switchNotification = settingView.findViewById(R.id.switch_notification);

        textAddress1 = settingView.findViewById(R.id.address1);
        textAddress2 = settingView.findViewById(R.id.address2);
        textAddress3 = settingView.findViewById(R.id.address3);
        separatorAddresses1 = settingView.findViewById(R.id.address_separator1);
        separatorAddresses2 = settingView.findViewById(R.id.address_separator2);
        textStartTime = settingView.findViewById(R.id.text_start_time);
        textEndTime = settingView.findViewById(R.id.text_end_time);

//        tagAQIGood = settingView.findViewById(R.id.num_tag_aqi_good);
//        tagAQIModerate = settingView.findViewById(R.id.num_tag_aqi_moderate);
//        tagAQIPoor = settingView.findViewById(R.id.num_tag_aqi_poor);
//        tagAQIUnhealthy = settingView.findViewById(R.id.num_tag_aqi_unhealthy);
//        tagAQIVeryUnhealthy = settingView.findViewById(R.id.num_tag_aqi_very_unhealthy);
//        tagPollenCountLow = settingView.findViewById(R.id.num_tag_pollen_count_low);
//        tagPollenCountMedium = settingView.findViewById(R.id.num_tag_pollen_count_medium);
//        tagPollenCountHigh = settingView.findViewById(R.id.num_tag_pollen_count_high);
//
//        slideBarAQIGood = settingView.findViewById(R.id.num_tag_aqi_good_slide_bar);
//        slideBarAQIModerate = settingView.findViewById(R.id.num_tag_aqi_moderate_slide_bar);
//        slideBarAQIPoor = settingView.findViewById(R.id.num_tag_aqi_poor_slide_bar);
//        slideBarAQIUnhealthy = settingView.findViewById(R.id.num_tag_aqi_unhealthy_slide_bar);
//        slideBarAQIVeryUnhealthy = settingView.findViewById(R.id.num_tag_aqi_very_unhealthy_slide_bar);
//        slideBarPollenCountLow = settingView.findViewById(R.id.num_tag_pollen_count_low_slide_bar);
//        slideBarPollenCountMedium = settingView.findViewById(R.id.num_tag_pollen_count_medium_slide_bar);
//        slideBar = settingView.findViewById(R.id.num_tag_pollen_count_high_slide_bar);
    }

    private void initListener() {
        config = getActivity().getSharedPreferences("config", MODE_MULTI_PROCESS);
        data = getActivity().getSharedPreferences("data", MODE_MULTI_PROCESS);

        settingAQI.setOnClickListener(settingPageClickListener);
        buttonDescriptionAQI.setOnClickListener(settingPageClickListener);
        buttonResetAQI.setOnClickListener(settingPageClickListener);
        seekBarAQIGood.setOnSeekBarChangeListener(extraSeekBarChangeListener);
        seekBarAQIModerate.setOnSeekBarChangeListener(extraSeekBarChangeListener);
        seekBarAQIPoor.setOnSeekBarChangeListener(extraSeekBarChangeListener);
        seekBarAQIUnhealthy.setOnSeekBarChangeListener(extraSeekBarChangeListener);
        seekBarAQIVeryUnhealthy.setOnSeekBarChangeListener(extraSeekBarChangeListener);
        seekBarInit(seekBarAQIGood);
        seekBarInit(seekBarAQIModerate);
        seekBarInit(seekBarAQIPoor);
        seekBarInit(seekBarAQIUnhealthy);
        seekBarInit(seekBarAQIVeryUnhealthy);

        settingPollenCount.setOnClickListener(settingPageClickListener);
        buttonDescriptionPollenCount.setOnClickListener(settingPageClickListener);
        buttonResetPollenCount.setOnClickListener(settingPageClickListener);
        seekBarPollenCountLow.setOnSeekBarChangeListener(extraSeekBarChangeListener);
        seekBarPollenCountMedium.setOnSeekBarChangeListener(extraSeekBarChangeListener);
        seekBarPollenCountHigh.setOnSeekBarChangeListener(extraSeekBarChangeListener);
        seekBarInit(seekBarPollenCountLow);
        seekBarInit(seekBarPollenCountMedium);
        seekBarInit(seekBarPollenCountHigh);

        settingAddresses.setOnClickListener(settingPageClickListener);
        textAddress1.setOnClickListener(settingPageClickListener);
        textAddress2.setOnClickListener(settingPageClickListener);
        textAddress3.setOnClickListener(settingPageClickListener);
        imagePlusAddress1.setOnClickListener(settingPageClickListener);
        imagePlusAddress2.setOnClickListener(settingPageClickListener);
        imageMinusAddress1.setOnClickListener(settingPageClickListener);
        imageMinusAddress2.setOnClickListener(settingPageClickListener);
        imageMinusAddress3.setOnClickListener(settingPageClickListener);
        addressListInit();

        switchNoDisturbingTime.setOnCheckedChangeListener(switchCheckedChangeListener);
        startNoDisturbing.setOnClickListener(settingPageClickListener);
        endNoDisturbing.setOnClickListener(settingPageClickListener);
        noDisturbingInit();

        switchNotification.setOnCheckedChangeListener(switchCheckedChangeListener);
        notificationInit();

        settingClearData.setOnClickListener(settingPageClickListener);

        settingAbout.setOnClickListener(settingPageClickListener);
    }

    private void seekBarInit(ExtraSeekBar extraSeekBar) {
        String id = getSharedIdOfSeekBarTag(extraSeekBar);
        int progress = config.getInt(id, 25);
        extraSeekBar.setProgress(progress);
        setSeekBarTag(extraSeekBar, progress);
    }

    private void setSeekBarTag(ExtraSeekBar extraSeekBar, int progress) {
        int numText = progress + extraSeekBar.getStartPoint();
        TextView numTag = settingView.findViewById(extraSeekBar.getNumTagId());
        LinearLayout slideBar = settingView.findViewById(extraSeekBar.getSlideBarId());
        numTag.setText(String.format(Locale.ENGLISH, "%d", numText));
        int slideBarWidth = slideBar.getWidth() - numTag.getWidth();
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) numTag.getLayoutParams();
        params.leftMargin = (int) (((double) progress / extraSeekBar.getMax()) * slideBarWidth);
        numTag.setLayoutParams(params);
    }

    private void addressesInit(TextView textAddress) {
        String id = getSharedIdOfTextView(textAddress);
        String address = config.getString(id, ADDRESS_TIP);
        textAddress.setText(address);
        if (!address.equals(ADDRESS_TIP)) {
            numberOfAddresses++;
        }
    }

    private void addressListInit() {
        numberOfAddresses = 0;
        addressesInit(textAddress1);
        addressesInit(textAddress2);
        addressesInit(textAddress3);
        if (numberOfAddresses != 0) {
            layerOfAddresses = numberOfAddresses;
        } else {
            layerOfAddresses = 1;
        }
        switch (layerOfAddresses) {
            case 1:
                if (numberOfAddresses != 0) {
                    imagePlusAddress1.setVisibility(View.VISIBLE);
                } else {
                    imagePlusAddress1.setVisibility(View.INVISIBLE);
                }
                textAddress2.setVisibility(View.GONE);
                imagePlusAddress2.setVisibility(View.GONE);
                imageMinusAddress2.setVisibility(View.GONE);
                separatorAddresses1.setVisibility(View.GONE);
                textAddress3.setVisibility(View.GONE);
                imageMinusAddress3.setVisibility(View.GONE);
                separatorAddresses2.setVisibility(View.GONE);
                break;
            case 2:
                imagePlusAddress1.setVisibility(View.INVISIBLE);
                textAddress2.setVisibility(View.VISIBLE);
                imagePlusAddress2.setVisibility(View.VISIBLE);
                imageMinusAddress2.setVisibility(View.VISIBLE);
                separatorAddresses1.setVisibility(View.VISIBLE);
                textAddress3.setVisibility(View.GONE);
                imageMinusAddress3.setVisibility(View.GONE);
                separatorAddresses2.setVisibility(View.GONE);
                break;
            case 3:
                imagePlusAddress1.setVisibility(View.INVISIBLE);
                textAddress2.setVisibility(View.VISIBLE);
                imagePlusAddress2.setVisibility(View.GONE);
                imageMinusAddress2.setVisibility(View.VISIBLE);
                separatorAddresses1.setVisibility(View.VISIBLE);
                textAddress3.setVisibility(View.VISIBLE);
                imageMinusAddress3.setVisibility(View.VISIBLE);
                separatorAddresses2.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void noDisturbingInit() {
        int startHour = config.getInt(START_HOUR, 23);
        int startMinute = config.getInt(START_MINUTE, 0);
        int endHour = config.getInt(END_HOUR, 8);
        int endMinute = config.getInt(END_MINUTE, 0);
        boolean checked = config.getBoolean(NO_DISTURBING, false);

        String hour = formatTime(startHour);
        String min = formatTime(startMinute);
        textStartTime.setText(String.format(Locale.ENGLISH, "%s:%s", hour, min));
        hour = formatTime(endHour);
        min = formatTime(endMinute);
        textEndTime.setText(String.format(Locale.ENGLISH, "%s:%s", hour, min));

        switchNoDisturbingTime.setChecked(checked);
        if (checked) {
            listNoDisturbingTime.setVisibility(View.VISIBLE);
        } else {
            listNoDisturbingTime.setVisibility(View.GONE);
        }
    }

    private void notificationInit() {
        boolean checked = config.getBoolean(NOTIFICATION, false);
        switchNotification.setChecked(checked);
    }

    private View.OnClickListener settingPageClickListener = new View.OnClickListener() {
        @SuppressLint("ApplySharedPref")
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.setting_aqi:
                    checkedAQI = !checkedAQI;
                    if (checkedAQI) {
                        settingAQI.setDrawableRight(R.mipmap.arrow_down);
                        listAQI.setVisibility(View.VISIBLE);
                    } else {
                        settingAQI.setDrawableRight(R.mipmap.arrow_right);
                        listAQI.setVisibility(View.GONE);
                    }
                    break;
                case R.id.setting_pollen_count:
                    checkedPollenCount = !checkedPollenCount;
                    if (checkedPollenCount) {
                        settingPollenCount.setDrawableRight(R.mipmap.arrow_down);
                        listPollenCount.setVisibility(View.VISIBLE);
                    } else {
                        settingPollenCount.setDrawableRight(R.mipmap.arrow_right);
                        listPollenCount.setVisibility(View.GONE);
                    }
                    break;
                case R.id.specification_aqi:
                    startActivity(new Intent(getActivity(), DescriptionAQIActivity.class));
                    break;
                case R.id.reset_aqi:
                    editor = config.edit();
                    editor.putInt(GOOD, 25)
                            .putInt(MODERATE, 25)
                            .putInt(POOR, 25)
                            .putInt(UNHEALTHY, 25)
                            .putInt(VERY_UNHEALTHY, 25)
                            .commit();
                    seekBarInit(seekBarAQIGood);
                    seekBarInit(seekBarAQIModerate);
                    seekBarInit(seekBarAQIPoor);
                    seekBarInit(seekBarAQIUnhealthy);
                    seekBarInit(seekBarAQIVeryUnhealthy);
                    break;
                case R.id.specification_pollen_count:
                    startActivity(new Intent(getActivity(), DescriptionPollenCountActivity.class));
                    break;
                case R.id.reset_pollen_count:
                    editor = config.edit();
                    editor.putInt(LOW, 25)
                            .putInt(MEDIUM, 25)
                            .putInt(HIGH, 25)
                            .commit();
                    seekBarInit(seekBarPollenCountLow);
                    seekBarInit(seekBarPollenCountMedium);
                    seekBarInit(seekBarPollenCountHigh);
                    break;
                case R.id.setting_addresses:
                    checkedAddresses = !checkedAddresses;
                    if (checkedAddresses) {
                        settingAddresses.setDrawableRight(R.mipmap.arrow_down);
                        addressListInit();
                        listAddresses.setVisibility(View.VISIBLE);
                    } else {
                        settingAddresses.setDrawableRight(R.mipmap.arrow_right);
                        listAddresses.setVisibility(View.GONE);
                    }
                    break;
                case R.id.address1:
                    try {
                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setBoundsBias(MELBOURNE_BOUNDS).build(getActivity());
                        startActivityForResult(intent, FIRST_ADDRESS_REQUEST_CODE);
                    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                        Toast.makeText(getActivity(), "Google Play Services is not available", Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.address2:
                    try {
                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setBoundsBias(MELBOURNE_BOUNDS).build(getActivity());
                        startActivityForResult(intent, SECOND_ADDRESS_REQUEST_CODE);
                    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                        Toast.makeText(getActivity(), "Google Play Services is not available", Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.address3:
                    try {
                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setBoundsBias(MELBOURNE_BOUNDS).build(getActivity());
                        startActivityForResult(intent, THIRD_ADDRESS_REQUEST_CODE);
                    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                        Toast.makeText(getActivity(), "Google Play Services is not available", Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.address1_plus:
                    textAddress2.setVisibility(View.VISIBLE);
                    imageMinusAddress2.setVisibility(View.VISIBLE);
                    separatorAddresses1.setVisibility(View.VISIBLE);
                    imagePlusAddress1.setVisibility(View.INVISIBLE);
                    layerOfAddresses++;
                    break;
                case R.id.address2_plus:
                    textAddress3.setVisibility(View.VISIBLE);
                    imageMinusAddress3.setVisibility(View.VISIBLE);
                    separatorAddresses2.setVisibility(View.VISIBLE);
                    imagePlusAddress2.setVisibility(View.GONE);
                    layerOfAddresses++;
                    break;
                case R.id.address1_minus:
                    if (layerOfAddresses == 3) {
                        textAddress1.setText(textAddress2.getText());
                        textAddress2.setText(textAddress3.getText());
                        if (numberOfAddresses == 3) {
                            imagePlusAddress2.setVisibility(View.VISIBLE);
                            copyAddress(ADDRESS2, ADDRESS1);
                            copyAddress(ADDRESS3, ADDRESS2);
                        } else if (numberOfAddresses == 2) {
                            moveAddress(ADDRESS2, ADDRESS1);
                        }
                        minusWhenLayerEq3();
                    } else if (layerOfAddresses == 2) {
                        textAddress1.setText(textAddress2.getText());
                        if (numberOfAddresses == 2) {
                            imagePlusAddress1.setVisibility(View.VISIBLE);
                            copyAddress(ADDRESS2, ADDRESS1);
                        } else if (numberOfAddresses == 1) {
                            removeAddress(ADDRESS1);
                        }
                        minusWhenLayerEq2();
                    } else if (layerOfAddresses == 1) {
                        settingAddresses.setDrawableRight(R.mipmap.arrow_right);
                        listAddresses.setVisibility(View.GONE);
                        checkedAddresses = false;
                        if (numberOfAddresses == 1) {
                            textAddress1.setText(ADDRESS_TIP);
                            removeAddress(ADDRESS1);
                        }
                    }
                    break;
                case R.id.address2_minus:
                    if (layerOfAddresses == 3) {
                        textAddress2.setText(textAddress3.getText());
                        if (numberOfAddresses == 3) {
                            imagePlusAddress2.setVisibility(View.VISIBLE);
                            copyAddress(ADDRESS3, ADDRESS2);
                        } else if (numberOfAddresses == 2){
                            removeAddress(ADDRESS2);
                        }
                        minusWhenLayerEq3();
                    } else if (layerOfAddresses == 2) {
                        minusWhenLayerEq2();
                        imagePlusAddress1.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.address3_minus:
                    minusWhenLayerEq3();
                    imagePlusAddress2.setVisibility(View.VISIBLE);
                    break;
                case R.id.start_no_disturbing:
                    int[] startTime = stringToTime(textStartTime.getText().toString());
                    new TimePickerDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            String hour = formatTime(hourOfDay);
                            String min = formatTime(minute);
                            textStartTime.setText(String.format(Locale.ENGLISH, "%s:%s", hour, min));
                            editor = config.edit();
                            editor.putInt(START_HOUR, hourOfDay)
                                    .putInt(START_MINUTE, minute)
                                    .apply();
                        }
                    }, startTime[0], startTime[1], true).show();
                    break;
                case R.id.end_no_disturbing:
                    int[] endTime = stringToTime(textEndTime.getText().toString());
                    new TimePickerDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            String hour = formatTime(hourOfDay);
                            String min = formatTime(minute);
                            textEndTime.setText(String.format(Locale.ENGLISH, "%s:%s", hour, min));
                            editor = config.edit();
                            editor.putInt(END_HOUR, hourOfDay)
                                    .putInt(END_MINUTE, minute)
                                    .apply();
                        }
                    }, endTime[0], endTime[1], true).show();
                    break;
                case R.id.setting_clear_data:
                    new AlertDialog.Builder(getActivity())
                            .setTitle(WARNING_TITLE)
                            .setMessage(WARNING_CONTENT)
                            .setNegativeButton(CANCEL_BUTTON, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setPositiveButton(CONFIRM_BUTTON, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    editor = config.edit();
                                    editor.clear().apply();
                                    initListener();
                                    if(getActivity()instanceof InitialView){
                                        ((InitialView)getActivity()).initialAllView();
                                    }
                                }
                            })
                            .create().show();
                    break;
                case R.id.setting_about_asthma_helper:
                    startActivity(new Intent(getActivity(), AboutActivity.class));
                    break;
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener extraSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            ExtraSeekBar extraSeekBar = (ExtraSeekBar) seekBar;
            setSeekBarTag(extraSeekBar, progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            ExtraSeekBar extraSeekBar = (ExtraSeekBar) seekBar;
            TextView numTag = settingView.findViewById(extraSeekBar.getNumTagId());
            numTag.setVisibility(View.VISIBLE);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            ExtraSeekBar extraSeekBar = (ExtraSeekBar) seekBar;
            TextView numTag = settingView.findViewById(extraSeekBar.getNumTagId());
            numTag.setVisibility(View.INVISIBLE);
            String id = getSharedIdOfSeekBarTag(extraSeekBar);
            editor = config.edit();
            editor.putInt(id, extraSeekBar.getProgress())
                    .apply();
        }
    };

    private Switch.OnCheckedChangeListener switchCheckedChangeListener = new Switch.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
            switch (compoundButton.getId()) {
                case R.id.switch_no_disturbing_time:
                    if (checked) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                listNoDisturbingTime.setVisibility(View.VISIBLE);
                                switchNotification.setChecked(true);
                                editor = config.edit();
                                editor.putBoolean(NO_DISTURBING, true)
                                        .putBoolean(NOTIFICATION, true)
                                        .apply();
                            }
                        }, 150);
                    } else {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                listNoDisturbingTime.setVisibility(View.GONE);
                                editor = config.edit();
                                editor.putBoolean(NO_DISTURBING, false)
                                        .apply();
                            }
                        }, 150);
                    }
                    break;
                case R.id.switch_notification:
                    if (checked) {
                        editor = config.edit();
                        editor.putBoolean(NOTIFICATION, true)
                                .apply();
                    } else {
                        switchNoDisturbingTime.setChecked(false);
                        editor = config.edit();
                        editor.putBoolean(NOTIFICATION, false)
                                .putBoolean(NO_DISTURBING, false)
                                .apply();
                    }
                    break;
            }
        }
    };

    private int[] stringToTime(String timeString) {
        int[] time = new int[2];
        String hour = timeString.substring(0, 2);
        String minute = timeString.substring(3, 5);
        time[0] = Integer.parseInt(hour);
        time[1] = Integer.parseInt(minute);
        return time;
    }

    private String formatTime(int time) {
        if (time < 10) {
            return String.format(Locale.ENGLISH, "0%d", time);
        } else {
            return String.format(Locale.ENGLISH, "%d", time);
        }
    }

    private void minusWhenLayerEq3() {
        textAddress3.setVisibility(View.GONE);
        imageMinusAddress3.setVisibility(View.GONE);
        separatorAddresses2.setVisibility(View.GONE);
        if (!isAddressEmpty(ADDRESS3)) {
            textAddress3.setText(ADDRESS_TIP);
            removeAddress(ADDRESS3);
        }
        layerOfAddresses--;
    }

    private void minusWhenLayerEq2() {
        textAddress2.setVisibility(View.GONE);
        imagePlusAddress2.setVisibility(View.GONE);
        imageMinusAddress2.setVisibility(View.GONE);
        separatorAddresses1.setVisibility(View.GONE);
        if (!isAddressEmpty(ADDRESS2)) {
            textAddress2.setText(ADDRESS_TIP);
            removeAddress(ADDRESS2);
        }
        layerOfAddresses--;
    }

    private String getSharedIdOfSeekBarTag(ExtraSeekBar seekBar) {
        switch (seekBar.getNumTagId()) {
            case R.id.num_tag_aqi_good:
                return GOOD;
            case R.id.num_tag_aqi_moderate:
                return MODERATE;
            case R.id.num_tag_aqi_poor:
                return POOR;
            case R.id.num_tag_aqi_unhealthy:
                return UNHEALTHY;
            case R.id.num_tag_aqi_very_unhealthy:
                return VERY_UNHEALTHY;
            case R.id.num_tag_pollen_count_low:
                return LOW;
            case R.id.num_tag_pollen_count_medium:
                return MEDIUM;
            case R.id.num_tag_pollen_count_high:
                return HIGH;
        }
        return null;
    }

    private String getSharedIdOfTextView(TextView textView) {
        switch (textView.getId()) {
            case R.id.address1:
                return ADDRESS1[ADDRESS];
            case R.id.address2:
                return ADDRESS2[ADDRESS];
            case R.id.address3:
                return ADDRESS3[ADDRESS];
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FIRST_ADDRESS_REQUEST_CODE:
                if (resultCode == -1) {
                    Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                    textAddress1.setText(place.getName());
                    imagePlusAddress1.setVisibility(View.VISIBLE);
                    if (isAddressEmpty(ADDRESS1)) {
                        numberOfAddresses++;
                    }
                    String address = (String) place.getName();
                    final double lat = place.getLatLng().latitude;
                    final double lng = place.getLatLng().longitude;
                    long latitude = Double.doubleToRawLongBits(lat);
                    long longitude = Double.doubleToRawLongBits(lng);
                    putAddress(ADDRESS1, address, latitude, longitude);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getAQIData(lat, lng, 0);
                            getPollenCountData(lat, lng, 0);
                        }
                    }).start();
                }
                break;
            case SECOND_ADDRESS_REQUEST_CODE:
                if (resultCode == -1) {
                    Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                    textAddress2.setText(place.getName());
                    imagePlusAddress2.setVisibility(View.VISIBLE);
                    if (isAddressEmpty(ADDRESS2)) {
                        numberOfAddresses++;
                    }
                    String address = (String) place.getName();
                    final double lat = place.getLatLng().latitude;
                    final double lng = place.getLatLng().longitude;
                    long latitude = Double.doubleToRawLongBits(lat);
                    long longitude = Double.doubleToRawLongBits(lng);
                    putAddress(ADDRESS2, address, latitude, longitude);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getAQIData(lat, lng, 1);
                            getPollenCountData(lat, lng, 1);
                        }
                    }).start();
                }
                break;
            case THIRD_ADDRESS_REQUEST_CODE:
                if (resultCode == -1) {
                    Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                    textAddress3.setText(place.getName());
                    if (isAddressEmpty(ADDRESS3)) {
                        numberOfAddresses++;
                    }
                    String address = (String) place.getName();
                    final double lat = place.getLatLng().latitude;
                    final double lng = place.getLatLng().longitude;
                    long latitude = Double.doubleToRawLongBits(lat);
                    long longitude = Double.doubleToRawLongBits(lng);
                    putAddress(ADDRESS3, address, latitude, longitude);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getAQIData(lat, lng, 2);
                            getPollenCountData(lat, lng, 2);
                        }
                    }).start();
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
        numberOfAddresses--;
    }

    private void copyAddress(String[] src, String[] target) {
        String address = config.getString(src[ADDRESS], null);
        long latitude = config.getLong(src[LATITUDE], 0);
        long longitude = config.getLong(src[LONGITUDE], 0);
        putAddress(target, address, latitude, longitude);
    }

    private void moveAddress(String[] src, String[] target) {
        copyAddress(src, target);
        removeAddress(src);
    }

    private boolean isAddressEmpty(String[] target) {
        return config.getString(target[ADDRESS], null) == null;
    }

    private void getAQIData(double latitude, double longitude, int count) {
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
            String name = String.format(Locale.ENGLISH, "AQI %d", count);
            editor = data.edit();
            editor.putInt(name, result)
                    .apply();
        } else {
            HttpURLConnection connection = null;
            int aqi;
            String name = String.format(Locale.ENGLISH, "AQI %d", count);
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

    private void getPollenCountData(double latitude, double longitude, int count) {
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
        String name = String.format(Locale.ENGLISH, "Pollen Count %d", count);
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
}
