/*
package com.activity.pis_azil.room;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.migration.Migration;

@Database(entities = {Korisnici.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract KorisniciDao korisniciDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "MargetaAzil")
                            .addMigrations(MIGRATION_1_2)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Log.d("Migration", "Applying migration from version 1 to 2");
            database.execSQL("CREATE TABLE IF NOT EXISTS `korisnici` (`id_korisnika` INTEGER PRIMARY KEY AUTOINCREMENT, `ime` TEXT, `email` TEXT, `lozinka` TEXT, `admin` INTEGER, `profileImg` TEXT)");
            database.execSQL("ALTER TABLE korisnici ADD COLUMN profileImg TEXT");
            Log.d("Migration", "Migration from version 1 to 2 applied successfully");
        }
    };

}
*/