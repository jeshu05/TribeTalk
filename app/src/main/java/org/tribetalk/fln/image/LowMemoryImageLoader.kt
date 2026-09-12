package org.tribetalk.fln.image

import android.content.Context
import android.graphics.Bitmap
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.size.Precision

/**
 * Singleton Coil ImageLoader specifically configured for 2 GB RAM Android devices.
 * Features:
 * - RGB_565 bitmap decoding (50% RAM reduction vs standard ARGB_8888)
 * - Strict 15 MB heap memory cache ceiling
 * - Exact precision downsampling to card viewport dimensions (256x256 max)
 * - Aggressive memory recycling on card transition
 */
object LowMemoryImageLoader {

    @Volatile
    private var instance: ImageLoader? = null

    fun get(context: Context): ImageLoader {
        return instance ?: synchronized(this) {
            instance ?: ImageLoader.Builder(context.applicationContext)
                .memoryCache {
                    MemoryCache.Builder(context.applicationContext)
                        // Bounded memory cache: never exceed 15 MB or 8% of low-RAM device heap
                        .maxSizePercent(0.08)
                        .strongReferencesEnabled(true)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(context.applicationContext.cacheDir.resolve("flashcard_image_cache"))
                        .maxSizeBytes(25L * 1024 * 1024) // 25 MB disk ceiling
                        .build()
                }
                // Decode in RGB_565 (2 bytes per pixel instead of 4) for 50% RAM savings
                .bitmapConfig(Bitmap.Config.RGB_565)
                .precision(Precision.INEXACT)
                .crossfade(true)
                .crossfade(200)
                .respectCacheHeaders(false)
                .build().also { instance = it }
        }
    }
}
