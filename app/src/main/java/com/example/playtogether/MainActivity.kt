package com.example.playtogether

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth

// Data Classes
data class Sport(
    val name: String,
    val location: String,
    val time: String,
    var availableMembers: Int,
    var requiredMembers: Int,
    val imageResId: Int? = 0, // Default drawable resource ID
    val imageUri: String? = null // URI for picked images
)

// ViewModel
class PlaytogetherViewModel : ViewModel() {
    var sports by mutableStateOf(
        listOf(
            Sport(
                "Basketball", "Halifax", "4pm", 10,
                1, R.drawable.basketball
            ),
            Sport(
                "Soccer", "Bedford", "5pm", 9,
                2, R.drawable.soccer
            ),
            Sport(
                "Cricket", "Dartmouth", "6pm", 11,
                0, R.drawable.cricket
            )
        )
    )

    fun updateSport(updatedSport: Sport) {
        sports = sports.map { sport ->
            if (sport.name == updatedSport.name) updatedSport else sport
        }
    }

    fun deleteSport(sportName: String) {
        sports = sports.filter { it.name != sportName }
    }
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
    NavHost(navController, startDestination = "sign_in") {
        composable("sign_up") { SignUpScreen(navController) }
        composable("sign_in") { SignInScreen(navController) }
        composable("main") { MainScreen(navController, viewModel) }
        composable("admin_dashboard") { AdminDashboardScreen(viewModel, navController) }
        composable("congratulations") { CongratulationsScreen(navController) }
        composable("sport_details/{sportName}") { backStackEntry ->
            val sportName = backStackEntry.arguments?.getString("sportName") ?: ""
            val sport = viewModel.sports.find { it.name == sportName }
            if (sport != null) {
                SportDetailsScreen(sport)
            }
        }
        composable("join_confirmation/{sportName}") { backStackEntry ->
            val sportName = backStackEntry.arguments?.getString("sportName") ?: ""
            JoinConfirmationScreen(sportName, navController)
        }
        composable("edit_sport/{sportName}") { backStackEntry ->
            val sportName = backStackEntry.arguments?.getString("sportName") ?: ""
            val sport = viewModel.sports.find { it.name == sportName }
            if (sport != null) {
                EditSportDialog(
                    sport, onDismiss = { navController.popBackStack() }, onSave =
                    { updatedSport ->
                    viewModel.updateSport(updatedSport)
                    navController.popBackStack()
                })
            }
        }
    }
}

@Composable
fun SignUpScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
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
                Text(
                    text = errorMessage, color = Color.Red, modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                )
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
                                navController.navigate("congratulations")
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
    val auth = FirebaseAuth.getInstance()
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
                Text(
                    text = errorMessage, color = Color.Red, modifier =
                    Modifier.align(Alignment.CenterHorizontally)
                )
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
                                val user = auth.currentUser
                                if (user?.email == "admin@gmail.com") {
                                    navController.navigate("admin_dashboard")
                                } else {
                                    navController.navigate("main")
                                }
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
    Column(modifier = Modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(viewModel.sports) { sport ->
                SportCard(
                    sport = sport, navController = navController, isAdmin = false,
                    viewModel = viewModel
                )
            }
        }
    }
}


@Composable
fun AdminDashboardScreen(viewModel: PlaytogetherViewModel, navController: NavController) {
    var showAddSportDialog by remember { mutableStateOf(false) }
    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { showAddSportDialog = true }) {
            Text("Add Sport")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(viewModel.sports) { sport ->
                SportCard(
                    sport = sport, navController = navController, isAdmin = true,
                    viewModel = viewModel
                )
            }
        }

        if (showAddSportDialog) {
            AddSportDialog(
                onDismiss = { showAddSportDialog = false },
                onAdd = { newSport ->
                    viewModel.sports += newSport
                    showAddSportDialog = false
                }
            )
        }
    }
}


@Composable
fun SportDetailsScreen(sport: Sport) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Sport Details", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Name: ${sport.name}")
        Text("Location: ${sport.location}")
        Text("Time: ${sport.time}")
        Text("Available Members: ${sport.availableMembers}")
        Text("Required Members: ${sport.requiredMembers}")
        // Add more details as needed
    }
}

@Composable
fun JoinConfirmationScreen(sportName: String, navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Join Confirmation", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("You have successfully joined $sportName!")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Go Back")
        }
    }
}


