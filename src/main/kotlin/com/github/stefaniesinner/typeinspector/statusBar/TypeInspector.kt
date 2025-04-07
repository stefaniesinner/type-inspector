package com.github.stefaniesinner.typeinspector.statusBar

import com.github.stefaniesinner.typeinspector.services.TypeInspectorAnalyzer
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidget.TextPresentation
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.Consumer
import java.awt.Component
import java.awt.event.MouseEvent
import javax.swing.SwingUtilities

/**
 * Widget that displays the type of the variable under the caret in the status bar.
 */
class TypeInspector(private val project: Project) : StatusBarWidget, TextPresentation {

    private var displayedTypeText: String = ""

    // Analyzer to compute the type
    private val analyzer: TypeInspectorAnalyzer = project.getService(TypeInspectorAnalyzer::class.java)

    /**
     * Updates the type displayed in the status bar by analyzing the variable at the current caret position.
     */
    private fun updateDisplayedType(editor: Editor) {
        ApplicationManager.getApplication().executeOnPooledThread {
            // Run PSI analysis in a read action for thread safety.
            val analysisResult = ApplicationManager.getApplication().runReadAction<String> {
                analyzer.analyzeType(editor, editor.caretModel.currentCaret.offset)
            }
            SwingUtilities.invokeLater {
                displayedTypeText = analysisResult
                WindowManager.getInstance().getStatusBar(project)?.updateWidget(ID())
            }
        }
    }

    override fun ID(): String = "TypeInspector"

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun getText(): String = if (displayedTypeText.isNotEmpty()) displayedTypeText else "Unknown"

    override fun getAlignment(): Float = Component.CENTER_ALIGNMENT

    override fun getTooltipText(): String = "Type of variable under the caret"

    override fun getClickConsumer(): Consumer<MouseEvent>? = null

    override fun install(statusBar: StatusBar) {
        // If an editor is already open, force an update
        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        if (editor != null) {
            updateDisplayedType(editor)
        }

        // Register a CaretListener to update the type when the caret position changes
        EditorFactory.getInstance().eventMulticaster.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) {
                event.caret?.let { updateDisplayedType(event.editor) }
            }
        }, this)
    }

}
