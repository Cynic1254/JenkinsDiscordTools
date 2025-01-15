package com.github.cynic1254

import groovy.json.JsonSlurper
import groovy.xml.MarkupBuilder

class UnrealTestResult {
    List<Device> devices = []
    String reportCreatedOn = ""
    Integer succeeded = 0
    Integer succeededWithWarnings = 0
    Integer failed = 0
    Integer notRun = 0
    Integer inProcess = 0
    Number totalDuration = 0.0
    Boolean comparisonExported = false
    String comparisonExportDirectory = ""
    List<Test> tests

    static class Device {
        String deviceName = ""
        String instance = ""
        String instanceName = ""
        String platform = ""
        String oSVersion = ""
        String model = ""
        String gPU = ""
        String cPUModel = ""
        String rAMInGB = ""
        String renderMode = ""
        String rHI = ""
        String appInstanceLog = ""
    }

    static class Test {
        String testDisplayName = ""
        String fullTestPath = ""
        String state = ""
        List<String> deviceInstance = []
        Number duration = 0.0
        String dateTime = ""
        List<Entry> entries = []
        Integer warnings = 0
        Integer errors = 0
        List artifacts = []

        static class Entry {
            Event event = new Event()
            String filename = ""
            Integer lineNumber = 0
            String timestamp = ""

            static class Event {
                String type = ""
                String message = ""
                String context = ""
                String artifact = ""
            }
        }
    }

    String WriteXMLToFile(Object steps, String FilePath) {
        steps.writeFile(file: FilePath, text: ToXML())

        return FilePath
    }

    @NonCPS
    String ToXML() {
        def sw = new StringWriter()
        def builder = new MarkupBuilder( sw )

        builder.doubleQuotes = true
        builder.mkp.xmlDeclaration version: "1.0", encoding: "utf-8"

        builder.testsuite( tests: succeeded + failed, failures: failed, time: totalDuration ) {
            for ( test in tests ) {
                builder.testcase( name: test.testDisplayName, classname: test.fullTestPath, status: test.state ) {
                    for ( entry in test.entries ) {
                        builder.failure( message: entry.event.message, type: entry.event.type, entry.filename + " " + entry.lineNumber )
                    }
                }
            }
        }

        return sw.toString()
    }

    static UnrealTestResult FromFile(Object steps, String FilePath) {
        String json = steps.readFile(file: FilePath, encoding: "UTF-8")
        return FromJSON(json.replace("\uFEFF", ""))
    }

    static UnrealTestResult FromJSON(String json) {
        def parsed = new JsonSlurper().parseText(json)

        return new UnrealTestResult(
                devices: parsed.devices.collect { deviceData ->
                    new Device(
                            deviceName: deviceData.deviceName,
                            instance: deviceData.instance,
                            instanceName: deviceData.instanceName,
                            platform: deviceData.platform,
                            oSVersion: deviceData.oSVersion,
                            model: deviceData.model,
                            gPU: deviceData.gPU,
                            cPUModel: deviceData.cPUModel,
                            rAMInGB: deviceData.rAMInGB,
                            renderMode: deviceData.renderMode,
                            rHI: deviceData.rHI,
                            appInstanceLog: deviceData.appInstanceLog
                    )
                },
                tests: parsed.tests.collect { testData ->
                    new Test(
                            testDisplayName: testData.testDisplayName,
                            fullTestPath: testData.fullTestPath,
                            state: testData.state,
                            deviceInstance: testData.deviceInstance,
                            duration: testData.duration,
                            dateTime: testData.dateTime,
                            entries: testData.entries.collect { entryData ->
                                new Test.Entry(
                                        event: new Test.Entry.Event(
                                                type: entryData.event.type,
                                                message: entryData.event.message,
                                                context: entryData.event.context,
                                                artifact: entryData.event.artifact
                                        ),
                                        filename: entryData.filename,
                                        lineNumber: entryData.lineNumber,
                                        timestamp: entryData.timestamp
                                )
                            },
                            warnings: testData.warnings,
                            errors: testData.errors,
                            artifacts: testData.artifacts
                    )
                },
                reportCreatedOn: parsed.reportCreatedOn,
                succeeded: parsed.succeeded,
                succeededWithWarnings: parsed.succeededWithWarnings,
                failed: parsed.failed,
                notRun: parsed.notRun,
                inProcess: parsed.inProcess,
                totalDuration: parsed.totalDuration,
                comparisonExported: parsed.comparisonExported,
                comparisonExportDirectory: parsed.comparisonExportDirectory
        )
    }
}
