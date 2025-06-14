package com.example.finalproject.data.repository

import com.example.finalproject.data.model.Observacao
import com.example.finalproject.data.service.AuthService
import com.example.finalproject.data.service.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage

class ObservacaoRepository {
    private val supabase = SupabaseProvider.client

    suspend fun listarObservacoesPorTarefa(tarefaId: String): List<Observacao> {
        return try {
            val observacoes = supabase.from("observacao")
                .select {
                    filter {
                        eq("tarefa_uuid", tarefaId)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Observacao>()
                
            println("DEBUG - Observações carregadas: ${observacoes.size}")
            observacoes
        } catch (e: Exception) {
            println("DEBUG - Erro ao listar observações: ${e.message}")
            emptyList()
        }
    }

    suspend fun criarObservacao(tarefaId: String, observacao: String, imagens: List<ByteArray> = emptyList()): Observacao? {
        return try {
            val currentUserUUID = AuthService.getCurrentUserId()
            
            // Criar a observação
            val novaObservacao = supabase.from("observacao").insert(
                Observacao(
                    tarefaId = tarefaId,
                    observacao = observacao,
                    createdBy = currentUserUUID,
                    modifiedBy = currentUserUUID
                )
            ) {
                select()
            }.decodeSingle<Observacao>()
            
            // Upload das imagens, se houver
            val imagensUrls = mutableListOf<String>()
            if (imagens.isNotEmpty() && novaObservacao.id != null) {
                imagens.forEachIndexed { index, imagemBytes ->
                    val imagemNome = "${novaObservacao.id}_imagem_$index.jpg"
                    supabase.storage.from("observacoes").upload(imagemNome, imagemBytes)
                    
                    // Obter URL da imagem
                    val imagemUrl = supabase.storage.from("observacoes").publicUrl(imagemNome)
                    imagensUrls.add(imagemUrl)
                }
                
                // Atualizar a observação com as URLs das imagens
                if (imagensUrls.isNotEmpty()) {
                    supabase.from("observacao").update(
                        {
                            set("anexos", imagensUrls) // Corrigido de "imagens" para "anexos"
                        }
                    ) {
                        filter {
                            eq("observacao_uuid", novaObservacao.id)
                        }
                    }
                }
            }
            
            // Retornar a observação criada
            novaObservacao.copy(anexos = imagensUrls)
        } catch (e: Exception) {
            println("DEBUG - Erro ao criar observação: ${e.message}")
            null
        }
    }

    suspend fun atualizarObservacao(
        observacaoId: String,
        textoObservacao: String,
        imagensAtuais: List<String>,
        novasImagens: List<ByteArray> = emptyList()
    ): Observacao? {
        return try {
            val currentUserUUID = AuthService.getCurrentUserId()

            // Buscar a observação atual
            val observacaoAtual = supabase.from("observacao")
                .select {
                    filter {
                        eq("observacao_uuid", observacaoId)
                    }
                }
                .decodeSingle<Observacao>()

            // Identificar imagens a remover (estão na observação atual mas não em imagensAtuais)
            val imagensParaRemover = observacaoAtual.anexos.filter { url -> url !in imagensAtuais }

            // Remover imagens do armazenamento que não estão mais na lista
            imagensParaRemover.forEach { imagemUrl ->
                try {
                    val imagemNome = imagemUrl.substringAfterLast("/")
                    supabase.storage.from("observacoes").delete(imagemNome)
                } catch (e: Exception) {
                    println("DEBUG - Erro ao excluir imagem ${imagemUrl}: ${e.message}")
                    // Continua mesmo se falhar a exclusão de uma imagem
                }
            }

            // Definir lista final de URLs (as que permaneceram + novas)
            val imagensUrls = imagensAtuais.toMutableList()

            // Upload de novas imagens, se houver
            if (novasImagens.isNotEmpty()) {
                novasImagens.forEachIndexed { index, imagemBytes ->
                    val imagemNome = "${observacaoId}_imagem_${System.currentTimeMillis()}_$index.jpg"
                    supabase.storage.from("observacoes").upload(imagemNome, imagemBytes)

                    // Obter URL da nova imagem
                    val imagemUrl = supabase.storage.from("observacoes").publicUrl(imagemNome)
                    imagensUrls.add(imagemUrl)
                }
            }

            // Atualizar a observação
            supabase.from("observacao").update(
                {
                    set("observacao", textoObservacao)
                    set("anexos", imagensUrls)
                    set("modified_by", currentUserUUID)
                }
            ) {
                filter {
                    eq("observacao_uuid", observacaoId)
                }
            }

            // Retornar a observação atualizada
            supabase.from("observacao")
                .select {
                    filter {
                        eq("observacao_uuid", observacaoId)
                    }
                }
                .decodeSingle<Observacao>()
        } catch (e: Exception) {
            println("DEBUG - Erro ao atualizar observação: ${e.message}")
            null
        }
    }

    suspend fun excluirImagem(observacaoId: String, imagemUrl: String): Boolean {
        return try {
            // Buscar observação para obter as imagens
            val observacao = supabase.from("observacao")
                .select {
                    filter {
                        eq("observacao_uuid", observacaoId)
                    }
                }
                .decodeSingle<Observacao>()

            // Remover a imagem do armazenamento
            val imagemNome = imagemUrl.substringAfterLast("/")
            supabase.storage.from("observacoes").delete(imagemNome)

            // Atualizar a lista de anexos da observação
            val novasUrls = observacao.anexos.filter { it != imagemUrl }

            // Atualizar a observação sem a imagem excluída
            supabase.from("observacao").update(
                {
                    set("anexos", novasUrls)
                    set("modified_by", AuthService.getCurrentUserId())
                }
            ) {
                filter {
                    eq("observacao_uuid", observacaoId)
                }
            }

            true
        } catch (e: Exception) {
            println("DEBUG - Erro ao excluir imagem: ${e.message}")
            false
        }
    }

    suspend fun excluirObservacao(observacaoId: String): Boolean {
        return try {
            // Primeiro, buscar a observação para obter as informações das imagens
            val observacao = supabase.from("observacao")
                .select {
                    filter {
                        eq("observacao_uuid", observacaoId)
                    }
                }
                .decodeSingle<Observacao>()
            
            // Excluir as imagens da observação, se houver
            observacao.anexos.forEach { imagemUrl ->
                try {
                    val imagemNome = imagemUrl.substringAfterLast("/")
                    supabase.storage.from("observacoes").delete(imagemNome)
                } catch (e: Exception) {
                    println("DEBUG - Erro ao excluir imagem ${imagemUrl}: ${e.message}")
                    // Continua tentando excluir as outras imagens
                }
            }

            // Excluir a observação
            supabase.from("observacao").delete {
                filter {
                    eq("observacao_uuid", observacaoId)
                }
            }
            
            true
        } catch (e: Exception) {
            println("DEBUG - Erro ao excluir observação: ${e.message}")
            false
        }
    }
}
