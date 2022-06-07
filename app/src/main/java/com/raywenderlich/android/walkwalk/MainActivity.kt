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

package com.raywenderlich.android.walkwalk

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.raywenderlich.android.walkwalk.databinding.ActivityMainBinding
import com.raywenderlich.android.walkwalk.service.ForegroundServiceState
import com.raywenderlich.android.walkwalk.service.WalkingService
import com.raywenderlich.android.walkwalk.utility.SharedPreferencesUtility

/**
 * Main Screen
 */
class MainActivity : AppCompatActivity() {

  lateinit var binding: ActivityMainBinding

  private val requestPermissionLauncher = registerForActivityResult(
      ActivityResultContracts.RequestPermission()
  ) { isPermissionGranted ->
    if (isPermissionGranted) {
      startWalkingService(ForegroundServiceState.STARTED.name)
    } else {
      Toast.makeText(this, "We need your permission in order to track your steps! :)", Toast.LENGTH_LONG).show()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.AppTheme)
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.startServiceButton.setOnClickListener {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        checkActivityRecognitionPermission()
      } else {
        startWalkingService(ForegroundServiceState.STARTED.name)
      }
    }

    binding.endServiceButton.setOnClickListener {
      startWalkingService(ForegroundServiceState.STOPPED.name)
    }
  }

  @SuppressLint("InlinedApi")
  private fun checkActivityRecognitionPermission() {
    when (PackageManager.PERMISSION_GRANTED) {
      ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) -> {
        startWalkingService(ForegroundServiceState.STARTED.name)
      }
      else -> {
        requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
      }
    }
  }

  private fun startWalkingService(intentAction: String) {
    if (isServiceStoppedAlready(intentAction)) return

    val intent =  Intent(this, WalkingService::class.java).apply {
      action = intentAction
    }
    ContextCompat.startForegroundService(this, intent)
  }

  private fun isServiceStoppedAlready(intentAction: String) =
    SharedPreferencesUtility.getForegroundServiceState(this) == ForegroundServiceState.STOPPED
        && intentAction == ForegroundServiceState.STOPPED.name
}
