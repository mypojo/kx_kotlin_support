package net.kotlinx.validation.bean

import jakarta.validation.ConstraintValidatorContext

/** 결과 메세지를 강제 변경 */
fun ConstraintValidatorContext.replace(text: String) {
    this.disableDefaultConstraintViolation()
    this.buildConstraintViolationWithTemplate(text).addConstraintViolation()
}