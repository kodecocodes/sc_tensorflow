/*
 * Copyright (c) 2018 Razeware LLC
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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package com.raywenderlich.android.tflclassifier

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileNotFoundException
import java.io.IOException


class MainActivity : AppCompatActivity() {

  internal val lock = Any()
  internal var runClassifier = false

  internal lateinit var backgroundThread: HandlerThread
  internal lateinit var backgroundHandler: Handler

  private lateinit var classifier: ImageClassifier

  private val periodicClassify = object : Runnable {
    override fun run() {
      synchronized(lock) {
        if (runClassifier) {
          classifyImage()
        }
      }
      backgroundHandler.post(this)
    }
  }

  companion object {
    private const val TAG = "MainActivity"

    private const val RESULT_PICK_IMAGE = 1

    internal const val HANDLE_THREAD_NAME = "HANDLE_THREAD_NAME"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    chooseButton.setOnClickListener {
      val photoPickerIntent = Intent(Intent.ACTION_PICK)
      photoPickerIntent.type = "image/*"
      startActivityForResult(photoPickerIntent, RESULT_PICK_IMAGE)
    }

    try {
      classifier = ImageClassifierQuantizedMobileNet(this)
    } catch (e: IOException) {
      Log.e(TAG, "Failed to initialize an image classifier.")
    }

    startBackgroundThread()
  }

  override fun onDestroy() {
    stopBackgroundThread()
    classifier.close()
    super.onDestroy()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (resultCode == Activity.RESULT_OK) {
      try {
        val imageUri = data?.data
        val imageStream = contentResolver.openInputStream(imageUri)
        val selectedImage = BitmapFactory.decodeStream(imageStream)
        imageView.setImageBitmap(selectedImage)
        backgroundHandler.post(periodicClassify)
      } catch (e: FileNotFoundException) {
        e.printStackTrace()
        Toast.makeText(this, getString(R.string.error_picking), Toast.LENGTH_SHORT).show()
      }

    } else {
      Toast.makeText(this, getString(R.string.no_pick), Toast.LENGTH_SHORT).show()
    }
  }

  private fun classifyImage() {
    val unscaledBitmap = (imageView.drawable as BitmapDrawable).bitmap
    val bitmap = Bitmap.createScaledBitmap(unscaledBitmap, classifier.imageSizeX, classifier.imageSizeY, false)
    val textToShow = classifier.classifyFrame(bitmap)
    bitmap.recycle()
    runOnUiThread { resultTextView.text = textToShow }
  }
}
