/*
package com.activity.pis_azil.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.activity.pis_azil.models.UserModel;

public class UserDatabase {

    private static final String TAG = "UserDatabase";
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public UserDatabase(Context context) {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public void saveUser(UserModel user) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ID, user.getIdKorisnika());
        values.put(DatabaseHelper.COLUMN_NAME, user.getIme());
        values.put(DatabaseHelper.COLUMN_EMAIL, user.getEmail());
        values.put(DatabaseHelper.COLUMN_PROFILE_IMG, user.getProfileImg());

        int rows = database.update(DatabaseHelper.TABLE_USER, values, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(user.getIdKorisnika())});
        if (rows == 0) {
            database.insert(DatabaseHelper.TABLE_USER, null, values);
        }
    }

    public UserModel getUser() {
        UserModel user = null;
        Cursor cursor = database.query(DatabaseHelper.TABLE_USER, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            user = new UserModel();
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
            int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME);
            int emailIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL);
            int profileImgIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMG);

            if (idIndex != -1) {
                user.setIdKorisnika(cursor.getInt(idIndex));
            } else {
                Log.e(TAG, "COLUMN_ID index not found");
            }

            if (nameIndex != -1) {
                user.setIme(cursor.getString(nameIndex));
            } else {
                Log.e(TAG, "COLUMN_NAME index not found");
            }

            if (emailIndex != -1) {
                user.setEmail(cursor.getString(emailIndex));
            } else {
                Log.e(TAG, "COLUMN_EMAIL index not found");
            }

            if (profileImgIndex != -1) {
                user.setProfileImg(cursor.getString(profileImgIndex));
            } else {
                Log.e(TAG, "COLUMN_PROFILE_IMG index not found");
            }

            cursor.close();
        }
        return user;
    }
}
*/