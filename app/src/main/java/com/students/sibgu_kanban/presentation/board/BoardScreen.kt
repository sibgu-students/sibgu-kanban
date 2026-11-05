
package com.students.sibgu_kanban.presentation.board

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.students.sibgu_kanban.domain.model.Task
import com.students.sibgu_kanban.domain.model.TaskStatus
import kotlinx.coroutines.launch



// Размеры шрифтов
private val HEADER_FONT = 30.sp
private val TASK_TITLE_FONT = 22.sp
private val STATUS_FONT = 18.sp
private val DIALOG_TITLE_FONT = 24.sp
private val BODY_FONT = 18.sp
private val BUTTON_FONT = 18.sp

// Цвет каждого статуса
fun getStatusColor(status: TaskStatus): Color {
    return when (status) {
        TaskStatus.TODO -> Color(0xFFEAB308)
        TaskStatus.IN_PROGRESS -> Color(0xFF3B82F6)
        TaskStatus.DONE -> Color(0xFF22C55E)
    }
}

@Composable
fun BoardScreen() {

    // Изначально список пустой
    val tasks = remember {
        mutableStateListOf<Task>()
    }

    var nextId by remember {
        mutableIntStateOf(1)
    }

    // Задача для просмотра
    var selectedTask by remember {
        mutableStateOf<Task?>(null)
    }

    // Задача для редактирования
    var editingTask by remember {
        mutableStateOf<Task?>(null)
    }

    // Окно создания
    var showAddDialog by remember {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp) {
                Text(
                    text = "Задачи",
                    fontSize = HEADER_FONT,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 36.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        },

        bottomBar = {
            Button(
                onClick = {
                    showAddDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(min = 56.dp)
            ) {
                Text(
                    text = "Добавить задачу",
                    fontSize = BUTTON_FONT
                )
            }
        }
    ) { padding ->

        if (tasks.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Задач пока нет",
                    fontSize = BODY_FONT,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = tasks,
                    key = { it.id }
                ) { task ->

                    TaskItem(
                        task = task,

                        onClick = {
                            selectedTask = task
                        },

                        onDelete = {
                            tasks.remove(task)

                            if (selectedTask?.id == task.id) {
                                selectedTask = null
                            }
                        },

                        onEdit = {
                            editingTask = task
                        }
                    )
                }
            }
        }
    }


    // Просмотр подробностей
    selectedTask?.let { task ->

        AlertDialog(
            onDismissRequest = {
                selectedTask = null
            },

            title = {
                Text(
                    text = task.title,
                    fontSize = DIALOG_TITLE_FONT,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 30.sp
                )
            },

            text = {
                Column {

                    Text(
                        text = "Описание:",
                        fontSize = BODY_FONT,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = task.description.ifBlank {
                            "Описание отсутствует"
                        },
                        fontSize = BODY_FONT,
                        lineHeight = 25.sp
                    )

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "Статус:",
                        fontSize = BODY_FONT,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = task.status.title,
                        fontSize = STATUS_FONT,
                        fontWeight = FontWeight.Bold,
                        color = getStatusColor(task.status)
                    )
                }
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        selectedTask = null
                    }
                ) {
                    Text(
                        text = "Закрыть",
                        fontSize = BUTTON_FONT
                    )
                }
            }
        )
    }


    // Создание новой задачи
    if (showAddDialog) {

        TaskEditorDialog(
            initialTask = null,

            onDismiss = {
                showAddDialog = false
            },

            onSave = { title, description, status ->

                tasks.add(
                    Task(
                        id = nextId,
                        title = title,
                        description = description,
                        status = status
                    )
                )

                nextId++
                showAddDialog = false
            }
        )
    }


    // Редактирование существующей задачи
    editingTask?.let { task ->

        TaskEditorDialog(
            initialTask = task,

            onDismiss = {
                editingTask = null
            },

            onSave = { title, description, status ->

                val index = tasks.indexOfFirst {
                    it.id == task.id
                }

                if (index != -1) {

                    tasks[index] = task.copy(
                        title = title,
                        description = description,
                        status = status
                    )
                }

                editingTask = null
            }
        )
    }
}


