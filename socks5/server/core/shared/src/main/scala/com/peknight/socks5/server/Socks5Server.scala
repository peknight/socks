package com.peknight.socks5.server

import cats.Show
import cats.data.EitherT
import cats.effect.{IO, IOApp}
import cats.syntax.either.*
import cats.syntax.option.*
import com.peknight.error.Error
import com.peknight.error.syntax.either.value
import com.peknight.socks.SocksVersion.socks5
import com.peknight.socks.error.{RequestEmpty, UnsupportedSocksVersion}
import com.peknight.socks5.auth.Method
import com.peknight.socks5.auth.Method.*
import com.peknight.socks5.auth.password.PasswordVersion.version1
import com.peknight.socks5.auth.password.Status.{Failure, Success}
import com.peknight.socks5.auth.password.{Status, UsernamePassword as UPassword}
import com.peknight.socks5.error.*
import fs2.{Chunk, Pull, Stream}
import scodec.bits.ByteVector

import java.nio.charset.Charset

object Socks5Server extends IOApp.Simple:
  val negotiationStream: Stream[IO, Byte] = Stream(0x05, 0x01, 0x00)

  private def readVersion[F[_]](input: Stream[F, Byte])(f: Byte => Option[Error])
  : Pull[F, Nothing, Either[Error, Stream[F, Byte]]] =
    input.pull.uncons1.map {
      case Some(version, tail) => f(version) match
        case Some(error) => error.asLeft
        case None => tail.asRight
      case None => RequestEmpty.asLeft
    }

  private def readSizedChunk[F[_]](input: Stream[F, Byte], onEmpty: => Error)
  : Pull[F, Nothing, Either[Error, (Chunk[Byte], Stream[F, Byte])]] =
    input.pull.uncons1.flatMap {
      case Some(n, tail) => tail.pull.unconsLimit(n).map {
        case Some(tuple) => tuple.asRight
        case None => onEmpty.asLeft
      }
      case None => Pull.pure(onEmpty.asLeft)
    }

  private def readSizedString[F[_]](input: Stream[F, Byte], onEmpty: => Error)
                                   (using charset: Charset)
  : Pull[F, Nothing, Either[Error, (String, Stream[F, Byte])]] =
    readSizedChunk[F](input, onEmpty).map(_.flatMap { (chunk, stream) =>
      val bytes = chunk.toByteVector
      given Show[ByteVector] = Show.show(_.toHex)
      bytes.decodeString.map((_, stream)).value(bytes)
    })

  private def negotiation[F[_]](input: Stream[F, Byte])(f: List[Method] => F[Method])
  : Pull[F, Stream[F, Byte], Either[Error, (AcceptableMethod, Stream[F, Byte])]] =
    val eitherT =
      for
        stream <- EitherT(readVersion[F](input) { version =>
          if version == socks5.code then none else UnsupportedSocksVersion(version).some
        })
        (chunk, stream) <- EitherT(readSizedChunk[F](stream, MethodEmpty))
        methods = chunk.map(Method.apply).toList
        (selected, stream) <- EitherT(Pull.eval(f(methods)).map {
          case NoAcceptableMethod => NoAcceptableMethod.asLeft
          case selected: AcceptableMethod => (selected, stream).asRight
        })
      yield
        (selected, stream)
    eitherT.value.flatMap {
      case r @ Right((selected, stream)) => Pull.output1(Stream(socks5.code, selected.code)) >> Pull.pure(r)
      case l @ Left(error) => Pull.output1(Stream(socks5.code, NoAcceptableMethod.code)) >> Pull.pure(l)
    }

  private def passwordAuth[F[_]](input: Stream[F, Byte])(f: UPassword => F[Status])
                                (using charset: Charset): Pull[F, Stream[F, Byte], Either[Error, Stream[F, Byte]]] =
    val eitherT =
      for
        stream <- EitherT(readVersion[F](input) { version =>
          if version == version1.code then none
          else UnsupportedPasswordVersion(version).some
        })
        (username, stream) <- EitherT(readSizedString[F](stream, UsernameEmpty))
        (password, stream) <- EitherT(readSizedString[F](stream, PasswordEmpty))
        _ <- EitherT(Pull.eval(f(UPassword(username, password))).map {
          case Success => stream.asRight
          case f @ Failure(code) => f.asLeft
        })
      yield
        stream
    eitherT.value.flatMap {
      case r @ Right(stream) => Pull.output1(Stream(version1.code, Success.code)) >> Pull.pure(r)
      case l @ Left(Failure(code)) => Pull.output1(Stream(version1.code, code)) >> Pull.pure(l)
      case l @ Left(error) => Pull.output1(Stream(version1.code, Failure.code)) >> Pull.pure(l)
    }


  private def authentication[F[_]](authMethod: AcceptableMethod, input: Stream[F, Byte])
                                  (password: UPassword => F[Status])
                                  (using charset: Charset)
  : Pull[F, Stream[F, Byte], Either[Error, Stream[F, Byte]]] =
    authMethod match
      case NoAuthenticationRequired => Pull.pure(input.asRight)
      case GSSAPI => ???
      case UsernamePassword => passwordAuth(input)(password)
      case method @ IANAAssigned(code) => ???
      case method @ PrivateMethod(code) => ???

  val run: IO[Unit] =
    for
      _ <- IO.println(ByteVector.fromHex("7777772e676f6f676c652e636f6d").map(_.decodeUtf8))
    yield
      ()
end Socks5Server
