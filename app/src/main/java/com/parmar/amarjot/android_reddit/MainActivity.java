package com.parmar.amarjot.android_reddit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.parmar.amarjot.android_reddit.Account.LoginActivity;
import com.parmar.amarjot.android_reddit.Comments.CommentsActivity;
import com.parmar.amarjot.android_reddit.model.Feed;
import com.parmar.amarjot.android_reddit.model.entry.Entry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;


public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";

    private static final String BASE_URL = "https://www.reddit.com/r/";

    // Default reddit subreddit feed
    private String currentFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    // Updates menu item when user signs in
    @Override
    protected void onPostResume() {
        super.onPostResume();
        setupToolbar();
    }

    private void init() {

        currentFeed = getLastSearchedSubreddit();
        setupToolbar();

        // if local cached reddit feed is recent enough, use that
        if (useLocalDB()) {
            Log.e(TAG, "init: displaying local list");
            //pullRedditFeedOnline();
            pullRedditFeedLocal();
        }
        // otherwise fetch the latest one and cache it
        else {
            Log.e(TAG, "init: displaying online list");
            pullRedditFeedOnline();
        }

        setupSearchButton();
    }

    // returns true if local cache is recently updated
    private boolean useLocalDB () {

        SharedPreferences prefs = getSharedPreferences(getString(R.string.SessionLastTime), MODE_PRIVATE);
        long lastTime = prefs.getLong("time", 0); //0 is the default value.

        if (lastTime != 0) {

            Date oldDate = new Date(lastTime);
            Date currentDate = new Date();

            long diff = currentDate.getTime() - oldDate.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;

            long totalMinutes = minutes + (hours * 60);

            if ( totalMinutes > 1) {
                return false;
            }
            else {
                return true;
            }
        }
        return false;
    }

    // saves current time, used to determine if local cache is recent
    private void saveCurrentTime() {
        Log.e(TAG, "saveCurrentTime: creared" );
        Date currentDate = new Date();
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.SessionLastTime), MODE_PRIVATE).edit();
        editor.putLong("time", currentDate.getTime());
        editor.apply();
    }

    // Pulls cached reddit feed from local DB
    private void pullRedditFeedLocal (){
        attachRedditFeedToList(loadPosts());
    }

    // Pulls reddit feed from .rss using retro fit
    private void pullRedditFeedOnline() {

        // Get Retrofit to fetch Reddit feed
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL).addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        FeedAPI feedAPI = retrofit.create(FeedAPI.class);

        Call<Feed> call = feedAPI.getFeed(currentFeed);

        call.enqueue(new Callback<Feed>() {
            @Override
            public void onResponse(Call<Feed> call, Response<Feed> response) {

                // Extract reddit feed into posts & display in list
                ArrayList<Post> posts = extractRedditFeed(response);
                attachRedditFeedToList(posts);
                // Caching reddit feed & recording what time these were fetched
                savePosts(posts);
                saveCurrentTime();
            }

            @Override
            public void onFailure(Call<Feed> call, Throwable t) {

                Log.e(TAG, "onFailure: unable to retrieve RSS: " + t.getMessage());
                Toast.makeText(MainActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //  Extract usefull information and return it
    private ArrayList<Post> extractRedditFeed(Response<Feed>  response) {

        // Used to store content that is displayed in list
        ArrayList<Post> posts = new ArrayList<Post>();

        try {
            List<Entry> entries = response.body().getEntries();

            // For each entry, extract and add it to posts array
            for (int i = 0; i< entries.size(); i++){

                // Extract href tag
                ExtractXML extractXML1 = new ExtractXML(entries.get(i).getContent(), "<a href=");
                List<String> postContent = extractXML1.start();

                // Extract image tag
                ExtractXML extractXML2 = new ExtractXML(entries.get(i).getContent(), "<img src=");
                try{
                    // if their is a img thumbnail it will only have one, hence 0 index
                    postContent.add(extractXML2.start().get(0));
                }
                catch (NullPointerException e){
                    postContent.add(null);
                    Log.e(TAG, "onResponse: NullPointerException(thumbnail):" + e.getMessage() );
                }
                catch (IndexOutOfBoundsException e){
                    postContent.add(null);
                    Log.e(TAG, "onResponse: IndexOutOfBoundsException(thumbnail):" + e.getMessage() );
                }


                int lastPosition = postContent.size() - 1;

                // Add data to post (each post used for single card in list)
                posts.add(new Post(
                        entries.get(i).getTitle(),
                        entries.get(i).getAuthor().getName(),
                        entries.get(i).getUpdated(),
                        // extracted href
                        postContent.get(0),
                        // extracted img thumbnail
                        postContent.get(lastPosition),
                        entries.get(i).getId()
                ));

                //printPost(posts);
            }
            return posts;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return posts;
    }

    // Attaches Posts to list and adds onClick listner
    private void attachRedditFeedToList(final ArrayList<Post> posts) {

        ListView listView = findViewById(R.id.listView);
        CustomListAdapter customListAdapter = new CustomListAdapter(MainActivity.this, R.layout.post_layout, posts);
        listView.setAdapter(customListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG , "onItemClick: Clicked: " + posts.get(i).toString());

                Intent intent = new Intent(MainActivity.this, CommentsActivity.class);

                intent.putExtra(getString(R.string.post_url), posts.get(i).getPostURL());
                intent.putExtra(getString(R.string.post_thumbnail), posts.get(i).getThumbnailURL());
                intent.putExtra(getString(R.string.post_title), posts.get(i).getTitle());
                intent.putExtra(getString(R.string.post_author), posts.get(i).getAuthor());
                intent.putExtra(getString(R.string.post_updated), posts.get(i).getDate_updated());
                intent.putExtra(getString(R.string.post_id), posts.get(i).getId());

                startActivity(intent);
                overridePendingTransition( R.anim.slide_in_right, R.anim.slide_out_right);
            }
        });
    }

    // Saves reddit feed into local db
    private void savePosts(ArrayList<Post> posts) {

        Log.d(TAG , "savePosts: Created");
        SQLiteDatabaseHelper feed = new SQLiteDatabaseHelper(this,"recentFeed");

        feed.clearDatabase();

        for (int i = 0; i < posts.size(); i++) {
            Log.d(TAG , "savePosts: Saving: " + posts.get(i).getId());
            feed.addPost(posts.get(i));
        }
    }

    // Get cached local feed and return it
    private ArrayList<Post> loadPosts() {
        Log.d(TAG , "loadPosts: Created");
        ArrayList<Post> posts = new  ArrayList<>();

        SQLiteDatabaseHelper feed = new SQLiteDatabaseHelper(this,"recentFeed");

        Cursor loadedPosts = feed.getPosts();
        while (loadedPosts.moveToNext()) {

            String title = loadedPosts.getString(1);
            String author = loadedPosts.getString(2);
            String date_updated = loadedPosts.getString(3);
            String postURL = loadedPosts.getString(4);
            String thumbnailURL = loadedPosts.getString(5);
            String id = loadedPosts.getString(6);


            Post newPost = new Post(title, author, date_updated, postURL, thumbnailURL, id);
            posts.add(newPost);
        }

        return posts;
    }

    // Checks if user if currently logged in
    public Boolean sessionExist() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        String username = preferences.getString(getString(R.string.SessionUsername), "");
        String modhash = preferences.getString(getString(R.string.SessionModhash), "");
        String cookie = preferences.getString(getString(R.string.SessionCookie), "");

        if (username.equals("") & modhash.equals("") & cookie.equals("")) {
            return false;
        }

        return true;
    }

    // Deletes users credentials, essentially logging them out.
    private void removeSession() {
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).
                edit().clear().apply();
    }

    // Sets up tool bar, & menu depending on if user is logged in or not
    private void setupToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.d(TAG, "onMenuItemClick: clicked menu item: " + item);


                switch(item.getItemId()){
                    case R.id.navLogin:
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.navLogout:
                        removeSession();
                        setupToolbar();
                        Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.navChangePassword:
                        Toast.makeText(MainActivity.this, "To be implemented", Toast.LENGTH_SHORT).show();
                        break;
                }

                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (sessionExist()) {
            getMenuInflater().inflate(R.menu.logined_in_menu, menu);
        }
        else {
            getMenuInflater().inflate(R.menu.login_menu, menu);
        }

        return true;
    }

    private void setupSearchButton() {

        ImageButton button = findViewById(R.id.buttonRefreshFeed);
        button.setBackground(getDrawable(R.drawable.ic_baseline_refresh_24px));

        final EditText editText = findViewById(R.id.editTextSearch);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String temp = editText.getText().toString();
                if (!temp.equals("")){
                    Log.e(TAG, "setupSearchButton: user searched: " + temp);
                    currentFeed = temp;
                    pullRedditFeedOnline();
                    setLastSearchedSubreddit();
                }
            }
        });

    }

    private void setLastSearchedSubreddit() {

        Log.e(TAG, "setLastSearchedSubreddit: creared" );
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.searchText), MODE_PRIVATE).edit();
        editor.putString("subreddit", currentFeed);
        editor.apply();
    }

    private String getLastSearchedSubreddit() {

        SharedPreferences prefs = getSharedPreferences(getString(R.string.searchText), MODE_PRIVATE);
        String lastSearchedSubreddit = prefs.getString("subreddit", ""); //"" is the default value.

        if (lastSearchedSubreddit.equals("")) {
            return getString(R.string.searchText);
        }

        return lastSearchedSubreddit;
    }

    // Used to print content contained in posts, left here for debugging
    private void printPost(ArrayList<Post> posts) {
        for(int j = 0; j<posts.size(); j++){
            Log.d(TAG, "onResponse: \n " +
                    "PostURL: " + posts.get(j).getPostURL() + "\n " +
                    "ThumbnailURL: " + posts.get(j).getThumbnailURL() + "\n " +
                    "Title: " + posts.get(j).getTitle() + "\n " +
                    "Author: " + posts.get(j).getAuthor() + "\n " +
                    "updated: " + posts.get(j).getDate_updated() + "\n " +
                    "id: " + posts.get(j).getId() + "\n ");
        }
    }
}
