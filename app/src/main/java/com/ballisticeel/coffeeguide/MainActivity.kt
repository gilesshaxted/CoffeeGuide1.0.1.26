package com.ballisticeel.coffeeguide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// --- MAIN ACTIVITY ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                CoffeeApp()
            }
        }
    }
}

// --- DATA MODELS ---

data class CoffeeDrink(
    val name: String,
    val description: String,
    val keyPoints: List<String>,
    val steps: List<String>, // NEW: Preparation Steps
    val sizeOz: String,
    val sizeMl: String,
    val caffeineMg: Int, // NEW: Caffeine Content
    val brewTimeSeconds: Int, // NEW: Timer Duration
    // Ratios (0.0 to 1.0)
    val espressoRatio: Float,
    val milkRatio: Float,
    val foamRatio: Float,
    val waterRatio: Float = 0f,
    val creamRatio: Float = 0f
)

// --- DATA SOURCE (Updated with Steps, Time, Caffeine) ---
val coffeeMenu = listOf(
    CoffeeDrink(
        "Espresso",
        "The foundation of most coffee drinks. Pure, intense coffee flavor extracted at high pressure.",
        listOf("Intense flavor", "Crema on top", "Served immediately"),
        listOf("Grind 18-20g of fine coffee.", "Tamp evenly with 30lbs pressure.", "Extract for 25-30 seconds.", "Serve immediately."),
        "1 oz", "30 ml", 64, 30,
        espressoRatio = 1f, milkRatio = 0f, foamRatio = 0f
    ),
    CoffeeDrink(
        "Ristretto",
        "A 'short' shot. Less water is pushed through the coffee, resulting in a sweeter, more concentrated flavor.",
        listOf("Sweeter than espresso", "Less caffeine", "Very little volume"),
        listOf("Grind fine coffee.", "Stop extraction early (15-20 seconds).", "Aim for 1:1 ratio (coffee in to liquid out)."),
        "0.75 oz", "22 ml", 50, 20,
        espressoRatio = 0.8f, milkRatio = 0f, foamRatio = 0f
    ),
    CoffeeDrink(
        "Lungo",
        "A 'long' shot. More water is pushed through, resulting in a bitterer, higher caffeine shot.",
        listOf("More volume", "More bitter notes", "Higher caffeine"),
        listOf("Grind slightly coarser than espresso.", "Extract for 40-50 seconds.", "Allow more water to pass through."),
        "2 oz", "60 ml", 80, 50,
        espressoRatio = 1f, milkRatio = 0f, foamRatio = 0f, waterRatio = 0.2f
    ),
    CoffeeDrink(
        "Macchiato",
        "Espresso 'stained' with a tiny dollop of foam. The milk cuts the acidity slightly.",
        listOf("Mostly espresso", "Dash of foam", "Strong hit"),
        listOf("Pull a double shot of espresso.", "Steam a small amount of milk.", "Spoon a dollop of foam onto the center."),
        "1.5 oz", "45 ml", 64, 45,
        espressoRatio = 0.8f, milkRatio = 0f, foamRatio = 0.2f
    ),
    CoffeeDrink(
        "Cortado",
        "Equal parts espresso and warm steamed milk. The milk reduces acidity without hiding the coffee flavor.",
        listOf("1:1 Ratio", "Smooth texture", "No thick foam"),
        listOf("Pull a double shot (2oz).", "Steam milk with very little air (no foam).", "Pour equal amount of milk into espresso."),
        "4 oz", "120 ml", 128, 60,
        espressoRatio = 0.5f, milkRatio = 0.5f, foamRatio = 0f
    ),
    CoffeeDrink(
        "Flat White",
        "Similar to a latte but with less volume and very thin 'microfoam'. Very velvety texture.",
        listOf("Silky texture", "Double shot espresso", "Microfoam (no stiff peaks)"),
        listOf("Pull a double ristretto or espresso.", "Steam milk to create microfoam (wet paint texture).", "Pour aiming for a thin layer of foam on top."),
        "6 oz", "180 ml", 128, 90,
        espressoRatio = 0.33f, milkRatio = 0.60f, foamRatio = 0.07f
    ),
    CoffeeDrink(
        "Cappuccino",
        "The classic thirds rule. Equal parts espresso, steamed milk, and thick foam.",
        listOf("1:1:1 Ratio", "Thick airy foam", "Chocolate powder optional"),
        listOf("Pull a double shot of espresso.", "Steam milk to 65°C creating thick foam.", "Pour espresso, then milk, then spoon foam on top."),
        "6 oz", "180 ml", 64, 120,
        espressoRatio = 0.33f, milkRatio = 0.33f, foamRatio = 0.33f
    ),
    CoffeeDrink(
        "Latte",
        "Espresso with a lot of steamed milk and a thin layer of foam on top. Milky and approachable.",
        listOf("Milky and sweet", "Thin foam layer", "Great for latte art"),
        listOf("Pull a single or double shot.", "Steam milk to 65°C / 150°F.", "Pour milk holding pitcher high, then lower to finish."),
        "10 oz", "300 ml", 64, 120,
        espressoRatio = 0.15f, milkRatio = 0.75f, foamRatio = 0.1f
    ),
    CoffeeDrink(
        "Mocha",
        "A latte with added chocolate syrup or powder mixed with the espresso.",
        listOf("Chocolate flavor", "Dessert-like", "Often topped with whipped cream"),
        listOf("Mix chocolate syrup with fresh espresso.", "Steam milk as you would for a latte.", "Pour milk and top with optional cream."),
        "10 oz", "300 ml", 64, 180,
        espressoRatio = 0.15f, milkRatio = 0.70f, foamRatio = 0.15f
    ),
    CoffeeDrink(
        "Americano",
        "Espresso diluted with hot water. Similar strength to drip coffee but different flavor profile.",
        listOf("Diluted espresso", "Retains crema", "Black coffee alternative"),
        listOf("Fill cup 2/3 with hot water (not boiling).", "Pull a double shot of espresso.", "Pour espresso *over* the water to keep crema."),
        "12 oz", "350 ml", 128, 60,
        espressoRatio = 0.2f, milkRatio = 0f, foamRatio = 0f, waterRatio = 0.8f
    ),
    CoffeeDrink(
        "Latte Macchiato",
        "Steamed milk 'stained' by espresso. The espresso is poured *into* the milk, creating layers.",
        listOf("Layered look", "Milk first, then coffee", "served in tall glass"),
        listOf("Steam milk and pour into tall glass.", "Let sit for 30 seconds to separate foam.", "Slowly pour espresso through the foam layer."),
        "10 oz", "300 ml", 64, 150,
        espressoRatio = 0.15f, milkRatio = 0.70f, foamRatio = 0.15f
    ),
    CoffeeDrink(
        "Espresso Con Panna",
        "Espresso topped with whipped cream.",
        listOf("Dessert treat", "Hot coffee, cold cream", "Rich texture"),
        listOf("Pull a double shot of espresso.", "Whip heavy cream until stiff peaks form.", "Float the cream gently on top of the coffee."),
        "2 oz", "60 ml", 64, 60,
        espressoRatio = 0.5f, milkRatio = 0f, foamRatio = 0f, creamRatio = 0.5f
    )
)

