// =============================================================================
// バージョン定義
// 依存ライブラリのバージョンを一元管理する
// =============================================================================
val ktorVersion = "2.3.12"
val kotlinVersion = "2.0.21"
val logbackVersion = "1.4.14"
val serializationVersion = "1.7.3"

plugins {
    kotlin("jvm") version "2.0.21"
    // @Serializable アノテーションによるJSON変換コード自動生成に必要
    kotlin("plugin.serialization") version "2.0.21"
    // ./gradlew run でアプリケーションを起動できるようにする
    application
}

group = "com.example"
version = "0.0.1"

// Nettyサーバーを起動するメイン関数を指定
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

repositories {
    mavenCentral()
}

dependencies {
    // --- Ktorサーバー本体 ---
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")       // コアAPI（routing, callなど）
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")      // Nettyエンジン（HTTPサーバー実装）
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")  // リクエスト/レスポンスの自動変換
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")  // kotlinx.serializationによるJSON処理
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")         // 例外→HTTPステータスコードの変換
    implementation("ch.qos.logback:logback-classic:$logbackVersion")            // ログ出力（Ktor内部で使用）

    // --- テスト ---
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")        // testApplication によるインメモリテスト
    testImplementation("io.ktor:ktor-client-content-negotiation-jvm:$ktorVersion") // テストクライアントでのJSON送受信
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}
