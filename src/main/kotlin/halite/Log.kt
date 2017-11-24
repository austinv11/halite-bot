package halite

import java.io.FileWriter
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedDeque

class Log private constructor(private val file: FileWriter) {
    companion object {
        private var instance: Log? = null
        private val buffer: ConcurrentLinkedDeque<String> = ConcurrentLinkedDeque()

        internal fun initialize(f: FileWriter) {
            instance = Log(f)
            flushBuffer()
        }
        
        fun isInitialized() = instance != null

        private fun flushBuffer() {
            buffer.removeIf { 
                log(it)
                true
            }
        }
        
        fun log(message: String) {
            if (isInitialized()) {
                try {
                    instance!!.file.write(message)
                    instance!!.file.write("\n")
                    instance!!.file.flush()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                buffer.add(message)
            }
        }
    }
}
