package com.cgens67.avidtune.ui.screens.insight

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cgens67.avidtune.R
import com.cgens67.avidtune.db.entities.Album
import com.cgens67.avidtune.db.entities.Artist
import com.cgens67.avidtune.db.entities.SongWithStats
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

val bbh_bartle = FontFamily.Default

object WrappedConstants {
    val YEAR = Calendar.getInstance().get(Calendar.YEAR)
    val PLAYLIST_NAME = "AvidTune Insight $YEAR"
}

data class MessagePair(val range: LongRange, val tease: String, val reveal: String)

object WrappedRepository {
    private val messages = listOf(
        MessagePair(0L..999L, "I really hope you are not dissapointed...", "That's **%d minutes**. Just warming up?"),
        MessagePair(1000L..4999L, "It seems like you found us recently...", "And you dedicated **%d minutes** to the tunes."),
        MessagePair(5000L..14999L, "Music is definitely your thing.", "**%d minutes** is a solid soundtrack for your year."),
        MessagePair(15000L..39999L, "Do you ever take your headphones off?", "**%d minutes** suggests music is your oxygen."),
        MessagePair(40000L..Long.MAX_VALUE, "Are you... okay?", "You literally lived here for **%d minutes**.")
    )

    fun getMessage(minutes: Long): MessagePair {
        val possibleMessages = messages.filter { minutes in it.range }
        val chosenMessage = if (possibleMessages.isNotEmpty()) possibleMessages.random() 
        else MessagePair(0L..Long.MAX_VALUE, "Looks like we lost count!", "But you definitely listened to **%d minutes** of music.")
        
        return chosenMessage.copy(reveal = chosenMessage.reveal.format(minutes))
    }
}

enum class ShapeType { Circle, Rect, Line }

private data class AnimatedElement(
    val shapeType: ShapeType,
    val initialX: Float, val initialY: Float,
    val targetX: Float, val targetY: Float,
    val size: Float, val alpha: Float, val duration: Int
)

@Composable
fun AnimatedBackground(elementCount: Int = 20, shapeTypes: List<ShapeType> = listOf(ShapeType.Circle)) {
    val random = remember { Random(System.currentTimeMillis()) }
    val elements = remember {
        List(elementCount) {
            val shapeType = shapeTypes.random(random)
            AnimatedElement(
                shapeType = shapeType,
                initialX = random.nextFloat(), initialY = random.nextFloat(),
                targetX = random.nextFloat(), targetY = random.nextFloat(),
                size = if (shapeType == ShapeType.Circle) random.nextFloat() * 15f + 5f else random.nextFloat() * 50f + 10f,
                alpha = random.nextFloat() * 0.3f + 0.1f,
                duration = random.nextInt(4000, 10000)
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition()
    val progressAnims = elements.map {
        infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(it.duration, easing = LinearEasing), RepeatMode.Reverse)
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        elements.forEachIndexed { index, element ->
            val progress = progressAnims[index].value
            val currentX = element.initialX + (element.targetX - element.initialX) * progress
            val currentY = element.initialY + (element.targetY - element.initialY) * progress

            when (element.shapeType) {
                ShapeType.Circle -> drawCircle(Color.White.copy(alpha = element.alpha), element.size, Offset(currentX * size.width, currentY * size.height))
                ShapeType.Rect -> drawRect(Color.White.copy(alpha = element.alpha), Offset(currentX * size.width, currentY * size.height), Size(element.size, element.size))
                ShapeType.Line -> drawLine(Color.White.copy(alpha = element.alpha), Offset(currentX * size.width, currentY * size.height), Offset((currentX + 0.1f) * size.width, (currentY + 0.1f) * size.height), 2f)
            }
        }
    }
}

@Composable
fun WrappedBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit = {}) {
    val infiniteTransition = rememberInfiniteTransition()
    val blob1Offset by infiniteTransition.animateFloat(0f, 2f * PI.toFloat(), infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Restart))
    val blob2Offset by infiniteTransition.animateFloat(0f, 2f * PI.toFloat(), infiniteRepeatable(tween(18000, easing = LinearEasing), RepeatMode.Restart))
    val scale by infiniteTransition.animateFloat(1f, 1.2f, infiniteRepeatable(tween(8000, easing = FastOutSlowInEasing), RepeatMode.Reverse))

    val blob1Colors = remember { listOf(Color(0xFF7C3AED).copy(alpha = 0.4f), Color.Transparent) }
    val blob2Colors = remember { listOf(Color(0xFF06B6D4).copy(alpha = 0.3f), Color.Transparent) }
    val blob3Colors = remember { listOf(Color(0xFFDB2777).copy(alpha = 0.2f), Color.Transparent) }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Color(0xFF0F0620))) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        val dotPoints = remember(widthPx, heightPx) {
            val points = ArrayList<Offset>()
            if (widthPx > 0 && heightPx > 0) {
                val dotSpacing = 30f
                for (x in 0..(widthPx / dotSpacing).toInt()) {
                    for (y in 0..(heightPx / dotSpacing).toInt()) {
                        points.add(Offset(x * dotSpacing, y * dotSpacing))
                    }
                }
            }
            points
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val b1X = width * 0.3f + sin(blob1Offset) * width * 0.2f
            val b1Y = height * 0.2f + cos(blob1Offset) * height * 0.1f
            drawCircle(Brush.radialGradient(blob1Colors, Offset(b1X, b1Y), width * 0.8f * scale), width * 0.8f * scale, Offset(b1X, b1Y))

            val b2X = width * 0.7f + cos(blob2Offset) * width * 0.2f
            val b2Y = height * 0.8f + sin(blob2Offset) * height * 0.1f
            drawCircle(Brush.radialGradient(blob2Colors, Offset(b2X, b2Y), width * 0.9f * scale), width * 0.9f * scale, Offset(b2X, b2Y))

            drawCircle(Brush.radialGradient(blob3Colors, Offset(width * 0.1f, height * 0.9f), width * 0.6f), width * 0.6f, Offset(width * 0.1f, height * 0.9f))

            if (dotPoints.isNotEmpty()) drawPoints(dotPoints, PointMode.Points, Color.White.copy(alpha = 0.05f), 3f, StrokeCap.Round)
        }
        Box(modifier = Modifier.fillMaxSize()) { content() }
    }
}

