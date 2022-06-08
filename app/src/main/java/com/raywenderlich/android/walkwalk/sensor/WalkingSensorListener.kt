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

package com.raywenderlich.android.walkwalk.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

private const val MICROSECONDS_IN_ONE_MINUTE: Long = 60000000
private const val INITIAL_STEP_COUNT_VALUE = 0

class WalkingSensorListener(context: Context, private val onSensorValueChanged: (Int) -> Unit) : SensorEventListener {

  private var stepsCount = INITIAL_STEP_COUNT_VALUE

  private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
  private val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

  fun reRegisterSensor() {
    deregisterSensor()

    onSensorValueChanged(stepsCount)

    stepCounterSensor?.let {
      sensorManager.registerListener(
          this@WalkingSensorListener,
          it,
          SensorManager.SENSOR_DELAY_FASTEST,
          MICROSECONDS_IN_ONE_MINUTE.toInt()
      )
    }
  }

  fun deregisterSensor() {
    try {
      stepsCount = INITIAL_STEP_COUNT_VALUE
      sensorManager.unregisterListener(this)
    } catch (e: Exception) {
      println(e.printStackTrace())
    }
  }

  override fun onSensorChanged(event: SensorEvent?) {
    event ?: return

    event.values.firstOrNull()?.let {
      val currentValue = stepsCount

      if (currentValue == INITIAL_STEP_COUNT_VALUE) {
        stepsCount = it.toInt()
        onSensorValueChanged(currentValue)
      } else {
        val difference = it.toInt() - currentValue
        onSensorValueChanged(difference)
      }
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    // Not needed for our use case
  }
}
