package com.github.cynic1254

import jenkins.Steps

class HelloWorld {
    Steps getSteps() {
        return new Steps()
    }

    void Print() {
        Steps.echo("Test")
    }
}
