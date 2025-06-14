package com.example.finalproject.data.repository

import com.example.finalproject.data.model.Projeto
import com.example.finalproject.data.model.UserProject
import com.example.finalproject.data.service.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ProjetoRepository {
    private val supabase = SupabaseProvider.client
    private val PROJETO_TABLE = "projeto"

    /**
     * Cria um novo projeto
     * @param nome Nome do projeto
     * @param descricao Descrição do projeto
     * @param status Status inicial do projeto
     * @param taxaConclusao Taxa de conclusão inicial do projeto
     * @return O projeto criado ou null em caso de falha
     */
    suspend fun criarProjeto(
        nome: String,
        descricao: String?,
        status: String = "ativo",
        taxaConclusao: Float = 0f
    ): Projeto? {
        return try {
            withContext(Dispatchers.IO) {
                val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())

                val novoProjeto = buildJsonObject {
                    put("nome", nome)
                    if (descricao != null) put("descricao", descricao)
                    put("status", status)
                    put("taxa_conclusao", taxaConclusao)
                    put("created_at", now)
                    put("updated_at", now)
                }

                println("DEBUG - Criando projeto: $novoProjeto")

                try {
                    // Inserir o projeto e retornar o resultado diretamente
                    val resultado = supabase.from(PROJETO_TABLE)
                        .insert(novoProjeto) {
                            select()
                        }
                        .decodeSingle<Projeto>()

                    println("DEBUG - Projeto criado com sucesso: $resultado")
                    resultado
                } catch (e: Exception) {
                    println("DEBUG - Erro ao criar projeto: ${e.message}")
                    e.printStackTrace()
                    null
                }
            }
        } catch (e: Exception) {
            println("DEBUG - Erro geral ao criar projeto: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Lista todos os projetos
     * @return Lista de projetos ou lista vazia em caso de falha
     */
    suspend fun listarProjetos(): List<Projeto> {
        return try {
            withContext(Dispatchers.IO) {
                supabase.from(PROJETO_TABLE)
                    .select(columns = Columns.ALL)
                    .decodeList<Projeto>()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtém um projeto pelo UUID
     * @param uuid UUID do projeto
     * @return O projeto ou null caso não encontre
     */
    suspend fun obterProjeto(uuid: UUID): Projeto? {
        return try {
            withContext(Dispatchers.IO) {
                supabase.from(PROJETO_TABLE)
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("projeto_uuid", uuid.toString())
                        }
                        limit(1)
                    }
                    .decodeSingle<Projeto>()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Atualiza um projeto existente
     * @param uuid UUID do projeto
     * @param nome Novo nome do projeto
     * @param descricao Nova descrição do projeto
     * @param status Novo status do projeto
     * @param taxaConclusao Nova taxa de conclusão do projeto
     * @return true se a atualização foi bem-sucedida, false caso contrário
     */
    suspend fun atualizarProjeto(
        uuid: UUID,
        nome: String,
        descricao: String?,
        status: String,
        taxaConclusao: Float
    ): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())

                val updates = buildJsonObject {
                    put("nome", nome)
                    if (descricao != null) put("descricao", descricao) else put("descricao", null)
                    put("status", status)
                    put("taxa_conclusao", taxaConclusao)
                    put("updated_at", now)
                }

                supabase.from(PROJETO_TABLE)
                    .update(updates) {
                        filter {
                            eq("projeto_uuid", uuid.toString())
                        }
                    }

                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Apaga um projeto pelo UUID
     * @param uuid UUID do projeto
     * @return true se a exclusão foi bem-sucedida, false caso contrário
     */
    suspend fun apagarProjeto(uuid: UUID): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                supabase.from(PROJETO_TABLE)
                    .delete {
                        filter {
                            eq("projeto_uuid", uuid.toString())
                        }
                    }

                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Adiciona ou atualiza um usuário em um projeto
    suspend fun adicionarUsuarioAoProjeto(
        userId: String,
        projectId: String,
        isManager: Boolean
    ): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val data = buildJsonObject {
                    put("utilizador_uuid", userId)
                    put("projeto_uuid", projectId)
                    put("e_gestor", isManager)
                    put("ativo", true)
                }
                supabase.from("utilizador_projeto")
                    .upsert(data) { select() }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Lista todos os membros de um projeto
    suspend fun listarMembrosDoProjeto(projectId: String): List<UserProject> {
        return try {
            withContext(Dispatchers.IO) {
                supabase.from("utilizador_projeto")
                    .select() {
                        filter { eq("projeto_uuid", projectId) }
                    }
                    .decodeList<UserProject>()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Lista todos os membros de um projeto com JOIN para obter informações completas
    suspend fun listarMembrosProjetoCompleto(projectId: String): List<UserProject> {
        return try {
            withContext(Dispatchers.IO) {
                val query = Columns.raw("*, utilizador!inner(nome, fotografia, username, admin)")
                supabase.from("utilizador_projeto")
                    .select(columns = query) {
                        filter { eq("projeto_uuid", projectId) }
                    }
                    .decodeList<UserProject>()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Atualiza o status e a função de um membro no projeto
    suspend fun atualizarMembrosDoProjetoStatus(
        userId: String,
        projectId: String,
        isManager: Boolean,
        isActive: Boolean
    ): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val data = buildJsonObject {
                    put("utilizador_uuid", userId)
                    put("projeto_uuid", projectId)
                    put("e_gestor", isManager)
                    put("ativo", isActive)
                }
                supabase.from("utilizador_projeto")
                    .upsert(data) {
                        filter {
                            eq("utilizador_uuid", userId)
                            eq("projeto_uuid", projectId)
                        }
                    }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
