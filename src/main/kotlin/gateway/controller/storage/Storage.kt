package gateway.controller.storage

import java.sql.Connection
import java.sql.DriverManager

class Storage(
    private val url: String,
    private val user: String,
    private val password: String
) {
    fun getConnection(): Connection =
        DriverManager.getConnection(url, user, password)
}
