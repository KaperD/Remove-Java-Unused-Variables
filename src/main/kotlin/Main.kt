import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.check
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import ru.hse.JavaUnusedVarsRemover
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) = RemoveUnused().main(args)

private class RemoveUnused : CliktCommand() {
    val saveOriginalLayout: Boolean by option("-r", "--raw", help = "Enables saving original layout of code")
        .flag()
    val outputFile: File? by option("-o", "--output", help = "File to save result").file()
        .check("File should be correct") {
            return@check !it.exists() || it.isFile && it.canWrite()
    }
    val inputFile: File? by argument("INPUT_FILE").file().optional().check("File should be correct") {
        return@check it.exists() && it.isFile && it.canRead()
    }

    override fun run() {
        val remover = JavaUnusedVarsRemover()
        val input = inputFile?.readText() ?: System.`in`.bufferedReader().readText()
        val output = remover.removeUnused(input, saveOriginalLayout)
        if (output.isFailure) {
            println("Failure: ${output.exceptionOrNull()?.message}")
            exitProcess(1)
        }
        val result = output.getOrThrow()
        outputFile?.writeText(result) ?: println(result)
    }
}
