package mekanism;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes(SyncMapperProccessor.CONTAINER_SYNC)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
//@AutoService(Processor.class)
public class SyncMapperProccessor extends AbstractProcessor {

    public static final String CONTAINER_SYNC = "mekanism.common.inventory.container.sync.dynamic.ContainerSync";
    public static final String GENERATED_CLASS_SUFFIX = "__SyncMapper";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //System.err.println("starting annotations proccessor");
        Messager messager = processingEnv.getMessager();
        Elements elementUtils = processingEnv.getElementUtils();
        Types typeUtils = processingEnv.getTypeUtils();
        TypeMirror iExtendedFluidTank = elementUtils.getTypeElement("mekanism.api.fluid.IExtendedFluidTank").asType();
        TypeMirror iGasTank = elementUtils.getTypeElement("mekanism.api.chemical.gas.IGasTank").asType();

        for (TypeElement annotation : annotations) {
            if (!annotation.getQualifiedName().contentEquals(CONTAINER_SYNC)) {
                continue;
            }
            Multimap<String, Pair<VariableElement, Supplier<String>>> annotatedElements = HashMultimap.create();
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
            Set<VariableElement> fields = ElementFilter.fieldsIn(elements);
            for (VariableElement field : fields) {
                TypeMirror fieldType = field.asType();
                Supplier<String> codeSupplier = null;
                String getter = "()->obj." + field.getSimpleName();
                String setter = "(value)->obj." + field.getSimpleName() + " = value";

                AnnotationMirror annotationMirror = field.getAnnotationMirrors().stream().filter(mirror->mirror.getAnnotationType().toString().equals(CONTAINER_SYNC)).findFirst().orElse(null);
                if (annotationMirror == null) {
                    messager.printMessage(Kind.ERROR, "Counldn't get Annotation info!", field);
                    continue;
                }
                Map<String, String> annotationValues = getAnnotationValues(annotationMirror);
                if (annotationValues.containsKey("getter")) {
                    getter = "obj::"+annotationValues.get("getter");
                }
                if (annotationValues.containsKey("setter")) {
                    setter = "obj::"+annotationValues.get("setter");
                }
                //todo tags

                if (field.getModifiers().contains(Modifier.PRIVATE) && !(annotationValues.containsKey("getter") && annotationValues.containsKey("setter"))) {
                    messager.printMessage(Kind.ERROR, "Getter and Setter required for private fields. Alternatively, use protected modifier", field);
                }

                switch (fieldType.getKind()){
                    case BOOLEAN:
                        codeSupplier = primitiveSyncer("Boolean", getter, setter);
                        break;
                    case BYTE:
                        codeSupplier = primitiveSyncer("Byte", getter, setter);
                        break;
                    case SHORT:
                        codeSupplier = primitiveSyncer("Short", getter, setter);
                        break;
                    case INT:
                        codeSupplier = primitiveSyncer("Int", getter, setter);
                        break;
                    case LONG:
                        codeSupplier = primitiveSyncer("Long", getter, setter);
                        break;
                    //case CHAR:
                    //    break;
                    case FLOAT:
                        codeSupplier = primitiveSyncer("Float", getter, setter);
                        break;
                    case DOUBLE:
                        codeSupplier = primitiveSyncer("Double", getter, setter);
                        break;
                    //case ARRAY:
                    //    break;
                    case DECLARED:
                        String typeName = fieldType.toString();
                        switch (typeName) {
                            case "net.minecraft.item.ItemStack":
                                codeSupplier = primitiveSyncer("ItemStack", getter, setter);
                                break;
                            case "net.minecraftforge.fluids.FluidStack":
                                codeSupplier = primitiveSyncer("FluidStack", getter, setter);
                                break;
                            case "mekanism.api.chemical.gas.GasStack":
                                codeSupplier = primitiveSyncer("GasStack", getter, setter);
                                break;
                            case "mekanism.api.chemical.infuse.InfusionStack":
                                codeSupplier = primitiveSyncer("InfusionStack", getter, setter);
                                break;
                            case "mekanism.api.chemical.pigment.PigmentStack":
                                codeSupplier = primitiveSyncer("PigmentStack", getter, setter);
                                break;
                            case "mekanism.api.chemical.slurry.SlurryStack":
                                codeSupplier = primitiveSyncer("SlurryStack", getter, setter);
                                break;
                            case "mekanism.common.lib.frequency.Frequency":
                                codeSupplier = primitiveSyncer("Frequency", getter, setter);
                                break;
                            case "net.minecraft.util.math.BlockPos":
                                codeSupplier = primitiveSyncer("BlockPos", getter, setter);
                                break;
                            case "mekanism.api.math.FloatingLong":
                                codeSupplier = primitiveSyncer("FloatingLong", getter, setter);
                                break;
                            default:
                                if (typeUtils.isAssignable(fieldType, iExtendedFluidTank)) {
                                    getter = "obj." + field.getSimpleName() + "::getFluid";
                                    setter = "obj." + field.getSimpleName() + "::setStack";
                                    codeSupplier = primitiveSyncer("FluidStack", getter, setter);
                                } else if (typeUtils.isAssignable(fieldType, iGasTank)) {
                                    getter = "obj." + field.getSimpleName() + "::getStack";
                                    setter = "obj." + field.getSimpleName() + "::setStack";
                                    codeSupplier = primitiveSyncer("GasStack", getter, setter);
                                } else {
                                    messager.printMessage(Kind.WARNING, "TODO Declared: " + fieldType.toString(), field);
                                    codeSupplier = () -> "null /*TODO Declared: " + fieldType.toString() + " */";
                                }
                                break;
                        }
                        break;
                    //case TYPEVAR:
                    //    break;
                    //case WILDCARD:
                    //    break;
                    default:
                        messager.printMessage(Kind.ERROR, "Unhandled field type: " + fieldType.getKind().name(), field);
                }
                if (codeSupplier != null) {
                    String containingClass = ((TypeElement) field.getEnclosingElement()).getQualifiedName().toString();
                    annotatedElements.put(containingClass, Pair.of(field, codeSupplier));
                }
            }

            Filer filer = processingEnv.getFiler();
            for (String containingClass : annotatedElements.keySet()) {
                try {
                    String packageName = null;
                    int lastDot = containingClass.lastIndexOf('.');
                    if (lastDot > 0) {
                        packageName = containingClass.substring(0, lastDot);
                    }

                    String simpleContainingClassName = containingClass.substring(lastDot + 1);
                    String mapperClassname = containingClass + GENERATED_CLASS_SUFFIX;
                    String builderSimpleClassName = mapperClassname.substring(lastDot + 1);

                    Collection<Pair<VariableElement, Supplier<String>>> syncItems = annotatedElements.get(containingClass);
                    JavaFileObject builderFile = filer.createSourceFile(mapperClassname, syncItems.stream().map(p->p.left).toArray(Element[]::new));
                    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
                        if (packageName != null) {
                            out.print("package ");
                            out.print(packageName);
                            out.println(";");
                            out.println();
                        }
                        out.println("import java.util.function.Consumer;");
                        out.println("import mekanism.common.inventory.container.sync.*;");
                        out.println("import mekanism.common.inventory.container.sync.chemical.*;");
                        out.println();

                        out.print("public class ");
                        out.print(builderSimpleClassName);
                        out.println(" {");
                        out.println();
                        out.print("    public static void register(");
                        out.print(simpleContainingClassName);
                        out.println(" obj, Consumer<ISyncableData> consumer) {");
                        for (Pair<VariableElement, Supplier<String>> syncItem : syncItems) {
                            out.println("      consumer.accept("+syncItem.right.get()+");");
                        }
                        out.println("  }");
                        out.println("}");
                    }
                } catch (IOException e) {
                    messager.printMessage(Kind.ERROR, e.toString());
                }
            }
        }

        return true;
    }

    private Map<String, String> getAnnotationValues(AnnotationMirror annotationMirror){
        Map<String,String> output = new HashMap<>();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> element : annotationMirror.getElementValues().entrySet()) {
            String key = element.getKey().getSimpleName().toString();
            if (key.equals("tags")){
                output.put("tags", ((List<Object>)element.getValue().getValue()).stream().map(it->it.toString()).collect(Collectors.joining(",")));//todo tags
            }
            output.put(key, element.getValue().getValue().toString());
        }
        return output;
    }

    private Supplier<String> primitiveSyncer(String type, String getter, String setter) {
        return () -> "Syncable"+ type + ".create(" + getter + ", " + setter + ")";
    }
}
