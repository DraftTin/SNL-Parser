package CodeAnalysis.syntax

class CombinedSyntax(kind: TokenKind, children: ArrayList<SyntaxNode>): SyntaxNode(kind) {
    override fun getChildren(): ArrayList<SyntaxNode> {
        return children
    }

    private var children = children
}