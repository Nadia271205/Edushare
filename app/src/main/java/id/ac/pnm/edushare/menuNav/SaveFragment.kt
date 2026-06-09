package id.ac.pnm.edushare.menuNav

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.ac.pnm.edushare.CommentActivity
import id.ac.pnm.edushare.EduShareApp
import id.ac.pnm.edushare.MateriAdapter
import id.ac.pnm.edushare.R
import id.ac.pnm.edushare.data.MateriModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SaveFragment : Fragment() {

    private lateinit var recyclerViewSaved: RecyclerView
    private lateinit var materiAdapter: MateriAdapter
    private lateinit var tvSavedCount: TextView
    private var savedList = ArrayList<MateriModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_save, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewSaved = view.findViewById(R.id.recyclerViewSaved)
        recyclerViewSaved.layoutManager = LinearLayoutManager(requireContext())
        tvSavedCount = view.findViewById<TextView>(R.id.tvSavedCount)


        materiAdapter = MateriAdapter(
            savedList,
            { materi ->
                val intent = android.content.Intent(requireContext(), CommentActivity::class.java)
                intent.putExtra("EXTRA_ID", materi.id)
                intent.putExtra("EXTRA_TITLE", materi.title)
                intent.putExtra("EXTRA_DESC", materi.description)
                intent.putExtra("EXTRA_AUTHOR", materi.uploaderName)
                intent.putExtra("EXTRA_IMAGE_URL", materi.fileUrl)
                intent.putExtra("EXTRA_CATEGORY", materi.category)
                startActivity(intent)
            },
            { materi ->
                deleteSavedMateri(materi)
            }
        )

        recyclerViewSaved.adapter = materiAdapter

        loadSavedData()
    }

    private fun loadSavedData() {
        val db = EduShareApp.db
        CoroutineScope(Dispatchers.IO).launch {
            val data = db.materiDao().getAll()
            withContext(Dispatchers.Main) {
                materiAdapter.updateData(data)

                tvSavedCount.text = "${data.size} materi tersimpan"
            }
        }
    }

    private fun deleteSavedMateri(materi: MateriModel) {
        val db = EduShareApp.db
        CoroutineScope(Dispatchers.IO).launch {
            db.materiDao().delete(materi)
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(requireContext(), "Materi dihapus dari simpanan", android.widget.Toast.LENGTH_SHORT).show()
                loadSavedData()
            }
        }
    }
}