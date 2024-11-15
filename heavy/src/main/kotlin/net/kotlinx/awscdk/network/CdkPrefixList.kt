package net.kotlinx.awscdk.network

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.ec2.CfnPrefixList
import software.amazon.awscdk.services.ec2.CfnPrefixList.EntryProperty
import software.amazon.awscdk.services.ec2.CfnPrefixListProps
import software.amazon.awscdk.services.ec2.IPeer
import software.amazon.awscdk.services.ec2.Peer

/** enum 정의 */
class CdkPrefixList : CdkInterface {

    @Kdsl
    constructor(block: CdkPrefixList.() -> Unit = {}) {
        apply(block)
    }

    /** 이름 */
    lateinit var name: String

    /** IP / 설명  으로 등록 */
    lateinit var prefixDatas: Map<String, String>

    /** 기본 최대 */
    val maxEntries: Int = 50

    override val logicalName: String
        get() = "${projectName}-prefix_${name}-${suff}"

    val feer: IPeer
        get() = Peer.prefixList(prefixList.attrPrefixListId)

    /** 결과 */
    lateinit var prefixList: CfnPrefixList

    /**
     * 프리픽스 리스트는 IP당 할당량을 사용함으로 range로 적용하려면 쿼터를 추가해야 한다.
     * 레인지 설정(ex 111.222.333.444/26)하면 자동으로 개별 등록됨 (maxEntries 각각 차지함) -> 따라거 최적화 필요하면 개별 등록할것
     * */
    fun create(stack: Stack): CdkPrefixList {
        prefixList = CfnPrefixList(
            stack, logicalName,
            CfnPrefixListProps.builder()
                .prefixListName(logicalName)
                .maxEntries(maxEntries) //SG rule의 디폴트 수 제한이 아마 60개 정도 됨
                .addressFamily("IPv4")
                .entries(prefixDatas.entries.map { EntryProperty.builder().cidr("${it.key}/32").description(it.value).build() })
                .build()
        )
        return this
    }
}