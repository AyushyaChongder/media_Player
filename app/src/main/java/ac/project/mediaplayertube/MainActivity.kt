package ac.project.mediaplayertube

import ac.project.mediaplayertube.databinding.ActivityMainBinding
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ac.project.mediaplayertube.ui.theme.MediaPlayerTubeTheme
import android.content.Intent

class MainActivity : ComponentActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.videobtn1.setOnClickListener {
            var url="https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            goToPlayerPage(url)
        }


        binding.videobtn2.setOnClickListener {
            var url="https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
            goToPlayerPage(url)
        }

        binding.videobtn3.setOnClickListener {
            var url="https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"
            goToPlayerPage(url)
        }
    }

    fun goToPlayerPage(url:String){
        var intent=Intent(this,MediaPlayerActivity::class.java)
        intent.putExtra("url",url)
        startActivity(intent)
    }
}

