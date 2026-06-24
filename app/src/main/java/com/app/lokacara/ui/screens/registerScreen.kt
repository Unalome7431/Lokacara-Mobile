package com.app.lokacara.ui.screens

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.lokacara.R
import com.app.lokacara.ui.components.LokacaraTextField
import com.app.lokacara.ui.theme.*
import com.app.lokacara.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToTerms: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val name by viewModel.name.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val confirmPassword by viewModel.confirmPassword.collectAsStateWithLifecycle()
    val isChecked by viewModel.isChecked.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val fieldErrors by viewModel.registerFieldErrors.collectAsStateWithLifecycle()
    val registerSuccess by viewModel.registerSuccess.collectAsStateWithLifecycle()
    val loginSuccess by viewModel.loginSuccess.collectAsStateWithLifecycle()

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { isVisible = true }

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

    RegisterContent(
        isVisible = isVisible,
        name = name,
        email = email,
        password = password,
        confirmPassword = confirmPassword,
        isChecked = isChecked,
        isLoading = isLoading,
        errorMessage = errorMessage,
        fieldErrors = fieldErrors,
        onNameChange = {
            viewModel.name.value = it
            viewModel.clearRegisterFieldError("name")
        },
        onEmailChange = {
            viewModel.email.value = it
            viewModel.clearRegisterFieldError("email")
        },
        onPasswordChange = {
            viewModel.password.value = it
            viewModel.clearRegisterFieldError("password")
            viewModel.clearRegisterFieldError("confirmPassword")
        },
        onConfirmPasswordChange = {
            viewModel.confirmPassword.value = it
            viewModel.clearRegisterFieldError("confirmPassword")
        },
        onCheckedChange = {
            viewModel.isChecked.value = it
            viewModel.clearRegisterFieldError("agreement")
        },
        onRegisterClick = { viewModel.register() },
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToTerms = onNavigateToTerms,
        onNavigateToPrivacy = onNavigateToPrivacy,
        onGoogleSignIn = { viewModel.loginWithGoogle(it.first, it.second) }
    )
}

@Composable
fun RegisterContent(
    isVisible: Boolean,
    name: String,
    email: String,
    password: String,
    confirmPassword: String,
    isChecked: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    fieldErrors: Map<String, String>,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
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
                text = "Daftar",
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
                value = name,
                onValueChange = onNameChange,
                placeholder = "Nama Lengkap"
            )
            FieldErrorText(fieldErrors["name"])
            Spacer(modifier = Modifier.height(16.dp))
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
                placeholder = "Kata Sandi",
                isPassword = true
            )
            FieldErrorText(fieldErrors["password"])
            Spacer(modifier = Modifier.height(16.dp))
            LokacaraTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                placeholder = "Konfirmasi Kata Sandi",
                isPassword = true
            )
            FieldErrorText(fieldErrors["confirmPassword"])
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(checkedColor = Primary500, uncheckedColor = Gray300),
                    modifier = Modifier.padding(end = 4.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Saya setuju dengan",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = Gray500
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "persyaratan layanan",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = Primary500,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable(onClick = onNavigateToTerms)
                                .padding(vertical = 4.dp)
                        )
                        Text(" dan ", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Gray500)
                        Text(
                            text = "kebijakan privasi",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = Primary500,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable(onClick = onNavigateToPrivacy)
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
            FieldErrorText(fieldErrors["agreement"])

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRegisterClick,
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
                    modifier = Modifier.clickable(onClick = onNavigateToLogin)
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
fun RegisterScreenPreview() {
    LokacaraMobileTheme {
        RegisterContent(
            isVisible = true,
            name = "",
            email = "",
            password = "",
            confirmPassword = "",
            isChecked = false,
            isLoading = false,
            errorMessage = null,
            fieldErrors = emptyMap(),
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onCheckedChange = {},
            onRegisterClick = {},
            onNavigateToLogin = {},
            onNavigateToTerms = {},
            onNavigateToPrivacy = {},
            onGoogleSignIn = {}
        )
    }
}
