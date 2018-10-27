package com.parmar.amarjot.android_reddit.Comments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parmar.amarjot.android_reddit.ExtractXML;
import com.parmar.amarjot.android_reddit.FeedAPI;
import com.parmar.amarjot.android_reddit.R;
import com.parmar.amarjot.android_reddit.URLS;
import com.parmar.amarjot.android_reddit.model.Feed;
import com.parmar.amarjot.android_reddit.model.entry.Entry;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class CommentsActivity extends AppCompatActivity {

    private static final String TAG = "CommentsActivity";
    URLS urls = new URLS();

    private static String postURL, postThumbnailURL, postTitle, postAuthor, postUpdated, postID;

    private int defaultImage;

    private String currentFeed;
    private ListView mListView;

    private ArrayList<Comment> mComments;
    private ProgressBar mProgressBar;
    private TextView progressText;

    // No longer used, was being used when attempting to post comments
    //private String modhash, cookie, username;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_comments_layout);

        mProgressBar = findViewById(R.id.commentsLoadingProgressBar);
        progressText = findViewById(R.id.progressText);

        // No longer used, was being used when attempting to post comments
        //getSessionParms();
        setupToolbar();

        mProgressBar.setVisibility(View.VISIBLE);

        setupImageLoader();
        initPost();
        init();
    }

    private void setupToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24px);
    }

    private void init(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urls.BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        FeedAPI feedAPI = retrofit.create(FeedAPI.class);

        Call<Feed> call = feedAPI.getFeed(currentFeed);

        call.enqueue(new Callback<Feed>() {
            @Override
            public void onResponse(Call<Feed> call, Response<Feed> response) {
                Log.d(TAG, "onResponse: feed: " + response.body().toString());
                Log.d(TAG, "onResponse: Server Response: " + response.toString());

                mComments = new ArrayList<>();
                List<Entry> entries = response.body().getEntries();
                for ( int i = 0; i < entries.size(); i++){
                    ExtractXML extract = new ExtractXML(entries.get(i).getContent(), "<div class=\"md\"><p>","</p>");
                    List<String> commentDetails = extract.start();

                    try{
                        Comment comment = new Comment(commentDetails.get(0),
                                entries.get(i).getAuthor().getName(),
                                entries.get(i).getUpdated(),
                                entries.get(i).getId());
                        mComments.add(comment);
                    }catch (IndexOutOfBoundsException e){

                        Log.e(TAG, "onResponse: IndexOutOfBoundsException: " + e.getMessage() );
                    }
                    catch (NullPointerException e){

                        Comment comment = new Comment(
                                commentDetails.get(0),
                                getString(R.string.none),
                                entries.get(i).getUpdated(),
                                entries.get(i).getId());

                        mComments.add(comment);
                        Log.e(TAG, "onResponse: NullPointerException: " + e.getMessage() );
                    }
                }
                mListView = findViewById(R.id.commentsListView);

                CommentsListAdapter adapter = new CommentsListAdapter(CommentsActivity.this, R.layout.comment_layout, mComments);
                mListView.setAdapter(adapter);

                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        getUserComment(mComments.get(position).getId());
                    }
                });

                mProgressBar.setVisibility(View.GONE);
                progressText.setText("");
            }

            @Override
            public void onFailure(Call<Feed> call, Throwable t) {
                Log.e(TAG, "onFailure: Unable to retrieve RSS: " + t.getMessage() );
                Toast.makeText(CommentsActivity.this, "An Error Occured", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initPost(){
        Intent incomingIntent = getIntent();
        postURL = incomingIntent.getStringExtra(getString(R.string.post_url));
        postThumbnailURL = incomingIntent.getStringExtra(getString(R.string.post_thumbnail));
        postTitle = incomingIntent.getStringExtra(getString(R.string.post_title));
        postAuthor = incomingIntent.getStringExtra(getString(R.string.post_author));
        postUpdated = incomingIntent.getStringExtra(getString(R.string.post_updated));
        postID = incomingIntent.getStringExtra(getString(R.string.post_id));

        TextView title = findViewById(R.id.postTitle);
        TextView author = findViewById(R.id.postAuthor);
        TextView updated = findViewById(R.id.postUpdated);
        ImageView thumbnail = findViewById(R.id.postThumbnail);
        Button btnReply = findViewById(R.id.btnPostReply);
        ProgressBar progressBar = findViewById(R.id.postLoadingProgressBar);

        title.setText(postTitle);
        author.setText(postAuthor);
        updated.setText(postUpdated);
        displayImage(postThumbnailURL, thumbnail, progressBar);

        //NOTE: NSFW posts will cause an error. We can catch it with ArrayIndexOutOfBoundsException
        try{
            String[] splitURL = postURL.split(urls.BASE_URL);
            currentFeed = splitURL[1];
        }catch (ArrayIndexOutOfBoundsException e){
            Log.e(TAG, "initPost: ArrayIndexOutOfBoundsException: " + e.getMessage() );
        }

        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: reply.");
                getUserComment(postID);
            }
        });

    }

    private void getUserComment(final String post_id){
        final Dialog dialog = new Dialog(CommentsActivity.this);
        dialog.setTitle(R.string.dialog);
        dialog.setContentView(R.layout.comment_input_dialog);

        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.95);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.6);

        dialog.getWindow().setLayout(width, height);
        dialog.show();

        Button btnPostComment = dialog.findViewById(R.id.btnPostComment);

        btnPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CommentsActivity.this, R.string.featureNotImplemented, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayImage(String imageURL, ImageView imageView, final ProgressBar progressBar){

        //create the imageloader object
        ImageLoader imageLoader = ImageLoader.getInstance();

        //create display options
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();

        //download and display image from url
        imageLoader.displayImage(imageURL, imageView, options , new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                progressBar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                progressBar.setVisibility(View.GONE);
            }

        });
    }

    /**
     * Required for setting up the Universal Image loader Library
     */
    private void setupImageLoader(){
        // UNIVERSAL IMAGE LOADER SETUP
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                CommentsActivity.this)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
        // END - UNIVERSAL IMAGE LOADER SETUP

        defaultImage = CommentsActivity.this.getResources().getIdentifier(getString(R.string.default_img),null,CommentsActivity.this.getPackageName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        this.overridePendingTransition(R.anim.slide_out_left,
                R.anim.slide_in_left);
    }

// No longer used, was being used when attempting to post comments
//    public void getSessionParms() {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(CommentsActivity.this);
//
//        username = preferences.getString(getString(R.string.SessionUsername), "");
//        modhash = preferences.getString(getString(R.string.SessionModhash), "");
//        cookie = preferences.getString(getString(R.string.SessionCookie), "");
//
//        Log.d(TAG, "getSessionParms: Storing session variables:  \n" +
//                "username: " + username + "\n" +
//                "modhash: " + modhash + "\n" +
//                "cookie: " + cookie + "\n"
//        );
//    }
}
