package org.tribetalk.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural cartoon vector illustrations for NIPUN Bharat kid flashcards and worksheets.
 * Strictly uses Compose Canvas geometry with zero bitmap memory overhead.
 * Delivers 60 FPS rendering on low-cost 2 GB RAM tablets with delightful, kid-friendly cartoon art.
 */
@Composable
fun FlnVectorGraphic(
    iconType: String,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
    numeralValue: Int? = null
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        when (iconType.lowercase()) {
            "elephant" -> ElephantCartoonGraphic(modifier = Modifier.size(size))
            "dog" -> DogCartoonGraphic(modifier = Modifier.size(size))
            "cat" -> CatCartoonGraphic(modifier = Modifier.size(size))
            "cow", "goat" -> CowCartoonGraphic(modifier = Modifier.size(size))
            "fish" -> FishCartoonGraphic(modifier = Modifier.size(size))
            "bird" -> BirdCartoonGraphic(modifier = Modifier.size(size))
            "mango" -> MangoCartoonGraphic(modifier = Modifier.size(size))
            "apple", "fruit" -> AppleCartoonGraphic(modifier = Modifier.size(size))
            "sun" -> SunCartoonGraphic(modifier = Modifier.size(size))
            "tree", "forest" -> TreeCartoonGraphic(modifier = Modifier.size(size))
            "flower" -> FlowerCartoonGraphic(modifier = Modifier.size(size))
            "school" -> SchoolCartoonGraphic(modifier = Modifier.size(size))
            "book" -> BookCartoonGraphic(modifier = Modifier.size(size))
            "pencil" -> PencilCartoonGraphic(modifier = Modifier.size(size))
            "train" -> TrainCartoonGraphic(modifier = Modifier.size(size))
            "number_counter" -> NumberBlocksCartoonGraphic(
                count = numeralValue ?: 1,
                modifier = Modifier.size(size)
            )
            "coin" -> CoinCartoonGraphic(modifier = Modifier.size(size))
            "water", "river" -> WaterCartoonGraphic(modifier = Modifier.size(size))
            "mountain" -> MountainCartoonGraphic(modifier = Modifier.size(size))
            "star" -> StarCartoonGraphic(modifier = Modifier.size(size))
            "akshar" -> BookCartoonGraphic(modifier = Modifier.size(size))
            "shape_circle" -> ShapeCircleGraphic(color = tint, modifier = Modifier.size(size))
            "shape_triangle" -> ShapeTriangleGraphic(color = tint, modifier = Modifier.size(size))
            "shape_square", "concept_big", "concept_small", "concept_up", "concept_down" -> NumberBlocksCartoonGraphic(count = 1, modifier = Modifier.size(size))
            else -> DefaultCuteStarGraphic(modifier = Modifier.size(size))
        }
    }
}

// =============================================================================
// 1. CUTE ELEPHANT (ᱦᱟᱹᱛᱤ)
// =============================================================================
@Composable
fun ElephantCartoonGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val skinColor = Color(0xFF818CF8) // Soft lavender-indigo
        val earInnerColor = Color(0xFFF472B6) // Rosy pink
        val blushColor = Color(0xFFFB7185).copy(alpha = 0.5f)

        // Back ear
        drawOval(
            color = skinColor.copy(alpha = 0.8f),
            topLeft = Offset(w * 0.12f, h * 0.18f),
            size = Size(w * 0.32f, h * 0.42f)
        )

        // Body
        drawOval(
            color = skinColor,
            topLeft = Offset(w * 0.22f, h * 0.28f),
            size = Size(w * 0.56f, h * 0.55f)
        )

        // Front Head
        drawCircle(
            color = skinColor,
            radius = w * 0.26f,
            center = Offset(w * 0.48f, h * 0.44f)
        )

        // Front Big Ear
        drawOval(
            color = skinColor,
            topLeft = Offset(w * 0.56f, h * 0.22f),
            size = Size(w * 0.34f, h * 0.44f)
        )
        // Inner Ear
        drawOval(
            color = earInnerColor,
            topLeft = Offset(w * 0.62f, h * 0.28f),
            size = Size(w * 0.22f, h * 0.30f)
        )

        // Curved Trunk
        val trunkPath = Path().apply {
            moveTo(w * 0.36f, h * 0.52f)
            quadraticTo(w * 0.20f, h * 0.65f, w * 0.24f, h * 0.78f)
            quadraticTo(w * 0.26f, h * 0.85f, w * 0.34f, h * 0.82f)
            quadraticTo(w * 0.36f, h * 0.74f, w * 0.30f, h * 0.72f)
            quadraticTo(w * 0.28f, h * 0.64f, w * 0.42f, h * 0.54f)
            close()
        }
        drawPath(path = trunkPath, color = skinColor)

        // Eye with twinkle
        drawCircle(color = Color(0xFF1E1B4B), radius = w * 0.045f, center = Offset(w * 0.42f, h * 0.40f))
        drawCircle(color = Color.White, radius = w * 0.016f, center = Offset(w * 0.405f, h * 0.385f))

        // Rosy Blush Cheek
        drawCircle(color = blushColor, radius = w * 0.05f, center = Offset(w * 0.44f, h * 0.49f))

        // Cute Little Water Spray Drops from Trunk
        drawCircle(color = Color(0xFF38BDF8), radius = w * 0.035f, center = Offset(w * 0.36f, h * 0.72f))
        drawCircle(color = Color(0xFF38BDF8), radius = w * 0.025f, center = Offset(w * 0.42f, h * 0.66f))
    }
}

