@file:Suppress("UnnecessaryVariable")
@file:OptIn(KspExperimental::class)

package net.kotlinx.ksp.generateInterface

import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * 어노테이션으로 인터페이스를 만들어주는 프로세서.
 * 상세 내용은 필요할때 다시 확인
 * from kotlin advance
 * */
@OptIn(KspExperimental::class)
class GenerateInterfaceProcessor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        //어노테이션 붙은거 찾아서 처리
        resolver.getSymbolsWithAnnotation(GenerateInterface::class.qualifiedName.toString())
            .filterIsInstance<KSClassDeclaration>()
            .also { logger.warn("===== 인터페이스 자동생성 -> ${it.joinToString { it.simpleName.getShortName() }} =====") }
            .forEach(::generateInterface)
        return emptyList()
    }

    private fun generateInterface(annotatedClass: KSClassDeclaration) {
        val interfaceName = annotatedClass.getAnnotationsByType(GenerateInterface::class).single().name
        val interfacePackage = annotatedClass.qualifiedName?.getQualifier().orEmpty()
        if (interfaceName.isBlank()) {
            throw Error("Interface name cannot be empty")
        }

        logger.info("== 인터페이스 자동생성 ${interfaceName} ==")
        val fileSpec = FileSpec
            .builder(interfacePackage, interfaceName)
            .addType(
                TypeSpec
                    .interfaceBuilder(interfaceName)
                    .addFunctions(
                        //퍼블릭 메소드만 해당됨
                        annotatedClass.getDeclaredFunctions().filter { it.isPublic() && !it.isConstructor() }.map { function ->
                            FunSpec
                                .builder(function.simpleName.getShortName())
                                .addModifiers(function.modifiers.filterNot { it in IGNORED_MODIFIERS }.plus(Modifier.ABSTRACT).mapNotNull { it.toKModifier() })
                                .addParameters(
                                    function.parameters
                                        .map { variableElement ->
                                            ParameterSpec
                                                .builder(variableElement.name!!.getShortName(), variableElement.type.toTypeName())
                                                .addAnnotations(variableElement.annotations.map { it.toAnnotationSpec() }.toList())
                                                .build()
                                        }
                                )
                                .returns(function.returnType!!.toTypeName())
                                //어노테이션도 그대로
                                .addAnnotations(function.annotations.map { it.toAnnotationSpec() }.toList())
                                .build()
                        }.toList()
                    )
                    .build()
            )
            .build()

        val dependencies = Dependencies(aggregating = false, annotatedClass.containingFile!!)
        // Inlined fileSpec.writeTo(codeGenerator, dependencies)
        val file = codeGenerator.createNewFile(dependencies, fileSpec.packageName, fileSpec.name)
        OutputStreamWriter(file, StandardCharsets.UTF_8).use(fileSpec::writeTo)
    }

    companion object {
        val IGNORED_MODIFIERS = listOf(Modifier.OPEN, Modifier.OVERRIDE, Modifier.PUBLIC)
    }


}