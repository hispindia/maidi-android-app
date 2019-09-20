package com.app.maidi.utils

import net.hockeyapp.android.CrashManagerListener

class MaidiCrashManagerListener : CrashManagerListener() {
    override fun shouldAutoUploadCrashes(): Boolean {
        return true
    }
}