package id.ac.pnm.edushare.menuNav

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import id.ac.pnm.edushare.CommentActivity
import id.ac.pnm.edushare.EduShareApp
import id.ac.pnm.edushare.MateriAdapter
import id.ac.pnm.edushare.R
import id.ac.pnm.edushare.UploadActivity
import id.ac.pnm.edushare.data.MateriModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var rvMateri: RecyclerView
    private lateinit var adapter: MateriAdapter

    private val materiList = mutableListOf<MateriModel>()

    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvMateri = view.findViewById(R.id.rvMateri)
        val fabTambah = view.findViewById<FloatingActionButton>(R.id.fab_tambah)

        val tvGreeting = view.findViewById<TextView>(R.id.tvGreeting)

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .get()
                .addOnSuccessListener { snapshot ->

                    val nama = snapshot.child("username").getValue(String::class.java)

                    tvGreeting.text = "Halo, ${nama ?: "pengguna"}"
                }
                .addOnFailureListener {
                    tvGreeting.text = "Halo, "
                }
        }

        adapter = MateriAdapter(
            materiList,
            onItemClick = { materi ->
                val intent = Intent(requireContext(), CommentActivity::class.java)
                intent.putExtra("EXTRA_ID", materi.id)
                intent.putExtra("EXTRA_TITLE", materi.title)
                intent.putExtra("EXTRA_DESC", materi.description)
                intent.putExtra("EXTRA_AUTHOR", materi.uploaderName)
                intent.putExtra("EXTRA_CATEGORY", materi.category)
                intent.putExtra("EXTRA_IMAGE_URL", materi.fileUrl)
                startActivity(intent)
            },
            onSaveClick = { materi ->
                val db = EduShareApp.db
                viewLifecycleOwner.lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        db.materiDao().insert(materi)
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Materi berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        )

        rvMateri.layoutManager = LinearLayoutManager(requireContext())
        rvMateri.adapter = adapter

        database = FirebaseDatabase.getInstance("https://edushare-8-trpl-a-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Tugas")
        loadMateri()

        fabTambah.setOnClickListener {
            val intent = Intent(requireActivity(), UploadActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadMateri() {

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                materiList.clear()

                for (data in snapshot.children) {
                    val materi = data.getValue(MateriModel::class.java)

                    if (materi != null) {
                        materi.id = data.key ?: ""
                        materiList.add(materi)
                    }
                }

                materiList.sortByDescending { it.timestamp }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }
}