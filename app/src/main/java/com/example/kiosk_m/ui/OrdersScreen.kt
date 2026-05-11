package com.example.kiosk_m.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.util.Locale
import com.example.kiosk_m.MenuItem
import com.example.kiosk_m.Order
import com.example.kiosk_m.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    orders: List<Order>,
    onBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    onOrderReceived: (Order) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Your Orders", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCart) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (orders.any { it.isMcDo }) Color(0xFFFFBC0D) else Color(0xFFE31837),
                    titleContentColor = if (orders.any { it.isMcDo }) Color.Black else Color.White,
                    navigationIconContentColor = if (orders.any { it.isMcDo }) Color.Black else Color.White,
                    actionIconContentColor = if (orders.any { it.isMcDo }) Color.Black else Color.White
                )
            )
        }
    ) { padding ->
        val currentOrderIsMcDo = orders.lastOrNull()?.isMcDo ?: false
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(if (currentOrderIsMcDo) Color(0xFFFFBC0D).copy(alpha = 0.1f) else Color(0xFFE31837).copy(alpha = 0.1f))
                .padding(16.dp)
        ) {
            Text(
                text = "Your Order",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No orders yet",
                            fontSize = 18.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(orders, key = { it.id }) { order ->
                        OrderCard(order = order, onOrderReceived = { onOrderReceived(order) })
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: Order, onOrderReceived: () -> Unit) {
    val totalItems = order.items.sumOf { it.quantity }
    val subtotal = order.items.sumOf { it.price * it.quantity }
    val vatableSales = subtotal / 1.12
    val vatAmount = subtotal - vatableSales

    // Order Number Calculation
    val orderNum = try {
        order.id.takeLast(4).let { if (it.all { c -> c.isDigit() }) it else "1001" }
    } catch (e: Exception) {
        "1001"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Receipt Header with Order Number
            Text(
                text = "ORDER #$orderNum",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Header with Logo and Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = if (order.isMcDo) R.drawable.mcdo_logo else R.drawable.jollibee_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = order.status,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }


            // Item Highlight (Image and Name)
            order.items.firstOrNull()?.let { firstItem ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        if (firstItem.imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = firstItem.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Image(
                                painter = painterResource(id = firstItem.imageRes),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = firstItem.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f),
                        lineHeight = 22.sp
                    )
                }
            }

            // M-KIOSK DINE-IN Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color.Black)
                Text(
                    text = " M-KIOSK DINE-IN ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color.Black)
            }

            // Item Details
            order.items.forEach { item ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.quantity}-pc ${item.name.uppercase()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = String.format(Locale.US, "%.2f", item.price * item.quantity),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                    // Components (Indented)
                    item.components.forEach { component ->
                        Text(
                            text = "    $component",
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = Color.Black, modifier = Modifier.padding(vertical = 8.dp))

            // Count and Subtotal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$totalItems Item(s)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = String.format(Locale.US, "Subtotal %.2f", subtotal),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // VAT Section
            ReceiptRow(label = "VATable Sales", value = String.format(Locale.US, "%.2f", vatableSales))
            ReceiptRow(label = "VAT-Exempt Sales", value = "0.00")
            ReceiptRow(label = "VAT Zero-Rated Sales", value = "0.00")
            ReceiptRow(label = "VAT Amount", value = String.format(Locale.US, "%.2f", vatAmount))

            Spacer(modifier = Modifier.height(24.dp))

            // Order Received Button (Right Aligned)
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Button(
                    onClick = onOrderReceived,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (order.isMcDo) Color(0xFFFFBC0D) else Color(0xFFE31837),
                        contentColor = if (order.isMcDo) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("Order Received", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}




@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = Color.Black, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 13.sp, color = Color.Black, fontWeight = FontWeight.Medium)
    }
}
