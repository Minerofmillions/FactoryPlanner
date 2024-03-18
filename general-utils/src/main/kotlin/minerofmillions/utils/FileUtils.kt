package minerofmillions.utils

import java.io.File

fun File.resolve(vararg children: String): File = children.fold(this, File::resolve)