package com.example.kiosk_m.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.runtime.collectAsState
import com.example.kiosk_m.FirebaseManager
import com.example.kiosk_m.MenuItem
import com.example.kiosk_m.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    isMcDo: Boolean = false,
    firebaseManager: FirebaseManager = FirebaseManager(),
    onBack: () -> Unit,
    onAddItem: (MenuItem) -> Unit
) {
    val themeColor = if (isMcDo) Color(0xFFFFBC0D) else Color(0xFFE31837)
    val themeTitle = if (isMcDo) "Menu" else "Jollibee Menu"
    var selectedItem by remember { mutableStateOf<MenuItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Featured Meals", "Super Meals", "Chickenjoy", "Burgers", "Chicken Fillet")

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Observe menu items from Firebase
    val menuItems by firebaseManager.getMenuItems().collectAsState(initial = emptyList())

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                drawerShape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp),
                modifier = Modifier.width(300.dp)
            ) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF28C12E)) // Green color from image
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "CATEGORIES",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }

                // Drawer Items
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        Column {
                            NavigationDrawerItem(
                                label = { 
                                    Text(
                                        text = category,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color(0xFF333333)
                                    ) 
                                },
                                selected = selectedCategory == category,
                                onClick = {
                                    selectedCategory = category
                                    scope.launch { drawerState.close() }
                                },
                                shape = RoundedCornerShape(0.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = Color(0xFFF0F0F0),
                                    unselectedContainerColor = Color.Transparent
                                ),
                                modifier = Modifier.height(56.dp)
                            )
                            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
            TopAppBar(
                title = { 
                    Text(themeTitle, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedItem != null) selectedItem = null else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Open Cart */ }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = themeColor,
                    titleContentColor = if (isMcDo) Color.Black else Color.White,
                    navigationIconContentColor = if (isMcDo) Color.Black else Color.White,
                    actionIconContentColor = if (isMcDo) Color.Black else Color.White
                )
            )
        },
        containerColor = if (selectedItem == null) Color(0xFFD9D9D9) else Color.White
    ) { padding ->
        if (selectedItem == null) {
            // MAIN MENU GRID
            Column(modifier = Modifier.padding(padding)) {
                // Search Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(25.dp),
                    color = Color.White
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        trailingIcon = { 
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        singleLine = true
                    )
                }

                // Categories Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.List, 
                        contentDescription = "Open Categories", 
                        tint = if (isMcDo) Color.Black else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(32.dp)
                            .clickable {
                                scope.launch { drawerState.open() }
                            }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(categories) { category ->
                            Text(
                                text = category,
                                fontSize = 16.sp,
                                fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedCategory == category) {
                                    if (isMcDo) Color(0xFFE31837) else Color(0xFFE31837) // Selected color
                                } else {
                                    Color.Black
                                },
                                modifier = Modifier.clickable { selectedCategory = category }
                            )
                        }
                    }
                }

                // Menu Items Grid
                Box(modifier = Modifier.weight(1f)) {
                    val filteredItems = menuItems.filter { 
                        (selectedCategory == "All" || it.category == selectedCategory) &&
                        it.name.contains(searchQuery, ignoreCase = true)
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredItems) { item ->
                            MenuCard(item = item, onClick = { selectedItem = item })
                        }
                    }
                }

                // Bottom Buttons
                BottomActionBar(
                    onCancel = onBack, 
                    onOrder = { /* TODO */ },
                    themeColor = themeColor
                )
            }
        } else {
            // ITEM DETAIL VIEW
            ItemDetailView(
                item = selectedItem!!,
                paddingValues = padding,
                onCancel = { selectedItem = null },
                onOrderNow = {
                    onAddItem(selectedItem!!)
                    selectedItem = null
                }
            )
        }
    }
}
}

