package com.github.cynic1254

import groovy.json.JsonOutput

class DiscordMessage {
    String username = null
    String avatar_url = null
    String content = null
    List<Embed> embeds = null
    Boolean tts = null
    Mentions allowed_mentions = null

    static class Embed {
        Integer color = null
        Author author = null
        String title = null
        String url = null
        String description = null
        List<Field> fields = null
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
        List<String> parse = null
        List<String> roles = null
        List<String> users = null
    }

    void Send(Object steps, String webhook) {
        steps.bat(label: "Send Discord Message", script: "curl -X POST -H \"Content-Type: application/json\" -d \"${JsonOutput.toJson(this).replace('"', '""')}\" ${webhook}")
    }

    static DiscordMessage BaseReportMessage(Object steps, String header, String state, Integer color = null) {
        return new DiscordMessage(
                embeds: [
                        new Embed(
                                title: header,
                                color: color,
                                fields: [
                                        new Embed.Field(
                                                name: "${steps.env.config}${steps.env.platform} ${steps.env.JOB_BASE_NAME} ${state}",
                                                value: "Last Changelist: ${steps.env.P4_CHANGELIST}"
                                        ),
                                        new Embed.Field(
                                                name: "Job url",
                                                value: "${steps.env.BUILD_URL}"
                                        )
                                ],
                                footer: new Embed.Footer(
                                        text: "${steps.env.JOB_BASE_NAME} (${steps.env.BUILD_NUMBER})"
                                )
                        )
                ]
        )
    }
}
