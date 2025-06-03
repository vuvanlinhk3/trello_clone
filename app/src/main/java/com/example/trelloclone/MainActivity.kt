package com.example.trelloclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
<<<<<<< HEAD
import com.example.trelloclone.ui.ProfileScreen
import com.example.trelloclone.ui.RegisterScreen
=======
import androidx.navigation.compose.rememberNavController
import com.example.trelloclone.ui.navigation.AppNavGraph
>>>>>>> 2358ed24e94b5e31b7e5a756897996e459caccc7
import com.example.trelloclone.ui.theme.TrelloCloneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrelloCloneTheme {
<<<<<<< HEAD
                ProfileScreen(
                    onRegisterSuccess = {
                        // TODO: Navigate to HomeScreen hoặc Dashboard sau khi đăng ký thành công
                    }
                )
=======
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
>>>>>>> 2358ed24e94b5e31b7e5a756897996e459caccc7
            }
        }
    }
}
