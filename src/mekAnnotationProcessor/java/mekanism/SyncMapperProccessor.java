package mekanism;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
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

    private TypeMirror iSyncableData;//consumer type
    private TypeElement iClassSyncer;//interface implementation

    private Map<TypeMirror, DeclaredSyncGenerator> declaredSyncGenerators;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();

        messager.printMessage(Kind.WARNING, "doing init");

        declaredSyncGenerators = new LinkedHashMap<>();
        addDeclaredSyncGenerator("mekanism.api.fluid.IExtendedFluidTank", (field, fieldSimpleName, valueParam, statementBuilder) -> {
            CodeBlock getter = CodeBlock.of("()->$N.$L.getFluid()", valueParam, field);
            CodeBlock setter = CodeBlock.of("newValue -> $N.$L.setStack(newValue)", valueParam, field);
            basicSyncer(statementBuilder, "FluidStack", getter, setter);
        });
        addDeclaredSyncGenerator("mekanism.api.chemical.gas.IGasTank", (field, fieldSimpleName, valueParam, statementBuilder) -> {
            CodeBlock getter = CodeBlock.of("()->$N.$L.getStack()", valueParam, field);
            CodeBlock setter = CodeBlock.of("newValue -> $N.$L.setStack(newValue)", valueParam, field);
            basicSyncer(statementBuilder, "GasStack", getter, setter);
        });
        addDeclaredSyncGenerator("mekanism.api.energy.IEnergyContainer", (field, fieldSimpleName, valueParam, statementBuilder) -> {
            CodeBlock getter = CodeBlock.of("()->$N.$L.getEnergy()", valueParam, field);
            CodeBlock setter = CodeBlock.of("newValue -> $N.$L.setEnergy(newValue)", valueParam, field);
            basicSyncer(statementBuilder, "FloatingLong", getter, setter);
        });
        addDeclaredSyncGenerator("mekanism.common.capabilities.heat.BasicHeatCapacitor", (field, fieldSimpleName, valueParam, statementBuilder) -> {
            basicSyncer(statementBuilder, "Double",
                  CodeBlock.of("()->$N.$L.getHeatCapacity()", valueParam, field),
                  CodeBlock.of("newValue -> $N.$L.setHeatCapacityFromPacket(newValue)", valueParam, field)
            );
            basicSyncer(statementBuilder, "Double",
                  CodeBlock.of("()->$N.$L.getHeat()", valueParam, field),
                  CodeBlock.of("newValue -> $N.$L.setHeat(newValue)", valueParam, field)
            );
        });
        addDeclaredSyncGenerator("mekanism.common.capabilities.merged.MergedTank", (field, fieldSimpleName, valueParam, statementBuilder) -> {
            basicSyncer(statementBuilder, "FluidStack",
                  CodeBlock.of("()->$N.$L.getFluidTank().getFluid()", valueParam, field),
                  CodeBlock.of("newValue -> $N.$L.getFluidTank().setStack(newValue)", valueParam, field)
            );
            basicSyncer(statementBuilder, "GasStack",
                  CodeBlock.of("()->$N.$L.getGasTank().getStack()", valueParam, field),
                  CodeBlock.of("newValue -> $N.$L.getGasTank().setStack(newValue)", valueParam, field)
            );
            basicSyncer(statementBuilder, "InfusionStack",
                  CodeBlock.of("()->$N.$L.getInfusionTank().getStack()", valueParam, field),
                  CodeBlock.of("newValue -> $N.$L.getInfusionTank().setStack(newValue)", valueParam, field)
            );
            basicSyncer(statementBuilder, "PigmentStack",
                  CodeBlock.of("()->$N.$L.getPigmentTank().getStack()", valueParam, field),
                  CodeBlock.of("newValue -> $N.$L.getPigmentTank().setStack(newValue)", valueParam, field)
            );
            basicSyncer(statementBuilder, "SlurryStack",
                  CodeBlock.of("()->$N.$L.getSlurryTank().getStack()", valueParam, field),
                  CodeBlock.of("newValue -> $N.$L.getSlurryTank().setStack(newValue)", valueParam, field)
            );
        });
        addDeclaredSyncGenerator("mekanism.api.chemical.merged.MergedChemicalTank", (field, fieldSimpleName, valueParam, statementBuilder) -> {
            basicSyncer(statementBuilder, "GasStack",
                  CodeBlock.of("()->$N.$L.getGasTank().getStack()", valueParam, field),
                  CodeBlock.of("newValue -> $N.$L.getGasTank().setStack(newValue)", valueParam, field)
            );
            basicSyncer(statementBuilder, "InfusionStack",
                  CodeBlock.of("()->$N.$L.getInfusionTank().getStack()", valueParam, field),
                  CodeBlock.of("newValue -> $N.$L.getInfusionTank().setStack(newValue)", valueParam, field)
            );
            basicSyncer(statementBuilder, "PigmentStack",
                  CodeBlock.of("()->$N.$L.getPigmentTank().getStack()", valueParam, field),
                  CodeBlock.of("newValue -> $N.$L.getPigmentTank().setStack(newValue)", valueParam, field)
            );
            basicSyncer(statementBuilder, "SlurryStack",
                  CodeBlock.of("()->$N.$L.getSlurryTank().getStack()", valueParam, field),
                  CodeBlock.of("newValue -> $N.$L.getSlurryTank().setStack(newValue)", valueParam, field)
            );
        });
        addDeclaredSyncGenerator("mekanism.common.lib.math.voxel.VoxelCuboid", (field, fieldSimpleName, valueParam, statementBuilder) -> {
            basicSyncer(statementBuilder, "BlockPos",
                  CodeBlock.of("()->$N.$L.getMinPos()", valueParam, field),
                  CodeBlock.of("newValue -> $N.$L.setMinPos(newValue)", valueParam, field)
            );
            basicSyncer(statementBuilder, "BlockPos",
                  CodeBlock.of("()->$N.$L.getMaxPos()", valueParam, field),
                  CodeBlock.of("newValue -> $N.$L.setMaxPos(newValue)", valueParam, field)
            );
        });

        iSyncableData = elementUtils.getTypeElement("mekanism.common.inventory.container.sync.ISyncableData").asType();
        iClassSyncer = Objects.requireNonNull(elementUtils.getTypeElement("mekanism.common.inventory.container.sync.IClassSyncer"));
    }

    private void addDeclaredSyncGenerator(String applicableType, DeclaredSyncGenerator generator) {
        TypeMirror typeMirror = Objects.requireNonNull(elementUtils.getTypeElement(applicableType), ()->"Couldn't get "+applicableType).asType();
        declaredSyncGenerators.put(typeMirror, generator);
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
            ClassName classSyncer = ClassName.get(iClassSyncer);

            //set up some things to create a registry
            FieldSpec registryField = FieldSpec.builder(
                  ParameterizedTypeName.get(
                        ClassName.get(Map.class),
                        ClassName.get(Class.class),
                        classSyncer
                  ), "REGISTRY", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL
            ).initializer("new $T<>()", HashMap.class).build();
            CodeBlock.Builder registryInitialiser = CodeBlock.builder();

            //common params for register() which don't vary
            ParameterSpec consumerParam = ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Consumer.class), ClassName.get(iSyncableData)), "consumer").build();
            ParameterSpec tagParam = ParameterSpec.builder(String.class, "tag").build();

            for (TypeElement containingClass : annotatedElements.keySet()) {
                try {
                    ClassName containingClassName = ClassName.get(containingClass);

                    //build base type
                    ClassName builderClassName = getBuilderClassName(containingClassName);
                    TypeSpec.Builder builderClass = TypeSpec.classBuilder(builderClassName)
                          .addModifiers(Modifier.PUBLIC, Modifier.FINAL).addSuperinterface(ParameterizedTypeName.get(classSyncer, containingClassName));

                    //add singleton instance
                    builderClass.addField(FieldSpec.builder(builderClassName, "INSTANCE", Modifier.FINAL, Modifier.PUBLIC, Modifier.STATIC).initializer("new $T()", builderClassName).build());

                    //build register method
                    ParameterSpec valueParam = ParameterSpec.builder(containingClassName, "obj").build();
                    MethodSpec.Builder registerMethod = MethodSpec.methodBuilder("register")
                          .addModifiers(Modifier.PUBLIC).addAnnotation(Override.class)
                          .returns(void.class)
                          .addParameter(valueParam)
                          .addParameter(consumerParam)
                          .addParameter(tagParam);

                    //Check if we need to pass to a parent class
                    TypeElement parentContainer = findApplicableParent(containingClass, annotatedElements.keySet());
                    if (parentContainer != null) {
                        registerMethod.addStatement("$T.INSTANCE.register($N, $N, $N)", getBuilderClassName(ClassName.get(parentContainer)), valueParam, consumerParam, tagParam);
                    }

                    //sort tags into CodeBlocks
                    Map<String, CodeBlock.Builder> tags = new LinkedHashMap<>();
                    tags.put("default", CodeBlock.builder());//make sure default is first
                    for (VariableElement syncItem : annotatedElements.get(containingClass)) {
                        ContainerSyncProxy containerSyncAnnotation = ContainerSyncProxy.from(messager, syncItem);
                        for (String tag : containerSyncAnnotation.tags) {
                            processField(syncItem, containerSyncAnnotation, valueParam, codeBlock -> tags.computeIfAbsent(tag, k->CodeBlock.builder()).addStatement("$N.accept($L)", consumerParam, codeBlock));
                        }
                    }

                    //output all the tags and their relevant syncers
                    for (Entry<String, Builder> tagElement : tags.entrySet()) {
                        String tag = tagElement.getKey();
                        CodeBlock.Builder builder = tagElement.getValue();
                        if (!builder.isEmpty()) {
                            registerMethod.addCode(
                                  CodeBlock.builder()
                                        .beginControlFlow("if ($N.equals($S))", tagParam, tag)
                                        .add(builder.build())
                                        .addStatement("return")
                                        .endControlFlow()
                                  .build()
                            );
                        }
                    }

                    //finish up
                    builderClass.addMethod(registerMethod.build());

                    JavaFile javaFile = JavaFile.builder(containingClassName.packageName(), builderClass.build())
                          .build();

                    javaFile.writeTo(filer);

                    registryInitialiser.addStatement("$N.put($T.class, $T.INSTANCE)", registryField, containingClassName, builderClassName);
                } catch (IOException e) {
                    messager.printMessage(Kind.ERROR, e.toString());
                }
            }

            //build registry class
            TypeSpec registryClass = TypeSpec.classBuilder("ContainerSyncRegistry")
                  .addModifiers(Modifier.PUBLIC)
                  .addField(
                        registryField
                  ).addInitializerBlock(registryInitialiser.build()).build();
            try {
                JavaFile.builder("mekanism", registryClass).build().writeTo(filer);
            } catch (IOException e) {
                messager.printMessage(Kind.ERROR, e.toString());
            }
        }

        return true;
    }

    private static ClassName getBuilderClassName(ClassName containerClassName) {
        return containerClassName.peerClass(containerClassName.simpleName() + GENERATED_CLASS_SUFFIX);
    }

    //@Nullable
    private TypeElement findApplicableParent(TypeElement current, Set<TypeElement> allTypes) {
        TypeMirror parentMirror = current.getSuperclass();
        if (parentMirror.getKind() == TypeKind.NONE) {
            return null;
        }
        Element element = typeUtils.asElement(parentMirror);
        if (!(element instanceof TypeElement)) {
            messager.printMessage(Kind.WARNING, "Parent isn't a TypeElement... "+parentMirror, element);
            return null;
        }
        TypeElement parentEl = (TypeElement) element;
        return allTypes.stream().filter(el->typeUtils.isSameType(parentMirror, el.asType())).findFirst().orElseGet(()->findApplicableParent(parentEl, allTypes));
    }

    private void processField(VariableElement field, ContainerSyncProxy containerSyncAnnotation, ParameterSpec valueParam, Consumer<CodeBlock> statementBuilder) {
        TypeMirror fieldType = field.asType();

        Name fieldSimpleName = field.getSimpleName();
        CodeBlock getter = CodeBlock.of("()->$N.$L", valueParam, field);
        CodeBlock setter = CodeBlock.of("newValue->$N.$L = newValue", valueParam, field);

        if (containerSyncAnnotation.getter != null) {
            getter = CodeBlock.of("$N::$L", valueParam, containerSyncAnnotation.getter);
        }
        if (containerSyncAnnotation.setter != null) {
            setter = CodeBlock.of("$N::$L", valueParam, containerSyncAnnotation.setter);
        }
        //todo tags

        if (field.getModifiers().contains(Modifier.PRIVATE) && (containerSyncAnnotation.getter == null || containerSyncAnnotation.setter == null)) {
            messager.printMessage(Kind.ERROR, "Getter and Setter required for private fields. Alternatively, use protected/package-private modifier", field);
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
                basicSyncer(statementBuilder, fieldKind.name().charAt(0)+(fieldKind.name().substring(1).toLowerCase(Locale.ROOT)), getter, setter);
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
                        basicSyncer(statementBuilder, typeName.substring(typeName.lastIndexOf(".")+1), getter, setter);
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
                            statementBuilder.accept(CodeBlock.of("$T.create($L, $L, $L, $L)", ClassName.get(syncableEnum), decoder, defaultVal, getter, setter));
                        } else {
                            Optional<DeclaredSyncGenerator> generator = declaredSyncGenerators.entrySet().stream()
                                  .filter(entry-> typeUtils.isAssignable(fieldType, entry.getKey())).map(Entry::getValue).findFirst();
                            if (generator.isPresent()){
                                generator.get().process(field, fieldSimpleName, valueParam, statementBuilder);
                            } else {
                                messager.printMessage(Kind.ERROR, "Unhandle Declared Type: " + fieldType.toString(), field);
                            }
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
    }

    private void basicSyncer(Consumer<CodeBlock> statementBuilder, String type, CodeBlock getter, CodeBlock setter) {
        TypeElement typeEl = getSyncableTypeEl(type);
        statementBuilder.accept(CodeBlock.of("$T.create($L, $L)", typeEl, getter, setter));
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

    interface DeclaredSyncGenerator {
        void process(VariableElement field, Name fieldSimpleName, ParameterSpec valueParam, Consumer<CodeBlock> statementBuilder);
    }

    static class ContainerSyncProxy {
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
            AnnotationMirror annotationMirror = field.getAnnotationMirrors().stream().filter(mirror->mirror.getAnnotationType().toString().equals(CONTAINER_SYNC)).findFirst().orElse(null);
            if (annotationMirror == null) {
                messager.printMessage(Kind.ERROR, "Couldn't get Annotation info!", field);
                throw new NullPointerException("Couldn't get Annotation info! "+field);
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
}
