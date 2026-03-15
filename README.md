# Ktor REST API

Kotlin + Ktor で構築したタスク管理 REST API です。

## 技術スタック

- **Kotlin** 2.0.21
- **Ktor** 2.3.12 (Netty エンジン)
- **kotlinx.serialization** (JSON 処理)
- **Gradle** (Kotlin DSL)

## Dev Container

VSCode + Docker Desktop があれば、ローカルにJDKやPostgreSQLをインストールせずに開発できます。

### コンテナ構成

```
┌─────────────────────────────────────────────────┐
│ このMac（ホスト）                                  │
│                                                  │
│  Docker Desktop                                  │
│  └── Dockerデーモン                              │
│       │                                          │
│       ├── appコンテナ（Dev Container）            │
│       │    ├── JDK 21                            │
│       │    ├── /workspace ← Macのプロジェクトをマウント│
│       │    └── ここで ./gradlew run / test を実行 │
│       │                                          │
│       ├── dbコンテナ（PostgreSQL 16）            │
│       │    └── アプリが接続するDB                 │
│       │                                          │
│       └── テスト用PostgreSQLコンテナ             │
│            └── Testcontainersが自動で起動・削除   │
│                                                  │
└─────────────────────────────────────────────────┘
```

### 起動方法

1. VSCodeでこのフォルダを開く
2. `Cmd+Shift+P` → 「Dev Containers: Reopen in Container」
3. コンテナ起動後、VSCodeのターミナルで：

```bash
./gradlew run   # サーバー起動 → http://localhost:8080
./gradlew test  # テスト実行
```

## セットアップ

```bash
# ビルド
./gradlew build

# 起動 (http://localhost:8080)
./gradlew run

# テスト
./gradlew test
```

## API エンドポイント

### GET /tasks

全タスクを取得する。

```bash
curl http://localhost:8080/tasks
```

### GET /tasks/{id}

指定IDのタスクを取得する。

```bash
curl http://localhost:8080/tasks/1
```

### POST /tasks

タスクを作成する。

```bash
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "買い物", "description": "牛乳を買う"}'
```

レスポンス例:

```json
{
  "id": 1,
  "title": "買い物",
  "description": "牛乳を買う",
  "completed": false
}
```

### PUT /tasks/{id}

タスクを更新する（部分更新対応）。

```bash
curl -X PUT http://localhost:8080/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"completed": true}'
```

### DELETE /tasks/{id}

タスクを削除する。

```bash
curl -X DELETE http://localhost:8080/tasks/1
```

## プロジェクト構成

```
src/main/kotlin/com/example/
├── Application.kt          # エントリポイント、モジュール設定
├── models/
│   └── Task.kt             # データモデル、インメモリリポジトリ
├── plugins/
│   ├── Routing.kt          # ルーティング設定
│   ├── Serialization.kt    # JSON シリアライズ設定
│   └── StatusPages.kt      # エラーハンドリング
└── routes/
    └── TaskRoutes.kt       # /tasks エンドポイント定義
```

## テストの仕組みメモ

### `./gradlew test` が動く仕組み

`src/test/` がテストコード置き場になり、`@Test` が付いたメソッドが自動でテスト対象になるのは **Gradle + JUnit の仕様**。Kotlin側では何も設定していない。

```kotlin
// build.gradle.kts
testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
//                                               ↑
//                          この依存関係を追加するだけで有効になる
```

JUnitの規約として `src/test/` 以下がテスト置き場、`@Test` が付いたメソッドがテスト対象と決まっており、Gradleはそれを自動で認識して実行する。

### Ryuk について

Testcontainers はテスト終了後にコンテナを自動削除するため、裏で **Ryuk** という専用コンテナを立ち上げる。テストがクラッシュしても孤児コンテナが残らないようにする仕組み。

```
Ryukコンテナ（監視役）
    ↓ テスト終了 or クラッシュを検知
テスト用PostgreSQLコンテナを自動削除
```

Dev Container 内では Ryuk がホストへの逆方向通信に失敗するため、`TESTCONTAINERS_RYUK_DISABLED=true` で無効化している（`.devcontainer/docker-compose.yml`）。無効化してもテスト終了時にコンテナは通常通り削除される。

### DevContainer 内でのネットワーク構成

DevContainer 内でテストを実行すると、Testcontainers は Docker デーモン（Linux VM 内）を使って PostgreSQL コンテナを起動する。このとき接続先ホストが問題になる。

```
devcontainer の中から見た localhost
    ↓
自分自身（devcontainer）を指す
    ↓
Testcontainers が起動した PostgreSQL コンテナには届かない！
```

`TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal` を設定することで解決する。

```
devcontainer → host.docker.internal:ランダムポート
                    ↓
             VM のネットワーク経由
                    ↓
             PostgreSQL コンテナ ✅
```

この設定は `.devcontainer/docker-compose.yml` の `environment` に記載している。

また、Docker ソケットのマウントは `- /var/run/docker.sock:/var/run/docker.sock` を使う。
Docker Desktop は Linux VM 内で動作しており、devcontainer も同じ VM 内で動くため
VM 内の `/var/run/docker.sock` を直接マウントすればよい
（Mac ホスト上に `/var/run/docker.sock` がなくても問題ない）。

### テスト実行の流れ

```
./gradlew test
    ↓
JUnit が src/test/ 以下の @Test メソッドを検出
    ↓
@BeforeClass でテストスイート全体の初期化
    → Testcontainers が PostgreSQL コンテナを起動
    → Flyway でマイグレーション実行
    ↓
各 @Test メソッドを実行
    → @Before で TestDatabaseFactory.clean()（DBリセット）
    → テスト本体（APIにリクエスト → レスポンス検証）
    ↓
@AfterClass でコンテナ停止・削除
```

## Ktor の仕組みメモ

### 唯一の「自動的な」仕組み

`application.conf` の以下の設定だけが Ktor のフレームワーク機構：

```hocon
modules = [ com.example.ApplicationKt.module ]
```

Ktor の `EngineMain` がこの設定を読み、`Application.module()` を自動で呼び出す。
これが唯一の"魔法"で、それ以外は全部ただの Kotlin コード。

### ディレクトリ構成は自由

```
plugins/  → プラグイン設定をまとめる慣習的なディレクトリ（名前は自由）
routes/   → ルーティングをまとめるディレクトリ（名前は自由）
models/   → データクラスを置く場所（名前は自由）
```

Spring Boot のように「このディレクトリに置けば自動スキャン」のような仕組みはない。
`plugins/` や `routes/` という名前も単なる慣習で、好きに変えられる。

### Spring Boot との違い

| | Ktor | Spring Boot |
|---|---|---|
| 設計思想 | 全部明示的に書く | Convention over Configuration |
| DI | なし（必要なら自分で導入） | 標準装備（@Autowired） |
| アノテーション | ほぼ不要 | 多用（@RestController 等） |
| モジュール読み込み | application.conf で指定 | クラスパススキャンで自動検出 |

Ktor は何がどう動いているか追いやすいのが特徴。

### 公式プロジェクトジェネレーター

[start.ktor.io](https://start.ktor.io) で必要なプラグインを選んでプロジェクトを生成できる。
生成される構成はこのプロジェクトとほぼ同じパターン。

## ドキュメント

- [Kotlin 文法メモ（Rubyユーザー向け）](docs/kotlin-syntax.md)

## 備考

- データはインメモリで管理しているため、サーバー再起動でリセットされます
- ポート番号は `src/main/resources/application.conf` で変更できます
