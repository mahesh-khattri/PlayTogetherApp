package com.example.playtogether

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import com.example.playtogether.ui.theme.PlaytogetherTheme
import com.google.firebase.auth.FirebaseAuth

// Data Classes
data class Sport(
    val name: String,
    val location: String,
    val time: String,
    val availableMembers: Int,
    val requiredMembers: Int,
    val imageResId: Int
)

// ViewModel
class PlaytogetherViewModel : ViewModel() {
    var sports by mutableStateOf(
        listOf(
            Sport("Basketball", "Halifax", "4pm", 10, 1, R.drawable.basketball),
            Sport("Soccer", "Bedford", "5pm", 9, 2, R.drawable.soccer),
            Sport("Cricket", "Dartmouth", "6pm", 11, 0, R.drawable.cricket)
        )
    )
}

// Main Activity
class Playtogether : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PlaytogetherApp()
            }
        }
    }
}

// Composable Functions
@Composable
fun PlaytogetherApp() {
    val navController = rememberNavController()
    val viewModel: PlaytogetherViewModel = viewModel()

    NavHost(navController, startDestination = "sign_up") {
        composable("sign_up") { SignUpScreen(navController) }
        composable("sign_in") { SignInScreen(navController) }
        composable("main") { MainScreen(navController, viewModel) }
        composable("sport_details/{sportName}") { backStackEntry ->
            val sportName = backStackEntry.arguments?.getString("sportName") ?: ""
            val sport = viewModel.sports.find { it.name == sportName }
            if (sport != null) {
                SportDetailsScreen(sport, navController)
            }
        }
        composable("join_confirmation/{sportName}") { backStackEntry ->
            val sportName = backStackEntry.arguments?.getString("sportName") ?: ""
            JoinConfirmationScreen(sportName, navController)
        }
    }
}

@Composable
fun SignUpScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var auth = FirebaseAuth.getInstance()
    var errorMessage by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    val signupPainter: Painter = painterResource(id = R.drawable.basketball)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Image(
            painter = signupPainter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.2f)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Sign Up", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = Color.Red, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(onClick = {
                if (email.isEmpty() || password.isEmpty()) {
                    errorMessage = "Email and Password cannot be empty"
                } else {
                    isProcessing = true
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isProcessing = false
                            if (task.isSuccessful) {
                                navController.navigate("main")
                            } else {
                                errorMessage = task.exception?.localizedMessage ?: "Sign Up failed"
                            }
                        }
                }
            }, enabled = !isProcessing) {
                Text("Sign Up")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { navController.navigate("sign_in") }) {
                Text("Already have an account? Sign In")
            }
        }
    }
}

@Composable
fun SignInScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var auth = FirebaseAuth.getInstance()
    var errorMessage by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    val signinPainter: Painter = painterResource(id = R.drawable.cricket)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Image(
            painter = signinPainter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.2f)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Sign In", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = Color.Red, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(onClick = {
                if (email.isEmpty() || password.isEmpty()) {
                    errorMessage = "Email and Password cannot be empty"
                } else {
                    isProcessing = true
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isProcessing = false
                            if (task.isSuccessful) {
                                navController.navigate("main")
                            } else {
                                errorMessage = "User not found or incorrect password"
                            }
                        }
                }
            }, enabled = !isProcessing) {
                Text("Sign In")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { navController.navigate("sign_up") }) {
                Text("Don't have an account? Sign Up")
            }
        }
    }
}

@Composable
fun MainScreen(navController: NavController, viewModel: PlaytogetherViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Available Sports", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(viewModel.sports) { sport ->
                SportCard(sport) { selectedSport ->
                    navController.navigate("sport_details/${selectedSport.name}")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SportCard(sport: Sport, onJoinClicked: (Sport) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = sport.imageResId),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(sport.name, style = MaterialTheme.typography.titleMedium)
                Text("Location: ${sport.location}")
                Text("Time: ${sport.time}")
                Text("Available Members: ${sport.availableMembers}")
                Text("Required Members: ${sport.requiredMembers}")
            }
            Button(onClick = { onJoinClicked(sport) }) {
                Text("Join")
            }
        }
    }
}

@Composable
fun SportDetailsScreen(sport: Sport, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(sport.name, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Location: ${sport.location}")
        Text("Time: ${sport.time}")
        Text("Available Members: ${sport.availableMembers}")
        Text("Required Members: ${sport.requiredMembers}")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("join_confirmation/${sport.name}") }) {
            Text("Join")
        }
    }
}

@Composable
fun JoinConfirmationScreen(sportName: String, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("You have successfully joined the $sportName event!")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("main") }) {
            Text("Back to Home")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PlaytogetherTheme {
        SignUpScreen(navController = rememberNavController())
    }
}


