package id.ac.pnm.edushare.data

data class CommentModel(
    var commentId: String = "",
    var materiId: String = "",
    var uploaderUid: String = "",
    var uploaderName: String = "",
    var commentText: String = "",
    var timestamp: Long = 0L
)