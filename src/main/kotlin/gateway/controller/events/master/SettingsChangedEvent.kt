package gateway.controller.events.master

import gateway.controller.events.Event
import gateway.controller.utils.SettingsList

class SettingsChangedEvent(val settings: SettingsList) : Event()
