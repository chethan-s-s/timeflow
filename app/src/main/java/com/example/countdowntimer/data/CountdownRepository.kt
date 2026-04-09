package com.example.countdowntimer.data

class CountdownRepository(private val dao: CountdownDao) {

    fun getAll() = dao.getAll()

    suspend fun getAllList() = dao.getAllList()

    suspend fun add(
        title: String,
        time: Long,
        imageUri: String? = null,
        colorIndex: Int = 0,
        repeatYearly: Boolean = false,
        category: String = "General"
    ): Long {
        return dao.insert(
            CountdownEntity(
                title = title,
                targetTime = time,
                imageUri = imageUri,
                colorIndex = colorIndex,
                repeatYearly = repeatYearly,
                category = category
            )
        )
    }

    suspend fun delete(item: CountdownEntity) {
        dao.delete(item)
    }

    suspend fun update(item: CountdownEntity) {
        dao.update(item)
    }

    suspend fun getById(id: Int) = dao.getCountdownById(id)

    suspend fun getByIds(ids: List<Int>) = dao.getCountdownsByIds(ids)

    suspend fun updateArchived(ids: List<Int>, archived: Boolean) {
        if (ids.isEmpty()) return
        dao.updateArchived(ids, archived)
    }

    suspend fun updateCategory(ids: List<Int>, category: String) {
        if (ids.isEmpty()) return
        dao.updateCategory(ids, category)
    }

    suspend fun deleteByIds(ids: List<Int>) {
        if (ids.isEmpty()) return
        dao.deleteByIds(ids)
    }

    suspend fun insertAll(items: List<CountdownEntity>): List<Long> {
        if (items.isEmpty()) return emptyList()
        return dao.insertAll(items)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}