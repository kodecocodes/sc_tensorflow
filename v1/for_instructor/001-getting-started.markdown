# Screencast Metadata

## Screencast Title

Getting Started with TensorFlow on Android

## Screencast Description

See how to make use of the TensorFlow Lite library to perform image classification on photos taken with your device camera.

## Language, Editor and Platform versions used in this screencast:

- **Language:** Kotlin 1.2
- **Platform:** Android 5.0
- **Editor**: Android Studio 3



Discuss machine learning and applications. 

Classification and regression.

Discuss deep learning

Use of neural networks with hidden layers of neurons between the input and output

Having > 3 layers qualifies the network as "deep". Some networks have hundreds or even thousands of layers. Classic deep learning models have on the order of 10 layers or less.

TensorFlow

Define tensor

Model training is typically done on desktop or server machines with one or more GPUs

Can use the resulting models on mobile devices to make predictions

TensorFlow Lite introduced in Nov 2017. Still in development.

We'll use TensorFlow Lite to perform image classification in a demo app that let's a user pick an image from their photos. We'll use an open source  MobileNet image classification model from Google, and some Google demo code to load and use the model. We'll cover how to create an image classifier and use it in your app.



## Scripted Demo

Make sure NDK is installed.

Show where to download model file from. SHow labels in Android Studio

Open initial project.

Build and run on emulator. Mention needing to run final code on device.

Show tensor flow lite dependency in 

Show model in the assets folder.

Show ImageClassifier and ImageClassifierQuantizedMobileNet java files. Show tflite in the first and modelpath, label path and runInference in the second.

Show threading calls in onCreate and MainActivityX

Create a property for the ImageClassifier:

```kotlin
private lateinit var classifier: ImageClassifier
```

Create on instance of the classifier in onCreate before call to startBackgroundThread:

```kotlin
    try {
      classifier = ImageClassifierQuantizedMobileNet(this)
    } catch (e: IOException) {
      Log.e(TAG, "Failed to initialize an image classifier.")
    }
```

Add to onDestroy after call to stopBackgroundThread

```kotlin
classifier.close()
```

Add a function to take image in the imageview as a bitmap, resize it to the size needed for the classifier, and pass it to the classifier.

```kotlin
  private fun classifyImage() {
    val unscaledBitmap = (imageView.drawable as BitmapDrawable).bitmap
    val bitmap = Bitmap.createScaledBitmap(unscaledBitmap, classifier.imageSizeX, classifier.imageSizeY, false)
    val textToShow = classifier.classifyFrame(bitmap)
    bitmap.recycle()
    runOnUiThread { resultTextView.text = textToShow }
  }
```

The classifier returns the text to show in the TextView, which you update on the UI thread.

Create a runnable that will classify the image and then post itself to the background handler to be run again:

```kotlin
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
```

Finally, tell the handler to start the periodic classification after setting the image on the ImageView in onActivityResult:

```kotlin
backgroundHandler.post(periodicClassify)
```

Show video of app running on a device.