// =============================================================================
// 2. PLAYFUL PUP (ᱥᱮᱛᱟ)
// =============================================================================
@Composable
fun DogCartoonGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val furColor = Color(0xFFF59E0B) // Golden caramel
        val earColor = Color(0xFFB45309) // Warm brown
        val tongueColor = Color(0xFFF43F5E)

        // Floppy Left Ear
        val leftEar = Path().apply {
            moveTo(w * 0.24f, h * 0.28f)
            quadraticTo(w * 0.10f, h * 0.45f, w * 0.22f, h * 0.62f)
            quadraticTo(w * 0.30f, h * 0.55f, w * 0.32f, h * 0.38f)
            close()
        }
        drawPath(path = leftEar, color = earColor)

        // Floppy Right Ear
        val rightEar = Path().apply {
            moveTo(w * 0.76f, h * 0.28f)
            quadraticTo(w * 0.90f, h * 0.45f, w * 0.78f, h * 0.62f)
            quadraticTo(w * 0.70f, h * 0.55f, w * 0.68f, h * 0.38f)
            close()
        }
        drawPath(path = rightEar, color = earColor)

        // Head
        drawCircle(color = furColor, radius = w * 0.30f, center = Offset(w * 0.50f, h * 0.48f))

        // Snout Muzzle
        drawOval(
            color = Color(0xFFFEF3C7),
            topLeft = Offset(w * 0.36f, h * 0.50f),
            size = Size(w * 0.28f, h * 0.24f)
        )

        // Black Nose
        drawOval(
            color = Color(0xFF18181B),
            topLeft = Offset(w * 0.45f, h * 0.52f),
            size = Size(w * 0.10f, h * 0.08f)
        )

        // Happy Open Tongue
        drawOval(
            color = tongueColor,
            topLeft = Offset(w * 0.46f, h * 0.64f),
            size = Size(w * 0.08f, h * 0.12f)
        )

        // Eyes with sparkles
        drawCircle(color = Color(0xFF18181B), radius = w * 0.045f, center = Offset(w * 0.41f, h * 0.42f))
        drawCircle(color = Color.White, radius = w * 0.016f, center = Offset(w * 0.395f, h * 0.405f))

        drawCircle(color = Color(0xFF18181B), radius = w * 0.045f, center = Offset(w * 0.59f, h * 0.42f))
        drawCircle(color = Color.White, radius = w * 0.016f, center = Offset(w * 0.575f, h * 0.405f))

        // Cute Rosy Cheeks
        drawCircle(color = Color(0xFFFB7185).copy(alpha = 0.5f), radius = w * 0.05f, center = Offset(w * 0.32f, h * 0.52f))
        drawCircle(color = Color(0xFFFB7185).copy(alpha = 0.5f), radius = w * 0.05f, center = Offset(w * 0.68f, h * 0.52f))
    }
}

// =============================================================================
// 3. COLORFUL CLOWNFISH (ᱦᱟᱹᱠᱩ)
// =============================================================================
@Composable
fun FishCartoonGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val fishColor = Color(0xFFFB923C) // Vibrant orange
        val stripeColor = Color(0xFFFFFFFF)

        // Tail Fin
        val tail = Path().apply {
            moveTo(w * 0.74f, h * 0.50f)
            lineTo(w * 0.92f, h * 0.28f)
            quadraticTo(w * 0.84f, h * 0.50f, w * 0.92f, h * 0.72f)
            close()
        }
        drawPath(path = tail, color = fishColor)

        // Main Body Oval
        val body = Path().apply {
            moveTo(w * 0.18f, h * 0.50f)
            quadraticTo(w * 0.46f, h * 0.16f, w * 0.78f, h * 0.50f)
            quadraticTo(w * 0.46f, h * 0.84f, w * 0.18f, h * 0.50f)
            close()
        }
        drawPath(path = body, color = fishColor)

        // Top Fin
        val topFin = Path().apply {
            moveTo(w * 0.40f, h * 0.24f)
            quadraticTo(w * 0.52f, h * 0.10f, w * 0.64f, h * 0.28f)
            close()
        }
        drawPath(path = topFin, color = fishColor)

        // White Curved Stripes
        drawRoundRect(
            color = stripeColor,
            topLeft = Offset(w * 0.42f, h * 0.25f),
            size = Size(w * 0.07f, h * 0.50f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        drawRoundRect(
            color = stripeColor,
            topLeft = Offset(w * 0.62f, h * 0.35f),
            size = Size(w * 0.06f, h * 0.30f),
            cornerRadius = CornerRadius(8f, 8f)
        )

        // Cute Big Eye
        drawCircle(color = Color.White, radius = w * 0.08f, center = Offset(w * 0.32f, h * 0.44f))
        drawCircle(color = Color(0xFF1E293B), radius = w * 0.045f, center = Offset(w * 0.30f, h * 0.44f))
        drawCircle(color = Color.White, radius = w * 0.016f, center = Offset(w * 0.285f, h * 0.425f))

        // Rosy Cheek
        drawCircle(color = Color(0xFFF43F5E).copy(alpha = 0.5f), radius = w * 0.04f, center = Offset(w * 0.35f, h * 0.55f))

        // Water Bubbles
        drawCircle(color = Color(0xFF38BDF8).copy(alpha = 0.6f), radius = w * 0.045f, center = Offset(w * 0.14f, h * 0.34f))
        drawCircle(color = Color(0xFF38BDF8).copy(alpha = 0.6f), radius = w * 0.030f, center = Offset(w * 0.10f, h * 0.22f))
    }
}

