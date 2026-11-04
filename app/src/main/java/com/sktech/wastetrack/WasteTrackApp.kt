package com.sktech.wastetrack

import android.app.Application
import com.sktech.wastetrack.util.LocaleHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WasteTrackApp : Application() {
    override fun onCreate() {
        super.onCreate()
        LocaleHelper.init(this)
    }
}
