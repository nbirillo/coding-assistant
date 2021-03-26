package org.jetbrains.research.ml.coding.assistant.report

import org.jetbrains.research.ml.coding.assistant.dataset.model.MetaInfo
import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import java.io.OutputStream

/**
 *  Generates algorithm's result report and writes it into outputStream
 */
interface HintReportGenerator {
    fun generate(outputStream: OutputStream, report: HintReport)
}

class MarkdownHintReportGenerator(private val codeRepository: CodeRepository) : HintReportGenerator {
    override fun generate(outputStream: OutputStream, report: HintReport) {
        val text = """
## ${report.algorithmName}

### Student code
```python
${report.partialSolution.fragment}
```

### Closest node's code
`vertex` = ${report.closestVertex}
`meta information`:
${report.closestVertex.toDebugString()}

```python
${report.closestVertex?.let(
            this::fetchCode
        ) ?: "No Hint Code"}
```

### Hint node's code 
`vertex` = ${report.nextNode}
`meta information`:
${report.nextNode.toDebugString()}

```python
${
            report.nextNode?.let(this::fetchCode) ?: "No Hint Code"
        }
```
""".trimIndent()
        outputStream.write(text.toByteArray())
    }

    private fun fetchCode(vertex: SolutionSpaceVertex): String {
        return codeRepository.getCode(vertex.info.first().id)
    }
}

/**
 * Uses inner generator to generate report for multiple algorithms
 */
class CompositeMarkdownHintReportGenerator(private val generator: HintReportGenerator) {
    fun generate(outputStream: OutputStream, reports: Collection<HintReport>) {
        val firstReport = reports.firstOrNull() ?: return
        outputStream.write(
            buildString {
                appendLine("# ${firstReport.taskName}")
appendLine()
appendLine(firstReport.partialSolution.metaInfo.toDebugString())
appendLine()
            }.toByteArray()
        )

        for (report in reports) {
            generator.generate(outputStream, report)
        }
    }
}

private fun SolutionSpaceVertex?.toDebugString(): String {
    return this?.info?.joinToString("\n\n") { it.metaInfo.toDebugString() } ?: "No meta information"
}

private fun MetaInfo.toDebugString(): String {
    return buildString {
        appendLine("* Age: $age")
        appendLine("* ProgramExperience: $programExperience")
        appendLine("* TestsResults: $testsResults")
    }
}
