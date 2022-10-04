@file:Suppress("DEPRECATION")

package shubh.dussehra


import android.annotation.SuppressLint
import android.graphics.Point
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.ar.sceneform.CameraNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import shubh.dussehra.databinding.ActivityMainBinding
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var camera: CameraNode
    private lateinit var point: Point
    private var ravanaNode: ModelNode? = null
    private var rambanaCounter: Int = 0
    private lateinit var soundPool: SoundPool
    private var ravanaLaughter by Delegates.notNull<Int>()
    private var bowAppearing by Delegates.notNull<Int>()
    private var arrowShooting by Delegates.notNull<Int>()
    private var ravanaDying by Delegates.notNull<Int>()
    private var fireworks by Delegates.notNull<Int>()
    private var ending by Delegates.notNull<Int>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val display = this.display
        display?.let {
            point = Point()
            display.getRealSize(point)
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        loadSoundPool()
        camera = binding.scene.cameraNode
        placeRavana()
        binding.shootbutton.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                v.scaleX = 0.9f
                v.scaleY = 0.9f
            } else if (event.action == MotionEvent.ACTION_UP) {
                v.scaleX = 1.0f
                v.scaleY = 1.0f
            }
            false
        }


        binding.shootbutton.setOnClickListener {
            shootRamabana(if ((0..100).random() % 2f == 0f) (60..180).random() else (180..240).random())
            rambanaCounter++

        }

    }

    private fun loadSoundPool() {
        val audioAttributes =
            AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_GAME).build()
        soundPool = SoundPool.Builder().setMaxStreams(1).setAudioAttributes(audioAttributes).build()
        ravanaLaughter = soundPool.load(this@MainActivity, R.raw.evil_laugh, 1)
        bowAppearing = soundPool.load(this@MainActivity, R.raw.bell, 1)
        arrowShooting = soundPool.load(this@MainActivity, R.raw.arrow, 1)
        ravanaDying = soundPool.load(this@MainActivity, R.raw.dying, 1)
        fireworks = soundPool.load(this@MainActivity, R.raw.fireworksound, 1)
        ending = soundPool.load(this@MainActivity, R.raw.ending, 1)
        lifecycleScope.launch {
            delay(1000)
            withContext(Dispatchers.Main) {
                playSound(ravanaLaughter)
                delay(6000)
                binding.shootbutton.visibility = View.VISIBLE
                binding.shootbutton.animate().alpha(1.0f)
                binding.shootbutton.animate()
                playSound(bowAppearing)
            }
        }


    }


    data class Model(
        val fileLocation: String,
        val scaleUnits: Float? = null,
        val placementMode: PlacementMode = PlacementMode.BEST_AVAILABLE,
        val applyPoseRotation: Boolean = true
    )

    private val ravana = Model(
        "models/ravana.glb",
        applyPoseRotation = true,
        placementMode = PlacementMode.PLANE_HORIZONTAL_AND_VERTICAL,
        scaleUnits = 1.5f,
    )

    private fun placeRavana() {
        val model = ravana
        ravanaNode = ModelNode(
            position = Position(x = 0.0f, y = -10.0f, z = -20.0f),
            rotation = Rotation(x = 0.0f, y = 0.0f, z = 0.0f),
            scale = Scale(15.0f)
        ).apply {
            loadModelAsync(
                context = this@MainActivity,
                lifecycle = lifecycle,
                glbFileLocation = model.fileLocation,
                autoAnimate = true,
                scaleToUnits = model.scaleUnits

                // Place the model origin at the bottom center
            )
            binding.scene.planeRenderer.isVisible = false
        }

        ravanaNode?.let {
            binding.scene.addChild(it)

        }

    }

    private fun shootRamabana(random: Int) {
        val node = ModelNode(
            position = Position(x = 0.0f, y = 0.0f, z = -20.0f),
            rotation = Rotation(x = 0.0f, y = random.toFloat(), z = 0.0f),
            scale = Scale(0.5f)
        ).apply {
            loadModelAsync(
                context = this@MainActivity,
                lifecycle = lifecycle,
                glbFileLocation = "models/arrow.glb",
                scaleToUnits = 20.0f,
                autoAnimate = true,
                centerOrigin = Position(x = 0.0f, y = 0.0f, z = 0.0f)
            )
        }
        binding.scene.addChild(node)
        playSound(arrowShooting)
        lifecycleScope.launch {
            delay(500)
            withContext(Dispatchers.Main) {
                playSound(ravanaDying)
            }
            if (rambanaCounter == 5) {
                rambanaCounter = 0
                withContext(Dispatchers.Main) {
                    binding.shootbutton.isVisible = false
                    binding.scene.isVisible = false
                    Glide.with(this@MainActivity).asGif().load(R.raw.massive_firework)
                        .into(binding.ivFireworks)
                    binding.endingFrame.isVisible = true
                    playSound(fireworks)
                }
                delay(2000)
                withContext(Dispatchers.Main) {
                    playSound(ending)
                }
                delay(2000)
                withContext(Dispatchers.Main) {
                    finish()
                }


            }
        }


    }

    private fun playSound(sound: Int) {
        try {
            soundPool.play(sound, 1f, 1f, 1, 0, 1f)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}