// =============================================================================
// 4. SWEET BLUEBIRD (ᱪᱮᱬᱮ)
// =============================================================================
@Composable
fun BirdCartoonGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val blueColor = Color(0xFF0284C7)
        val bellyColor = Color(0xFFFDE047)
        val beakColor = Color(0xFFF97316)

        // Perch Branch
        drawRoundRect(
            color = Color(0xFF78350F),
            topLeft = Offset(w * 0.15f, h * 0.74f),
            size = Size(w * 0.70f, h * 0.07f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        // Green Leaf on branch
        drawOval(
            color = Color(0xFF22C55E),
            topLeft = Offset(w * 0.70f, h * 0.67f),
            size = Size(w * 0.16f, h * 0.08f)
        )

        // Tail feathers
        val tail = Path().apply {
            moveTo(w * 0.26f, h * 0.55f)
            lineTo(w * 0.10f, h * 0.68f)
            lineTo(w * 0.20f, h * 0.62f)
            close()
        }
        drawPath(path = tail, color = blueColor)

        // Main Body Oval
        drawOval(
            color = blueColor,
            topLeft = Offset(w * 0.25f, h * 0.30f),
            size = Size(w * 0.48f, h * 0.44f)
        )

        // Yellow Belly
        drawOval(
            color = bellyColor,
            topLeft = Offset(w * 0.44f, h * 0.40f),
            size = Size(w * 0.26f, h * 0.32f)
        )

        // Head
        drawCircle(color = blueColor, radius = w * 0.20f, center = Offset(w * 0.55f, h * 0.36f))

        // Cute Orange Beak
        val beak = Path().apply {
            moveTo(w * 0.72f, h * 0.34f)
            lineTo(w * 0.88f, h * 0.38f)
            lineTo(w * 0.72f, h * 0.42f)
            close()
        }
        drawPath(path = beak, color = beakColor)

        // Eye with twinkle
        drawCircle(color = Color(0xFF0F172A), radius = w * 0.045f, center = Offset(w * 0.63f, h * 0.34f))
        drawCircle(color = Color.White, radius = w * 0.016f, center = Offset(w * 0.615f, h * 0.325f))

        // Wing
        drawOval(
            color = Color(0xFF0369A1),
            topLeft = Offset(w * 0.30f, h * 0.42f),
            size = Size(w * 0.26f, h * 0.20f)
        )
    }
}

// =============================================================================
// 5. JUICY SMILING MANGO (ᱩᱞ)
// =============================================================================
@Composable
fun MangoCartoonGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Fresh Green Leaf
        val leaf = Path().apply {
            moveTo(w * 0.50f, h * 0.18f)
            quadraticTo(w * 0.75f, h * 0.08f, w * 0.82f, h * 0.20f)
            quadraticTo(w * 0.65f, h * 0.26f, w * 0.50f, h * 0.18f)
            close()
        }
        drawPath(path = leaf, color = Color(0xFF22C55E))

        // Brown Stem
        drawRoundRect(
            color = Color(0xFF78350F),
            topLeft = Offset(w * 0.47f, h * 0.12f),
            size = Size(w * 0.06f, h * 0.10f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // Golden Mango Body
        val mangoPath = Path().apply {
            moveTo(w * 0.50f, h * 0.20f)
            cubicTo(w * 0.86f, h * 0.24f, w * 0.88f, h * 0.64f, w * 0.60f, h * 0.86f)
            cubicTo(w * 0.42f, h * 0.94f, w * 0.22f, h * 0.84f, w * 0.22f, h * 0.54f)
            cubicTo(w * 0.22f, h * 0.32f, w * 0.36f, h * 0.20f, w * 0.50f, h * 0.20f)
            close()
        }
        drawPath(path = mangoPath, color = Color(0xFFFBBF24)) // Golden Yellow
        // Peach gradient overlay on side
        drawCircle(color = Color(0xFFF97316).copy(alpha = 0.35f), radius = w * 0.22f, center = Offset(w * 0.62f, h * 0.60f))

        // Kawaii Smiling Eyes
        drawCircle(color = Color(0xFF1E1B4B), radius = w * 0.04f, center = Offset(w * 0.42f, h * 0.48f))
        drawCircle(color = Color.White, radius = w * 0.015f, center = Offset(w * 0.405f, h * 0.465f))

        drawCircle(color = Color(0xFF1E1B4B), radius = w * 0.04f, center = Offset(w * 0.60f, h * 0.48f))
        drawCircle(color = Color.White, radius = w * 0.015f, center = Offset(w * 0.585f, h * 0.465f))

        // Cheerful Smile
        val smile = Path().apply {
            moveTo(w * 0.47f, h * 0.56f)
            quadraticTo(w * 0.51f, h * 0.63f, w * 0.55f, h * 0.56f)
        }
        drawPath(path = smile, color = Color(0xFF78350F), style = Stroke(width = 4f, cap = StrokeCap.Round))

        // Rosy Blush Cheeks
        drawCircle(color = Color(0xFFFB7185).copy(alpha = 0.5f), radius = w * 0.045f, center = Offset(w * 0.35f, h * 0.55f))
        drawCircle(color = Color(0xFFFB7185).copy(alpha = 0.5f), radius = w * 0.045f, center = Offset(w * 0.67f, h * 0.55f))
    }
}

