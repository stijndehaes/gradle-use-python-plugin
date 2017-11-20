package ru.vyarus.gradle.plugin.python.test

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import ru.vyarus.gradle.plugin.python.AbstractKitTest
import ru.vyarus.gradle.plugin.python.cmd.Pip

/**
 * @author Vyacheslav Rusakov
 * @since 20.11.2017
 */
class PipInstallTaskKitTest extends AbstractKitTest {

    @Override
    def setup() {
        // make sure correct version installed
        new Pip(ProjectBuilder.builder().build(), null).install('click==6.7')
    }

    def "Check no modules list"() {

        setup:
        build """
            plugins {
                id 'ru.vyarus.use-python'
            }

            python {
                pip 'click:6.7'
                showInstalledVersions = false
            }

        """

        when: "run task"
        BuildResult result = run('pipInstall')

        then: "no all modules list printed"
        result.task(':pipInstall').outcome == TaskOutcome.SUCCESS
        !result.output.contains('python -m pip install click')
        !result.output.contains('python -m pip list')
    }

    def "Check always install"() {

        setup:
        build """
            plugins {
                id 'ru.vyarus.use-python'
            }

            python {
                pip 'click:6.7'
                alwaysInstallModules = true
            }

        """

        when: "run task"
        BuildResult result = run('pipInstall')

        then: "click install called"
        result.task(':pipInstall').outcome == TaskOutcome.SUCCESS
        result.output.contains('Requirement already satisfied: click==6.7')
        result.output.contains('python -m pip list')
    }
}
