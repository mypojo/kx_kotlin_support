package net.kotlinx.slack

//https://api.slack.com/reference/surfaces/formatting

/** 코드 */
fun String.slackCode(): String = "`${this}`"

/** 코드블럭  */
fun String.slackQuote(): String = "```${this}```"

/** Block quotes  */
fun String.slackBlockQuote(): String = ">${this}"

/** 멘션 (@붙여서 알람 주는거)  */
fun String.slackMention(): String = "<@${this}>"

/**
 * 링크달기
 * ex) 배치 에러시 로그 링크, ECR 업로드시 확인 링크 등
 */
fun String.slackLink(link: String): String = "<${link}|${this}>"