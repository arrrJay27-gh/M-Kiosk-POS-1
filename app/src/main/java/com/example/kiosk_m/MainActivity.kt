package com.example.kiosk_m

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.kiosk_m.ui.LoginScreen
import com.example.kiosk_m.ui.MenuScreen
import com.example.kiosk_m.ui.CartScreen
import com.example.kiosk_m.ui.PaymentScreen
import com.example.kiosk_m.ui.GCashScreen
import com.example.kiosk_m.ui.OrdersScreen
import com.example.kiosk_m.ui.SignUpScreen
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.FirebaseApp
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val firebaseManager = FirebaseManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase manually to ensure it's ready for the ViewModel
        FirebaseApp.initializeApp(this)

        setContent {
            var currentScreen by remember { mutableStateOf("home") }
            var isLoggedIn by remember { mutableStateOf(false) }
            val cartItems = remember { mutableStateListOf<MenuItem>() }
            val activeOrders = remember { mutableStateListOf<MenuItem>() }

            val bottomBar: @Composable () -> Unit = {
                KioskBottomBar(
                    cartCount = cartItems.size,
                    onNavigateToMenu = { currentScreen = "menu" },
                    onNavigateToCart = { currentScreen = "cart" },
                    onNavigateToOrders = { currentScreen = "orders" },
                    onNavigateToHome = { currentScreen = "home" },
                    currentScreen = currentScreen
                )
            }

            when (currentScreen) {
                "login" -> {
                    LoginScreen(
                        onLoginSuccess = {
                            isLoggedIn = true
                            currentScreen = "home"
                        },
                        onNavigateToSignUp = { currentScreen = "signup" }
                    )
                }
                "signup" -> {
                    SignUpScreen(
                        onBack = { currentScreen = "login" },
                        onSignUpComplete = {
                            currentScreen = "login"
                        }
                    )
                }
                "menu" -> {
                    MenuScreen(
                        onBack = { currentScreen = "home" },
                        onAddItem = { item ->
                            if (!isLoggedIn) {
                                currentScreen = "login"
                            } else {
                                cartItems.add(item)
                                currentScreen = "cart"
                            }
                        }
                    )
                }
                "cart" -> {
                    CartScreen(
                        cartItems = cartItems,
                        onBack = { currentScreen = "menu" },
                        onAddItem = { currentScreen = "menu" },
                        onOrderNow = { currentScreen = "payment" },
                        onCancel = { 
                            cartItems.clear()
                            currentScreen = "menu"
                        }
                    )
                }
                "payment" -> {
                    PaymentScreen(
                        cartItems = cartItems,
                        onBack = { currentScreen = "cart" },
                        onProceed = { currentScreen = "gcash_payment" },
                        onCancel = { currentScreen = "cart" }
                    )
                }
                "gcash_payment" -> {
                    GCashScreen(
                        onBack = { currentScreen = "payment" },
                        onProceed = {
                            activeOrders.addAll(cartItems)
                            cartItems.clear()
                            currentScreen = "orders"
                        },
                        onCancel = { currentScreen = "payment" }
                    )
                }
                "orders" -> {
                    OrdersScreen(
                        orders = activeOrders,
                        onBack = { currentScreen = "home" },
                        onNavigateToCart = { currentScreen = "cart" },
                        onOrderReceived = { 
                            activeOrders.clear()
                        },
                        bottomBar = bottomBar
                    )
                }
                "home" -> {
                    HomepageView(
                        manager = firebaseManager,
                        isLoggedIn = isLoggedIn,
                        cartItems = cartItems,
                        onLoginRequired = { currentScreen = "login" },
                        onLogout = { isLoggedIn = false },
                        onNavigateToMenu = { currentScreen = "menu" },
                        onNavigateToCart = { currentScreen = "cart" },
                        bottomBar = bottomBar
                    )
                }
            }
        }
    }
}

