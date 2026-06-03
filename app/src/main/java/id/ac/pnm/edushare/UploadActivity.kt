package id.ac.pnm.edushare

import android.net.Uri
import android.os.Bundle
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
import id.ac.pnm.edushare.data.tugasModel
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

    private val filePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

        if (uri != null) {
            selectedFileUri = uri
            tvFileName.text = "File dipilih"
        }
    }

    private fun getSelectedCategory(): String {

        return when (chipGroup.checkedChipId) {

            R.id.chipMath -> "Matematika"
            R.id.chipPhysics -> "Fisika"
            R.id.chipChemistry -> "Kimia"

            else -> "Lainnya"
        }
    }

    private fun uploadTask() {

        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (title.isEmpty()) {
            etTitle.error = "Judul wajib diisi!"
            return
        }

        if (selectedFileUri == null) {
            Toast.makeText(this, "Pilih file terlebih dahulu!", Toast.LENGTH_SHORT).show()
        }

        val uid = auth.currentUser?.uid ?: return

        val fileName = System.currentTimeMillis().toString()

        val storageRef = storage.reference
            .child("tugas")
            .child("$fileName.jpg")

        storageRef.putFile(selectedFileUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->

                    saveToDatabase(
                        title,
                        description,
                        getSelectedCategory(),
                        downloadUrl.toString(),
                        uid
                    )
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Upload gagal!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToDatabase(title: String, description: String, category: String, fileUrl: String, uid: String) {

        val tugasId = database.child("Tugas").push().key ?: return

        val tugas = tugasModel(
            id = tugasId,
            title = title,
            description = description,
            category = category,
            fileUrl = fileUrl,
            uploaderUid = uid,
            timestamp = System.currentTimeMillis()
        )

        database.child("Tugas")
            .child(tugasId)
            .setValue(tugas)
            .addOnSuccessListener {

                lifecycleScope.launch {
                    EduShareApp.db
                        .tugasDao()
                        .insert(tugas)
                }

                Toast.makeText(this, "Upload berhasil!", Toast.LENGTH_SHORT).show()

                finish()
            }
            .addOnFailureListener {

                Toast.makeText(this, "Gagal menyimpan data!!", Toast.LENGTH_SHORT).show()

            }
    }
}