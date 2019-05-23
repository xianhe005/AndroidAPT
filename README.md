# AndroidAPT
## APT
APT(Annotation Processing Tool)是一种处理注释的工具,它对源代码文件进行检测找出其中的Annotation，使用Annotation进行额外的处理。
Annotation处理器在处理Annotation时可以根据源文件中的Annotation生成额外的源文件和其它的文件(文件具体内容由Annotation处理器的编写者决定),APT还会编译生成的源文件和原来的源文件，将它们一起生成class文件。
## 创建Annotation Module
android studio 现在是最主流的安卓开发工具，而且也是最好用的，所有我这里讲的是apt在studio上的使用教程。
首先，我们需要新建一个Java Library,用来定义注解，所以库名最好为annotation，见名知意。注意，库一定要为Java Library，因为android Library不会引入javax.annotation等包。

此库的build.gradle如下：
```
apply plugin: 'java-library'

dependencies {
    implementation fileTree(include: ['*.jar'], dir: 'libs')
}

sourceCompatibility = JavaVersion.VERSION_1_8
targetCompatibility = JavaVersion.VERSION_1_8
```
## 创建apt Module
创建一个名为apt的Java Library，见名知意，此库是用来编写如何处理注解的代码，同时通过注解自动生成代码。

此库的build.gradle如下：
```
apply plugin: 'java-library'
apply plugin: 'java'

sourceCompatibility = JavaVersion.VERSION_1_8
targetCompatibility = JavaVersion.VERSION_1_8
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.google.auto.service:auto-service:1.0-rc2'//AutoService 主要的作用是注解 processor 类，自动生成。
    implementation 'com.squareup:javapoet:1.8.0'//JavaPoet 这个库的主要作用就是帮助我们通过类调用的形式来生成代码。
    implementation project(':annotation')//依赖上面创建的annotation Module。
}
```
## 定义注解
在annotation库新建一个Test接口，定义注解，如下：
```
package com.hxh.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Test {
}
```
## 定义Processor类
在apt库里定义注解处理器AbstractProcessor实现类AnnotationProcessor
```
package com.hxh.apt;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

@AutoService(Processor.class)//自动生成 javax.annotation.processing.IProcessor 文件
@SupportedSourceVersion(SourceVersion.RELEASE_8)//java版本支持
@SupportedAnnotationTypes({"com.hxh.annotation.Test"})//标注注解处理器支持的注解类型，就是我们刚才定义的接口Test(注意:全路径)，可以写入多个注解类型。
public class AnnotationProcessor extends AbstractProcessor {

    private Messager mMessager;
    private Elements mElements;
    private Filer mFiler;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnv.getFiler();//文件相关的辅助类
        mElements = processingEnv.getElementUtils();//元素相关的辅助类
        mMessager = processingEnv.getMessager();//日志相关的辅助类
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        String s = null;
        try {
            s = new String("测试:".getBytes("gbk"), StandardCharsets.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        MethodSpec methodMain = MethodSpec.methodBuilder("main")//创建main方法
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)//定义修饰符为 public static
                .addJavadoc("@param args test\n")//在生成的代码前添加注释
                .returns(void.class)//定义返回类型
                .addParameter(String[].class, "args")//定义方法参数
                .addStatement("$T.out.println($S + " + "args.toString()" + ")", System.class, s)//定义方法体
                .build();
        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")//创建HelloWorld类
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)//定义修饰符为 public final
                .addMethod(methodMain)//添加方法
                //.addJavadoc("@  此方法由apt自动生成")//定义方法参数
                .build();
        JavaFile javaFile = JavaFile.builder("com.hxh.apt", helloWorld).build();// 生成源代码
        try {
            javaFile.writeTo(mFiler);// 在 app module/build/generated/source/apt 生成一份源代码
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
```
## 注册AnnotationProcessor
在apt库下的src/main目录下创建resources/META-INF/services/javax.annotation.processing.Processor文件,其内容如下:
```
com.hxh.apt.AnnotationProcessor
```
即:AnnotationProcessor的全路径名
## 配置app的build.gradle
```
apply plugin: 'com.android.application'

android {
    compileSdkVersion 28
    defaultConfig {
        applicationId "com.hxh.androidapt"
        minSdkVersion 15
        targetSdkVersion 28
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation fileTree(include: ['*.jar'], dir: 'libs')
    implementation 'com.android.support:appcompat-v7:28.0.0'
    implementation 'com.android.support.constraint:constraint-layout:1.1.3'
    testImplementation 'junit:junit:4.12'
    androidTestImplementation 'com.android.support.test:runner:1.0.2'
    androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.2'

    implementation project(':annotation')//注解库引入
    annotationProcessor project(':apt')//核心，有了这个app才会处理apt的代码
}
```
### 注解的使用
在随意一个类添加@Test注解
```
package com.hxh.androidapt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.hxh.annotation.Test;

@Test
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
```
## 生成注解处理器生成的代码(注解处理器只能生成增加代码，不能修原代码内容)
然后编译，在app的build/generated/source/apt目录下，可看到生成的代码，如下：
```
package com.hxh.apt;

import java.lang.String;
import java.lang.System;

public final class HelloWorld {
  /**
   * @param args test
   */
  public static void main(String[] args) {
    System.out.println("测试:" + args.toString());
  }
}
```
至此，一个简单的HelloWorld就完成了，然后就可以像正常的java类一样使用编译生成的代码了。
ps：通过注解处理器的方式生成代码，由于代码在编译期就生成了，最后会一起打包，比用反射在运行期去反射效率高，适合于框架架构层面。
