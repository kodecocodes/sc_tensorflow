# Screencast Metadata

## Screencast Title

Getting Started with TensorFlow on Android

## Screencast Description

See how to make use of the TensorFlow Lite library to perform image classification on photos taken with your device camera.

## Language, Editor and Platform versions used in this screencast:

- **Language:** Kotlin 1.2
- **Platform:** Android 5.0
- **Editor**: Android Studio 3


Machine learning has seen a massive increase in interest and application in recent years, thanks in large measure to advances made in the the subfield called Deep Learning.

[Slide 1 - Machine Learning]

Machine learning at a basic level consists of a model that takes inputs and maps to outputs, based on some type of learning that can be done within the model Two of the main applications of machine learning are classification and regression. In classification, a machine learning model is trained to pick from a small number of discrete outputs for a given input. In regression, the model is trained to determine an output from a continuum of values. 

[Slide 2 - Deep Learning]

In deep learning, the machine learning model takes the form of a _neural network_, which is a network of nodes called _neurons_ that interact in a way analogous to the neurons in the brain. Neural networks are not actually themselves models of the brain, however. The network takes the form of a number of a layers, from the input layer, through a certain number of layers called _hidden_ layers, to the output layer, where classification of regression are determined.

In a general sense, having greater than about 3 hidden layers qualifies the network as "deep". Some networks have hundreds or even thousands of layers. Moany of the canonical deep learning models that have been developed have on the order of 10 layers or less.

[Slide 3 - Applications]

Examples of deep learning applications include computer vision, stock predictions

[Slide 4 - TensorFlow]

TensorFlow is a machine learning and deep learning library from Google that lets you create, train, and use deep learning models. The tensor in the name is a term from mathematics and physics, with a tensor being a generalization to multiple dimensions of the vectors and matrices you learned about in your physics courses. Vectors are 1D tensors, matrices are 2D tensors, and general tensors have 3 or more dimensions. Tensors also find heavy usage in Einstein's general relativity.

[Slide 5 - TensorFlow Lite]

Deep learning model training is typically done on desktop or server machines with one or more GPUs. Google has even created proprietary hardware called TPUs to speed up the calculations done with TensorFlow. For mobile devices, Google has released a library named TensorFlow Lite that allow you to use trained deep learning models to make predictions on mobile devices.

We'll use TensorFlow Lite in this screencast to perform image classification in a demo app that let's a user pick an image from their photos. We'll use an open source  MobileNet image classification model from Google, and some Google demo code to load and use the model. We'll cover how to create an image classifier and use it in your app.

## Scripted Demo

First make sure we have the Android NDK is installed, since it's needed to work with TensorFlow Lite.

We can download a pre-trained model for image classification from the TensorFlow site. The model we'll use is called MobileNet and consists of 1001 different classifications of images. The classifications are called labels in machine learning.

We have an starter project that let's you pick a photo from your photo app and show it in an image view.

We can build and run the starter project on an Android emulator. We'll have to run the final project on an Android device in order to use TensorFlow Lite.

The starter project has the TensorFlow Lite dependecy in the app build.gradle file. We leave the version as + in order to use the latest version of the library.

The starter project also has the trained model file that we downloaded and its label text file in the app assets folder.

There are two Java classes in the project that we copied from the TensorFlow Lite demo app from Google.

ImageClassifier is an abstract base class that has a tflite property that connects the app to TensorFlow Lite. There is also a classifyFrame method that takes a bitmap parameter and which calls an abstract method runInference that must be overriden in subclasses.

The concrete subclass ImageClassifierQuantizedMobileNet use the downloaded MobileNet model file in the assets folder, and has an override of runInference that uses the tflite property to classify the bitmap.

We have some Kotlin extensions for MainActivity in MainActivityX.kt that allow us to use a background thread to do the classification.

We also have a handler property in MainActivity that we use to interact with the background thread.

Now we'll get started using the trained model to perform classification.

We declare a property for the ImageClassifier in MainActivity:

```kotlin
private lateinit var classifier: ImageClassifier
```

Then we create an instance of the classifier in onCreate before call to startBackgroundThread:

```kotlin
    try {
      classifier = ImageClassifierQuantizedMobileNet(this)
    } catch (e: IOException) {
      Log.e(TAG, "Failed to initialize an image classifier.")
    }
```

We close the classifier in onDestroy after the call to stopBackgroundThread:

```kotlin
classifier.close()
```

We add a private function to take the image from the imageview as a bitmap, resize it to the size needed for the classifier, and pass it to the classifier for classification. The classifier returns the text to show in the TextView in our layout, which we update on the UI thread.

```kotlin
  private fun classifyImage() {
    val unscaledBitmap = (imageView.drawable as BitmapDrawable).bitmap
    val bitmap = Bitmap.createScaledBitmap(unscaledBitmap, classifier.imageSizeX, classifier.imageSizeY, false)
    val textToShow = classifier.classifyFrame(bitmap)
    bitmap.recycle()
    runOnUiThread { resultTextView.text = textToShow }
  }
```

We create a runnable that will classify the image and then post itself to the background handler to be run again, so the classification keeps running periodically:

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

Finally, we tell the handler to start the periodic classification after setting the image on the ImageView in onActivityResult:

```kotlin
backgroundHandler.post(periodicClassify)
```

With everything in place, we can run the app on a device. Here we're seeing a screen recording from a device running the final app. We pick images from our photos and each gets classified successfully in the app. The classifier is not 100% accurate, so you'll likely see some misclassifications if you run the final app on your device.

At this point, you should have a good feel for how to use a trained TensorFlow Lite model in an app. We've covered loading a classifier into your app and using the classifier to make predictions.

You can learn more about TensorFlow and TensorFlow Life at the official site tensorflow.org, and stay tuned for many more TensorFlow screencasts and tutorials at raywenderlich.com. Thanks for watching!