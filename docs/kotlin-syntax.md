# Kotlin 文法メモ（Rubyユーザー向け）

このプロジェクトで登場した Kotlin 特有の文法をまとめる。

## インクリメント演算子 `++`

Ruby には存在しない演算子。C / Java / Kotlin 系共通。

```kotlin
var n = 1

// 後置（postfix）: 現在の値を使ってから +1
val a = n++   // a = 1, n = 2

// 前置（prefix）: +1 してから値を使う
val b = ++n   // n = 3, b = 3
```

```ruby
# Ruby で同じことをする場合
a = n       # 後置相当
n += 1

n += 1      # 前置相当
b = n
```

## ラベル付き return（`return@get`, `return@post` 等）

ラムダの中では素の `return` が使えないため、`@ラベル` でどのラムダから抜けるかを指定する。
Ktor 固有ではなく **Kotlin の言語機能**。

```kotlin
get("{id}") {
    val id = call.parameters["id"]?.toIntOrNull()
        ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))
    //   ^^^^^^^^^^^ このラムダ（get）から抜ける

    call.respond(task)
}
```

```ruby
# Ruby の next に近い（ブロックの残りをスキップ）
get ":id" do
  id = params[:id].to_i
  next halt(400, "Invalid ID") if id.zero?

  json task
end
```

## エルビス演算子 `?:`

`?.` で null チェックし、null だった場合のフォールバックを `?:` で指定する。

```kotlin
val id = call.parameters["id"]?.toIntOrNull()
    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))
// parameters["id"] が null → toIntOrNull() をスキップ → ?: の右辺を実行
// toIntOrNull() が null（数値でない）→ ?: の右辺を実行
```

```ruby
# Ruby だと条件分岐で書く
id = params[:id]&.to_i  # &. は Kotlin の ?. に相当
halt(400, "Invalid ID") unless id
```

## セーフコール `?.`

null かもしれないオブジェクトに対して安全にメソッドを呼ぶ。null なら即座に null を返す。

```kotlin
val length = name?.length  // name が null なら length も null
```

```ruby
length = name&.length  # Ruby のぼっち演算子と同じ
```

## `object` 宣言（シングルトン）

`object` で宣言するとクラスのインスタンスがアプリ全体で1つだけになる。

```kotlin
object UserRepository {
    private val users = mutableListOf<User>()
    fun all(): List<User> = users.toList()
}

// 使用時（new 不要、直接アクセス）
UserRepository.all()
```

```ruby
# Ruby だとクラスメソッドで似たことをする
class UserRepository
  @users = []
  def self.all = @users.dup
end

UserRepository.all
```

## data class

`equals`, `hashCode`, `toString`, `copy` を自動生成するクラス。
Ruby の `Struct` や `Data`（Ruby 3.2+）に近い。

```kotlin
data class User(val id: Int, val name: String, val email: String)

// copy で一部フィールドだけ変えた新しいインスタンスを作れる
val updated = user.copy(name = "New Name")
```

```ruby
User = Data.define(:id, :name, :email)
# ただし Ruby の Data は immutable で copy 相当はない
```

## `val` と `var`

```kotlin
val name = "Alice"   // 再代入不可（Ruby の freeze に近いが変数自体が不変）
var count = 0        // 再代入可
```

Ruby には対応する仕組みがない（定数 `NAME = "Alice"` は警告が出るだけで再代入できる）。

## 文字列テンプレート `$`

```kotlin
val name = "Alice"
println("Hello, $name")            // 変数の埋め込み
println("ID: ${user.id}")          // 式の埋め込み
```

```ruby
puts "Hello, #{name}"    # Ruby の式展開と同じ
```
