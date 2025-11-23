package utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun OnShake(onShake: () -> Unit) {
    val motionManager = remember { CMMotionManager() }
    
    DisposableEffect(Unit) {
        if (motionManager.isAccelerometerAvailable()) {
            motionManager.accelerometerUpdateInterval = 0.2
            val queue = NSOperationQueue.mainQueue
            
            motionManager.startAccelerometerUpdatesToQueue(queue) { data, error ->
                if (data != null) {
                    data.acceleration.useContents { 
                        val gForce = x * x + y * y + z * z
                        if (gForce > 2.5) { // Threshold > 1.5G roughly
                             onShake()
                        }
                    }
                }
            }
        }

        onDispose {
            motionManager.stopAccelerometerUpdates()
        }
    }
}