// =============================================================================
// 6. CHEERFUL SMILING SUN (ᱵᱮᱲᱟ)
// =============================================================================
@Composable
fun SunCartoonGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.5f
        val sunColor = Color(0xFFFBBF24)
        val rayColor = Color(0xFFF59E0B)

        // 8 Playful Curved Rays
        for (i in 0 until 8) {
            val angle = i * (3.14159f * 2f / 8f)
            val rx = cx + cos(angle) * (w * 0.36f)
            val ry = cy + sin(angle) * (h * 0.36f)
            drawCircle(color = rayColor, radius = w * 0.06f, center = Offset(rx, ry))
        }

        // Main Sun Face
        drawCircle(color = sunColor, radius = w * 0.28f, center = Offset(cx, cy))

        // Happy Eyes
        drawCircle(color = Color(0xFF1E293B), radius = w * 0.04f, center = Offset(cx - w * 0.10f, cy - h * 0.04f))
        drawCircle(color = Color.White, radius = w * 0.015f, center = Offset(cx - w * 0.115f, cy - h * 0.055f))

        drawCircle(color = Color(0xFF1E293B), radius = w * 0.04f, center = Offset(cx + w * 0.10f, cy - h * 0.04f))
        drawCircle(color = Color.White, radius = w * 0.015f, center = Offset(cx + w * 0.085f, cy - h * 0.055f))

        // Happy Open Smile
        val smile = Path().apply {
            moveTo(cx - w * 0.08f, cy + h * 0.06f)
            quadraticTo(cx, cy + h * 0.15f, cx + w * 0.08f, cy + h * 0.06f)
        }
        drawPath(path = smile, color = Color(0xFF78350F), style = Stroke(width = 4f, cap = StrokeCap.Round))

        // Rosy Cheeks
        drawCircle(color = Color(0xFFFB7185).copy(alpha = 0.5f), radius = w * 0.04f, center = Offset(cx - w * 0.15f, cy + h * 0.04f))
        drawCircle(color = Color(0xFFFB7185).copy(alpha = 0.5f), radius = w * 0.04f, center = Offset(cx + w * 0.15f, cy + h * 0.04f))
    }
}

// =============================================================================
// 7. LUSH CARTOON TREE WITH APPLES (ᱫᱟᱨᱮ)
// =============================================================================
@Composable
fun TreeCartoonGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Brown Sturdy Trunk
        val trunk = Path().apply {
            moveTo(w * 0.42f, h * 0.52f)
            lineTo(w * 0.38f, h * 0.88f)
            quadraticTo(w * 0.50f, h * 0.86f, w * 0.62f, h * 0.88f)
            lineTo(w * 0.58f, h * 0.52f)
            close()
        }
        drawPath(path = trunk, color = Color(0xFF92400E))

        // Fluffy Cloud Foliage (3 overlapping circles)
        drawCircle(color = Color(0xFF16A34A), radius = w * 0.22f, center = Offset(w * 0.36f, h * 0.42f))
        drawCircle(color = Color(0xFF16A34A), radius = w * 0.22f, center = Offset(w * 0.64f, h * 0.42f))
        drawCircle(color = Color(0xFF22C55E), radius = w * 0.26f, center = Offset(w * 0.50f, h * 0.30f))

        // Red Apples dotted on tree
        val apples = listOf(
            Offset(w * 0.38f, h * 0.32f),
            Offset(w * 0.58f, h * 0.28f),
            Offset(w * 0.48f, h * 0.42f),
            Offset(w * 0.66f, h * 0.46f)
        )
        for (pos in apples) {
            drawCircle(color = Color(0xFFEF4444), radius = w * 0.04f, center = pos)
            drawCircle(color = Color.White.copy(alpha = 0.7f), radius = w * 0.015f, center = Offset(pos.x - 2f, pos.y - 2f))
        }
    }
}

