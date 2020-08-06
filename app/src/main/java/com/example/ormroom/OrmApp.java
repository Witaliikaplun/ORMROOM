package com.example.ormroom;

import android.app.Application;

import androidx.room.Room;

public class OrmApp extends Application {

    private static final String DATABASE_NAME = "DATABASE_USER_GIT" ;
    public static UserGitDatabase database ;
    public static OrmApp INSTANCE ;
    @Override
    public void onCreate() {
        super .onCreate();
        database = Room. databaseBuilder (getApplicationContext()   ,
                UserGitDatabase. class , DATABASE_NAME ).build();
        INSTANCE = this ;
    }
    public UserGitDatabase getDB() {
        return database ;
    }
    public static OrmApp get() {
        return INSTANCE ;
    }
}
