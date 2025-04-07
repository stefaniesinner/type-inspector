package com.github.stefaniesinner.typeinspector.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Service.Level
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.PyTargetExpression
import com.jetbrains.python.psi.types.TypeEvalContext

/**
 * Service that analyzes the type of the variable under the caret.
 */
@Service(Level.PROJECT)
class TypeInspectorAnalyzer(private val project: Project) {

    private data class CaretPositionKey(val filePath: String, val caretOffset: Int)

    // Map that stores analysis results for each caret position
    private val results = mutableMapOf<CaretPositionKey, String>()

    /**
     * Retrieves the PSI file from the given editor.
     */
    private fun getPsiFile(editor: Editor) =
        PsiDocumentManager.getInstance(project).getPsiFile(editor.document)

    /**
     * Creates a unique key for the given PSI file path and caret offset.
     */
    private fun createCaretPositionKey(psiFilePath: String, caretOffset: Int): CaretPositionKey {
        return CaretPositionKey(psiFilePath, caretOffset)
    }

    /**
     * Analyzes the type of the variable at the given editor and caret offset.
     * Returns a string with the type, or a default message if not found.
     */
    fun analyzeType(editor: Editor, caretOffset: Int): String {
        val psiFile = getPsiFile(editor) ?: return "No file found"
        val virtualFile = psiFile.virtualFile ?: return "No file found"
        val key = createCaretPositionKey(virtualFile.path, caretOffset)

        // Return result if available
        results[key]?.let { return it }

        // Find the PSI element at the current caret position
        val elementAtCaret = psiFile.findElementAt(caretOffset) ?: return "No variable found"
        val targetVariable = PsiTreeUtil.getParentOfType(elementAtCaret, PyTargetExpression::class.java)

        val result = if (targetVariable != null) {
            val context = TypeEvalContext.userInitiated(project, psiFile)
            val pyType = context.getType(targetVariable)
            if (pyType != null) "Type: ${pyType.name}" else "No type recognized"
        } else {
            "No variable found"
        }
        results[key] = result
        return result
    }
}
