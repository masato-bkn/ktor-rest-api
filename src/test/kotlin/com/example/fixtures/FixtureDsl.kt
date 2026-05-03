package com.example.fixtures

/**
 * @FixtureDsl を付けた Scope クラス同士では、内側のスコープから外側のレシーバのメソッドを暗黙的に呼べなくなる。
 *
 * 例: 以下はコンパイルエラーになる
 * ```
 * fixture {
 *     user(name = "Alice", email = "a@example.com") {
 *         user(name = "Bob", email = "b@example.com") { } // Error: 外側の FixtureScope.user() は呼べない
 *     }
 * }
 * ```
 */
@DslMarker
annotation class FixtureDsl
