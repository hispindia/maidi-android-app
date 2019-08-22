package com.app.maidi.custom

import net.hockeyapp.android.CrashManagerListener

class MaidiCrashManagerListener : CrashManagerListener() {
    override fun shouldAutoUploadCrashes(): Boolean {
        return true
    }
}