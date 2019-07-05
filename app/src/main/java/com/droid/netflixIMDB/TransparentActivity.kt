package com.droid.netflixIMDB

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class TransparentActivity : AppCompatActivity() {

    companion object {
        const val ARG_RATING: String = "imdb_rating"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transparent)
        checkRatingIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkRatingIntent(intent)
    }

    private fun checkRatingIntent(intent: Intent?) {
        if (intent?.getStringExtra(ARG_RATING) != null) {
            Toast.makeText(this, "Rating: ${intent.getStringExtra(ARG_RATING)}", Toast.LENGTH_SHORT).show()
        }
    }
}
