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

            observacoes
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun criarObservacao(tarefaId: String, observacao: String, imagens: List<ByteArray> = emptyList()): Observacao? {
        return try {
            val currentUserUUID = AuthService.getCurrentUserId()

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
            
            // Upload das imagens
            val imagensUrls = mutableListOf<String>()
            if (imagens.isNotEmpty() && novaObservacao.id != null) {
                imagens.forEachIndexed { index, imagemBytes ->
                    val imagemNome = "${novaObservacao.id}_imagem_$index.jpg"
                    supabase.storage.from("observacoes").upload(imagemNome, imagemBytes)

                    val imagemUrl = supabase.storage.from("observacoes").publicUrl(imagemNome)
                    imagensUrls.add(imagemUrl)
                }

                if (imagensUrls.isNotEmpty()) {
                    supabase.from("observacao").update(
                        {
                            set("anexos", imagensUrls)
                        }
                    ) {
                        filter {
                            eq("observacao_uuid", novaObservacao.id)
                        }
                    }
                }
            }

            novaObservacao.copy(anexos = imagensUrls)
        } catch (e: Exception) {
            e.printStackTrace()
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

            val observacaoAtual = supabase.from("observacao")
                .select {
                    filter {
                        eq("observacao_uuid", observacaoId)
                    }
                }
                .decodeSingle<Observacao>()

            val imagensParaRemover = observacaoAtual.anexos.filter { url -> url !in imagensAtuais }

            imagensParaRemover.forEach { imagemUrl ->
                try {
                    val imagemNome = imagemUrl.substringAfterLast("/")
                    supabase.storage.from("observacoes").delete(imagemNome)
                } catch (e: Exception) {
                    e.printStackTrace()                }
            }

            val imagensUrls = imagensAtuais.toMutableList()

            // Upload das novas imagens
            if (novasImagens.isNotEmpty()) {
                novasImagens.forEachIndexed { index, imagemBytes ->
                    val imagemNome = "${observacaoId}_imagem_${System.currentTimeMillis()}_$index.jpg"
                    supabase.storage.from("observacoes").upload(imagemNome, imagemBytes)

                    val imagemUrl = supabase.storage.from("observacoes").publicUrl(imagemNome)
                    imagensUrls.add(imagemUrl)
                }
            }

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

            // Retorna a observação atualizada
            supabase.from("observacao")
                .select {
                    filter {
                        eq("observacao_uuid", observacaoId)
                    }
                }
                .decodeSingle<Observacao>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun eliminarImagem(observacaoId: String, imagemUrl: String): Boolean {
        return try {
            val observacao = supabase.from("observacao")
                .select {
                    filter {
                        eq("observacao_uuid", observacaoId)
                    }
                }
                .decodeSingle<Observacao>()

            val imagemNome = imagemUrl.substringAfterLast("/")
            supabase.storage.from("observacoes").delete(imagemNome)

            val novasUrls = observacao.anexos.filter { it != imagemUrl }

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
            e.printStackTrace()
            false
        }
    }

    suspend fun eliminarObservacao(observacaoId: String): Boolean {
        return try {
            val observacao = supabase.from("observacao")
                .select {
                    filter {
                        eq("observacao_uuid", observacaoId)
                    }
                }
                .decodeSingle<Observacao>()

            observacao.anexos.forEach { imagemUrl ->
                try {
                    val imagemNome = imagemUrl.substringAfterLast("/")
                    supabase.storage.from("observacoes").delete(imagemNome)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            supabase.from("observacao").delete {
                filter {
                    eq("observacao_uuid", observacaoId)
                }
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
