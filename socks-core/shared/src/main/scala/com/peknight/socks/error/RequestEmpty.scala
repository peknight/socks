package com.peknight.socks.error

object RequestEmpty extends StreamEmpty:
  override def lowPriorityMessage: Option[String] = Some("request empty")
end RequestEmpty
