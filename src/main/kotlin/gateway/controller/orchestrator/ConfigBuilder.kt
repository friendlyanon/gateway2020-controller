package gateway.controller.orchestrator

import gateway.controller.utils.JSONObject
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class ConfigBuilder(gatewayId: Int, conn: Connection) {
    val result = JSONObject()
    private var tables: Iterator<String>? = null

    init {
        val stmt = conn.createStatement()
        var count = 0
        var isResultSet = stmt.execute("CALL `fetch_gateway`('$gatewayId')")
        while (true) {
            if (isResultSet) {
                when (++count) {
                    1 -> processGateway(stmt)
                    2 -> processTableNames(stmt)
                    else -> processTable(stmt)
                }
            } else if (stmt.updateCount == -1) {
                break
            }
            isResultSet = stmt.getMoreResults(Statement.CLOSE_CURRENT_RESULT)
        }
    }

    private fun processTableNames(stmt: Statement) {
        val names = mutableListOf<String>()
        stmt.resultSet.apply {
            while (next()) {
                names.add(getString(1))
            }
        }
        tables = names.iterator()
    }

    private fun processGateway(stmt: Statement) {
        result["gateway"] = stmt.resultSet.get()
    }

    private fun processTable(stmt: Statement) {
        result[tables!!.next()] = stmt.resultSet.asSequence().toObject()
    }

    private fun Sequence<JSONObject>.toObject() = JSONObject().also {
        for (obj in this) {
            it[obj["id"].toString()] = obj
        }
    }

    private fun ResultSet.get(): JSONObject? {
        if (!next()) {
            return null
        }

        val obj = JSONObject()
        for (i in 1..metaData.columnCount) {
            obj[metaData.getColumnName(i)] = getObject(i)
        }
        return obj
    }

    private fun ResultSet.asSequence() = generateSequence { get() }
}
