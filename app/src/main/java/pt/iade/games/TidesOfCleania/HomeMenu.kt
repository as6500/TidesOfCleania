package pt.iade.games.TidesOfCleania

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.google.android.material.appbar.MaterialToolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeMenu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_menu)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            view.setPadding(
                view.paddingLeft,
                insets.getInsets(WindowInsetsCompat.Type.statusBars()).top,
                view.paddingRight,
                view.paddingBottom
            )
            insets
        }

        val btnFirst = findViewById<Button>(R.id.btnFirst)
        val btnSecond = findViewById<Button>(R.id.btnSecond)

        btnFirst.setOnClickListener {
            val intent = Intent(this, AquaFit::class.java)
            startActivity(intent)
        }

        btnSecond.setOnClickListener {
            val intent = Intent(this, AquaOke::class.java)
            startActivity(intent)
        }
    }
}
