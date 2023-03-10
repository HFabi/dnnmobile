package de.hfabi.dnnmobile.utils

import android.content.Context
import de.hfabi.dnnmobile.DnnLogger
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset

/**
 * MlUtils
 *
 * Contains utilities for processing of data.
 */
object MlUtils {

    /**
     * helper to read CSV files from assets
     */
    fun readFromAssetCSV(
        context: Context,
        assetPath: String,
        resultSize: Int,
        skipBytes: Long = 0
    ): FloatArray {
        val inputStream: InputStream?
        var result: FloatArray = floatArrayOf()
        try {
            inputStream = context.assets.open(assetPath)
            result = readFromCSV(inputStream, resultSize, skipBytes)
            inputStream.close()
        } catch (e: Error) {
            DnnLogger.logE(e, "Error readFromAssetCSV $assetPath")
        }
        return result
    }

    /**
     * helper to read CSV files from assets
     */
    fun readFromCSV(
        inputStream: InputStream,
        resultSize: Int,
        skipBytes: Long = 0
    ): FloatArray {
        val result = FloatArray(resultSize)
        val reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
        if (skipBytes != 0L) {
            reader.skip(skipBytes)
        }
        var nextLine = reader.readLine()
        var index = 0
        while (nextLine != null) {
            val line = nextLine.split(",").map { it.toFloat() }

            for (value in line) {
                result[index] = value
                index++
            }
            nextLine = reader.readLine()
        }
        reader.close()
        return result
    }

    /**
     * Load model from assets
     */
    fun getModelByteBuffer(context: Context, modelPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        return FileInputStream(fileDescriptor.fileDescriptor).channel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    /**
     * classToOneHotEncoding
     *
     * @param numericClass numeric class
     * @param totalNumberOfClasses total number of classes
     * @return one hot encoded class
     */
    fun classToOneHotEncoding(numericClass: Int, totalNumberOfClasses: Int): FloatArray {
        return FloatArray(totalNumberOfClasses).also { it[numericClass] = 1f }
    }

    /**
     * oneHotEncodingToNumeric
     *
     * @param oneHotEncodedClass one hot encoded data
     * @return numeric class
     */
    fun oneHotEncodingToNumeric(oneHotEncodedClass: FloatArray): Int {
        var max = 0f
        var classIndex = 0
        oneHotEncodedClass.forEachIndexed { index, element ->
            if (element > max) {
                max = element
                classIndex = index
            }
        }
        return classIndex
    }
}