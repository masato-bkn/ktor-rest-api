package com.example.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

/**
 * ContentNegotiationプラグインの設定
 * リクエスト/レスポンスのContent-Typeに応じて自動的にシリアライズ/デシリアライズを行う
 * - call.receive<T>() でリクエストJSONを自動変換
 * - call.respond(obj) でオブジェクトをJSON出力
 */
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            // レスポンスJSONを整形して見やすくする（開発時に便利）
            prettyPrint = true
        })
    }
}
