package com.example.trelloclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.trelloclone.ui.ProfileScreen
import com.example.trelloclone.ui.RegisterScreen
import com.example.trelloclone.ui.theme.TrelloCloneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrelloCloneTheme {
                ProfileScreen(
                    onRegisterSuccess = {
                        // TODO: Navigate to HomeScreen hoặc Dashboard sau khi đăng ký thành công
                    }
                )
            }
        }
    }
}
