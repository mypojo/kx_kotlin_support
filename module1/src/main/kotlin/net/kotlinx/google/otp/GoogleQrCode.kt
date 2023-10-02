package net.kotlinx.google.otp

import okhttp3.HttpUrl.Companion.toHttpUrl


/**
 * 구글 QR코드 생성기
 * https://developers.google.com/chart/infographics/docs/qr_codes?hl=ko
 * */
object GoogleQrCode {

    /**
     * 구글 OTP 인증기 등록 QR링크 생성
     * https://github.com/google/google-authenticator/wiki/Key-Uri-Format#issuer
     * 확인 필요
     * */
    fun otpRegLink(name: String, user: String, secret: String): String {
        return "https://chart.apis.google.com/chart?cht=qr&chs=200x200&chld=H|0".toHttpUrl()
            .newBuilder()
            .addQueryParameter("chl", "otpauth://totp/${name}:${user}?secret=${secret}")
            .build().toString()
    }


}
