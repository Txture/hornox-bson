package org.chronos.ng.chronodocs.impl.json

import org.jetbrains.annotations.Nullable

class AsciiTable {

    private val content: MutableList<TableRow> = mutableListOf()
    private val columnToAlignment: MutableMap<Int, Align> = mutableMapOf()
    private val columnsWithSeparators: MutableSet<Int> = mutableSetOf()

    @JvmOverloads
    constructor(layout: String? = null) {
        if (layout != null) {
            this.setLayout(*layout.toCharArray())
        }
    }

    fun setLayout(vararg chars: Char) {
        val layout = chars.toList()
        var columnIndex = 0
        layout.forEach { char ->
            when (char) {
                'l', 'L' -> {
                    setColumnAlignment(columnIndex, Align.LEFT); columnIndex++
                }
                'r', 'R' -> {
                    setColumnAlignment(columnIndex, Align.RIGHT); columnIndex++
                }
                'c', 'C' -> {
                    setColumnAlignment(columnIndex, Align.CENTER); columnIndex++
                }
                '|' -> columnsWithSeparators += columnIndex
            }
        }
    }

    fun addRule(separator: Char = '-') {
        content.add(SeparatorRow(separator))
    }

    fun addRow(@Nullable vararg columns: Any?) {
        content.add(TextRow(columns.toList()))
    }

    fun setColumnAlignment(columnIndex: Int, alignment: Align) {
        columnToAlignment[columnIndex] = alignment
    }

    fun render(): String {
        val columnCount = this.content.asSequence().mapNotNull { it.columns() }.maxOrNull()
        if (columnCount == null) {
            // no content!
            return ""
        }
        val columnToWidth = mutableMapOf<Int, Int>()
        for (columnIndex in 0 until columnCount) {
            val columnWidth = this.content.asSequence().mapNotNull { it.cellWidth(columnIndex) }.maxOrNull()
                ?: 0
            columnToWidth[columnIndex] = columnWidth
        }
        val builder = StringBuilder()
        for (row in content) {
            val renderedRow = row.render(
                totalColumns = columnCount,
                separatorBeforeColumn = { columnsWithSeparators.contains(it) },
                separatorAfterColumn = { (it + 1 == columnCount) && columnsWithSeparators.contains(it + 1) },
                columnAlignment = {
                    columnToAlignment[it]
                        ?: Align.LEFT
                },
                columnWidth = {
                    columnToWidth[it]
                        ?: 0
                }
            )
            builder.append(renderedRow)
            builder.append("\n")
        }
        return builder.toString()
    }


    private abstract class TableRow {

        abstract fun cellWidth(columnIndex: Int): Int

        abstract fun render(totalColumns: Int, separatorBeforeColumn: (Int) -> Boolean, separatorAfterColumn: (Int) -> Boolean, columnAlignment: (Int) -> Align, columnWidth: (Int) -> Int): String

        abstract fun columns(): Int?

    }

    private class TextRow(cells: List<Any?>) : TableRow() {

        private val cells = cells.map { it?.toString() }

        override fun cellWidth(columnIndex: Int): Int {
            if (columnIndex >= this.cells.size) {
                return 0
            }
            val content = this.cells[columnIndex]
            if (content == null) {
                return 0
            } else {
                return content.length
            }
        }

        override fun render(totalColumns: Int, separatorBeforeColumn: (Int) -> Boolean, separatorAfterColumn: (Int) -> Boolean, columnAlignment: (Int) -> Align, columnWidth: (Int) -> Int): String {
            // partition the row into multi-column-blocks (NULLs indicate multi-columns)
            val blocks = mutableListOf<CellBlock>()
            var currentIndex = 0
            var currentBlock = CellBlock(null, 0, 0)
            while (currentIndex < totalColumns) {
                val cellContent = this.cellContent(currentIndex)
                if (cellContent != null) {
                    // check if current block has content
                    if (currentBlock.content == null) {
                        // use this text for the content
                        currentBlock.content = cellContent
                        currentBlock.toIndex = currentIndex
                    } else {
                        // close previous block
                        blocks += currentBlock
                        currentBlock = CellBlock(cellContent, currentIndex, currentIndex)
                    }
                } else {
                    // add to existing block
                    currentBlock.toIndex = currentIndex
                }
                currentIndex++
            }
            blocks += currentBlock
            return blocks.asSequence().map { it.render(separatorBeforeColumn, separatorAfterColumn, columnAlignment, columnWidth) }.joinToString(separator = "")
        }