@Composable
fun AnimatedDecorativeElement(modifier: Modifier = Modifier, isVisible: Boolean) {
    val rotation = remember { Animatable(0f) }
    val shapeType = remember { Random.nextInt(3) }
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(Random.nextLong(500))
            rotation.animateTo(360f, infiniteRepeatable(tween(Random.nextInt(1000, 3000)), RepeatMode.Restart))
        }
    }
    Canvas(modifier.graphicsLayer { rotationZ = rotation.value }) {
        val strokeWidth = 2.dp.toPx()
        when (shapeType) {
            0 -> drawArc(Color.White.copy(0.2f), 0f, 90f, false, style = Stroke(strokeWidth))
            1 -> drawCircle(Color.White.copy(0.2f), style = Stroke(strokeWidth))
            2 -> drawRect(Color.White.copy(0.2f), style = Stroke(strokeWidth))
        }
    }
}

@Composable
fun AutoResizingText(text: String, modifier: Modifier = Modifier, style: TextStyle) {
    var scaledTextStyle by remember { mutableStateOf(style) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text, style = scaledTextStyle, maxLines = 1, softWrap = false,
        modifier = modifier.drawWithContent { if (readyToDraw) drawContent() },
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth) scaledTextStyle = scaledTextStyle.copy(fontSize = scaledTextStyle.fontSize * 0.9)
            else readyToDraw = true
        }
    )
}

@Composable
fun FormattedText(text: String, modifier: Modifier = Modifier, style: TextStyle) {
    val annotatedString = buildAnnotatedString {
        val parts = text.split("(?=\\*\\*)|(?<=\\*\\*)".toRegex())
        var isBold = false
        for (part in parts) {
            if (part == "**") isBold = !isBold
            else withStyle(SpanStyle(fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)) { append(part) }
        }
    }
    Text(annotatedString, modifier, style = style)
}

