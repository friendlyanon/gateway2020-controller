package gateway.controller.orchestrator

import gateway.controller.utils.getLogger
import java.io.File

class ModuleManager {
    private val modules: List<String>
    private val scriptStarter: ScriptStarter

    init {
        val isWindows = System.getProperty("os.name").startsWith("Windows")
        scriptStarter = if (isWindows) WindowsStarter() else UnixStarter()

        modules = File("modules").listFiles()
            ?.filter { scriptStarter.hasScript(it.toPath()) }
            ?.map { it.absolutePath }
            ?: emptyList()

        if (modules.isEmpty()) {
            LOG.info("No modules were found")
        }
    }

    fun start(configJson: String) {
        TODO("Not yet implemented")
    }

    companion object {
        private val LOG = getLogger<ModuleManager>()
    }
}
