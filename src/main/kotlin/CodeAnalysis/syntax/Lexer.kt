package CodeAnalysis.syntax

import java.io.Reader
import java.util.*

class Lexer(data: Reader) {
    // 数据流
    private var data: Reader
    // 当前扫描的字符
    private var readPoint: Char = ' '
    // 当前的扫描行
    private var row: Int = 1
    // 当前的扫描列
    private var col: Int = 0
    private var words: Hashtable<String, SyntaxToken> = Hashtable()

    var diagnostics = LinkedList<String>()

    companion object{
        var line: Int = 1   // 扫描到的行数
    }

    /**
     * 添加保留字
     */
    init {
        reserve(SyntaxToken(TokenKind.PROGRAM,"program"))
        reserve(SyntaxToken(TokenKind.PROCEDURE,"procedure"))
        reserve(SyntaxToken(TokenKind.WHILE,"while"))
        reserve(SyntaxToken(TokenKind.DO,"do"))
        reserve(SyntaxToken(TokenKind.ELIHW,"endwh"))
        reserve(SyntaxToken(TokenKind.BEGIN,"begin"))
        reserve(SyntaxToken(TokenKind.END,"end"))
        reserve(SyntaxToken(TokenKind.RETURN,"return"))
        reserve(SyntaxToken(TokenKind.INTEGER,"integer"))
        reserve(SyntaxToken(TokenKind.REAL,"float"))
        reserve(SyntaxToken(TokenKind.CHAR,"char"))
        reserve(SyntaxToken(TokenKind.BOOL,"bool"))
        reserve(SyntaxToken(TokenKind.WHILE,"for"))
        reserve(SyntaxToken(TokenKind.VAR,"var"))
        reserve(SyntaxToken(TokenKind.IF,"if"))
        reserve(SyntaxToken(TokenKind.THEN,"then"))
        reserve(SyntaxToken(TokenKind.ELSE,"else"))
        reserve(SyntaxToken(TokenKind.FI,"fi"))
        reserve(SyntaxToken(TokenKind.TYPE, "type"))
        reserve(SyntaxToken(TokenKind.ARRAY, "array"))
        reserve(SyntaxToken(TokenKind.OF, "of"))
        reserve(SyntaxToken(TokenKind.READ, "read"))
        reserve((SyntaxToken(TokenKind.WRITE, "write")))

        this.data = data
    }

    /**
     * 添加保留字
     */
    private fun reserve(word: SyntaxToken) {
        words.put(word.text, word)
    }

    /**
     * 读取下一个字符
     */
    private fun readch() {
        readPoint = data.read().toChar()
        if(readPoint == '\r') {
            // 如果是'\r'后接'\n'则跳过'\n'
            readch('\n')
            this.row++
            this.col = 0        // col 设置为0，因为'\n'设置为空白位，之后会多读一次
        }
        else {
            this.col++
        }
    }


    /**
     * 读取一个字符验证是否是ch，验证成功则将readPoint置空
     * @return 验证的结果
     */
    fun readch(ch: Char): Boolean {
        readch()
        if(readPoint != ch) return false
        readPoint = ' '      // 置空
        return true
    }

