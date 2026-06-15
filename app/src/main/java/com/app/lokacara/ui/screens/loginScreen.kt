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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.app.lokacara.R
import com.app.lokacara.ui.components.GoogleButton
import com.app.lokacara.ui.components.LokacaraTextField
import com.app.lokacara.ui.components.SnackbarManager
import com.app.lokacara.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.lokacara.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val loginSuccess by viewModel.loginSuccess.collectAsState()
    val forgotPasswordLoading by viewModel.forgotPasswordLoading.collectAsState()
    val forgotPasswordSuccess by viewModel.forgotPasswordSuccess.collectAsState()
    val forgotPasswordError by viewModel.forgotPasswordError.collectAsState()

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.1f))

        Text(
            text = stringResource(R.string.auth_login),
            style = MaterialTheme.typography.displaySmall,
            color = Primary500,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

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
                        viewModel.loginWithGoogle(idToken, acct.email)
                    }
                }
            } catch (e: Exception) {
                SnackbarManager.showError("Gagal login dengan Google")
            }
        }

        GoogleButton(
            text = stringResource(R.string.auth_login_google),
            enabled = !isLoading && googleWebClientId.isNotBlank(),
            onClick = { googleLauncher.launch(googleSignInClient.signInIntent) }
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
            onValueChange = { viewModel.email.value = it },
            placeholder = stringResource(R.string.auth_email_placeholder)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LokacaraTextField(
            value = password,
            onValueChange = { viewModel.password.value = it },
            placeholder = stringResource(R.string.auth_password_placeholder),
            isPassword = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Lupa Kata Sandi?", // TODO: move to string resources if needed
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = Gray500,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { showForgotPasswordDialog = true }
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login() },
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

        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = msg,
                style = MaterialTheme.typography.labelSmall,
                color = SemanticErrorBase,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.logo_lokacara),
            contentDescription = "Logo Bawah",
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 100.dp)
        )
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotPasswordDialog = false
                forgotEmail = ""
                viewModel.resetForgotPasswordSuccess()
            },
            title = { Text("Lupa Kata Sandi", fontFamily = NunitoFont, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Masukkan email Anda untuk menerima link reset password.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LokacaraTextField(
                        value = forgotEmail,
                        onValueChange = { forgotEmail = it },
                        placeholder = "Email",
                        isOutlined = true,
                        shape = RoundedCornerShape(12.dp),
                        containerColor = Color.White
                    )
                    forgotPasswordError?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(err, color = SemanticErrorBase, style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.forgotPassword(forgotEmail) },
                    enabled = !forgotPasswordLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary500),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    if (forgotPasswordLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Kirim", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showForgotPasswordDialog = false
                    forgotEmail = ""
                    viewModel.resetForgotPasswordSuccess()
                }) {
                    Text("Batal", color = Gray500)
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_7")
@Composable
fun LoginScreenPreview() {
    LokacaraMobileTheme {
        LoginScreen(
            onNavigateToRegister = {},
            onLoginSuccess = {},
            viewModel = hiltViewModel()
        )
    }
}
