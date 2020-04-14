package gateway.controller.orchestrator

import gateway.controller.utils.JSONObject
import java.sql.Connection
import java.sql.Statement

// TODO look into implementing this in a database procedure
class ConfigBuilder(gatewayId: Int, conn: Connection) {
    val result = JSONObject()

    init {
        result["gateway"] = conn.where("gateways", "id", gatewayId).get()!!

        val stations = conn.where("stations", "gateway_id", gatewayId).toList()
        val stationIds = ids()
        val inputParameterIds = ids()
        result["stations"] = stations.toObject(
            { stationIds.add(it) },
            { inputParameterIds.add(get("input_parameter_id") as Int) }
        )

        result["input_parameters"] = conn
            .where("input_parameters", "id", inputParameterIds)
            .toList()
            .toObject()

        val sensors = conn.where("sensors", "station_id", stationIds).toList()
        val descriptorIds = ids()
        val sensorTypeIds = ids()
        val validationIds = ids()
        result["sensors"] = sensors.toObject(
            { descriptorIds.add(get("descriptor_id") as Int) },
            { sensorTypeIds.add(get("sensor_type_ids") as Int) },
            { get("validation_id")?.let { validationIds.add(it as Int) } }
        )

        result["validations"] = when {
            validationIds.isEmpty() -> JSONObject()
            else -> conn
                .where("validations", "id", validationIds)
                .toList()
                .toObject()
        }

        val descriptors = conn
            .where("descriptors", "id", descriptorIds)
            .toList()
        val groupIds = ids()
        val unitSet = mutableSetOf<Int>()
        result["descriptors"] = descriptors.toObject(
            { get("group_id")?.let { groupIds.add(it as Int) } },
            { unitSet.add(get("unit_id") as Int) }
        )

        result["groups"] = when {
            groupIds.isEmpty() -> JSONObject()
            else -> conn.where("groups", "id", groupIds).toList().toObject()
        }

        result["sensor_types"] = conn
            .where("sensor_types", "id", sensorTypeIds)
            .toList()
            .toObject({ unitSet.add(get("unit_id") as Int) })

        result["units"] = conn
            .where("units", "id", unitSet.toList())
            .toList()
            .toObject()
    }

    private fun ids() = mutableListOf<Int>()

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

    private fun Connection.where(tbl: String, column: String, id: Int) =
        run("SELECT * FROM $tbl WHERE $column = ?", listOf(id))

    private fun Connection.where(tbl: String, column: String, ids: List<Int>) =
        run("SELECT * FROM $tbl WHERE $column IN ".appendArray(ids), ids)

    private fun String.appendArray(list: List<Int>) = this + when (list.size) {
        0 -> throw IllegalArgumentException("Empty list")
        1 -> "(?)"
        else -> StringBuffer(list.size * 3).apply {
            append("(?")
            for (j in 2..list.size) {
                append(", ?")
            }
            append(')')
        }.toString()
    }

    private fun Connection.run(sql: String, values: List<Int>): Statement {
        val stmt = prepareStatement(sql)
        values.forEachIndexed { i, v -> stmt.setInt(i + 1, v) }
        return stmt.apply { execute() }
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
}
