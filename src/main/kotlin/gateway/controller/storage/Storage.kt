package gateway.controller.storage

import java.sql.DriverManager

class Storage(
    private val url: String,
    private val user: String,
    private val password: String
) {
    fun getConnection() = DriverManager.getConnection(url, user, password)
}