// --- COMPOSABLES ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoffeeApp() {
    var selectedCoffee by remember { mutableStateOf(coffeeMenu.first()) }
    var isMetric by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Barista Guide") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF003077),
                    titleContentColor = Color.White
                ),
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text("oz", color = if (!isMetric) Color.White else Color.White.copy(0.6f))
                        Switch(
                            checked = isMetric,
                            onCheckedChange = { isMetric = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF003077),
                                checkedTrackColor = Color.White,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.Gray
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Text("ml", color = if (isMetric) Color.White else Color.White.copy(0.6f))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

            // Menu
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(coffeeMenu) { coffee ->
                    FilterChip(
                        selected = (coffee == selectedCoffee),
                        onClick = { selectedCoffee = coffee },
                        label = { Text(coffee.name) }
                    )
                }
            }

            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row: Name + Caffeine Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedCoffee.name,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4E342E)
                    )

                    // NEW: Caffeine Badge
                    Badge(containerColor = Color(0xFFFFA000)) {
                        Text(
                            text = "⚡ ${selectedCoffee.caffeineMg}mg",
                            modifier = Modifier.padding(4.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // VISUALS
                CoffeeCupVisual(drink = selectedCoffee)

                // NEW: Brew Timer
                BrewTimer(seconds = selectedCoffee.brewTimeSeconds)

                Spacer(modifier = Modifier.height(16.dp))

                // Size
                val displaySize = if (isMetric) selectedCoffee.sizeMl else selectedCoffee.sizeOz
                Text(
                    text = "Classic Size: $displaySize",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Key Points
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFEBE9)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Key Characteristics:", fontWeight = FontWeight.Bold)
                        selectedCoffee.keyPoints.forEach { point ->
                            Text("• $point", modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }

                // NEW: Preparation Steps
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)), // Light Blue for instructions
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("How to Make:", fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
                        selectedCoffee.steps.forEachIndexed { index, step ->
                            Text("${index + 1}. $step", modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }

                // Description
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = selectedCoffee.description,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

// --- NEW COMPOSABLE: BREW TIMER ---
@Composable
fun BrewTimer(seconds: Int) {
    var timeLeft by remember(seconds) { mutableIntStateOf(seconds) }
    var isRunning by remember(seconds) { mutableStateOf(false) }

    // Timer Logic
    LaunchedEffect(isRunning, timeLeft) {
        if (isRunning && timeLeft > 0) {
            delay(1000L)
            timeLeft--
        } else if (timeLeft == 0) {
            isRunning = false
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (timeLeft > 0) "Brew Timer" else "Ready!",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = "${timeLeft}s",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (timeLeft < 5 && isRunning) Color.Red else Color.Black
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = { isRunning = !isRunning },
            colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) Color(0xFFD32F2F) else Color(0xFF388E3C))
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Warning else Icons.Default.PlayArrow,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isRunning) "Pause" else "Start")
        }

        IconButton(onClick = {
            isRunning = false
            timeLeft = seconds
        }) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.Gray)
        }
    }
}

