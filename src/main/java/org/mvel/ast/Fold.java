package org.mvel.ast;

import org.mvel.compiler.ExecutableStatement;
import org.mvel.integration.VariableResolverFactory;
import org.mvel.integration.impl.DefaultLocalVariableResolverFactory;
import org.mvel.integration.impl.ItemResolverFactory;
import org.mvel.util.FastList;
import org.mvel.util.ParseTools;
import static org.mvel.util.ParseTools.isWhitespace;
import static org.mvel.util.ParseTools.subCompileExpression;

import java.util.Collection;
import java.util.List;

public class Fold extends ASTNode {
    private ExecutableStatement subEx;
    private ExecutableStatement dataEx;
    private ExecutableStatement constraintEx;

    public Fold(char[] name) {
        this.name = name;

        System.out.println("FOLD<<" + new String(name) + ">>");

        int cursor = 0;
        for (; cursor < name.length; cursor++) {
            if (isWhitespace(name[cursor])) {
                while (cursor < name.length && isWhitespace(name[cursor])) cursor++;

                if (name[cursor] == 'i' && name[cursor + 1] == 'n' && ParseTools.isJunct(name[cursor + 2])) {
                    break;
                }
            }
        }

        subEx = (ExecutableStatement) subCompileExpression(ParseTools.subset(name, 0, cursor - 1));
        int start = cursor += 2; // skip 'in'

        for (; cursor < name.length; cursor++) {
            if (isWhitespace(name[cursor])) {
                while (cursor < name.length && isWhitespace(name[cursor])) cursor++;

                if (name[cursor] == 'i' && name[cursor + 1] == 'f' && ParseTools.isJunct(name[cursor + 2])) {
                    int s = cursor + 2;
                    char[] xx = ParseTools.subset(name, s, name.length - s);

                    System.out.println("<<" + new String(xx) + ">>");

                    constraintEx = (ExecutableStatement) subCompileExpression(xx);
                    break;
                }
            }
        }

        dataEx = (ExecutableStatement) subCompileExpression(ParseTools.subset(name, start, cursor - start));


    }

    public Object getReducedValueAccelerated(Object ctx, Object thisValue, VariableResolverFactory factory) {
        List list;

        if (constraintEx != null) {
            ItemResolverFactory.ItemResolver itemR = new ItemResolverFactory.ItemResolver("$");
            ItemResolverFactory itemFactory = new ItemResolverFactory(itemR, new DefaultLocalVariableResolverFactory(factory));

            Collection col = ((Collection) dataEx.getValue(ctx, thisValue, factory));
            list = new FastList(col.size());
            for (Object o : col) {
                itemR.setValue(o);
                if ((Boolean) constraintEx.getValue(ctx, thisValue, itemFactory)) {
                    list.add(subEx.getValue(o, thisValue, factory));
                }
            }
        }
        else {
            Collection col = ((Collection) dataEx.getValue(ctx, thisValue, factory));
            list = new FastList(col.size());
            for (Object o : col) {
                list.add(subEx.getValue(o, thisValue, factory));
            }
        }

        return list;
    }

    public Object getReducedValue(Object ctx, Object thisValue, VariableResolverFactory factory) {
        return getReducedValueAccelerated(ctx, thisValue, factory);
    }
}