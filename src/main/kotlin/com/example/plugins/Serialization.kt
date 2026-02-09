package com.example.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

/**
 * ContentNegotiationプラグインの設定
 *
 * Content Negotiation = クライアントとサーバーが Content-Type / Accept ヘッダーで
 * 「どのデータ形式でやり取りするか」を決める HTTP の仕組み。
 * このプラグインを install することで、Kotlin オブジェクト ⇔ JSON の自動変換が有効になる。
 *
 * Rails ではフレームワークに組み込み済み（render json: @task で自動変換）だが、
 * Ktor では明示的にこの設定が必要。
 *
 * これにより以下が可能になる:
 * - call.receive<T>() でリクエスト JSON → Kotlin オブジェクトに自動変換
 * - call.respond(obj) で Kotlin オブジェクト → JSON に自動変換
 *
 * NOTE: テスト側のクライアントにも同様の設定が必要（TaskRoutesTest#jsonClient 参照）
 */
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            // レスポンスJSONを整形して見やすくする（開発時に便利）
            prettyPrint = true
        })
    }
}
