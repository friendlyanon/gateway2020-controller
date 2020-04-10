package gateway.controller.events.master

import gateway.controller.events.Event
import gateway.controller.utils.Queue

class InquireStatusEvent(val port: Queue) : Event()
