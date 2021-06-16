package CodeAnalysis.syntax


/**
 * Token原始类型，其他Token继承该token，继承SyntaxNode，方便表示子节点
 */
open class SyntaxToken(kind: TokenKind, var text: String, var value: Any = "", var row: Int = 0, var col: Int = 0): SyntaxNode(kind) {
    // 该token的类型
    override var kind = kind

    companion object {
        val EndOfFileToken = SyntaxToken(TokenKind.EOF, "end of file")     // EOF
        val EmptyToken = SyntaxToken(TokenKind.EmptyToken, "null")

        val eq = SyntaxToken(TokenKind.EqualsToken,"=")
        val gt = SyntaxToken(TokenKind.GT,">")
        val lt = SyntaxToken(TokenKind.LT, "<")
        val ge = SyntaxToken(TokenKind.GE, ">=")
        val le = SyntaxToken(TokenKind.LE, "<=")
        val lsparen = SyntaxToken(TokenKind.OpenParenToken, "(")
        val rsparen = SyntaxToken(TokenKind.ClosedParenToken,")")
        val plus = SyntaxToken(TokenKind.PlusToken,"+")
        val minus = SyntaxToken(TokenKind.MinusToken,"-")
        val times = SyntaxToken(TokenKind.StarToken,"*")
        val div = SyntaxToken(TokenKind.SlashToken,"/")
        val assign = SyntaxToken(TokenKind.ASSIGN,":=")
        val semi = SyntaxToken(TokenKind.SemiToken,";")
        val mod = SyntaxToken(TokenKind.MOD,"%")
        val lbracket = SyntaxToken(TokenKind.LBRACKET,"[")
        val rbracket = SyntaxToken(TokenKind.RBRACKET,"]")
        val comma = SyntaxToken(TokenKind.COMMA,",")
        val dot = SyntaxToken(TokenKind.DOT,".")
        val ampersandampersand = SyntaxToken(TokenKind.AmpersandAmpersandToken, "&&")
        val pipepipe = SyntaxToken(TokenKind.PipePipeToken, "||")
        val bang = SyntaxToken(TokenKind.BangToken, "!")
        val nequals = SyntaxToken(TokenKind.NotEqualsToken, "!=")
        val dotdot = SyntaxToken(TokenKind.DotDotToken, "..")
    }

    // 返回一个带有row和col的SyntaxToken复制
    fun copy(row: Int, col: Int):SyntaxToken {
        return SyntaxToken(kind, text, value, row, col)
    }

    /**
     * 输出token的kind
     */
    override fun toString(): String {
        return "<${this.kind}>"
    }
}