// Pages
@Composable
fun WrappedIntro(onNext: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(200); visible = true }

    WrappedBackground(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize(), Alignment.CenterHorizontally, Arrangement.Center) {
            AnimatedVisibility(visible, enter = fadeIn(tween(1000, 200)) + slideInVertically(tween(1000, 200))) {
                Image(painterResource(R.drawable.avidtune), null, Modifier.size(100.dp).clip(CircleShape))
            }
            Spacer(Modifier.height(16.dp))
            AnimatedVisibility(visible, enter = fadeIn(tween(1000, 400)) + slideInVertically(tween(1000, 400))) {
                val baseStyle = TextStyle(fontFamily = bbh_bartle, textAlign = TextAlign.Center, letterSpacing = 2.sp, fontSize = 50.sp)
                Box {
                    AutoResizingText("AvidTune Insight", Modifier.padding(start = 2.dp, top = 2.dp), baseStyle.copy(color = Color.DarkGray))
                    AutoResizingText("AvidTune Insight", Modifier.padding(start = 1.dp, top = 1.dp), baseStyle.copy(color = Color.Gray))
                    AutoResizingText("AvidTune Insight", Modifier, baseStyle.copy(color = Color.White))
                }
            }
            Spacer(Modifier.height(8.dp))
            AnimatedVisibility(visible, enter = fadeIn(tween(1000, 600)) + slideInVertically(tween(1000, 600))) {
                Text("A look back at your year in music.", color = Color.White, fontSize = 16.sp, textAlign = TextAlign.Center)
            }
        }
        AnimatedVisibility(visible, enter = fadeIn(tween(1000, 1000)) + slideInVertically(tween(1000, 1000)), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp)) {
            Button(onClick = onNext, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(Color.White)) {
                Text("Let's go!", color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
            }
        }
    }
}

@Composable
fun WrappedMinutesTease(messagePair: MessagePair?, onNavigateForward: () -> Unit, isDataReady: Boolean) {
    LaunchedEffect(Unit) { delay(3500); onNavigateForward() }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(messagePair != null && isDataReady, enter = fadeIn(tween(1000)) + scaleIn(initialScale = 0.9f, animationSpec = tween(1000))) {
            Text(messagePair?.tease ?: "", Modifier.padding(horizontal = 24.dp), Color.White, fontSize = 30.sp, textAlign = TextAlign.Center, fontFamily = bbh_bartle)
        }
    }
}

@Composable
fun WrappedMinutesScreen(messagePair: MessagePair?, totalMinutes: Long, isVisible: Boolean) {
    val animatedMinutes = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()
    LaunchedEffect(isVisible, totalMinutes) {
        if (isVisible && totalMinutes > 0) animatedMinutes.animateTo(totalMinutes.toFloat(), tween(1500, easing = FastOutSlowInEasing))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(vertical = 32.dp), Alignment.CenterHorizontally, Arrangement.Center) {
            FormattedText(messagePair?.tease ?: "", Modifier.padding(horizontal = 24.dp), MaterialTheme.typography.headlineSmall.copy(Color.White, textAlign = TextAlign.Center))
            Spacer(Modifier.height(32.dp))
            BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                val density = LocalDensity.current
                val baseStyle = MaterialTheme.typography.displayLarge.copy(Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontFamily = bbh_bartle, drawStyle = Stroke(with(density) { 2.dp.toPx() }))
                val textStyle = remember(totalMinutes, maxWidth) {
                    var style = baseStyle.copy(fontSize = 96.sp)
                    var textWidth = textMeasurer.measure(totalMinutes.toString(), style).size.width
                    while (textWidth > constraints.maxWidth) { style = style.copy(fontSize = style.fontSize * 0.95f); textWidth = textMeasurer.measure(totalMinutes.toString(), style).size.width }
                    style.copy(lineHeight = style.fontSize * 1.08f)
                }
                Text(animatedMinutes.value.toInt().toString(), style = textStyle, maxLines = 1, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(16.dp))
            FormattedText(messagePair?.reveal ?: "", Modifier.padding(horizontal = 24.dp), MaterialTheme.typography.bodyLarge.copy(Color.White.copy(0.8f), textAlign = TextAlign.Center))
        }
    }
}

