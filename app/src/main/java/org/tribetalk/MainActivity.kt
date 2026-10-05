package org.tribetalk

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tribetalk.core.NativePipeline
import org.tribetalk.core.TribeTalkTranslator
import org.tribetalk.core.memory.GovernorStage
import org.tribetalk.core.memory.MemoryGovernor
import org.tribetalk.core.memory.MemoryHudOverlay
import org.tribetalk.core.orchestration.ResourceOrchestrator
import org.tribetalk.fln.image.FlnImageLoader
import org.tribetalk.ui.screens.FlnViewModel
import org.tribetalk.ui.screens.HomeScreen
import org.tribetalk.ui.screens.MainAppScreen
import org.tribetalk.ui.screens.TranslationViewModel
import org.tribetalk.ui.theme.TribeTalkTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TranslationViewModel by viewModels()
    private val flnViewModel: FlnViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Wire MemoryGovernor to enforce sequential single-stage residency on 2 GB tablets
        val governor = MemoryGovernor.get(applicationContext)
        ResourceOrchestrator.attachMemoryGovernor(governor)
        governor.evictHandler = { victim ->
            when (victim) {
                GovernorStage.ASR -> viewModel.onnxConformerAsr.release()
                GovernorStage.NMT -> TribeTalkTranslator.release()
                GovernorStage.SLM -> lifecycleScope.launch(Dispatchers.IO) { flnViewModel.localModel.unload() }
                GovernorStage.TTS -> {}
            }
        }

        setContent {
            TribeTalkTheme {
                // Request audio record permission on launch if not granted
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (!isGranted) {
                        Toast.makeText(
                            this,
                            "Microphone permission is required for speech translation",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                LaunchedEffect(Unit) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!hasPermission) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        MainAppScreen(
                            translationViewModel = viewModel,
                            flnViewModel = flnViewModel
                        )
                        if (BuildConfig.DEBUG) {
                            Box(modifier = Modifier.align(Alignment.TopCenter)) {
                                MemoryHudOverlay(governor = governor)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        val governor = MemoryGovernor.get(applicationContext)
        governor.evictAll()
        FlnImageLoader.evictCaches()
        NativePipeline.trimMemory()
    }
}
