package net.kotlinx.domain.job

/**
 * 잡 안에서 발생한 예외를 한번 감싸준다.
 * 잡 예외시 이미 콜백 예외를 전달하기때문에 두번 처리를 하지 않기 위한 목적으로도 사용됨
 * */
class JobException(parent: Throwable) : RuntimeException(parent)