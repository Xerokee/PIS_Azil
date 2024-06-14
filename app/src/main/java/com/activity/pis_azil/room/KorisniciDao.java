/*
package com.activity.pis_azil.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface KorisniciDao {
    @Insert
    void insert(Korisnici user);

    @Update
    void update(Korisnici user);

    @Query("SELECT * FROM korisnici WHERE id_korisnika = :id LIMIT 1")
    Korisnici getUserById(int id);

    @Query("SELECT * FROM korisnici WHERE email = :email LIMIT 1")
    Korisnici getUserByEmail(String email);
}
*/