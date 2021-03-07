package org.jetbrains.research.ml.coding.assistant.report

import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import org.jetbrains.research.ml.coding.assistant.solutionSpace.builder.SolutionSpaceGraphBuilder
import java.io.File

interface HintReportGenerator {
    fun generate(intoFile: File, report: HintReport)
}

// for report only. very bad complexity
internal fun SolutionSpaceGraphBuilder.getCode(id: String): String {
    return graph.vertexSet()
        .first { vertex -> vertex.partialSolutions.any { it.id == id } }
        .representativeSolution.psiFragment.text
}

class MarkdownHintReportGenerator(private val codeRepository: CodeRepository) : HintReportGenerator {
    override fun generate(intoFile: File, report: HintReport) {
        val text = """
## ${report.algorithmName}

### Student code
```python
${report.studentCode}
```

### Closest node's code
`vertex` = ${report.closestVertex}

```python
${fetchCode(
            report.closestVertex
        )}
```

### Hint node's code 
`vertex` = ${report.nextNode}
```python
${report.nextNode?.let(this::fetchCode) ?: "No Hint Code"}
```
""".trimIndent()
        intoFile.appendText(text)
    }

    private fun fetchCode(vertex: SolutionSpaceVertex): String {
        return codeRepository.getCode(vertex.info.first().id)
    }
}
