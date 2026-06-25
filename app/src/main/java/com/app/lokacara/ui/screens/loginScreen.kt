package com.app.lokacara.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.lokacara.R
import com.app.lokacara.ui.components.GoogleButton
import com.app.lokacara.ui.components.LokacaraTextField
import com.app.lokacara.ui.components.SnackbarManager
import com.app.lokacara.ui.theme.*
import com.app.lokacara.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val email by viewModel.email.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val fieldErrors by viewModel.loginFieldErrors.collectAsStateWithLifecycle()
    val loginSuccess by viewModel.loginSuccess.collectAsStateWithLifecycle()
    val forgotPasswordLoading by viewModel.forgotPasswordLoading.collectAsStateWithLifecycle()
    val forgotPasswordSuccess by viewModel.forgotPasswordSuccess.collectAsStateWithLifecycle()
    val forgotPasswordError by viewModel.forgotPasswordError.collectAsStateWithLifecycle()
    val googleSignInConfigured = stringResource(R.string.google_web_client_id).isNotBlank()

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
        viewModel.resetForm()
    }

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            viewModel.resetLoginSuccess()
            onLoginSuccess()
        }
    }

    LaunchedEffect(forgotPasswordSuccess) {
        if (forgotPasswordSuccess) {
            viewModel.resetForgotPasswordSuccess()
            showForgotPasswordDialog = false
            forgotEmail = ""
        }
    }

    LoginContent(
        isVisible = isVisible,
        email = email,
        password = password,
        isLoading = isLoading,
        errorMessage = errorMessage,
        fieldErrors = fieldErrors,
        onEmailChange = {
            viewModel.email.value = it
            viewModel.clearLoginFieldError("email")
        },
        onPasswordChange = {
            viewModel.password.value = it
            viewModel.clearLoginFieldError("password")
        },
        onLoginClick = { viewModel.login() },
        onForgotPasswordClick = { showForgotPasswordDialog = true },
        onNavigateToRegister = onNavigateToRegister,
        onGoogleSignIn = { viewModel.loginWithGoogle(it.first, it.second) }
    )

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            email = forgotEmail,
            onEmailChange = { forgotEmail = it },
            isLoading = forgotPasswordLoading,
            errorMessage = forgotPasswordError,
            googleSignInConfigured = googleSignInConfigured,
            onSubmit = { viewModel.forgotPassword(forgotEmail) },
            onDismiss = {
                showForgotPasswordDialog = false
                forgotEmail = ""
                viewModel.resetForgotPasswordSuccess()
            }
        )
    }
}

@Composable
fun LoginContent(
    isVisible: Boolean,
    email: String,
    password: String,
    isLoading: Boolean,
    errorMessage: String?,
    fieldErrors: Map<String, String>,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onGoogleSignIn: (Pair<String, String?>) -> Unit
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(500)) +
                androidx.compose.animation.slideInVertically(
                    initialOffsetY = { 100 },
                    animationSpec = androidx.compose.animation.core.tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.auth_login),
                style = MaterialTheme.typography.displaySmall,
                color = Primary500,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(48.dp))

            GoogleSignInSection(
                isLoading = isLoading,
                onGoogleSignIn = onGoogleSignIn
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Gray300)
                Text(
                    text = " atau ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray500,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Gray300)
            }

            Spacer(modifier = Modifier.height(24.dp))

            LokacaraTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = "Email",
                keyboardType = KeyboardType.Email
            )
            FieldErrorText(fieldErrors["email"])
            Spacer(modifier = Modifier.height(16.dp))
            LokacaraTextField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = stringResource(R.string.auth_password_placeholder),
                isPassword = true
            )
            FieldErrorText(fieldErrors["password"])

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Lupa Kata Sandi?",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = Gray500,
                modifier = Modifier
                    .align(Alignment.End)
                    .heightIn(min = 48.dp)
                    .clickable(onClick = onForgotPasswordClick)
                    .padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isLoading,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                ),
                colors = ButtonDefaults.buttonColors(containerColor = Primary500),
                shape = RoundedCornerShape(100.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.auth_login), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = errorMessage != null,
                enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = SemanticErrorBase,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Belum memiliki akun? ", style = MaterialTheme.typography.labelSmall, color = Gray500)
                Text(
                    text = stringResource(R.string.auth_register),
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary500,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onNavigateToRegister)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_lokacara),
                contentDescription = "Logo Bawah",
                modifier = Modifier.height(112.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun GoogleSignInSection(
    isLoading: Boolean,
    onGoogleSignIn: (Pair<String, String?>) -> Unit
) {
    val context = LocalContext.current
    val googleWebClientId = stringResource(R.string.google_web_client_id)
    val googleSignInOptions = remember(googleWebClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).apply {
            requestEmail()
            if (googleWebClientId.isNotBlank()) requestIdToken(googleWebClientId)
        }.build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, googleSignInOptions) }
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            account.result?.let { acct ->
                acct.idToken?.let { idToken ->
                    onGoogleSignIn(Pair(idToken, acct.email))
                }
            }
        } catch (_: Exception) {
            SnackbarManager.showError("Gagal login dengan Google")
        }
    }

    GoogleButton(
        text = stringResource(R.string.auth_login_google),
        enabled = !isLoading && googleWebClientId.isNotBlank(),
        onClick = { googleLauncher.launch(googleSignInClient.signInIntent) }
    )
    if (googleWebClientId.isBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Login Google belum dikonfigurasi di perangkat ini.",
            style = MaterialTheme.typography.labelSmall,
            color = Gray500,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun ForgotPasswordDialog(
    email: String,
    onEmailChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    googleSignInConfigured: Boolean,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.forgot_password_title), fontFamily = NunitoFont, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    stringResource(R.string.forgot_password_email_only_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (googleSignInConfigured) {
                        stringResource(R.string.forgot_password_google_hint)
                    } else {
                        stringResource(R.string.forgot_password_google_config_missing)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray500
                )
                Spacer(modifier = Modifier.height(16.dp))
                LokacaraTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    placeholder = "Email",
                    isOutlined = true,
                    shape = RoundedCornerShape(12.dp),
                    containerColor = Color.White
                )
                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(err, color = SemanticErrorBase, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Primary500),
                shape = RoundedCornerShape(100.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.forgot_password_send), color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.forgot_password_cancel), color = Gray500)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun FieldErrorText(message: String?) {
    if (message == null) return
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = message,
        style = MaterialTheme.typography.labelSmall,
        color = SemanticErrorBase,
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_7")
@Composable
fun LoginScreenPreview() {
    LokacaraMobileTheme {
        LoginContent(
            isVisible = true,
            email = "",
            password = "",
            isLoading = false,
            errorMessage = null,
            fieldErrors = emptyMap(),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onForgotPasswordClick = {},
            onNavigateToRegister = {},
            onGoogleSignIn = {}
        )
    }
}