// =============================================================================
// 8. COLORFUL VILLAGE SCHOOL (ᱤᱛᱩᱱ ᱟᱥᱲᱟ)
// =============================================================================
@Composable
fun SchoolCartoonGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // School Walls
        drawRoundRect(
            color = Color(0xFFFEF3C7), // Warm cream
            topLeft = Offset(w * 0.20f, h * 0.44f),
            size = Size(w * 0.60f, h * 0.42f),
            cornerRadius = CornerRadius(8f, 8f)
        )

        // Red Pitched Roof
        val roof = Path().apply {
            moveTo(w * 0.14f, h * 0.46f)
            lineTo(w * 0.50f, h * 0.20f)
            lineTo(w * 0.86f, h * 0.46f)
            close()
        }
        drawPath(path = roof, color = Color(0xFFEF4444))

        // Fluttering Blue Flag on Top
        val flagPole = Path().apply {
            moveTo(w * 0.50f, h * 0.20f)
            lineTo(w * 0.50f, h * 0.08f)
        }
        drawPath(path = flagPole, color = Color(0xFF64748B), style = Stroke(width = 4f))
        val flag = Path().apply {
            moveTo(w * 0.50f, h * 0.08f)
            lineTo(w * 0.66f, h * 0.13f)
            lineTo(w * 0.50f, h * 0.18f)
            close()
        }
        drawPath(path = flag, color = Color(0xFF3B82F6))

        // Arched Door
        drawRoundRect(
            color = Color(0xFFB45309),
            topLeft = Offset(w * 0.43f, h * 0.62f),
            size = Size(w * 0.14f, h * 0.24f),
            cornerRadius = CornerRadius(12f, 12f)
        )

        // Blue Window Panes
        drawRoundRect(
            color = Color(0xFF38BDF8),
            topLeft = Offset(w * 0.26f, h * 0.54f),
            size = Size(w * 0.12f, h * 0.14f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = Color(0xFF38BDF8),
            topLeft = Offset(w * 0.62f, h * 0.54f),
            size = Size(w * 0.12f, h * 0.14f),
            cornerRadius = CornerRadius(4f, 4f)
        )
    }
}

// =============================================================================
// 9. CUTE NUMBER BLOCKS (ᱞᱮᱠᱷᱟ / COUNTING)
// =============================================================================
@Composable
fun NumberBlocksCartoonGraphic(count: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val safeCount = count.coerceIn(1, 10)
        val colors = listOf(
            Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B),
            Color(0xFFEC4899), Color(0xFF8B5CF6), Color(0xFF06B6D4)
        )

        val cols = if (safeCount <= 4) 2 else if (safeCount <= 6) 3 else 4
        val rows = (safeCount + cols - 1) / cols
        val blockSize = (w / (cols + 0.8f)).coerceIn(18.dp.toPx(), 42.dp.toPx())
        val spacing = 6.dp.toPx()

        val startX = (w - (cols * blockSize + (cols - 1) * spacing)) / 2f
        val startY = (h - (rows * blockSize + (rows - 1) * spacing)) / 2f

        for (i in 0 until safeCount) {
            val col = i % cols
            val row = i / cols
            val bx = startX + col * (blockSize + spacing)
            val by = startY + row * (blockSize + spacing)
            val blockColor = colors[i % colors.size]

            // 3D Block Base
            drawRoundRect(
                color = blockColor,
                topLeft = Offset(bx, by),
                size = Size(blockSize, blockSize),
                cornerRadius = CornerRadius(12f, 12f)
            )
            // Lighter top highlight
            drawRoundRect(
                color = Color.White.copy(alpha = 0.35f),
                topLeft = Offset(bx + 2f, by + 2f),
                size = Size(blockSize - 4f, blockSize * 0.4f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            // Googly Eye on block
            val eyeX = bx + blockSize * 0.5f
            val eyeY = by + blockSize * 0.55f
            drawCircle(color = Color.White, radius = blockSize * 0.22f, center = Offset(eyeX, eyeY))
            drawCircle(color = Color(0xFF0F172A), radius = blockSize * 0.12f, center = Offset(eyeX, eyeY))
            drawCircle(color = Color.White, radius = blockSize * 0.04f, center = Offset(eyeX - 1f, eyeY - 1f))
        }
    }
}

// =============================================================================
// 10. CUTE STORYBOOK (ᱯᱩᱛᱷᱤ)
// =============================================================================
@Composable
fun BookCartoonGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Open Book Cover
        drawRoundRect(
            color = Color(0xFF6366F1),
            topLeft = Offset(w * 0.15f, h * 0.36f),
            size = Size(w * 0.70f, h * 0.44f),
            cornerRadius = CornerRadius(12f, 12f)
        )

        // Left Page
        drawRoundRect(
            color = Color(0xFFFFFBEB),
            topLeft = Offset(w * 0.18f, h * 0.38f),
            size = Size(w * 0.31f, h * 0.38f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        // Right Page
        drawRoundRect(
            color = Color(0xFFFFFBEB),
            topLeft = Offset(w * 0.51f, h * 0.38f),
            size = Size(w * 0.31f, h * 0.38f),
            cornerRadius = CornerRadius(8f, 8f)
        )

        // Wavy text lines on pages
        for (i in 1..3) {
            drawLine(
                color = Color(0xFF94A3B8),
                start = Offset(w * 0.22f, h * (0.42f + i * 0.07f)),
                end = Offset(w * 0.45f, h * (0.42f + i * 0.07f)),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF94A3B8),
                start = Offset(w * 0.55f, h * (0.42f + i * 0.07f)),
                end = Offset(w * 0.78f, h * (0.42f + i * 0.07f)),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }

        // Floating Gold Sparkles
        drawCircle(color = Color(0xFFFBBF24), radius = w * 0.035f, center = Offset(w * 0.48f, h * 0.22f))
        drawCircle(color = Color(0xFFFBBF24), radius = w * 0.025f, center = Offset(w * 0.62f, h * 0.20f))
        drawCircle(color = Color(0xFFFBBF24), radius = w * 0.020f, center = Offset(w * 0.36f, h * 0.24f))
    }
}

