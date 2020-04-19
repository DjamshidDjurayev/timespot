package service.error

final case class NotAuthorizedThrowable(private val message: String = "") extends Throwable(message)
