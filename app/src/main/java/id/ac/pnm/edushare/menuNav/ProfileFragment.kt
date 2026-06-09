package id.ac.pnm.edushare.menuNav

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import id.ac.pnm.edushare.EduShareApp
import id.ac.pnm.edushare.LoginActivity
import id.ac.pnm.edushare.R
import id.ac.pnm.edushare.data.MateriModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvKelas: TextView
    private lateinit var tvSekolah: TextView

    private lateinit var tvUploadCount: TextView
    private lateinit var tvSavedCount: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        tvUsername = view.findViewById<TextView>(R.id.tvUsernameValue)
        tvEmail = view.findViewById<TextView>(R.id.tvEmailValue)
        tvKelas = view.findViewById<TextView>(R.id.tvKelasValue)
        tvSekolah = view.findViewById<TextView>(R.id.tvSekolahValue)

        tvUploadCount = view.findViewById(R.id.tvUploadCount)
        tvSavedCount = view.findViewById(R.id.tvSavedCount)

        loadProfile()
        loadUploadCount()
        loadSavedCount()

        val btnLogout = view.findViewById<MaterialButton>(R.id.bt_logout)

        btnLogout.setOnClickListener {
            auth.signOut()

            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()

        }
    }

    private fun loadProfile() {
        val currentUser = auth.currentUser ?: return
        val uid = currentUser.uid


        database = FirebaseDatabase
            .getInstance("https://edushare-8-trpl-a-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("Users").child(uid)

        database.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {

                    val user = snapshot.getValue(UserModel::class.java)

                    tvUsername.text = user?.username
                    tvEmail.text = user?.email
                    tvKelas.text = user?.kelas
                    tvSekolah.text = user?.sekolah
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Firebase", "Error: ${error.message}")
            }

        })
    }

    private fun loadUploadCount() {
        val uid = auth.currentUser?.uid ?: return

        FirebaseDatabase
            .getInstance("https://edushare-8-trpl-a-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("Tugas")
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    var count = 0

                    for (child in snapshot.children) {
                        val materi = child.getValue(MateriModel::class.java)

                        if (materi?.uploaderUid == uid) {
                            count++
                        }
                    }

                    tvUploadCount.text = count.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", error.message)
                }

            })
    }

    private fun loadSavedCount() {
        CoroutineScope(Dispatchers.IO).launch {

            val totalSaved = EduShareApp.db
                .materiDao()
                .getAll()
                .size

            withContext(Dispatchers.Main) {
                tvSavedCount.text = totalSaved.toString()
            }
        }
    }
}






