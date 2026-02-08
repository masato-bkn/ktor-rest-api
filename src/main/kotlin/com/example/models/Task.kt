package com.example.models

import kotlinx.serialization.Serializable

// =============================================================================
// データモデル
// @Serializable により JSON ⇔ オブジェクト の相互変換が自動生成される
// =============================================================================

/** タスクのレスポンスモデル（APIから返却されるデータ構造） */
@Serializable
data class Task(
    val id: Int,
    val title: String,
    val description: String = "",
    val completed: Boolean = false
)

/** タスク作成時のリクエストボディ（POST /tasks で使用） */
@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String = ""
)

/**
 * タスク更新時のリクエストボディ（PUT /tasks/{id} で使用）
 * 全フィールドがnullable → 送信されたフィールドのみ更新する（部分更新）
 */
@Serializable
data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val completed: Boolean? = null
)

// =============================================================================
// リポジトリ
// インメモリでタスクを管理する。サーバー再起動でデータはリセットされる。
// DB導入時はこのobjectをインターフェースに切り替える想定。
// =============================================================================

/** タスクのCRUD操作を提供するインメモリリポジトリ */
object TaskRepository {
    private val tasks = mutableListOf<Task>()
    /** 自動採番用カウンター（削除しても再利用しない） */
    private var nextId = 1

    /** 全タスクを返す。防御的コピーにより内部リストの直接変更を防ぐ */
    fun all(): List<Task> = tasks.toList()

    fun findById(id: Int): Task? = tasks.find { it.id == id }

    fun create(request: CreateTaskRequest): Task {
        val task = Task(
            id = nextId++,
            title = request.title,
            description = request.description
        )
        tasks.add(task)
        return task
    }

    /**
     * 指定IDのタスクを部分更新する
     * リクエストのnullでないフィールドのみ上書きし、
     * nullのフィールドは既存の値を保持する
     */
    fun update(id: Int, request: UpdateTaskRequest): Task? {
        val index = tasks.indexOfFirst { it.id == id }
        if (index == -1) return null

        val existing = tasks[index]
        val updated = existing.copy(
            title = request.title ?: existing.title,
            description = request.description ?: existing.description,
            completed = request.completed ?: existing.completed
        )
        tasks[index] = updated
        return updated
    }

    fun delete(id: Int): Boolean {
        return tasks.removeAll { it.id == id }
    }

    /** テスト用: データとIDカウンターをリセットする */
    fun clear() {
        tasks.clear()
        nextId = 1
    }
}
