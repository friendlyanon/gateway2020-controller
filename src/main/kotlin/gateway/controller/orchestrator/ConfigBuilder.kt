package gateway.controller.orchestrator

import gateway.controller.utils.JSONObject
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class ConfigBuilder(gatewayId: Int, conn: Connection) {
    val result = JSONObject()
    private val tables = arrayOf(
        "stations",
        "input_parameters",
        "sensors",
        "validations",
        "descriptors",
        "groups",
        "sensor_types",
        "units"
    ).iterator()

    init {
        val stmt = conn.prepareStatement("CALL `fetch_gateway`(?)")
        stmt.setInt(1, gatewayId)

        var first = true
        var isResultSet = stmt.execute()
        while (true) {
            if (isResultSet) {
                if (first) {
                    processGateway(stmt)
                    first = false
                } else {
                    processTable(stmt)
                }
            } else if (stmt.updateCount == -1) {
                break
            }
            isResultSet = stmt.getMoreResults(Statement.CLOSE_CURRENT_RESULT)
        }
    }

    private fun processGateway(stmt: Statement) {
        result["gateway"] = stmt.resultSet.get()!!
    }

    private fun processTable(stmt: Statement) {
        result[tables.next()] = stmt.resultSet.asSequence().toObject()
    }

    private fun Sequence<JSONObject>.toObject() = JSONObject().also {
        for (obj in this) {
            it[(obj["id"] as Int).toString()] = obj
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
