package id.ac.pnm.edushare

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        val logo = findViewById<ImageView>(R.id.ivLogo)
        val title = findViewById<TextView>(R.id.tvSplashTitle)

        val scaleAnim =
            AnimationUtils.loadAnimation(this, R.anim.scale_anim)

        val fadeText =
            AnimationUtils.loadAnimation(this, R.anim.fade_text)

        logo.startAnimation(scaleAnim)
        title.startAnimation(fadeText)

        Handler(Looper.getMainLooper()).postDelayed({

            if (auth.currentUser != null) {

                startActivity(
                    Intent(this, MainActivity::class.java)
                )

            } else {

                startActivity(
                    Intent(this, LoginActivity::class.java)
                )
            }

            finish()

        }, 3000)
    }

}