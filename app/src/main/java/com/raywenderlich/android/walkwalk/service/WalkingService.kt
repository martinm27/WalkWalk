/*
 * Copyright (c) 2022 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.walkwalk.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.widget.Toast
import com.raywenderlich.android.walkwalk.WalkingSensorListener
import com.raywenderlich.android.walkwalk.utility.NotificationUtility
import com.raywenderlich.android.walkwalk.utility.SharedPreferencesUtility
import java.util.concurrent.atomic.AtomicInteger

private const val WAKE_LOCK_TIMEOUT_TEN_MINUTES = 10 * 60 * 1000L

class WalkingService : Service() {

  private var wakeLock: PowerManager.WakeLock? = null
  private var isServiceStarted = false

  private val notificationUtility by lazy { NotificationUtility(this) }
  private val sensorStepCountLastValue = AtomicInteger()

  private val walkingSensorListener by lazy {
    WalkingSensorListener(this, this::updateNotification)
  }

  override fun onCreate() {
    super.onCreate()

    Toast.makeText(this, "Service started", Toast.LENGTH_LONG).show()
    startForeground(NotificationUtility.NOTIFICATION_ID, notificationUtility.getNotification())
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent != null) {
      when (intent.action) {
        ForegroundServiceState.STARTED.name -> startWalkingService()
        ForegroundServiceState.STOPPED.name -> stopWalkingService()
        else -> throw IllegalArgumentException("This should never happen. No action in the received intent")
      }
    }
    // by returning this we make sure the service is restarted if the system kills the service
    return START_STICKY
  }

  private fun startWalkingService() {
    if (isServiceStarted) return

    isServiceStarted = true
    SharedPreferencesUtility.setForegroundServiceState(this, ForegroundServiceState.STARTED)

    walkingSensorListener.reRegisterSensor()

    // we need this lock so our service gets not affected by Doze Mode
    wakeLock =
        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
          newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WalkingService::lock").apply {
            acquire(WAKE_LOCK_TIMEOUT_TEN_MINUTES)
          }
        }
  }

  private fun stopWalkingService() {
    try {
      wakeLock?.let {
        if (it.isHeld) {
          it.release()
        }
      }
      walkingSensorListener.deregisterSensor()
      stopForeground(true)
      stopSelf()
    } catch (e: Exception) {
      e.printStackTrace()
    }
    isServiceStarted = false
    SharedPreferencesUtility.setForegroundServiceState(this, ForegroundServiceState.STOPPED)
  }

  private fun updateNotification(stepCountValue: Int) {
    if (stepCountValue != sensorStepCountLastValue.get()) {
      sensorStepCountLastValue.set(stepCountValue)
      notificationUtility.updateNotification(sensorStepCountLastValue.get().toString())
    }
  }

/*
  override fun onTaskRemoved(rootIntent: Intent) {
    val restartServiceIntent = Intent(applicationContext, WalkingService::class.java).apply {
      setPackage(packageName)
    }

    val restartServicePendingIntent: PendingIntent =
        PendingIntent.getService(
            this,
            1,
            restartServiceIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            else PendingIntent.FLAG_ONE_SHOT)

    (applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
        .set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent)
  }
*/

  override fun onBind(intent: Intent?): IBinder? = null
}
