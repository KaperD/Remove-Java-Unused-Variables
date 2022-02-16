package ru.hse

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class JavaUnusedVarsRemoverTest {
    @Test
    fun `test simple`() {
        testSuccess(
            """
                public class Main {
                    int x;
                
                    void f() {
                        int x = 3;
                        int y = x;
                        int z = this.x;
                    }
                }
                
            """.trimIndent(),
            """
                public class Main {
                
                    int x;
                
                    void f() {
                        int x = 3;
                    }
                }
                
            """.trimIndent()
        )
    }

    @Test
    fun `test use shadowed`() {
        testSuccess(
            """
                public class Main {
                    public int x;
                
                    void f() {
                        int x = 3;
                        class A {
                        
                            int x = 4;
                            
                            fun f() {
                                System.out.println(x);
                            }
                        }
                    }
                }
                
            """.trimIndent(),
            """
                public class Main {
                
                    void f() {
                        class A {
                
                            int x = 4;
                
                            fun f() {
                                System.out.println(x);
                            }
                        }
                    }
                }
                
            """.trimIndent()
        )
    }

    @Test
    fun `test field used by inner class`() {
        testSuccess(
            """
                public class Main {
                    int x = 1;
                
                    class A {
                
                        int x;
                
                        void f() {
                            int x = 3;
                            System.out.println(Main.this.x);
                        }
                    }
                }
                
            """.trimIndent(),
            """
                public class Main {
                
                    int x = 1;
                
                    class A {
                
                        void f() {
                            System.out.println(Main.this.x);
                        }
                    }
                }
                
            """.trimIndent()
        )
    }

    @Test
    fun `test multiple`() {
        testSuccess(
            """
                public class Main {
                    private int a,b,c = 4;
                
                    void f() {
                        Integer x = 3, y = 2, z = 8;
                        String s = y.toString() + b;
                        System.out.println(s);
                    }
                }
                
            """.trimIndent(),
            """
                public class Main {
                
                    private int b;
                
                    void f() {
                        Integer y = 2;
                        String s = y.toString() + b;
                        System.out.println(s);
                    }
                }
                
            """.trimIndent()
        )
    }

    @Test
    fun `test same name`() {
        testSuccess(
            """
                public class Main {
                    int x;
                
                    void f() {
                        int x = 1;
                    }
                
                    void g() {
                        int x = 1;
                        System.out.println(x);
                    }
                }
                
            """.trimIndent(),
            """
                public class Main {
                
                    void f() {
                    }
                
                    void g() {
                        int x = 1;
                        System.out.println(x);
                    }
                }
                
            """.trimIndent()
        )
    }

    @Test
    fun `test used to read`() {
        val sourceCode = """
                public class Main {
                
                    int y;
                
                    void f() {
                        int x = 3;
                        int b = x + y;
                        g(b);
                    }
                
                    void g(int a) {
                    }
                }
                
            """.trimIndent()
        testSuccess(
            sourceCode,
            sourceCode
        )
    }

    @Test
    fun `test used to write`() {
        val sourceCode = """
                public class Main {
                
                    int y = 4;
                
                    void f() {
                        int x = 3;
                        x = 4;
                        y = 3;
                    }
                }
                
            """.trimIndent()
        testSuccess(
            sourceCode,
            sourceCode
        )
    }

    @Test
    fun `test used in function call`() {
        val sourceCode = """
                public class Main {
                
                    int y = 3;
                
                    void f() {
                        int x = 3;
                        g(x);
                        g(y);
                    }
                
                    void g(int a) {
                    }
                }
                
            """.trimIndent()
        testSuccess(
            sourceCode,
            sourceCode
        )
    }

    @Test
    fun `test used in class`() {
        val sourceCode = """
                public class Main {
                
                    int y = 4;
                
                    void f() {
                        int x = 3;
                        class A {
                
                            void g() {
                                System.out.println(x);
                                System.out.println(y);
                            }
                        }
                    }
                }
                
            """.trimIndent()
        testSuccess(
            sourceCode,
            sourceCode
        )
    }

    @Test
    fun `test used in lambda`() {
        val sourceCode = """
                import java.util.function.Supplier;
                
                public class Main {
                
                    int y;
                
                    void f() {
                        int x = 3;
                        Supplier<Integer> r = () -> x + y;
                        r.get();
                    }
                }
                
            """.trimIndent()
        testSuccess(
            sourceCode,
            sourceCode
        )
    }

    @Test
    fun `test used to call method`() {
        val sourceCode = """
                import java.util.function.Supplier;
                
                public class Main {
                
                    Supplier<Integer> y = () -> 3;
                
                    void f() {
                        Supplier<Integer> x = () -> 3;
                        x.get();
                        y.get();
                    }
                }
                
            """.trimIndent()
        testSuccess(
            sourceCode,
            sourceCode
        )
    }

    @Test
    fun `test wrong java code`() {
        testSuccess(
            """
                public class Main {
                
                    void f() {
                        int x = 3;
                        class A {
                            fun f() {
                                System.out.println(Main.this.x);
                            }
                        }
                    }
                }
                
            """.trimIndent(),
            """
                public class Main {
                
                    void f() {
                        class A {
                
                            fun f() {
                                System.out.println(Main.this.x);
                            }
                        }
                    }
                }
                
            """.trimIndent()
        )
    }

    @Test
    fun `test another wrong java code`() {
        testSuccess(
            """
                public class Main {
                
                    void f() {
                        a = b;
                        class A {
                            int a;
                        }
                    }
                }
                
            """.trimIndent(),
            """
                public class Main {
                
                    void f() {
                        a = b;
                        class A {
                        }
                    }
                }
                
            """.trimIndent()
        )
    }

    private fun testSuccess(sourceCode: String, expected: String) {
        val remover = JavaUnusedVarsRemover()
        val actual = remover.removeUnused(sourceCode, false)
        assertTrue(actual.isSuccess)
        assertEquals(expected, actual.getOrThrow())
    }
}
