package com.parmar.amarjot.android_reddit;

import com.parmar.amarjot.android_reddit.model.Feed;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface FeedAPI {

    String BASE_URL = "https://www.reddit.com/r/";


    @GET(BASE_URL + "{feed_name}/.rss")
    Call<Feed> getFeed(@Path("feed_name") String feed_name);
}
