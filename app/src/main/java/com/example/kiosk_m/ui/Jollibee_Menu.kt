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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiosk_m.MenuItem
import com.example.kiosk_m.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onBack: () -> Unit,
    onAddItem: (MenuItem) -> Unit
) {
    var selectedItem by remember { mutableStateOf<MenuItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Chickenjoy") }
    val categories = listOf("Super Meals", "Chickenjoy", "Burgers", "Chicken Fillet")

    val menuItems = listOf(
        MenuItem("1", "1-pc. Chickenjoy w/ drinks M", 125.0, "Chickenjoy", R.drawable.d_meal2),
        MenuItem("2", "1-pc. Chickenjoy w/ Fries & drinks", 155.0, "Chickenjoy", R.drawable.d_meal2),
        MenuItem("3", "1-pc. Chickenjoy w/ jolly spaghetti solo", 165.0, "Chickenjoy", R.drawable.d_meal2),
        MenuItem("4", "1-pc. Chickenjoy w/ coke Float", 145.0, "Chickenjoy", R.drawable.d_meal2),
        MenuItem("5", "2-pc. Chickenjoy", 199.0, "Chickenjoy", R.drawable.d_meal2),
        MenuItem("6", "2-pc. Chickenjoy", 199.0, "Chickenjoy", R.drawable.d_meal2)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Jollibee Menu", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) 
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
                    containerColor = Color(0xFFE31837),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
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
                        contentDescription = null, 
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(32.dp)
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
                                color = if (selectedCategory == category) Color(0xFFE31837) else Color.Black,
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
                BottomActionBar(onCancel = onBack, onOrder = { /* TODO */ })
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
                Image(
                    painter = painterResource(id = item.imageRes),
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .padding(20.dp),
                    contentScale = ContentScale.Fit
                )

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
                    shape = RoundedCornerShape(24.dp),
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
        shape = RoundedCornerShape(24.dp),
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
fun BottomActionBar(onCancel: () -> Unit, onOrder: () -> Unit) {
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE31837)),
                shape = RoundedCornerShape(27.dp)
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
            Button(
                onClick = onOrder,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE31837)),
                shape = RoundedCornerShape(27.dp)
            ) {
                Text("Order Now", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.name,
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = item.name,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.fillMaxWidth().height(40.dp)
            )
        }
    }
}