@Composable
fun WrappedTotalSongsScreen(uniqueSongCount: Int, isVisible: Boolean) {
    val animatedSongs = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()
    LaunchedEffect(isVisible, uniqueSongCount) {
        if (isVisible && uniqueSongCount > 0) animatedSongs.animateTo(uniqueSongCount.toFloat(), tween(1500, easing = FastOutSlowInEasing))
    }
    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(shapeTypes = listOf(ShapeType.Line))
        Column(Modifier.fillMaxSize().padding(vertical = 32.dp), Alignment.CenterHorizontally, Arrangement.Center) {
            Text("Songs you\nlistened to", Modifier.padding(horizontal = 24.dp), MaterialTheme.typography.headlineSmall.copy(Color.White, textAlign = TextAlign.Center))
            Spacer(Modifier.height(32.dp))
            BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                val density = LocalDensity.current
                val baseStyle = MaterialTheme.typography.displayLarge.copy(Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontFamily = bbh_bartle, drawStyle = Stroke(with(density) { 2.dp.toPx() }))
                val textStyle = remember(uniqueSongCount, maxWidth) {
                    var style = baseStyle.copy(fontSize = 96.sp)
                    var textWidth = textMeasurer.measure(uniqueSongCount.toString(), style).size.width
                    while (textWidth > constraints.maxWidth) { style = style.copy(fontSize = style.fontSize * 0.95f); textWidth = textMeasurer.measure(uniqueSongCount.toString(), style).size.width }
                    style.copy(lineHeight = style.fontSize * 1.08f)
                }
                Text(animatedSongs.value.toInt().toString(), style = textStyle, maxLines = 1, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(16.dp))
            Text("That's a lot of melodies.", Modifier.padding(horizontal = 24.dp), MaterialTheme.typography.bodyLarge.copy(Color.White.copy(0.8f), textAlign = TextAlign.Center))
        }
    }
}

