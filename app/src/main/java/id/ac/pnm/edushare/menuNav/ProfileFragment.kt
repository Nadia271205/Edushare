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
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import id.ac.pnm.edushare.LoginActivity
import id.ac.pnm.edushare.R

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvKelas: TextView
    private lateinit var tvSekolah: TextView


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

        loadProfile()


        val btnLogout = view.findViewById<Button>(R.id.bt_logout)

        btnLogout.setOnClickListener {
            auth.signOut()


            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()

        }
    }

    private fun loadProfile() {
        val currentUser = auth.currentUser ?: return
        val uid = currentUser.uid


        database = FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(uid)

        database.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {

                    val user = snapshot.getValue(userModel::class.java)

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
}






