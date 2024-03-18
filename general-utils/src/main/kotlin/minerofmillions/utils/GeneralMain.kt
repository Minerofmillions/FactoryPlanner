package minerofmillions.utils

import kotlinx.coroutines.runBlocking
import java.io.File

fun main() = runBlocking {
    println(File("E:\\SBUnpacked").listFiles()!!.mapParallel(File::deleteRecursively).all(::identity))
}