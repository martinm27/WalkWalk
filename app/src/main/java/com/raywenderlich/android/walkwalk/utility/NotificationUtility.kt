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

package com.raywenderlich.android.walkwalk.utility

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.raywenderlich.android.walkwalk.MainActivity
import com.raywenderlich.android.walkwalk.R

private const val NOTIFICATION_CHANNEL_ID = "WalkWalkChannel"
private const val NOTIFICATION_CHANNEL_NAME = "WalkWalkChannelName"
private const val NOTIFICATION_CHANNEL_DESCRIPTION = "WalkWalkChannelDescription"

class NotificationUtility(private val context: Context) {

  companion object {
    const val NOTIFICATION_ID = 101
  }

  private val notificationBuilder: NotificationCompat.Builder by lazy {
    NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setContentTitle(context.getString(R.string.app_name))
        .setContentText(0.toString())
        .setSound(null)
        .setVibrate(longArrayOf(0L))
        .setContentIntent(contentIntent)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
  }

  private val notificationManager by lazy {
    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
  }

  private val contentIntent by lazy {
    val intentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    } else {
      PendingIntent.FLAG_UPDATE_CURRENT
    }

    PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        intentFlags
    )
  }

  fun getNotification(): Notification {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      notificationManager.createNotificationChannel(buildNotificationChannel())
    }

    return notificationBuilder.build()
  }

  fun updateNotification(notificationText: String? = null) {
    notificationText?.let { notificationBuilder.setContentText(it) }
    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun buildNotificationChannel() =
      NotificationChannel(
          NOTIFICATION_CHANNEL_ID,
          NOTIFICATION_CHANNEL_NAME,
          NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        enableVibration(false)
        setSound(null, null)
        enableLights(false)
        vibrationPattern = longArrayOf(0L)
        description = NOTIFICATION_CHANNEL_DESCRIPTION
        setShowBadge(false)
      }
}
