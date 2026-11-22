package com.mihir.island

enum class IslandActivityKind { MEDIA, TIMER, CALL, NAVIGATION, DELIVERY, SPORTS, FITNESS, TRANSIT, FLIGHT, TRANSFER, NOTIFICATION }

data class IslandActivityModel(
    val id: String, val kind: IslandActivityKind, val title: String, val subtitle: String = "",
    val compactText: String = "", val expandedText: String = "", val priority: Int = 0,
    val updatedAtMillis: Long = System.currentTimeMillis(), val expiresAtMillis: Long? = null
)

object IslandActivityRegistry {
    private val entries = LinkedHashMap<String, IslandActivityModel>()
    @Synchronized fun publish(model: IslandActivityModel) { entries[model.id] = model }
    @Synchronized fun remove(id: String) { entries.remove(id) }
    @Synchronized fun clear(kind: IslandActivityKind) { entries.entries.removeAll { it.value.kind == kind } }
    @Synchronized fun active(now: Long = System.currentTimeMillis()): List<IslandActivityModel> {
        entries.entries.removeAll { it.value.expiresAtMillis?.let { expiry -> expiry <= now } == true }
        return entries.values.sortedWith(compareByDescending<IslandActivityModel> { it.priority }.thenByDescending { it.updatedAtMillis })
    }
    @Synchronized fun top(now: Long = System.currentTimeMillis()): IslandActivityModel? = active(now).firstOrNull()
}