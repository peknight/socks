package com.peknight.socks.error

object Socks5VersionEmpty extends StreamEmpty:
  override def lowPriorityMessage: Option[String] = Some("socks5 version empty")
end Socks5VersionEmpty
