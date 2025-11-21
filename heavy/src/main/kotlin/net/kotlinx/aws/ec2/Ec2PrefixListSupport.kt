package net.kotlinx.aws.ec2

import aws.sdk.kotlin.services.ec2.Ec2Client
import aws.sdk.kotlin.services.ec2.describeManagedPrefixLists
import aws.sdk.kotlin.services.ec2.model.AddPrefixListEntry
import aws.sdk.kotlin.services.ec2.model.PrefixListEntry
import aws.sdk.kotlin.services.ec2.model.RemovePrefixListEntry
import aws.sdk.kotlin.services.ec2.modifyManagedPrefixList
import aws.sdk.kotlin.services.ec2.paginators.getManagedPrefixListEntriesPaginated
import kotlinx.coroutines.flow.*

/** 최신 Prefix List 버전 조회 */
suspend fun Ec2Client.getPrefixListVersion(prefixListId: String): Long {
    val resp = this.describeManagedPrefixLists {
        this.prefixListIds = listOf(prefixListId)
    }
    return resp.prefixLists?.firstOrNull()?.version ?: error("PrefixList $prefixListId 의 버전을 조회할 수 없습니다")
}

/**
 * Prefix List 내에 특정 CIDR 존재 여부
 * SDK의 Paginated Flow 사용 (전체 엔트리 조회가 필요함)
 */
suspend fun Ec2Client.prefixListContains(prefixListId: String, cidr: String): Boolean {
    val entry = getManagedPrefixListEntriesPaginated {
        this.prefixListId = prefixListId
    }.flatMapConcat { page ->
        page.entries.orEmpty().asFlow()
    }.map { it.cidr }.firstOrNull { it == cidr }
    return entry != null
}

/**
 * Prefix List 에 등록된 모든 CIDR 을 Set 으로 반환
 * - SDK의 Paginated Flow 를 사용하여 전체 엔트리를 조회
 */
suspend fun Ec2Client.getPrefixListCidrs(prefixListId: String): List<PrefixListEntry> =
    getManagedPrefixListEntriesPaginated { this.prefixListId = prefixListId }
        .flatMapConcat { page -> page.entries.orEmpty().asFlow() }
        .toList()

/**
 * 입력으로 원하는 CIDR 전체를 받아 Prefix List 를 동기화 (버전 무시)
 * - 기존에 없으면 추가
 * - 기존에 있는데 입력에 없으면 제거
 * 경고!! 보통 SG당 60개 제한임. SG는 5개까지 달수있음으로 일반적으로는 최대 300개 까지 가능함 (비현실적임!)
 * 경고!! 다수의 시스템이 있는경우 각각 업데이트 해도 되고, share를 해서 써도 된다.  -> 관리는 각각 업데이트 하는게 더 편한듯
 */
suspend fun Ec2Client.updateIpToPrefixList(prefixListId: String, cidrs: List<Pair<String, String>>) {

    // 현재 등록된 CIDR 전체 조회
    val existingCidrs: List<String> = getPrefixListCidrs(prefixListId).map { it.cidr!! }

    // 원하는 최종 상태
    val desiredMap: Map<String, String> = cidrs.associate { it.first to it.second }
    val desiredCidrs: Set<String> = desiredMap.keys

    // 추가/제거 대상 계산
    val addEntries = desiredCidrs
        .asSequence()
        .filter { it !in existingCidrs }
        .map { cidr ->
            AddPrefixListEntry {
                this.cidr = cidr
                this.description = desiredMap[cidr]
            }
        }
        .toList()

    val removeEntries = existingCidrs
        .asSequence()
        .filter { it !in desiredCidrs }
        .map { cidr ->
            RemovePrefixListEntry {
                this.cidr = cidr
            }
        }
        .toList()

    if (addEntries.isEmpty() && removeEntries.isEmpty()) return

    val prefixListVersion = getPrefixListVersion(prefixListId) //update 시에는 버전 필수
    modifyManagedPrefixList {
        this.prefixListId = prefixListId
        this.currentVersion = prefixListVersion
        if (addEntries.isNotEmpty()) this.addEntries = addEntries
        if (removeEntries.isNotEmpty()) this.removeEntries = removeEntries
    }
}


/**
 * AWS Managed Prefix List 에 CIDR 추가
 * - 이미 존재하면 아무 작업도 하지 않고 로그만 남김
 * @param cidr 예: "1.2.3.4/32"
 */
suspend fun Ec2Client.addIpToPrefixList(prefixListId: String, cidrs: List<Pair<String, String>>) {
    if (cidrs.isEmpty()) return

    // 현재 등록된 CIDR 전체 조회 후, 중복되는 항목은 제외
    val existingCidrs: List<String> = getPrefixListCidrs(prefixListId).map { it.cidr!! }

    val addEntries = cidrs
        .asSequence()
        .filter { (cidr, _) -> cidr !in existingCidrs }
        .map { (cidr, description) ->
            AddPrefixListEntry {
                this.cidr = cidr
                this.description = description
            }
        }
        .toList()

    if (addEntries.isEmpty()) return

    val currentVersion = getPrefixListVersion(prefixListId)
    this.modifyManagedPrefixList {
        this.prefixListId = prefixListId
        this.currentVersion = currentVersion
        this.addEntries = addEntries
    }
}


/**
 * AWS Managed Prefix List 에서 CIDR 제거
 * - 존재하지 않으면 아무 작업도 하지 않고 로그만 남김
 */
suspend fun Ec2Client.removeIpFromPrefixList(prefixListId: String, cidrs: List<Pair<String, String>>) {
    if (cidrs.isEmpty()) return

    val targetCidrs = cidrs.map { it.first }

    // 현재 등록된 CIDR 중 제거 대상만 선별
    val existingCidrs: List<String> = getPrefixListCidrs(prefixListId).map { it.cidr!! }

    val removeEntries = targetCidrs
        .asSequence()
        .filter { it in existingCidrs }
        .map { cidr ->
            RemovePrefixListEntry {
                this.cidr = cidr
            }
        }
        .toList()

    if (removeEntries.isEmpty()) return

    val currentVersion = getPrefixListVersion(prefixListId)
    modifyManagedPrefixList {
        this.prefixListId = prefixListId
        this.currentVersion = currentVersion
        this.removeEntries = removeEntries
    }
}

