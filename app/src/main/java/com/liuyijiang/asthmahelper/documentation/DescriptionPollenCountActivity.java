package com.liuyijiang.asthmahelper.documentation;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.liuyijiang.asthmahelper.R;

import java.util.Locale;

public class DescriptionPollenCountActivity extends Activity {

    private TextView textLowStandard;
    private TextView textMediumStandard;
    private TextView textHighStandard;
    private TextView textExtremeStandard;

    private static final String LOW = "Low";
    private static final String MEDIUM = "Medium";
    private static final String HIGH = "High";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description_pollen_count);
        initial();
        initialView();
    }

    private void initial() {
        textLowStandard = findViewById(R.id.standard_low);
        textMediumStandard = findViewById(R.id.standard_medium);
        textHighStandard = findViewById(R.id.standard_high);
        textExtremeStandard = findViewById(R.id.standard_extreme);
    }

    private void initialView() {
        SharedPreferences config = this.getSharedPreferences("config", MODE_MULTI_PROCESS);
        int num0 = 0;
        int num1 = config.getInt(LOW, 25) + 25;
        int num2 = config.getInt(MEDIUM, 25) + 75;
        int num3 = config.getInt(HIGH, 25) + 155;

        textLowStandard.setText(String.format(Locale.ENGLISH, "%d - %d", num0, num1));
        textMediumStandard.setText(String.format(Locale.ENGLISH, "%d - %d", num1, num2));
        textHighStandard.setText(String.format(Locale.ENGLISH, "%d - %d", num2, num3));
        textExtremeStandard.setText(String.format(Locale.ENGLISH, "%d+", num3));
    }
}
