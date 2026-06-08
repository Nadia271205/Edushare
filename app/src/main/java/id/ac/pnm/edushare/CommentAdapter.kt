package id.ac.pnm.edushare

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.ac.pnm.edushare.data.CommentModel

class CommentAdapter : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private var commentList = mutableListOf<CommentModel>()

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCommentAuthor: TextView = itemView.findViewById(R.id.tvCommentAuthor)
        val tvCommentTime: TextView = itemView.findViewById(R.id.tvCommentTime)
        val tvCommentText: TextView = itemView.findViewById(R.id.tvCommentText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.tvCommentAuthor.text = comment.uploaderName
        holder.tvCommentText.text = comment.commentText
        val timeAgo = DateUtils.getRelativeTimeSpanString(
            comment.timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
        holder.tvCommentTime.text = timeAgo.toString()
    }

    override fun getItemCount(): Int = commentList.size

    fun updateData(newList: List<CommentModel>) {
        commentList = newList.toMutableList()
        notifyDataSetChanged()
    }
}