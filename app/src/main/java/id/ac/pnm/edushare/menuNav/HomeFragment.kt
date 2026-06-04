package id.ac.pnm.edushare.menuNav

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import id.ac.pnm.edushare.MateriAdapter
import id.ac.pnm.edushare.R
import id.ac.pnm.edushare.UploadActivity
import id.ac.pnm.edushare.data.TugasModel

class HomeFragment : Fragment() {

    private lateinit var rvMateri: RecyclerView
    private lateinit var adapter: MateriAdapter

    private val materiList = mutableListOf< TugasModel>()

    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvMateri = view.findViewById(R.id.rvMateri)
        val fabTambah = view.findViewById<FloatingActionButton>(R.id.fab_tambah)

        adapter = MateriAdapter(
            materiList,

            onItemClick = { materi ->

            },
            onSaveClick = { materi ->

            },
        )

        rvMateri.layoutManager = LinearLayoutManager(requireContext())

        rvMateri.adapter = adapter

        database = FirebaseDatabase.getInstance().getReference("Tugas")

        loadMateri()

        fabTambah.setOnClickListener {
            val intent = Intent(requireActivity(), UploadActivity::class.java )
            startActivity(intent)
        }
    }

    private fun loadMateri() {

        database.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                materiList.clear()

                for (data in snapshot.children) {
                    val materi = data.getValue(TugasModel::class.java)

                    if (materi != null) {
                        materiList.add(materi)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

}