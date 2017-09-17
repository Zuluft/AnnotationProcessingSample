package com.example.compiler;

import com.example.lib.TestAnnotation;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.example.lib.TestAnnotation")
public class TestProcessor extends AbstractProcessor {

    private static final String METHOD_PREFIX = "start";
    private static final ClassName classIntent = ClassName.get("android.content", "Intent");
    private static final ClassName classContext = ClassName.get("android.content", "Context");

    private Messager mMessager;
    private Map<String, String> mAnnotatedClasses;
    private Elements mElements;
    private Filer mFiler;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMessager = processingEnvironment.getMessager();
        mElements = processingEnvironment.getElementUtils();
        mFiler = processingEnvironment.getFiler();
        mAnnotatedClasses = new HashMap<>();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(TestAnnotation.class)) {

            if (element.getKind() != ElementKind.CLASS) {
                mMessager.printMessage(Diagnostic.Kind.ERROR, "Can be applied to class.");
                return true;
            }

            TypeElement typeElement = (TypeElement) element;
            mAnnotatedClasses.put(
                    typeElement.getSimpleName().toString(),
                    mElements.getPackageOf(typeElement).getQualifiedName().toString());
        }
        TypeSpec.Builder navigatorClass = TypeSpec
                .classBuilder("Navigator")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        for (Map.Entry<String, String> element : mAnnotatedClasses.entrySet()) {
            String className = element.getKey();
            String packageName = element.getValue();
            ClassName activityClass = ClassName.get(packageName, className);
            MethodSpec intentMethod = MethodSpec
                    .methodBuilder(METHOD_PREFIX + className)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(classIntent)
                    .addParameter(classContext, "context")
                    .addStatement("return new $T($L, $L)", classIntent, "context", activityClass + ".class")
                    .build();
            navigatorClass.addMethod(intentMethod);
        }
        try {
            JavaFile.builder("com.annotationsample", navigatorClass.build())
                    .build()
                    .writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
