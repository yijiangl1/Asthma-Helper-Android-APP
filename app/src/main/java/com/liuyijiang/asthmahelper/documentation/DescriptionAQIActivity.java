package com.liuyijiang.asthmahelper.documentation;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.liuyijiang.asthmahelper.R;

import java.util.Locale;

public class DescriptionAQIActivity extends Activity {

    private TextView textGoodStandard;
    private TextView textModerateStandard;
    private TextView textPoorStandard;
    private TextView textUnhealthyStandard;
    private TextView textVeryUnhealthyStandard;
    private TextView textHazardousStandard;

    private static final String GOOD = "Good";
    private static final String MODERATE = "Moderate";
    private static final String POOR = "Poor";
    private static final String UNHEALTHY = "Unhealthy";
    private static final String VERY_UNHEALTHY = "Very Unhealthy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description_aqi);
        initial();
        initialView();
    }

    private void initial() {
        textGoodStandard = findViewById(R.id.standard_good);
        textModerateStandard = findViewById(R.id.standard_moderate);
        textPoorStandard = findViewById(R.id.standard_poor);
        textUnhealthyStandard = findViewById(R.id.standard_unhealthy);
        textVeryUnhealthyStandard = findViewById(R.id.standard_very_unhealthy);
        textHazardousStandard = findViewById(R.id.standard_hazardous);
    }

    private void initialView() {
        SharedPreferences config = this.getSharedPreferences("config", MODE_MULTI_PROCESS);
        int num0 = 0;
        int num1 = config.getInt(GOOD, 25) + 25;
        int num2 = config.getInt(MODERATE, 25) + 75;
        int num3 = config.getInt(POOR, 25) + 125;
        int num4 = config.getInt(UNHEALTHY, 25) + 175;
        int num5 = config.getInt(VERY_UNHEALTHY, 25) + 275;

        textGoodStandard.setText(String.format(Locale.ENGLISH, "%d - %d", num0, num1));
        textModerateStandard.setText(String.format(Locale.ENGLISH, "%d - %d", num1, num2));
        textPoorStandard.setText(String.format(Locale.ENGLISH, "%d - %d", num2, num3));
        textUnhealthyStandard.setText(String.format(Locale.ENGLISH, "%d - %d", num3, num4));
        textVeryUnhealthyStandard.setText(String.format(Locale.ENGLISH, "%d - %d", num4, num5));
        textHazardousStandard.setText(String.format(Locale.ENGLISH, "%d+", num5));
    }
}
