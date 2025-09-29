package com.example.byeoldori.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.byeoldori.ui.screen.login.EmailVerificationScreen
import com.example.byeoldori.ui.screen.login.FindEmailScreen
import com.example.byeoldori.ui.screen.login.LoginScreen
import com.example.byeoldori.ui.screen.login.ResetPasswordScreen
import com.example.byeoldori.ui.screen.login.SignUpConsentScreen
import com.example.byeoldori.ui.screen.login.SignUpScreen
import com.example.byeoldori.ui.theme.Background
import com.example.byeoldori.viewmodel.login.SignUpViewModel

sealed class AuthRoute(val route: String) {
    data object Login   : AuthRoute("login")
    data object Consent : AuthRoute("consent")
    data object SignUp  : AuthRoute("signup")
    data object EmailVerification : AuthRoute("email_verification")
    data object FindEmail : AuthRoute("find_email")
    data object ResetPassword : AuthRoute("reset_password")
}

@Composable
fun AuthRoot() {
    val nav = rememberNavController()
    val signupvm: SignUpViewModel = hiltViewModel()

    Background(Modifier.fillMaxSize()) {
        NavHost(navController = nav, startDestination = AuthRoute.Login.route) {
            composable(AuthRoute.Login.route) {
                LoginScreen(
                    onSignUp = { nav.navigate(AuthRoute.Consent.route) },
                    onFindEmail = { nav.navigate(AuthRoute.FindEmail.route) },
                    onResetPassword = { nav.navigate(AuthRoute.ResetPassword.route) }
                )
            }
            composable(AuthRoute.Consent.route) {
                SignUpConsentScreen(
                    onNext = { nav.navigate(AuthRoute.SignUp.route) },
                    onBack = { nav.popBackStack() },
                    vm = signupvm
                )
            }
            composable(AuthRoute.SignUp.route) {
                SignUpScreen(
                    onNext = { nav.navigate(AuthRoute.EmailVerification.route) },
                    onBack = { nav.popBackStack() },
                    vm = signupvm
                )
            }
            composable(AuthRoute.EmailVerification.route) {
                EmailVerificationScreen(
                    onLogin = { nav.navigate(AuthRoute.Login.route) },
                    onBack = { nav.popBackStack() },
                    vm = signupvm
                )
            }
            composable(AuthRoute.FindEmail.route) {
                FindEmailScreen(
                    onBack = { nav.popBackStack() }
                )
            }
            composable(AuthRoute.ResetPassword.route) {
                ResetPasswordScreen(
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
}

