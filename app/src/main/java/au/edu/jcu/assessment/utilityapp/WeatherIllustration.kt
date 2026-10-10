package au.edu.jcu.assessment.utilityapp

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

// A decorative Canvas illustration; the adjacent text describes the weather.
@Composable
fun CuteWeatherIllustration(
    code: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        fun point(x: Float, y: Float) =
            Offset(w * x / 100f, h * y / 100f)

        val cocoa = Color(0xFF654832)
        val sunshine = Color(0xFFFFC568)
        val cloudCream = Color(0xFFFFF9EF)
        val cloudShadow = Color(0xFFE6CFB4)
        val rainBlue = Color(0xFF7CABB3)

        val clear = code == 0 || code == 1
        val partlyCloudy = code == 2
        val fog = code == 45 || code == 48
        val rain = code in listOf(
            51, 53, 55, 56, 57, 61, 63, 65,
            66, 67, 80, 81, 82
        )
        val snow = code in listOf(71, 73, 75, 77, 85, 86)
        val storm = code in listOf(95, 96, 99)

        // Sun and rounded rays.
        if (clear || partlyCloudy) {
            val sunCenter = if (clear) point(50f, 46f) else point(32f, 31f)
            val radius = w * if (clear) 0.24f else 0.17f

            for (index in 0 until 8) {
                val angle = Math.PI * index / 4.0
                val inner = radius + w * 0.055f
                val outer = radius + w * 0.11f
                drawLine(
                    color = sunshine,
                    start = Offset(
                        sunCenter.x + kotlin.math.cos(angle).toFloat() * inner,
                        sunCenter.y + kotlin.math.sin(angle).toFloat() * inner
                    ),
                    end = Offset(
                        sunCenter.x + kotlin.math.cos(angle).toFloat() * outer,
                        sunCenter.y + kotlin.math.sin(angle).toFloat() * outer
                    ),
                    strokeWidth = w * 0.045f,
                    cap = StrokeCap.Round
                )
            }
            drawCircle(sunshine, radius, sunCenter)

            if (clear) {
                drawCircle(cocoa, w * 0.018f, point(42f, 44f))
                drawCircle(cocoa, w * 0.018f, point(58f, 44f))
                drawArc(
                    color = cocoa,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = point(44f, 48f),
                    size = androidx.compose.ui.geometry.Size(
                        w * 0.12f, h * 0.08f
                    ),
                    style = Stroke(w * 0.02f, cap = StrokeCap.Round)
                )
            }
        }

        // Soft cloud with a little face.
        if (!clear) {
            drawRoundRect(
                color = cloudShadow,
                topLeft = point(17f, 46f),
                size = androidx.compose.ui.geometry.Size(
                    w * 0.70f, h * 0.25f
                ),
                cornerRadius = CornerRadius(w * 0.13f)
            )
            drawCircle(cloudCream, w * 0.18f, point(34f, 48f))
            drawCircle(cloudCream, w * 0.23f, point(54f, 39f))
            drawCircle(cloudCream, w * 0.17f, point(74f, 49f))
            drawRoundRect(
                color = cloudCream,
                topLeft = point(17f, 45f),
                size = androidx.compose.ui.geometry.Size(
                    w * 0.70f, h * 0.23f
                ),
                cornerRadius = CornerRadius(w * 0.12f)
            )
            drawCircle(cocoa, w * 0.016f, point(44f, 52f))
            drawCircle(cocoa, w * 0.016f, point(62f, 52f))
            drawArc(
                color = cocoa,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = point(48f, 55f),
                size = androidx.compose.ui.geometry.Size(
                    w * 0.10f, h * 0.06f
                ),
                style = Stroke(w * 0.018f, cap = StrokeCap.Round)
            )
        }

        if (rain) {
            listOf(34f, 54f, 74f).forEach { x ->
                drawLine(
                    color = rainBlue,
                    start = point(x, 77f),
                    end = point(x - 4f, 89f),
                    strokeWidth = w * 0.04f,
                    cap = StrokeCap.Round
                )
            }
        }

        if (snow) {
            listOf(34f, 54f, 74f).forEach { x ->
                drawCircle(rainBlue, w * 0.035f, point(x, 83f))
            }
        }

        if (fog) {
            listOf(78f, 88f).forEach { y ->
                drawLine(
                    color = cloudShadow,
                    start = point(27f, y),
                    end = point(77f, y),
                    strokeWidth = w * 0.04f,
                    cap = StrokeCap.Round
                )
            }
        }

        if (storm) {
            val lightning = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.55f, h * 0.71f)
                lineTo(w * 0.43f, h * 0.84f)
                lineTo(w * 0.54f, h * 0.84f)
                lineTo(w * 0.47f, h * 0.98f)
                lineTo(w * 0.69f, h * 0.79f)
                lineTo(w * 0.57f, h * 0.79f)
                close()
            }
            drawPath(lightning, sunshine)
        }
    }
}