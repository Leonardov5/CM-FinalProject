package com.example.finalproject.ui.screens.projects

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.data.model.Projeto
import com.example.finalproject.data.repository.ProjetoRepository
import com.example.finalproject.ui.theme.*
import com.example.finalproject.ui.viewmodels.projects.ProjectsViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.stringResource
import com.example.finalproject.R
import com.example.finalproject.data.PreferencesManager
import com.example.finalproject.utils.updateAppLanguage
import com.example.finalproject.ui.components.projects.AddTaskDialog

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onProjectClick: (String) -> Unit = {}, // Callback para navegação para detalhes do projeto
) {
    val viewModel: ProjectsViewModel = viewModel()
    var isLanguageLoaded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val noProjectsFound = stringResource(id = R.string.no_projects_found)
    val newProjectDialogTitle = stringResource(id = R.string.new_project_dialog_title)
    val projectNameLabel = stringResource(id = R.string.project_name_label)
    val projectDescriptionLabel = stringResource(id = R.string.project_description_label)
    val cancel = stringResource(id = R.string.cancel)
    val create = stringResource(id = R.string.create)
    val projectCreatedSuccess = stringResource(id = R.string.project_created_success)


    LaunchedEffect(Unit) {
        val savedLanguage = PreferencesManager.getLanguage(context)
        updateAppLanguage(context, savedLanguage)
        isLanguageLoaded = true
    }

    if(isLanguageLoaded){
        // Sempre que a tela for recomposicionada, recarrega os projetos
        LaunchedEffect(Unit) {
            viewModel.loadProjects()
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(0.7f),
                                shape = RoundedCornerShape(25.dp),
                                color = surfaceVariantLight
                            ) {
                                Text(
                                    text = stringResource(id = R.string.projects_title),
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    color = onSurfaceVariantLight
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = stringResource(id = R.string.menu),
                                tint = primaryLight
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onProfileClick) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = stringResource(id = R.string.profile),
                                tint = primaryLight
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = backgroundLight
                    ),
                    windowInsets = WindowInsets(0)
                )
            },
            floatingActionButton = {
                // Só mostrar o FAB se o usuário for admin
                if (viewModel.isAdmin) {
                    FloatingActionButton(
                        onClick = {
                            if (viewModel.projects.isNotEmpty()) {
                                // Se tiver projetos, mostra o diálogo para escolher
                                // entre adicionar projeto ou tarefa
                                viewModel.showAddProjectDialog()
                            } else {
                                // Se não tiver projetos, só pode adicionar projetos
                                viewModel.showAddProjectDialog()
                            }
                        },
                        containerColor = primaryLight,
                        contentColor = onPrimaryLight
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(id = R.string.add_project)
                        )
                    }
                }
            },
            containerColor = backgroundLight,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            // Projects List
            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryLight)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (viewModel.projects.isNotEmpty()) {
                        items(viewModel.projects) { projeto ->
                            ProjectCard(
                                projectName = projeto.nome,
                                lastUpdated = projeto.updatedAt.toString(),
                                projeto = projeto,
                                onClick = { selectedProjeto ->
                                    // Navegar para a tela de detalhes do projeto
                                    selectedProjeto.id?.let { projetoId ->
                                        onProjectClick(projetoId)
                                    }
                                }
                            )
                        }
                    } else {
                        // Exibir mensagem quando não houver projetos
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = noProjectsFound,
                                    fontSize = 16.sp,
                                    color = onSurfaceVariantLight
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // Diálogo para adicionar novo projeto
            if (viewModel.showAddDialog) {
                Dialog(onDismissRequest = { viewModel.hideAddProjectDialog() }) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = newProjectDialogTitle,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = onPrimaryContainerLight,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = viewModel.projectName,
                                onValueChange = { viewModel.onProjectNameChange(it) },
                                label = { Text(projectNameLabel) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryLight,
                                    unfocusedBorderColor = primaryLight.copy(alpha = 0.7f),
                                    focusedLabelColor = primaryLight,
                                    unfocusedLabelColor = primaryLight.copy(alpha = 0.7f),
                                    cursorColor = primaryLight
                                )
                            )

                            OutlinedTextField(
                                value = viewModel.projectDescription,
                                onValueChange = { viewModel.onProjectDescriptionChange(it) },
                                label = { Text(projectDescriptionLabel) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .padding(bottom = 16.dp),
                                singleLine = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryLight,
                                    unfocusedBorderColor = primaryLight.copy(alpha = 0.7f),
                                    focusedLabelColor = primaryLight,
                                    unfocusedLabelColor = primaryLight.copy(alpha = 0.7f),
                                    cursorColor = primaryLight
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { viewModel.hideAddProjectDialog() },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = primaryLight
                                    )
                                ) {
                                    Text(cancel)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        viewModel.createProject(
                                            onSuccess = {
                                                Toast.makeText(context, projectCreatedSuccess, Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { errorMessage ->
                                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryLight,
                                        contentColor = onPrimaryLight
                                    )
                                ) {
                                    Text(create)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectCard(
    projectName: String,
    lastUpdated: String = "Last updated: June 1, 2025",
    projeto: Projeto? = null,
    onClick: (Projeto) -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (projeto != null) {
                    onClick(projeto)
                }
            },
        shape = RoundedCornerShape(12.dp),
        color = secondaryContainerLight,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = null,
                tint = onSecondaryContainerLight,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = projectName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = onSecondaryContainerLight
                )

                Text(
                    text = lastUpdated,
                    fontSize = 12.sp,
                    color = onSecondaryContainerLight.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = stringResource(id = R.string.view_details),
                    modifier = Modifier.rotate(90f),
                    tint = onSecondaryContainerLight
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun ProjectsScreenPreview() {
    MaterialTheme {
        ProjectsScreen()
    }
}