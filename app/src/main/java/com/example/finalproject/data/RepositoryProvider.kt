package com.example.finalproject.data

import com.example.finalproject.data.repository.UserRepository

/**
 * Classe que facilita o acesso a todos os repositórios do aplicativo
 */
object RepositoryProvider {
    /**
     * Repositório de usuários
     */
    val userRepository by lazy { UserRepository() }

    // Outros repositórios podem ser adicionados aqui conforme necessário
}
