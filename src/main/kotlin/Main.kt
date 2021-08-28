import java.io.File
import java.io.FilenameFilter
import java.lang.Math.sqrt


const val VERSION = "ReadSierraChartScidKotlin 0.1.0"
const val futures_root = "ES"
const val futures_root_len = futures_root.length
const val update_only = true // only process .scid files in datafile_dir which do not have counterparts in datafile_outdir
const val datafile_dir = "C:/SierraChart/Data/"
const val datafile_outdir = "C:/Users/lel48/SierraChartData/"
val futures_codes = mapOf( 'H' to 3, 'M' to 6, 'U' to 9, 'Z' to 12 )

enum class ReturnCodes {
    Successful, Ignored, MalformedFuturesFileName, IOErrorReadingData
}

fun main(args: Array<String>) {
    // display program arguments
    println("Program arguments: ${args.joinToString()}")
    val begin = System.nanoTime()

    val files = File(datafile_dir).listFiles()
    files.forEach { processFile(it) }

    val end = System.nanoTime()
    val elapsedTimeInSecs = (end-begin).toDouble() * 1e-9
    println("Elapsed time in seconds: %.3f".format(elapsedTimeInSecs))
}
fun processFile(file: File) {
    if (!filterFile(file))
        return

    val x = 1
}

fun filterFile(file: File):Boolean {
    val filename = file.canonicalPath
    val baseName = file.nameWithoutExtension
    if (baseName.endsWith(futures_root))
        return false;
    if (baseName.length < futures_root_len) {
        println("$filename has invalid futures contract: $baseName")
        return false
    }
    if (!baseName.startsWith(futures_root))
        return false
    val futures_code: Char = baseName[futures_root.length]
    if (!(futures_code in futures_codes)) {
        println("$filename has invalid futures month code: $futures_code")
        return false
    }
    if (!baseName.startsWith(futures_root))
        return false

    println("Processing $filename")
    return true
}
