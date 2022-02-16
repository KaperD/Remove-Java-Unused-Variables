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
import com.github.javaparser.resolution.UnsolvedSymbolException
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
        try {
            cu.accept(object : ModifierVisitor<Void>() {
                override fun visit(n: VariableDeclarator, arg: Void?): Visitable? {
                    if (!cu.uses(n)) {
                        n.remove()
                    }
                    return super.visit(n, arg)
                }
            }, null)
        } catch (e: UnsolvedSymbolException) {
            return Result.failure(e)
        }
        return if (saveOriginalLayout) {
            Result.success(LexicalPreservingPrinter.print(cu))
        } else {
            Result.success(cu.toString())
        }
    }

    private fun Node.uses(variable: VariableDeclarator): Boolean {
        var uses = false
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
                if (n.nameAsString == variable.nameAsString) {

                    val resolved = n.resolve()
                    if (resolved is JavaParserVariableDeclaration) {
                        if (resolved.variableDeclarator === variable) {
                            uses = true
                        }
                    }
                    if (resolved is JavaParserFieldDeclaration) {
                        if (resolved.variableDeclarator === variable) {
                            uses = true
                        }
                    }
                }
            }
        }, null)
        return uses
    }
}
