package net.kotlinx.validation.bean

import jakarta.validation.Validation
import jakarta.validation.Validator
import net.kotlinx.string.CharSets
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.validation.beanvalidation.MessageSourceResourceBundleLocator


/**
 * 벨리데이션 결과를 매핑하는 컨버터 도구
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
}