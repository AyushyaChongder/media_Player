package ac.project.mediaplayertube

import ac.project.mediaplayertube.databinding.ActivityMediaPlayerBinding
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector

@UnstableApi
@OptIn(UnstableApi::class)
class MediaPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaPlayerBinding
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var trackSelector: DefaultTrackSelector

    private var isPlaying: Boolean = false
    private var isVolumeOn: Boolean = true
    private var isFullscreen: Boolean = false
    private var playbackPosition: Long = 0

    private lateinit var gestureDetector: GestureDetector
    private val hideHandler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable { hideControls() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = intent.getStringExtra("url")

        // Initializing Exoplayer with Track Selector
        trackSelector = DefaultTrackSelector(this)
        exoPlayer = ExoPlayer.Builder(this).setTrackSelector(trackSelector).build()
        binding.mediaPlayer.player = exoPlayer

        val mediaItem = MediaItem.fromUri(url!!)
        exoPlayer.setMediaItem(mediaItem)

        if (savedInstanceState != null) {
            playbackPosition = savedInstanceState.getLong("playbackPosition", 0)
            isFullscreen = savedInstanceState.getBoolean("isFullscreen", false)
        }

        exoPlayer.prepare()
        exoPlayer.play()
        exoPlayer.seekTo(playbackPosition)

        // Play/Pause ImageView
        binding.playPauseButton.setOnClickListener {
            isPlaying = !isPlaying
            if (isPlaying) {
                exoPlayer.play()
                binding.playPauseButton.setImageResource(R.drawable.ic_pause)
            } else {
                exoPlayer.pause()
                binding.playPauseButton.setImageResource(R.drawable.ic_play)
            }
        }

        // Forward Button ImageView
        binding.forwardButton.setOnClickListener {
            exoPlayer.seekTo(exoPlayer.currentPosition + 10000) // Seek forward by 10 seconds
        }

        // Rewind Button ImageView
        binding.rewindButton.setOnClickListener {
            exoPlayer.seekTo(exoPlayer.currentPosition - 10000) // Seek backward by 10 seconds
        }

        // SeekBar for tracking progress
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    exoPlayer.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not needed for this implementation
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not needed for this implementation
            }
        })

        // Playback Error Handling
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(applicationContext, "Error playing media!", Toast.LENGTH_SHORT).show()
                super.onPlayerError(error)
            }
        })

        // Update SeekBar and timing text based on player state
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                val currentPosition = exoPlayer.currentPosition
                val duration = exoPlayer.duration

                // Update SeekBar progress
                binding.seekBar.max = duration.toInt()
                binding.seekBar.progress = currentPosition.toInt()

                // Update play/pause button state
                binding.playPauseButton.setImageResource(
                    if (exoPlayer.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                )
            }

            override fun onPositionDiscontinuity(reason: Int) {
                super.onPositionDiscontinuity(reason)
                // Update SeekBar progress
                binding.seekBar.progress = exoPlayer.currentPosition.toInt()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                // Update play/pause button state
                binding.playPauseButton.setImageResource(
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                )
            }
        })

        // Volume Button
        binding.volumeButton.setOnClickListener {
            isVolumeOn = !isVolumeOn
            if (isVolumeOn) {
                exoPlayer.volume = 1f
                binding.volumeButton.setImageResource(R.drawable.ic_volume_on)
            } else {
                exoPlayer.volume = 0f
                binding.volumeButton.setImageResource(R.drawable.ic_volume_off)
            }
        }

        // Quality Button
        binding.qualityButton.setOnClickListener {
            showQualityPopup(it)
        }

        // Fullscreen ImageView
        binding.fullscreenButton.setOnClickListener {
            toggleFullscreen()
        }

        // Gesture Detector for showing/hiding controls
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                toggleControlsVisibility()
                return true
            }
        })

        binding.mediaPlayer.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        // Initially hide the controls
        hideControls()
    }

    private fun showQualityPopup(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.quality_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.quality_auto -> changeQuality("Auto")
                R.id.quality_low -> changeQuality("Low")
                R.id.quality_medium -> changeQuality("Medium")
                R.id.quality_high -> changeQuality("High")
            }
            true
        }
        popupMenu.show()
    }

    private fun changeQuality(quality: String) {
        val parametersBuilder = trackSelector.buildUponParameters()
        when (quality) {
            "Auto" -> parametersBuilder.setMaxVideoSize(Integer.MAX_VALUE, Integer.MAX_VALUE)
            "Low" -> parametersBuilder.setMaxVideoSize(426, 240) // 240p
            "Medium" -> parametersBuilder.setMaxVideoSize(854, 480) // 480p
            "High" -> parametersBuilder.setMaxVideoSize(1280, 720) // 720p
        }
        trackSelector.setParameters(parametersBuilder)
    }

    private fun toggleControlsVisibility() {
        if (binding.topControls.isVisible) {
            hideControls()
        } else {
            showControls()
        }
    }

    private fun showControls() {
        binding.topControls.isVisible = true
        binding.playPauseButton.isVisible = true
        binding.rewindButton.isVisible = true
        binding.forwardButton.isVisible = true
        binding.seekBar.isVisible = true

        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, 3000) // Hide controls after 3 seconds
    }

    private fun hideControls() {
        binding.topControls.isVisible = false
        binding.playPauseButton.isVisible = false
        binding.rewindButton.isVisible = false
        binding.forwardButton.isVisible = false
        binding.seekBar.isVisible = false
    }

    override fun onStart() {
        super.onStart()
        exoPlayer.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        exoPlayer.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("playbackPosition", exoPlayer.currentPosition)
        outState.putBoolean("isFullscreen", isFullscreen)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isFullscreen = savedInstanceState.getBoolean("isFullscreen", false)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && !isFullscreen) {
            setFullscreen(true)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT && isFullscreen) {
            setFullscreen(false)
        }
    }

    private fun toggleFullscreen() {
        if (isFullscreen) {
            // Switch to portrait mode
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            binding.fullscreenButton.setImageResource(R.drawable.ic_fullscreen_enter)
        } else {
            // Switch to landscape mode
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            binding.fullscreenButton.setImageResource(R.drawable.ic_fullscreen_exit)
        }
        isFullscreen = !isFullscreen
    }

    private fun setFullscreen(fullscreen: Boolean) {
        if (fullscreen) {
            // Adjust layout for landscape mode
            binding.mediaPlayer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            hideSystemUI()
        } else {
            // Adjust layout for portrait mode
            binding.mediaPlayer.layoutParams.height = resources.displayMetrics.heightPixels * 3 / 5// Example fixed height for portrait mode
            showSystemUI()
        }
        isFullscreen = fullscreen
    }

    private fun hideSystemUI() {
        binding.root.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
    }

    private fun showSystemUI() {
        binding.root.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }
}