        private fun cellContent(columnIndex: Int): String? {
            return if (columnIndex >= this.cells.size) {
                null
            } else {
                this.cells[columnIndex]
            }
        }

        override fun columns(): Int {
            return this.cells.size
        }

        private class CellBlock(var content: String?, var fromIndex: Int, var toIndex: Int) {

            val cells: Int get() = this.toIndex - this.fromIndex + 1

            fun render(separatorBeforeColumn: (Int) -> Boolean, separatorAfterColumn: (Int) -> Boolean, columnAlignment: (Int) -> Align, columnWidth: (Int) -> Int): String {
                val builder = StringBuilder()
                if (separatorBeforeColumn(this.fromIndex)) {
                    builder.append("|")
                } else {
                    builder.append(" ")
                }
                // margin left
                builder.append(" ")
                // total block width in characters
                val width = totalWidth(columnWidth)
                val align = if (this.cells > 1) {
                    // multi-columns are always left-aligned
                    Align.LEFT
                } else {
                    // check the alignment function for our cell
                    columnAlignment(this.fromIndex)
                }
                builder.append(
                    align.pad(
                        this.content
                            ?: "", width
                    )
                )
                // margin right
                builder.append(" ")
                // check if we need another separator
                if (separatorAfterColumn(this.toIndex)) {
                    builder.append("|")
                }
                return builder.toString()
            }

            private fun totalWidth(columnWidth: (Int) -> Int): Int {
                return (this.fromIndex..this.toIndex).asSequence().map { columnWidth(it) }.sum() +
                    // add the additional width for column separator, margin left and margin right
                    (this.toIndex - this.fromIndex) * 3
            }

        }
    }

    private class SeparatorRow(
        val fillChar: Char
    ) : TableRow() {

        override fun cellWidth(columnIndex: Int): Int {
            return 0
        }

        override fun columns(): Int? {
            return null
        }

        override fun render(totalColumns: Int, separatorBeforeColumn: (Int) -> Boolean, separatorAfterColumn: (Int) -> Boolean, columnAlignment: (Int) -> Align, columnWidth: (Int) -> Int): String {
            var totalWidth = 0
            for (columnIndex in 0 until totalColumns) {
                // separator spacing
                totalWidth += 1
                // left margin
                totalWidth += 1
                // content
                totalWidth += columnWidth(columnIndex)
                // right margin
                totalWidth += 1
                if (separatorAfterColumn(columnIndex)) {
                    // separator spacing
                    totalWidth += 1
                }
            }
            return this.fillChar.toString().repeat(totalWidth)
        }

    }

    enum class Align {
        LEFT {

            override fun pad(content: String, maxWidth: Int, padChar: Char): String {
                if (content.length >= maxWidth) {
                    return content
                }
                return content.padEnd(maxWidth, padChar)
            }

        },

        RIGHT {

            override fun pad(content: String, maxWidth: Int, padChar: Char): String {
                if (content.length >= maxWidth) {
                    return content
                }
                return content.padStart(maxWidth, padChar)
            }

        },

        CENTER {

            override fun pad(content: String, maxWidth: Int, padChar: Char): String {
                if (content.length >= maxWidth) {
                    return content
                }
                val padChars = maxWidth - content.length
                val padLeft = padChars / 2
                val padRight = padChars - padLeft
                return padChar.toString().repeat(padLeft) + content + padChar.toString().repeat(padRight)
            }

        }
        ;

        abstract fun pad(content: String, maxWidth: Int, padChar: Char = ' '): String
    }

}