package mekanism;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

@SupportedAnnotationTypes(SyncMapperProccessor.CONTAINER_SYNC)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
//@AutoService(Processor.class)
public class SyncMapperProccessor extends AbstractProcessor {

    public static final String CONTAINER_SYNC = "mekanism.common.inventory.container.sync.dynamic.ContainerSync";
    public static final String GENERATED_CLASS_SUFFIX = "__SyncMapper";
    public static final String SYNC_PACKAGE = "mekanism.common.inventory.container.sync";

    private Messager messager;
    private Elements elementUtils;
    private Types typeUtils;

    private TypeMirror iExtendedFluidTank;
    private TypeMirror iGasTank;
    private TypeMirror iSyncableData;
    private TypeMirror iEnergyContainer;

    private TypeElement iClassSyncer;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();

        iExtendedFluidTank = elementUtils.getTypeElement("mekanism.api.fluid.IExtendedFluidTank").asType();
        iGasTank = elementUtils.getTypeElement("mekanism.api.chemical.gas.IGasTank").asType();
        iSyncableData = elementUtils.getTypeElement("mekanism.common.inventory.container.sync.ISyncableData").asType();
        iEnergyContainer = elementUtils.getTypeElement("mekanism.api.energy.IEnergyContainer").asType();
        iClassSyncer = Objects.requireNonNull(elementUtils.getTypeElement("mekanism.common.inventory.container.sync.IClassSyncer"));
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //System.err.println("starting annotations proccessor");



