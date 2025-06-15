package com.example.finalproject.ui.viewmodels.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.model.Observacao
import com.example.finalproject.data.model.User
import com.example.finalproject.data.repository.ObservacaoRepository
import com.example.finalproject.data.repository.UtilizadorRepository
import com.example.finalproject.data.service.UserService
import kotlinx.coroutines.launch
import java.io.File

class ObservacoesViewModel : ViewModel() {

    var observacoes by mutableStateOf<List<Observacao>>(emptyList())
        private set

    var usuarios by mutableStateOf<Map<String, User>>(emptyMap())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var tarefaId by mutableStateOf<String?>(null)
        private set

    var showAddObservacaoDialog by mutableStateOf(false)
        private set

    // Nova observação
    var novaObservacaoTexto by mutableStateOf("")
    var imagensTemporarias by mutableStateOf<List<File>>(emptyList())

    private val observacaoRepository = ObservacaoRepository()
    private val utilizadorRepository = UtilizadorRepository()

    // Novo: usuário atual e status de administrador
    var user by mutableStateOf<User?>(null)
        private set

    var isAdminUser by mutableStateOf(false)
        private set

    // Estados para edição
    var editandoObservacao by mutableStateOf<Observacao?>(null)
    var showEditObservacaoDialog by mutableStateOf(false)
    var textoObservacaoEditada by mutableStateOf("")

    fun carregarObservacoes(tarefaId: String) {
        this.tarefaId = tarefaId
        isLoading = true
        error = null

        viewModelScope.launch {
            try {
                // Carregar as observações da tarefa
                observacoes = observacaoRepository.listarObservacoesPorTarefa(tarefaId)

                // Carregar dados dos usuários que criaram as observações
                val usuariosIds = observacoes.mapNotNull { it.createdBy }.distinct()
                if (usuariosIds.isNotEmpty()) {
                    val listaUsuarios = utilizadorRepository.listarTodosUtilizadores()
                    usuarios = listaUsuarios.filter { it.id in usuariosIds }.associateBy { it.id ?: "" }
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
            // Limpar campos ao fechar o diálogo
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
                // Converter arquivos de imagem para ByteArray
                val imagensBytes = imagensTemporarias.map { arquivo ->
                    arquivo.readBytes()
                }

                // Criar observação
                val novaObservacao = observacaoRepository.criarObservacao(
                    tarefaId = tarefaIdAtual,
                    observacao = novaObservacaoTexto,
                    imagens = imagensBytes
                )

                if (novaObservacao != null) {
                    // Recarregar observações
                    carregarObservacoes(tarefaIdAtual)

                    // Limpar campos
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

    fun excluirObservacao(observacaoId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        isLoading = true

        viewModelScope.launch {
            try {
                val sucesso = observacaoRepository.eliminarObservacao(observacaoId)

                if (sucesso) {
                    // Recarregar observações se a exclusão for bem-sucedida
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

    fun obterUsuario(userId: String?): User? {
        return if (userId != null) usuarios[userId] else null
    }

    fun loadUser(currentUser: User? = null) {
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
        editandoObservacao = observacao
        textoObservacaoEditada = observacao.observacao
        imagensTemporarias = emptyList() // Limpa as imagens temporárias para novas adições
        showEditObservacaoDialog = true
    }

    fun cancelarEdicao() {
        editandoObservacao = null
        textoObservacaoEditada = ""
        imagensTemporarias = emptyList()
        showEditObservacaoDialog = false
    }

    fun salvarEdicaoObservacao(onSuccess: () -> Unit, onError: (String) -> Unit, imagensAtuais: List<String> = emptyList()) {
        val observacao = editandoObservacao
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
                // Converter arquivos de imagem para ByteArray
                val imagensBytes = imagensTemporarias.map { arquivo ->
                    arquivo.readBytes()
                }

                // Atualizar observação
                val observacaoAtualizada = observacaoRepository.atualizarObservacao(
                    observacaoId = observacao.id ?: "",
                    textoObservacao = textoObservacaoEditada,
                    imagensAtuais = imagensAtuais,
                    novasImagens = imagensBytes
                )

                if (observacaoAtualizada != null) {
                    // Recarregar observações
                    tarefaId?.let { carregarObservacoes(it) }

                    // Limpar campos
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

    fun excluirImagemDeObservacao(observacaoId: String, imagemUrl: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        isLoading = true

        viewModelScope.launch {
            try {
                val sucesso = observacaoRepository.eliminarImagem(observacaoId, imagemUrl)

                if (sucesso) {
                    // Recarregar observações
                    tarefaId?.let { carregarObservacoes(it) }
                    onSuccess()
                } else {
                    onError("Erro ao excluir imagem")
                }
            } catch (e: Exception) {
                onError("Erro ao excluir imagem: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun podeEditarObservacao(observacao: Observacao): Boolean {
        // Usuário pode editar se for o criador ou administrador
        return user?.id == observacao.createdBy
    }
}
