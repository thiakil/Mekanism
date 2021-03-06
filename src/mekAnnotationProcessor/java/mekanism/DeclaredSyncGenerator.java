package mekanism;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import java.util.function.Consumer;
import javax.lang.model.element.VariableElement;

/**
 * Provides special handling for complex types
 */
@FunctionalInterface
interface DeclaredSyncGenerator {
    void process(VariableElement field, ParameterSpec valueParam, Consumer<CodeBlock> statementBuilder);
}
