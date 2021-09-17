package com.grument.gpudelegatebugreproduce

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.tensorflow.lite.Delegate
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

open class MainActivity : AppCompatActivity() {

    private val scope = CoroutineScope(
        Job() + Dispatchers.IO
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        scope.launch {
            initInterpreter(this@MainActivity)
        }
    }

    private fun initDelegate(): Delegate? {

        val compatList = CompatibilityList()

        return if (compatList.isDelegateSupportedOnThisDevice)
            GpuDelegate()
        else {
            null
        }
    }

    @Throws(IOException::class)
    private fun initInterpreter(context: Context): Interpreter {

        val tfliteOptions = Interpreter
            .Options()

        val delegate: Delegate? = initDelegate()

        if (delegate != null) {
            tfliteOptions.addDelegate(delegate)
        } else {
            tfliteOptions.setNumThreads(4)
        }

        return Interpreter(loadModelFile(context), tfliteOptions)
    }

    @Throws(IOException::class)
    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val retFile = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        fileDescriptor.close()
        return retFile
    }
}