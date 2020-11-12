package com.onedream.yellowknife_compiler;

import com.onedream.yellowknife_annotation.YellowKnifeBindView;
import com.onedream.yellowknife_annotation.YellowKnifeClickView;
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
        annotationTypes.add(YellowKnifeBindView.class.getCanonicalName());
        annotationTypes.add(YellowKnifeClickView.class.getCanonicalName());
        return annotationTypes;
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, Map<String, Set<Element>>> elementMap = parseElements(roundEnv.getElementsAnnotatedWith(YellowKnifeBindView.class));
        Map<Integer, String> methodMap = parseMethodElements(roundEnv.getElementsAnnotatedWith(YellowKnifeClickView.class));
        generateJavaFile(elementMap, methodMap);
        return true;
    }


    private Map<String, Map<String, Set<Element>>> parseElements(Set<? extends Element> elements) {
        Map<String, Map<String, Set<Element>>> elementMap = new LinkedHashMap<>();
        // 遍历全部元素
        for (Element element : elements) {
            if (!element.getKind().isField()) {//不是变量直接跳过
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

    //解析出【绑定的点击事件】
    private Map<Integer, String> parseMethodElements(Set<? extends Element> elements) {
        Map<Integer, String> elementMap = new LinkedHashMap<>();
        for (Element element : elements) {
            if (element.getKind() == ElementKind.METHOD) {
                String methodName = element.getSimpleName().toString();
                YellowKnifeClickView clickView = element.getAnnotation(YellowKnifeClickView.class);
                elementMap.put(clickView.viewId(), methodName);
            }
        }
        return elementMap;
    }

    //生成Java文件
    private void generateJavaFile(Map<String, Map<String, Set<Element>>> elementMap, Map<Integer, String> methodMap) {
        Set<Map.Entry<String, Map<String, Set<Element>>>> packageElements = elementMap.entrySet();
        for (Map.Entry<String, Map<String, Set<Element>>> packageEntry : packageElements) {

            String packageName = packageEntry.getKey();
            Map<String, Set<Element>> typeElementMap = packageEntry.getValue();

            Set<Map.Entry<String, Set<Element>>> typeElements = typeElementMap.entrySet();
            for (Map.Entry<String, Set<Element>> typeEntry : typeElements) {

                String typeName = typeEntry.getKey();
                Set<Element> variableElements = typeEntry.getValue();

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
                    YellowKnifeBindView bindView = variableElement.getAnnotation(YellowKnifeBindView.class);

                    bindMethodBuilder.addStatement("target." + variableName + " = activity.findViewById(" + bindView.viewId() + ")");
                    Set<Map.Entry<Integer, String>> methodMapSet = methodMap.entrySet();
                    for (Map.Entry<Integer, String> method : methodMapSet) {
                        if (bindView.viewId() == method.getKey()) {
                            //添加点击事件
                            bindMethodBuilder.addStatement("target." + variableName + ".setOnClickListener(new $T.OnClickListener() {\n" +
                                    "    @Override\n" +
                                    "    public void onClick($T v) {\n" +
                                    "            target." + method.getValue() + "(v);" +
                                    "    \n }\n" +
                                    "})", ClassName.get("android.view", "View"), ClassName.get("android.view", "View"));
                        }
                    }
                    //
                    unbindMethodBuilder.addStatement("target." + variableName + " = null");
                }

                unbindMethodBuilder.addStatement("target = null");

                TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(typeName + "_ViewBinding")
                        .addModifiers(Modifier.PUBLIC)
                        .addSuperinterface(ClassName.get(Constant.UNBINDER_PACKAGE_NAME, Constant.UNBINDER_CLASS_NAME))
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
    }
}