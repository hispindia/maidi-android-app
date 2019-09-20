# CAF

## Integrate Caf

### Integrate module to project
```
git submodule add https://bitbucket.org/beesightsoft/caf_android.git libs/caf_android
```

- Custom config version at top gradle file (Optional)
```
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    ext {
        // Config version for caf
        cafVersion = [
                buildGradle          : "3.0.1",
                grgit                : "2.1.1",
                compileSdkVersion    : 26,
                buildToolsVersion    : "26.0.2",
                minSdkVersion        : 16,
                targetSdkVersion     : 26,
                gson                 : "2.8.2",
                okhttp               : "3.10.0",
                retrofit             : "2.3.0",
                rxAndroid            : "1.2.0",
                rxJava               : "1.1.5",
                dagger               : "2.13",
                daggerCompiler       : "2.13",
                jsr250Api            : "1.0",
                threetenabp          : "1.0.5",
                hawk                 : "1.23",
                cicerone             : "3.0.0",
                logentries           : "logentries-android-4.4.1",
                eventbus             : "3.1.1",
                cookie               : "v1.0.1"
        ]
    }

    repositories {
        jcenter()
    }
    dependencies {
        classpath "com.android.tools.build:gradle:${cafVersion["buildGradle"]}"
    }
}

allprojects {
    repositories {
        jcenter()
        mavenCentral()
        google()
        maven { url "https://jitpack.io" }
    }
}


task clean(type: Delete) {
    delete rootProject.buildDir
}

subprojects {
    project.plugins.whenPluginAdded { plugin ->
        if ("com.android.build.gradle.AppPlugin".equals(plugin.class.name)) {
            project.android.dexOptions.preDexLibraries = false
        } else if ("com.android.build.gradle.LibraryPlugin".equals(plugin.class.name)) {
            project.android.dexOptions.preDexLibraries = false
        }
    }
}
```

## Clone project with submodule
```
git clone --recursive <main project repo url>
```

## Migrate app from gradle 2.0 to 3.0
### In root build.gradle
- Change gradle build tool version in buildscript -> dependencies
```
classpath 'com.android.tools.build:gradle:2.2.3'
```
To
```
classpath 'com.android.tools.build:gradle:3.0.1'
```

- Add repositories
```
allprojects {
    repositories {
        jcenter()
        mavenCentral()
        google()
        maven { url "https://jitpack.io" }
    }
}
```

### In app build.gradle
- Remove android apt, retrolambda
```
classpath "com.neenbedankt.gradle.plugins:android-apt:1.8"
classpath "me.tatarka:gradle-retrolambda:3.2.2"

apply plugin: 'me.tatarka.retrolambda'
apply plugin: 'android-apt'

```

- Update dependency configuration
```
compile -> implementation (recommended)
compile -> api (same as compile)
provided -> compileOnly
apk -> runtimeOnly
testCompile -> testImplementation
debugCompile -> debugImplementation
androidTestCompile -> androidTestImplementation
apt -> annotationProcessor
```

- Update in android config block
```
android {
    applicationVariants.all { variant ->
        variant.outputs.all {
            outputFileName = "${variant.name}-${variant.versionName}.apk"
        }
    }

    flavorDimensions "default"
    defaultConfig {
        //Others config
        //...
        //Android annotation
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = ['resourcePackageName': android.defaultConfig.applicationId]
            }
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
afterEvaluate {
    tasks.matching {
        it.name.startsWith('dex')
    }.each { dx ->
        if (dx.additionalParameters == null) {
            dx.additionalParameters = ['--multi-dex']
        } else {
            dx.additionalParameters += '--multi-dex'
        }
    }
}
```








