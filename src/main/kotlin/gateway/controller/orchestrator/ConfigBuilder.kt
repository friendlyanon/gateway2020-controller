package gateway.controller.orchestrator

import gateway.controller.utils.JSONObject
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement

class ConfigBuilder(gatewayId: Int, conn: Connection) {
    val result = JSONObject()

    init {
        result["gateway"] = conn
            .run("SELECT * FROM ? WHERE id = ?", "gateways", gatewayId)
            .get()!!

        val stations = conn
            .run("SELECT * FROM ? WHERE gateway_id = ?", "stations", gatewayId)
            .toList()
        val stationIds = mutableListOf<Int>()
        val inputParameterIds = mutableListOf<Int>()
        result["stations"] = stations.toObject(
            { stationIds.add(it) },
            { inputParameterIds.add(get("input_parameter_id") as Int) }
        )

        result["input_parameters"] = conn
            .run("SELECT * FROM ? WHERE id IN (?)", "input_parameters", inputParameterIds)
            .toList()
            .toObject()

        TODO("Finish fetching everything")
    }

    private fun List<JSONObject>.toObject(
        vararg transformers: JSONObject.(Int) -> Unit
    ) = JSONObject().also {
        for (obj in this) {
            val id = obj["id"] as Int
            it[id.toString()] = obj
            for (transform in transformers) {
                it.transform(id)
            }
        }
    }

    private fun withCollections(
        sql: String,
        bindings: Array<out Any?>,
        prepare: (String) -> PreparedStatement
    ): Statement {
        var i = -1
        val replacedSql = sql.replace(Regex("\\?")) {
            when (val value = bindings[++i]) {
                is Collection<*> -> when (value.size) {
                    1 -> "?"
                    else -> StringBuffer((value.size - 1) * 3 + 1).apply {
                        append('?')
                        for (j in 2..value.size) {
                            append(", ?")
                        }
                    }.toString()
                }
                else -> "?"
            }
        }
        return run(prepare(replacedSql), sequence {
            for (value in bindings) {
                when (value) {
                    is Collection<*> -> yieldAll(value)
                    else -> yield(value)
                }
            }
        }.iterator())
    }

    private fun run(stmt: PreparedStatement, values: Iterator<*>): Statement {
        var i = 0
        for (value in values) {
            when (value) {
                is String -> stmt.setString(++i, value)
                is Int -> stmt.setInt(++i, value)
            }
        }
        return stmt.apply { execute() }
    }

    private fun Connection.run(sql: String, vararg values: Any?) = when {
        values.isEmpty() -> createStatement().apply { executeQuery(sql) }
        values.hasCollection() ->
            withCollections(sql, values) { prepareStatement(it) }
        else -> run(prepareStatement(sql), values.iterator())
    }

    private fun Statement.get(): JSONObject? {
        if (!resultSet.next()) {
            return null
        }

        val obj = JSONObject()
        val meta = resultSet.metaData
        for (i in 1..meta.columnCount) {
            obj[meta.getColumnName(i)] = resultSet.getObject(i)
        }
        return obj
    }

    private fun Statement.toList() = generateSequence { get() }.toList()

    private fun Array<out Any?>.hasCollection() = any { it is Collection<*> }
}
