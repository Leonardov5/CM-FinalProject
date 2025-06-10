package com.example.finalproject.ui.screens.projects

import android.app.Activity
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
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.stringResource
import com.example.finalproject.R
import com.example.finalproject.data.PreferencesManager
import com.example.finalproject.data.model.Projeto
import com.example.finalproject.data.model.User
import com.example.finalproject.data.repository.ProjetoRepository
import com.example.finalproject.data.service.UserService
import com.example.finalproject.ui.theme.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projetoId: String,
    projetoRepository: ProjetoRepository = ProjetoRepository(),
    currentUser: User? = null,
    onBackClick: () -> Unit = {},
    onAddTaskClick: () -> Unit = {}

) {
    println("ProjectsDetailScreen: Composable chamado")
    var projeto by remember { mutableStateOf<Projeto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isAdmin by remember { mutableStateOf(false) }
    var user by remember { mutableStateOf(currentUser) }
    var showFabActions by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()



    // Carregar o usuário atual se não for fornecido
    LaunchedEffect(key1 = true) {
        if (user == null) {
            // Buscar o usuário atual do UserService
            user = UserService.getCurrentUserData()
        }

        // Verificar se o usuário é admin
        isAdmin = user?.admin == true
    }

    // Carregar os detalhes do projeto
    LaunchedEffect(key1 = projetoId) {
        isLoading = true
        val savedLanguage = PreferencesManager.getLanguage(context)
        print("Saved language: $savedLanguage")
        updateAppLanguage(context, savedLanguage)
        try {
            val uuid = UUID.fromString(projetoId)
            projeto = projetoRepository.obterProjeto(uuid)
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao carregar projeto", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
        isLoading = false
    }

    if(!isLoading){
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

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.project_details_title),
                            fontWeight = FontWeight.Medium,
                            color = onBackgroundLight
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = backgroundLight,
                        titleContentColor = onBackgroundLight
                    ),
                    windowInsets = WindowInsets(0)
                )
            },
            floatingActionButton = {
                // Mostrar FAB apenas para admin
                if (isAdmin) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        // FAB menu
                        AnimatedVisibility(
                            visible = showFabActions,
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
                                        showFabActions = false
                                        onAddTaskClick()
                                    }
                                )

                                ActionButton(
                                    icon = Icons.Default.Edit,
                                    label = editProject,
                                    onClick = {
                                        showFabActions = false
                                        // Implementar futuramente
                                        Toast.makeText(context, "Funcionalidade ainda não implementada", Toast.LENGTH_SHORT).show()
                                    }
                                )

                                ActionButton(
                                    icon = Icons.Default.Delete,
                                    label = deleteProject,
                                    onClick = {
                                        showFabActions = false
                                        showDeleteConfirmDialog = true
                                    }
                                )

                                projeto?.let { p ->
                                    val nextStatus = when(p.status) {
                                        "ativo" -> "concluido"
                                        "concluido" -> "ativo"
                                        else -> "ativo"
                                    }

                                    val statusLabel = when(nextStatus) {
                                        "concluido" -> markAsActive
                                        "ativo" -> markAsCompleted
                                        else -> "Alterar Status"
                                    }

                                    val statusIcon = when(nextStatus) {
                                        "concluido" -> Icons.Default.Done
                                        else -> Icons.Default.Refresh
                                    }

                                    ActionButton(
                                        icon = statusIcon,
                                        label = statusLabel,
                                        onClick = {
                                            showFabActions = false
                                            scope.launch {
                                                try {
                                                    val result = projetoRepository.alterarStatusProjeto(
                                                        UUID.fromString(p.id),
                                                        nextStatus
                                                    )
                                                    if (result) {
                                                        // Recarregar o projeto
                                                        projeto = projetoRepository.obterProjeto(UUID.fromString(p.id))
                                                        Toast.makeText(context, statusUpdatedSuccess, Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, statusUpdateFailed, Toast.LENGTH_SHORT).show()
                                                    }
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, statusUpdateError, Toast.LENGTH_SHORT).show()
                                                    e.printStackTrace()
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // Main FAB
                        FloatingActionButton(
                            onClick = { showFabActions = !showFabActions },
                            containerColor = primaryLight,
                            contentColor = onPrimaryLight
                        ) {
                            Icon(
                                imageVector = if (showFabActions) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = if (showFabActions) closeMenu else openMenu
                            )
                        }
                    }
                }
            },
            containerColor = backgroundLight,
            contentWindowInsets = WindowInsets(0)
        ) { paddingValues ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryLight)
                }
            } else if (projeto != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Título do projeto
                    Text(
                        text = projeto!!.nome,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = onBackgroundLight,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    // Status indicator
                    StatusChip(status = projeto!!.status)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Seção de progresso
                    ProjectInfoSection(
                        title = stringResource(id = R.string.progress),
                        content = {
                            LinearProgressIndicator(
                                progress = { projeto!!.taxaConclusao.toFloat() / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = primaryLight,
                                trackColor = surfaceVariantLight
                            )

                            Text(
                                text = "${projeto!!.taxaConclusao}%",
                                fontSize = 14.sp,
                                color = onBackgroundLight,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Seção de descrição
                    projeto!!.descricao?.let {
                        ProjectInfoSection(
                            title = stringResource(id = R.string.description),
                            content = {
                                Text(
                                    text = it,
                                    fontSize = 16.sp,
                                    color = onBackgroundLight
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

                            val createdDate = projeto!!.createdAt?.let {
                                stringResource(id = R.string.created_at, formatDate(it) ?: it)
                            } ?: stringResource(id = R.string.unknown_creation_date)

                            val updatedDate = projeto!!.updatedAt?.let {
                                stringResource(id = R.string.updated_at, formatDate(it) ?: it)
                            } ?: stringResource(id = R.string.unknown_update_date)

                            Column {
                                Text(
                                    text = createdDate,
                                    fontSize = 16.sp,
                                    color = onBackgroundLight
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = updatedDate,
                                    fontSize = 16.sp,
                                    color = onBackgroundLight
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
                                    color = onBackgroundLight
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
                        color = onSurfaceVariantLight
                    )
                }
            }
        }

        // Diálogo de confirmação para apagar projeto
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text(confirmDeleteTitle) },
                text = {
                    Text(stringResource(id = R.string.confirm_delete_text, projeto?.nome ?: ""))
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmDialog = false
                            projeto?.id?.let { projetoId ->
                                scope.launch {
                                    try {
                                        val result = projetoRepository.apagarProjeto(UUID.fromString(projetoId))
                                        if (result) {
                                            Toast.makeText(context, deleteSuccess, Toast.LENGTH_SHORT).show()
                                            onBackClick()
                                        } else {
                                            Toast.makeText(context, deleteFailed, Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, context.getString(R.string.delete_error, e.message ?: ""), Toast.LENGTH_SHORT).show()
                                        e.printStackTrace()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = errorLight,
                            contentColor = onErrorLight
                        )
                    ) {
                        Text("Apagar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirmDialog = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
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
            color = primaryLight
        )

        Spacer(modifier = Modifier.height(8.dp))

        content()
    }
}

@Composable
private fun StatusChip(status: String) {
    val (backgroundColor, textColor, statusText) = when(status) {
        "ativo" -> Triple(secondaryLight, onSecondaryLight, stringResource(id = R.string.active))
        "concluido" -> Triple(primaryLight, onPrimaryLight, stringResource(id = R.string.completed))
        "inativo" -> Triple(surfaceVariantLight, onSurfaceVariantLight, stringResource(id = R.string.inactive))
        "cancelado" -> Triple(errorLight, onErrorLight, stringResource(id = R.string.cancelled))
        else -> Triple(surfaceVariantLight, onSurfaceVariantLight, status.replaceFirstChar { it.uppercase() })
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