// --- UPDATED VISUAL: POURING & STEAM ANIMATION ---
@Composable
fun CoffeeCupVisual(drink: CoffeeDrink) {
    // 1. Pouring Animation State
    var animationProgress by remember { mutableFloatStateOf(0f) }

    // Trigger animation when drink changes
    LaunchedEffect(drink) {
        animationProgress = 0f
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        ) { value, _ -> animationProgress = value }
    }

    // 2. Steam Animation (Infinite)
    val infiniteTransition = rememberInfiniteTransition(label = "steam")
    val steamY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -50f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "steamY"
    )
    val steamAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "steamAlpha"
    )

    Box(
        modifier = Modifier
            .height(280.dp) // Slightly taller for steam
            .width(180.dp)
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // --- STEAM PARTICLES ---
        Canvas(modifier = Modifier.fillMaxWidth().height(100.dp).align(Alignment.TopCenter)) {
            val waveWidth = 20.dp.toPx()

            // Draw 3 steam lines
            listOf(0.3f, 0.5f, 0.7f).forEachIndexed { index, startXFraction ->
                val startX = size.width * startXFraction

                // Simple sine wave path
                val path = Path()
                path.moveTo(startX, size.height + steamY + (index * 10)) // Move up based on animation
                path.cubicTo(
                    startX + waveWidth, size.height + steamY - 20,
                    startX - waveWidth, size.height + steamY - 40,
                    startX, size.height + steamY - 80
                )

                drawPath(
                    path = path,
                    color = Color.LightGray.copy(alpha = steamAlpha),
                    style = Stroke(width = 4.dp.toPx())
                )
            }
        }

        // --- CUP & LIQUIDS ---
        Box(
            modifier = Modifier
                .height(250.dp)
                .fillMaxWidth()
                .border(4.dp, Color.Black, shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .background(Color.White)
        ) {
            // Apply filling animation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animationProgress) // Grows from 0 to 100%
                    .align(Alignment.BottomCenter)
            ) {
                val total = drink.foamRatio + drink.milkRatio + drink.waterRatio + drink.espressoRatio + drink.creamRatio

                // Empty space
                if (total < 1f) {
                    Spacer(modifier = Modifier.weight(1f - total).fillMaxWidth().background(Color.Transparent))
                }

                // Stack ingredients (Top visual = First in Column logic here because of Bottom alignment)
                // Whipped Cream
                if (drink.creamRatio > 0) {
                    Box(modifier = Modifier.weight(drink.creamRatio).fillMaxWidth().background(Color(0xFFF5F5F5))) {
                        Text("Whipped Cream", modifier = Modifier.align(Alignment.Center), color = Color.Black, fontSize = 10.sp)
                    }
                }
                // Foam
                if (drink.foamRatio > 0) {
                    Box(modifier = Modifier.weight(drink.foamRatio).fillMaxWidth().background(Color(0xFFE0E0E0))) {
                        Text("Foam", modifier = Modifier.align(Alignment.Center), color = Color.Black, fontSize = 10.sp)
                    }
                }
                // Water
                if (drink.waterRatio > 0) {
                    Box(modifier = Modifier.weight(drink.waterRatio).fillMaxWidth().background(Color(0xFFB3E5FC))) {
                        Text("Water", modifier = Modifier.align(Alignment.Center), color = Color.Black, fontSize = 10.sp)
                    }
                }
                // Milk
                if (drink.milkRatio > 0) {
                    Box(modifier = Modifier.weight(drink.milkRatio).fillMaxWidth().background(Color(0xFFFFF8E1))) {
                        Text("Milk", modifier = Modifier.align(Alignment.Center), color = Color.Black, fontSize = 10.sp)
                    }
                }
                // Espresso
                if (drink.espressoRatio > 0) {
                    Box(modifier = Modifier.weight(drink.espressoRatio).fillMaxWidth().background(Color(0xFF4E342E))) {
                        Text("Espresso", modifier = Modifier.align(Alignment.Center), color = Color.White, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
