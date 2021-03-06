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
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.ElementScanner8;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

@SupportedAnnotationTypes(SyncMapperProccessor.CONTAINER_SYNC)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
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

        declaredSyncGenerators = new LinkedHashMap<>();
        addDeclaredSyncGenerator("mekanism.api.fluid.IExtendedFluidTank", (field, valueParam, statementBuilder) -> {
            CodeBlock getter = CodeBlock.of("()->$N.get().$L.getFluid()", valueParam, field);
            CodeBlock setter = CodeBlock.of("newValue -> $N.get().$L.setStack(newValue)", valueParam, field);
            basicSyncer(statementBuilder, "FluidStack", getter, setter);
        });
        addDeclaredSyncGenerator("mekanism.api.chemical.gas.IGasTank", (field, valueParam, statementBuilder) -> {
            CodeBlock getter = CodeBlock.of("()->$N.get().$L.getStack()", valueParam, field);
            CodeBlock setter = CodeBlock.of("newValue -> $N.get().$L.setStack(newValue)", valueParam, field);
            basicSyncer(statementBuilder, "GasStack", getter, setter);
        });
        addDeclaredSyncGenerator("mekanism.api.energy.IEnergyContainer", (field, valueParam, statementBuilder) -> {
            CodeBlock getter = CodeBlock.of("()->$N.get().$L.getEnergy()", valueParam, field);
            CodeBlock setter = CodeBlock.of("newValue -> $N.get().$L.setEnergy(newValue)", valueParam, field);
            basicSyncer(statementBuilder, "FloatingLong", getter, setter);
        });
        addDeclaredSyncGenerator("mekanism.common.capabilities.heat.BasicHeatCapacitor", (field, valueParam, statementBuilder) -> {
            basicSyncer(statementBuilder, "Double",
                  CodeBlock.of("()->$N.get().$L.getHeatCapacity()", valueParam, field),
                  CodeBlock.of("newValue -> $N.get().$L.setHeatCapacityFromPacket(newValue)", valueParam, field)
            );
            basicSyncer(statementBuilder, "Double",
                  CodeBlock.of("()->$N.get().$L.getHeat()", valueParam, field),
                  CodeBlock.of("newValue -> $N.get().$L.setHeat(newValue)", valueParam, field)
            );
        });
        DeclaredSyncGenerator mergedChemicalTankGenerator = (field, valueParam, statementBuilder) -> {
            basicSyncer(statementBuilder, "GasStack",
                  CodeBlock.of("()->$N.get().$L.getGasTank().getStack()", valueParam, field),
                  CodeBlock.of("newValue -> $N.get().$L.getGasTank().setStack(newValue)", valueParam, field)
            );
            basicSyncer(statementBuilder, "InfusionStack",
                  CodeBlock.of("()->$N.get().$L.getInfusionTank().getStack()", valueParam, field),
                  CodeBlock.of("newValue -> $N.get().$L.getInfusionTank().setStack(newValue)", valueParam, field)
            );
            basicSyncer(statementBuilder, "PigmentStack",
                  CodeBlock.of("()->$N.get().$L.getPigmentTank().getStack()", valueParam, field),
                  CodeBlock.of("newValue -> $N.get().$L.getPigmentTank().setStack(newValue)", valueParam, field)
            );
            basicSyncer(statementBuilder, "SlurryStack",
                  CodeBlock.of("()->$N.get().$L.getSlurryTank().getStack()", valueParam, field),
                  CodeBlock.of("newValue -> $N.get().$L.getSlurryTank().setStack(newValue)", valueParam, field)
            );
        };
        addDeclaredSyncGenerator("mekanism.common.capabilities.merged.MergedTank", (field, valueParam, statementBuilder) -> {
            basicSyncer(statementBuilder, "FluidStack",
                  CodeBlock.of("()->$N.get().$L.getFluidTank().getFluid()", valueParam, field),
                  CodeBlock.of("newValue -> $N.get().$L.getFluidTank().setStack(newValue)", valueParam, field)
            );
            //process superclass
            mergedChemicalTankGenerator.process(field, valueParam, statementBuilder);
        });
        addDeclaredSyncGenerator("mekanism.api.chemical.merged.MergedChemicalTank", mergedChemicalTankGenerator);
        addDeclaredSyncGenerator("mekanism.common.lib.math.voxel.VoxelCuboid", (field, valueParam, statementBuilder) -> {
            basicSyncer(statementBuilder, "BlockPos",
                  CodeBlock.of("()->$N.get().$L.getMinPos()", valueParam, field),
                  CodeBlock.of("newValue -> $N.get().$L.setMinPos(newValue)", valueParam, field)
            );
            basicSyncer(statementBuilder, "BlockPos",
                  CodeBlock.of("()->$N.get().$L.getMaxPos()", valueParam, field),
                  CodeBlock.of("newValue -> $N.get().$L.setMaxPos(newValue)", valueParam, field)
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
                        ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class)),//Class<?>
                        ParameterizedTypeName.get(classSyncer, WildcardTypeName.subtypeOf(Object.class))//IClassSyncer<?>
                  ), "REGISTRY", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL
            ).initializer("new $T<>()", HashMap.class).build();
            CodeBlock.Builder registryInitialiser = CodeBlock.builder();

            //common params for register() which don't vary
            ParameterSpec consumerParam = ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Consumer.class), ClassName.get(iSyncableData)), "consumer").build();
            ParameterSpec tagParam = ParameterSpec.builder(String.class, "tag").build();

            Set<TypeElement> annotatedTypes = annotatedElements.keySet();
            for (TypeElement containingClass : annotatedTypes) {
                try {
                    ClassName containingClassName = ClassName.get(containingClass);

                    //build base type
                    ClassName builderClassName = getBuilderClassName(containingClassName);
                    TypeVariableName targetTypeVariable = TypeVariableName.get("TARGET");
                    TypeSpec.Builder builderClass = TypeSpec.classBuilder(builderClassName)
                          .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                          .addSuperinterface(ParameterizedTypeName.get(classSyncer, targetTypeVariable))
                          .addOriginatingElement(containingClass)
                          .addTypeVariable(targetTypeVariable.withBounds(containingClassName));

                    //add singleton instance
                    builderClass.addField(FieldSpec.builder(builderClassName, "INSTANCE", Modifier.FINAL, Modifier.PUBLIC, Modifier.STATIC).initializer("new $T()", builderClassName).build());

                    //build register method
                    ParameterSpec valueParam = ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Supplier.class), targetTypeVariable), "target").build();
                    MethodSpec.Builder registerMethod = MethodSpec.methodBuilder("register")
                          .addModifiers(Modifier.PUBLIC).addAnnotation(Override.class)
                          .returns(void.class)
                          .addParameter(valueParam)
                          .addParameter(consumerParam)
                          .addParameter(tagParam);

                    //Check if we need to pass to a parent class
                    TypeElement parentContainer = findApplicableParent(containingClass, annotatedTypes);
                    if (parentContainer != null) {
                        registerMethod.addStatement("$T.INSTANCE.register($N, $N, $N)", getBuilderClassName(parentContainer), valueParam, consumerParam, tagParam);
                        builderClass.addOriginatingElement(parentContainer);
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
                    boolean isFirst = true;
                    Builder ifBlockBuilder = CodeBlock.builder();
                    String basePattern = "($N.equals($S))";
                    for (Entry<String, Builder> tagElement : tags.entrySet()) {
                        String tag = tagElement.getKey();
                        CodeBlock.Builder builder = tagElement.getValue();
                        if (!builder.isEmpty()) {
                            if (isFirst) {
                                ifBlockBuilder.beginControlFlow("if "+basePattern, tagParam, tag);
                                isFirst = false;
                            } else {
                                ifBlockBuilder.nextControlFlow("else if "+basePattern, tagParam, tag);
                            }
                            ifBlockBuilder.add(builder.build());
                        }
                    }
                    registerMethod.addCode(ifBlockBuilder.endControlFlow().build());

                    //finish up
                    builderClass.addMethod(registerMethod.build());

                    JavaFile javaFile = JavaFile.builder(containingClassName.packageName(), builderClass.build())
                          .build();

                    javaFile.writeTo(filer);

                } catch (IOException e) {
                    messager.printMessage(Kind.ERROR, e.toString());
                }
            }

            //build registry class.
            String registryClassName;
            if (annotatedTypes.stream().anyMatch(el-> el.getQualifiedName().toString().contains("mekanism.generators"))){
                registryClassName = "GeneratorsContainerSyncRegistry";
            } else if (annotatedTypes.stream().anyMatch(el-> el.getQualifiedName().toString().contains("mekanism.additions"))){
                registryClassName = "AdditionsContainerSyncRegistry";
            } else if (annotatedTypes.stream().anyMatch(el-> el.getQualifiedName().toString().contains("mekanism.tools"))){
                registryClassName = "ToolsContainerSyncRegistry";
            } else if (annotatedTypes.stream().anyMatch(el-> el.getQualifiedName().toString().contains("mekanism.defense"))){
                registryClassName = "DefenceContainerSyncRegistry";
            } else {
                registryClassName = "ContainerSyncRegistry";
            }
            TypeSpec.Builder registryClass = TypeSpec.classBuilder(registryClassName)
                  .addModifiers(Modifier.PUBLIC)
                  .addField(registryField);
            //locate any Types (incl inners) that have a syncer or have a superclass with one
            for (Element rootElement : roundEnv.getRootElements()) {
                rootElement.accept(new ElementScanner8<Void, Void>(){
                    @Override
                    public Void visitType(TypeElement e, Void unused) {
                        TypeElement applicableClass = annotatedTypes.stream().anyMatch(el->typeUtils.isSameType(e.asType(), el.asType())) ? e : findApplicableParent(e, annotatedTypes);
                        if (applicableClass != null) {
                            registryInitialiser.addStatement("$N.put($T.class, $T.INSTANCE)", registryField, ClassName.get(e), getBuilderClassName(applicableClass));
                            registryClass.addOriginatingElement(applicableClass);
                        }
                        return super.visitType(e, unused);
                    }
                }, null);
            }
            registryClass.addStaticBlock(registryInitialiser.build());
            try {
                JavaFile.builder("mekanism", registryClass.build()).build().writeTo(filer);
            } catch (IOException e) {
                messager.printMessage(Kind.ERROR, e.toString());
            }
        }

        return true;
    }

    private static ClassName getBuilderClassName(ClassName containerClassName) {
        return containerClassName.peerClass(containerClassName.simpleName() + GENERATED_CLASS_SUFFIX);
    }

    private static ClassName getBuilderClassName(TypeElement containerClass) {
        return getBuilderClassName(ClassName.get(containerClass));
    }

    /**
     * Recursively find a superclass which has annotated methods (i.e. a builder does or will exist)
     *
     * @param current The type whose parent we want to find
     * @param allTypes the set of annotated type elements we found in this round
     * @return an applicable parent or null
     */
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
        return allTypes.stream()
              .filter(el->typeUtils.isSameType(parentMirror, el.asType())) // if it's in our list of annotated items, it's valid
              .findFirst()
              .orElseGet(()->{//if the builder class exists (perhaps from another source set) it's valid
            TypeElement parentBuilder = elementUtils.getTypeElement(getBuilderClassName(parentEl).canonicalName());
            if (parentBuilder != null) {
                return parentEl;
            }
            return findApplicableParent(parentEl, allTypes);
        });
    }

    private void processField(VariableElement field, ContainerSyncProxy containerSyncAnnotation, ParameterSpec valueParam, Consumer<CodeBlock> statementBuilder) {
        TypeMirror fieldType = field.asType();

        Name fieldSimpleName = field.getSimpleName();
        CodeBlock getter = CodeBlock.of("()->$N.get().$L", valueParam, field);
        CodeBlock setter = CodeBlock.of("newValue->$N.get().$L = newValue", valueParam, field);

        if (containerSyncAnnotation.getter != null) {
            getter = CodeBlock.of("()->$N.get().$L()", valueParam, containerSyncAnnotation.getter);
        }
        if (containerSyncAnnotation.setter != null) {
            setter = CodeBlock.of("newValue->$N.get().$L(newValue)", valueParam, containerSyncAnnotation.setter);
        }

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
                                generator.get().process(field, valueParam, statementBuilder);
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

}
