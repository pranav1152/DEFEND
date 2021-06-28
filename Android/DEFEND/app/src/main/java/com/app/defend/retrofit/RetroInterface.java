package com.app.defend.retrofit;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RetroInterface {

	@POST("/get_flags")
	Call<String> get_flags(@Body String string);
}
