package com.mike.unikonnect

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mike.unikonnect.model.User
import com.mike.unikonnect.model.Details
import com.mike.unikonnect.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreDetails(context: Context, navController: NavController) {
    val database = MyDatabase.database.child("Users")
    var users by remember { mutableStateOf<List<User>?>(null) }
    val auth = FirebaseAuth.getInstance()
    val imageLink by remember { mutableStateOf(auth.currentUser?.photoUrl) }

    fun checkEmailExists(email: String, onResult: (Boolean) -> Unit) {
        val query = database.orderByChild("email").equalTo(email) // Query for the email
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val emailExists = snapshot.exists() // Check if the email exists
                onResult(emailExists) // Call the callback with the result
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
                onResult(false) // You might want to handle errors differently
            }
        })
    }

    var emailFound by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var addloading by remember { mutableStateOf(false) }
    var brush  = Brush.verticalGradient(
        colors = listOf(
            CC.primary(),
            CC.secondary()
        )
    )

    LaunchedEffect(Unit) {
        MyDatabase.getUsers { fetchedUsers ->
            users = fetchedUsers

            checkEmailExists(Details.email.value) { exists ->
                if (exists) {
                    // Find the user with the matching email
                    val existingUser = fetchedUsers?.find { it.email == Details.email.value }

                    if (existingUser != null) {
                        Details.firstName.value = existingUser.firstName
                        Details.lastName.value = existingUser.lastName
                        val userName =
                            existingUser.firstName+" "+ existingUser.lastName // Assuming your User class has a 'name' property
                        loading = false
                        Toast.makeText(context, "Welcome back, ${userName.substringBefore(' ')}!", Toast.LENGTH_SHORT)
                            .show()
                        navController.navigate("dashboard")
                    } else {
                        // Handle the case where the user is not found (shouldn't happen if email exists)
                        loading = false
                        Toast.makeText(
                            context,
                            "Unexpected error: User not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    loading = false
                    Toast.makeText(context, "Add your name", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = "Details", style = CC.titleTextStyle(context)) },
            navigationIcon = {
                IconButton(
                    onClick = { navController.navigate("login") },
                    modifier = Modifier.absolutePadding(left = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = CC.textColor(),
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = CC.primary())
        )
    }, containerColor = CC.primary()) {
            // main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush)
                    .padding(it),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    CC.SingleLinedTextField(
                        value = Details.firstName.value,
                        onValueChange = {
                            if (!emailFound) { // Only update if email is not found
                                Details.firstName.value = it
                            }
                        },
                        label = "First name",
                        context = context,
                        singleLine = true,
                        enabled = !emailFound // Disable the field if email is found
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    CC.SingleLinedTextField(
                        value = Details.lastName.value,
                        onValueChange = {
                            if (!emailFound) { // Only update if email is not found
                                Details.lastName.value = it
                            }
                        },
                        label = "Last name",
                        context = context,
                        singleLine = true,
                        enabled = !emailFound // Disable the field if email is found
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            addloading = true
                            MyDatabase.generateIndexNumber { indexNumber ->
                                val user = User(id = indexNumber, firstName = Details.firstName.value, lastName = Details.lastName.value, phoneNumber = "", email = Details.email.value, profileImageLink = imageLink.toString())
                                MyDatabase.writeUsers(user) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Added successfully", Toast.LENGTH_SHORT).show()
                                        addloading = false
                                        Details.firstName.value = ""
                                        Details.lastName.value = ""
                                        navController.navigate("dashboard")
                                    } else {
                                        Toast.makeText(context,"Failed to write user to database",Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }


                        }, modifier = Modifier
                            .width(275.dp),
                        colors = ButtonDefaults.buttonColors(CC.secondary()),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (loading || addloading) {
                                CircularProgressIndicator(
                                    color = CC.primary(),
                                    trackColor = CC.textColor(),
                                    modifier = Modifier.size(30.dp)
                                )
                                Spacer(modifier = Modifier.width(20.dp))
                            }
                            if (loading) {
                                Text("Checking Database", style = CC.descriptionTextStyle(context))
                            } else {
                                Text(
                                    if (addloading) "Adding" else "Add",
                                    style = CC.descriptionTextStyle(context)
                                )
                            }
                        }
                    }
                }
            }

    }
}

@Preview
@Composable
fun Extra() {
    MoreDetails(
        navController = rememberNavController(), context = LocalContext.current
    )
}
