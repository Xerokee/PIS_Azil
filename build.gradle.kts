// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")
    }
}

allprojects {
    repositories {
    }
}

plugins {
    id("com.android.application") version "8.1.2" apply false
}