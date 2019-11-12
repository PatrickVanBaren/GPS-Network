package com.example.androidtutorialgps;

import android.app.Application;

public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		GpsDataStorage.createInstance();
	}
}
