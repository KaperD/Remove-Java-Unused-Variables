package ru.hse

import com.github.javaparser.JavaParser
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.ParserConfiguration.LanguageLevel
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName
import com.github.javaparser.ast.visitor.ModifierVisitor
import com.github.javaparser.ast.visitor.Visitable
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter
import com.github.javaparser.resolution.Resolvable
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserVariableDeclaration
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver

class JavaUnusedVarsRemover {
    private val languageLevel: LanguageLevel = LanguageLevel.BLEEDING_EDGE

    fun removeUnused(sourceCode: String, saveOriginalLayout: Boolean): Result<String> {
        val parser = JavaParser(
            ParserConfiguration()
                .setLanguageLevel(languageLevel)
                .setSymbolResolver(JavaSymbolSolver(CombinedTypeSolver()))
        )
        val parseResult = parser.parse(sourceCode)
        if (parseResult.result.isEmpty) {
            return Result.failure(IllegalArgumentException("Wrong Java source code"))
        }
        val cu = parseResult.result.get()
        if (saveOriginalLayout) {
            LexicalPreservingPrinter.setup(cu)
        }
        val usedVariables = cu.usedVariables()
        cu.accept(object : ModifierVisitor<Void>() {
            override fun visit(n: VariableDeclarator, arg: Void?): Visitable? {
                if (!usedVariables.contains(VariableDeclaratorWrapper(n))) {
                    return null
                }
                return super.visit(n, arg)
            }
        }, null)
        return if (saveOriginalLayout) {
            Result.success(LexicalPreservingPrinter.print(cu))
        } else {
            Result.success(cu.toString())
        }
    }

    private fun Node.usedVariables(): Set<VariableDeclaratorWrapper> {
        val used = mutableSetOf<VariableDeclaratorWrapper>()
        accept(object : VoidVisitorAdapter<Void>() {
            override fun visit(n: NameExpr, arg: Void?) {
                super.visit(n, arg)
                visit(n)
            }

            override fun visit(n: FieldAccessExpr, arg: Void?) {
                super.visit(n, arg)
                visit(n)
            }

            fun <T> visit(n: T)
                    where T : Resolvable<out ResolvedValueDeclaration>,
                          T : NodeWithSimpleName<*> {
                try {
                    val resolved = n.resolve()
                    if (resolved is JavaParserVariableDeclaration) {
                        used.add(VariableDeclaratorWrapper(resolved.variableDeclarator))
                    }
                    if (resolved is JavaParserFieldDeclaration) {
                        used.add(VariableDeclaratorWrapper(resolved.variableDeclarator))
                    }
                } catch (ignored: Exception) {
                    // We can't resolve static vars from other files, for example. Will ignore it
                }
            }
        }, null)
        return used
    }

    /**
     * Used to compare VariableDeclarator by link
     */
    private class VariableDeclaratorWrapper(val variable: VariableDeclarator) {
        override fun equals(other: Any?): Boolean {
            if (other !is VariableDeclaratorWrapper) return false
            return variable === other.variable
        }

        override fun hashCode(): Int {
            return variable.hashCode()
        }
    }
}
