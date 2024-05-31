package net.kotlinx.validation


/**
 * 하나의 성공메세지 or 다수의 실패 메세지가 존재할 수 있는 벨리데이터 정의 (공용)
 * 예외발생시 리스트에 추가. -> 리스트에 하나라도 추가되면 에러로 간주. 반대로 비어있으면 성공으로 간주
 * 리턴은 성공 메세지
 * */
typealias Validator = suspend (MutableList<String>) -> String