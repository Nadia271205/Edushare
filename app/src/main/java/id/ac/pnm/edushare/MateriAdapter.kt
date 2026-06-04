package id.ac.pnm.edushare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.ac.pnm.edushare.data.TugasModel

class MateriAdapter(
    private val materiList: MutableList<TugasModel>,
    private val onItemClick: (TugasModel) -> Unit,
    private val onSaveClick: (TugasModel) -> Unit
): RecyclerView.Adapter<MateriAdapter.MateriViewHolder>() {

    inner class MateriViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val ivThumbnail = itemView.findViewById<ImageView>(R.id.ivThumbnail)
        val tvMapel = itemView.findViewById<TextView>(R.id.tvMapel)
        val tvJudul = itemView.findViewById<TextView>(R.id.tvJudul)
        val tvDeskripsi = itemView.findViewById<TextView>(R.id.tvDeskripsi)
        val tvAuthor = itemView.findViewById<TextView>(R.id.tvAuthor)
        val btnSave = itemView.findViewById<ImageButton>(R.id.btnSave)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MateriViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_materi,
                parent,
                false
            )
        return MateriViewHolder(view)
    }

    override fun onBindViewHolder(holder: MateriAdapter.MateriViewHolder, position: Int) {
        val materi = materiList[position]

        holder.tvMapel.text = materi.category
        holder.tvJudul.text = materi.title
        holder.tvDeskripsi.text = materi.description
        holder.tvAuthor.text = materi.uploaderName

        Glide.with(holder.itemView.context)
            .load(materi.fileUrl)
            .placeholder(R.drawable.image_placeholder)
            .error(R.drawable.image_placeholder)
            .into(holder.ivThumbnail)

        holder.itemView.setOnClickListener {
            onItemClick(materi)
        }

        holder.btnSave.setOnClickListener {
            onSaveClick(materi)
        }
    }

    override fun getItemCount(): Int {
        return materiList.size
    }

    fun updateData(newList: List<TugasModel>) {

        materiList.clear()
        materiList.addAll(newList)

        notifyDataSetChanged()
    }

}