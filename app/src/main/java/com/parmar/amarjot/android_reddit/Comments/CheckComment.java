package com.parmar.amarjot.android_reddit.Comments;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CheckComment {

    @SerializedName("sucess")
    @Expose
    public String success;

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "CheckComment{" +
                "success='" + success + '\'' +
                '}';
    }
}
