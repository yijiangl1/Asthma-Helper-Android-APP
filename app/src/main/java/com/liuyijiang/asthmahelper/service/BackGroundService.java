package com.liuyijiang.asthmahelper.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.liuyijiang.asthmahelper.MainActivity;
import com.liuyijiang.asthmahelper.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BackGroundService extends Service {

    public static final String ACTION = "com.liuyijiang.asthmahelper.service.BackGroundService";
    private SharedPreferences config;
    private SharedPreferences data;
    private SharedPreferences.Editor editor;

    private List<Integer> aqiList = new ArrayList<>();
    private List<Integer> pollenCountList = new ArrayList<>();

    private static final int ADDRESS = 0;
    private static final int LATITUDE = 1;
    private static final int LONGITUDE = 2;
    private static final String[] ADDRESS1 = {"Address1", "Address1 Latitude", "Address1 Longitude"};
    private static final String[] ADDRESS2 = {"Address2", "Address2 Latitude", "Address2 Longitude"};
    private static final String[] ADDRESS3 = {"Address3", "Address3 Latitude", "Address3 Longitude"};
    private static final String NO_DISTURBING = "No Disturbing";
    private static final String START_HOUR = "Start Hour";
    private static final String START_MINUTE = "Start Minute";
    private static final String END_HOUR = "End Hour";
    private static final String END_MINUTE = "End Minute";
    private static final String NOTIFICATION = "Notification";
    private static final String GOOD = "Good";
    private static final String MODERATE = "Moderate";
    private static final String POOR = "Poor";
    private static final String UNHEALTHY = "Unhealthy";
    private static final String VERY_UNHEALTHY = "Very Unhealthy";
    private static final String LOW = "Low";
    private static final String MEDIUM = "Medium";
    private static final String HIGH = "High";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        config = this.getSharedPreferences("config", MODE_MULTI_PROCESS);
        data = this.getSharedPreferences("data", MODE_MULTI_PROCESS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startDownload();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startDownload() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 3;
                for (double lat = -37.5; lat > -38.11; lat -= 0.15) {
                    for (double lng = 144.7; lng < 145.46; lng += 0.15) {
                        int aqi = getAQIData(lat, lng, count);
                        int pollen = getPollenCountData(lat, lng, count);
                        aqiList.add(aqi);
                        pollenCountList.add(pollen);
                        count++;
                    }
                }

                boolean needNotification = config.getBoolean(NOTIFICATION, false);
                boolean canDisturb = canDisturb();

                int aqi;
                int pollenCount;

                double[] latlng1 = getLatLngFromLog(ADDRESS1);
                double[] latlng2 = getLatLngFromLog(ADDRESS2);
                double[] latlng3 = getLatLngFromLog(ADDRESS3);

                if (latlng1 != null) {
                    aqi = getAQIData(latlng1[0], latlng1[1], 0);
                    pollenCount = getPollenCountData(latlng1[0], latlng1[1], 0);
                    if (needNotification && canDisturb && needDisturb(aqi, pollenCount)) {
                        String address = config.getString(ADDRESS1[ADDRESS], null);
                        String aqiText, pollenCountText;
                        if (aqi < 0) {
                            aqiText = "No Data";
                        } else {
                            aqiText = String.valueOf(aqi);
                        }
                        if (pollenCount < 0) {
                            pollenCountText = "No Data";
                        } else {
                            pollenCountText = String.valueOf(pollenCount);
                        }
                        String context = String.format(Locale.ENGLISH, "AQI: %s                 Pollen Count: %s", aqiText, pollenCountText);
                        String longContext = generateNotification(aqi, pollenCount);
                        showNotification(1, address, context, longContext);
                    }
                }
                if (latlng2 != null) {
                    aqi = getAQIData(latlng2[0], latlng2[1], 1);
                    pollenCount = getPollenCountData(latlng2[0], latlng2[1], 1);
                    if (needNotification && canDisturb && needDisturb(aqi, pollenCount)) {
                        String address = config.getString(ADDRESS2[ADDRESS], null);
                        String aqiText, pollenCountText;
                        if (aqi < 0) {
                            aqiText = "No Data";
                        } else {
                            aqiText = String.valueOf(aqi);
                        }
                        if (pollenCount < 0) {
                            pollenCountText = "No Data";
                        } else {
                            pollenCountText = String.valueOf(pollenCount);
                        }
                        String context = String.format(Locale.ENGLISH, "AQI: %s                 Pollen Count: %s", aqiText, pollenCountText);
                        String longContext = generateNotification(aqi, pollenCount);
                        showNotification(2, address, context, longContext);
                    }
                }
                if (latlng3 != null) {
                    aqi = getAQIData(latlng3[0], latlng3[1], 2);
                    pollenCount = getPollenCountData(latlng3[0], latlng3[1], 2);
                    if (needNotification && canDisturb && needDisturb(aqi, pollenCount)) {
                        String address = config.getString(ADDRESS3[ADDRESS], null);
                        String aqiText, pollenCountText;
                        if (aqi < 0) {
                            aqiText = "No Data";
                        } else {
                            aqiText = String.valueOf(aqi);
                        }
                        if (pollenCount < 0) {
                            pollenCountText = "No Data";
                        } else {
                            pollenCountText = String.valueOf(pollenCount);
                        }
                        String context = String.format(Locale.ENGLISH, "AQI: %s                 Pollen Count: %s", aqiText, pollenCountText);
                        String longContext = generateNotification(aqi, pollenCount);
                        showNotification(3, address, context, longContext);
                    }
                }
                Intent intent = new Intent();
                intent.putExtra("state", true);
                intent.setAction("com.liuyijiang.asthmahelper.service.BackGroundService.state");
                sendBroadcast(intent);
            }
        }).start();
    }

    private void showNotification(int id, String title, String context, String longContext) {
        Intent mainIntent = new Intent(this, MainActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.icon)
                .setContentTitle(title)
                .setContentText(context)
                .setContentIntent(mainPendingIntent)
                .setTicker(context)
                .setStyle(new Notification.BigTextStyle().bigText(longContext))
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);
        if (mManager != null) {
            mManager.notify(id, builder.build());
        }
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

    private boolean canDisturb() {
        boolean noDisturbing = config.getBoolean(NO_DISTURBING, false);
        Calendar now = Calendar.getInstance();
        int startHour = config.getInt(START_HOUR, 23);
        int startMinute = config.getInt(START_MINUTE, 0);
        int endHour = config.getInt(END_HOUR, 8);
        int endMinute = config.getInt(END_MINUTE, 0);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        if (noDisturbing) {
            if (startHour < endHour) {
                if ((startHour < hour) && (hour < endHour)) {
                    return false;
                } else if (startHour == hour) {
                    return startMinute > minute;
                } else if (hour == endHour) {
                    return minute > endMinute;
                }
            } else if (startHour == endHour) {
                return (startMinute > minute) || (minute > endMinute);
            } else if (startHour > endHour) {
                if ((startHour < hour) || (hour < endHour)) {
                    return false;
                } else if (startHour == hour) {
                    return startMinute > minute;
                } else if (hour == endHour) {
                    return minute > endMinute;
                }
            }
        }
        return true;
    }

    private boolean needDisturb(int aqi, int pollenCount) {
        int moderate = config.getInt(GOOD, 25) + 25;
        //int moderate = config.getInt(MODERATE, 25) + 75;
        int low = config.getInt(LOW, 25) + 25;
        return (moderate < aqi) || (low < pollenCount);
    }

    private double[] getLatLngFromLog(String[] target) {
        if (config.contains(target[ADDRESS])) {
            long defaultLat = Double.doubleToRawLongBits(-37.8136);
            long defaultLng = Double.doubleToRawLongBits(144.9631);
            double latitude = Double.longBitsToDouble(config.getLong(target[LATITUDE], defaultLat));
            double longitude = Double.longBitsToDouble(config.getLong(target[LONGITUDE], defaultLng));
            return new double[]{latitude, longitude};
        }
        return null;
    }

    private int getAQIData(double latitude, double longitude, int count) {
        if ((count <= 2) && (-38.1 <= latitude) && (latitude <= -37.5) && (144.7 <= longitude) && (longitude <= 145.45)) {
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
            return result;
        } else {
            HttpURLConnection connection = null;
            int aqi = -1;
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
            return aqi;
        }
    }

    private int getPollenCountData(double latitude, double longitude, int count) {
        int pollenCount;
        if ((count <= 2) && (-38.1 <= latitude) && (latitude <= -37.5) && (144.7 <= longitude) && (longitude <= 145.45)) {
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

    private String generateNotification(int aqi, int pollenCount) {
        int good = config.getInt(GOOD, 25) + 25;
        int moderate = config.getInt(MODERATE, 25) + 75;
        int poor = config.getInt(POOR, 25) + 125;
        int unhealthy = config.getInt(UNHEALTHY, 25) + 175;
        int veryUnhealthy = config.getInt(VERY_UNHEALTHY, 25) + 275;
        int low = config.getInt(LOW, 25) + 25;
        int medium = config.getInt(MEDIUM, 25) + 75;
        int high = config.getInt(HIGH, 25) + 155;
        String line1, line2, line3, line4;

        if (aqi < 0) {
            line1 = null;
            line2 = null;
        } else if (aqi <= good) {
            line1 = String.format(Locale.ENGLISH, "The AQI is %d and the air quality is good:", aqi);
            line2 = "Air quality is considered satisfactory, and air pollution poses little or no risk";
        } else if (aqi <= moderate) {
            line1 = String.format(Locale.ENGLISH, "The AQI is %d and the air quality is moderate:", aqi);
            line2 = "Air quality is acceptable; however, for some pollutants there may be a moderate health concern for a very small number of people who are unusually sensitive to air pollution";
        } else if (aqi <= poor) {
            line1 = String.format(Locale.ENGLISH, "The AQI is %d and the air quality is poor:", aqi);
            line2 = "Active children and adults, and people with respiratory disease, such as asthma, should limit prolonged outdoor exertion";
        } else if (aqi <= unhealthy) {
            line1 = String.format(Locale.ENGLISH, "The AQI is %d and the air quality is unhealthy:", aqi);
            line2 = "Active children and adults, and people with respiratory disease, such as asthma, should avoid prolonged outdoor exertion; everyone else, especially children, should limit prolonged outdoor exertion";
        } else if (aqi <= veryUnhealthy) {
            line1 = String.format(Locale.ENGLISH, "The AQI is %d and the air quality is very unhealthy:", aqi);
            line2 = "Active children and adults, and people with respiratory disease, such as asthma, should avoid all outdoor exertion; everyone else, especially children, should limit outdoor exertion";
        } else {
            line1 = String.format(Locale.ENGLISH, "The AQI is %d and the air quality is hazardous:", aqi);
            line2 = "Everyone should avoid all outdoor exertion";
        }

        if (pollenCount < 0) {
            line3 = null;
            line4 = null;
        } else if (pollenCount <= low) {
            line3 = String.format(Locale.ENGLISH, "The pollen count is %d whose level is low:", pollenCount);
            line4 = "Very few sufferers of pollen allergies would be expected to experience symptoms";
        } else if (pollenCount <= medium) {
            line3 = String.format(Locale.ENGLISH, "The pollen count is %d whose level is medium:", pollenCount);
            line4 = "Some sufferers of pollen allergies will experience symptoms, but these are unlikely to be severe";
        } else if (pollenCount <= high) {
            line3 = String.format(Locale.ENGLISH, "The pollen count is %d whose level is high:", pollenCount);
            line4 = "Many sufferers of pollen allergies will experience symptoms. It is better to stay away from sources of pollen";
        } else {
            line3 = String.format(Locale.ENGLISH, "The pollen count is %d whose level is extreme:", pollenCount);
            line4 = "Most sufferers of pollen allergies will experience symptoms and symptoms may be severe in some sufferers. The best precaution is to stay indoors";
        }
        String result;
        if ((line1 != null) && (line3 != null)) {
            result = String.format(Locale.ENGLISH, "\n%s\n\n%s\n\n%s\n\n%s", line1, line2, line3, line4);
        } else if (line1 != null) {
            result = String.format(Locale.ENGLISH, "\n%s\n\n%s", line1, line2);
        } else if (line3 != null) {
            result = String.format(Locale.ENGLISH, "\n%s\n\n%s", line3, line4);
        } else {
            result = "No Data Available!";
        }
        return result;
    }
}
