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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onLoginSuccess: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val name by viewModel.name.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val isChecked by viewModel.isChecked.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val registerSuccess by viewModel.registerSuccess.collectAsState()
    val loginSuccess by viewModel.loginSuccess.collectAsState()

    LaunchedEffect(registerSuccess) {
        if (registerSuccess) {
            viewModel.resetRegisterSuccess()
            viewModel.resetForm()
            onNavigateToLogin()
        }
    }

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            viewModel.resetLoginSuccess()
            onLoginSuccess()
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
        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = "Daftar",
            style = MaterialTheme.typography.displaySmall,
            color = Primary500,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        val context = LocalContext.current
        val googleWebClientId = stringResource(R.string.google_web_client_id)
        val googleSignInOptions = remember(googleWebClientId) {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).apply {
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
                        viewModel.loginWithGoogle(idToken)
                    }
                }
            } catch (e: Exception) {
                SnackbarManager.showError("Gagal login dengan Google")
            }
        }

        GoogleButton(
            text = "Daftar dengan Google",
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
            value = name,
            onValueChange = { viewModel.name.value = it },
            placeholder = "Nama Lengkap"
        )

        Spacer(modifier = Modifier.height(16.dp))

        LokacaraTextField(
            value = email,
            onValueChange = { viewModel.email.value = it },
            placeholder = "Email / Nomor Telepon"
        )

        Spacer(modifier = Modifier.height(16.dp))

        LokacaraTextField(
            value = password,
            onValueChange = { viewModel.password.value = it },
            placeholder = "Kata Sandi",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        LokacaraTextField(
            value = confirmPassword,
            onValueChange = { viewModel.confirmPassword.value = it },
            placeholder = "Konfirmasi Kata Sandi",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { viewModel.isChecked.value = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = Primary500,
                    uncheckedColor = Gray300
                ),
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = buildAnnotatedString {
                    append("Saya setuju dengan ")
                    withStyle(style = SpanStyle(color = Primary500)) {
                        append("persyaratan layanan")
                    }
                    append(" dan ")
                    withStyle(style = SpanStyle(color = Primary500)) {
                        append("kebijakan privasi")
                    }
                },
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = Gray500
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.register() },
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
                Text("Daftar", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
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
            Text("Sudah memiliki akun? ", style = MaterialTheme.typography.labelSmall, color = Gray500)
            Text(
                text = "Masuk",
                style = MaterialTheme.typography.labelSmall,
                color = Primary500,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToLogin() }
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
}

@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_7")
@Composable
fun RegisterScreenPreview() {
    LokacaraMobileTheme {
        RegisterScreen(
            onNavigateToLogin = {},
            viewModel = hiltViewModel()
        )
    }
}
