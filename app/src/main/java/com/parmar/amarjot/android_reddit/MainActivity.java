package com.parmar.amarjot.android_reddit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parmar.amarjot.android_reddit.model.Feed;
import com.parmar.amarjot.android_reddit.model.entry.Entry;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";

    private static final String BASE_URL = "https://www.reddit.com/r/";

    private String currentFeed = "Art";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {

        // Sets up the reddit feed on list view
        initRedditFeed();
        setupSearchButton();
    }


    private void setupSearchButton() {

        Button button = findViewById(R.id.buttonRefreshFeed);
        final EditText editText = findViewById(R.id.editTextSearch);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String temp = editText.getText().toString();
                if (temp != null) {

                    currentFeed = temp;
                    initRedditFeed();
                }
            }
        });

    }

    private void initRedditFeed() {

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
            }

            @Override
            public void onFailure(Call<Feed> call, Throwable t) {

                Log.e(TAG, "onFailure: unable to retrieve RSS: " + t.getMessage());
                Toast.makeText(MainActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Get a feed from reddit API, extract usefull information and return it
    private ArrayList<Post> extractRedditFeed(Response<Feed>  response) {
        List<Entry> entries = response.body().getEntries();

        // Used to store content that is displayed in list
        ArrayList<Post> posts = new ArrayList<Post>();

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
                    postContent.get(lastPosition)
            ));

            //printPost(posts);
        }
        return posts;
    }

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

                startActivity(intent);
            }
        });
    }

    // Used to print content contained in posts
    private void printPost(ArrayList<Post> posts) {
        for(int j = 0; j<posts.size(); j++){
            Log.d(TAG, "onResponse: \n " +
                    "PostURL: " + posts.get(j).getPostURL() + "\n " +
                    "ThumbnailURL: " + posts.get(j).getThumbnailURL() + "\n " +
                    "Title: " + posts.get(j).getTitle() + "\n " +
                    "Author: " + posts.get(j).getAuthor() + "\n " +
                    "updated: " + posts.get(j).getDate_updated() + "\n ");
        }
    }
}
