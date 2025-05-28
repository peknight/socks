package com.peknight.socks.error

object PasswordVersionEmpty extends StreamEmpty:
  override def lowPriorityMessage: Option[String] = Some("password version empty")
end PasswordVersionEmpty
