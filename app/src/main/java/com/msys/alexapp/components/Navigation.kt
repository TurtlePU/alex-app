package com.msys.alexapp.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.msys.alexapp.repo.Role
import com.msys.alexapp.repo.Role.Companion.toRole
import com.msys.alexapp.ui.theme.AlexAppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface AlexAppService {
  suspend fun signIn(email: String, password: String)
  fun invitationsFrom(role: Role): Flow<List<String>>
}

@Composable
fun AlexAppService.NavComposable() {
  val navController = rememberNavController()
  NavHost(navController = navController, startDestination = "authorization") {
    composable("authorization") {
      object : AuthorizationService {
        override fun become(role: Role) = navController.navigate("invitations/$role")
        override suspend fun signIn(email: String, password: String) {
          this@NavComposable.signIn(email, password)
        }
      }.Authorization()
    }
    composable("invitations/{role}") { backStack ->
      object : InvitationService {
        override val role get() = backStack.arguments!!.getString("role")!!.toRole()!!
        override fun invitationsFrom(role: Role) = this@NavComposable.invitationsFrom(role)
      }.Invitations()
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
      override fun invitationsFrom(role: Role) = flowOf(listOf("wow"))
    }.NavComposable()
  }
}