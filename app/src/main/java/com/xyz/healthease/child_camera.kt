package com.xyz.healthease

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.xyz.healthease.api.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors




class child_camera : AppCompatActivity() {

    private lateinit var cameraImage: ImageView
    private lateinit var captureImgBtn: Button
    private lateinit var resultText: TextView
    private lateinit var processBtn: Button
    private lateinit var classifiedResult: TextView
    private lateinit var upload: Button

    private lateinit var doctorName: TextView
    private lateinit var hospitalName: TextView
    private lateinit var button: Button

    private var currentPhotoPath: String? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

    private lateinit var interpreter: Interpreter
    private lateinit var vocab: Map<String, Int>
    private lateinit var labels: List<String>

    private val apiService: ApiService by lazy { ApiClient.getApiService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_child_camera)

        cameraImage = findViewById(R.id.cameraImage)
        captureImgBtn = findViewById(R.id.captureImgBtn)
        resultText = findViewById(R.id.resultText)
        processBtn = findViewById(R.id.processBtn)
        classifiedResult = findViewById(R.id.classifiedResult)
        upload = findViewById(R.id.upload2)


        button=findViewById(R.id.buttonExtract)
        doctorName=findViewById(R.id.doctorNameText)
        hospitalName=findViewById(R.id.hospitalNameText)

        upload.visibility = View.VISIBLE

        // Initialize model and vocab
        val modelPath = "model_main.tflite"
        interpreter = Interpreter(loadModel(this, modelPath))
        loadVocab()
        loadLabels()
        val sharedPreferences = getSharedPreferences("HealthEasePrefs", MODE_PRIVATE)

