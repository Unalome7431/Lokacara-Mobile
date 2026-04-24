package com.app.lokacara.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.lokacara.R
import com.app.lokacara.ui.components.GoogleButton
import com.app.lokacara.ui.components.LokacaraTextField
import com.app.lokacara.ui.theme.*

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(150.dp))

        Text(
            text = "Masuk",
            style = MaterialTheme.typography.displaySmall,
            color = Primary500,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        GoogleButton(
            text = "Masuk dengan Google",
            onClick = {  }
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
            onValueChange = { email = it },
            placeholder = "Email / Nomor Telepon"
        )

        Spacer(modifier = Modifier.height(16.dp))

        LokacaraTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Kata Sandi",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Lupa Kata Sandi?",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = Gray500,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { }
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            ),
            colors = ButtonDefaults.buttonColors(containerColor = Primary500),
            shape = RoundedCornerShape(100.dp)
        ) {
            Text("Masuk", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.logo_lokacara), // Sesuaikan id logomu
            contentDescription = "Logo Bawah",
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 100.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_7")
@Composable
fun LoginScreenPreview() {
    LokacaraMobileTheme {
        LoginScreen(
            onNavigateToRegister = {}
        )
    }
}