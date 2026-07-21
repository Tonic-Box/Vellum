plugins {
    id("java")
    id("application")
}

group = "com.tonic.vellum"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jline:jline:3.25.1")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    // --release only exists on JDK 9+. When Gradle itself runs on JDK 8 (e.g. the IDE's
    // Gradle JVM), fall back to the source/target compatibility set above.
    if (JavaVersion.current().isJava9Compatible) {
        options.release.set(8)
    }
}

application {
    mainClass.set("com.tonic.examples.DashboardDemo")
}

tasks.test {
    useJUnitPlatform()
}

fun docSiteRepoUrl(): String? = try {
    val process = ProcessBuilder("git", "remote", "get-url", "origin").start()
    val url = process.inputStream.bufferedReader().readText().trim()
    if (process.waitFor() == 0 && url.isNotEmpty()) {
        url.removeSuffix(".git").replace(Regex("^git@([^:]+):"), "https://$1/")
    } else {
        null
    }
} catch (ignored: Exception) {
    null
}

fun docSiteLinksBar(template: File, projectName: String): String {
    val repoUrl = docSiteRepoUrl()
    return template.readText(Charsets.UTF_8)
        .lines()
        .filterNot { repoUrl == null && it.contains("@REPO_URL@") }
        .joinToString("\n")
        .replace("@REPO_URL@", repoUrl ?: "")
        .replace("@PROJECT_NAME@", projectName)
        .trim()
}

fun docSiteBarPage(linksBar: String, title: String): String =
    "<!DOCTYPE HTML>\n<html lang=\"en\">\n<head>\n<title>$title</title>\n" +
            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
            "<link rel=\"stylesheet\" type=\"text/css\" href=\"javadoc-dark.css\">\n" +
            "</head>\n<body class=\"withSiteBar\">\n$linksBar\n</body>\n</html>\n"

fun docSiteWrapFrameset(content: String): String {
    val open = content.indexOf("<frameset")
    val close = content.lastIndexOf("</frameset>")
    if (open < 0 || close < 0) {
        return content
    }
    return content.substring(0, open) +
            "<frameset rows=\"37,*\" title=\"Site frame\">\n" +
            "<frame src=\"site-bar.html\" title=\"Site links\" scrolling=\"no\">\n" +
            content.substring(open, close + "</frameset>".length) +
            "\n</frameset>" +
            content.substring(close + "</frameset>".length)
}

tasks.register<Javadoc>("docSite") {
    group = "documentation"
    description = "Generates the public API docsite into docs/"
    dependsOn(tasks.compileJava)
    source = sourceSets.main.get().allJava
    exclude("com/tonic/examples/**")
    classpath = sourceSets.main.get().output.classesDirs + sourceSets.main.get().compileClasspath
    setDestinationDir(file("docs"))
    outputs.upToDateWhen { false }
    (options as StandardJavadocDocletOptions).apply {
        memberLevel = JavadocMemberLevel.PUBLIC
        windowTitle = "Vellum"
        docTitle = "Vellum $version"
        encoding = "UTF-8"
        if (JavaVersion.current() >= JavaVersion.VERSION_1_9 && JavaVersion.current() <= JavaVersion.VERSION_12) {
            addBooleanOption("-frames", true)
        }
    }
    doFirst {
        delete("docs")
    }
    doLast {
        val docsDir = file("docs")
        copy {
            from("doc-assets/javadoc-dark.css")
            into(docsDir)
        }
        val linksBar = docSiteLinksBar(file("doc-assets/links-bar.html"), project.name.lowercase())
        val barInjection = "<script type=\"text/javascript\">if (window == top) document.documentElement.className += \" withSiteBar\";</script>\n$linksBar"
        val bodyTag = Regex("<body[^>]*>")
        val framesRedirect = Regex("<script type=\"text/javascript\">\\s*if \\(targetPage == \"\" \\|\\| targetPage == \"undefined\"\\)\\s*window\\.location\\.replace\\('overview-summary\\.html'\\);\\s*</script>")
        var framesetSeen = false
        docsDir.walkTopDown().filter { it.isFile && it.extension == "html" && it.name != "site-bar.html" }.forEach { page ->
            var content = page.readText(Charsets.UTF_8)
            if (content.contains("</head>") && !content.contains("javadoc-dark.css")) {
                val depth = generateSequence(page.parentFile) { it.parentFile }
                    .takeWhile { it != docsDir }
                    .count()
                val href = "../".repeat(depth) + "javadoc-dark.css"
                content = content.replace("</head>", "<link rel=\"stylesheet\" type=\"text/css\" href=\"$href\">\n</head>")
            }
            if (content.contains("<frameset") && !content.contains("site-bar.html")) {
                content = docSiteWrapFrameset(content)
                framesetSeen = true
            } else if (!page.name.endsWith("-frame.html") && !content.contains("class=\"siteBar\"")) {
                content = bodyTag.replaceFirst(content, "$0\n" + Regex.escapeReplacement(barInjection))
            }
            if (content.contains("class=\"mainContainer\"")) {
                content = framesRedirect.replace(content, "")
            }
            page.writeText(content, Charsets.UTF_8)
        }
        if (framesetSeen) {
            File(docsDir, "site-bar.html").writeText(docSiteBarPage(linksBar, "Site links"), Charsets.UTF_8)
        }
    }
}
