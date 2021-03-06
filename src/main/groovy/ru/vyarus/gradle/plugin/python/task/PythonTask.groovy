package ru.vyarus.gradle.plugin.python.task

import groovy.transform.CompileStatic
import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import ru.vyarus.gradle.plugin.python.cmd.Python

/**
 * Task to execute python command (call module, script) using globally installed python.
 * All python tasks are called after default pipInstall task.
 * <p>
 * In essence, task duplicates {@link Python} utility configuration and use it for execution.
 * <p>
 * Task may be used as base class for specific modules tasks.
 *
 * @author Vyacheslav Rusakov
 * @since 11.11.2017
 */
@CompileStatic
class PythonTask extends BasePythonTask {

    /**
     * Working directory. Not required, but could be useful for some modules (e.g. generators).
     */
    @Input
    @Optional
    String workDir
    /**
     * Create work directory if it doesn't exist. Enabled by default.
     */
    @Input
    boolean createWorkDir = true
    /**
     * Module name. If specified, "-m module " will be prepended to specified command (if command not specified then
     * modules will be called directly).
     */
    @Input
    @Optional
    String module
    /**
     * Python command to execute. If module name set then it will be module specific command.
     * Examples:
     * <ul>
     * <li>direct module call: {@code '-m mod cmd'}
     * <li>code execution: {@code '-c import sys;\nsys...'}
     * <li>file execution: {@code 'path/to/file.py} (relative to workDir)
     * </ul>
     * Command could be specified as string, array or list (iterable).
     */
    @Input
    @Optional
    Object command
    /**
     * Python logs output level. By default it's {@link LogLevel@LIFECYCLE} (visible with '-i' gradle flag).
     */
    @Input
    @Optional
    LogLevel logLevel = LogLevel.LIFECYCLE
    /**
     * Python arguments applied to all executed commands. Arguments applied before called command
     * (and so option may be useful for cases impossible with {@link #extraArgs}, applied after command).
     * For example, it could be used for -I or -S flags (be aware that -S can cause side effects, especially
     * inside virtual environments).
     */
    @Input
    @Optional
    List<String> pythonArgs = []
    /**
     * Extra arguments to append to every called command.
     * Useful for pre-configured options, applied to all executed commands
     */
    @Input
    @Optional
    List<String> extraArgs = []
    /**
     * Environment variables for executed python process (variables specified in gradle's
     * {@link org.gradle.process.ExecSpec#environment(java.util.Map)} during python process execution).
     */
    @Input
    @Optional
    Map<String, Object> environment = [:]
    /**
     * Prefix each line of python output. By default it's '\t' to indicate command output.
     */
    @Input
    @Optional
    String outputPrefix = '\t'

    @TaskAction
    void run() {
        String mod = getModule()
        Object cmd = getCommand()
        if (!mod && !cmd) {
            throw new GradleException('Module or command to execute must be defined')
        }
        initWorkDirIfRequired()

        Python python = python
                .logLevel(getLogLevel())
                .outputPrefix(getOutputPrefix())
                .workDir(getWorkDir())
                .pythonArgs(getPythonArgs())
                .extraArgs(getExtraArgs())
                .environment(environment)

        if (mod) {
            python.callModule(mod, cmd)
        } else {
            python.exec(cmd)
        }
    }

    /**
     * Add python arguments, applied before command.
     *
     * @param args arguments
     */
    @SuppressWarnings('ConfusingMethodName')
    void pythonArgs(String... args) {
        if (args) {
            getPythonArgs().addAll(args)
        }
    }

    /**
     * Add extra arguments, applied to command.
     *
     * @param args arguments
     */
    @SuppressWarnings('ConfusingMethodName')
    void extraArgs(String... args) {
        if (args) {
            getExtraArgs().addAll(args)
        }
    }

    /**
     * Add environment variable for python process (will override previously set value).
     *
     * @param var variable name
     * @param value variable value
     */
    @SuppressWarnings('ConfusingMethodName')
    void environment(String var, Object value) {
        getEnvironment().put(var, value)
    }

    /**
     * Add environment variables for python process (will override already set values, but not replace context
     * map completely). May be called multiple times: all variables would be aggregated.
     *
     * @param vars (may be null)
     */
    @SuppressWarnings('ConfusingMethodName')
    void environment(Map<String, Object> vars) {
        if (vars) {
            getEnvironment().putAll(vars)
        }
    }

    private void initWorkDirIfRequired() {
        String dir = getWorkDir()
        if (dir && isCreateWorkDir()) {
            File wrkd = project.file(dir)
            if (!wrkd.exists()) {
                wrkd.mkdirs()
            }
        }
    }
}