        for (TypeElement annotation : annotations) {
            if (!annotation.getQualifiedName().contentEquals(CONTAINER_SYNC)) {
                continue;
            }
            Multimap<TypeElement, VariableElement> annotatedElements = HashMultimap.create();
            Set<VariableElement> fields = ElementFilter.fieldsIn(roundEnv.getElementsAnnotatedWith(annotation));
            for (VariableElement field : fields) {
                annotatedElements.put((TypeElement) field.getEnclosingElement(), field);
            }

            Filer filer = processingEnv.getFiler();
            ParameterSpec consumerParam = ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Consumer.class), ClassName.get(iSyncableData)), "consumer").build();

            for (TypeElement containingClass : annotatedElements.keySet()) {
                try {
                    ClassName containingClassName = ClassName.get(containingClass);
                    ParameterSpec valueParam = ParameterSpec.builder(containingClassName, "obj").build();
                    MethodSpec.Builder registerMethod = MethodSpec.methodBuilder("register")
                          .addModifiers(Modifier.PUBLIC).addAnnotation(Override.class)
                          .returns(void.class)
                          .addParameter(valueParam)
                          .addParameter(consumerParam);

                    TypeSpec.Builder builderClass = TypeSpec.classBuilder(containingClassName.peerClass(containingClassName.simpleName()+GENERATED_CLASS_SUFFIX))
                          .addModifiers(Modifier.PUBLIC, Modifier.FINAL).addSuperinterface(ParameterizedTypeName.get(ClassName.get(iClassSyncer), containingClassName));

                    for (VariableElement syncItem : annotatedElements.get(containingClass)) {
                        registerMethod.addStatement("$N.accept($L)", consumerParam, processField(syncItem, valueParam));
                    }

                    builderClass.addMethod(registerMethod.build());

                    JavaFile javaFile = JavaFile.builder(containingClassName.packageName(), builderClass.build())
                          .build();

                    javaFile.writeTo(filer);
                } catch (IOException e) {
                    messager.printMessage(Kind.ERROR, e.toString());
                }
            }
        }

        return true;
    }

    private String makeMethodReference(ParameterSpec valueParam, String methodName) {
        return makeMethodReference(valueParam.name, methodName);
    }
    private String makeMethodReference(String valueParam, String methodName) {
        return String.format("%s::%s", valueParam, methodName);
    }

    private CodeBlock processField(VariableElement field, ParameterSpec valueParam) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        TypeMirror fieldType = field.asType();

        Name fieldSimpleName = field.getSimpleName();
        String getter = String.format("()->%s.%s", valueParam.name, fieldSimpleName);
        String setter = String.format("newValue->%s.%s = newValue", valueParam.name, fieldSimpleName);

        AnnotationMirror annotationMirror = field.getAnnotationMirrors().stream().filter(mirror->mirror.getAnnotationType().toString().equals(CONTAINER_SYNC)).findFirst().orElse(null);
        if (annotationMirror == null) {
            messager.printMessage(Kind.ERROR, "Couldn't get Annotation info!", field);
            return codeBlock.add("null").build();
        }
        Map<String, String> annotationValues = getAnnotationValues(annotationMirror);
        if (annotationValues.containsKey("getter")) {
            getter = makeMethodReference(valueParam, annotationValues.get("getter"));
        }
        if (annotationValues.containsKey("setter")) {
            setter = makeMethodReference(valueParam, annotationValues.get("setter"));
        }
        //todo tags

        if (field.getModifiers().contains(Modifier.PRIVATE) && !(annotationValues.containsKey("getter") && annotationValues.containsKey("setter"))) {
            messager.printMessage(Kind.ERROR, "Getter and Setter required for private fields. Alternatively, use protected modifier", field);
        }

        TypeKind fieldKind = fieldType.getKind();
        switch (fieldKind){
            case BOOLEAN:
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
                basicSyncer(codeBlock, fieldKind.name().charAt(0)+(fieldKind.name().substring(1).toLowerCase(Locale.ROOT)), getter, setter);
                break;
            //case ARRAY:
            //    break;
            case DECLARED:
                String typeName = fieldType.toString();
                switch (typeName) {
                    case "net.minecraft.item.ItemStack":
                    case "net.minecraftforge.fluids.FluidStack":
                    case "mekanism.api.chemical.gas.GasStack":
                    case "mekanism.api.chemical.infuse.InfusionStack":
                    case "mekanism.api.chemical.pigment.PigmentStack":
                    case "mekanism.api.chemical.slurry.SlurryStack":
                    case "mekanism.common.lib.frequency.Frequency":
                    case "net.minecraft.util.math.BlockPos":
                    case "mekanism.api.math.FloatingLong":
                        basicSyncer(codeBlock, typeName.substring(typeName.lastIndexOf(".")+1), getter, setter);
                        break;
                    default:
                        Element fieldTypeEl = typeUtils.asElement(fieldType);
                        if (fieldTypeEl != null && fieldTypeEl.getKind() == ElementKind.ENUM) {
                            TypeElement syncableEnum = getSyncableTypeEl("Enum");
                            CodeBlock decoder = CodeBlock.of("idx->$T.values()[idx]", fieldType);
                            CodeBlock defaultVal = CodeBlock.of("$T.values()[0]", fieldType);
                            if (fieldTypeEl instanceof TypeElement) {
                                TypeElement enumType = (TypeElement) fieldTypeEl;
                                Element firstConstant = enumType.getEnclosedElements().stream().filter(element -> element.getKind()==ElementKind.ENUM_CONSTANT).findFirst().orElse(null);
                                if (firstConstant != null) {
                                    defaultVal = CodeBlock.of("$T.$L", fieldType, firstConstant);
                                }
                                Element byIndexStatic = ElementFilter.methodsIn(enumType.getEnclosedElements()).stream().filter(element->element.getSimpleName().contentEquals("byIndexStatic") && element.getModifiers().contains(Modifier.STATIC)).findFirst().orElse(null);
                                if (byIndexStatic != null) {
                                    decoder = CodeBlock.of("$T::byIndexStatic", fieldType);
                                }
                            }
                            codeBlock.add("$T.create($L, $L, $L, $L)", ClassName.get(syncableEnum), decoder, defaultVal, getter, setter);
                        }
                        else if (typeUtils.isAssignable(fieldType, iExtendedFluidTank)) {
                            getter = makeMethodReference(valueParam.name+"." + fieldSimpleName, "getFluid");
                            setter = makeMethodReference(valueParam.name+"." + fieldSimpleName, "setStack");
                            basicSyncer(codeBlock, "FluidStack", getter, setter);
                        } else if (typeUtils.isAssignable(fieldType, iGasTank)) {
                            getter = makeMethodReference(valueParam.name+"." + fieldSimpleName, "getStack");
                            setter = makeMethodReference(valueParam.name+"." + fieldSimpleName, "setStack");
                            basicSyncer(codeBlock, "GasStack", getter, setter);
                        } else if (typeUtils.isAssignable(fieldType, iEnergyContainer)) {
                            getter = makeMethodReference(valueParam.name+"." + fieldSimpleName, "getEnergy");
                            setter = makeMethodReference(valueParam.name+"." + fieldSimpleName, "setEnergy");
                            basicSyncer(codeBlock, "FloatingLong", getter, setter);
                        } else {
                            messager.printMessage(Kind.WARNING, "TODO Declared: " + fieldType.toString(), field);
                            codeBlock.add("null /*TODO Declared: " + fieldType.toString() + " */");
                        }
                        break;
                }
                break;
            //case TYPEVAR:
            //    break;
            //case WILDCARD:
            //    break;
            default:
                messager.printMessage(Kind.ERROR, "Unhandled field type: " + fieldKind.name(), field);
        }
        return codeBlock.build();
    }

    private Map<String, String> getAnnotationValues(AnnotationMirror annotationMirror){
        Map<String,String> output = new HashMap<>();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> element : annotationMirror.getElementValues().entrySet()) {
            String key = element.getKey().getSimpleName().toString();
            if (key.equals("tags")){
                output.put("tags", ((List<?>)element.getValue().getValue()).stream().map(it->it.toString()).collect(Collectors.joining(",")));//todo tags
            }
            output.put(key, element.getValue().getValue().toString());
        }
        return output;
    }

    private void basicSyncer(CodeBlock.Builder codeBlock, String type, String getter, String setter) {
        TypeElement typeEl = getSyncableTypeEl(type);
        codeBlock.add("$T.create($L, $L)", typeEl, getter, setter);
        //"Syncable"+ type + ".create(" + getter + ", " + setter + ")";
    }

    private TypeElement getSyncableTypeEl(String type) {
        TypeElement typeEl = elementUtils.getTypeElement(SYNC_PACKAGE + ".Syncable" + type);
        if (typeEl == null) {
            typeEl = elementUtils.getTypeElement(SYNC_PACKAGE + ".chemical.Syncable" + type);// gas/pigment/slurry/infusion
        }
        if (typeEl == null) {
            messager.printMessage(Kind.ERROR, "Unable to locate Syncable" + type);
        }
        return typeEl;
    }
}
