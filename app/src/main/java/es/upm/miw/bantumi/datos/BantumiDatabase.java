package es.upm.miw.bantumi.datos;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Resultado.class}, version = 2, exportSchema = false)
public abstract class BantumiDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "bantumi_db";
    private static volatile BantumiDatabase INSTANCE;

    public abstract ResultadoDao resultadoDao();

    public static BantumiDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (BantumiDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            BantumiDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
