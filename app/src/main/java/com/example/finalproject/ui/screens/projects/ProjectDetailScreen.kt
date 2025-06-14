package com.example.finalproject.ui.screens.projects

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Monitor
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.finalproject.R
import com.example.finalproject.Screen
import com.example.finalproject.data.PreferencesManager
import com.example.finalproject.data.model.User
import com.example.finalproject.ui.components.projects.AddMemberDialog
import com.example.finalproject.ui.components.projects.AddTaskDialog
import com.example.finalproject.ui.components.projects.EditProjectDialog
import com.example.finalproject.ui.components.projects.ProjectAnalyticsExporterDialog
import com.example.finalproject.ui.components.projects.WorkerDetailDialog
import com.example.finalproject.ui.components.projects.WorkersListProject
import com.example.finalproject.ui.theme.primaryLight
import com.example.finalproject.ui.theme.surfaceVariantLight
import com.example.finalproject.ui.viewmodels.projects.ProjectAnalyticsExporter
import com.example.finalproject.ui.viewmodels.projects.ProjectDetailViewModel
import com.example.finalproject.utils.updateAppLanguage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

fun formatDate(iso: String?): String? {
    return try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        iso?.let { dateFormat.format(isoFormat.parse(it)) }
    } catch (e: Exception) {
        iso
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projetoId: String,
    navController: NavController,
    currentUser: User? = null,
    onBackClick: () -> Unit = {},
    onAddTaskClick: () -> Unit = {},
    viewModel: ProjectDetailViewModel = viewModel()
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Carregar o usuário atual se não for fornecido
    LaunchedEffect(key1 = true) {
        viewModel.loadUser(currentUser)
        viewModel.loadProject(projetoId)
        viewModel.checkIfManager(projetoId)
        viewModel.loadMembrosProjetoCompleto(projetoId)

        val savedLanguage = PreferencesManager.getLanguage(context)
        print("Saved language: $savedLanguage")
        updateAppLanguage(context, savedLanguage)
    }

    LaunchedEffect(viewModel.navigateToTasksForProject) {
        viewModel.navigateToTasksForProject?.let { projectId ->
            println("Navigating to tasks for project: $projectId")
            navController.navigate(Screen.TaskManagement.createRoute(projectId))
            viewModel.onTasksNavigationHandled()
        }
    }

    val viewTasks = stringResource(id = R.string.view_tasks)
    val addTask = stringResource(id = R.string.add_task)
    val editProject = stringResource(id = R.string.edit_project)
    val deleteProject = stringResource(id = R.string.delete_project)
    val closeMenu = stringResource(id = R.string.close_menu)
    val openMenu = stringResource(id = R.string.open_menu)
    val confirmDeleteTitle = stringResource(id = R.string.confirm_delete_title)
    val deleteSuccess = stringResource(id = R.string.delete_success)
    val back = stringResource(id = R.string.back)
    val notFound = stringResource(id = R.string.not_found)
    val projectUpdatedSuccess = stringResource(id = R.string.project_updated_success)
    val projectUpdateError = stringResource(id = R.string.project_update_error)
    val memberSuccess = stringResource(id = R.string.member_added_success)
    val workerDataUpdatedSuccess = stringResource(id = R.string.worker_data_updated_success)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.project_details_title),
                        fontWeight = FontWeight.Medium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = back,
                            tint = primaryLight
                        )
                    }
                },
                windowInsets = WindowInsets(0)
            )
        },
        floatingActionButton = {
            // Mostrar FAB apenas para admin
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // FAB menu
                AnimatedVisibility(
                    visible = viewModel.showFabActions,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }) + expandVertically(),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }) + shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        if(viewModel.isAdmin  || viewModel.isManager) {
                            ActionButton(
                                icon = Icons.Default.Add,
                                label = addTask,
                                onClick = {
                                    viewModel.toggleFabActions()
                                    viewModel.showAddTaskDialog()
                                }
                            )
                        }

                        ActionButton(
                            icon = Icons.Default.List,
                            label = viewTasks,
                            onClick = {
                                viewModel.toggleFabActions()
                                viewModel.onViewTasksClick()
                            }
                        )

                        if( viewModel.isAdmin || viewModel.isManager) {
                            ActionButton(
                                icon = Icons.Default.PersonAdd,
                                label = stringResource(id = R.string.add_member),
                                onClick = {
                                    viewModel.toggleFabActions()
                                    viewModel.showAddMemberDialog()
                                }
                            )
                        }
                        if( viewModel.isAdmin || viewModel.isManager) {
                            ActionButton(
                                icon = Icons.Default.Edit,
                                label = editProject,
                                onClick = {
                                    viewModel.toggleFabActions()
                                    viewModel.showEditProjectDialog()
                                }
                            )
                        }
                       if( viewModel.isAdmin) {
                            ActionButton(
                                icon = Icons.Default.Delete,
                                label = deleteProject,
                                onClick = {
                                    viewModel.toggleFabActions()
                                    viewModel.showDeleteConfirmDialog()
                                }
                            )
                        }
                        if(viewModel.isAdmin){
                            ActionButton(
                                icon = Icons.Outlined.Analytics,
                                label = stringResource(id = R.string.export_project_analytics),
                                onClick = {
                                    viewModel.toggleFabActions()
                                    viewModel.showAnalyticsExporterDialog()
                                }
                            )
                        }
                    }
                }
                // Main FAB
                FloatingActionButton(
                    onClick = { viewModel.toggleFabActions() },
                ) {
                    Icon(
                        imageVector = if (viewModel.showFabActions) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (viewModel.showFabActions) closeMenu else openMenu
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = primaryLight)
            }
        } else if (viewModel.projeto != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Título do projeto
                Text(
                    text = viewModel.projeto!!.nome,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Status indicator
                StatusChip(status = viewModel.projeto!!.status)

                Spacer(modifier = Modifier.height(24.dp))

                // Seção de progresso
                ProjectInfoSection(
                    title = stringResource(id = R.string.progress),
                    content = {
                        LinearProgressIndicator(
                            progress = { viewModel.projeto!!.taxaConclusao.toFloat() / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Text(
                            text = "${viewModel.projeto!!.taxaConclusao}%",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Seção de descrição
                viewModel.projeto!!.descricao?.let {
                    ProjectInfoSection(
                        title = stringResource(id = R.string.description),
                        content = {
                            Text(
                                text = it,
                                fontSize = 16.sp,
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                    // Seção de datas
                    ProjectInfoSection(
                        title = stringResource(id = R.string.dates),
                        content = {
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                            val createdDate = viewModel.projeto!!.createdAt?.let {
                                stringResource(id = R.string.created_at) + "\n" + formatDate(it)
                            } ?: stringResource(id = R.string.unknown_creation_date)

                            val updatedDate = viewModel.projeto!!.updatedAt?.let {
                                stringResource(id = R.string.updated_at) + "\n" + formatDate(it)
                            } ?: stringResource(id = R.string.unknown_update_date)


                            Column {
                                Text(
                                    text = createdDate,
                                    fontSize = 16.sp,
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = updatedDate,
                                    fontSize = 16.sp,
                                )
                            }
                        }
                    )

                Spacer(modifier = Modifier.height(16.dp))

                ProjectInfoSection(
                    title = stringResource(id = R.string.workers),
                    content = {
                        // Usar diretamente a lista de membros com informações completas
                        WorkersListProject(
                            workers = viewModel.membrosProjetoCompleto,
                            emptyText = stringResource(id = R.string.no_workers),
                            onWorkerClick = { userId ->
                                // Encontrar o worker pelo ID e mostrar o diálogo de detalhes
                                val worker = viewModel.membrosProjetoCompleto.find { it.userId == userId }
                                worker?.let { viewModel.showWorkerDetailDialog(it) }
                            }
                        )
                    }
                )

                    // Espaço para o FAB
                    Spacer(modifier = Modifier.height(100.dp))
                }
            } else {
                // Mostrar mensagem se o projeto não for encontrado
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = notFound,
                        fontSize = 16.sp,
                    )
                }
            }
        }

    // Diálogo de confirmação para apagar projeto
    if (viewModel.showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirmDialog() },
            title = { Text(confirmDeleteTitle) },
            text = {
                Text(stringResource(id = R.string.confirm_delete_text, viewModel.projeto?.nome ?: ""))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.projeto?.id?.let { projId ->
                            scope.launch {
                                try {
                                    viewModel.deleteProject(projId)
                                    Toast.makeText(context, deleteSuccess, Toast.LENGTH_SHORT).show()
                                    onBackClick()
                                } catch (e: Exception) {
                                    Toast.makeText(context, context.getString(R.string.delete_error, e.message ?: ""), Toast.LENGTH_SHORT).show()
                                    e.printStackTrace()
                                }
                            }
                        }
                    },
                ) {
                    Text(stringResource(id = R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.hideDeleteConfirmDialog() }
                ) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }

    // Diálogo para adicionar tarefa
    if (viewModel.showAddTaskDialog) {
        AddTaskDialog(
            show = true,
            onDismiss = { viewModel.hideAddTaskDialog() },
            onAddTask = { nome, descricao, prioridade, status, dataInicio, dataFim ->
                // Lógica para adicionar tarefa agora delegada ao ViewModel
                scope.launch {
                    try {
                        viewModel.addTask(
                            projetoId = projetoId,
                            nome = nome,
                            descricao = descricao,
                            prioridade = prioridade,
                            status = status,
                            dataInicio = dataInicio,
                            dataFim = dataFim
                        )
                        Toast.makeText(
                            context,
                            context.getString(R.string.task_added_success),
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.task_add_error, e.message ?: ""),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }

// No ProjectDetailScreen.kt
    if (viewModel.showAddMemberDialog) {
        LaunchedEffect(Unit) {
            viewModel.loadAllUsers()
        }
        val usersNotInProject = viewModel.allUsers.filter { user ->
            viewModel.membrosProjetoCompleto.none { membro -> membro.userId == user.id }
        }
        AddMemberDialog(
            isAdmin = viewModel.isAdmin,
            users = usersNotInProject,
            onDismiss = { viewModel.hideAddMemberDialog() },
            onAdd = { userId, isManager ->
                scope.launch {
                    viewModel.addMemberToProject(userId, isManager)
                    Toast.makeText(context, memberSuccess, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    // Diálogo para editar projeto
    if (viewModel.showEditProjectDialog) {
        EditProjectDialog(
            show = true,
            projeto = viewModel.projeto,
            onDismiss = { viewModel.hideEditProjectDialog() },
            onSaveProject = { nome, descricao, status, taxaConclusao ->
                viewModel.projeto?.id?.let { projectId ->
                    scope.launch {
                        try {
                            viewModel.updateProject(
                                projetoId = projectId.toString(),
                                nome = nome,
                                descricao = descricao,
                                status = status,
                                taxaConclusao = taxaConclusao
                            )
                            Toast.makeText(context, projectUpdatedSuccess, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "$projectUpdateError: ${e.message}", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }
                    }
                }
            }
        )
    }

    // Diálogo para detalhes do trabalhador
    if (viewModel.showWorkerDetailDialog) {
        WorkerDetailDialog(
            show = true,
            worker = viewModel.selectedWorker,
            onDismiss = { viewModel.hideWorkerDetailDialog() },
            onEdit = { userId, isManager, isActive ->
                scope.launch {
                    viewModel.updateWorkerRole(userId, isManager, isActive)
                    Toast.makeText(context, workerDataUpdatedSuccess, Toast.LENGTH_SHORT).show()                }
            },
            isAdmin = viewModel.isAdmin || viewModel.isManager
        )
    }

    // Diálogo para exportar análises do projeto
    if (viewModel.showAnalyticsExporterDialog) {
        ProjectAnalyticsExporterDialog(
            show = true,
            projetoId = projetoId,
            onDismiss = { viewModel.hideAnalyticsExporterDialog() },
            onExport = { format ->
                scope.launch {
                    viewModel.exportProjectAnalytics(format, context)
                }
            }
        )
    }
}

// Componentes para a tela de detalhes do projeto
@Composable
private fun ProjectInfoSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        content()
    }
}

@Composable
private fun StatusChip(status: String) {
    val (backgroundColor, textColor, statusText) = when(status) {
        "ativo" -> Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, stringResource(id = R.string.active))
        "concluido" -> Triple(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer, stringResource(id = R.string.completed))
        "inativo" -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, stringResource(id = R.string.inactive))
        "cancelado" -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, stringResource(id = R.string.cancelled))
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, status.replaceFirstChar { it.uppercase() })
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = statusText,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .defaultMinSize(minWidth = 200.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = label,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
