package gateway.controller.workers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import gateway.controller.database.DbWrapper
import gateway.controller.database.DbWrapper.Table
import gateway.controller.database.SqlDatabase
import gateway.controller.database.Storage
import gateway.controller.events.master.DbRequestEvent
import gateway.controller.events.master.DbRequestEvent.Type
import gateway.controller.events.master.RestartEvent
import gateway.controller.orchestrator.ConfigFetcher
import gateway.controller.orchestrator.ModuleManager
import gateway.controller.utils.Queue
import java.sql.Connection
import java.util.concurrent.SynchronousQueue

// TODO
//  when modules request key-value data to be stored, the actual stored key
//  should follow the pattern below:
//    <module_folder>/<key>
//  Assuming <module_folder> is "reader" and <key> is "lastId", when reading
//  them out should look like this:
//    val readerLastId: String? = localStorage.readOnlyUse(STORE) {
//        getOrDefault("reader/lastId", null)
//    }
class Orchestrator(queue: Queue) : AbstractWorker(queue) {
    override fun run() {
        try {
            val manager = ModuleManager()
            val json = getStorage<SqlDatabase>(Type.REMOTE).useConnection {
                makeConfigJson(it)
            }
            manager.start(json)
        } catch (e: Throwable) {
            put(RestartEvent(e))
        }
    }

    private fun makeConfigJson(conn: Connection): String {
        val gatewayId = getStorage<DbWrapper>(Type.LOCAL)
            .readOnlyUse(Table.DETAILS) { get("gatewayId")!!.toInt() }
        val config = ConfigFetcher(gatewayId, conn)
        return jacksonObjectMapper().writeValueAsString(config.fetch())
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Storage> getStorage(type: Type) =
        SynchronousQueue<Storage>().run {
            put(DbRequestEvent(this, type))
            take() as T
        }
}
