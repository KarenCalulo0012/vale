package com.kcalulo.vale

import android.app.Application
import android.content.Intent
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant
import kotlin.system.exitProcess

/**
 * Catches uncaught exceptions so a crash writes a local diagnostic log and relaunches the app
 * at Home instead of surfacing the OS "app has stopped" dialog. VALE is fully offline (spec:
 * cloud sync is LATER, not V1), so this stays local-only rather than pulling in a crash-reporting
 * SDK — logs land in app-private storage where they can be pulled via `adb bugreport` if needed.
 */
class ValeCrashHandler(
    private val application: Application,
    private val previousHandler: Thread.UncaughtExceptionHandler?,
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            writeCrashLog(throwable)
        } catch (loggingFailure: Exception) {
            Log.e(TAG, "Failed to write crash log", loggingFailure)
        }

        try {
            val restartIntent = Intent(application, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            application.startActivity(restartIntent)
        } catch (restartFailure: Exception) {
            Log.e(TAG, "Failed to restart after crash", restartFailure)
        }

        previousHandler?.uncaughtException(thread, throwable)
        exitProcess(1)
    }

    private fun writeCrashLog(throwable: Throwable) {
        val stackTrace = StringWriter().also { throwable.printStackTrace(PrintWriter(it)) }.toString()
        val crashDir = File(application.filesDir, "crash_logs").apply { mkdirs() }
        val logFile = File(crashDir, "crash_${Instant.now().toEpochMilli()}.txt")
        logFile.writeText("Crashed at ${Instant.now()}\n\n$stackTrace")

        // Keep only the most recent crash logs; no need to accumulate indefinitely.
        crashDir.listFiles()
            ?.sortedByDescending { it.lastModified() }
            ?.drop(MAX_RETAINED_LOGS)
            ?.forEach { it.delete() }
    }

    companion object {
        private const val TAG = "ValeCrashHandler"
        private const val MAX_RETAINED_LOGS = 5

        fun install(application: Application) {
            val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(ValeCrashHandler(application, previousHandler))
        }
    }
}
