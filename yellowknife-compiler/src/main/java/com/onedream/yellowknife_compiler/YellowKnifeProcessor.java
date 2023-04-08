package com.onedream.yellowknife_compiler;

import com.onedream.yellowknife_annotation.Bind;
import com.onedream.yellowknife_annotation.OnClick;
import com.onedream.yellowknife_annotation.UnBinder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * @author jdallen
 * @since 2020/9/2
 */
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class YellowKnifeProcessor extends AbstractProcessor {

    private static final ClassName CLASS_NAME_OF_VIEW = ClassName.get("android.view", "View");


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new LinkedHashSet<>();
        annotationTypes.add(Bind.class.getCanonicalName());
        annotationTypes.add(OnClick.class.getCanonicalName());
        return annotationTypes;
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, Map<String, Set<Element>>> fieldMap = parseFieldElements(roundEnv.getElementsAnnotatedWith(Bind.class));
        Map<String, Map<String, Map<Integer, Element>>> methodMap = parseMethodElements(roundEnv.getElementsAnnotatedWith(OnClick.class));
        generateJavaFile(fieldMap, methodMap);
        return true;
    }


    //Map<String, Map<String, Set<Element>>>第一个String的Key是包名，第二个String的Key是类名
    private Map<String, Map<String, Set<Element>>> parseFieldElements(Set<? extends Element> elements) {
        Map<String, Map<String, Set<Element>>> elementMap = new LinkedHashMap<>();
        // 遍历全部元素
        for (Element element : elements) {
            if (element.getKind() != ElementKind.FIELD) {//不是指定的类型直接跳过
                continue;
            }
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            // 获取类名
            String typeName = typeElement.getSimpleName().toString();
            // 获取类的上一级元素，即包元素
            PackageElement packageElement = (PackageElement) typeElement.getEnclosingElement();
            // 获取包名
            String packageName = packageElement.getQualifiedName().toString();

            Map<String, Set<Element>> typeElementMap = elementMap.get(packageName);
            if (typeElementMap == null) {
                typeElementMap = new LinkedHashMap<>();
            }

            Set<Element> variableElements = typeElementMap.get(typeName);
            if (variableElements == null) {
                variableElements = new LinkedHashSet<>();
            }
            variableElements.add(element);

            typeElementMap.put(typeName, variableElements);
            elementMap.put(packageName, typeElementMap);
        }
        return elementMap;
    }

    //Map<String, Map<String, Set<Element>>>第一个String的Key是包名，第二个String的Key是类名
    private Map<String, Map<String, Map<Integer, Element>>> parseMethodElements(Set<? extends Element> elements) {
        Map<String, Map<String, Map<Integer, Element>>> elementMap = new LinkedHashMap<>();
        // 遍历全部元素
        for (Element element : elements) {
            if (element.getKind() != ElementKind.METHOD) {//不是指定的类型直接跳过
                continue;
            }
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            // 获取类名
            String typeName = typeElement.getSimpleName().toString();
            // 获取类的上一级元素，即包元素
            PackageElement packageElement = (PackageElement) typeElement.getEnclosingElement();
            // 获取包名
            String packageName = packageElement.getQualifiedName().toString();

            Map<String, Map<Integer, Element>> typeElementMap = elementMap.get(packageName);
            if (typeElementMap == null) {
                typeElementMap = new LinkedHashMap<>();
            }

            Map<Integer, Element> variableElements = typeElementMap.get(typeName);
            if (variableElements == null) {
                variableElements = new LinkedHashMap<>();
            }
            //
            OnClick clickView = element.getAnnotation(OnClick.class);
            int[] methodBindViewIdArr = clickView.value();
            if (null != methodBindViewIdArr && methodBindViewIdArr.length > 0) {
                for (int clickViewId : methodBindViewIdArr) {
                    variableElements.put(clickViewId, element);
                }
            }
            typeElementMap.put(typeName, variableElements);
            elementMap.put(packageName, typeElementMap);
        }
        return elementMap;
    }


    //生成Java文件
    private void generateJavaFile(Map<String, Map<String, Set<Element>>> fieldMap, Map<String, Map<String, Map<Integer, Element>>> methodMap) {
        Set<Map.Entry<String, Map<String, Set<Element>>>> packageElements = fieldMap.entrySet();
        for (Map.Entry<String, Map<String, Set<Element>>> packageEntry : packageElements) {
            onePackage(packageEntry, methodMap);
        }
    }


    private void onePackage(Map.Entry<String, Map<String, Set<Element>>> packageEntry, Map<String, Map<String, Map<Integer, Element>>> methodMap) {
        String packageName = packageEntry.getKey();
        Map<String, Set<Element>> typeElementMap = packageEntry.getValue();

        Set<Map.Entry<String, Set<Element>>> typeElements = typeElementMap.entrySet();
        //
        //该包下所有类中的注入方法
        Map<String, Map<Integer, Element>> typeMethodMap = methodMap.get(packageName);
        if (null == typeMethodMap) {
            typeMethodMap = new LinkedHashMap<>();
        }
        for (Map.Entry<String, Set<Element>> typeEntry : typeElements) {
            oneClass(packageName, typeEntry, typeMethodMap, methodMap);
        }
    }

    private void oneClass(String packageName, Map.Entry<String, Set<Element>> typeEntry, Map<String, Map<Integer, Element>> typeMethodMap, Map<String, Map<String, Map<Integer, Element>>> methodMap) {
        String typeName = typeEntry.getKey();
        Set<Element> variableElements = typeEntry.getValue();
        //该类中的注入方法
        Map<Integer, Element> methodSet = typeMethodMap.get(typeName);
        if (null == methodSet) {
            methodSet = new LinkedHashMap<>();
        }
        ClassName className = ClassName.get(packageName, typeName);
        FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(className, "target", Modifier.PRIVATE);
        MethodSpec.Builder bindMethodBuilder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(className, "activity")
                .addStatement("target = activity");
        MethodSpec.Builder unbindMethodBuilder = MethodSpec.methodBuilder("unbind")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC);

        for (Element element : variableElements) {
            VariableElement variableElement = (VariableElement) element;
            String variableName = variableElement.getSimpleName().toString();
            //初始化控件
            Bind bindView = variableElement.getAnnotation(Bind.class);
            bindMethodBuilder.addStatement("target." + variableName + " = activity.findViewById(" + bindView.value() + ")");
            //绑定点击事件
            Element methodElement = methodSet.get(bindView.value());
            if (null != methodElement) {
                String methodName = methodElement.getSimpleName().toString();
                //添加点击事件
                bindMethodBuilder.addStatement("target." + variableName + ".setOnClickListener(new $T.OnClickListener() {\n" +
                        "\t@Override\n" +
                        "\tpublic void onClick($T v) {\n" +
                        "\t\ttarget." + methodName + "(v);\n" +
                        "\t}\n" +
                        "})", CLASS_NAME_OF_VIEW, CLASS_NAME_OF_VIEW);
                //移除调绑定的方法
                methodSet.remove(bindView.value());
            }
            //添加解绑方法的语句
            unbindMethodBuilder.addStatement("target." + variableName + " = null");
        }

        //剩下的绑定方法(没有使用BindView绑定的，且有绑定点击事件的控件ID)
        if (null != methodSet && methodSet.size() > 0) {
            for (Map.Entry<Integer, Element> clickMethodEntry : methodSet.entrySet()) {
                //添加点击事件
                bindMethodBuilder.addStatement("activity.findViewById(" + clickMethodEntry.getKey() + ").setOnClickListener(new $T.OnClickListener() {\n" +
                        "\t@Override\n" +
                        "\tpublic void onClick($T v) {\n" +
                        "\t\ttarget." + clickMethodEntry.getValue().getSimpleName().toString() + "(v);\n" +
                        "\t}\n" +
                        "})", CLASS_NAME_OF_VIEW, CLASS_NAME_OF_VIEW);
            }
        }

        unbindMethodBuilder.addStatement("target = null");

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(typeName + "_ViewBinding")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(UnBinder.class)
                .addField(fieldSpecBuilder.build())
                .addMethod(bindMethodBuilder.build())
                .addMethod(unbindMethodBuilder.build());

        JavaFile javaFile = JavaFile.builder(packageName, typeBuilder.build()).build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}