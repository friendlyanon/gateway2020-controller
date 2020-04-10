package gateway.controller.database

import org.mapdb.DB
import org.mapdb.Serializer

class DbWrapper(private val db: DB) : Storage {
    enum class Table { DETAILS, STORE }

    fun <T> readOnlyUse(tbl: Table, block: Map<String, String>.() -> T) =
        getMap(tbl).block()

    fun <T> readWriteUse(
        tbl: Table,
        block: MutableMap<String, String>.() -> T
    ): T {
        try {
            return getMap(tbl).block()
        } finally {
            db.commit()
        }
    }

    private fun getMap(tbl: Table) = db
        .hashMap(tbl.name.toLowerCase(), Serializer.STRING, Serializer.STRING)
        .createOrOpen()
}
