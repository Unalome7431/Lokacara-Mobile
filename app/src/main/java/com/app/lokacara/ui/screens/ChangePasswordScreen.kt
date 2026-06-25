package com.app.lokacara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.app.lokacara.R
import com.app.lokacara.ui.components.LokacaraTextField
import com.app.lokacara.ui.navigation.navigateBackOrHome
import com.app.lokacara.ui.theme.Gray50
import com.app.lokacara.ui.theme.Gray500
import com.app.lokacara.ui.theme.Gray900
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.Primary100
import com.app.lokacara.ui.theme.Primary500
import com.app.lokacara.ui.theme.SemanticErrorBase
import com.app.lokacara.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val changePasswordSuccess by viewModel.changePasswordSuccess.collectAsStateWithLifecycle()
    val isGoogleAuth by viewModel.isGoogleAuth.collectAsStateWithLifecycle()
    val hasLocalPassword by viewModel.hasLocalPassword.collectAsStateWithLifecycle()
    val isCreatePasswordFlow = isGoogleAuth && !hasLocalPassword

    LaunchedEffect(changePasswordSuccess) {
        if (changePasswordSuccess) {
            viewModel.resetChangePasswordSuccess()
            navController.navigateBackOrHome()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.change_password_title),
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateBackOrHome() }) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "Kembali",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Gray900,
                    navigationIconContentColor = Gray900
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        ChangePasswordForm(
            modifier = Modifier.padding(innerPadding),
            viewModel = viewModel,
            isLoading = isLoading,
            errorMessage = errorMessage,
            isCreatePasswordFlow = isCreatePasswordFlow,
            showOldPasswordField = !isGoogleAuth
        )
    }
}

@Composable
private fun ChangePasswordForm(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel,
    isLoading: Boolean,
    errorMessage: String?,
    isCreatePasswordFlow: Boolean,
    showOldPasswordField: Boolean
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isCreatePasswordFlow) "Buat Kata Sandi" else "Ubah Kata Sandi",
            style = MaterialTheme.typography.headlineSmall,
            color = Primary500,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isCreatePasswordFlow) {
                "Tambahkan kata sandi lokal agar akun Google ini juga bisa dipakai login dengan email dan kata sandi."
            } else {
                "Pastikan kata sandi baru Anda unik dan kuat untuk menjaga keamanan akun."
            },
            fontFamily = NunitoFont,
            fontSize = 14.sp,
            color = Gray500,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                ChangePasswordFields(
                    viewModel = viewModel,
                    showOldPasswordField = showOldPasswordField
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        errorMessage?.let { msg ->
            Text(
                text = msg,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = SemanticErrorBase,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = { viewModel.changePassword() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary500)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (isCreatePasswordFlow) "Buat Kata Sandi" else "Simpan Kata Sandi Baru",
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ChangePasswordFields(
    viewModel: AuthViewModel,
    showOldPasswordField: Boolean
) {
    val oldPassword by viewModel.oldPassword.collectAsStateWithLifecycle()
    val newPassword by viewModel.newPassword.collectAsStateWithLifecycle()
    val confirmPassword by viewModel.confirmPassword.collectAsStateWithLifecycle()

    if (showOldPasswordField) {
        LokacaraTextField(
            value = oldPassword,
            onValueChange = { viewModel.oldPassword.value = it },
            placeholder = stringResource(R.string.change_password_old_placeholder),
            label = stringResource(R.string.change_password_old_label),
            isPassword = true,
            isOutlined = true,
            containerColor = Gray50,
            shape = RoundedCornerShape(12.dp)
        )
    }
    LokacaraTextField(
        value = newPassword,
        onValueChange = { viewModel.newPassword.value = it },
        placeholder = stringResource(R.string.change_password_new_placeholder),
        label = stringResource(R.string.change_password_new_label),
        isPassword = true,
        isOutlined = true,
        containerColor = Gray50,
        shape = RoundedCornerShape(12.dp)
    )
    LokacaraTextField(
        value = confirmPassword,
        onValueChange = { viewModel.confirmPassword.value = it },
        placeholder = stringResource(R.string.change_password_confirm_placeholder),
        label = stringResource(R.string.change_password_confirm_label),
        isPassword = true,
        isOutlined = true,
        containerColor = Gray50,
        shape = RoundedCornerShape(12.dp)
    )
}
