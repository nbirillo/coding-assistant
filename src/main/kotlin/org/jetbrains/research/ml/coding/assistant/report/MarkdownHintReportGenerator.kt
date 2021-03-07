package org.jetbrains.research.ml.coding.assistant.report

import org.jetbrains.research.ml.coding.assistant.solutionSpace.SolutionSpaceVertex
import java.io.File

class MarkdownHintReportGenerator(private val codeRepository: CodeRepository) : HintReportGenerator {
    override fun generate(intoFile: File, report: HintReport) {
        val text = """
## ${report.algorithmName}

### Student code
```python
${report.studentCode}
```

### Closest node's code

```python
${fetchCode(report.closestVertex)}
```

### Hint node's code 

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
