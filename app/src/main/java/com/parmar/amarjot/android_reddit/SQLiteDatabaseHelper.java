package com.parmar.amarjot.android_reddit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteDatabaseHelper extends SQLiteOpenHelper {

    private String TABLE_NAME;
    private static final String COL0 = "ID";
    private static final String COL1 = "title";
    private static final String COL2 = "author";
    private static final String COL3 = "date_updated";
    private static final String COL4 = "postURL";
    private static final String COL5 = "thumbnailURL";
    private static final String COL6 = "postID";

    public SQLiteDatabaseHelper(Context context, String _tableName) {

        super(context, _tableName, null, 1);
        TABLE_NAME = _tableName;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        Log.d("SQL" , "onCreate: Created");

        String createTable = "CREATE TABLE " + TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " " + COL1 + " TEXT," + " " + COL2 + " TEXT," + " " + COL3 + " TEXT," + " " +
                COL4 + " TEXT," + " " + COL5 + " TEXT," + " " +
                COL6 + " TEXT)";

        Log.d("SQL" , createTable);
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP IF TABLE EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean addPost(Post post) {

        Post addPost = post;
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, addPost.getTitle());
        contentValues.put(COL2, addPost.getAuthor());
        contentValues.put(COL3, addPost.getDate_updated());
        contentValues.put(COL4, addPost.getPostURL());
        contentValues.put(COL5, addPost.getThumbnailURL());
        contentValues.put(COL6, addPost.getId());
        SQLiteDatabase db = this.getWritableDatabase();

        long result = db.insert(TABLE_NAME, null, contentValues);

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean deleteRecipe(String recipeName) {

        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TABLE_NAME, COL1 + "=\"" + recipeName + "\"", null);

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public Cursor getPosts() {

        Cursor posts;
        SQLiteDatabase db = this.getWritableDatabase();
        String query = null;

        query = "SELECT * FROM " + TABLE_NAME;
        posts = db.rawQuery(query, null);

        return posts;
    }

    public Post getPost(String id) {
        Post post = null;
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL6 + "=" + "\"" + id + "\"";
        Cursor data = db.rawQuery(query, null);


        while (data.moveToNext()) {
            String postTitle = data.getString(1);
            String postAuthor = data.getString(2);
            String postDate_updated = data.getString(3);
            String postPostURL = data.getString(4);
            String postThumbnailURL = data.getString(5);
            String postID= data.getString(6);
            post = new Post(postTitle, postAuthor, postDate_updated, postPostURL, postThumbnailURL, postID);
            return post;
        }
        return post;
    }

    public void clearDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDBQuery = "DELETE FROM "+TABLE_NAME;
        db.execSQL(clearDBQuery);
    }
}