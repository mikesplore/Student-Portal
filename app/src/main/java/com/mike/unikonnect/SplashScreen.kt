package com.mike.unikonnect

import android.content.Context
import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import com.mike.unikonnect.CommonComponents as CC

@Composable
fun SplashScreen(navController: NavController, context: Context) {
    var startAnimation by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = tween(
            durationMillis = 800,
            easing = { OvershootInterpolator(2f).getInterpolation(it) }
        ), label = ""
    )
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val destination = if (currentUser != null) "dashboard" else "login"

    LaunchedEffect(Unit) {
        startAnimation = true // Assuming you have animation logic
        delay(3000)
        navController.navigate(destination) {
            popUpTo("splash") { inclusive = true } // Optional: Clear splash from back stack
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CC.primary(), // Top color
                        CC.secondary()  // Bottom color
                    )
                )
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.logo), // Replace with your logo
                contentDescription = "App Logo",
                tint = CC.extraColor2(),
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Uni Konnect",
                style = CC.titleTextStyle(context)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your Ultimate Educational Companion",
                style = CC.descriptionTextStyle(context)
            )
        }

        CircularProgressIndicator(
            color = CC.textColor(),
            trackColor = CC.primary(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen(navController = rememberNavController(), context = LocalContext.current)
}