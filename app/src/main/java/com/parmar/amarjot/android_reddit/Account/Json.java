package com.parmar.amarjot.android_reddit.Account;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// Used for returned JSON data when signing in
public class Json {

    @SerializedName("data")
    @Expose
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Json{" +
                "data=" + data +
                '}';
    }
}
