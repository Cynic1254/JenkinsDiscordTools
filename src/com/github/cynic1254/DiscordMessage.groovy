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

    void Send(Object steps, String webhook) {
        steps.bat(label: "Send Discord Message", script: "curl -X POST -H \"Content-Type: application/json\" -d \"${JsonOutput.toJson(this).replace('"', '""')}\" ${webhook}")
    }

    static Embed CreateEmbed() {
        return new Embed()
    }

    static DiscordMessage BaseReportMessage(Object steps, String header, String state, Integer color = null) {
        return new DiscordMessage(
                embeds: [
                        new Embed(
                                title: header,
                                color: color,
                                fields: [
                                        new Embed.Field(
                                                name: "${Unreal.config}${Unreal.platform} ${Jenkins.jobBaseName} ${state}",
                                                value: "Last Changelist: ${steps.env.P4_CHANGELIST}"
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

    static DiscordMessage Succeeded(Object steps) {
        return BaseReportMessage(steps, ":white_check_mark: BUILD SUCCEEDED :white_check_mark:", "has succeeded", 65280)
    }

    static DiscordMessage Failed(Object steps) {
        return BaseReportMessage(steps, ":x: BUILD FAILED :x:", "has failed", 16711680)
    }

    static DiscordMessage PartFailed(Object steps) {
        return BaseReportMessage(steps, ":o: BUILD FAILED PARTIALLY :o:", "has partially failed", 16744192)
    }

    static DiscordMessage Unstable(Object steps) {
        return BaseReportMessage(steps, ":warning: UNSTABLE BUILD :warning:", "is unstable", 16776960)
    }

    static DiscordMessage Aborted(Object steps) {
        return BaseReportMessage(steps, ":stop_sign: BUILD ABORTED :stop_sign:", "has been aborted", 255)
    }
}
