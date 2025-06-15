package com.example.finalproject.ui.viewmodels.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.model.Observacao
import com.example.finalproject.data.model.Utilizador
import com.example.finalproject.data.repository.ObservacaoRepository
import com.example.finalproject.data.repository.UtilizadorRepository
import com.example.finalproject.data.service.UserService
import kotlinx.coroutines.launch
import java.io.File

class ObservacoesViewModel : ViewModel() {

    var observacoes by mutableStateOf<List<Observacao>>(emptyList())
        private set

    var utilizadores by mutableStateOf<Map<String, Utilizador>>(emptyMap())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var tarefaId by mutableStateOf<String?>(null)
        private set

    var showAddObservacaoDialog by mutableStateOf(false)
        private set

    var novaObservacaoTexto by mutableStateOf("")
    var imagensTemporarias by mutableStateOf<List<File>>(emptyList())

    private val observacaoRepository = ObservacaoRepository()
    private val utilizadorRepository = UtilizadorRepository()

    var user by mutableStateOf<Utilizador?>(null)
        private set

    var isAdminUser by mutableStateOf(false)
        private set

    var observacaoEmEdicao by mutableStateOf<Observacao?>(null)
    var showEditObservacaoDialog by mutableStateOf(false)
    var textoObservacaoEditada by mutableStateOf("")

    fun carregarObservacoes(tarefaId: String) {
        this.tarefaId = tarefaId
        isLoading = true
        error = null

        viewModelScope.launch {
            try {
                observacoes = observacaoRepository.listarObservacoesPorTarefa(tarefaId)

                val utilizadoresIds = observacoes.mapNotNull { it.createdBy }.distinct()
                if (utilizadoresIds.isNotEmpty()) {
                    val listaUtilizadores = utilizadorRepository.listarTodosUtilizadores()
                    utilizadores = listaUtilizadores.filter { it.id in utilizadoresIds }.associateBy { it.id ?: "" }
                }
            } catch (e: Exception) {
                error = "Erro ao carregar observações: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun toggleAddObservacaoDialog() {
        showAddObservacaoDialog = !showAddObservacaoDialog
        if (!showAddObservacaoDialog) {
            novaObservacaoTexto = ""
            imagensTemporarias = emptyList()
        }
    }

    fun adicionarImagem(arquivo: File) {
        imagensTemporarias = imagensTemporarias + arquivo
    }

    fun removerImagem(index: Int) {
        imagensTemporarias = imagensTemporarias.filterIndexed { i, _ -> i != index }
    }

    fun salvarObservacao(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (novaObservacaoTexto.isBlank()) {
            onError("A descrição da observação não pode estar vazia")
            return
        }

        val tarefaIdAtual = tarefaId
        if (tarefaIdAtual == null) {
            onError("ID da tarefa não encontrado")
            return
        }

        isLoading = true

        viewModelScope.launch {
            try {
                val imagensBytes = imagensTemporarias.map { arquivo ->
                    arquivo.readBytes()
                }

                val novaObservacao = observacaoRepository.criarObservacao(
                    tarefaId = tarefaIdAtual,
                    observacao = novaObservacaoTexto,
                    imagens = imagensBytes
                )

                if (novaObservacao != null) {
                    carregarObservacoes(tarefaIdAtual)

                    novaObservacaoTexto = ""
                    imagensTemporarias = emptyList()

                    onSuccess()
                } else {
                    onError("Erro ao criar observação")
                }
            } catch (e: Exception) {
                onError("Erro ao salvar observação: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun eliminarObservacao(observacaoId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        isLoading = true

        viewModelScope.launch {
            try {
                val sucesso = observacaoRepository.eliminarObservacao(observacaoId)

                if (sucesso) {
                    tarefaId?.let { carregarObservacoes(it) }
                    onSuccess()
                } else {
                    onError("Erro ao excluir observação")
                }
            } catch (e: Exception) {
                onError("Erro ao excluir observação: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun obterUtilizador(userId: String?): Utilizador? {
        return if (userId != null) utilizadores[userId] else null
    }

    fun loadUser(currentUser: Utilizador? = null) {
        viewModelScope.launch {
            try {
                user = currentUser ?: UserService.getCurrentUserData()
                isAdminUser = user?.admin == true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isAdmin(): Boolean {
        return isAdminUser
    }

    fun iniciarEdicaoObservacao(observacao: Observacao) {
        observacaoEmEdicao = observacao
        textoObservacaoEditada = observacao.observacao
        imagensTemporarias = emptyList()
        showEditObservacaoDialog = true
    }

    fun cancelarEdicao() {
        observacaoEmEdicao = null
        textoObservacaoEditada = ""
        imagensTemporarias = emptyList()
        showEditObservacaoDialog = false
    }

    fun salvarEdicaoObservacao(onSuccess: () -> Unit, onError: (String) -> Unit, imagensAtuais: List<String> = emptyList()) {
        val observacao = observacaoEmEdicao
        if (observacao == null) {
            onError("Nenhuma observação selecionada para edição")
            return
        }

        if (textoObservacaoEditada.isBlank()) {
            onError("A descrição da observação não pode estar vazia")
            return
        }

        isLoading = true

        viewModelScope.launch {
            try {
                val imagensBytes = imagensTemporarias.map { arquivo ->
                    arquivo.readBytes()
                }

                val observacaoAtualizada = observacaoRepository.atualizarObservacao(
                    observacaoId = observacao.id ?: "",
                    textoObservacao = textoObservacaoEditada,
                    imagensAtuais = imagensAtuais,
                    novasImagens = imagensBytes
                )

                if (observacaoAtualizada != null) {
                    tarefaId?.let { carregarObservacoes(it) }

                    cancelarEdicao()

                    onSuccess()
                } else {
                    onError("Erro ao atualizar observação")
                }
            } catch (e: Exception) {
                onError("Erro ao salvar alterações: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun podeEditarObservacao(observacao: Observacao): Boolean {
        return user?.id == observacao.createdBy
    }
}