@Composable
fun ItemDetailView(
    item: MenuItem,
    paddingValues: PaddingValues,
    onCancel: () -> Unit,
    onOrderNow: () -> Unit
) {
    var selectedChoice by remember { mutableStateOf("Coke regular") }
    val choices = listOf(
        "Coke regular",
        "Coke Zero Regular",
        "Go Coke Float",
        "7up Regular",
        "Go Large Ice Tea",
        "Go Large Royal",
        "Go large Sprite"
    )

    // Selection states for new options
    var selectedB by remember { mutableStateOf(setOf<String>()) }
    var selectedC by remember { mutableStateOf(setOf<String>()) }
    var selectedD by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Color.White)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            item {
                // Product Image
                if (item.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .padding(20.dp),
                        contentScale = ContentScale.Fit,
                        placeholder = painterResource(id = R.drawable.d_meal2),
                        error = painterResource(id = R.drawable.d_meal2)
                    )
                } else {
                    Image(
                        painter = painterResource(id = if (item.imageRes != 0) item.imageRes else R.drawable.d_meal2),
                        contentDescription = item.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .padding(20.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                // Product Name
                Text(
                    text = item.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Choice Card A
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, Color(0xFFF0F0F0))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Choice A*",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        choices.forEach { choice ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedChoice = choice }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedChoice == choice,
                                    onClick = { selectedChoice = choice },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = choice, fontSize = 15.sp, color = Color.Black)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Choice B (Optional)
                ChoiceCheckboxCard(
                    title = "Choice B",
                    optional = true,
                    items = listOf("Extra Chickenjoy Gravy", "Extra Rice"),
                    prices = mapOf("Extra Chickenjoy Gravy" to "+5", "Extra Rice" to "+35"),
                    selectedItems = selectedB,
                    onToggle = { item ->
                        selectedB = if (selectedB.contains(item)) selectedB - item else selectedB + item
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Choice C (Optional)
                ChoiceCheckboxCard(
                    title = "Choice C",
                    optional = true,
                    items = listOf("Large Jolly Crispy Fries", "Medium Jolly Fries", "Regular Jolly Fries"),
                    selectedItems = selectedC,
                    onToggle = { item ->
                        selectedC = if (selectedC.contains(item)) selectedC - item else selectedC + item
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Choice D (Optional)
                ChoiceCheckboxCard(
                    title = "Choice D",
                    optional = true,
                    items = listOf(
                        "Chocolate Sundae",
                        "Cookies & Cream Sundae",
                        "Go Mini Choco Sundae",
                        "Large Peace Mango Pie",
                        "Regular Peace Mango Pie",
                        "Rocky Road Sundae Made w KITKAT"
                    ),
                    selectedItems = selectedD,
                    onToggle = { item ->
                        selectedD = if (selectedD.contains(item)) selectedD - item else selectedD + item
                    }
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Bottom Action Bar
        BottomActionBar(onCancel = onCancel, onOrder = onOrderNow)
    }
}

@Composable
fun ChoiceCheckboxCard(
    title: String,
    optional: Boolean = false,
    items: List<String>,
    prices: Map<String, String> = emptyMap(),
    selectedItems: Set<String>,
    onToggle: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                if (optional) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "(Optional)",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(item) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedItems.contains(item),
                        onCheckedChange = { onToggle(item) },
                        colors = CheckboxDefaults.colors(checkedColor = Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = item, fontSize = 15.sp, color = Color.Black, modifier = Modifier.weight(1f))
                    prices[item]?.let { price ->
                        Text(text = price, fontSize = 15.sp, color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun BottomActionBar(onCancel: () -> Unit, onOrder: () -> Unit, themeColor: Color = Color(0xFFE31837)) {
    Surface(
        color = Color(0xFFD9D9D9),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (themeColor == Color(0xFFFFBC0D)) Color(0xFFFFBC0D) else themeColor),
                shape = RoundedCornerShape(27.dp)
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (themeColor == Color(0xFFFFBC0D)) Color.Black else Color.White)
            }
            Button(
                onClick = onOrder,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (themeColor == Color(0xFFFFBC0D)) Color(0xFFFFBC0D) else themeColor),
                shape = RoundedCornerShape(27.dp)
            ) {
                Text("Order Now", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (themeColor == Color(0xFFFFBC0D)) Color.Black else Color.White)
            }
        }
    }
}

@Composable
fun MenuCard(item: MenuItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Image Section with rounded corners and light grey background
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(15.dp),
                color = Color(0xFFF5F5F5)
            ) {
                if (item.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentScale = ContentScale.Fit,
                        placeholder = painterResource(id = R.drawable.d_meal2),
                        error = painterResource(id = R.drawable.d_meal2)
                    )
                } else {
                    Image(
                        painter = painterResource(id = if (item.imageRes != 0) item.imageRes else R.drawable.d_meal2),
                        contentDescription = item.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text section and Price
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.Black,
                    lineHeight = 16.sp,
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(end = 8.dp)
                )
                
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(0.8f)
                ) {
                    Text(
                        text = "₱ ${"%.2f".format(java.util.Locale.US, item.price)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Jollibee Red Plus Button
                    Surface(
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onClick() },
                        shape = CircleShape,
                        color = Color(0xFFE31837) // Jollibee Red
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
