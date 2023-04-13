package com.msys.alexapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.msys.alexapp.components.Authorization
import com.msys.alexapp.components.AuthorizationCallback
import com.msys.alexapp.components.Carousel
import com.msys.alexapp.services.Role
import com.msys.alexapp.ui.theme.AlexAppTheme
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AlexAppTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          object : AlexAppService {
            override suspend fun signIn(email: String, password: String) {
              FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
            }
          }.NavComposable()
        }
      }
    }
  }
}

interface AlexAppService {
  suspend fun signIn(email: String, password: String)
}

@Composable
fun AlexAppService.NavComposable() {
  val navController = rememberNavController()
  NavHost(navController = navController, startDestination = "authorization") {
    composable("authorization") {
      object : AuthorizationCallback {
        override fun become(role: Role) = navController.navigate("carousel")
        override suspend fun signIn(email: String, password: String) {
          this@NavComposable.signIn(email, password)
        }
      }.Authorization()
    }
    composable("carousel") {
      Carousel()
    }
  }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  AlexAppTheme {
    object : AlexAppService {
      override suspend fun signIn(email: String, password: String) {}
    }.NavComposable()
  }
}