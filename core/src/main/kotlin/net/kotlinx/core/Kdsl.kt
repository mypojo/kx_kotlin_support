package net.kotlinx.core

/**
 * this를 생략할 수 없게 해줌 -> 1뎁스에서만 DSL 사용 가능
 * 이거 하나로 대충 다 DSL 마킹할것 (ktor 보니 이렇게 하네)
 * IDE에서 다른색으로 표현도 해준다~
 *
 * 주의! 클래스에 붙여야 작동함. 생성자에 있는거 필요시 클래스로 옮기기
 * */
@DslMarker
annotation class Kdsl