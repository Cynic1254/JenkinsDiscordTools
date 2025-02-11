package com.github.cynic1254

class Unreal {
    static Object steps
    static String config = "Shipping"
    static String platform = "Win64"
    static String enginePath = ""
    static String projectPath = ""
    private static Boolean initialized = false

    static void Init(String config, String platform, String enginePath, String projectPath) {
        this.config = config
        this.platform = platform
        this.enginePath = enginePath
        this.projectPath = projectPath
        initialized = true
        steps = Library.steps
    }

    static void BuildPrecompiledProject(String outDir) {
        if (!initialized)
        {
            Init(steps.env.config as String, steps.env.platform as String, steps.env.enginePath as String, steps.env.projectPath as String)
        }
        steps.bat(label: "Build Precompiled Project", script: "\"${enginePath}\\Build\\BatchFiles\\RunUAT.bat\" BuildCookRun -Project=\"${projectPath}\" -NoP4 -nocompileeditor -skipbuildeditor -TargetPlatform=${platform} -Platform=${platform} -ClientConfig=${config} -Cook -Build -Stage -Pak -Archive -Archivedirectory=\\\"${outDir}\\\" -Rocket -Prereqs -iostore -compressed -Package -nocompile -nocompileuat")
    }

    static void BuildBlueprintProject(String outDir) {
        if (!initialized)
        {
            Init(steps.env.config as String, steps.env.platform as String, steps.env.enginePath as String, steps.env.projectPath as String)
        }
        steps.bat(label: "Package UE5 project", script: "\"${enginePath}\\Build\\BatchFiles\\RunUAT.bat\" BuildCookRun -Project=\"${projectPath}\" -NoP4 -Distribution -TargetPlatform=${platform} -Platform=${platform} -ClientConfig=${config} -ServerConfig=${config} -Cook -Allmaps -Build -Stage -Pak -Archive -Archivedirectory=\"${outDir}\" -Rocket -Prereqs -Package")
    }

    static void fixupRedirects() {
        if (!initialized)
        {
            Init(steps.env.config as String, steps.env.platform as String, steps.env.enginePath as String, steps.env.projectPath as String)
        }
        steps.bat(label: "Fix up redirectors", script: "\"${enginePath}\\Binaries\\${platform}\\UnrealEditor.exe\" \"${projectPath}\" -run=ResavePackages -fixupredirects -autocheckout -projectonly -unattended -stdout")
    }

    static UnrealTestResult RunAutomationCommand(String testCommand, Boolean markUnstableIfFail = true) {
        if (!initialized)
        {
            Init(steps.env.config as String, steps.env.platform as String, steps.env.enginePath as String, steps.env.projectPath as String)
        }

        Integer result = steps.bat(label: "Automated Tests", script: "\"${enginePath}\\Binaries\\${platform}\\UnrealEditor-Cmd.exe\" \"${projectPath}\" -stdout -fullstdlogoutput -buildmachine -nullrhi -unattended -NoPause -NoSplash -NoSound -ExecCmds=\"Automation ${testCommand};Quit\" -ReportExportPath=\"${steps.env.WORKSPACE}\\Logs\\UnitTestsReport\"", returnStatus: true)

        if (markUnstableIfFail && result != 0) {
            steps.unstable("Some tests did not pass!")
        }

        return UnrealTestResult.FromFile(steps, "${steps.env.WORKSPACE}\\Logs\\UnitTestsReport\\index.json")
    }
}
