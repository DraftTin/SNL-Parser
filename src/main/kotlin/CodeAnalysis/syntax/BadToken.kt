package CodeAnalysis.syntax

/**
 * 未识别的token
 * @param ch 对应未识别的字符
 */
class BadToken(val ch: Char, row: Int = 0, col: Int = 0): SyntaxToken(TokenKind.BadToken, ch.toString(), row = row, col = col) {
    override fun toString(): String {
        return "<BadToken: '$ch'>"
    }
}