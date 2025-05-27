package com.peknight.socks5.api

import com.peknight.socks5.auth.Method

trait Socks5Api[F[_]]:
  def negotiation(methods: List[Method]): F[Method]
end Socks5Api
