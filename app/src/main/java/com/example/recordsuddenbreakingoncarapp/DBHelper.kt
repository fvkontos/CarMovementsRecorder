package com.example.recordsuddenbreakingoncarapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "CREATE TABLE $TABLE_SPEED (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_SPEED REAL," +
                "$COLUMN_TIMESTAMP TEXT," +
                "$COLUMN_LATITUDE REAL," +
                "$COLUMN_LONGITUDE REAL)"
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SPEED")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "speed.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_SPEED = "speed"
        const val COLUMN_ID = "id"
        const val COLUMN_SPEED = "speed"
        const val COLUMN_TIMESTAMP = "timestamp"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
    }
}