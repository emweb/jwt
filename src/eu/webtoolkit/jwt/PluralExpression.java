package eu.webtoolkit.jwt;

import org.antlr.v4.runtime.*;

final class PluralExpression {
    private PluralExpression() {
    }

    private static final class ErrorListener extends ConsoleErrorListener {
        private String lastError = "";

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer,
                                Object offendingSymbol,
                                int line,
                                int charPositionInLine,
                                String msg,
                                RecognitionException e) {
            lastError = "line " + line + ":" + charPositionInLine + " " + msg;
        }

        String getLastError() {
            return lastError;
        }
    }

    private static final class Visitor extends PluralExpressionBaseVisitor<Long> {
        Visitor(long n) {
            this.n = n;
        }
        
        @Override
        public Long visitStatement(PluralExpressionParser.StatementContext ctx) {
        	return visit(ctx.expression());
        }

        @Override
        public Long visitExpression(PluralExpressionParser.ExpressionContext ctx) {
            long res = visit(ctx.orExpression());
            if (ctx.expression().isEmpty()) {
                return res;
            } else {
                if (res != 0L) {
                    return visit(ctx.expression(0));
                } else {
                    return visit(ctx.expression(1));
                }
            }
        }

        @Override
        public Long visitOrExpression(PluralExpressionParser.OrExpressionContext ctx) {
        	if (ctx.andExpression().size() == 1) {
        		return visit(ctx.andExpression(0));
        	} else {
        		return ctx.andExpression().stream()
                    .map(this::visit)
                    .reduce(0L, (l, r) -> l != 0L || r != 0L ? 1L : 0L);
        	}
        }

        @Override
        public Long visitAndExpression(PluralExpressionParser.AndExpressionContext ctx) {
        	if (ctx.eqExpression().size() == 1) {
        		return visit(ctx.eqExpression(0));
        	} else {
        		return ctx.eqExpression().stream()
                    .map(this::visit)
                    .reduce(1L, (l, r) -> l != 0L && r != 0L ? 1L : 0L);
        	}
        }

        @Override
        public Long visitEqExpression(PluralExpressionParser.EqExpressionContext ctx) {
            long res = visit(ctx.relationalExpression(0));
            for (int i = 0; i < ctx.relationalExpression().size() - 1; ++i) {
                if (ctx.eqOperator(i).EQ() != null) {
                    res = (res == visit(ctx.relationalExpression(i + 1))) ? 1L : 0L;
                } else if (ctx.eqOperator(i).NEQ() != null) {
                    res = (res != visit(ctx.relationalExpression(i + 1))) ? 1L : 0L;
                }
            }
            return res;
        }

        @Override
        public Long visitRelationalExpression(PluralExpressionParser.RelationalExpressionContext ctx) {
            long res = visit(ctx.additiveExpression(0));
            for (int i = 0; i < ctx.additiveExpression().size() - 1; ++i) {
                if (ctx.cmpOperator(i).GT() != null) {
                    res = (res > visit(ctx.additiveExpression(i + 1))) ? 1L : 0L;
                } else if (ctx.cmpOperator(i).GTE() != null) {
                    res = (res >= visit(ctx.additiveExpression(i + 1))) ? 1L : 0L;
                } else if (ctx.cmpOperator(i).LT() != null) {
                    res = (res < visit(ctx.additiveExpression(i + 1))) ? 1L : 0L;
                } else if (ctx.cmpOperator(i).LTE() != null) {
                    res = (res <= visit(ctx.additiveExpression(i + 1))) ? 1L : 0L;
                }
            }
            return res;
        }

        @Override
        public Long visitAdditiveExpression(PluralExpressionParser.AdditiveExpressionContext ctx) {
            long res = visit(ctx.term(0));
            for (int i = 0; i < ctx.term().size() - 1; ++i) {
                if (ctx.sumOperator(i).PLUS() != null) {
                    res = res + visit(ctx.term(i + 1));
                } else if (ctx.sumOperator(i).MINUS() != null) {
                    res = res - visit(ctx.term(i + 1));
                }
            }
            return res;
        }

        @Override
        public Long visitTerm(PluralExpressionParser.TermContext ctx) {
            long res = visit(ctx.factor(0));
            for (int i = 0; i < ctx.factor().size() - 1; ++i) {
                if (ctx.prodOperator(i).MULT() != null) {
                    res = res * visit(ctx.factor(i + 1));
                } else if (ctx.prodOperator(i).DIV() != null) {
                    res = res / visit(ctx.factor(i + 1));
                } else if (ctx.prodOperator(i).MOD() != null) {
                    res = res % visit(ctx.factor(i + 1));
                }
            }
            return res;
        }

        @Override
        public Long visitGroup(PluralExpressionParser.GroupContext ctx) {
            return visit(ctx.expression());
        }

        @Override
        public Long visitLiteral(PluralExpressionParser.LiteralContext ctx) {
            return Long.valueOf(ctx.INTEGER().getText());
        }

        @Override
        public Long visitVariable(PluralExpressionParser.VariableContext ctx) {
            return getN();
        }

        public long getN() { return n; }

        private long n;
    }

    static int evalPluralCase(String pluralExpression, long amount) {
        ErrorListener errorListener = new ErrorListener();

        CharStream stream = CharStreams.fromString(pluralExpression);

        PluralExpressionLexer lex = new PluralExpressionLexer(stream);
        lex.removeErrorListener(ConsoleErrorListener.INSTANCE);
        lex.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lex);
        PluralExpressionParser parser = new PluralExpressionParser(tokens);
        parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
        parser.addErrorListener(errorListener);

        Visitor visitor = new Visitor(amount);
        long result = visitor.visit(parser.statement());

        if (!errorListener.getLastError().isEmpty()) {
            throw new WException("An error occurred processing plural expression " + pluralExpression +
                    " with n=" + amount + ": " + errorListener.getLastError());
        } else if (result < 0) {
            throw new WException("Expression '" + pluralExpression + "' evaluates to '" +
                    result + "' for n=" + amount + ", and values smaller than 0 are not allowed.");
        } else if (result > Integer.MAX_VALUE) {
            throw new WException("Expression '" + pluralExpression + "' evaluates to '" +
                    result + "' for n=" + amount + ", exceeding Integer.MAX_VALUE");
        }

        return (int)result;
    }
}
