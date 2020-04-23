package gateway.controller.orchestrator

import java.nio.file.Path

interface ScriptStarter {
    val script: String

    fun start(directory: String): Int

    fun hasScript(path: Path): Boolean {
        return path.resolve(script).toFile().isFile
    }
}
