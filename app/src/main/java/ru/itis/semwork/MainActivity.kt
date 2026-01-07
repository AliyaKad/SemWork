package ru.itis.semwork

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import ru.itis.core.models.Poem
import ru.itis.feature.auth.api.AuthRepository
import ru.itis.feature.auth.impl.presentation.AuthScreen
import ru.itis.feature.auth.impl.presentation.SettingsScreen
import ru.itis.feature.favorites.impl.presentation.PoemDetailScreen
import ru.itis.feature.creator.impl.presentation.CreatorScreen
import ru.itis.feature.creator.impl.presentation.MyPoemDetailScreen
import ru.itis.feature.creator.impl.presentation.MyPoemsScreen
import ru.itis.feature.favorites.impl.presentation.FavoritesScreen
import ru.itis.feature.random.impl.presentation.RandomPoemScreen
import ru.itis.feature.search.impl.presentation.SearchScreen
import java.net.URLDecoder
import java.net.URLEncoder

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val gson = Gson()

    NavHost(
        navController = navController,
        startDestination = "session_check"
    ) {
        composable("session_check") {
            SessionCheckScreen(
                onLoggedIn = {
                    navController.navigate("main") {
                        popUpTo("session_check") { inclusive = true }
                    }
                },
                onLoggedOut = {
                    navController.navigate("auth") {
                        popUpTo("session_check") { inclusive = true }
                    }
                }
            )
        }

        composable("auth") {
            AuthScreen(
                onNavigateToHome = {
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                onShowError = { message ->
                    Log.e("AuthScreen", "Error: $message")
                },
                onShowMessage = { message ->
                    Log.d("AuthScreen", "Message: $message")
                }
            )
        }

        composable("main") {
            MainScreen(
                onFeatureClick = { route ->
                    when (route) {
                        "random_poem" -> navController.navigate("random_poem")
                        "search" -> navController.navigate("search")
                        "favorites" -> navController.navigate("favorites")
                        "my_poems" -> navController.navigate("my_poems")
                        "creator" -> navController.navigate("creator")
                        "settings" -> navController.navigate("settings")
                    }
                }
            )
        }

        composable("random_poem") {
            RandomPoemScreen()
        }

        composable("search") {
            SearchScreen(
                onNavigateToPoem = { poem ->
                    val poemJson = gson.toJson(poem)
                    val encodedJson = URLEncoder.encode(poemJson, "UTF-8")
                    navController.navigate("poem_detail/$encodedJson")
                },
                onShowMessage = { message ->
                    Log.d("SearchScreen", "Message: $message")
                }
            )
        }

        composable(
            route = "poem_detail/{poemJson}",
            arguments = listOf(
                navArgument("poemJson") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedJson = backStackEntry.arguments?.getString("poemJson") ?: ""

            val (poem, error) = remember(encodedJson) {
                try {
                    val poemJson = URLDecoder.decode(encodedJson, "UTF-8")
                    val poem = gson.fromJson(poemJson, Poem::class.java)
                    poem to null
                } catch (e: Exception) {
                    null to e.message
                }
            }

            if (poem != null) {
                PoemDetailScreen(
                    poem = poem,
                    onNavigateBack = { navController.navigateUp() }
                )
            } else {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Ошибка") },
                            navigationIcon = {
                                IconButton(
                                    onClick = { navController.navigateUp() }
                                ) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Назад"
                                    )
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Ошибка",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Ошибка загрузки стихотворения")
                            error?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        composable("favorites") {
            FavoritesScreen(
                onNavigateToPoem = { poem ->
                    val poemJson = gson.toJson(poem)
                    val encodedJson = URLEncoder.encode(poemJson, "UTF-8")
                    navController.navigate("poem_detail/$encodedJson")
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable("my_poems") {
            MyPoemsScreen(
                onNavigateToPoem = { poem ->
                    val poemJson = gson.toJson(poem)
                    val encodedJson = URLEncoder.encode(poemJson, "UTF-8")
                    navController.navigate("my_poem_detail/$encodedJson")
                },
                onNavigateToCreatePoem = {
                    navController.navigate("creator")
                },
                onNavigateBack = {
                    navController.navigateUp()
                },
                onShowMessage = { message ->
                    Log.d("MyPoems", "Message: $message")
                },
                onShowDeleteDialog = { poem, onConfirm ->
                }
            )
        }

        composable(
            route = "my_poem_detail/{poemJson}",
            arguments = listOf(
                navArgument("poemJson") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val poemJson = backStackEntry.arguments?.getString("poemJson")
            val decodedJson = URLDecoder.decode(poemJson, "UTF-8")
            val poem = gson.fromJson(decodedJson, Poem::class.java)

            MyPoemDetailScreen(
                poem = poem,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable("creator") {
            CreatorScreen(
                onNavigateBack = { navController.navigateUp() },
                onShowSuccess = {
                    navController.navigate("my_poems") {
                        popUpTo("my_poems") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onShowError = { message ->
                    Log.e("CreatorScreen", "Error: $message")
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.navigateUp() },
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo("settings") { inclusive = true }
                    }
                }
            )
        }
    }
}
