package com.example.ormroom;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RoomModelDao {
    @Query( "SELECT * FROM roommodel" )
    abstract List<RoomModel> getAll();

//    @Query ( "SELECT * FROM roommodel WHERE userId LIKE :name LIMIT 1" )
//    RoomModel findByUserId(String userId);

    @Insert
    void insertAll(List<RoomModel> item);

    @Update
    void update(RoomModel item);

    @Delete
    void delete(RoomModel item);

    @Query ( "DELETE FROM roommodel" )
    void deleteAll();
}