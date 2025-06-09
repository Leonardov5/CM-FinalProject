package com.example.finalproject.ui.screens.projects

import android.widget.Toast
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onProjectClick: (String) -> Unit = {}, // Callback para navegação para detalhes do projeto
) {
    val viewModel: ProjectsViewModel = viewModel()

    val context = LocalContext.current

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
                                text = "My Projects",
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
                            contentDescription = "Menu",
                            tint = primaryLight
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = primaryLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundLight
                )
            )
        },
        floatingActionButton = {
            // Só mostrar o FAB se o usuário for admin
            if (viewModel.isAdmin) {
                FloatingActionButton(
                    onClick = { viewModel.showAddProjectDialog() },
                    containerColor = primaryLight,
                    contentColor = onPrimaryLight
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adicionar Projeto"
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
                                text = "Nenhum projeto encontrado",
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
                            text = "Novo Projeto",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = onPrimaryContainerLight,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = viewModel.projectName,
                            onValueChange = { viewModel.onProjectNameChange(it) },
                            label = { Text("Nome do projeto") },
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
                            label = { Text("Descrição") },
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
                                Text("Cancelar")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    viewModel.createProject(
                                        onSuccess = {
                                            Toast.makeText(context, "Projeto criado com sucesso!", Toast.LENGTH_SHORT).show()
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
                                Text("Criar")
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
                    contentDescription = "View details",
                    modifier = Modifier.rotate(90f),
                    tint = onSecondaryContainerLight
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectsScreenPreview() {
    MaterialTheme {
        ProjectsScreen()
    }
}