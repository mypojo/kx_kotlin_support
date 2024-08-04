package net.kotlinx.validation.bean

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import net.kotlinx.core.Comment
import net.kotlinx.validation.bean.anno.*
import org.hibernate.validator.constraints.Length

class ValidationDemoString {

    //==================================================== 널체크 ======================================================
    @Comment("이름")
    @NotEmpty
    @Length(max = 3, message = "#길이가 너무 길어요")
    var name: String? = null

    @Comment("이름2")
    @NotEmpty(message = "이름을 입력해주세요")
    var name2: String? = null

    /**
     * 슷지만 남겨진 후 4~8인지 벨리데이션 체크된다.
     */
    @Comment("그룹명")
    @Length(min = 4, max = 8)
    var groupName: String? = null

    @Comment("사용자 타입")
    @Pattern(regexp = "NEW|DEFAULT") //기본 메세지로 사용. 이하 2개중 하나만 입력되어야한다.
    var userType: String? = null

    @Comment("사용자 타입 명")
    @Pattern(regexp = "\\d*", message = "숫자만 입력되어야 합니다")
    var userTypeName: String? = null

    @Comment("IP")
    @ValidIpAddress
    var ip: String? = null

    @Comment("휴대전화번호")
    @ValidHp
    var hp: String? = null

    @ValidByte(6)
    var vchar: String? = null

    @ValidDate
    @Comment("최근 초대일자")
    var lastInviteDate: String? = null

    @ValidMultiNumber(11)
    @Comment("입찰 금액")
    var bidCost: Long? = null

}
