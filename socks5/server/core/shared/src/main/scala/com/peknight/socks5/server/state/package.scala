package com.peknight.socks5.server

import cats.data.StateT
import com.peknight.error.Error
import fs2.{Pull, Stream}

package object state:
  type State[F[_], O, A] = StateT[[X] =>> Pull[F, O, Either[Error, X]], Stream[F, Byte], A]
end state
