package com.github.cynic1254

import groovy.json.JsonOutput

class DiscordMessage_Embed_Author {
    String name = null
    String url = null
    String icon_url = null
}

class DiscordMessage_Embed_Field {
    String name = null
    String value = null
    Boolean inline = null
}

class URL {
    String url = null
}

class DiscordMessage_Embed_Footer {
    String text = null
    String icon_url = null
}

class DiscordMessage_Embed {
    Integer color = null
    DiscordMessage_Embed_Author author = null
    String title = null
    String url = null
    String description = null
    List<DiscordMessage_Embed_Field> fields = []
    URL thumbnail = null
    URL image = null
    DiscordMessage_Embed_Footer footer = null
    String timestamp = null
}

class DiscordMessage_Mentions {
    List<String> parse = []
    List<String> roles = []
    List<String> users = []
}

class DiscordMessage {
    String username = null
    String avatar_url = null
    String content = null
    List<DiscordMessage_Embed> embeds = []
    Boolean tts = null
    DiscordMessage_Mentions allowed_mentions = null

    void Send(Object steps, String webhook) {
        steps.bat(label: "Send Discord Message", script: "curl -X POST -H \"Content-Type: application/json\" -d \"${JsonOutput.toJson(this).replace('"', '""')}\" ${webhook}")
    }

    static DiscordMessage BaseReportMessage(Object steps, String header, String state, Integer color = null) {
        return new DiscordMessage(
                embeds: [
                        new DiscordMessage_Embed(
                                title: header,
                                color: color,
                                fields: [
                                        new DiscordMessage_Embed_Field(
                                                name: "${Unreal.config}${Unreal.platform} ${Jenkins.jobBaseName} ${state}",
                                                value: "Last Changelist: ${steps.env.P4_CHANGELIST}"
                                        ),
                                        new DiscordMessage_Embed_Field(
                                                name: "Job url",
                                                value: "${Jenkins.buildURL}"
                                        )
                                ],
                                footer: new DiscordMessage_Embed_Footer(
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
