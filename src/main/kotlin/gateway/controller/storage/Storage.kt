package gateway.controller.storage

import java.sql.Connection
import java.sql.DriverManager

class Storage(private val dsn: String) {
    fun <T> useConnection(block: (Connection) -> T) =
        DriverManager.getConnection(dsn).use(block)
}
