package fr.uptrash.fuckupplanning.ui.homework

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import fr.uptrash.fuckupplanning.data.model.Homework
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkScreen(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    viewModel: HomeworkViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.homeworkList.isEmpty() -> {
                    EmptyState(modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.homeworkList) { homework ->
                            HomeworkCard(
                                homework = homework,
                                onToggleComplete = { viewModel.toggleHomeworkCompletion(homework) },
                                onDelete = { viewModel.deleteHomework(homework.id) }
                            )
                        }
                    }
                }
            }

            ExtendedFloatingActionButton(
                text = { Text("Add Homework") },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Homework") },
                onClick = { viewModel.showAddDialog() },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd),
                expanded = true
            )
        }
    }

    // Add Dialog
    if (uiState.showAddDialog) {
        AddHomeworkBottomSheet(
            onDismiss = { viewModel.hideAddDialog() },
            onAddHomework = { description, dueDate ->
                viewModel.addHomework(description, dueDate)
                viewModel.hideAddDialog()
            }
        )
    }

    // Error Snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar for error
            viewModel.clearError()
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Assignment,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No homework yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = "Tap the + button to add your first homework",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun HomeworkCard(
    homework: Homework,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Due date
                    FlowRow(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        itemVerticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isOverdue(homework.dueDate)) {
                                        Color.Red.copy(alpha = 0.1f)
                                    } else {
                                        MaterialTheme.colorScheme.outline.copy(0.2f)
                                    },
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Due: ${formatDate(homework.dueDate)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isOverdue(homework.dueDate)) Color.Red else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.secondary.copy(0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${homework.year.name} - ${homework.tp.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Description
                    if (homework.description.isNotEmpty()) {
                        Text(
                            text = homework.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Button(
                            onClick = onDelete
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun isOverdue(dueDate: Long): Boolean {
    return dueDate < System.currentTimeMillis()
}