// =============================================================================
// build.gradle.kts - プロジェクトのビルド設定ファイル
// =============================================================================
// Rails でいう Gemfile（依存管理）+ Rakefile（タスク定義）を1つにまとめたもの。
// 拡張子 .kts = Kotlin Script。Groovy版（.gradle）と違い、
// IDEの補完・型チェックが効くため、Kotlinプロジェクトではこちらが主流。
//
// よく使うコマンド:
//   ./gradlew build  - フルビルド（下記の処理を順に実行）
//                      1. compileKotlin   : .kt → .class にコンパイル
//                      2. processResources: application.conf 等をコピー
//                      3. jar             : .class を JAR ファイルに固める
//                      4. compileTestKotlin: テストコードをコンパイル
//                      5. test            : テスト実行
//                      6. check           : テスト結果の検証
//                      ※ Ruby はインタプリタ言語なのでコンパイル不要だが、
//                        Kotlin はコンパイル言語のため「ソース→バイトコード」変換が必須
//   ./gradlew run    - サーバー起動（rails server 相当）
//   ./gradlew test   - テスト実行（rake test 相当。build との違いは JAR 生成をスキップする点）
// =============================================================================

// =============================================================================
// バージョン定義
// 依存ライブラリのバージョンを一元管理する（Gemfile のバージョン指定に相当）
// =============================================================================
val ktorVersion = "2.3.12"
val kotlinVersion = "2.0.21"
val logbackVersion = "1.4.14"
val serializationVersion = "1.7.3"
val exposedVersion = "0.56.0" // JetBrains製 Kotlin ORM（Rails の ActiveRecord に相当）
val flywayVersion = "11.3.0" // DBマイグレーションツール（Rails の db:migrate に相当）
val testcontainersVersion = "1.20.4" // テスト時に Docker コンテナを自動起動するライブラリ

// Gradleプラグイン（ビルドに必要なツール群）
// Gemfile でいう gem のうち、開発ツール系に相当する
plugins {
    kotlin("jvm") version "2.0.21"
    // @Serializable アノテーションによるJSON変換コード自動生成に必要
    kotlin("plugin.serialization") version "2.0.21"
    // ./gradlew run でアプリケーションを起動できるようにする
    application
    // コードフォーマッター（Kotlin公式スタイルガイド準拠）
    // ./gradlew ktlintCheck でチェック、./gradlew ktlintFormat で自動修正
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
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
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion") // コアAPI（routing, callなど）
    // Nettyエンジン（HTTPサーバー実装）
    // 少数のスレッドで大量の同時接続を捌くノンブロッキングI/O方式。
    // 他エンジン（CIO, Jetty, Tomcat）に差し替える場合はこの行を変更する。
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion") // リクエスト/レスポンスの自動変換
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion") // kotlinx.serializationによるJSON処理
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion") // 例外→HTTPステータスコードの変換
    implementation("ch.qos.logback:logback-classic:$logbackVersion") // ログ出力（Ktor内部で使用）

    // --- DB (Exposed + PostgreSQL + HikariCP) ---
    // Exposed: JetBrains製の Kotlin ORM。SQL を Kotlin DSL で書ける。
    //   Rails の ActiveRecord が「モデル定義 → SQL自動生成」なのと同様に、
    //   Exposed も「テーブル定義 → 型安全なクエリ」を提供する。
    //   ただし ActiveRecord のような規約ベース（命名で自動マッピング）ではなく、
    //   明示的にテーブル定義とクエリを書く DSL スタイル。
    //   core = テーブル定義・クエリDSL、jdbc = JDBC経由のDB接続
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    // PostgreSQL JDBC ドライバ（Java/KotlinからPostgreSQLに接続するための橋渡し）
    // Ruby でいう pg gem に相当
    implementation("org.postgresql:postgresql:42.7.4")
    // HikariCP: 高速なDBコネクションプール
    // DB接続の「使い回し」を管理する。接続を毎回作り直すのは遅いため、
    // プール（接続の在庫）を保持して再利用する。
    // Rails では ActiveRecord 内部に組み込まれているが（pool: 5 の設定）、
    // Kotlin/JVM では HikariCP を明示的に導入する。
    implementation("com.zaxxer:HikariCP:6.2.1")

    // --- Flyway (DBマイグレーション) ---
    // SQLファイルベースでスキーマ変更履歴を管理する。Rails の db:migrate に相当。
    // flyway-core: マイグレーションエンジン本体
    // flyway-database-postgresql: PostgreSQL 固有のサポート（Flyway 10+ で必要）
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")

    // --- テスト ---
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion") // testApplication によるインメモリテスト
    testImplementation("io.ktor:ktor-client-content-negotiation-jvm:$ktorVersion") // テストクライアントでのJSON送受信
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")

    // --- Testcontainers (テスト用 PostgreSQL コンテナ) ---
    // テストコードから Docker で本物の PostgreSQL コンテナを自動起動・停止する。
    // H2 等のインメモリDBで代用するとPostgreSQL固有の挙動をテストできないため、
    // 本番と同じDBエンジンでテストする戦略を採用。
    // Ruby でいう database_cleaner + Docker の組み合わせに近い。
    //   testcontainers: コア（Docker操作の抽象化）
    //   postgresql:     PostgreSQLContainer クラスを提供
    //   junit-jupiter:  JUnit5との統合（@Testcontainers アノテーション等）
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
}
