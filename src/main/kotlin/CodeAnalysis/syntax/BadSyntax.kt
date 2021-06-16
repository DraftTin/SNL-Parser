package CodeAnalysis.syntax

class BadSyntax(var tok: SyntaxToken): SyntaxNode(TokenKind.BadSyntax) {
    override fun getChildren(): ArrayList<SyntaxNode> {
        return arrayListOf(tok)
    }
}