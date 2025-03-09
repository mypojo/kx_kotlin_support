package net.kotlinx.aws.glue

import aws.sdk.kotlin.services.glue.*
import aws.sdk.kotlin.services.glue.model.*
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.awsConfig
import net.kotlinx.aws.regist

val AwsClient.glue: GlueClient
    get() = getOrCreateClient { GlueClient { awsConfig.build(this) }.regist(awsConfig) }

/** 일반 데이터베이스 생성 */
suspend fun GlueClient.createDatabase(databaseName: String, desc: String? = null) {
    this.createDatabase {
        this.databaseInput {
            this.name = databaseName
            this.description = desc
        }
    }
}

/**
 * 데이터베이스 링크 생성
 * 다른데서 내 계정으로 쉐어(RAM)한 데이터베이스를 내가 athena 등에서 읽을수 있게 DB링크로 만들어줄때 사용
 * 이렇게 링크 생성하면, athena 에서 database 로 보임
 * @param fromdatabaseName to_${projectName}
 *  */
suspend fun GlueClient.createDatabaseLink(databaseName: String, fromAwsId: String, fromdatabaseName: String) {
    this.createDatabase {
        this.databaseInput {
            this.name = databaseName
            //링크에는 desc를 넣을 수 없음
            this.targetDatabase {
                this.catalogId = fromAwsId
                this.databaseName = fromdatabaseName
            }
        }
    }
}

/** 테이블 정보 조회 */
suspend fun GlueClient.getTable(databaseName: String, tableName: String): Table {
    return this.getTable {
        this.databaseName = databaseName
        this.name = tableName
    }.table!!
}

/**
 * 최적화 옵션을 설정함
 * role 필요
 * #1 Trust relationships = glue
 * #2 기본적인 s3, glue 등의 권한 필요
 * #3 lake formation 에서도 역할 필요 (UI에서 Grant required permissions 체크기 자동 등록됨 -> 이렇게 사용하지 말고 적절한거 태그화해서 미리 세팅할것)
 *
 * */
suspend fun GlueClient.createOrUpdateTableOptimizerByDefault(databaseName: String, tableName: String, roleName: String) {
    val log = KotlinLogging.logger {}
    val config = this.awsConfig
    /** 압축설정 */
    try {
        this.createTableOptimizer {
            this.catalogId = config.awsId
            this.databaseName = databaseName
            this.tableName = tableName
            this.type = TableOptimizerType.Compaction
            tableOptimizerConfiguration = TableOptimizerConfiguration {
                enabled = true
                roleArn = config.arn("iam", "role/$roleName")
            }
        }
        log.info { " => [${databaseName}.${tableName}] Compaction 생성완료" }
    } catch (e: AlreadyExistsException) {
        this.updateTableOptimizer {
            this.catalogId = config.awsId
            this.databaseName = databaseName
            this.tableName = tableName
            this.type = TableOptimizerType.Compaction
            tableOptimizerConfiguration = TableOptimizerConfiguration {
                enabled = true
                roleArn = config.arn("iam", "role/$roleName")
            }
        }
        log.info { " => [${databaseName}.${tableName}] Compaction 수정완료" }
    }
    /** 클린설정 */
    try {
        this.createTableOptimizer {
            this.catalogId = config.awsId
            this.databaseName = databaseName
            this.tableName = tableName
            this.type = TableOptimizerType.Retention
            tableOptimizerConfiguration = TableOptimizerConfiguration {
                enabled = true
                roleArn = config.arn("iam", "role/$roleName")
                retentionConfiguration = RetentionConfiguration {
                    icebergConfiguration = IcebergRetentionConfiguration {
                        cleanExpiredFiles = true
                    }
                }

            }
        }
        log.info { " => [${databaseName}.${tableName}] Retention 생성완료" }
    } catch (e: AlreadyExistsException) {
        this.updateTableOptimizer {
            this.catalogId = config.awsId
            this.databaseName = databaseName
            this.tableName = tableName
            this.type = TableOptimizerType.Retention
            tableOptimizerConfiguration = TableOptimizerConfiguration {
                enabled = true
                roleArn = config.arn("iam", "role/$roleName")
                retentionConfiguration = RetentionConfiguration {
                    icebergConfiguration = IcebergRetentionConfiguration {
                        cleanExpiredFiles = true
                    }
                }

            }
        }
        log.info { " => [${databaseName}.${tableName}] Retention 수정완료" }
    }

    /** 고아객체 삭제 */
    val table = this.getTable(databaseName, tableName)
    try {
        this.createTableOptimizer {
            this.catalogId = config.awsId
            this.databaseName = databaseName
            this.tableName = tableName
            this.type = TableOptimizerType.OrphanFileDeletion
            tableOptimizerConfiguration = TableOptimizerConfiguration {
                enabled = true
                roleArn = config.arn("iam", "role/$roleName")
                orphanFileDeletionConfiguration = OrphanFileDeletionConfiguration {
                    icebergConfiguration = IcebergOrphanFileDeletionConfiguration {
                        location = table.storageDescriptor!!.location
                    }
                }
            }
        }
        log.info { " => [${databaseName}.${tableName}] OrphanFileDeletion 생성완료" }
    } catch (e: AlreadyExistsException) {
        this.updateTableOptimizer {
            this.catalogId = config.awsId
            this.databaseName = databaseName
            this.tableName = tableName
            this.type = TableOptimizerType.OrphanFileDeletion
            tableOptimizerConfiguration = TableOptimizerConfiguration {
                enabled = true
                roleArn = config.arn("iam", "role/$roleName")
                orphanFileDeletionConfiguration = OrphanFileDeletionConfiguration {
                    icebergConfiguration = IcebergOrphanFileDeletionConfiguration {
                        location = table.storageDescriptor!!.location
                    }
                }
            }
        }
        log.info { " => [${databaseName}.${tableName}] OrphanFileDeletion 수정완료" }
    }

}