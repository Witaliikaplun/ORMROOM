package com.example.ormroom;

import androidx.room.Database;
import androidx.room.RoomDatabase;



@Database(entities = {RoomModel.class }, version = 1 )
public abstract class UserGitDatabase extends RoomDatabase {
    public abstract RoomModelDao productDao();
}
