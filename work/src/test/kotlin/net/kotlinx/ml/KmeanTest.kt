//package net.kotlinx.ml
//
//
//import net.kotlinx.kotest.BeSpecLog
//import org.deeplearning4j.clustering.cluster.Point
//import org.deeplearning4j.clustering.kmeans.KMeansClustering
//import org.junit.jupiter.api.Test
//import org.nd4j.linalg.cpu.nativecpu.NDArray
//
//
//class KmeanTest : BeSpecLog({
//
//    @Test
//    fun test() {
//
//
//        // 데이터 준비
//        val data = arrayOf(
//            floatArrayOf(1.0f, 2.0f),
//            floatArrayOf(2.0f, 3.0f),
//            floatArrayOf(3.0f, 4.0f),
//            floatArrayOf(4.0f, 5.0f),
//            floatArrayOf(5.0f, 6.0f),
//            floatArrayOf(6.0f, 7.0f),
//            floatArrayOf(7.0f, 8.0f),
//            floatArrayOf(8.0f, 9.0f),
//            floatArrayOf(9.0f, 10.0f)
//        ).mapIndexed { index, it ->
//            Point("data$index",NDArray(it))
//        }
//
//        // KMeansClustering 생성
//
//        // KMeansClustering 생성
//        val clustering: KMeansClustering = KMeansClustering.setup(2, 100,"std")
//
//        clustering.applyTo(data)
//
//
////        clustering
////
////        // 모델을 사용하여 데이터를 군집화
////
////        // 모델을 사용하여 데이터를 군집화
////        val labels: INDArray = model.predict(data)
////
////        // 군집화 결과 출력
////
////        // 군집화 결과 출력
////        for (i in 0 until labels.length()) {
////            println("data[" + i + "]: " + data[i] + ", label: " + labels.getDouble(i))
////        }
//
//    }
//
//})