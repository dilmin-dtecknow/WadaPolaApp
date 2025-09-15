package lk.javainstitute.wadapola.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SQLiteHelper extends SQLiteOpenHelper {

    public SQLiteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE contact (\n" +
                "    worker_id TEXT PRIMARY KEY\n" +
                "                   NOT NULL,\n" +
                "    fname     TEXT NOT NULL,\n" +
                "    lname     TEXT NOT NULL,\n" +
                "    mobile    TEXT NOT NULL,\n" +
                "    email     TEXT NOT NULL,\n" +
                "    type      TEXT NOT NULL\n" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
