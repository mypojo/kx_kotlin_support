package net.kotlinx.kopring.spring

import org.springframework.core.io.FileSystemResource
import java.io.File

fun File.toResource(): FileSystemResource = FileSystemResource(this)