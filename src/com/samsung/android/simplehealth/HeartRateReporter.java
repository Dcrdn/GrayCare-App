package com.samsung.android.simplehealth;

import android.util.Log;

import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthDataObserver;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import java.util.Calendar;
import java.util.TimeZone;


public class HeartRateReporter {
    private final HealthDataStore mStore;
    private HeartRateObserver mHeartRateObserver;
    private static final long ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L;

    public HeartRateReporter(HealthDataStore store) {
        mStore = store;
    }

    public void start(HeartRateObserver listener) {
        Log.d("DIEGO", "Iniciando constructor.");

        mHeartRateObserver = listener;
        Log.d("DIEGO", "asigna el listener.");

        HealthDataObserver.addObserver(mStore, HealthConstants.HeartRate.HEALTH_DATA_TYPE, mObserver);
        Log.d("DIEGO", "termina de crear observador. Apunto de crear readHeartRate");

        readHeartRate();
    }

    private void readHeartRate() {
        Log.d("DIEGO", "Entrando a heart rate.");

        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        // Set time range from start time of today to the current time
        long startTime = getStartTimeOfToday();
        long endTime = startTime + ONE_DAY_IN_MILLIS;
        Log.d("DIEGO", "apunto de crear el request");
/*
        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.HeartRate.HEART_RATE)
                .setProperties(new String[] {HealthConstants.HeartRate.HEART_RATE})
                .setLocalTimeRange(HealthConstants.HeartRate.START_TIME, HealthConstants.HeartRate.TIME_OFFSET,
                        startTime, endTime)
                .build();*/

        HealthDataResolver.Filter filter = HealthDataResolver.Filter.and(HealthDataResolver.Filter.greaterThanEquals(HealthConstants.HeartRate.START_TIME, startTime),
                HealthDataResolver.Filter.lessThanEquals(HealthConstants.HeartRate.START_TIME, endTime));

        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.HeartRate.HEALTH_DATA_TYPE)
                .setProperties(new String[]{
                        HealthConstants.HeartRate.HEART_BEAT_COUNT,
                        HealthConstants.HeartRate.HEART_RATE
                })
                .setFilter(filter)
                .build();

        Log.d("DIEGO", "termina el request y no muere ");


        try {
            Log.d("DIEGO", "llega al try");

            resolver.read(request).setResultListener(mListener);
        } catch (Exception e) {
            Log.d("DIEGO", "llega aca ptm");

            Log.e("*&*&*&", "Getting Heart fails.", e);
        }
    }

    private long getStartTimeOfToday() {
        Calendar today = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        return today.getTimeInMillis();
    }

    private final HealthResultHolder.ResultListener<HealthDataResolver.ReadResult> mListener = result -> {
        int count = 0;
        Log.d("DIEGO", "entra al mListener");

        try {
            Log.d("DIEGO", "try de mListener");

            for (HealthData data : result) {
                count = data.getInt(HealthConstants.HeartRate.HEART_RATE);
                Log.d("DIEGO", data.getInt(HealthConstants.HeartRate.HEART_RATE)+"");
            }
        }finally {
            Log.d("DIEGO", "c muere");
            result.close();
        }

        if (mHeartRateObserver != null) {
            mHeartRateObserver.onChanged(count);
        }
    };

    private final HealthDataObserver mObserver = new HealthDataObserver(null) {

        @Override
        public void onChange(String dataTypeName) {
            Log.d("*&*&*&", "Observer receives a data changed event");
            readHeartRate();
        }
    };

    public interface HeartRateObserver {
        void onChanged(int count);
    }
}