@Composable
fun CongratulationsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Congratulations! You have successfully signed up!",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("sign_in") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go to Sign In")
        }
    }
}
@Composable
fun ConfirmationDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = message)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
fun EditSportDialog(
    sport: Sport,
    onDismiss: () -> Unit,
    onSave: (Sport) -> Unit
) {
    var name by remember { mutableStateOf(sport.name) }
    var location by remember { mutableStateOf(sport.location) }
    var time by remember { mutableStateOf(sport.time) }
    var availableMembers by remember { mutableStateOf(sport.availableMembers.toString()) }
    var requiredMembers by remember { mutableStateOf(sport.requiredMembers.toString()) }
    var imageUri by remember { mutableStateOf(sport.imageUri) }

    // Image picker related state and launcher
//    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it.toString()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Edit Sport", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                TextField(value = name, onValueChange = { name = it },
                    label = { Text("Sport Name") })
                Spacer(modifier = Modifier.height(8.dp))

                TextField(value = location, onValueChange = { location = it },
                    label = { Text("Location") })
                Spacer(modifier = Modifier.height(8.dp))

                TextField(value = time, onValueChange = { time = it },
                    label = { Text("Time") })
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = availableMembers,
                    onValueChange = { availableMembers = it },
                    label = { Text("Available Members") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = requiredMembers,
                    onValueChange = { requiredMembers = it },
                    label = { Text("Required Members") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Display selected image or default image
                val painter: Painter? = if (imageUri != null) {
                    rememberAsyncImagePainter(imageUri)
                } else {
                    sport.imageResId?.let { painterResource(id = it) } // Replace with your default image resource
                }
                if (painter != null) {
                    Image(
                        painter = painter,
                        contentDescription = "Sport Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Button to pick an image
                Button(onClick = {
                    imagePickerLauncher.launch("image/*")
                }) {
                    Text("Pick Image")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        val updatedSport = sport.copy(
                            name = name,
                            location = location,
                            time = time,
                            availableMembers = availableMembers.toIntOrNull() ?: 0,
                            requiredMembers = requiredMembers.toIntOrNull() ?: 0,
                            imageUri = imageUri
                        )
                        onSave(updatedSport)
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun AddSportDialog(
    onDismiss: () -> Unit,
    onAdd: (Sport) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var availableMembers by remember { mutableStateOf("") }
    var requiredMembers by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    // Image picker related state and launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it.toString()
        }
    }
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Add New Sport", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                TextField(value = name, onValueChange = { name = it },
                    label = { Text("Sport Name") })
                Spacer(modifier = Modifier.height(8.dp))

                TextField(value = location, onValueChange = { location = it },
                    label = { Text("Location") })
                Spacer(modifier = Modifier.height(8.dp))

                TextField(value = time, onValueChange = { time = it },
                    label = { Text("Time") })
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = availableMembers,
                    onValueChange = { availableMembers = it },
                    label = { Text("Available Members") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = requiredMembers,
                    onValueChange = { requiredMembers = it },
                    label = { Text("Required Members") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Display selected image or default image
                val painter: Painter = if (imageUri != null) {
                    rememberAsyncImagePainter(imageUri)
                } else {
                    painterResource(id = R.drawable.cricket)
                }
                Image(
                    painter = painter,
                    contentDescription = "Sport Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Button to pick an image
                Button(onClick = {
                    imagePickerLauncher.launch("image/*")
                }) {
                    Text("Pick Image")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(onClick = {
                        if (name.isBlank() || location.isBlank() || time.isBlank() ||
                            availableMembers.isBlank() || requiredMembers.isBlank()
                        ) {
                            errorMessage = "Please fill out all the input fields."
                            showErrorDialog = true
                        } else {
                            // Create the new Sport object safely
                            val newSport = Sport(
                                name = name,
                                location = location,
                                time = time,
                                availableMembers = availableMembers.toIntOrNull() ?: 0,
                                requiredMembers = requiredMembers.toIntOrNull() ?: 0,
                                imageUri = imageUri // Nullable, so it can be null
                            )
                            onAdd(newSport)

                        }
                    }) {
                        Text("Add")
                    }
                }
            }
        }
    }
}


@Composable
fun SportCard(
    sport: Sport,
    navController: NavController,
    isAdmin: Boolean,
    viewModel: PlaytogetherViewModel
) {
    var showJoinConfirmation by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showAddSportDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { navController.navigate("sport_details/${sport.name}") },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val painter = if (sport.imageUri != null) {
                rememberAsyncImagePainter(sport.imageUri)
            } else {
                sport.imageResId?.let { painterResource(id = it) }
            }

            if (painter != null) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = sport.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "Location: ${sport.location}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Time: ${sport.time}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Available: ${sport.availableMembers}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Required: ${sport.requiredMembers}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))

            if (isAdmin) {
                // Admin options: Edit and Delete
                Button(onClick = { showEditDialog = true }) {
                    Text("Edit Sport")
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { showDeleteConfirmation = true }) {
                    Text("Delete Sport")
                }

                if (showDeleteConfirmation) {
                    ConfirmationDialog(
                        message = "Are you sure you want to delete ${sport.name}?",
                        onConfirm = {
                            viewModel.deleteSport(sport.name)
                            navController.currentBackStackEntry// Pop back to the previous screen
                            showDeleteConfirmation = false // Dismiss the dialog
                        },
                        onDismiss = { showDeleteConfirmation = false } // Dismiss the dialog
                    )
                }
            } else if (sport.requiredMembers > 0) {
                // Normal user option: Join
                Button(onClick = { showJoinConfirmation = true }) {
                    Text("Join Sport")
                }

                if (showJoinConfirmation) {
                    ConfirmationDialog(
                        message = "Do you really want to join ${sport.name}?",
                        onConfirm = {
                            sport.availableMembers += 1
                            sport.requiredMembers -= 1

                            // Handle the joining logic here
                            navController.navigate("join_confirmation/${sport.name}")
                        },
                        onDismiss = { showJoinConfirmation = false }
                    )
                }
            }

            if (showEditDialog) {
                EditSportDialog(
                    sport = sport,
                    onDismiss = { showEditDialog = false },
                    onSave = { updatedSport ->
                        viewModel.updateSport(updatedSport)
                        showEditDialog = false
                    }
                )
            }
            if (showAddSportDialog) {
                AddSportDialog(
                    onDismiss = { showAddSportDialog = false },
                    onAdd = { newSport ->
                        viewModel.sports += newSport
                        showAddSportDialog = false
                    })
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        PlaytogetherApp()
    }
}

