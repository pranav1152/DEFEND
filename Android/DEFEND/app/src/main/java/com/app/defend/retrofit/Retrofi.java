package com.app.defend.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Retrofi {
	public static Retrofit init() {
		return new Retrofit.Builder().baseUrl("http://192.168.0.103:5000/").addConverterFactory(GsonConverterFactory.create()).build();
	}
}
