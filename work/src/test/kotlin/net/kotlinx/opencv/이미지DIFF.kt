package net.kotlinx.opencv

import net.kotlinx.file.slash
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.system.ResourceHolder
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfDMatch
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.DescriptorMatcher
import org.opencv.features2d.ORB
import org.opencv.imgcodecs.Imgcodecs
import kotlin.math.min

/**
 * https://github.com/Kotlin/dataframe
 * */
class 이미지DIFF : BeSpecLog() {

    private val root = ResourceHolder.WORKSPACE.parentFile.slash("AI").slash("bedrock_랜딩페이지모니터링")
    private val imageFile = listOf(
        root.slash("cp_event.png"),
        root.slash("cp_x.png"),
        root.slash("cp_item01.png"),
    )

    init {
        initTest(KotestUtil.IGNORE)

        log.info { "라이브러리 로드.." }
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
        log.info { "라이브러리 로드 종료" }

        Given("이미지DIFF") {

            Then("로컬") {

                val difference = calculateImageDifference(imageFile[0].absolutePath, imageFile[0].absolutePath)
                println("이미지 차이: %.2f%%".format(difference))

            }

        }
    }

    fun calculateImageDifference(imagePath1: String, imagePath2: String): Double {

        val img1 = Imgcodecs.imread(imagePath1, Imgcodecs.IMREAD_GRAYSCALE)
        val img2 = Imgcodecs.imread(imagePath2, Imgcodecs.IMREAD_GRAYSCALE)

        val orb = ORB.create()
        val keypoints1 = MatOfKeyPoint()
        val keypoints2 = MatOfKeyPoint()
        val descriptors1 = Mat()
        val descriptors2 = Mat()

        orb.detect(img1, keypoints1)
        orb.detect(img2, keypoints2)
        orb.compute(img1, keypoints1, descriptors1)
        orb.compute(img2, keypoints2, descriptors2)

        val matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING)
        val matches = MatOfDMatch()
        matcher.match(descriptors1, descriptors2, matches)

        val matchesList = matches.toList()
        val goodMatches = matchesList.filter { it.distance < 50.0 }

        val similarity = goodMatches.size.toDouble() / min(keypoints1.rows(), keypoints2.rows())
        val difference = (1 - similarity) * 100

        return difference
    }

}