    /**
     * 扫描并返回一个token
     */
    fun scan(): SyntaxToken {
        while(true) {
            if(readPoint == ' ' || readPoint == '\t') {
                readch()
            }
            // windows换行"\r\n"
            else if(readPoint == '\r') {
                if(readch('\n')) {
                    readch()
                }
            }
            else break
        }
        val savedRow = row
        val savedCol = col
        // 保留符号
        when(readPoint) {
            '=' -> {
                readch()
                return SyntaxToken.eq.copy(savedRow, savedCol)
            }
            '>' -> {
                if(readch('=')) {
                    return SyntaxToken.ge.copy(savedRow, savedCol)
                } else {
                    return SyntaxToken.gt.copy(savedRow, savedCol)
                }
            }
            '<' -> {
                if(readch('=')) {
                    return SyntaxToken.le.copy(savedRow, savedCol)
                } else {
                    return SyntaxToken.lt.copy(savedRow, savedCol)
                }
            }
            '(' -> {
                readch()
                return SyntaxToken.lsparen.copy(savedRow, savedCol)
            }
            ')' -> {
                readch()
                return SyntaxToken.rsparen.copy(savedRow, savedCol)
            }
            '+' -> {
                readch()
                return SyntaxToken.plus.copy(savedRow, savedCol)
            }
            '-' -> {
                readch()
                return SyntaxToken.minus.copy(savedRow, savedCol)
            }
            '*' -> {
                readch()
                return SyntaxToken.times.copy(savedRow, savedCol)
            }
            '/' -> {
                readch()
                return SyntaxToken.div.copy(savedRow, savedCol)
            }
            ':' -> {
                if(readch('=')) {
                    return SyntaxToken.assign.copy(savedRow, savedCol)
                } else {
                    return BadToken(':').copy(savedRow, savedCol)
                }
            }
            '{' -> {
                while(!readch('}') && readPoint != (-1).toChar()) {
                    continue
                }
                if(readPoint == (-1).toChar()) {
                    diagnostics.add("Comment should be closed. row: $row, col: $col")
                }
                return scan()
            }
            ';' -> {
                readch()
                return SyntaxToken.semi.copy(savedRow, savedCol)
            }
            '%' -> {
                readch()
                return SyntaxToken.mod.copy(savedRow, savedCol)
            }
            '[' -> {
                readch()
                return SyntaxToken.lbracket.copy(savedRow, savedCol)
            }
            ']' -> {
                readch()
                return SyntaxToken.rbracket.copy(savedRow, savedCol)
            }
            ',' -> {
                readch()
                return SyntaxToken.comma.copy(savedRow, savedCol)
            }
            '.' -> {
                if(readch('.')) {
                    return SyntaxToken.dotdot.copy(savedRow, savedCol)
                } else {
                    return SyntaxToken.dot.copy(savedRow, savedCol)
                }
            }
            '&' -> {
                if(readch('&')) {
                    return SyntaxToken.ampersandampersand.copy(savedRow, savedCol)
                } else {
                    return BadToken('&').copy(savedRow, savedCol)
                }
            }
            '|' -> {
                if (readch('|')) {
                    return SyntaxToken.pipepipe.copy(savedRow, savedCol)
                } else {
                    return BadToken('|')
                }
            }
            '!' -> {
                if(readch('=')) {
                    return SyntaxToken.nequals.copy(savedRow, savedCol)
                } else {
                    return SyntaxToken.bang.copy(savedRow, savedCol)
                }
            }
        }
        // 整数或浮点数
        if(readPoint.isDigit()) {
            var _text = ""  // 记录数值

            while(readPoint.isDigit()) {
                _text += readPoint
                readch()
            }
            var intValue = 0
            try {
                intValue = _text.toInt()
            } catch (e: NumberFormatException) {
                diagnostics.add("The number $_text can't be represented by an int32")
            }
            return SyntaxToken(kind = TokenKind.NumberToken, text = _text, value = intValue, row = savedRow, col = savedCol)
//            if(readPoint != '.') {
//                var intValue = 0
//                try {
//                    intValue = _text.toInt()
//                } catch (e: NumberFormatException) {
//                    diagnostics.add("The number $_text can't be represented by an int32")
//                }
//                return SyntaxToken(kind = TokenKind.NumberToken, text = _text, value = intValue)
//            }
//            _text += '.'
//            readch()
//            while(readPoint.isDigit()) {
//                _text += readPoint
//                readch()
//            }
//            var floatValue = 0F
//            try {
//                floatValue = _text.toFloat()
//            } catch (e: java.lang.NumberFormatException) {
//                diagnostics.add("The number $_text can't be represented by an float number")
//            }
//            return SyntaxToken(kind = TokenKind.RealToken, text = _text, value = floatValue)
        }
        // 变量或保留字
        if(readPoint.isLetter()) {
            var name = StringBuffer("")
            while(readPoint.isLetterOrDigit()) {
                name.append(readPoint)
                readch()
            }
            val s = name.toString()
            when(getWordType(s)) {
                TokenKind.TrueToken -> return SyntaxToken(kind = TokenKind.TrueToken, text = "true", value = true, row = savedRow, col = savedCol)
                TokenKind.FalseToken -> return SyntaxToken(kind = TokenKind.FalseToken, text = "false", value = false, row = savedRow, col = savedCol)
            }
            val tok = words[s]
            // 如果已经在words表中存在则不需要再生成一个token
            if(tok != null) return tok.copy(savedRow, savedCol)
            // 生成一个变量token
            val w = SyntaxToken(kind = TokenKind.ID, text = s, value = "", row = savedRow, col = savedCol)
            words.put(w.text, w)
            return w
        }
        // 字符型
        if(readPoint == '\'') {
            readch()
            // ''的情况不是字符
            if(readPoint == '\'')  {
                return BadToken(ch = '\'', row = savedRow, col = savedCol)
            }
            // 检查是否有封闭的''
            else {
                // 记录当前的peek, 也就是字符值
                val tmp = readPoint
                readch()
                // 不是字符, 回溯peek, 并返回BadToken
                if(readPoint != '\'') {
                    when(tmp) {
                        // 如果忽略的是空白符直接返回
                        ' ', '\t' -> {}
                        // 如果忽略的是换行符则增加一行
                        '\r' -> {
                            if(readch('\n')) {
                                readch()
                            }
                        }
                        // 其他情况回溯
                        else -> readPoint = tmp
                    }
                    return BadToken(ch = '\'', row = savedRow, col = savedCol)
                }
                // 是字符, 返回字符token
                else {
                    readch()
                    return SyntaxToken(TokenKind.CharToken, tmp.toString(), tmp, row = savedRow, col = savedCol)
                }
            }
        }
        if(readPoint == (-1).toChar()) return SyntaxToken(TokenKind.EOF, "end of file", row = savedRow, col = savedCol)
        // 其他情况，返回bad token
        diagnostics.add("ERROR: bad  character input: '$readPoint'. row: $row, col: $col")
        val tok = BadToken(ch = readPoint)
        readPoint = ' '
        return tok
    }

    /**
     * 根据读取的字符串判断是否是特殊保留字，如true, false
     */
    fun getWordType(s: String): TokenKind = when(s) {
        "true" -> TokenKind.TrueToken
        "false" -> TokenKind.FalseToken
        else -> TokenKind.ID
    }
}