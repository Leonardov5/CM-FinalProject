package com.example.finalproject.data.repository

import com.example.finalproject.data.model.ProjectAnalytics
import com.example.finalproject.data.model.Projeto
import com.example.finalproject.data.model.UserProject
import com.example.finalproject.data.service.SupabaseProvider
import com.example.finalproject.data.service.UserService
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

                try {
                    val resultado = supabase.from(PROJETO_TABLE)
                        .insert(novoProjeto) {
                            select()
                        }
                        .decodeSingle<Projeto>()

                    resultado
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun listarProjetos(): List<Projeto> {
        return try {
            withContext(Dispatchers.IO) {
                val currentUser = UserService.getCurrentUserData()
                val isAdmin = currentUser?.admin
                val userId = currentUser?.id.toString()

                if (isAdmin == true) {
                    supabase.from(PROJETO_TABLE)
                        .select(columns = Columns.ALL)
                        .decodeList<Projeto>()
                } else {
                    supabase.from(PROJETO_TABLE)
                        .select(columns = Columns.raw("*,utilizador_projeto!inner(utilizador_uuid,ativo)")) {
                            filter {
                                eq("utilizador_projeto.utilizador_uuid", userId)
                                eq("utilizador_projeto.ativo", true)
                            }
                        }
                        .decodeList<Projeto>()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

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

    suspend fun eliminarProjeto(uuid: UUID): Boolean {
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

    suspend fun adicionarUtilizadorProjeto(
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

    suspend fun listarMembrosProjeto(projectId: String): List<UserProject> {
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

    suspend fun atualizarMembroDoProjeto(
        userId: String,
        projectId: String,
        isManager: Boolean,
        isActive: Boolean,
        performance: Int
    ): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val data = buildJsonObject {
                    put("utilizador_uuid", userId)
                    put("projeto_uuid", projectId)
                    put("e_gestor", isManager)
                    put("ativo", isActive)
                    put("performance", performance)
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

    suspend fun getProjectAnalytics(projectId: String): ProjectAnalytics?{
        return try {
            withContext(Dispatchers.IO) {
                supabase.from("vw_project_stats")
                    .select(columns = Columns.ALL) {
                        filter { eq("projeto_uuid", projectId.toString()) }
                    }.decodeSingleOrNull<ProjectAnalytics>()
            }
        } catch (e: Exception){
            e.printStackTrace()
            null
        }
    }
}