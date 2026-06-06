package id.ac.pnm.edushare

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import id.ac.pnm.edushare.data.CommentModel

class CommentActivity : AppCompatActivity() {

    private lateinit var tvPostTitle: TextView
    private lateinit var tvPostMeta: TextView
    private lateinit var tvPostContent: TextView
    private lateinit var tvCommentCount: TextView
    private lateinit var ivBack: ImageView
    private lateinit var recyclerViewComments: RecyclerView
    private lateinit var etAddComment: EditText
    private lateinit var ivSendComment: ImageView

    private lateinit var commentAdapter: CommentAdapter
    private var commentList = ArrayList<CommentModel>()

    private lateinit var databaseReference: DatabaseReference
    private lateinit var userDatabase: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var materiId: String = ""
    private var currentUserName: String = "Siswa"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comment)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvPostTitle = findViewById(R.id.tvPostTitle)
        tvPostMeta = findViewById(R.id.tvPostMeta)
        tvPostContent = findViewById(R.id.tvPostContent)
        tvCommentCount = findViewById(R.id.tvCommentCount)
        ivBack = findViewById(R.id.ivBack)
        recyclerViewComments = findViewById(R.id.recyclerViewComments)
        etAddComment = findViewById(R.id.etAddComment)
        ivSendComment = findViewById(R.id.ivSendComment)

        auth = FirebaseAuth.getInstance()
        databaseReference = Firebase.database.getReference("Comments")
        userDatabase = Firebase.database.getReference("Users")

        materiId = intent.getStringExtra("EXTRA_ID") ?: ""
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Materi"
        val content = intent.getStringExtra("EXTRA_DESC") ?: ""
        val author = intent.getStringExtra("EXTRA_AUTHOR") ?: "Unknown"

        tvPostTitle.text = title
        tvPostContent.text = content
        tvPostMeta.text = "Diposting oleh $author"

        commentAdapter = CommentAdapter(commentList)
        recyclerViewComments.adapter = commentAdapter

        ivBack.setOnClickListener { finish() }

        ivSendComment.setOnClickListener {
            postComment()
        }

        getCurrentUserName()
        loadComments()
    }

    private fun getCurrentUserName() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            userDatabase.child(uid).get().addOnSuccessListener { snapshot ->
                currentUserName = snapshot.child("username").getValue(String::class.java) ?: "Siswa"
            }
        }
    }

    private fun postComment() {
        val commentText = etAddComment.text.toString().trim()
        if (commentText.isEmpty()) {
            Toast.makeText(this, "Komentar tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val commentId = databaseReference.child(materiId).push().key ?: return

        val commentModel = CommentModel(
            commentId = commentId,
            materiId = materiId,
            uploaderUid = uid,
            uploaderName = currentUserName,
            commentText = commentText,
            timestamp = System.currentTimeMillis()
        )

        databaseReference.child(materiId).child(commentId).setValue(commentModel)
            .addOnSuccessListener {
                etAddComment.setText("")
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengirim komentar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadComments() {
        if (materiId.isEmpty()) return

        databaseReference.child(materiId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentList.clear()
                for (data in snapshot.children) {
                    val comment = data.getValue<CommentModel>()
                    if (comment != null) {
                        commentList.add(comment)
                    }
                }
                commentAdapter.updateData(commentList)
                tvCommentCount.text = "(${commentList.size})"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CommentActivity, "Gagal memuat komentar", Toast.LENGTH_SHORT).show()
            }
        })
    }
}