// Карточка задачи со свайпами
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(
    task: Task,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->

            when (value) {

                // Свайп влево — удаление
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }

                // Свайп вправо — редактирование
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false
                }

                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,

        backgroundContent = {

            val direction = dismissState.dismissDirection

            val backgroundColor = when (direction) {
                SwipeToDismissBoxValue.StartToEnd ->
                    Color(0xFF22C55E)

                SwipeToDismissBoxValue.EndToStart ->
                    Color(0xFFEF4444)

                else ->
                    Color.Transparent
            }

            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd ->
                    Alignment.CenterStart

                else ->
                    Alignment.CenterEnd
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment
            ) {

                when (direction) {

                    SwipeToDismissBoxValue.StartToEnd -> {
                        Text(
                            text = "✏️ Изменить",
                            fontSize = BODY_FONT,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    SwipeToDismissBoxValue.EndToStart -> {
                        Text(
                            text = "🗑️ Удалить",
                            fontSize = BODY_FONT,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    else -> {}
                }
            }
        }
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                },
            shape = RoundedCornerShape(12.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = task.title,
                        fontSize = TASK_TITLE_FONT,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 28.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = task.status.title,
                        fontSize = STATUS_FONT,
                        fontWeight = FontWeight.Medium,
                        color = getStatusColor(task.status)
                    )
                }

                IconButton(
                    onClick = onDelete
                ) {
                    Text(
                        text = "🗑️",
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}


// Универсальное окно добавления и редактирования
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskEditorDialog(
    initialTask: Task?,
    onDismiss: () -> Unit,
    onSave: (String, String, TaskStatus) -> Unit
) {

    var title by remember(initialTask) {
        mutableStateOf(initialTask?.title ?: "")
    }

    var description by remember(initialTask) {
        mutableStateOf(initialTask?.description ?: "")
    }

    val statuses = TaskStatus.entries

    val initialStatus = initialTask?.status
        ?: TaskStatus.TODO

    val initialPage = statuses.indexOf(initialStatus)

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { statuses.size }
    )

    val scope = rememberCoroutineScope()

    val selectedStatus = statuses[pagerState.currentPage]

    val isEditing = initialTask != null

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                text = if (isEditing) {
                    "Редактировать задачу"
                } else {
                    "Новая задача"
                },
                fontSize = DIALOG_TITLE_FONT,
                fontWeight = FontWeight.Bold,
                lineHeight = 30.sp
            )
        },

        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                // Название
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                    },
                    label = {
                        Text(
                            text = "Название",
                            fontSize = 16.sp
                        )
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = BODY_FONT
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                // Описание
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    label = {
                        Text(
                            text = "Описание",
                            fontSize = 16.sp
                        )
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = BODY_FONT
                    ),
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Статус задачи:",
                    fontSize = BODY_FONT,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(14.dp))

                // Карусель выбора статуса
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    val pageWidth = maxWidth * 0.60f

                    val sidePadding =
                        (maxWidth - pageWidth) / 2

                    HorizontalPager(
                        state = pagerState,
                        pageSize = PageSize.Fixed(pageWidth),
                        contentPadding = PaddingValues(
                            horizontal = sidePadding
                        ),
                        pageSpacing = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->

                        val item = statuses[page]

                        val isSelected =
                            pagerState.currentPage == page

                        val statusColor = getStatusColor(item)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 68.dp)
                                .alpha(
                                    if (isSelected) 1f
                                    else 0.35f
                                )
                                .clickable {
                                    scope.launch {
                                        pagerState.animateScrollToPage(
                                            page
                                        )
                                    }
                                },

                            shape = RoundedCornerShape(14.dp),

                            color = if (isSelected) {
                                statusColor.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },

                            border = if (isSelected) {
                                BorderStroke(
                                    2.dp,
                                    statusColor
                                )
                            } else {
                                null
                            }
                        ) {

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(4.dp)
                            ) {

                                Text(
                                    text = item.title,
                                    fontSize = STATUS_FONT,
                                    lineHeight = 22.sp,
                                    color = statusColor,
                                    fontWeight = if (isSelected) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Индикаторы под каруселью
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    statuses.forEachIndexed { index, _ ->

                        val isSelected =
                            pagerState.currentPage == index

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(
                                    width = if (isSelected) {
                                        16.dp
                                    } else {
                                        6.dp
                                    },
                                    height = 6.dp
                                )
                                .background(
                                    color = if (isSelected) {
                                        getStatusColor(statuses[index])
                                    } else {
                                        MaterialTheme.colorScheme
                                            .onSurface.copy(alpha = 0.3f)
                                    },
                                    shape = RoundedCornerShape(50)
                                )
                        )
                    }
                }
            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        title.trim(),
                        description.trim(),
                        selectedStatus
                    )
                },
                enabled = title.isNotBlank()
            ) {
                Text(
                    text = if (isEditing) {
                        "Сохранить"
                    } else {
                        "Добавить"
                    },
                    fontSize = BUTTON_FONT
                )
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Отмена",
                    fontSize = BUTTON_FONT
                )
            }
        }
    )
}
