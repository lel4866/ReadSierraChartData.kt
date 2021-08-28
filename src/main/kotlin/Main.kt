import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder


const val VERSION = "ReadSierraChartScidKotlin 0.1.0"
const val futures_root = "ES"
const val futures_root_len = futures_root.length
const val update_only = true // only process .scid files in datafile_dir which do not have counterparts in datafile_outdir
const val datafile_dir = "C:/SierraChart/Data/"
const val datafile_outdir = "C:/Users/lel48/SierraChartData/"
val futures_month_codes = mapOf( 'H' to 3, 'M' to 6, 'U' to 9, 'Z' to 12 )

enum class ReturnCodes {
    Successful, Ignored, MalformedFuturesFileName, IOErrorReadingData
}

data class FuturesContract(var code: Char, var sYear: String, var year: Int)

data class ScidHeader(
    var FileTypeUniqueHeaderID: Int = 0,  // "SCID"
    var HeaderSize :Int = 0,
    var RecordSize:Int = 0,
    var Version:Short = 0
)

data class ScidRecord(
    var SCDateTime: Long = 0,
    var Open: Float = 0f,
    var High: Float = 0f,
    var Low: Float = 0f,
    var Close: Float = 0f,
    var NumTrades: Int = 0,
    var TotalVolume: Int = 0,
    var BidVolume: Int = 0,
    var AskVolume: Int = 0,
)

fun main(args: Array<String>) {
    // display program arguments
    println("Program arguments: ${args.joinToString()}")
    val begin = System.nanoTime()

    val files = File(datafile_dir).listFiles()
    //files.toList().parallelStream().forEach { processFile(it) }
    //files.forEach { processFile(it) }

    val end = System.nanoTime()
    val elapsedTimeInSecs = (end-begin).toDouble() * 1e-9
    println("Elapsed time in seconds: %.3f".format(elapsedTimeInSecs))
}

fun processFile(file: File) {
    val contract: FuturesContract? = filterFile(file)
    if (contract == null)
        return

    // get filenames for temporary .csv output file and final .zip file
    val futures_contract = futures_root + contract.code + contract.sYear;
    val out_fn_root = datafile_outdir + futures_contract;
    val out_path_csv = out_fn_root + ".csv"; // full path
    val out_path_zip = out_fn_root + ".zip"; // full path

    val ds = DataInputStream(BufferedInputStream(file.inputStream()))

    // read header
    val header = ScidHeader()
    header.FileTypeUniqueHeaderID = Integer.reverseBytes(ds.readInt())
    header.HeaderSize = Integer.reverseBytes(ds.readInt())
    header.RecordSize = Integer.reverseBytes(ds.readInt())
    header.Version = ds.readShort()
    val version: Int = ds.readShort().toInt()
    header.Version = Integer.reverseBytes(version).toShort()
    ds.skipNBytes((header.HeaderSize - 16).toLong())

    val ba = ByteArray(4)
    val record = ScidRecord()

    var count = 0
    while(ds.available() > 0) {
        count++
        record.SCDateTime = ByteBuffer.wrap(ds.readNBytes(8)).order(ByteOrder.LITTLE_ENDIAN).long
        record.Open = ByteBuffer.wrap(ds.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).float
        record.High = ByteBuffer.wrap(ds.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).float
        record.Low = ByteBuffer.wrap(ds.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).float
        record.Close = ByteBuffer.wrap(ds.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).float
        record.NumTrades = ByteBuffer.wrap(ds.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).int
        record.TotalVolume = ByteBuffer.wrap(ds.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).int
        record.BidVolume = ByteBuffer.wrap(ds.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).int
        record.AskVolume = ByteBuffer.wrap(ds.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).int
        val yy = 1
    }
    val xx = 2
}

fun filterFile(file: File): FuturesContract? {
    val filename = file.canonicalPath
    val baseName = file.nameWithoutExtension
    if (baseName.endsWith(futures_root))
        return null;
    if (baseName.length < futures_root_len) {
        println("$filename has invalid futures contract: $baseName")
        return null
    }
    if (!baseName.startsWith(futures_root))
        return null

    val futures_month_code = baseName[futures_root.length]
    if (!(futures_month_code in futures_month_codes)) {
        println("$filename has invalid futures month code: $futures_month_code")
        return null
    }
    if (!baseName.startsWith(futures_root))
        return null

    val sYear = file.nameWithoutExtension.substring(futures_root.length + 1, futures_root.length + 3)
    val year = try {
        sYear.toInt() + 2000
    }
    catch (exception: NumberFormatException) {
        println("${file.canonicalPath} has invalid futures contract year: $sYear")
        return null
    }

    println("Processing $filename")
    return FuturesContract(futures_month_code, sYear, year)
}
