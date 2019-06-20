package com.app.maidi.database

import com.raizlabs.android.dbflow.annotation.Database

@Database(name = MaidiDatabase.NAME, version = MaidiDatabase.VERSION)
class MaidiDatabase {
    companion object{
        const val NAME = "Dhis2"
        const val VERSION = 1
    }
}