// =============================================================================
// 11. CUTE PENCIL (ᱯᱮᱱᱥᱤᱞ)
// =============================================================================
@Composable
fun PencilCartoonGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Pencil Body (Yellow)
        drawRoundRect(
            color = Color(0xFFFBBF24),
            topLeft = Offset(w * 0.36f, h * 0.32f),
            size = Size(w * 0.28f, h * 0.42f),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Pink Eraser Top
        drawRoundRect(
            color = Color(0xFFF472B6),
            topLeft = Offset(w * 0.36f, h * 0.18f),
            size = Size(w * 0.28f, h * 0.12f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        // Silver Ferrule
        drawRect(
            color = Color(0xFFCBD5E1),
            topLeft = Offset(w * 0.36f, h * 0.28f),
            size = Size(w * 0.28f, h * 0.05f)
        )

        // Wood Tip (Triangle)
        val tip = Path().apply {
            moveTo(w * 0.36f, h * 0.74f)
            lineTo(w * 0.50f, h * 0.90f)
            lineTo(w * 0.64f, h * 0.74f)
            close()
        }
        drawPath(path = tip, color = Color(0xFFFEF3C7))

        // Graphite Lead
        val lead = Path().apply {
            moveTo(w * 0.46f, h * 0.85f)
            lineTo(w * 0.50f, h * 0.90f)
            lineTo(w * 0.54f, h * 0.85f)
            close()
        }
        drawPath(path = lead, color = Color(0xFF1E293B))

        // Cute Smiley Eyes
        drawCircle(color = Color(0xFF1E293B), radius = w * 0.03f, center = Offset(w * 0.44f, h * 0.48f))
        drawCircle(color = Color(0xFF1E293B), radius = w * 0.03f, center = Offset(w * 0.56f, h * 0.48f))
        // Smile
        val smile = Path().apply {
            moveTo(w * 0.47f, h * 0.55f)
            quadraticTo(w * 0.50f, h * 0.60f, w * 0.53f, h * 0.55f)
        }
        drawPath(path = smile, color = Color(0xFF78350F), style = Stroke(width = 3f, cap = StrokeCap.Round))
    }
}

// =============================================================================
// 12. CUTE FLOWER (ᱵᱟᱦᱟ)
// =============================================================================
@Composable
fun FlowerCartoonGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.48f
        val petalColor = Color(0xFFEC4899) // Bright pink

        // Stem
        drawLine(
            color = Color(0xFF16A34A),
            start = Offset(cx, cy),
            end = Offset(cx, h * 0.88f),
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )

        // Leaves
        drawOval(
            color = Color(0xFF22C55E),
            topLeft = Offset(cx - w * 0.22f, h * 0.66f),
            size = Size(w * 0.24f, h * 0.10f)
        )

        // 5 Round Petals
        for (i in 0 until 5) {
            val angle = i * (3.14159f * 2f / 5f)
            val px = cx + cos(angle) * (w * 0.22f)
            val py = cy + sin(angle) * (h * 0.22f)
            drawCircle(color = petalColor, radius = w * 0.14f, center = Offset(px, py))
        }

        // Center Gold Circle with smile
        drawCircle(color = Color(0xFFFBBF24), radius = w * 0.16f, center = Offset(cx, cy))
        drawCircle(color = Color(0xFF1E293B), radius = w * 0.025f, center = Offset(cx - w * 0.06f, cy - h * 0.02f))
        drawCircle(color = Color(0xFF1E293B), radius = w * 0.025f, center = Offset(cx + w * 0.06f, cy - h * 0.02f))
        val smile = Path().apply {
            moveTo(cx - w * 0.05f, cy + h * 0.04f)
            quadraticTo(cx, cy + h * 0.08f, cx + w * 0.05f, cy + h * 0.04f)
        }
        drawPath(path = smile, color = Color(0xFF78350F), style = Stroke(width = 3f, cap = StrokeCap.Round))
    }
}

// =============================================================================
// 13. ADDITIONAL SWEET HELPERS: APPLE, CAT, COW, WATER, MOUNTAIN, TRAIN, STAR
// =============================================================================
@Composable
fun AppleCartoonGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Leaf
        val leaf = Path().apply {
            moveTo(w * 0.50f, h * 0.22f)
            quadraticTo(w * 0.72f, h * 0.12f, w * 0.76f, h * 0.24f)
            close()
        }
        drawPath(path = leaf, color = Color(0xFF22C55E))

        // Stem
        drawLine(
            color = Color(0xFF78350F),
            start = Offset(w * 0.50f, h * 0.26f),
            end = Offset(w * 0.48f, h * 0.14f),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )

        // Apple Red Body (two overlapping circles)
        drawCircle(color = Color(0xFFEF4444), radius = w * 0.28f, center = Offset(w * 0.40f, h * 0.54f))
        drawCircle(color = Color(0xFFEF4444), radius = w * 0.28f, center = Offset(w * 0.60f, h * 0.54f))
        drawCircle(color = Color.White.copy(alpha = 0.5f), radius = w * 0.06f, center = Offset(w * 0.36f, h * 0.42f))

        // Eyes
        drawCircle(color = Color(0xFF1E293B), radius = w * 0.035f, center = Offset(w * 0.44f, h * 0.54f))
        drawCircle(color = Color(0xFF1E293B), radius = w * 0.035f, center = Offset(w * 0.56f, h * 0.54f))
    }
}

