package com.example.kiosk_m.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Patterns
import com.google.firebase.database.FirebaseDatabase

@Composable
fun SignUpScreen(
    onBack: () -> Unit,
    onSignUpComplete: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf(List(6) { "" }) }
    var step by remember { mutableIntStateOf(1) } // 1: Email, 2: OTP, 3: Registration Form, 4: Credentials

    val database = FirebaseDatabase.getInstance().reference

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(if (step >= 3) Color.White else Color(0xFFE31837))) {
        if (step < 3) {
            // 1. SLANTED BACKGROUND
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.38f)
                    lineTo(size.width, size.height * 0.62f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = Color(0xFFFFCF53))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (step < 3) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (step == 2) step = 1 else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    "MOBILE",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "KIOSK",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(60.dp))
            }

            AnimatedContent(targetState = step, label = "SignUpSteps") { targetStep ->
                when (targetStep) {
                    1 -> EmailStep(
                        email = email,
                        onEmailChange = { email = it },
                        onNext = { step = 2 }
                    )
                    2 -> OtpStep(
                        email = email,
                        otpCode = otpCode,
                        onOtpChange = { index, value ->
                            if (value.length <= 1) {
                                val newOtp = otpCode.toMutableList()
                                newOtp[index] = value
                                otpCode = newOtp
                            }
                        },
                        onNext = { step = 3 }
                    )
                    3 -> RegistrationForm(
                        firstName = firstName,
                        onFirstNameChange = { firstName = it },
                        lastName = lastName,
                        onLastNameChange = { lastName = it },
                        mobileNumber = mobileNumber,
                        onMobileNumberChange = { mobileNumber = it },
                        onBackToLogin = onBack,
                        onNext = { step = 4 }
                    )
                    4 -> CredentialsForm(
                        userName = userName,
                        onUserNameChange = { userName = it },
                        password = password,
                        onPasswordChange = { password = it },
                        confirmPassword = confirmPassword,
                        onConfirmPasswordChange = { confirmPassword = it },
                        onBackToLogin = onBack,
                        onConfirmed = {
                            val userId = database.child("users").push().key ?: ""
                            val userMap = mapOf(
                                "email" to email,
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "mobileNumber" to mobileNumber,
                                "userName" to userName,
                                "password" to password // Note: In a real app, never store passwords in plain text!
                            )
                            database.child("users").child(userId).setValue(userMap)
                                .addOnSuccessListener {
                                    onSignUpComplete()
                                }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmailStep(
    email: String,
    onEmailChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val isEmailValid = email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Getting started",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Create account to continue",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(60.dp))

        Text(
            "Use your email address to sign-up",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
        )

        TextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = { Text("Enter Email Address", color = Color.Gray, fontSize = 16.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onNext,
            enabled = isEmailValid,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFB700),
                disabledContainerColor = Color(0xFFFFB700).copy(alpha = 0.5f),
                disabledContentColor = Color.White.copy(alpha = 0.5f)
            )
        ) {
            Text(
                "NEXT",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun OtpStep(
    email: String,
    otpCode: List<String>,
    onOtpChange: (Int, String) -> Unit,
    onNext: () -> Unit
) {
    val isOtpComplete = otpCode.all { it.isNotEmpty() }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "ENTER VERIFICATION CODE",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "We sent a 6-digit verification code to",
            color = Color.White,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = email,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            otpCode.forEachIndexed { index, value ->
                TextField(
                    value = value,
                    onValueChange = { onOtpChange(index, it) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Text("Can't received code? ", color = Color.White, fontSize = 12.sp)
            Text(
                "RESEND",
                color = Color.Red,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { /* Resend logic */ }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onNext,
            enabled = isOtpComplete,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFB700),
                disabledContainerColor = Color(0xFFFFB700).copy(alpha = 0.5f),
                disabledContentColor = Color.White.copy(alpha = 0.5f)
            )
        ) {
            Text(
                "NEXT",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun RegistrationForm(
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    mobileNumber: String,
    onMobileNumberChange: (String) -> Unit,
    onBackToLogin: () -> Unit,
    onNext: () -> Unit
) {
    val isFormValid = firstName.isNotEmpty() && lastName.isNotEmpty() && mobileNumber.length >= 10

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Register an Account",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Sign up now to unlock exclusive deals\nand discounts.",
                color = Color.Gray,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row {
                Text("Already have an account? ", color = Color.Black, fontSize = 14.sp)
                Text(
                    "Log-in",
                    color = Color(0xFFE31837),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onBackToLogin() }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // First Name
        Column(modifier = Modifier.fillMaxWidth()) {
            Row {
                Text("First Name", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("*", color = Color.Red)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = firstName,
                onValueChange = onFirstNameChange,
                placeholder = { Text("Enter your First Name", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color.Gray
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Last Name
        Column(modifier = Modifier.fillMaxWidth()) {
            Row {
                Text("Last Name", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("*", color = Color.Red)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = lastName,
                onValueChange = onLastNameChange,
                placeholder = { Text("Enter your Last Name", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color.Gray
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mobile Number
        Column(modifier = Modifier.fillMaxWidth()) {
            Row {
                Text("Mobile Number", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("*", color = Color.Red)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.height(56.dp).width(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+63", fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = onMobileNumberChange,
                    placeholder = { Text("Enter Mobile Number", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color.Gray
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFB700),
                disabledContainerColor = Color(0xFFFFB700).copy(alpha = 0.5f)
            )
        ) {
            Text(
                "Create Account",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun CredentialsForm(
    userName: String,
    onUserNameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    onBackToLogin: () -> Unit,
    onConfirmed: () -> Unit
) {
    val isFormValid = userName.isNotEmpty() && password.isNotEmpty() && password == confirmPassword

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Register an Account",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Sign up now to unlock exclusive deals\nand discounts.",
                color = Color.Gray,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row {
                Text("Already have an account? ", color = Color.Black, fontSize = 14.sp)
                Text(
                    "Log-in",
                    color = Color(0xFFE31837),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onBackToLogin() }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // User Name
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("User Name", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = userName,
                onValueChange = onUserNameChange,
                placeholder = { Text("Enter user name", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color.Gray
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Password
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Password", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = { Text("Enter password", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color.Gray
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Confirmed Password
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Confirmed Password", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                placeholder = { Text("Confirmed your password", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color.Gray
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onConfirmed,
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFB700),
                disabledContainerColor = Color(0xFFFFB700).copy(alpha = 0.5f)
            )
        ) {
            Text(
                "Confirmed",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
