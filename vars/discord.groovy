
def Send(message, uri) {
    bat(label: "Send Discord Message", script: "curl -X POST -H \"Content-Type: application/json\" -d \"${message}\" ${uri}")
}