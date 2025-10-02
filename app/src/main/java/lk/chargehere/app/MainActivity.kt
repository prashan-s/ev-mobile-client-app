package lk.chargehere.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import lk.chargehere.app.auth.GoogleSignInManager
import lk.chargehere.app.navigation.ChargeHereNavigation
import lk.chargehere.app.navigation.NavigationGraph
import lk.chargehere.app.ui.theme.ChargeHereTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var googleSignInManager: GoogleSignInManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        android.util.Log.d("MainActivity", "MainActivity onCreate called")

        // Initialize Google Sign-In
        googleSignInManager.initialize(this)

        enableEdgeToEdge()
        setContent {
            ChargeHereTheme {
                ChargeHereApp()
            }
        }
    }
}

@Composable
fun ChargeHereApp() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        ChargeHereNavigation(
            navController = navController,
            startDestination = NavigationGraph.Auth.route,
            modifier = Modifier.padding(innerPadding)
        )
    }
}