package net.kotlinx.notebook

import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.read
import org.junit.jupiter.api.Test

/**
 * https://github.com/Kotlin/dataframe
 * */
class 데이터프레임 {

    @Test
    fun `만들기`() {
        // create columns
        val fromTo by columnOf("LoNDon_파리", "MAdrid_miLAN", "londON_StockhOlm", "Budapest_PaRis", "Brussels_londOn")
        val flightNumber by columnOf(10045.0, Double.NaN, 10065.0, Double.NaN, 10085.0)
        val recentDelays by columnOf("23,47", null, "24, 43, 87", "13", "67, 32")
        val airline by columnOf("KLM(!)", "{Air France} (12)", "(British Airways. )", "12. Air France", "'Swiss Air'")

        val df = dataFrameOf(fromTo, flightNumber, recentDelays, airline)

        df.print()

        // typed accessors for columns
// that will appear during
// dataframe transformation
        val origin by column<String>()
        val destination by column<String>()

        val clean = df
            // fill missing flight numbers
            .fillNA { flightNumber }.with { prev()!!["flightNumber"].toString().toDouble() + 10 }

            // convert flight numbers to int
            .convert { flightNumber }.toInt()

            // clean 'airline' column
            .update { airline }.with { "([a-zA-Z\\s]+)".toRegex().find(it)?.value ?: "" }

            // split 'fromTo' column into 'origin' and 'destination'
            .split { fromTo }.by("_").into(origin, destination)

            // clean 'origin' and 'destination' columns
            .update { origin and destination }.with { it.lowercase().replaceFirstChar(Char::uppercase) }

            // split lists of delays in 'recentDelays' into separate columns
            // 'delay1', 'delay2'... and nest them inside original column `recentDelays`
            .split { recentDelays }.inward { "delay$it" }

            // convert string values in `delay1`, `delay2` into ints
            .parse { recentDelays }

        clean.print()
    }

    @Test
    fun load() {

        val df = DataFrame.read("https://raw.githubusercontent.com/mwaskom/seaborn-data/master/iris.csv")
        println(df)

    }


}