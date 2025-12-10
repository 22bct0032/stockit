package com.example.stockit.ui.screens.auth

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.stockit.ui.theme.StockItTheme
import com.example.stockit.utils.AuthManager
import com.example.stockit.network.ApiConfig
import com.example.stockit.network.SignUpRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUp: () -> Unit,
    onNavigateToSignIn: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var startAnimation by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Validation states
    var isFullNameValid by remember { mutableStateOf(true) }
    var isEmailValid by remember { mutableStateOf(true) }
    var isPasswordValid by remember { mutableStateOf(true) }
    var isConfirmPasswordValid by remember { mutableStateOf(true) }

    // Animation states
    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "content_alpha"
    )
    
    val contentScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "content_scale"
    )

    // Validation functions
    fun validateFullName(name: String): String? {
        return when {
            name.isBlank() -> "Full name is required"
            name.length < 2 -> "Full name must be at least 2 characters"
            name.any { it.isDigit() } -> "Full name cannot contain numbers"
            name.any { !it.isLetter() && !it.isWhitespace() && it != '\'' && it != '-' } -> 
                "Full name can only contain letters, spaces, hyphens, and apostrophes"
            name.any { it.isUpperCase() } -> "Full name must be in lowercase only"
            else -> null
        }
    }

    fun validateEmail(email: String): String? {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return when {
            email.isBlank() -> "Email is required"
            !email.matches(emailPattern.toRegex()) -> "Please enter a valid email address"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 8 -> "Password must be at least 8 characters long"
            !password.any { it.isUpperCase() } -> "Password must contain at least 1 uppercase letter"
            !password.any { it.isLowerCase() } -> "Password must contain at least 1 lowercase letter"
            !password.any { it.isDigit() } -> "Password must contain at least 1 number"
            !password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) } -> 
                "Password must contain at least 1 special character (!@#$%^&*()_+-=[]{}|;:,.<>?)"
            else -> null
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Please confirm your password"
            password != confirmPassword -> "Passwords do not match"
            else -> null
        }
    }

    // Start animation
    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F172A)
                    )
                )
            )
    ) {
        // Background decoration matching WatchlistScreen
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 3
            
            drawCircle(
                color = Color.White.copy(alpha = 0.02f),
                radius = size.width * 0.6f,
                center = Offset(centerX - size.width * 0.3f, centerY - 100.dp.toPx())
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.015f),
                radius = size.width * 0.8f,
                center = Offset(centerX + size.width * 0.2f, centerY + 200.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .scale(contentScale)
                .alpha(contentAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // Enhanced Title
            Text(
                text = "Create Account",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = (-1).sp,
                lineHeight = 48.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Sign up to start tracking your investments",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE2E8F0),
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
                letterSpacing = 0.5.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Enhanced Form Fields
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Full Name Field
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { 
                        fullName = it
                        isFullNameValid = validateFullName(it) == null
                        if (errorMessage.isNotEmpty()) errorMessage = ""
                    },
                    label = { 
                        Text(
                            "Full Name",
                            color = if (isFullNameValid) Color(0xFFE2E8F0) else Color(0xFFEF4444),
                            fontWeight = FontWeight.Medium
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isFullNameValid) Color(0xFF6366F1) else Color(0xFFEF4444)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    isError = !isFullNameValid,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isFullNameValid) Color(0xFF6366F1) else Color(0xFFEF4444),
                        unfocusedBorderColor = if (isFullNameValid) Color(0xFF334155) else Color(0xFFEF4444),
                        errorBorderColor = Color(0xFFEF4444),
                        focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.95f),
                        unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.95f),
                        errorContainerColor = Color(0xFF1E293B).copy(alpha = 0.95f),
                        focusedLabelColor = if (isFullNameValid) Color(0xFF6366F1) else Color(0xFFEF4444),
                        unfocusedLabelColor = if (isFullNameValid) Color(0xFFE2E8F0) else Color(0xFFEF4444),
                        errorLabelColor = Color(0xFFEF4444),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        errorTextColor = Color.White
                    )
                )

                // Show full name validation error
                validateFullName(fullName)?.let { error ->
                    if (fullName.isNotEmpty()) {
                        Text(
                            text = error,
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        isEmailValid = validateEmail(it) == null
                        if (errorMessage.isNotEmpty()) errorMessage = ""
                    },
                    label = { 
                        Text(
                            "Email",
                            color = if (isEmailValid) Color(0xFFE2E8F0) else Color(0xFFEF4444),
                            fontWeight = FontWeight.Medium
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = if (isEmailValid) Color(0xFF6366F1) else Color(0xFFEF4444)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    isError = !isEmailValid,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEmailValid) Color(0xFF6366F1) else Color(0xFFEF4444),
                        unfocusedBorderColor = if (isEmailValid) Color(0xFF334155) else Color(0xFFEF4444),
                        errorBorderColor = Color(0xFFEF4444),
                        focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.95f),
                        unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.95f),
                        errorContainerColor = Color(0xFF1E293B).copy(alpha = 0.95f),
                        focusedLabelColor = if (isEmailValid) Color(0xFF6366F1) else Color(0xFFEF4444),
                        unfocusedLabelColor = if (isEmailValid) Color(0xFFE2E8F0) else Color(0xFFEF4444),
                        errorLabelColor = Color(0xFFEF4444),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        errorTextColor = Color.White
                    )
                )

                // Show email validation error
                validateEmail(email)?.let { error ->
                    if (email.isNotEmpty()) {
                        Text(
                            text = error,
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        isPasswordValid = validatePassword(it) == null
                        isConfirmPasswordValid = validateConfirmPassword(it, confirmPassword) == null
                        if (errorMessage.isNotEmpty()) errorMessage = ""
                    },
                    label = { 
                        Text(
                            "Password",
                            color = if (isPasswordValid) Color(0xFFE2E8F0) else Color(0xFFEF4444),
                            fontWeight = FontWeight.Medium
                        ) 
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = if (isPasswordValid) Color(0xFF6366F1) else Color(0xFFEF4444)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    isError = !isPasswordValid,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isPasswordValid) Color(0xFF6366F1) else Color(0xFFEF4444),
                        unfocusedBorderColor = if (isPasswordValid) Color(0xFF334155) else Color(0xFFEF4444),
                        errorBorderColor = Color(0xFFEF4444),
                        focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.95f),
                        unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.95f),
                        errorContainerColor = Color(0xFF1E293B).copy(alpha = 0.95f),
                        focusedLabelColor = if (isPasswordValid) Color(0xFF6366F1) else Color(0xFFEF4444),
                        unfocusedLabelColor = if (isPasswordValid) Color(0xFFE2E8F0) else Color(0xFFEF4444),
                        errorLabelColor = Color(0xFFEF4444),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        errorTextColor = Color.White
                    )
                )

                // Show password validation error
                validatePassword(password)?.let { error ->
                    if (password.isNotEmpty()) {
                        Text(
                            text = error,
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }

                // Confirm Password Field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        isConfirmPasswordValid = validateConfirmPassword(password, it) == null
                        if (errorMessage.isNotEmpty()) errorMessage = ""
                    },
                    label = { 
                        Text(
                            "Confirm Password",
                            color = if (isConfirmPasswordValid) Color(0xFFE2E8F0) else Color(0xFFEF4444),
                            fontWeight = FontWeight.Medium
                        ) 
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                tint = if (isConfirmPasswordValid) Color(0xFF6366F1) else Color(0xFFEF4444)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { 
                            focusManager.clearFocus()
                            val allFieldsValid = isFullNameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid
                            if (allFieldsValid && fullName.isNotBlank() && email.isNotBlank() && 
                                password.isNotBlank() && confirmPassword.isNotBlank()) {
                                scope.launch {
                                    signUpUser(
                                        fullName = fullName,
                                        email = email,
                                        password = password,
                                        confirmPassword = confirmPassword,
                                        context = context,
                                        onLoading = { isLoading = it },
                                        onError = { errorMessage = it },
                                        onSuccess = onSignUp
                                    )
                                }
                            }
                        }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    isError = !isConfirmPasswordValid,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isConfirmPasswordValid) Color(0xFF6366F1) else Color(0xFFEF4444),
                        unfocusedBorderColor = if (isConfirmPasswordValid) Color(0xFF334155) else Color(0xFFEF4444),
                        errorBorderColor = Color(0xFFEF4444),
                        focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.95f),
                        unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.95f),
                        errorContainerColor = Color(0xFF1E293B).copy(alpha = 0.95f),
                        focusedLabelColor = if (isConfirmPasswordValid) Color(0xFF6366F1) else Color(0xFFEF4444),
                        unfocusedLabelColor = if (isConfirmPasswordValid) Color(0xFFE2E8F0) else Color(0xFFEF4444),
                        errorLabelColor = Color(0xFFEF4444),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        errorTextColor = Color.White
                    )
                )

                // Show confirm password validation error
                validateConfirmPassword(password, confirmPassword)?.let { error ->
                    if (confirmPassword.isNotEmpty()) {
                        Text(
                            text = error,
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Enhanced Error Message for server errors
            AnimatedVisibility(
                visible = errorMessage.isNotEmpty(),
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E293B).copy(alpha = 0.95f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFEF4444).copy(alpha = 0.08f),
                                        Color(0xFFEF4444).copy(alpha = 0.02f)
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(1000f, 1000f)
                                )
                            )
                    ) {
                        Text(
                            text = errorMessage,
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFFEF4444),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (errorMessage.isNotEmpty()) 16.dp else 8.dp))

            // Enhanced Sign Up Button
            val allFieldsValid = isFullNameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid
            Button(
                onClick = {
                    errorMessage = ""
                    
                    // Validate all fields first
                    val nameError = validateFullName(fullName)
                    val emailError = validateEmail(email)
                    val passwordError = validatePassword(password)
                    val confirmPasswordError = validateConfirmPassword(password, confirmPassword)
                    
                    if (nameError != null) {
                        errorMessage = nameError
                        return@Button
                    }
                    if (emailError != null) {
                        errorMessage = emailError
                        return@Button
                    }
                    if (passwordError != null) {
                        errorMessage = passwordError
                        return@Button
                    }
                    if (confirmPasswordError != null) {
                        errorMessage = confirmPasswordError
                        return@Button
                    }
                    
                    scope.launch {
                        signUpUser(
                            fullName = fullName,
                            email = email,
                            password = password,
                            confirmPassword = confirmPassword,
                            context = context,
                            onLoading = { isLoading = it },
                            onError = { errorMessage = it },
                            onSuccess = onSignUp
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && allFieldsValid && fullName.isNotBlank() && email.isNotBlank() && 
                         password.isNotBlank() && confirmPassword.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1),
                    disabledContainerColor = Color(0xFF334155)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign Up",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Enhanced Sign In Link
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 16.sp,
                    color = Color(0xFF94A3B8)
                )
                TextButton(
                    onClick = onNavigateToSignIn
                ) {
                    Text(
                        text = "Sign In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6366F1)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Updated signUpUser function with better error handling
suspend fun signUpUser(
    fullName: String,
    email: String,
    password: String,
    confirmPassword: String,
    context: Context,
    onLoading: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onSuccess: () -> Unit
) {
    onLoading(true)
    
    try {
        val request = SignUpRequest(fullName, email, password, confirmPassword)
        val response = ApiConfig.authApiService.signUp(request)
        
        if (response.success) {
            val accessToken = response.access_token ?: ""
            val refreshToken = response.refresh_token ?: ""
            val userId = response.user_id ?: ""
            val userEmail = response.user_email ?: ""
            val userFullName = response.user_fullName ?: ""
            
            // Store tokens and user info in SharedPreferences
            val authManager = AuthManager.getInstance(context)
            authManager.saveUserData(accessToken, refreshToken, userId, userEmail, userFullName)
            
            // Add a small delay to ensure state propagation
            kotlinx.coroutines.delay(100)
            
            onSuccess()
        } else {
            onError(response.message ?: "Sign up failed")
        }
        
    } catch (e: HttpException) {
        val errorMessage = when (e.code()) {
            400 -> "Invalid input. Please check your information and try again."
            409 -> "An account with this email already exists. Please sign in instead."
            422 -> "Validation failed. Please ensure all fields meet the requirements."
            429 -> "Too many attempts. Please try again later."
            500 -> "Server error. Please try again later."
            else -> "Sign up failed. Please try again."
        }
        onError(errorMessage)
    } catch (e: Exception) {
        android.util.Log.e("SignUpError", "Full error details:", e)
        val detailedError = when {
            e is java.net.UnknownHostException -> "Cannot reach server. Check internet connection."
            e is javax.net.ssl.SSLException -> "SSL Error: ${e.message?.take(100)}"
            e is javax.net.ssl.SSLHandshakeException -> "SSL Handshake failed. Server certificate issue."
            e is java.net.SocketTimeoutException -> "Connection timeout. Server might be down."
            e.message?.contains("SSL", ignoreCase = true) == true -> 
                "SSL certificate error. Contact server administrator."
            e.message?.contains("UNRECOGNIZED_NAME", ignoreCase = true) == true ->
                "Server SSL certificate name mismatch."
            else -> "Network error: ${e.javaClass.simpleName} - ${e.message?.take(100)}"
        }
        onError(detailedError)
    } finally {
        onLoading(false)
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    StockItTheme {
        SignUpScreen(
            onSignUp = {},
            onNavigateToSignIn = {}
        )
    }
}