package com.erfangc.docusandbox.templates.models

data class AutoFillInstruction(

    /**
     * The dataProperty to copy data from
     * if this [Field] is a checkbox, this property shall be ignored. If [onlyIf]
     * passes - then the checkbox would be checked
     */
    val copyFrom: String? = null,

    /**
     * If this [Field] is a checkbox, it shall be auto 'checked' when [onlyIf] evaluates to true,
     * unchecked otherwise
     */
    val onlyIf: OnlyIf? = null,

    /**
     * Takes result of the other properties and apply some transformation
     */
    val transform: Transform? = null,
)