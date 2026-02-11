package com.example.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

/** エラーレスポンスの共通フォーマット（全APIで統一した形式でエラーを返す） */
@Serializable
data class ErrorResponse(val message: String)

/**
 * StatusPagesプラグインの設定
 * アプリケーション内で発生した例外をキャッチし、
 * 適切なHTTPステータスコードとエラーメッセージのJSONに変換する
 *
 * 例外ハンドラはより具体的な型から順に定義する（具体 → 汎用）
 */
fun Application.configureStatusPages() {
    install(StatusPages) {
        // バリデーションエラーなど、クライアント起因の問題 → 400
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message ?: "Bad request"))
        }
        // その他の予期しないエラー → 500（フォールバック）
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(cause.message ?: "Internal server error"),
            )
        }
    }
}
