package mekanism;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic.Kind;

/**
 * Provides access to the {@link mekanism.common.inventory.container.sync.dynamic.ContainerSync} annotation data from the {@link AnnotationMirror}
 */
class ContainerSyncProxy {

    final String getter;
    final String setter;
    final List<String> tags;

    private ContainerSyncProxy(String getter, String setter, List<String> tags) {
        this.getter = getter;
        this.setter = setter;
        this.tags = tags;
    }

    private static final List<String> DEFAULT_TAG = Collections.singletonList("default");

    static ContainerSyncProxy from(Messager messager, VariableElement field) {
        AnnotationMirror annotationMirror = field.getAnnotationMirrors().stream().filter(mirror -> mirror.getAnnotationType().toString().equals(SyncMapperProccessor.CONTAINER_SYNC)).findFirst().orElse(null);
        if (annotationMirror == null) {
            messager.printMessage(Kind.ERROR, "Couldn't get Annotation info!", field);
            throw new NullPointerException("Couldn't get Annotation info! " + field);
        }
        String getter = null, setter = null;
        List<String> tags = DEFAULT_TAG;
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> element : annotationMirror.getElementValues().entrySet()) {
            String key = element.getKey().getSimpleName().toString();
            switch (key) {
                case "getter":
                    getter = element.getValue().getValue().toString();
                    if (getter.equals("")) {
                        getter = null;
                    }
                    break;
                case "setter":
                    setter = element.getValue().getValue().toString();
                    if (setter.equals("")) {
                        setter = null;
                    }
                    break;
                case "tags":
                    tags = ((List<?>) element.getValue().getValue()).stream().map(it -> {
                        //messager.printMessage(Kind.WARNING, "found "+it.getClass(), element.getKey());
                        AnnotationValue value = (AnnotationValue) it;
                        return value.getValue().toString();
                    }).collect(Collectors.toList());
                    break;
            }
        }
        return new ContainerSyncProxy(getter, setter, tags);
    }
}
