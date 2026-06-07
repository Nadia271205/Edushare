package id.ac.pnm.edushare.menuNav

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import id.ac.pnm.edushare.CommentActivity
import id.ac.pnm.edushare.EduShareApp
import id.ac.pnm.edushare.MateriAdapter
import id.ac.pnm.edushare.R
import id.ac.pnm.edushare.data.MateriModel
import id.ac.pnm.edushare.data.local.AppDatabase
import kotlinx.coroutines.launch
import kotlin.jvm.java

class SearchFragment : Fragment() {

    private lateinit var searchView: android.widget.SearchView
    private lateinit var recyclerViewCatalog: androidx.recyclerview.widget.RecyclerView
    private lateinit var materiAdapter: MateriAdapter

    private var originalList = ArrayList<MateriModel>()
    private var displayList = ArrayList<MateriModel>()

    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseReference = FirebaseDatabase.getInstance("https://edushare-8-trpl-a-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Tugas")

        searchView = view.findViewById(R.id.searchView)
        recyclerViewCatalog = view.findViewById(R.id.recyclerViewCatalog)

        materiAdapter = MateriAdapter(
            displayList,
            { materi ->
                val intent = android.content.Intent(requireContext(), CommentActivity::class.java)
                intent.putExtra("EXTRA_ID", materi.id)
                intent.putExtra("EXTRA_TITLE", materi.title)
                intent.putExtra("EXTRA_DESC", materi.description)
                intent.putExtra("EXTRA_AUTHOR", materi.uploaderName)
                startActivity(intent)
            },
            { materi ->
                val db = EduShareApp.db
                viewLifecycleOwner.lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        db.materiDao().insert(materi)
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            android.widget.Toast.makeText(requireContext(), "Materi berhasil disimpan!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        )

        recyclerViewCatalog.adapter = materiAdapter

        loadData()

        searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterData(newText.orEmpty())
                return true
            }
        })
    }

    private fun filterData(text: String) {
        val filteredList = ArrayList<MateriModel>()
        for (item in originalList) {
            if (item.title.lowercase(java.util.Locale.getDefault()).contains(text.lowercase(java.util.Locale.getDefault())) ||
                item.category.lowercase(java.util.Locale.getDefault()).contains(text.lowercase(java.util.Locale.getDefault()))
            ) {
                filteredList.add(item)
            }
        }
        materiAdapter.updateData(filteredList)
    }

    private fun loadData() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                originalList.clear()
                displayList.clear()
                for (dataSnapshot in snapshot.children) {
                    val materi = dataSnapshot.getValue<MateriModel>()
                    if (materi != null) {
                        originalList.add(materi)
                        displayList.add(materi)
                    }
                }
                materiAdapter.updateData(displayList)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}