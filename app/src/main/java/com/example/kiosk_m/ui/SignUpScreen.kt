package com.example.kiosk_m.ui

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import org.json.JSONObject

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
    val otpCode = remember { mutableStateListOf("", "", "", "", "", "") }
    var generatedOtp by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(1) } // 1: Email, 2: OTP, 3: Registration Form, 4: Credentials
    var isLoading by remember { mutableStateOf(false) }
    var resendTimer by remember { mutableIntStateOf(0) }

    LaunchedEffect(resendTimer) {
        if (resendTimer > 0) {
            kotlinx.coroutines.delay(1000)
            resendTimer--
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        if (step >= 3) {
            // RED TOP BAR FOR REGISTRATION FORM
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFFE31837))
            ) {
                IconButton(
                    onClick = { if (step > 1) step-- else onBack() },
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 8.dp, top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
        }

        if (step < 3) {
            // ORIGINAL RED BACKGROUND FOR STEP 1 & 2
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE31837)))
            
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
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (step < 3) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (step == 2) step = 1 else onBack() },
                        modifier = Modifier.offset(x = (-15).dp)
                    ) {
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
                        onNext = {
                            val trimmedEmail = email.trim()
                            if (trimmedEmail.isEmpty()) return@EmailStep
                            isLoading = true
                            
                            // 1. Check if email is already registered in Firebase Auth
                            auth.fetchSignInMethodsForEmail(trimmedEmail).addOnCompleteListener { checkTask ->
                                if (checkTask.isSuccessful) {
                                    val signInMethods = checkTask.result?.signInMethods
                                    if (!signInMethods.isNullOrEmpty()) {
                                        isLoading = false
                                        Toast.makeText(context, "Email already in use. Please use another or login.", Toast.LENGTH_LONG).show()
                                        return@addOnCompleteListener
                                    }
                                    
                                    // 2. If email is free, generate and send OTP
                                    val otp = (100000..999999).random().toString()
                                    generatedOtp = otp
                                    scope.launch {
                                        val errorMsg = sendOtpEmail(trimmedEmail, otp)
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            if (errorMsg == null) {
                                                step = 2
                                                Toast.makeText(context, "OTP Sent to $trimmedEmail", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Log.e("SignUpScreen", "Email fail: $errorMsg")
                                                Toast.makeText(context, "Failed to send OTP: $errorMsg", Toast.LENGTH_LONG).show()
                                                Log.i("OTP_DEBUG", "FALLBACK OTP: $otp")
                                            }
                                        }
                                    }
                                } else {
                                    isLoading = false
                                    val error = checkTask.exception?.localizedMessage ?: "Error checking email"
                                    Log.e("SignUpScreen", "Email Check Error: $error")
                                    // If email enumeration protection is ON, fetchSignInMethods might fail. 
                                    // In that case, we proceed anyway and let createUser handle it later.
                                    val otp = (100000..999999).random().toString()
                                    generatedOtp = otp
                                    scope.launch {
                                        val errorMsg = sendOtpEmail(trimmedEmail, otp)
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            step = 2
                                        }
                                    }
                                }
                            }
                        }
                    )
                    2 -> OtpStep(
                        email = email,
                        otpCode = otpCode,
                        onOtpChange = { index, value ->
                            if (value.length <= 1) {
                                otpCode[index] = value
                            }
                        },
                        onNext = {
                            val enteredOtp = otpCode.joinToString("")
                            if (generatedOtp.isNotEmpty() && enteredOtp == generatedOtp) {
                                Log.i("OTP_DEBUG", "SUCCESS: OTP Verified Successfully ($enteredOtp)")
                                Toast.makeText(context, "Verification Successful!", Toast.LENGTH_SHORT).show()
                                step = 3
                            } else {
                                Log.e("OTP_DEBUG", "FAILURE: Match Failed! Entered: '$enteredOtp', Expected: '$generatedOtp'")
                                Toast.makeText(context, "Invalid OTP code. Please try again.", Toast.LENGTH_SHORT).show()
                                // Don't clear automatically, so user can correct the mistake
                            }
                        },
                        resendTimer = resendTimer,
                        onResendOtp = {
                            if (resendTimer > 0) return@OtpStep
                            val trimmedEmail = email.trim()
                            isLoading = true
                            val otp = (100000..999999).random().toString()
                            generatedOtp = otp
                            scope.launch {
                                val errorMsg = sendOtpEmail(trimmedEmail, otp)
                                withContext(Dispatchers.Main) {
                                    isLoading = false
                                    if (errorMsg == null) {
                                        resendTimer = 60
                                        Toast.makeText(context, "OTP resent to $trimmedEmail", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed: $errorMsg", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
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
                            val trimmedEmail = email.trim()
                            if (password != confirmPassword) {
                                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                return@CredentialsForm
                            }
                            if (password.length < 6) {
                                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                                return@CredentialsForm
                            }
                            isLoading = true
                            auth.createUserWithEmailAndPassword(trimmedEmail, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val userId = auth.currentUser?.uid ?: ""
                                        val cleanMobile = if (mobileNumber.startsWith("0")) mobileNumber.substring(1) else mobileNumber
                                        val userMap = mapOf(
                                            "email" to trimmedEmail,
                                            "firstName" to firstName,
                                            "lastName" to lastName,
                                            "mobileNumber" to "+63$cleanMobile",
                                            "userName" to userName,
                                            "createdAt" to System.currentTimeMillis()
                                        )
                                        database.child("users").child(userId).setValue(userMap)
                                            .addOnSuccessListener {
                                                isLoading = false
                                                Toast.makeText(context, "Registration Successful!", Toast.LENGTH_LONG).show()
                                                onSignUpComplete()
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                val dbError = e.message ?: "Database Error"
                                                Log.e("SignUpScreen", "Database Write Failed: $dbError")
                                                
                                                // CLEANUP: If DB fails, delete the Auth account so the user can try again
                                                auth.currentUser?.delete()
                                                
                                                if (dbError.contains("Permission denied")) {
                                                    Toast.makeText(context, "Database Rules Error: Please update your rules in Firebase Console.", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, "Database Error: $dbError", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    } else {
                                        isLoading = false
                                        val errorMsg = task.exception?.localizedMessage ?: "Registration failed"
                                        Log.e("SignUpScreen", "Firebase Auth Error: $errorMsg")
                                        
                                        if (errorMsg.contains("already in use")) {
                                            Toast.makeText(context, "This email is already registered. If you didn't finish registration, please contact support or use another email.", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                        }
                    )
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) { },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = if (step >= 3) Color(0xFFE31837) else Color.White)
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
    val isEmailValid = email.trim().isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
        )

        TextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = { Text("Enter Email Address", color = Color.Gray, fontSize = 17.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
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
    onNext: () -> Unit,
    resendTimer: Int,
    onResendOtp: () -> Unit
) {
    val isOtpComplete = otpCode.all { it.isNotEmpty() }
    val focusRequesters = remember { List(6) { FocusRequester() } }

    // Automatically focus the first field when screen loads or is cleared
    LaunchedEffect(otpCode.all { it.isEmpty() }) {
        if (otpCode.all { it.isEmpty() }) {
            focusRequesters[0].requestFocus()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            otpCode.forEachIndexed { index, value ->
                var isFocused by remember { mutableStateOf(false) }
                
                BasicTextField(
                    value = value,
                    onValueChange = { input ->
                        if (input.length > 1) {
                            val digits = input.filter { it.isDigit() }
                            if (digits.isNotEmpty()) {
                                digits.forEachIndexed { i, char ->
                                    if (index + i < 6) onOtpChange(index + i, char.toString())
                                }
                                focusRequesters[(index + digits.length).coerceAtMost(5)].requestFocus()
                            }
                        } else if (input.length == 1 && input.all { it.isDigit() }) {
                            onOtpChange(index, input)
                            if (index < 5) focusRequesters[index + 1].requestFocus()
                        } else if (input.isEmpty()) {
                            onOtpChange(index, "")
                            if (index > 0) focusRequesters[index - 1].requestFocus()
                        }
                    },
                    modifier = Modifier
                        .size(width = 40.dp, height = 52.dp)
                        .focusRequester(focusRequesters[index])
                        .onFocusChanged { isFocused = it.isFocused }
                        .onKeyEvent { event ->
                            if (event.key == Key.Backspace && event.type == KeyEventType.KeyDown) {
                                if (value.isEmpty() && index > 0) {
                                    onOtpChange(index - 1, "")
                                    focusRequesters[index - 1].requestFocus()
                                    return@onKeyEvent true
                                }
                            }
                            false
                        },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.Black
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = if (index == 5) ImeAction.Done else ImeAction.Next
                    ),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.5.dp,
                                    color = if (isFocused) Color(0xFFE31837) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            innerTextField()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Text("Can't received code? ", color = Color.White, fontSize = 12.sp)
            Text(
                if (resendTimer > 0) "RESEND IN ${resendTimer}s" else "RESEND",
                color = if (resendTimer > 0) Color.LightGray else Color.Red,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(enabled = resendTimer == 0) { onResendOtp() }
            )
        }

        Spacer(modifier = Modifier.height(60.dp))

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
fun RegistrationHeader(onBackToLogin: () -> Unit) {
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
            .padding(horizontal = 24.dp)
            .padding(top = 110.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RegistrationHeader(onBackToLogin = onBackToLogin)

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
    val isFormValid = userName.isNotEmpty() && password.length >= 6 && password == confirmPassword

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 110.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RegistrationHeader(onBackToLogin = onBackToLogin)

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
                "Confirm",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

private suspend fun sendOtpEmail(email: String, otp: String): String? = withContext(Dispatchers.IO) {
    Log.i("OTP_DEBUG", "***********************************************")
    Log.i("OTP_DEBUG", "*                                             *")
    Log.i("OTP_DEBUG", "*          YOUR LEGIT OTP IS: $otp            *")
    Log.i("OTP_DEBUG", "*                                             *")
    Log.i("OTP_DEBUG", "***********************************************")
    Log.d("SignUpScreen", "Attempting to send OTP $otp to $email")
    try {
        val url = URL("https://api.emailjs.com/api/v1.0/email/send")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.doOutput = true
        
        // YOUR UPDATED CREDENTIALS
        val serviceId = "service_5o1n85b"
        val templateId = "template_rs0zjje"
        val publicKey = "enfRPgfFFALruR0u0"
        val accessToken = "yO3U3_qXw4tZlYyAJBwBJ"

        val jsonInputString = JSONObject().apply {
            put("service_id", serviceId)
            put("template_id", templateId)
            put("user_id", publicKey)
            if (accessToken.isNotEmpty()) put("accessToken", accessToken)
            put("template_params", JSONObject().apply {
                put("email", email)
                put("passcode", otp)
                put("time", "15 minutes") // Adding a value for the {{time}} variable in your template
            })
        }.toString()

        Log.d("SignUpScreen", "EmailJS Request Payload: $jsonInputString")
        
        conn.outputStream.use { os ->
            os.write(jsonInputString.toByteArray(Charsets.UTF_8))
        }

        val responseCode = conn.responseCode
        val responseText = if (responseCode == 200) {
            conn.inputStream.bufferedReader().use { it.readText() }
        } else {
            conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown Error"
        }
        
        Log.d("SignUpScreen", "EmailJS Response Code: $responseCode Body: $responseText")
        
        if (responseCode == 200) null else "Server Error ($responseCode): $responseText"
    } catch (e: java.net.UnknownHostException) {
        Log.e("SignUpScreen", "DNS Error: No internet in emulator", e)
        "Internet error. Please check Emulator DNS settings or try 'Cold Boot'."
    } catch (e: Exception) {
        Log.e("SignUpScreen", "Network Error", e)
        "Network Error: ${e.localizedMessage ?: e.message}"
    }
}
