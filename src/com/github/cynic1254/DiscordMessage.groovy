package com.github.cynic1254


import groovy.json.JsonOutput

class DiscordMessage {
    String username = null
    String avatar_url = null
    String content = null
    List<Embed> embeds = []
    Boolean tts = null
    Mentions allowed_mentions = null

    static class Embed {
        Integer color = null
        Author author = null
        String title = null
        String url = null
        String description = null
        List<Field> fields = []
        URL thumbnail = null
        URL image = null
        Footer footer = null
        String timestamp = null

        static class Author {
            String name = null
            String url = null
            String icon_url = null
        }

        static class Field {
            String name = null
            String value = null
            Boolean inline = null
        }

        static class URL {
            String url = null
        }

        static class Footer {
            String text = null
            String icon_url = null
        }
    }

    static class Mentions {
        List<String> parse = []
        List<String> roles = []
        List<String> users = []
    }

    void Send(String webhook) {
        Library.steps.bat(label: "Send Discord Message", script: "curl -X POST -H \"Content-Type: application/json\" -d \"${JsonOutput.toJson(this).replace('"', '""')}\" ${webhook}")
    }

    static DiscordMessage FromTestResults(UnrealTestResult testResults) {def embed = new Embed()

        if (testResults.failed > 0) {
            embed.title = "Some tests failed!"
            embed.color = 16776960
            embed.description = "(${testResults.failed}/${testResults.failed + testResults.succeeded}) tests failed"
            embed.fields = ParseTestResultFields(testResults)
        } else {
            embed.title = "All tests succeeded!"
            embed.color = 65280
            embed.description = "Successfully ran all tests"
        }

        embed.footer = new Embed.Footer(
                text: "Ran ${testResults.succeeded + testResults.failed} tests in ${String.format("%.4f", testResults.totalDuration)} seconds. full test results: ${Library.steps.env.BUILD_URL}"
        )

        return new DiscordMessage(
                avatar_url: "https://preview.redd.it/is-it-normal-for-games-to-have-a-unreal-engine-logo-as-its-v0-ekvife6ql3xc1.jpeg?auto=webp&s=fcec369f522ba22bc828c7c2672140eb965c51cb",
                username: "Unreal Test Result",
                embeds: [embed]
        )
    }

    private static List<Embed.Field> ParseTestResultFields(UnrealTestResult results) {
        List<Embed.Field> fields = []

        for (i in 0..< results.failed) {
            def test = results.tests[i]
            fields.add(new Embed.Field(
                    name: test.testDisplayName,
                    value: "${test.state} after ${String.format('%.4f', test.duration)} seconds"
            ))

            for(entry in test.entries) {
                fields.add(new Embed.Field(
                        name: entry.event.type,
                        value: entry.event.message,
                        inline: true
                ))
            }
        }

        if (fields.size() > 25) {
            fields = fields.subList(0, 24)
            fields.add(new Embed.Field(
                    name: "Shortening List due to field limit",
                    value: "..."
            ))
        }

        return fields
    }

    static DiscordMessage BaseReportMessage(String header, String state, Integer color = null) {
        return new DiscordMessage(
                embeds: [
                        new Embed(
                                title: header,
                                color: color,
                                fields: [
                                        new Embed.Field(
                                                name: "${Unreal.config}${Unreal.platform} ${Jenkins.jobBaseName} ${state}",
                                                value: "Last Changelist: ${Library.steps.env.P4_CHANGELIST}"
                                        ),
                                        new Embed.Field(
                                                name: "Job url",
                                                value: "${Jenkins.buildURL}"
                                        )
                                ],
                                footer: new Embed.Footer(
                                        text: "${Jenkins.jobBaseName} (${Jenkins.buildNumber})"
                                )
                        )
                ]
        )
    }

    static DiscordMessage Succeeded() {
        return BaseReportMessage(":white_check_mark: BUILD SUCCEEDED :white_check_mark:", "has succeeded", 65280)
    }

    static DiscordMessage Failed() {
        return BaseReportMessage(":x: BUILD FAILED :x:", "has failed", 16711680)
    }

    static DiscordMessage PartFailed() {
        return BaseReportMessage(":o: BUILD FAILED PARTIALLY :o:", "has partially failed", 16744192)
    }

    static DiscordMessage Unstable() {
        return BaseReportMessage(":warning: UNSTABLE BUILD :warning:", "is unstable", 16776960)
    }

    static DiscordMessage Aborted() {
        return BaseReportMessage(":stop_sign: BUILD ABORTED :stop_sign:", "has been aborted", 255)
    }

    static DiscordMessage SteamPushStarted(String AuthorizerID) {
        return new DiscordMessage(
                embeds: [
                        new Embed(
                                title: "Started push to steam",
                                color: 	16777215, //white
                                fields: [
                                        new Embed.Field(
                                                name: "Push to Steam has been initiated, SteamGuard Authorization might be required",
                                                value: "${Library.steps.env.BUILD_URL}"
                                        ),
                                        new Embed.Field(
                                                name: "Authorizer:",
                                                value: "<@${AuthorizerID}>"
                                        )
                                ],
                                footer: new Embed.Footer(
                                        text: Library.steps.env.JOB_BASE_NAME as String
                                )
                        )
                ],
                allowed_mentions: new Mentions(
                        users: [AuthorizerID]
                ),
                username: "Steam Auth",
                avatar_url: "https://e1.pngegg.com/pngimages/855/514/png-clipart-clay-os-6-a-macos-icon-steam-steam-logo-thumbnail.png"
        )
    }

    static DiscordMessage SteamPushFailed() {
        return new DiscordMessage(
                username: "Steam Auth",
                avatar_url: "https://e1.pngegg.com/pngimages/855/514/png-clipart-clay-os-6-a-macos-icon-steam-steam-logo-thumbnail.png",
                embeds: [
                        new Embed(
                                title: "Steam Authorization Timed Out",
                                color: 	16777215, //white
                                fields: [
                                        new Embed.Field(
                                                name: "Steam Authorization was not provided in time, Build has not been uploaded to steam, will be uploaded to GDrive instead",
                                                value: "${Library.steps.env.BUILD_URL}"
                                        )
                                ],
                                footer: new Embed.Footer(
                                        text: Library.steps.env.JOB_BASE_NAME as String
                                )
                        )
                ]
        )
    }
}
