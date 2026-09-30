package com.canoezequiel.moodflow

import android.app.Application

class MoodApplication : Application() {
    companion object {
        lateinit var context: Application
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }
}