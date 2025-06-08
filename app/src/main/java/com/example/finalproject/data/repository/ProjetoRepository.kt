package com.example.finalproject.data.repository

import com.example.finalproject.data.model.Projeto
import com.example.finalproject.data.service.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
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
     * @return O projeto criado ou null em caso de falha
     */
    suspend fun criarProjeto(nome: String, descricao: String?): Projeto? {
        return try {
            withContext(Dispatchers.IO) {
                val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())

                val novoProjeto = buildJsonObject {
                    put("nome", nome)
                    if (descricao != null) put("descricao", descricao)
                    put("status", "ativo")
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
     * @param projeto Projeto com os dados atualizados
     * @return true se a atualização foi bem-sucedida, false caso contrário
     */
    suspend fun atualizarProjeto(projeto: Projeto): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val updates = buildJsonObject {
                    put("nome", projeto.nome)
                    if (projeto.descricao != null) put("descricao", projeto.descricao)
                    put("status", projeto.status)
                }

                supabase.from(PROJETO_TABLE)
                    .update(updates) {
                        filter {
                            eq("projeto_uuid", projeto.id.toString())
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
     * Altera o status de um projeto
     * @param uuid UUID do projeto
     * @param status Novo status do projeto
     * @return true se a atualização foi bem-sucedida, false caso contrário
     */
    suspend fun alterarStatusProjeto(uuid: UUID, status: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val updates = buildJsonObject {
                    put("status", status)
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
}
