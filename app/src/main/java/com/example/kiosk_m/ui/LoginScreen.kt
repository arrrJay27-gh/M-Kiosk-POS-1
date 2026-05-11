package com.example.kiosk_m.ui

import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.Typeface
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiosk_m.R
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CurvedJoyText(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    Canvas(modifier = modifier.fillMaxWidth().height(160.dp)) {
        val path = AndroidPath().apply {
            // Arc coordinates tuned for "Choose your"
            addArc(25f, 10f, size.width * 0.8f, 350f, 195f, 150f)
        }

        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = with(density) { 32.sp.toPx() }
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.nativeCanvas.drawTextOnPath("Choose your", path, 0f, 0f, paint)

            val paintJoy = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = with(density) { 52.sp.toPx() }
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            // Use relative positioning for "JOY!"
            canvas.nativeCanvas.drawText("JOY!", size.width * 0.25f, size.height * 0.55f, paintJoy)
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val viewModel: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val context = androidx.compose.ui.platform.LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showLoginForm by remember { mutableStateOf(false) }

    // Using BoxWithConstraints to calculate relative sizes for images/text
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color(0xFFE31837))) {
        val screenWidth = maxWidth

        // 1. SLANTED BACKGROUND
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(0f, size.height * 0.48f)
                lineTo(size.width, size.height * 0.72f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path, color = Color(0xFFFFCF53))
        }

        // 2. MAIN SCROLLABLE CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {

            // Header Section
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                Column(modifier = Modifier.padding(top = 20.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.jollibee_logo),
                        contentDescription = null,
                        modifier = Modifier.size(screenWidth * 0.12f)
                    )
                    Text(
                        "Jollibee",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    CurvedJoyText(modifier = Modifier.fillMaxWidth(0.7f))
                }

                Image(
                    painter = painterResource(id = R.drawable.chicken_bucket),
                    contentDescription = null,
                    modifier = Modifier
                        .size(screenWidth * 0.55f)
                        .align(Alignment.TopEnd)
                        .offset(x = 10.dp, y = 40.dp)
                        .graphicsLayer(rotationZ = -5f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // McDonald's Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 27.dp)
                    .height(IntrinsicSize.Min)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.mcdo_fries),
                    contentDescription = null,
                    modifier = Modifier
                        .size(screenWidth * 0.45f)
                        .align(Alignment.CenterStart)
                        .offset(x = (-30).dp)
                        .graphicsLayer(rotationZ = -5f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(bottom = 20.dp)
                ) {
                    Text(
                        "McDonald's", 
                        color = Color.White,
                        fontSize = (screenWidth.value * 0.05f).sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(screenWidth * 0.16f)
                            .background(Color(0xFFE31837), RoundedCornerShape(50))
                            .padding(5.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.mcdo_logo),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedVisibility(
                visible = !showLoginForm,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "MOBILE",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "KIOSK",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(60.dp)) // Reduced spacer to move button up
                    
                    // TOGGLE BUTTON CONTAINER
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.85f) // Adjusted width to match the image better
                            .height(60.dp)
                            .shadow(12.dp, RoundedCornerShape(12.dp)) // Drop shadow
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color(0xFFFFB700)) // Sign-in yellow
                                .clickable { showLoginForm = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sign-in", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color.White) // Sign-up white
                                .clickable { onNavigateToSignUp() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sign-up", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

            AnimatedVisibility(
                visible = showLoginForm,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                // 4. LOGIN CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 40.dp),
                    shape = RoundedCornerShape(40.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFB700))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            IconButton(onClick = { showLoginForm = false }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Text(
                                "Sign in to your account",
                                color = Color.White,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Social Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SocialButton(
                                text = "facebook",
                                bgColor = Color(0xFF3B5998),
                                iconRes = R.drawable.ic_facebook,
                                modifier = Modifier.weight(1f)
                            )
                            SocialButton(
                                text = "Google",
                                bgColor = Color(0xFF72CB91),
                                iconRes = R.drawable.ic_google,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.6f))
                            Text(" or ", color = Color.White, fontSize = 14.sp, fontStyle = FontStyle.Italic)
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.6f))
                        }

                        CustomTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "Enter your email address" 
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        CustomTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "Password",
                            isPassword = true
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Log-in Button with shadow
                        Button(
                            onClick = {
                                if (email.isNotEmpty() && password.isNotEmpty()) {
                                    isLoading = true
                                    viewModel.loginUser(email, password) { isSuccess, errorMessage ->
                                        isLoading = false
                                        if (isSuccess) {
                                            onLoginSuccess()
                                        } else {
                                            Toast.makeText(context, errorMessage ?: "Invalid Email or Password", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(50.dp)
                                .shadow(4.dp, RoundedCornerShape(25.dp)), // Added shadow
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8C00))
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("LOG-IN", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = buildAnnotatedString {
                                append("Do you have account? ")
                                withStyle(style = SpanStyle(color = Color(0xFFE31837), fontStyle = FontStyle.Italic, textDecoration = TextDecoration.Underline)) {
                                    append("sign-up")
                                }
                            },
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { onNavigateToSignUp() }
                        )
                    }
                }
            }
        }
    }
}

class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    fun loginUser(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        val trimmedEmail = email.trim()
        auth.signInWithEmailAndPassword(trimmedEmail, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    val error = task.exception?.localizedMessage ?: "Login failed"
                    android.util.Log.e("FirebaseAuth", "Login failed for $trimmedEmail: $error")
                    onResult(false, error)
                }
            }
    }
}

@Composable
fun SocialButton(text: String, bgColor: Color, iconRes: Int, modifier: Modifier) {
    Button(
        onClick = { },
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        shape = RoundedCornerShape(22.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(text, fontSize = 12.sp, color = Color.White, maxLines = 1)
        }
    }
}

@Composable
fun CustomTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, isPassword: Boolean = false) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, fontStyle = FontStyle.Italic, color = Color.Gray, fontSize = 14.sp) },
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(27.dp),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFD9D9D9),
            unfocusedContainerColor = Color(0xFFD9D9D9),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color.Gray
        )
    )
}
