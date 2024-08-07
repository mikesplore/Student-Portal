package com.mike.unikonnect

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.unikonnect.model.Chat
import com.mike.unikonnect.model.Update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.mike.unikonnect.model.MyCode
import com.mike.unikonnect.model.User
import com.mike.unikonnect.model.Course
import com.mike.unikonnect.model.AccountDeletion
import com.mike.unikonnect.model.Day
import com.mike.unikonnect.model.AttendanceState
import com.mike.unikonnect.model.Announcement
import com.mike.unikonnect.model.Assignment
import com.mike.unikonnect.model.Screens
import com.mike.unikonnect.model.Attendance
import com.mike.unikonnect.model.Fcm
import com.mike.unikonnect.model.Feedback
import com.mike.unikonnect.model.Message
import com.mike.unikonnect.model.GridItem
import com.mike.unikonnect.model.ScreenTime
import com.mike.unikonnect.model.Section
import com.mike.unikonnect.model.Timetable
import com.mike.unikonnect.model.UserPreferences
import com.mike.unikonnect.ui.theme.GlobalColors


object MyDatabase {
    val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var calendar: Calendar = Calendar.getInstance()
    private var year = calendar.get(Calendar.YEAR)


    // index number
    fun generateIndexNumber(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val indexNumber = "CP$newCode$year"
            onIndexNumberGenerated(indexNumber) // Pass the generated index number to the callback
        }
    }

    fun generateChatID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val indexNumber = "CH$newCode$year"
            onIndexNumberGenerated(indexNumber) // Pass the generated index number to the callback
        }
    }

    fun generateAccountDeletionID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val indexNumber = "AD$newCode$year"
            onIndexNumberGenerated(indexNumber) // Pass the generated index number to the callback
        }
    }

    fun generateFCMID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val indexNumber = "FC$newCode$year"
            onIndexNumberGenerated(indexNumber) // Pass the generated index number to the callback
        }
    }

    fun generateSharedPreferencesID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val indexNumber = "SP$newCode$year"
            onIndexNumberGenerated(indexNumber) // Pass the generated index number to the callback
        }
    }

    fun generateFeedbackID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val indexNumber = "FB$newCode$year"
            onIndexNumberGenerated(indexNumber) // Pass the generated index number to the callback
        }
    }

    private fun generateAttendanceID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val indexNumber = "AT$newCode$year"
            onIndexNumberGenerated(indexNumber) // Pass the generated index number to the callback
        }
    }

    fun generateScreenTimeID(onLastDateGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val dateCode = "ST$newCode$year"
            onLastDateGenerated(dateCode) // Pass the generated index number to the callback
        }
    }


    //chats functions
    fun sendMessage(chat: Chat, onComplete: (Boolean) -> Unit) {
        database.child("Group Discussions").push().setValue(chat).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

    fun fetchChats(onChatsFetched: (List<Chat>) -> Unit) {
        database.child("Group Discussions")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chats = snapshot.children.mapNotNull { it.getValue(Chat::class.java) }
                    onChatsFetched(chats)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }


    private fun updateAndGetCode(onCodeUpdated: (Int) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("Code")

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val myCode = snapshot.getValue(MyCode::class.java)!!
                    myCode.code += 1
                    database.setValue(myCode).addOnSuccessListener {
                        onCodeUpdated(myCode.code) // Pass the incremented code to the callback
                    }
                } else {
                    val newCode = MyCode(code = 1)
                    database.setValue(newCode).addOnSuccessListener {
                        onCodeUpdated(newCode.code) // Pass the initial code to the callback
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error appropriately (e.g., log it or notify the user)
            }
        })
    }

    // Function to save Update to the "Update" node in the database
    fun saveUpdate(update: Update, onSuccess: () -> Unit, onFailure: (Exception?) -> Unit) {
        val updatesRef = database.child("Updates") // Reference to the "Updates" node
        updatesRef.setValue(update).addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Function to retrieve Update from the "Update" node
    fun getUpdate(onResult: (Update?) -> Unit) {
        val updatesRef = database.child("Updates")
        updatesRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val update = snapshot.getValue(Update::class.java)
                onResult(update)
            } else {
                onResult(null) // Handle the case where no update data exists
            }
        }.addOnFailureListener { exception ->
            // Handle potential errors during data retrieval
            onResult(null)
        }
    }

    fun fetchAttendanceState(courseCode: String, onStateFetched: (AttendanceState?) -> Unit) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("AttendanceStates").child(courseCode)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val attendanceState = snapshot.getValue(AttendanceState::class.java)
                    onStateFetched(attendanceState) // Pass the fetched state or null if not found
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error, maybe pass null to indicate failure
                    onStateFetched(null)
                }
            })
    }

    // Attendance functions
    fun signAttendance(
        studentID: String,
        courseCode: String,
        status: String,
        onResult: (Boolean) -> Unit
    ) {
        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        // Check if attendance has been signed today before saving
        checkAttendanceRecord(studentID, courseCode, today) { isSignedToday ->
            if (isSignedToday) {
                // Attendance already signed for today, do not save again
                onResult(false)
            } else {
                // Attendance not yet signed for today, proceed to save
                val attendanceRef =
                    database.child("Attendances").child(courseCode).child(studentID).push()
                val attendance = Attendance(
                    id = attendanceRef.key ?: "",
                    date = today,
                    status = status,
                    studentId = studentID
                )

                attendanceRef.setValue(attendance).addOnSuccessListener {
                        onResult(true) // Successfully signed attendance
                    }.addOnFailureListener {
                        onResult(false) // Failed to sign attendance
                    }
            }
        }
    }


    fun fetchAttendances(
        studentID: String, courseCode: String, onAttendanceFetched: (List<Attendance>) -> Unit
    ) {
        val attendanceRef = database.child("Attendances/$courseCode/$studentID")
        attendanceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val attendances =
                    snapshot.children.mapNotNull { it.getValue(Attendance::class.java) }
                onAttendanceFetched(attendances)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error, maybe pass an empty list or an error state to the callback
                onAttendanceFetched(emptyList())
            }
        })
    }


    private fun checkAttendanceRecord(
        studentID: String,
        courseCode: String,
        date: String,
        onResult: (Boolean) -> Unit
    ) {
        val key = "Attendances/$courseCode/$studentID"
        database.child(key).orderByChild("date").equalTo(date)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        onResult(true) // Attendance record found for today
                    } else {
                        onResult(false) // No attendance record found for today
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error checking attendance record: ${error.message}")
                    onResult(false) // Error occurred, consider it as no record found
                }
            })
    }


    fun sendUserToUserMessage(message: Message, path: String, onComplete: (Boolean) -> Unit) {
        database.child(path).push().setValue(message).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

    fun fetchUserToUserMessages(path: String, onMessagesFetched: (List<Message>) -> Unit) {
        database.child(path).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                onMessagesFetched(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun writeCourse(course: Course, onSuccess: () -> Unit) {
        database.child("Courses").child(course.courseCode).setValue(course).addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener { exception ->
            }
    }

    fun writeAccountDeletionData(accountDeletion: AccountDeletion, onSuccess: () -> Unit) {
        database.child("Account Deletion").child(accountDeletion.id).setValue(accountDeletion)
            .addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener { exception ->
            }
    }

    fun writeScren(courseScreen: Screens, onSuccess: () -> Unit) {
        database.child("Screens").child(courseScreen.screenId).setValue(courseScreen)
            .addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener {}
    }

    fun writePreferences(preferences: UserPreferences, onSuccess: () -> Unit) {
        database.child(" User Preferences").child(preferences.studentID).setValue(preferences)
            .addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener {}
    }

    fun fetchPreferences(userId: String, onPreferencesFetched: (UserPreferences?) -> Unit) {
        database.child(" User Preferences").child(userId).get()
            .addOnSuccessListener { snapshot ->
                val preferences = snapshot.getValue(UserPreferences::class.java)
                onPreferencesFetched(preferences)
            }
            .addOnFailureListener {
                onPreferencesFetched(null) // Handle failure, e.g., by returning null
            }
    }

    // Courses functions
    fun fetchCourses(onCoursesFetched: (List<Course>) -> Unit) {
        database.child("Courses").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val courseList = snapshot.children.mapNotNull { it.getValue(Course::class.java) }
                onCoursesFetched(courseList) // Call the callback with the fetched courses
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error, maybe pass an empty list or an error state to the callback
                onCoursesFetched(emptyList())
            }
        })
    }

    // User functions
    fun writeUsers(user: User, onComplete: (Boolean) -> Unit) {
        database.child("Users").child(user.id).setValue(user).addOnSuccessListener {
                onComplete(true) // Success: invoke callback with 'true'
            }.addOnFailureListener {
                onComplete(false) // Failure: invoke callback with 'false'
            }
    }

    //delete user
    fun deleteUser(userId: String, onComplete: (Boolean) -> Unit) {
        database.child("Users").child(userId).removeValue().addOnSuccessListener {
                onComplete(true) // Success: invoke callback with 'true'
            }.addOnFailureListener {
                onComplete(false) // Failure: invoke callback with 'false'
            }
    }

    fun getUsers(onUsersFetched: (List<User>?) -> Unit) {
        database.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
                onUsersFetched(users)
            }

            override fun onCancelled(error: DatabaseError) {
                onUsersFetched(null)
            }
        })
    }

    fun fetchUserDataByEmail(email: String, callback: (User?) -> Unit) {
        database.child("Users").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (userSnapshot in snapshot.children) {
                        val userEmail = userSnapshot.child("email").getValue(String::class.java)
                        if (userEmail == email) {
                            val userId = userSnapshot.child("id").getValue(String::class.java) ?: ""
                            val firstName =
                                userSnapshot.child("firstName").getValue(String::class.java) ?: ""
                            val lastName =
                                userSnapshot.child("lastName").getValue(String::class.java) ?: ""
                            val phoneNumber =
                                userSnapshot.child("phoneNumber").getValue(String::class.java) ?: ""
                            val gender =
                                userSnapshot.child("gender").getValue(String::class.java) ?: ""
                            val profileImageLink =
                                userSnapshot.child("profileImageLink").getValue(String::class.java) ?: ""

                            callback(
                                User(
                                    id = userId,
                                    email = userEmail,
                                    firstName = firstName,
                                    lastName = lastName,
                                    phoneNumber = phoneNumber,
                                    gender = gender,
                                    profileImageLink = profileImageLink

                                )
                            )
                            return
                        }
                    }
                    callback(null)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null) // Handle or log the error as needed
                }
            })
    }

    fun fetchUserDataByAdmissionNumber(admissionNumber: String, callback: (User?) -> Unit) {
        database.child("Users").orderByChild("id").equalTo(admissionNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (userSnapshot in snapshot.children) {
                        val userId = userSnapshot.child("id").getValue(String::class.java)
                        if (userId == admissionNumber) {
                            val userEmail =
                                userSnapshot.child("Email").getValue(String::class.java) ?: ""
                            val firstName =
                                userSnapshot.child("firstName").getValue(String::class.java) ?: ""
                            val lastName =
                                userSnapshot.child("lastName").getValue(String::class.java) ?: ""
                            val phoneNumber =
                                userSnapshot.child("phoneNumber").getValue(String::class.java) ?: ""
                            val gender =
                                userSnapshot.child("gender").getValue(String::class.java) ?: ""
                            val profileImageLink =
                                userSnapshot.child("profileImageLink").getValue(String::class.java) ?: ""
                            callback(
                                User(
                                    id = userId,
                                    email = userEmail,
                                    firstName = firstName,
                                    lastName = lastName,
                                    phoneNumber = phoneNumber,
                                    gender = gender,
                                    profileImageLink = profileImageLink
                                )
                            )
                            return
                        }
                    }
                    callback(null)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null) // Handle or log the error as needed
                }
            })
    }


    // Authentication functions
    fun updatePassword(newPassword: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.updatePassword(newPassword)?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener { exception -> onFailure(exception) }
    }

    // Items functions
    fun readItems(courseId: String, section: Section, onItemsRead: (List<GridItem>) -> Unit) {
        database.child("Course Resources").child(courseId).child(section.name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val items = snapshot.children.mapNotNull { it.getValue(GridItem::class.java) }
                    onItemsRead(items)
                }

                override fun onCancelled(error: DatabaseError) {
                    onItemsRead(emptyList())
                }
            })
    }

    // Feedback functions
    fun writeFeedback(feedback: Feedback, onSuccess: () -> Unit, onFailure: (Exception?) -> Unit) {
        val feedbackRef: DatabaseReference = database.child("Feedback").child(feedback.id)
        feedbackRef.setValue(feedback).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    //Screen time functions
    fun saveScreenTime(
        screenTime: ScreenTime,
        onSuccess: () -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        val screenTimeRef = database.child("ScreenTime").child(screenTime.id)
        screenTimeRef.setValue(screenTime).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    fun getScreenTime(screenID: String, onScreenTimeFetched: (ScreenTime?) -> Unit) {
        val screenTimeRef = database.child("ScreenTime").child(screenID)
        screenTimeRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val retrievedScreenTime = snapshot.getValue(ScreenTime::class.java)
                onScreenTimeFetched(retrievedScreenTime)
            } else {
                onScreenTimeFetched(null)
            }
        }.addOnFailureListener {
            onScreenTimeFetched(null)
        }
    }

    fun getAllScreenTimes(onScreenTimesFetched: (List<ScreenTime>) -> Unit) {
        val screenTimeRef = database.child("ScreenTime")
        screenTimeRef.get().addOnSuccessListener { snapshot ->
            val screenTimes = mutableListOf<ScreenTime>()
            for (childSnapshot in snapshot.children) {
                val screenTime = childSnapshot.getValue(ScreenTime::class.java)
                screenTime?.let { screenTimes.add(it) }
            }
            onScreenTimesFetched(screenTimes)
        }.addOnFailureListener {
            onScreenTimesFetched(emptyList()) // Return an empty list on failure
        }
    }

    fun getScreenDetails(screenID: String, onScreenDetailsFetched: (Screens?) -> Unit) {
        val screenDetailsRef = database.child("Screens").child(screenID)
        screenDetailsRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val screenDetails = snapshot.getValue(Screens::class.java)
                onScreenDetailsFetched(screenDetails)
            } else {
                onScreenDetailsFetched(null)
            }
        }.addOnFailureListener {
            onScreenDetailsFetched(null)
        }
    }

    fun fetchAverageRating(onAverageRatingFetched: (String) -> Unit) {
        val feedbackRef = database.child("Feedback")
        feedbackRef.get().addOnSuccessListener { snapshot ->
            var totalRating = 0.0
            var count = 0

            for (childSnapshot in snapshot.children) {
                val feedback = childSnapshot.getValue(Feedback::class.java)
                feedback?.rating?.let {
                    totalRating += it
                    count++
                }
            }

            val averageRating = if (count > 0) totalRating / count else 0.0
            val formattedAverage = String.format(Locale.US, "%.1f", averageRating)
            onAverageRatingFetched(formattedAverage)
        }.addOnFailureListener {
            onAverageRatingFetched(String.format(Locale.US, "%.1f", 0.0))
        }
    }

    // FCM functions
    fun writeFcmToken(token: Fcm) {
        database.child("FCM").child(token.id).setValue(token)
    }

    // Timetable functions
    fun getTimetable(dayId: String, onAssignmentsFetched: (List<Timetable>?) -> Unit) {
        database.child("Timetable").orderByChild("dayId").equalTo(dayId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val timetable =
                        snapshot.children.mapNotNull { it.getValue(Timetable::class.java) }
                    onAssignmentsFetched(timetable)
                }

                override fun onCancelled(error: DatabaseError) {
                    onAssignmentsFetched(null)
                }
            })
    }

    fun getCurrentDayTimetable(dayName: String, onTimetableFetched: (List<Timetable>?) -> Unit) {
        // Step 1: Fetch the dayId from the Day node using the dayName
        database.child("Days").orderByChild("name").equalTo(dayName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dayId = snapshot.children.firstOrNull()?.key

                    if (dayId != null) {
                        // Step 2: Use the fetched dayId to query the Timetable node
                        database.child("Timetable").orderByChild("dayId").equalTo(dayId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val timetable =
                                        snapshot.children.mapNotNull { it.getValue(Timetable::class.java) }
                                    onTimetableFetched(timetable)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    onTimetableFetched(null)
                                }
                            })
                    } else {
                        // Day not found
                        onTimetableFetched(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onTimetableFetched(null)
                }
            })
    }

    // Days functions
    fun getDays(onCoursesFetched: (List<Day>?) -> Unit) {
        database.child("Days").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val days = snapshot.children.mapNotNull { it.getValue(Day::class.java) }
                onCoursesFetched(days)
            }

            override fun onCancelled(error: DatabaseError) {
                onCoursesFetched(null)
            }
        })
    }

    // Assignment functions
    fun getAssignments(courseCode: String, onAssignmentsFetched: (List<Assignment>?) -> Unit) {
        database.child("Assignments").orderByChild("courseCode").equalTo(courseCode)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val assignments =
                        snapshot.children.mapNotNull { it.getValue(Assignment::class.java) }
                    onAssignmentsFetched(assignments)
                }

                override fun onCancelled(error: DatabaseError) {
                    onAssignmentsFetched(null)
                }
            })
    }

    // Announcements functions
    fun getAnnouncements(onUsersFetched: (List<Announcement>?) -> Unit) {
        database.child("Announcements").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val announcements =
                    snapshot.children.mapNotNull { it.getValue(Announcement::class.java) }
                onUsersFetched(announcements)
            }

            override fun onCancelled(error: DatabaseError) {
                onUsersFetched(null)
            }
        })
    }

}


fun ExitScreen(context: Context, screenID: String, timeSpent: Long){

    GlobalColors.loadColorScheme(context)
    // Fetch the screen details
    MyDatabase.getScreenDetails(screenID) { screenDetails ->
        if (screenDetails != null) {
            MyDatabase.writeScren(courseScreen = screenDetails) {}
            // Fetch existing screen time
            MyDatabase.getScreenTime(screenID) { existingScreenTime ->
                val totalScreenTime = if (existingScreenTime != null) {
                    Log.d("Screen Time", "Retrieved Screen time: $existingScreenTime")
                    existingScreenTime.time + timeSpent
                } else {
                    timeSpent
                }

                // Create a new ScreenTime object
                val screenTime = ScreenTime(
                    id = screenID,
                    screenName = screenDetails.screenName,
                    time = totalScreenTime
                )

                // Save the updated screen time
                MyDatabase.saveScreenTime(screenTime = screenTime, onSuccess = {
                    Log.d("Screen Time", "Saved $totalScreenTime to the database")
                }, onFailure = {
                    Log.d("Screen Time", "Failed to save $totalScreenTime to the database")
                })
            }

        } else {
            Log.d("Screen Time", "Screen details not found for ID: $screenID")
        }
    }


}



