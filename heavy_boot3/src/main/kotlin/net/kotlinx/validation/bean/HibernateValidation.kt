package net.kotlinx.validation.bean

import jakarta.validation.Validation
import jakarta.validation.Validator
import net.kotlinx.string.CharSets
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.validation.beanvalidation.MessageSourceResourceBundleLocator


/**
 * 벨리데이션 결과를 매핑하는 컨버터 도구
 *
 * 주의!!!    스프링 기본 등록 & 컨트롤러에서 @Valid -> 이거 사용하지 않음!!
 * 워낙 변수가 많고 중첩 체크해야하는 경우도 있음으로 직접 예외를 던지고 catch 하는 방식을 사용한다
 */
object HibernateValidation {

    /**
     * 기본 벨리데이터
     * 국제화 규격 기본 인코딩이 UTF-8 이 아니기 때문에 강제 조정해준다.
     * 인코딩이 자동 변경된다면 IDE 의 editor -> file encoding -> 프로퍼티 속성을 UTF_8로 변경해주자
     *  */
    @Suppress("UsePropertyAccessSyntax")
    private val VALIDATOR: Validator by lazy {
        Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(
                ResourceBundleMessageInterpolator(
                    MessageSourceResourceBundleLocator(
                        ResourceBundleMessageSource().apply {
                            setBasename("ValidationMessages")
                            setDefaultEncoding(CharSets.UTF_8.name())
                        }
                    )
                )
            )
            .buildValidatorFactory()
            .validator
    }


    /** 벨리데이션 실행 후 결과를 감싸서 리턴함 */
    fun validate(obj: Any): List<ValidationResult> = VALIDATOR.validate(obj).map { HibernateValidationResult(it).toValidationResult() }

    /** 강제로 벨리데이터를 가져올때 사용 */
    fun getValidatorInner(): Validator = VALIDATOR
}