        // Permissions and image capture setup
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                captureImage()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                currentPhotoPath?.let { path ->
                    val bitmap = BitmapFactory.decodeFile(path)
                    cameraImage.setImageBitmap(bitmap)
                    recognizeText(bitmap)
                }
            }
        }

        captureImgBtn.setOnClickListener {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }

        processBtn.setOnClickListener {
            val extractedText = resultText.text.toString()
            if (extractedText.isNotBlank()) {
                classifyText(extractedText)
            } else {
                Toast.makeText(this, "No text available for classification", Toast.LENGTH_SHORT).show()
            }
        }

        button.setOnClickListener {
            sendTextToModel(resultText.text.toString())
        }

        upload.setOnClickListener {
            currentPhotoPath?.let { path ->
                val file = File(path)
                val patientId = sharedPreferences.getString("PATIENT_ID", null)
                if (patientId != null) {
                    uploadImageToCloudinary(file)
                    println("file uploaded succesfully")
                } else {
                    Toast.makeText(this, "Patient ID is missing", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "No image to upload", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun captureImage() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(this, "Error occurred while creating the file", Toast.LENGTH_SHORT).show()
            null
        }
        photoFile?.also {
            val photoUri: Uri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", it)
            takePictureLauncher.launch(photoUri)
        }
    }

    private fun recognizeText(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image).addOnSuccessListener { ocrText ->
            resultText.text = ocrText.text
            resultText.movementMethod = ScrollingMovementMethod()

            Toast.makeText(this, "Text recognized successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to recognize text: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun classifyText(text: String) {
        val inputVector = preprocessText(text)
        val inputBuffer = ByteBuffer.allocateDirect(inputVector.size * 4).order(ByteOrder.nativeOrder())
        inputVector.forEach { inputBuffer.putFloat(it) }

        val outputBuffer = ByteBuffer.allocateDirect(labels.size * 4).order(ByteOrder.nativeOrder())
        interpreter.run(inputBuffer, outputBuffer)

        val outputArray = FloatArray(labels.size)
        outputBuffer.rewind()
        for (i in outputArray.indices) {
            outputArray[i] = outputBuffer.float
        }

        val maxIndex = outputArray.indices.maxByOrNull { outputArray[it] } ?: -1
        val predictedLabel = if (maxIndex >= 0) labels[maxIndex] else "Unknown"
        classifiedResult.text = "Classification Result: \n$predictedLabel"
    }


    private fun preprocessText(text: String): FloatArray {
        val tokens = text.lowercase().split(Regex("\\s+")).map { it.replace(Regex("[^a-z0-9]"), "") }
        val featureVector = FloatArray(1000) { 0f }

        for (token in tokens) {
            val index = vocab[token]
            if (index != null && index < 1000) {
                featureVector[index] += 1f
            }
        }
        return featureVector
    }

    private fun loadModel(context: Context, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelPath)
        FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
            val fileChannel = inputStream.channel
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
        }
    }

    private fun loadVocab() {
        val vocabJson = assets.open("tfidf_vocab.json").bufferedReader().use { it.readText() }

        // Parse the entire JSON object
        val fullJson: Map<String, Any> = Gson().fromJson(vocabJson, object : TypeToken<Map<String, Any>>() {}.type)

        // Extract "vocab" safely and convert to Map<String, Int>
        val extractedVocab = (fullJson["vocab"] as? Map<String, Number>) ?: emptyMap()
        vocab = extractedVocab.mapValues { it.value.toInt() }  // Ensure conversion to Int

        // Extract "idf" safely (if needed) and convert to List<Float>
        val extractedIdf = (fullJson["idf"] as? List<Number>)?.map { it.toFloat() } ?: emptyList()
    }

    private fun loadLabels() {
        val labelsJson = assets.open("labels.json").bufferedReader().use { it.readText() }
        labels = Gson().fromJson(labelsJson, object : TypeToken<List<String>>() {}.type)
    }

    private fun sendTextToModel(text: String) {
        if (text.isEmpty()) {
            Toast.makeText(this, "No extracted text available", Toast.LENGTH_SHORT).show()
            return
        }
        val request = TextRequest(text)
        RetrofitClient.instance.extractTextDetails(request)
            .enqueue(object : Callback<TextExtractionResponse> {
                override fun onResponse(
                    call: Call<TextExtractionResponse>,
                    response: Response<TextExtractionResponse>
                ) {
                    if (response.isSuccessful) {
                        val doctorNames = response.body()?.doctor_names?.joinToString(", ") ?: "Not Found"
                        val hospitalNames = response.body()?.hospital_names?.joinToString(", ") ?: "Not Found"
                        doctorName.text =  "Doctor: $doctorNames"
                        hospitalName.text = "Hospital: $hospitalNames"
                    } else {
                        doctorName.text = "Error: ${response.message()}"
                        hospitalName.text = "Error: ${response.message()}"
                    }
                }


                override fun onFailure(call: Call<TextExtractionResponse>, t: Throwable) {
                    doctorName.text = "Failed: ${t.message}"
                    hospitalName.text = "Failed: ${t.message}"
                }
            })
    }

    private fun uploadImageToCloudinary(imageFile: File) {
        val cloudinary = Cloudinary(
            ObjectUtils.asMap(
                "cloud_name", "dkrspzrhj",
                "api_key", "263152711571172",
                "api_secret", "hbn_y81gadRKr9LnOjmjhiHVieY"
            )
        )

        Executors.newSingleThreadExecutor().execute {
            try {
                val result = cloudinary.uploader().upload(imageFile, ObjectUtils.emptyMap())
                runOnUiThread {
                    // Debugging - Print the whole result to see its structure
                    println("Cloudinary Response: $result")
                    Toast.makeText(this@child_camera, "Response: $result", Toast.LENGTH_LONG).show()

                    // Check if secure_url exists and retrieve it
                    val imageUrl = result["secure_url"] as? String
                    val publicId = result["public_id"] as? String

                    if (imageUrl != null && publicId != null) {
                        Toast.makeText(this@child_camera, "Image Uploaded: $imageUrl", Toast.LENGTH_SHORT).show()
                        Glide.with(this@child_camera).load(imageUrl).into(cameraImage)

                        // Get Patient ID
                        val sharedPreferences = getSharedPreferences("HealthEasePrefs", MODE_PRIVATE)
                        val patientId = sharedPreferences.getString("PATIENT_ID", null)

                        // Get Child ID
                        val childPrefs = getSharedPreferences("ChildPrefs", MODE_PRIVATE)
                        val childId = childPrefs.getString("CHILD_ID", null)

                        if (patientId != null && childId != null) {
                            // Send to MongoDB
                            uploadReportToMongoDB(patientId, childId, publicId, imageUrl)
                        } else {
                            Toast.makeText(
                                this@child_camera,
                                "Patient ID or Child ID is missing",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@child_camera,
                            "Image URL or Public ID not found in response",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@child_camera, "Upload Failed: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }



    private fun uploadReportToMongoDB(patientId: String, childId: String, publicId: String, imageUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create request body using ChildReportRequest
                val reportRequest = ApiService.ChildReportRequest(
                    patientId = patientId,
                    childId = childId,  // Required for child reports
                    publicId = publicId,
                    imageUrl = imageUrl,
                    reportCategory = classifiedResult.text.toString().replace("Classification Result: \n", ""),
                    hospitalName = hospitalName.text.toString().replace("Hospital: ", ""),
                    doctorName = doctorName.text.toString().replace("Doctor: ", "")
                )

                // Call the new API for uploading child reports
                val response = apiService.uploadChildReport(reportRequest)

                runOnUiThread {
                    if (response.isSuccessful) {
                        val message = response.body()?.message ?: "Child report uploaded successfully"
                        Toast.makeText(this@child_camera, message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@child_camera, "Upload failed: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@child_camera, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        interpreter.close()
    }

}