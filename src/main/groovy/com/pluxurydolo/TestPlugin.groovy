package com.pluxurydolo

import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.TestLoggerPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

import static com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA

class TestPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply(TestLoggerPlugin)

        configureTestLogger(project)
        configureIdea(project)

        def integrationTestSourceSet = project.sourceSets.create('integrationTest')

        integrationTestSourceSet.compileClasspath += project.sourceSets.main.output
        integrationTestSourceSet.compileClasspath += project.sourceSets.main.compileClasspath
        integrationTestSourceSet.compileClasspath += project.sourceSets.test.compileClasspath

        integrationTestSourceSet.runtimeClasspath += project.sourceSets.main.output
        integrationTestSourceSet.runtimeClasspath += project.sourceSets.main.compileClasspath
        integrationTestSourceSet.runtimeClasspath += project.sourceSets.test.runtimeClasspath

        project.configurations {
            integrationTestImplementation.extendsFrom(project.configurations.testImplementation)
        }

        project.tasks.register('integrationTest', Test) {
            group = 'verification'

            testClassesDirs = integrationTestSourceSet.output.classesDirs
            classpath = integrationTestSourceSet.runtimeClasspath

            useJUnitPlatform()
            systemProperty 'spring.profiles.active', 'test'
        }

        project.tasks.named('check') {
            dependsOn project.tasks.named('integrationTest')
        }

        project.tasks.withType(Test).configureEach { testTask ->
            if (testTask.name == 'test') {
                testTask.useJUnitPlatform()
                testTask.systemProperty 'spring.profiles.active', 'test'
            }
        }
    }

    private static void configureTestLogger(Project project) {
        project.extensions.configure(TestLoggerExtension) { extension ->
            extension.showExceptions = true
            extension.showStackTraces = true
            extension.showSummary = true
            extension.showPassed = true
            extension.showSkipped = true
            extension.showFailed = true
            extension.theme = MOCHA
        }
    }

    private static void configureIdea(Project project) {
        project.plugins.apply('idea')

        project.afterEvaluate {
            if (project.hasProperty('sourceSets')) {
                def integrationTestSourceSet = project.sourceSets.findByName('integrationTest')
                if (integrationTestSourceSet) {
                    project.idea.module.testSources.from(integrationTestSourceSet.java.srcDirs)
                }
            }
        }
    }
}
