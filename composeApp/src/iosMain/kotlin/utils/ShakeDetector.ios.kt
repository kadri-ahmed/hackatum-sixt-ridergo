package utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue

@Composable
actual fun OnShake(onShake: () -> Unit) {
    val motionManager = remember { CMMotionManager() }
    
    DisposableEffect(Unit) {
        if (motionManager.accelerometerAvailable) {
            motionManager.accelerometerUpdateInterval = 0.2
            val queue = NSOperationQueue.mainQueue
            
            motionManager.startAccelerometerUpdatesToQueue(queue) { data, error ->
                if (data != null) {
                    val acceleration = data.acceleration
                    val x = acceleration.x
                    val y = acceleration.y
                    val z = acceleration.z
                    
                    // Simple threshold check (gravity is ~1.0 in Gs)
                    val gForce = x * x + y * y + z * z
                    if (gForce > 2.5) { // Threshold > 1.5G roughly
                         onShake()
                    }
                }
            }
        }

        onDispose {
            motionManager.stopAccelerometerUpdates()
        }
    }
}
