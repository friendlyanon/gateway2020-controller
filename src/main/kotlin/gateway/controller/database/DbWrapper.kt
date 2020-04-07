package gateway.controller.database

import org.mapdb.DB
import org.mapdb.Serializer

class DbWrapper(private val db: DB) {
    enum class Table { DETAILS, STORE }

    fun readOnlyUse(tbl: Table, block: (Map<String, String>) -> Unit) {
        block(getMap(tbl))
    }

    fun readWriteUse(tbl: Table, block: (MutableMap<String, String>) -> Unit) {
        block(getMap(tbl))
        db.commit()
    }

    private fun getMap(tbl: Table) = db
        .hashMap(tbl.name.toLowerCase(), Serializer.STRING, Serializer.STRING)
        .createOrOpen()
}
