package com.github.stefaniesinner.typeinspector.statusBar

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

/**
 * Factory for creating the Type Inspector widget.
 */
class TypeInspectorFactory : StatusBarWidgetFactory {

    override fun getId(): String = "TypeInspector"

    override fun getDisplayName(): String = "Type Inspector"

    override fun createWidget(project: Project): StatusBarWidget = TypeInspector(project)

    override fun disposeWidget(widget: StatusBarWidget) = widget.dispose()
}
