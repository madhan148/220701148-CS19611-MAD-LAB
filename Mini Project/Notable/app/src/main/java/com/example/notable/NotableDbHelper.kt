package com.example.notable

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDate

class NotableDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "notable.db"
        const val DATABASE_VERSION = 1

        const val TABLE_EVENTS = "events"
        const val COLUMN_NAME = "name"
        const val COLUMN_DATE = "date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_EVENTS (
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                PRIMARY KEY ($COLUMN_NAME, $COLUMN_DATE)
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EVENTS")
        onCreate(db)
    }

    fun addEvent(name: String, date: LocalDate) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_DATE, date.toString())
        }
        db.insertWithOnConflict(TABLE_EVENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllEvents(): List<Pair<String, LocalDate>> {
        val events = mutableListOf<Pair<String, LocalDate>>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_EVENTS,
            arrayOf(COLUMN_NAME, COLUMN_DATE),
            null, null, null, null,
            "$COLUMN_DATE ASC"
        )

        with(cursor) {
            while (moveToNext()) {
                val name = getString(getColumnIndexOrThrow(COLUMN_NAME))
                val dateStr = getString(getColumnIndexOrThrow(COLUMN_DATE))
                events.add(name to LocalDate.parse(dateStr))
            }
        }
        cursor.close()
        return events
    }

    fun deleteEvent(name: String, date: LocalDate) {
        val db = writableDatabase
        db.delete(
            TABLE_EVENTS,
            "$COLUMN_NAME = ? AND $COLUMN_DATE = ?",
            arrayOf(name, date.toString())
        )
    }
}