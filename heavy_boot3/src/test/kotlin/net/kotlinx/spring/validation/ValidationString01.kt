package net.kotlinx.spring.validation

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import net.kotlinx.core.Comment
import net.kotlinx.domain.validation.ann.*
import org.hibernate.validator.constraints.Length

class ValidationString01 {


    //==================================================== 널체크 ======================================================
    @Comment("이름")
    var name: @NotEmpty @Length(max = 3, message = "#길이가 너무 길어요") String? = null

    @Comment("그룹명1")
    var groupName01: String? = null
        @Length(min = 4, max = 8, message = "{min}~{max} 자 사이의 값을 입력해주세요") //이렇게 달아도 되긴 함
        get

    @Comment("그룹명2")
    @get:NotEmpty
    @Size
    @get:Length(min = 4, max = 8) //get: 으로 해도 됨
    var groupName02: String? = null

    //==================================================== 패턴 ======================================================
    @Comment("사용자 타입")
    @field:NotEmpty
    @Pattern(regexp = "NEW|DEFAULT")
    var userType: String? = null

    @Comment("사업자번호1")
    @field:ValidBid
    var comNo1: String? = null

    @Comment("사업자번호2")
    @field:ValidBid
    var comNo2: String? = null


    @Comment("본문1")
    @field:ValidByte(10)
    var contents1: String? = null

    @Comment("입찰가")
    @field:ValidMultiNumber
    var bidCost: Int? = null


    //==================================================== 날짜 ======================================================
    @Comment("최근 초대일자")
    @field:ValidDateString
    var lastInviteDate: String? = null

    @Comment("전화번호")
    @field:ValidTel
    var tel: String? = null

    //
    //	@StringDate(pattern= TimeFormat.YMD)
    //	private String basicDate;
    //
    //
    //	@Comment("키워드명")
    //	@ConvStringToLowerCase  //CSV용
    //	@JsonAdapter(GsonAdapterUtil.StringToLowerAdapter.class) //JSON용
    //	private String kwdName;
    //
    //	@MaxByte(18)
    //	private String vchar;
    //
    //	@MaxByte(value = 18,charset = CharSets.MS949)
    //	private String vcharKr;
    //
    //	@Comment("사용자 이름")
    //	@Pattern2(regexp="ㄱ-ㅎㅏ-ㅣ가-힣-a-zA-Z0-8")
    //	private String userName;
    //
    //	@ConvStringPattern(regexp="[A-Z0-8]")
    //	private String alphaNum;
    //
    //	@Ip
    //	private String ip;
    //
    //	@Email
    //	private String email;
    //
    //	@URL
    //	private String url;
    //
    //	@CreditCardNumber
    //	private String cno;
    //
    //	private String startDate;
    //	private String endDate;
    //
    //	@Pattern2(regexp="\\d")
    //	private String accountNo;
    //

    //
    //	@StringDate
    //	@ConvStringPattern(regexp="[0-9]")
    //	private String start;
    //
    //	@StringDate
    //	@ConvStringPattern(regexp="[0-9]")
    //	private String end;
}
