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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import id.ac.pnm.edushare.data.CommentModel

class CommentActivity : AppCompatActivity() {

    private lateinit var tvPostTitle: TextView
    private lateinit var tvPostMeta: TextView
    private lateinit var tvPostContent: TextView
    private lateinit var tvCommentCount: TextView
    private lateinit var tvMapel: TextView
    private lateinit var ivBack: ImageView
    private lateinit var ivThumbnail: ImageView
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
        tvMapel = findViewById(R.id.tvMapel)
        ivBack = findViewById(R.id.ivBack)
        ivThumbnail = findViewById(R.id.ivThumbnail)
        recyclerViewComments = findViewById(R.id.recyclerViewComments)
        etAddComment = findViewById(R.id.etAddComment)
        ivSendComment = findViewById(R.id.ivSendComment)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance("https://edushare-8-trpl-a-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Comments")
        userDatabase = FirebaseDatabase.getInstance("https://edushare-8-trpl-a-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users")

        // Ambil data dari Intent
        materiId = intent.getStringExtra("EXTRA_ID") ?: ""
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Materi"
        val content = intent.getStringExtra("EXTRA_DESC") ?: ""
        val author = intent.getStringExtra("EXTRA_AUTHOR") ?: "Unknown"
        val category = intent.getStringExtra("EXTRA_CATEGORY") ?: ""
        val imageUrl = intent.getStringExtra("EXTRA_IMAGE_URL") ?: ""

        tvMapel.text = category
        tvPostTitle.text = title
        tvPostContent.text = content
        tvPostMeta.text = "Diposting oleh $author"

        if (imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .into(ivThumbnail)
        } else {
            ivThumbnail.visibility = android.view.View.GONE
        }

        commentAdapter = CommentAdapter()
        recyclerViewComments.adapter = commentAdapter
        recyclerViewComments.layoutManager = LinearLayoutManager(this)
        commentAdapter.updateData(commentList)

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
                Toast.makeText(this, "Komentar terkirim", Toast.LENGTH_SHORT).show()
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
                    val map = data.getValue<Map<String, Any>>()
                    if (map != null) {
                        val comment = CommentModel(
                            commentId = map["commentId"] as? String ?: "",
                            materiId = map["materiId"] as? String ?: "",
                            uploaderUid = map["uploaderUid"] as? String ?: "",
                            uploaderName = map["uploaderName"] as? String ?: "",
                            commentText = map["commentText"] as? String ?: "",
                            timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L
                        )
                        commentList.add(comment)
                    }
                }
                commentAdapter.updateData(commentList)
                tvCommentCount.text = "(${commentList.size})"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CommentActivity, "Gagal memuat komentar: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}