package net.kotlinx.dooray

//https://helpdesk.dooray.com/share/pages/9wWo-xwiR66BO5LGshgVTg/3216291598897944508
//대부분 정상작동 안함..  왜있는거지?

/**
 * 링크달기
 */
fun String.doorayLink(link: String): String = "[${link}](${this})"