@Composable
fun HomepageView(
    manager: FirebaseManager,
    isLoggedIn: Boolean,
    cartItems: MutableList<MenuItem>,
    onLoginRequired: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToCart: () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    var userName by remember { mutableStateOf("Guest") }
    var userLocation by remember { mutableStateOf("1600 - Mountain View") }
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            fetchRealLocation(context) { userLocation = it }
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            manager.fetchUserData { userName = it }
        } else {
            userName = "Guest"
        }
    }

    LaunchedEffect(Unit) {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation) {
            fetchRealLocation(context) { userLocation = it }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(color = Color(0xFFE31837))
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height * 0.6f)
                    lineTo(0f, size.height * 0.35f)
                    close()
                }
                drawPath(path = path, color = Color(0xFFE31837))
            }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = bottomBar
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(span = { GridItemSpan(2) }) {
                        Column {
                            LocationHeader(locationName = userLocation)
                            Spacer(Modifier.height(24.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Hello, $userName!",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(
                                    onClick = if (isLoggedIn) onLogout else onLoginRequired,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFE31837)),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(if (isLoggedIn) "Sign Out" else "Sign In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Choose your day!",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    item(span = { GridItemSpan(2) }) {
                        PromoCardJollibee(onOrder = onNavigateToMenu)
                    }

                    item(span = { GridItemSpan(2) }) {
                        PromoCardMcDo(onOrder = onNavigateToMenu)
                    }

                    item(span = { GridItemSpan(2) }) {
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Restaurant,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Featured meal",
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    val featuredMeals = listOf(
                        MenuItem("1", "Mushroom Pepper Steak", 95.0, "Rice Meals", R.drawable.d_meal1),
                        MenuItem("2", "1pc Chicken McDo", 125.0, "Chicken", R.drawable.d_meal2),
                        MenuItem("3", "Cheeseburger Deluxe", 85.0, "Burgers", R.drawable.d_meal3),
                        MenuItem("4", "McSpaghetti", 110.0, "Pasta", R.drawable.d_meal2),
                        MenuItem("5", "Burger McDo", 65.0, "Burgers", R.drawable.d_meal1),
                        MenuItem("6", "Big Mac", 199.0, "Burgers", R.drawable.d_meal3)
                    )

                    items(featuredMeals) { meal ->
                        MealCard(
                            item = meal,
                            onAdd = {
                                if (!isLoggedIn) onLoginRequired() else cartItems.add(it)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PromoCardJollibee(onOrder: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(190.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.jollibee_dashboard),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Button(
                onClick = onOrder,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 12.dp)
                    .height(40.dp)
                    .width(130.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFE31837)),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text("Order Now!", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun PromoCardMcDo(onOrder: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFBC0D)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.mcdo_logo),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Mc Donald's",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Text(
                text = "McSavers Sulit Busog\nMeals Philippines 2026",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 12.dp, top = 20.dp)
                    .width(300.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 5.dp, end = 90.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.d_meal2),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp)
                )
                Text("1pc chicken mcdo", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 150.dp, top = 30.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.d_meal1),
                    contentDescription = null,
                    modifier = Modifier.size(70.dp)
                )
                Text("Mushroom pepper Steak", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Image(
                painter = painterResource(id = R.drawable.d_meal3),
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterEnd).padding(top = 5.dp, end = 25.dp).size(70.dp)
            )

            Button(
                onClick = onOrder,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 12.dp)
                    .height(40.dp)
                    .width(130.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD9D9D9),
                    contentColor = Color(0xFFE31837)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Order Now!", fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun fetchRealLocation(context: Context, onLocationFetched: (String) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location ->
            if (location != null) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses: List<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val featureName = address.featureName ?: ""
                        onLocationFetched(featureName)
                    }
                } catch (e: Exception) { }
            }
        }
}

@Composable
fun LocationHeader(locationName: String) {
    Card(
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, Color.White),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(24.dp))
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text("YOU'RE ORDERING FROM", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Text(locationName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun MealCard(item: MenuItem, onAdd: (MenuItem) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAdd(item) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = item.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black,
                maxLines = 2,
                minLines = 2
            )
            Text(
                text = "₱${item.price}",
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun KioskBottomBar(
    cartCount: Int,
    onNavigateToMenu: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToHome: () -> Unit,
    currentScreen: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFFFBC0D),
        modifier = modifier.fillMaxWidth().height(90.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Rounded.Home,
                label = "Home",
                tint = if (currentScreen == "home") Color(0xFFE31837) else Color.White,
                onClick = onNavigateToHome
            )

            BottomNavItem(
                icon = Icons.AutoMirrored.Rounded.MenuBook,
                label = "Menu",
                tint = if (currentScreen == "menu") Color(0xFFE31837) else Color.White,
                onClick = onNavigateToMenu
            )

            BottomNavItem(
                icon = Icons.Rounded.Fastfood,
                label = "Orders",
                tint = if (currentScreen == "orders") Color(0xFFE31837) else Color.White,
                onClick = onNavigateToOrders
            )

            BottomNavItem(
                icon = Icons.Rounded.Favorite,
                label = "Favorite",
                tint = if (currentScreen == "favorite") Color(0xFFE31837) else Color.White,
                onClick = { }
            )

            BottomNavItem(
                icon = Icons.Rounded.ShoppingCart,
                label = "Cart",
                tint = if (currentScreen == "cart") Color(0xFFE31837) else Color.White,
                badgeCount = cartCount,
                onClick = onNavigateToCart
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    tint: Color = Color.White,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (badgeCount > 0) {
            BadgedBox(
                badge = {
                    Badge(containerColor = Color.White, contentColor = Color(0xFFE31837)) {
                        Text(badgeCount.toString(), fontSize = 10.sp)
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
