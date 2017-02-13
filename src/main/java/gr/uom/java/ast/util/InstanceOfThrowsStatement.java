package gr.uom.java.ast.util;

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;

/**
 * Created by Ferooz on 10/02/17.
 */
public class InstanceOfThrowsStatement implements StatementInstanceChecker {

    public boolean instanceOf(Statement statement) {
        if(statement instanceof ThrowStatement)
            return true;
        else
            return false;
    }

}
