package com.example.finalproject.ui.viewmodels.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.model.Tarefa
import com.example.finalproject.data.model.Trabalho
import com.example.finalproject.data.model.User
import com.example.finalproject.data.repository.ProjetoRepository
import com.example.finalproject.data.repository.TarefaRepository
import com.example.finalproject.data.repository.TrabalhoRepository
import com.example.finalproject.data.repository.UtilizadorRepository
import com.example.finalproject.data.service.UserService
import kotlinx.coroutines.launch

class TrabalhosViewModel : ViewModel() {

    // Dados dos trabalhos
    var trabalhos by mutableStateOf<List<Trabalho>>(emptyList())
        private set

    // Usuários associados aos trabalhos
    var usuarios by mutableStateOf<Map<String, User>>(emptyMap())
        private set

    // Informações da tarefa relacionada
    var tarefa by mutableStateOf<Tarefa?>(null)
        private set

    // Estados da interface
    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var tarefaId by mutableStateOf<String?>(null)
        private set

    // Dados do usuário atual e permissões
    var user by mutableStateOf<User?>(null)
        private set

    var isAdminUser by mutableStateOf(false)
        private set

    var isManager by mutableStateOf(false)
        private set

    // Repositórios
    private val trabalhoRepository = TrabalhoRepository()
    private val tarefaRepository = TarefaRepository()
    private val utilizadorRepository = UtilizadorRepository()
    private val projetoRepository = ProjetoRepository()

    // Função para carregar os trabalhos de uma tarefa
    fun carregarTrabalhos(tarefaId: String) {
        this.tarefaId = tarefaId
        isLoading = true
        error = null

        viewModelScope.launch {
            try {
                // Carregar a tarefa associada
                tarefa = tarefaRepository.obterTarefaPorId(tarefaId)

                // Carregar os trabalhos da tarefa
                trabalhos = trabalhoRepository.listarTrabalhosPorTarefa(tarefaId)

                // Carregar dados dos usuários que criaram os trabalhos
                val usuariosIds = trabalhos.mapNotNull { it.createdBy }.distinct()
                if (usuariosIds.isNotEmpty()) {
                    val listaUsuarios = utilizadorRepository.listarTodosUtilizadores()
                    usuarios = listaUsuarios.filter { it.id in usuariosIds }.associateBy { it.id ?: "" }
                }
            } catch (e: Exception) {
                error = "Erro ao carregar trabalhos: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Função para obter informações do usuário
    fun loadUser(currentUser: User? = null) {
        viewModelScope.launch {
            try {
                user = currentUser ?: UserService.getCurrentUserData()
                isAdminUser = user?.admin == true

                // Verificar se é gerente do projeto
                tarefa?.projetoId?.let { projetoId ->
                    isManager = verificarGerente(projetoId, user?.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Função para verificar se o usuário é gerente do projeto
    private suspend fun verificarGerente(projetoId: String, userId: String?): Boolean {
        if (userId == null) return false

        return try {
            val membros = projetoRepository.listarMembrosProjetoCompleto(projetoId)
            membros.any { it.userId == userId && it.isManager }
        } catch (e: Exception) {
            false
        }
    }

    // Função para obter um usuário pelo ID
    fun obterUsuario(userId: String?): User? {
        return if (userId != null) usuarios[userId] else null
    }

    // Função para excluir um trabalho (apenas para admin ou gerente)
    fun excluirTrabalho(trabalhoId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!isAdminUser && !isManager) {
            onError("Você não tem permissão para excluir trabalhos")
            return
        }

        isLoading = true

        viewModelScope.launch {
            try {
                val sucesso = trabalhoRepository.eliminarTrabalho(trabalhoId)

                if (sucesso) {
                    // Recarregar trabalhos se a exclusão for bem-sucedida
                    tarefaId?.let { carregarTrabalhos(it) }
                    onSuccess()
                } else {
                    onError("Erro ao excluir trabalho")
                }
            } catch (e: Exception) {
                onError("Erro ao excluir trabalho: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
}
