package service.error

final case class CommonErrorThrowable(private val message: String = "") extends Throwable(message)
