package id.ac.pnm.edushare.menuNav

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.ac.pnm.edushare.MateriAdapter
import id.ac.pnm.edushare.R
import id.ac.pnm.edushare.data.MateriModel
import id.ac.pnm.edushare.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SaveFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SaveFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recyclerViewSaved: RecyclerView
    private lateinit var materiAdapter: MateriAdapter
    private var savedList = ArrayList<MateriModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_save, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewSaved = view.findViewById(R.id.recyclerViewSaved)

        materiAdapter = MateriAdapter(
            materiList = savedList,
            onItemClick = { materi ->

            },
            onSaveClick = { materi ->
                deleteSavedMateri(materi)
            }
        )

        recyclerViewSaved.adapter = materiAdapter

        loadSavedData()
    }

    private fun loadSavedData() {
        val db = AppDatabase.getDatabase(requireContext())
        CoroutineScope(Dispatchers.IO).launch {
            val data = db.materiDao().getAll()
            withContext(Dispatchers.Main) {
                materiAdapter.updateData(data)
            }
        }
    }

    private fun deleteSavedMateri(materi: MateriModel) {
        val db = AppDatabase.getDatabase(requireContext())
        CoroutineScope(Dispatchers.IO).launch {
            db.materiDao().delete(materi)
            loadSavedData()
        }
    }
}