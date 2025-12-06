package net.kotlinx.aws.cost

import aws.sdk.kotlin.services.costexplorer.CostExplorerClient
import aws.sdk.kotlin.services.costexplorer.getCostAndUsage
import aws.sdk.kotlin.services.costexplorer.model.DateInterval
import aws.sdk.kotlin.services.costexplorer.model.Granularity
import aws.sdk.kotlin.services.costexplorer.model.GroupDefinition
import aws.sdk.kotlin.services.costexplorer.model.GroupDefinitionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.kotlinx.string.replaceAll
import net.kotlinx.time.toYmdF01
import java.time.LocalDate

/**
 * 일 단위로 Service, Tag(project) 기준 그룹핑하여 비용 조회 (페이징 처리)
 * SDK의 페이징 토큰을 사용하여 Flow로 페이지 단위 결과(List)를 순차적으로 반환
 * time(YYYYMMDD), tag(프로젝트 태그 값), key(서비스명), cost(달러)
 */
suspend fun CostExplorerClient.costByTagService(
    /** 태그 키 (Cost Allocation 태그로 활성화되어 있어야 함) */
    projectTagKey: String = "ProjectName",
    /** 오늘 기준 1년 전 1일 */
    start: LocalDate = LocalDate.now().minusYears(1).withDayOfMonth(1),
    /** 오늘 */
    end: LocalDate = LocalDate.now(),
    costType: String = CostExplorerUtil.AMORTIZED_COST,
): Flow<List<CostExplorerLine2>> {

    // Tag 그룹 정의 (두 번째 그룹)
    val tagGroup = GroupDefinition {
        key = projectTagKey
        type = GroupDefinitionType.Tag
    }

    return flow {
        var token: String? = null
        do {
            val resp = this@costByTagService.getCostAndUsage {
                granularity = Granularity.Daily
                timePeriod = DateInterval {
                    this.start = start.toYmdF01()
                    this.end = end.toYmdF01()
                }
                metrics = listOf(costType)
                groupBy = listOf(CostExplorerUtil.BY_SERVICE, tagGroup)
                nextPageToken = token
            }

            val pageLines: List<CostExplorerLine2> = resp.resultsByTime.orEmpty().flatMap { result ->
                val startDate = result.timePeriod?.start!! // 2024-02-25 이런식으로 그대로 넣음 (아이스버그 data 타입)
                result.groups.orEmpty().mapNotNull { g ->
                    val keys = g.keys ?: return@mapNotNull null
                    if (keys.size != 2) return@mapNotNull null
                    val serviceRaw = keys[0]
                    val tagRaw = keys[1]
                    val service = serviceRaw.replaceFirst("Amazon ", "").replaceAll(CostExplorerUtil.REPLACER)
                    val tag = tagRaw.substringAfter('$', tagRaw) // 'project$myproj' 형식이면 값만 추출
                    val amount = g.metrics?.get(costType)?.amount?.toDoubleOrNull() ?: return@mapNotNull null
                    CostExplorerLine2(
                        time = startDate,
                        tag = tag,
                        key = service,
                        cost = amount,
                    )
                }
            }

            emit(pageLines)

            token = resp.nextPageToken
        } while (!token.isNullOrEmpty())
    }
}
