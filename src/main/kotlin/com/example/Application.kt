package com.example

import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.example.plugins.configureStatusPages
import io.ktor.server.application.*

/**
 * アプリケーションのエントリポイント
 * application.conf の設定に基づいてNettyサーバーを起動する
 */
fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

/**
 * Ktorアプリケーションモジュール
 * application.conf から自動的に呼び出される
 * プラグインのインストール順序は依存関係を考慮している:
 *   1. Serialization - JSONの変換設定（他のプラグインが依存）
 *   2. StatusPages   - 例外ハンドリング（ルーティングより先に設定）
 *   3. Routing       - APIエンドポイントの定義
 */
fun Application.module() {
    configureSerialization()
    configureStatusPages()
    configureRouting()
}
