package com.parmar.amarjot.android_reddit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

class CommentsActivity extends AppCompatActivity {

    private static final String TAG = "CommentsActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_comments_layout);
    }
}
