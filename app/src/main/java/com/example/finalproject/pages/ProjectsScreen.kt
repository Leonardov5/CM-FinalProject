package com.example.finalproject.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.finalproject.components.*
import com.example.finalproject.data.model.Projeto
import com.example.finalproject.data.model.User
import com.example.finalproject.data.repository.ProjetoRepository
import com.example.finalproject.data.service.UserService
import com.example.finalproject.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    projetoRepository: ProjetoRepository = ProjetoRepository(),
    currentUser: User? = null, // Parâmetro para receber o usuário atual
    onProjectClick: (String) -> Unit = {} // Callback para navegação para detalhes do projeto
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var projectName by remember { mutableStateOf("") }
    var projectDescription by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }
    var user by remember { mutableStateOf(currentUser) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var projects by remember { mutableStateOf<List<Projeto>>(emptyList()) }

    // Carregar o usuário atual se não for fornecido
    LaunchedEffect(key1 = true) {
        if (user == null) {
            // Buscar o usuário atual do UserService
            user = UserService.getCurrentUserData()
            println("DEBUG - Usuário carregado: $user")
        }

        // Verificar se o usuário é admin
        isAdmin = user?.admin == true
        println("DEBUG - Usuário é admin: $isAdmin")
    }

    // Carregar projetos quando a tela for montada
    LaunchedEffect(key1 = true) {
        println("DEBUG - Carregando projetos com repositório: $projetoRepository")
        isLoading = true
        projects = projetoRepository.listarProjetos()
        println("DEBUG - Projetos carregados: ${projects.size}")
        isLoading = false
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            // Só mostrar o FAB se o usuário for admin
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = primaryLight,
                    contentColor = onPrimaryLight
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adicionar Projeto"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundLight)
                .padding(paddingValues)
        ) {
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

            // Projects List
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryLight)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (projects.isNotEmpty()) {
                        items(projects) { projeto ->
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
        }

        // Diálogo para adicionar novo projeto
        if (showAddDialog) {
            Dialog(onDismissRequest = { showAddDialog = false }) {
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
                            value = projectName,
                            onValueChange = { projectName = it },
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
                            value = projectDescription,
                            onValueChange = { projectDescription = it },
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
                                onClick = { showAddDialog = false },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = primaryLight
                                )
                            ) {
                                Text("Cancelar")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (projectName.isNotBlank()) {
                                        scope.launch {
                                            println("DEBUG - Repositório de projetos: $projetoRepository")
                                            isLoading = true

                                            // Fechar o diálogo imediatamente
                                            showAddDialog = false

                                            val novoProjeto = projetoRepository.criarProjeto(
                                                nome = projectName,
                                                descricao = projectDescription.takeIf { it.isNotBlank() }
                                            )
                                            println("DEBUG - Projeto criado: $novoProjeto")

                                            if (novoProjeto != null) {
                                                // Atualiza a lista de projetos
                                                projects = projetoRepository.listarProjetos()
                                                // Exibe um Toast em vez do Snackbar
                                                Toast.makeText(context, "Projeto criado com sucesso!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                // Exibe um Toast de erro
                                                Toast.makeText(context, "Erro ao criar projeto", Toast.LENGTH_SHORT).show()
                                            }
                                            isLoading = false
                                            projectName = ""
                                            projectDescription = ""
                                        }
                                    } else {
                                        // Exibe um Toast para campo obrigatório
                                        Toast.makeText(context, "Nome do projeto é obrigatório", Toast.LENGTH_SHORT).show()
                                    }
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
