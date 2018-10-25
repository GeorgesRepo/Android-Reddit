package com.parmar.amarjot.android_reddit;

import com.parmar.amarjot.android_reddit.model.Feed;

import retrofit2.Call;
import retrofit2.http.GET;

public interface FeedAPI {

    String BASE_URL = "https://www.reddit.com/r/";

    @GET("earthporn/.rss")
    Call<Feed> getFeed();

}
