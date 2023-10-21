package net.kotlinx.hibernate.config

import com.google.common.base.CaseFormat
import mu.KotlinLogging
import org.hibernate.boot.model.naming.Identifier
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment

class PhysicalNamingStrategyUnderscore : PhysicalNamingStrategyStandardImpl() {

    private val log = KotlinLogging.logger {}

    override fun toPhysicalColumnName(name: Identifier, context: JdbcEnvironment): Identifier {
        val toColumnName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name.text)
        log.trace { " -> 컬럼명 자동변경 ${name.text} -> $toColumnName" }
        return Identifier.toIdentifier(toColumnName)
    }
}
