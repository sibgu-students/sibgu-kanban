package com.students.sibgu_kanban

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.students.sibgu_kanban.ui.theme.SibgukanbanTheme
import com.students.sibgu_kanban.presentation.board.BoardScreen
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SibgukanbanTheme {
                BoardScreen()
            }
        }
    }
}

@Composable
fun FirebaseMessageScreen(modifier: Modifier = Modifier) {
    var message by remember { mutableStateOf("Загрузка…") }

    DisposableEffect(Unit) {
        // The test document uses an automatic ID, so read the first document.
        val registration = FirebaseFirestore.getInstance()
            .collection("test")
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                message = if (error != null) {
                    "Ошибка загрузки: ${error.localizedMessage}"
                } else {
                    val document = snapshot?.documents?.firstOrNull()
                    when {
                        document == null ->
                            "В коллекции test пока нет документов"

                        document.get("message") !is String ->
                            "Поле message отсутствует или не является строкой"

                        else ->
                            document.getString("message").orEmpty()
                    }
                }
            }

        onDispose { registration.remove() }
    }

    Text(text = message, modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun FirebaseMessagePreview() {
    SibgukanbanTheme {
        Text("Пример сообщения")
    }
}

@Preview(showBackground = true)
@Composable
fun BoardScreenPreview() {
    SibgukanbanTheme {
        BoardScreen()
    }
}
