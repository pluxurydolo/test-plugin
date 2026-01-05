package com.pluxurydolo

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

class IntegrationTestPlugin implements Plugin<Project> {
    void apply(Project project) {
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

    private void configureIdea(Project project) {
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
