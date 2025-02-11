package com.github.cynic1254

class ScriptVariables {
    static void Init(Object script)
    {
        this.script = script
        script.echo("Hello World")
    }

    public static Object script
}
