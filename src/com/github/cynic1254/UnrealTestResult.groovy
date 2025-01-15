package com.github.cynic1254

import com.github.cynic1254.DiscordMessage

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

    DiscordMessage ToDiscordMessage(Object steps) {
        def message = new DiscordMessage()
        message.avatar_url = "https://preview.redd.it/is-it-normal-for-games-to-have-a-unreal-engine-logo-as-its-v0-ekvife6ql3xc1.jpeg?auto=webp&s=fcec369f522ba22bc828c7c2672140eb965c51cb"
        message.username = "Unreal Test Result"

        def embed = DiscordMessage.CreateEmbed()

        if (failed > 0) {
            embed.title = "Some tests failed!"
            embed.description = "(${failed}/${failed + succeeded}) tests failed"
            embed.fields = GetFailedFields()
            embed.footer = DiscordMessage.CreateFooter(
                    text: "Ran ${succeeded + failed} tests in ${String.format("%.4f", totalDuration)} seconds [full test results](${steps.env.BUILD_URL})"
            )
        }

        message.embeds = [embed]

        return message
    }

    String WriteXMLToFile(Object steps, String FilePath) {
        steps.writeFile(file: FilePath, text: ToXML())

        return FilePath
    }

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

    private List<DiscordMessage.Embed.Field> GetFailedFields() {
        List<DiscordMessage.Embed.Field> fields = []

        for (i in 0..<failed) {
            def test = tests[i]
            fields.add(new DiscordMessage.Embed.Field(
                    name: test.testDisplayName,
                    value: "${test.state} after ${String.format('%.4f', test.duration)} seconds"
            ))

            for(entry in test.entries) {
                fields.add(new DiscordMessage.Embed.Field(
                        name: entry.event.type,
                        value: entry.event.message,
                        inline: true
                ))
            }
        }

        if (fields.size() > 25) {
            fields = fields.subList(0, 24)
            fields.add(new DiscordMessage.Embed.Field(
                    name: "Shortening List due to field limit",
                    value: "..."
            ))
        }

        return fields
    }
}
