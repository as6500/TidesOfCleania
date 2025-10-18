package pt.iade.games.TidesOfCleania

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AquaFit : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aquafit)

        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            // go back to home menu
            val intent = Intent(this, HomeMenu::class.java)
            startActivity(intent)
            finish()
        }
    }
}
