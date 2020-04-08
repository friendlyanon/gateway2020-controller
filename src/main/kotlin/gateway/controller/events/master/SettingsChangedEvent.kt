package gateway.controller.events.master

import gateway.controller.events.Event

// TODO figure out a datastructure for the settings
class SettingsChangedEvent(val settings: Any) : Event()
