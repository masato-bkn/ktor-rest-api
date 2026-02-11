# Ktor REST API

Kotlin + Ktor で構築したタスク管理 REST API です。

## 技術スタック

- **Kotlin** 2.0.21
- **Ktor** 2.3.12 (Netty エンジン)
- **kotlinx.serialization** (JSON 処理)
- **Gradle** (Kotlin DSL)

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
