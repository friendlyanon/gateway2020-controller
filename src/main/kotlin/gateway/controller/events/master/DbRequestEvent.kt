package gateway.controller.events.master

import gateway.controller.database.Storage
import gateway.controller.events.Event
import java.util.concurrent.SynchronousQueue

class DbRequestEvent(
    val port: SynchronousQueue<Storage>,
    val type: Type
) : Event() {
    enum class Type { LOCAL, REMOTE }
}
