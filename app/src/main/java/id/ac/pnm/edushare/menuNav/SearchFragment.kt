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
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import id.ac.pnm.edushare.MateriAdapter
import id.ac.pnm.edushare.R
import id.ac.pnm.edushare.data.MateriModel
import id.ac.pnm.edushare.data.local.AppDatabase
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var searchView: android.widget.SearchView
    private lateinit var recyclerViewCatalog: androidx.recyclerview.widget.RecyclerView
    private lateinit var materiAdapter: MateriAdapter
    private var materiList = ArrayList<MateriModel>()
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseReference = Firebase.database.getReference("Tugas")

        searchView = view.findViewById(R.id.searchView)
        recyclerViewCatalog = view.findViewById(R.id.recyclerViewCatalog)

        materiAdapter = MateriAdapter(
            materiList,
            { materi ->

            },
            { materi ->
                val db = AppDatabase.getDatabase(requireContext())
                viewLifecycleOwner.lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    db.materiDao().insert(materi)
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
        for (item in materiList) {
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
                materiList.clear()
                for (dataSnapshot in snapshot.children) {
                    val materi = dataSnapshot.getValue<MateriModel>()
                    if (materi != null) {
                        materiList.add(materi)
                    }
                }
                materiAdapter.updateData(materiList)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}