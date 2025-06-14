package com.example.finalproject.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Rocket
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.finalproject.R
import androidx.navigation.NavController
import com.example.finalproject.data.PreferencesManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IntroSlider(navController: NavController) {
    val pagerState = rememberPagerState { 3 }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val onboardingPages = listOf(
        OnboardingPage(
            title = "Bem-vindo ao",
            description = "Perfis personalizados para Administrador, Gestor de Projeto e Utilizador",
            imageRes = R.drawable.logolight,
            icon = Icons.Outlined.Groups
        ),
        OnboardingPage(
            title = "Colaboração e Eficiência",
            description = "Organiza, atribui e acompanha tarefas e projetos num único lugar",
            imageRes = R.drawable.logolight,
            icon = Icons.Outlined.CheckCircle
        ),
        OnboardingPage(
            title = "Get Started",
            description = "You're all set to go!",
            imageRes = R.drawable.logolight,
            icon = Icons.Outlined.Rocket
        )
    )

    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(8.dp)
        ) {
            // Only show skip button on first 2 pages
            if (pagerState.currentPage < onboardingPages.size - 1) {
                TextButton(
                    onClick = {
                        PreferencesManager.setFirstLaunchComplete(context)
                        navController.navigate("login") {
                            popUpTo("intro_slider") { inclusive = true }
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text("Skip")
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageScreen(onboardingPages[page])
        }

        Box(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                Modifier.align(Alignment.CenterStart),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(onboardingPages.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(color, shape = androidx.compose.foundation.shape.CircleShape)
                    )
                }
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < onboardingPages.size - 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        // Navigate to login and mark intro as completed
                        PreferencesManager.setFirstLaunchComplete(context)
                        navController.navigate("login") {
                            popUpTo("intro_slider") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(if (pagerState.currentPage == onboardingPages.size - 1) "Finish" else "Next")
            }
        }
    }
}

@Composable
fun OnboardingPageScreen(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(180.dp)
                .padding(bottom = 24.dp)
        )

        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)