@Composable
fun CatCartoonGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val catColor = Color(0xFFFB923C) // Orange tabby

        // Ears
        val leftEar = Path().apply {
            moveTo(w * 0.26f, h * 0.40f)
            lineTo(w * 0.24f, h * 0.18f)
            lineTo(w * 0.42f, h * 0.30f)
            close()
        }
        drawPath(path = leftEar, color = catColor)

        val rightEar = Path().apply {
            moveTo(w * 0.58f, h * 0.30f)
            lineTo(w * 0.76f, h * 0.18f)
            lineTo(w * 0.74f, h * 0.40f)
            close()
        }
        drawPath(path = rightEar, color = catColor)

        // Head
        drawCircle(color = catColor, radius = w * 0.28f, center = Offset(w * 0.50f, h * 0.50f))

        // Eyes
        drawCircle(color = Color(0xFF0F172A), radius = w * 0.04f, center = Offset(w * 0.40f, h * 0.46f))
        drawCircle(color = Color.White, radius = w * 0.015f, center = Offset(w * 0.385f, h * 0.445f))
        drawCircle(color = Color(0xFF0F172A), radius = w * 0.04f, center = Offset(w * 0.60f, h * 0.46f))
        drawCircle(color = Color.White, radius = w * 0.015f, center = Offset(w * 0.585f, h * 0.445f))

        // Pink Nose
        val nose = Path().apply {
            moveTo(w * 0.46f, h * 0.54f)
            lineTo(w * 0.54f, h * 0.54f)
            lineTo(w * 0.50f, h * 0.60f)
            close()
        }
        drawPath(path = nose, color = Color(0xFFF472B6))

        // Whiskers
        drawLine(Color(0xFF78350F), Offset(w * 0.24f, h * 0.54f), Offset(w * 0.36f, h * 0.55f), 3f)
        drawLine(Color(0xFF78350F), Offset(w * 0.24f, h * 0.62f), Offset(w * 0.36f, h * 0.60f), 3f)
        drawLine(Color(0xFF78350F), Offset(w * 0.76f, h * 0.54f), Offset(w * 0.64f, h * 0.55f), 3f)
        drawLine(Color(0xFF78350F), Offset(w * 0.76f, h * 0.62f), Offset(w * 0.64f, h * 0.60f), 3f)
    }
}

@Composable
fun CowCartoonGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Head
        drawCircle(color = Color(0xFFF8FAFC), radius = w * 0.28f, center = Offset(w * 0.50f, h * 0.46f))
        // Black spot on head
        drawOval(
            color = Color(0xFF1E293B),
            topLeft = Offset(w * 0.30f, h * 0.26f),
            size = Size(w * 0.22f, h * 0.20f)
        )

        // Pink Snout
        drawOval(
            color = Color(0xFFFBCFE8),
            topLeft = Offset(w * 0.32f, h * 0.52f),
            size = Size(w * 0.36f, h * 0.24f)
        )
        // Nostrils
        drawCircle(color = Color(0xFF78350F), radius = w * 0.03f, center = Offset(w * 0.44f, h * 0.64f))
        drawCircle(color = Color(0xFF78350F), radius = w * 0.03f, center = Offset(w * 0.56f, h * 0.64f))

        // Eyes
        drawCircle(color = Color(0xFF1E293B), radius = w * 0.035f, center = Offset(w * 0.42f, h * 0.42f))
        drawCircle(color = Color(0xFF1E293B), radius = w * 0.035f, center = Offset(w * 0.58f, h * 0.42f))
    }
}

@Composable
fun WaterCartoonGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Giant Cute Water Drop with Face
        val drop = Path().apply {
            moveTo(w * 0.50f, h * 0.16f)
            cubicTo(w * 0.78f, h * 0.48f, w * 0.82f, h * 0.78f, w * 0.50f, h * 0.84f)
            cubicTo(w * 0.18f, h * 0.78f, w * 0.22f, h * 0.48f, w * 0.50f, h * 0.16f)
            close()
        }
        drawPath(path = drop, color = Color(0xFF38BDF8))
        drawCircle(color = Color.White.copy(alpha = 0.6f), radius = w * 0.08f, center = Offset(w * 0.40f, h * 0.42f))

        // Eyes
        drawCircle(color = Color(0xFF0C4A6E), radius = w * 0.04f, center = Offset(w * 0.44f, h * 0.60f))
        drawCircle(color = Color(0xFF0C4A6E), radius = w * 0.04f, center = Offset(w * 0.56f, h * 0.60f))
    }
}