@Composable
fun WrappedTopSongScreen(topSong: SongWithStats?, isVisible: Boolean) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(isVisible) { if (isVisible) { delay(200); visible = true } }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(32.dp), Alignment.CenterHorizontally, Arrangement.Center) {
            AnimatedVisibility(visible, enter = fadeIn(tween(1000, 200)) + slideInVertically(tween(1000, 200))) {
                Text("But your absolute\nfavorite was...", MaterialTheme.typography.headlineSmall.copy(color = Color.White), textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(32.dp))
            AnimatedVisibility(visible, enter = fadeIn(tween(1000, 400)) + slideInVertically(tween(1000, 400))) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(topSong?.thumbnailUrl).build(),
                    contentDescription = null, modifier = Modifier.size(200.dp).clip(RoundedCornerShape(3.dp)), contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.height(16.dp))
            AnimatedVisibility(visible, enter = fadeIn(tween(1000, 600)) + slideInVertically(tween(1000, 600))) {
                Text(topSong?.title ?: "No Data", MaterialTheme.typography.headlineMedium.copy(color = Color.White), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(8.dp))
            AnimatedVisibility(visible, enter = fadeIn(tween(1000, 1000)) + slideInVertically(tween(1000, 1000))) {
                Text("Listened for ${(topSong?.timeListened ?: 0) / 60000} minutes", MaterialTheme.typography.bodyLarge.copy(color = Color.White.copy(0.8f)), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun WrappedTop5SongsScreen(topSongs: List<SongWithStats>, isVisible: Boolean) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(isVisible) { if (isVisible) { delay(200); visible = true } }
    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(25, listOf(ShapeType.Rect))
        Column(Modifier.fillMaxSize().padding(32.dp), Alignment.CenterHorizontally, Arrangement.Center) {
            AnimatedVisibility(visible, enter = fadeIn(tween(1000, 200)) + slideInVertically(tween(1000, 200))) {
                Text("Your Top 5 Songs", fontSize = 40.sp, fontFamily = bbh_bartle, color = Color.White, textAlign = TextAlign.Center, lineHeight = 44.sp)
            }
            Spacer(Modifier.height(32.dp))
            Column {
                topSongs.forEachIndexed { index, song ->
                    AnimatedVisibility(visible, enter = fadeIn(tween(600, 400 + (index * 200))) + slideInVertically(tween(600, 400 + (index * 200)))) {
                        Row(Modifier.padding(vertical = 8.dp), Alignment.CenterVertically) {
                            Text("${index + 1}", fontFamily = bbh_bartle, fontSize = 36.sp, color = Color.White.copy(0.8f), modifier = Modifier.width(40.dp))
                            Spacer(Modifier.width(16.dp))
                            AsyncImage(model = song.thumbnailUrl, contentDescription = null, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(3.dp)), contentScale = ContentScale.Crop)
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(song.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WrappedTotalAlbumsScreen(uniqueAlbumCount: Int, isVisible: Boolean) {
    val animatedAlbums = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(isVisible, uniqueAlbumCount) {
        if (isVisible) { visible = true; if (uniqueAlbumCount > 0) animatedAlbums.animateTo(uniqueAlbumCount.toFloat(), tween(1500, easing = FastOutSlowInEasing)) }
    }
    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(shapeTypes = listOf(ShapeType.Circle))
        Column(Modifier.fillMaxSize().padding(vertical = 32.dp), Alignment.CenterHorizontally, Arrangement.Center) {
            AnimatedVisibility(visible, enter = fadeIn(tween(1000, 200)) + slideInVertically(tween(1000, 200))) {
                Text("Albums you\ndiscovered", Modifier.padding(horizontal = 24.dp), MaterialTheme.typography.headlineSmall.copy(Color.White, textAlign = TextAlign.Center))
            }
            Spacer(Modifier.height(32.dp))
            BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                val density = LocalDensity.current
                val baseStyle = MaterialTheme.typography.displayLarge.copy(Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontFamily = bbh_bartle, drawStyle = Stroke(with(density) { 2.dp.toPx() }))
                val textStyle = remember(uniqueAlbumCount, maxWidth) {
                    var style = baseStyle.copy(fontSize = 96.sp)
                    var textWidth = textMeasurer.measure(uniqueAlbumCount.toString(), style).size.width
                    while (textWidth > constraints.maxWidth) { style = style.copy(fontSize = style.fontSize * 0.95f); textWidth = textMeasurer.measure(uniqueAlbumCount.toString(), style).size.width }
                    style.copy(lineHeight = style.fontSize * 1.08f)
                }
                Text(animatedAlbums.value.toInt().toString(), style = textStyle, maxLines = 1, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(16.dp))
            AnimatedVisibility(visible, enter = fadeIn(tween(1000, 600)) + slideInVertically(tween(1000, 600))) {
                Text("That's a lot of skips and repeats.", Modifier.padding(horizontal = 24.dp), MaterialTheme.typography.bodyLarge.copy(Color.White.copy(0.8f), textAlign = TextAlign.Center))
            }
        }
    }
}

@Composable
fun WrappedTopAlbumScreen(topAlbum: Album?, isVisible: Boolean) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(isVisible) { if (isVisible) visible = true }
    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(shapeTypes = listOf(ShapeType.Rect))
        Column(Modifier.fillMaxSize().padding(32.dp), Alignment.CenterHorizontally, Arrangement.Center) {
            AnimatedVisibility(visible, enter = fadeIn(tween(1000, 200)) + slideInVertically(tween(1000, 200))) {
                Text("But there was one album\nthat ruled them all.", TextStyle(fontFamily = bbh_bartle, fontSize = 40.sp, color = Color.White, textAlign = TextAlign.Center, lineHeight = 48.sp))
            }
            Spacer(Modifier.height(32.dp))
            AnimatedVisibility(visible, enter = fadeIn(tween(1000, 400)) + slideInVertically(tween(1000, 400))) {
                AsyncImage(model = topAlbum?.thumbnailUrl, contentDescription = null, modifier = Modifier.size(200.dp).clip(RoundedCornerShape(3.dp)), contentScale = ContentScale.Crop)
            }
            Spacer(Modifier.height(16.dp))
            AnimatedVisibility(visible, enter = fadeIn(tween(1000, 600)) + slideInVertically(tween(1000, 600))) {
                Text(topAlbum?.title ?: "No Data", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(8.dp))
            AnimatedVisibility(visible, enter = fadeIn(tween(1000, 800)) + slideInVertically(tween(1000, 800))) {
                Text("Listened for ${(topAlbum?.timeListened ?: 0) / 60000} minutes", fontSize = 16.sp, color = Color.White.copy(0.8f), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun WrappedTop5AlbumsScreen(topAlbums: List<Album>, isVisible: Boolean) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(isVisible) { if (isVisible) { delay(200); visible = true } }
    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(shapeTypes = listOf(ShapeType.Circle))
        Column(Modifier.fillMaxSize().padding(32.dp), Alignment.CenterHorizontally, Arrangement.Center) {
            AnimatedVisibility(visible, enter = fadeIn(tween(1000, 200)) + slideInVertically(tween(1000, 200))) {
                Text("Your Top 5\nAlbums", TextStyle(fontFamily = bbh_bartle, fontSize = 48.sp, color = Color.White, textAlign = TextAlign.Center, lineHeight = 56.sp))
            }
            Spacer(Modifier.height(32.dp))
            Column {
                topAlbums.forEachIndexed { index, album ->
                    AnimatedVisibility(visible, enter = fadeIn(tween(600, 400 + (index * 200))) + slideInVertically(tween(600, 400 + (index * 200)))) {
                        Row(Modifier.padding(vertical = 8.dp), Alignment.CenterVertically) {
                            Text("${index + 1}", fontFamily = bbh_bartle, fontSize = 36.sp, color = Color.White.copy(0.8f), modifier = Modifier.width(40.dp))
                            Spacer(Modifier.width(16.dp))
                            AsyncImage(model = album.thumbnailUrl, contentDescription = null, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(3.dp)), contentScale = ContentScale.Crop)
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(album.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                                Text("${(album.timeListened ?: 0) / 60000} mins", color = Color.White.copy(0.7f), fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WrappedTotalArtistsScreen(uniqueArtistCount: Int, isVisible: Boolean) {
    val animatedArtists = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()
    LaunchedEffect(isVisible, uniqueArtistCount) {
        if (isVisible && uniqueArtistCount > 0) animatedArtists.animateTo(uniqueArtistCount.toFloat(), tween(1500, easing = FastOutSlowInEasing))
    }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(vertical = 32.dp), Alignment.CenterHorizontally, Arrangement.Center) {
            Text("Artists you\nlistened to", Modifier.padding(horizontal = 24.dp), MaterialTheme.typography.headlineSmall.copy(Color.White, textAlign = TextAlign.Center))
            Spacer(Modifier.height(32.dp))
            BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                val density = LocalDensity.current
                val baseStyle = MaterialTheme.typography.displayLarge.copy(Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontFamily = bbh_bartle, drawStyle = Stroke(with(density) { 2.dp.toPx() }))
                val textStyle = remember(uniqueArtistCount, maxWidth) {
                    var style = baseStyle.copy(fontSize = 96.sp)
                    var textWidth = textMeasurer.measure(uniqueArtistCount.toString(), style).size.width
                    while (textWidth > constraints.maxWidth) { style = style.copy(fontSize = style.fontSize * 0.95f); textWidth = textMeasurer.measure(uniqueArtistCount.toString(), style).size.width }
                    style.copy(lineHeight = style.fontSize * 1.08f)
                }
                Text(animatedArtists.value.toInt().toString(), style = textStyle, maxLines = 1, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(16.dp))
            Text("Your music taste is quite diverse.", Modifier.padding(horizontal = 24.dp), MaterialTheme.typography.bodyLarge.copy(Color.White.copy(0.8f), textAlign = TextAlign.Center))
        }
    }
}

@Composable
fun WrappedTopArtistScreen(topArtist: Artist?, isVisible: Boolean) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(isVisible) { if (isVisible) visible = true }
    Column(Modifier.fillMaxSize().padding(32.dp), Alignment.CenterHorizontally, Arrangement.Center) {
        AnimatedVisibility(visible, enter = fadeIn(tween(1000, 200)) + slideInVertically(tween(1000, 200))) {
            Text("Your Top Artist", MaterialTheme.typography.headlineSmall.copy(Color.White), textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(32.dp))
        AnimatedVisibility(visible, enter = fadeIn(tween(1000, 400)) + slideInVertically(tween(1000, 400))) {
            AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(topArtist?.artist?.thumbnailUrl).build(), contentDescription = null, modifier = Modifier.size(200.dp).clip(CircleShape), contentScale = ContentScale.Crop)
        }
        Spacer(Modifier.height(16.dp))
        AnimatedVisibility(visible, enter = fadeIn(tween(1000, 600)) + slideInVertically(tween(1000, 600))) {
            Text(topArtist?.artist?.name ?: "No Data", MaterialTheme.typography.headlineMedium.copy(Color.White), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
        AnimatedVisibility(visible, enter = fadeIn(tween(1000, 800)) + slideInVertically(tween(1000, 800))) {
            Text("Listened for ${(topArtist?.timeListened ?: 0) / 60000} minutes", MaterialTheme.typography.bodyLarge.copy(Color.White.copy(0.8f)), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun WrappedTop5ArtistsScreen(topArtists: List<Artist>, isVisible: Boolean) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(isVisible) { if (isVisible) { delay(200); visible = true } }
    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(15, listOf(ShapeType.Line))
        Column(Modifier.fillMaxSize().padding(32.dp), Alignment.CenterHorizontally, Arrangement.Center) {
            AnimatedVisibility(visible, enter = fadeIn(tween(1000, 200)) + slideInVertically(tween(1000, 200))) {
                Text("Top 5 Artists", fontSize = 40.sp, fontFamily = bbh_bartle, color = Color.White, textAlign = TextAlign.Center, lineHeight = 44.sp)
            }
            Spacer(Modifier.height(32.dp))
            Column {
                topArtists.forEachIndexed { index, artist ->
                    AnimatedVisibility(visible, enter = fadeIn(tween(600, 400 + (index * 200))) + slideInVertically(tween(600, 400 + (index * 200)))) {
                        Row(Modifier.padding(vertical = 8.dp), Alignment.CenterVertically) {
                            Text("${index + 1}", fontFamily = bbh_bartle, fontSize = 36.sp, color = Color.White.copy(0.8f), modifier = Modifier.width(40.dp))
                            Spacer(Modifier.width(16.dp))
                            AsyncImage(model = artist.artist.thumbnailUrl, contentDescription = null, modifier = Modifier.size(64.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(artist.artist.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("${(artist.timeListened ?: 0) / 60000} mins", color = Color.White.copy(0.7f), fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistPage(state: WrappedState, onCreatePlaylist: () -> Unit) {
    val playlistCreationState = state.playlistCreationState
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(200); startAnimation = true }
    val contentAlpha by animateFloatAsState(if (startAnimation) 1f else 0f, tween(800, 200))

    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(shapeTypes = listOf(ShapeType.Circle))
        Column(Modifier.fillMaxSize().padding(32.dp).alpha(contentAlpha), Arrangement.Center, Alignment.CenterHorizontally) {
            AutoResizingText("Your Insight Playlist\nis ready.", TextStyle(fontFamily = bbh_bartle, fontSize = 40.sp, color = Color.White, textAlign = TextAlign.Center, lineHeight = 48.sp))
            Spacer(Modifier.height(32.dp))
            Image(painterResource(R.drawable.previewalbum), null, Modifier.size(256.dp).clip(RoundedCornerShape(3.dp)))
            Spacer(Modifier.height(24.dp))
            Text("AvidTune Insight ${WrappedConstants.YEAR}", TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White))
            Spacer(Modifier.height(48.dp))
            Button(
                onClick = { if (playlistCreationState == PlaylistCreationState.Idle) onCreatePlaylist() },
                shape = CircleShape, colors = ButtonDefaults.buttonColors(Color.White), modifier = Modifier.height(50.dp)
            ) {
                when (playlistCreationState) {
                    is PlaylistCreationState.Idle -> Text("Add to Library", TextStyle(color = Color.Black, fontWeight = FontWeight.Bold))
                    is PlaylistCreationState.Creating -> CircularProgressIndicator(Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.dp)
                    is PlaylistCreationState.Success -> Text("Saved!", TextStyle(color = Color.Black, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
fun ConclusionPage(onClose: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(30, listOf(ShapeType.Circle, ShapeType.Line))
        Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.avidtune), null, Modifier.size(96.dp).clip(CircleShape))
            Spacer(Modifier.height(24.dp))
            Text("Thank you for listening", TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White))
            Spacer(Modifier.height(8.dp))
            Text("See you next time!", TextStyle(fontSize = 16.sp, color = Color.Gray))
            Spacer(Modifier.height(48.dp))
            Button(onClick = onClose, shape = CircleShape, colors = ButtonDefaults.buttonColors(Color.White)) {
                Text("Close Insight", TextStyle(color = Color.Black, fontWeight = FontWeight.Bold))
            }
        }
    }
}
