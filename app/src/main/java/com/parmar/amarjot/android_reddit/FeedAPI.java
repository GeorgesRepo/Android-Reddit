package com.parmar.amarjot.android_reddit;

import com.parmar.amarjot.android_reddit.Account.CheckLogin;
import com.parmar.amarjot.android_reddit.model.Feed;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FeedAPI {

    String BASE_URL = "https://www.reddit.com/r/";

    // Used to get subbreddits
    @GET(BASE_URL + "{feed_name}/.rss")
    Call<Feed> getFeed(@Path("feed_name") String feed_name);

    // Used to sign in
    @POST("{user}")
    Call<CheckLogin> signIn(
            // Making up the url to make request with
            @HeaderMap Map<String, String> headers,
            @Path("user") String username,
            @Query("user") String user,
            @Query("passwd") String password,
            @Query("api_type") String type
    );
}
