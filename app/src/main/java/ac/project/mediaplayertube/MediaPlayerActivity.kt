package ac.project.mediaplayertube

import ac.project.mediaplayertube.databinding.ActivityMediaPlayerBinding
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class MediaPlayerActivity : AppCompatActivity() {

    lateinit var binding: ActivityMediaPlayerBinding

    lateinit var exoPlayer: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMediaPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url=intent.getStringExtra("url")

        //Initializing Exoplayer
        exoPlayer=ExoPlayer.Builder(this).build()
        binding.mediaPlayer.player=exoPlayer

        val mediaItem=MediaItem.fromUri(url!!)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

        exoPlayer.addListener(object :Player.Listener{
          override fun onPlayerError(error:PlaybackException){
              Toast.makeText(applicationContext,"Erroe playing media!",Toast.LENGTH_SHORT).show()
            super.onPlayerError(error)
          }
        })
    }

    override fun onStart() {
        super.onStart()
        exoPlayer.playWhenReady=true
    }

    override fun onStop() {
        super.onStop()
        exoPlayer.playWhenReady=false
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }
}