package gateway.controller

import gateway.controller.storage.Storage

fun main() {
    Master(Storage("jdbc:h2:~/test", "controller", "secret")).run()
}
