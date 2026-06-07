package id.ac.pnm.edushare

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import id.ac.pnm.edushare.data.MateriModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UploadActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var chipGroup: ChipGroup
    private lateinit var tvFileName: TextView
    private lateinit var btnPublish: Button

    private var selectedFileUri: Uri? = null

    private val storage = FirebaseStorage.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upload)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        chipGroup = findViewById(R.id.chipGroupCategories)
        tvFileName = findViewById(R.id.tvFileName)
        btnPublish = findViewById(R.id.btnPublish)

        val uploadArea = findViewById<LinearLayout>(R.id.llUploadArea)

        uploadArea.setOnClickListener {
            filePicker.launch("image/*")
        }

        btnPublish.setOnClickListener {
            uploadTask()
        }


    }

    private val filePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

            if (uri != null) {
                selectedFileUri = uri
                tvFileName.text = "File dipilih"
            }
        }

    private fun getSelectedCategory(): String {

        val chipId = chipGroup.checkedChipId

        if (chipId == View.NO_ID) {
            return "Lainnya"
        }

        val chip =findViewById<com.google.android.material.chip.Chip>(chipId)

        return chip.text.toString()
    }

    private fun uploadTask() {

        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (title.isEmpty()) {
            etTitle.error = "Judul wajib diisi!"
            return
        }

        if (description.isEmpty()) {
            etDescription.error = "Deskripsi wajib diisi!"
            return
        }

        val fileUri = selectedFileUri

        if (fileUri == null) {
            Toast.makeText(this, "Pilih file terlebih dahulu!", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = auth.currentUser?.uid ?: return

        btnPublish.isEnabled = false
        btnPublish.text = "Mengunggah..."

        val fileName = System.currentTimeMillis().toString()

        val storageRef = storage.reference
            .child("tugas")
            .child("$fileName.jpg")

        storageRef.putFile(fileUri)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    storageRef.downloadUrl
                        .addOnCompleteListener { urlTask ->
                            if (urlTask.isSuccessful) {
                                val downloadUrl = urlTask.result.toString()

                                val tugasId =
                                    database.child("Tugas").push().key

                                if (tugasId == null) {

                                    btnPublish.isEnabled = true
                                    btnPublish.text = "Upload"

                                    Toast.makeText(
                                        this,
                                        "Gagal membuat ID tugas",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    return@addOnCompleteListener
                                }

                                database.child("Users")
                                    .child(uid)
                                    .get()
                                    .addOnCompleteListener { userTask ->

                                        val username =
                                            if (userTask.isSuccessful && userTask.result.exists()) {
                                                userTask.result.child("username")
                                                    .getValue(String::class.java)
                                                    ?: "Unknown"
                                            } else {
                                                "Unknown"
                                            }

                                        val tugas = MateriModel(
                                            id = tugasId,
                                            title = title,
                                            category = getSelectedCategory(),
                                            description = description,
                                            fileUrl = downloadUrl,
                                            uploaderUid = uid,
                                            uploaderName = username,
                                            timestamp = System.currentTimeMillis()
                                        )

                                        database.child("Tugas")
                                            .child(tugasId)
                                            .setValue(tugas)
                                            .addOnCompleteListener { saveTask ->
                                                if (saveTask.isSuccessful) {


                                                    lifecycleScope.launch(Dispatchers.IO) {
                                                        try {
                                                            EduShareApp.db
                                                                .materiDao()
                                                                .insert(tugas)
                                                        } catch (e: Exception) {
                                                        }
                                                    }

                                                    Toast.makeText(
                                                        this,
                                                        "Upload berhasil!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()

                                                    finish()
                                                } else {
                                                    btnPublish.isEnabled = true
                                                    btnPublish.text = "Upload"

                                                    Toast.makeText(
                                                        this,
                                                        "Gagal menyimpan data ke database",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                    }
                            } else {
                                btnPublish.isEnabled = true
                                btnPublish.text = "Upload"

                                Toast.makeText(
                                    this,
                                    "Gagal mendapatkan URL file",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        }
                } else {
                    btnPublish.isEnabled = true
                    btnPublish.text = "Upload"

                    Toast.makeText(this, "Upload file gagal", Toast.LENGTH_LONG).show()
                }
            }
    }
}