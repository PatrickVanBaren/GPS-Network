package com.example.androidtutorialgps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Хранилище - поставщик данных.
 */
public class GpsDataStorage {

    private static GpsDataStorage sInstance;

    private final int CAPACITY = 20;

    private final List<Data> mDataList = new ArrayList<>(CAPACITY);

    private Listener mListener;

    public static void createInstance() {
        sInstance = new GpsDataStorage();
    }

    public static GpsDataStorage getInstance() {
        return sInstance;
    }

    public void addData(final double latitude, final double longitude) {
        addToArray(latitude, longitude, true);
    }

    public void addError() {
        addToArray(0.0f, 0.0f, false);
    }

    public void setListener(final Listener listener) {
        mListener = listener;
    }

    public final List<Data> getAll() { return Collections.unmodifiableList(mDataList); }

    /**
     * "Прокручиваемый" лист - старые данные удаляются.
     * @param latitude
     * @param longitude
     * @param success
     */
    private void addToArray(double latitude, double longitude, final boolean success) {
        final Data data = new Data(System.currentTimeMillis(), latitude, longitude, success);
        if(mDataList.size() == CAPACITY) {
            mDataList.remove(0);
        }
        mDataList.add(data);

        if (mListener != null) {
            mListener.onDataChanged(this);
        }
    }

    /**
     * Интерфейс подписки
     */
    public interface Listener {
        void onDataChanged(GpsDataStorage sender);
    }

    /**
     * Класс неизменяемых данных, для простоты без акцессоров.
     */
    public static class Data {

        public final long timestamp;

        public final double latitude;

        public final double longitude;

        public final boolean success;

        public Data(long timestamp, double latitude, double longitude, boolean success) {
            this.timestamp = timestamp;
            this.latitude = latitude;
            this.longitude = longitude;
            this.success = success;
        }

    }
}