@Composable
fun MountainCartoonGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Back Mountain
        val backMtn = Path().apply {
            moveTo(w * 0.10f, h * 0.85f)
            lineTo(w * 0.40f, h * 0.28f)
            lineTo(w * 0.70f, h * 0.85f)
            close()
        }
        drawPath(path = backMtn, color = Color(0xFF0EA5E9))

        // Front Mountain
        val frontMtn = Path().apply {
            moveTo(w * 0.35f, h * 0.85f)
            lineTo(w * 0.65f, h * 0.38f)
            lineTo(w * 0.92f, h * 0.85f)
            close()
        }
        drawPath(path = frontMtn, color = Color(0xFF38BDF8))

        // Snow Caps
        val snow1 = Path().apply {
            moveTo(w * 0.32f, h * 0.44f)
            lineTo(w * 0.40f, h * 0.28f)
            lineTo(w * 0.48f, h * 0.44f)
            lineTo(w * 0.40f, h * 0.40f)
            close()
        }
        drawPath(path = snow1, color = Color.White)
    }
}

@Composable
fun TrainCartoonGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Train Engine Body
        drawRoundRect(
            color = Color(0xFF10B981),
            topLeft = Offset(w * 0.20f, h * 0.40f),
            size = Size(w * 0.60f, h * 0.35f),
            cornerRadius = CornerRadius(12f, 12f)
        )

        // Cabin
        drawRoundRect(
            color = Color(0xFF059669),
            topLeft = Offset(w * 0.20f, h * 0.26f),
            size = Size(w * 0.26f, h * 0.30f),
            cornerRadius = CornerRadius(8f, 8f)
        )

        // Yellow Window
        drawRoundRect(
            color = Color(0xFFFEF08A),
            topLeft = Offset(w * 0.24f, h * 0.30f),
            size = Size(w * 0.16f, h * 0.16f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // Smoke Stack
        drawRect(
            color = Color(0xFF374151),
            topLeft = Offset(w * 0.64f, h * 0.28f),
            size = Size(w * 0.10f, h * 0.14f)
        )

        // Wheels
        drawCircle(color = Color(0xFF1F2937), radius = w * 0.09f, center = Offset(w * 0.35f, h * 0.78f))
        drawCircle(color = Color(0xFFE5E7EB), radius = w * 0.04f, center = Offset(w * 0.35f, h * 0.78f))

        drawCircle(color = Color(0xFF1F2937), radius = w * 0.09f, center = Offset(w * 0.65f, h * 0.78f))
        drawCircle(color = Color(0xFFE5E7EB), radius = w * 0.04f, center = Offset(w * 0.65f, h * 0.78f))
    }
}

@Composable
fun CoinCartoonGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(color = Color(0xFFEAB308), radius = w * 0.36f, center = Offset(w * 0.5f, h * 0.5f))
        drawCircle(color = Color(0xFFFDE047), radius = w * 0.30f, center = Offset(w * 0.5f, h * 0.5f))
        drawCircle(color = Color(0xFFCA8A04), radius = w * 0.26f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = 3f))
        // Rupee Symbol bar
        drawLine(Color(0xFF854D0E), Offset(w * 0.40f, h * 0.44f), Offset(w * 0.60f, h * 0.44f), 5f, StrokeCap.Round)
        drawLine(Color(0xFF854D0E), Offset(w * 0.40f, h * 0.52f), Offset(w * 0.58f, h * 0.52f), 5f, StrokeCap.Round)
    }
}

@Composable
fun StarCartoonGraphic(modifier: Modifier = Modifier) {
    DefaultCuteStarGraphic(modifier = modifier)
}

@Composable
fun DefaultCuteStarGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.5f
        val starColor = Color(0xFFFBBF24)

        // 5-Pointed Star Path
        val path = Path()
        val outerRadius = w * 0.38f
        val innerRadius = w * 0.18f
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val angle = i * 3.14159f / 5f - 3.14159f / 2f
            val x = cx + cos(angle) * r
            val y = cy + sin(angle) * r
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path = path, color = starColor)

        // Cute Face inside star
        drawCircle(color = Color(0xFF1E293B), radius = w * 0.035f, center = Offset(cx - w * 0.08f, cy - h * 0.02f))
        drawCircle(color = Color(0xFF1E293B), radius = w * 0.035f, center = Offset(cx + w * 0.08f, cy - h * 0.02f))
        val smile = Path().apply {
            moveTo(cx - w * 0.05f, cy + h * 0.06f)
            quadraticTo(cx, cy + h * 0.10f, cx + w * 0.05f, cy + h * 0.06f)
        }
        drawPath(path = smile, color = Color(0xFF78350F), style = Stroke(width = 3f, cap = StrokeCap.Round))
    }
}

@Composable
private fun ShapeCircleGraphic(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(color = color.copy(alpha = 0.25f), radius = size.width * 0.36f)
        drawCircle(color = color, radius = size.width * 0.36f, style = Stroke(width = 8f))
    }
}

@Composable
private fun ShapeTriangleGraphic(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.85f, h * 0.85f)
            lineTo(w * 0.15f, h * 0.85f)
            close()
        }
        drawPath(path = path, color = color.copy(alpha = 0.25f))
        drawPath(path = path, color = color, style = Stroke(width = 8f, cap = StrokeCap.Round))
    }
}
