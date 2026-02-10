// =============================================================================
// build.gradle.kts - プロジェクトのビルド設定ファイル
// =============================================================================
// Rails でいう Gemfile（依存管理）+ Rakefile（タスク定義）を1つにまとめたもの。
// 拡張子 .kts = Kotlin Script。Groovy版（.gradle）と違い、
// IDEの補完・型チェックが効くため、Kotlinプロジェクトではこちらが主流。
//
// よく使うコマンド:
//   ./gradlew build  - ビルド（bundle install + rake build 相当）
//   ./gradlew run    - サーバー起動（rails server 相当）
//   ./gradlew test   - テスト実行（rake test 相当）
// =============================================================================

// =============================================================================
// バージョン定義
// 依存ライブラリのバージョンを一元管理する（Gemfile のバージョン指定に相当）
// =============================================================================
val ktorVersion = "2.3.12"
val kotlinVersion = "2.0.21"
val logbackVersion = "1.4.14"
val serializationVersion = "1.7.3"

// Gradleプラグイン（ビルドに必要なツール群）
// Gemfile でいう gem のうち、開発ツール系に相当する
plugins {
    kotlin("jvm") version "2.0.21"
    // @Serializable アノテーションによるJSON変換コード自動生成に必要
    kotlin("plugin.serialization") version "2.0.21"
    // ./gradlew run でアプリケーションを起動できるようにする
    application
}

group = "com.example"
version = "0.0.1"

// ./gradlew run で起動するメインクラスを指定
// Kotlinファイル Application.kt のトップレベル関数 main() は、
// コンパイル後に ApplicationKt クラスとして生成される
application {
    mainClass.set("com.example.ApplicationKt")
}

// JDK 24環境でもJVM 21ターゲットでコンパイルする
// Ktor 2.xがJVM 21までを公式サポートしているため
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

// ライブラリの取得先（Ruby でいう source 'https://rubygems.org' に相当）
repositories {
    mavenCentral()
}

// 依存ライブラリ（Gemfile の gem 宣言に相当）
// implementation = 本番用、testImplementation = テスト専用
dependencies {
    // --- Ktorサーバー本体 ---
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")       // コアAPI（routing, callなど）
    // Nettyエンジン（HTTPサーバー実装）
    // 少数のスレッドで大量の同時接続を捌くノンブロッキングI/O方式。
    // 他エンジン（CIO, Jetty, Tomcat）に差し替える場合はこの行を変更する。
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")  // リクエスト/レスポンスの自動変換
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")  // kotlinx.serializationによるJSON処理
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")         // 例外→HTTPステータスコードの変換
    implementation("ch.qos.logback:logback-classic:$logbackVersion")            // ログ出力（Ktor内部で使用）

    // --- テスト ---
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")        // testApplication によるインメモリテスト
    testImplementation("io.ktor:ktor-client-content-negotiation-jvm:$ktorVersion") // テストクライアントでのJSON送受信
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}
