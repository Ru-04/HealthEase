package com.xyz.healthease

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.xyz.healthease.api.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class camera : AppCompatActivity() {

    private lateinit var cameraImage: ImageView
    private lateinit var captureImgBtn: Button
    private lateinit var resultText: TextView
    private lateinit var processBtn: Button
    private lateinit var classifiedResult: TextView
    private lateinit var upload: Button

    private var currentPhotoPath: String? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

    private lateinit var interpreter: Interpreter
    private lateinit var vocab: Map<String, Int>
    private lateinit var labelMapping: Map<Int, String>

    private val apiService: ApiService by lazy { ApiClient.getApiService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        cameraImage = findViewById(R.id.cameraImage)
        captureImgBtn = findViewById(R.id.captureImgBtn)
        resultText = findViewById(R.id.resultText)
        processBtn = findViewById(R.id.processBtn)
        classifiedResult = findViewById(R.id.classifiedResult)
        upload = findViewById(R.id.upload2)

        upload.visibility = View.VISIBLE

        // Initialize model and vocab
        val modelPath = "model_compatible.tflite"
        interpreter = Interpreter(loadModel(this, modelPath))
        loadWordIndex()
        loadLabelMap()
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

        upload.setOnClickListener {
            currentPhotoPath?.let { path ->
                val file = File(path)
                val patientId = sharedPreferences.getString("PATIENT_ID", null)
                if (patientId != null) {
                    uploadImage(file, patientId)
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
            Toast.makeText(this, "Text recognized successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to recognize text: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun classifyText(text: String) {
        val input = preprocessText(text, maxLength = 100)
        val output = Array(1) { FloatArray(labelMapping.size) }
        interpreter.run(arrayOf(input), output)
        val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        val category = labelMapping[maxIndex] ?: "Unknown"
        classifiedResult.text = "Classification Result:\n$category"
    }

    private fun uploadImage(file: File, patientId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val filePart = MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    file.asRequestBody("image/*".toMediaTypeOrNull())
                )
                val patientIdPart = patientId.toRequestBody("text/plain".toMediaTypeOrNull())
                val response = ApiClient.getApiService().uploadImage(filePart, patientIdPart)
                runOnUiThread {
                    if (response.containsKey("message")) {
                        Toast.makeText(this@camera, response["message"].toString(), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@camera, "Unexpected response: $response", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@camera, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun preprocessText(text: String, maxLength: Int = 100): FloatArray {
        val tokens = text.lowercase().split("\\s+".toRegex())
        val indices = tokens.map { vocab[it] ?: 1 }
        val paddedIndices = if (indices.size >= maxLength) {
            indices.take(maxLength)
        } else {
            indices + List(maxLength - indices.size) { 0 }
        }
        return paddedIndices.map { it.toFloat() }.toFloatArray()
    }

    private fun loadModel(context: Context, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelPath)
        FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }
    }

    private fun loadWordIndex() {
        val wordIndexJson = assets.open("word_index (1).json").bufferedReader().use { it.readText() }
        vocab = Gson().fromJson(wordIndexJson, object : TypeToken<Map<String, Int>>() {}.type)
    }

    private fun loadLabelMap() {
        val labelMapJson = assets.open("label_map (1).json").bufferedReader().use { it.readText() }
        labelMapping = Gson().fromJson<Map<String, String>>(labelMapJson, object : TypeToken<Map<String, String>>() {}.type).mapKeys { it.key.toInt() }
    }

    override fun onDestroy() {
        super.onDestroy()
        interpreter.close()
    }
}
