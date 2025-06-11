package com.example.finalproject.ui.screens.projects

import android.app.Activity
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.stringResource
import com.example.finalproject.R
import com.example.finalproject.data.PreferencesManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.data.model.Projeto
import com.example.finalproject.data.model.User
import com.example.finalproject.data.repository.ProjetoRepository
import com.example.finalproject.data.repository.TarefaRepository
import com.example.finalproject.data.service.UserService
import com.example.finalproject.ui.components.projects.AddTaskDialog
import com.example.finalproject.ui.components.projects.EditProjectDialog
import com.example.finalproject.ui.theme.*
import com.example.finalproject.ui.viewmodels.projects.ProjectDetailViewModel
import com.example.finalproject.utils.updateAppLanguage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
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
    projetoRepository: ProjetoRepository = ProjetoRepository(),
    tarefaRepository: TarefaRepository = TarefaRepository(),
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

        val savedLanguage = PreferencesManager.getLanguage(context)
        print("Saved language: $savedLanguage")
        updateAppLanguage(context, savedLanguage)
    }

    val addTask = stringResource(id = R.string.add_task)
    val editProject = stringResource(id = R.string.edit_project)
    val deleteProject = stringResource(id = R.string.delete_project)
    val markAsCompleted = stringResource(id = R.string.mark_as_completed)
    val markAsActive = stringResource(id = R.string.mark_as_active)
    val statusUpdatedSuccess = stringResource(id = R.string.status_updated_success)
    val statusUpdateFailed = stringResource(id = R.string.status_update_failed)
    val statusUpdateError = stringResource(id = R.string.status_update_error)
    val closeMenu = stringResource(id = R.string.close_menu)
    val openMenu = stringResource(id = R.string.open_menu)
    val confirmDeleteTitle = stringResource(id = R.string.confirm_delete_title)
    val deleteSuccess = stringResource(id = R.string.delete_success)
    val deleteFailed = stringResource(id = R.string.delete_failed)
    val back = stringResource(id = R.string.back)
    val notFound = stringResource(id = R.string.not_found)
    val projectUpdatedSuccess = stringResource(id = R.string.project_updated_success)
    val projectUpdateError = stringResource(id = R.string.project_update_error)

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
            if (viewModel.isAdmin) {
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
                            ActionButton(
                                icon = Icons.Default.Add,
                                label = addTask,
                                onClick = {
                                    viewModel.toggleFabActions()
                                    viewModel.showAddTaskDialog()
                                }
                            )

                            ActionButton(
                                icon = Icons.Default.Edit,
                                label = editProject,
                                onClick = {
                                    viewModel.toggleFabActions()
                                    viewModel.showEditProjectDialog()
                                }
                            )


                            ActionButton(
                                icon = Icons.Default.Delete,
                                label = deleteProject,
                                onClick = {
                                    viewModel.toggleFabActions()
                                    viewModel.showDeleteConfirmDialog()
                                }
                            )
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
                                stringResource(id = R.string.created_at, formatDate(it) ?: it)
                            } ?: stringResource(id = R.string.unknown_creation_date)

                            val updatedDate = viewModel.projeto!!.updatedAt?.let {
                                stringResource(id = R.string.updated_at, formatDate(it) ?: it)
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

                    // Seção de trabalhadores (Placeholder)
                    ProjectInfoSection(
                        title = stringResource(id = R.string.workers),
                        content = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Placeholder para futura implementação
                                Text(
                                    text = stringResource(id = R.string.no_workers),
                                    fontSize = 16.sp,
                                )
                            }
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
                    Text("Apagar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.hideDeleteConfirmDialog() }
                ) {
                    Text("Cancelar")
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
                        Toast.makeText(context, "Tarefa adicionada com sucesso", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Erro ao adicionar tarefa: ${e.message}", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
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
        color = surfaceVariantLight,
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
                tint = primaryLight,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = label,
                color = onSurfaceVariantLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
