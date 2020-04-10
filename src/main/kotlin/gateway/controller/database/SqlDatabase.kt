package gateway.controller.database

import java.sql.Connection
import java.sql.DriverManager

class SqlDatabase(private val dsn: String) : Storage {
    fun <T> useConnection(block: (Connection) -> T) =
        DriverManager.getConnection(dsn).use(block)
}
