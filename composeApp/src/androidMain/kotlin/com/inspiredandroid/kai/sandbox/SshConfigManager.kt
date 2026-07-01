package com.inspiredandroid.kai.sandbox

import java.io.File

class SshConfigManager(private val sshDir: File) {

    init {
        sshDir.mkdirs()
    }

    fun ensureDefaults() {
        val configFile = File(sshDir, "config")
        if (!configFile.exists() || !configFile.readText().contains("Host kai:defaults")) {
            configFile.appendText(
                """
Host kai:defaults
    ServerAliveInterval 60
    ServerAliveCountMax 3
    StrictHostKeyChecking accept-new

""".trimIndent()
            )
        }
    }

    fun upsertHost(alias: String, hostname: String, user: String?, port: Int?, identityFile: String?): Boolean {
        val configFile = File(sshDir, "config")
        val existingContent = if (configFile.exists()) configFile.readText() else ""
        val newContent = StringBuilder()
        var updated = false
        var inHostBlock = false

        existingContent.lines().forEach { line ->
            if (line.trimStart().startsWith("Host ") && line.contains(alias)) {
                inHostBlock = true
                updated = true
                newContent.append("Host $alias\n")
                newContent.append("    HostName $hostname\n")
                user?.let { newContent.append("    User $it\n") }
                port?.let { newContent.append("    Port $it\n") }
                identityFile?.let { newContent.append("    IdentityFile $it\n") }
            } else if (inHostBlock && line.trimStart().startsWith("Host ")) {
                inHostBlock = false
                newContent.append("$line\n")
            } else if (!inHostBlock) {
                newContent.append("$line\n")
            }
        }

        if (!updated) {
            newContent.append("\nHost $alias\n")
            newContent.append("    HostName $hostname\n")
            user?.let { newContent.append("    User $it\n") }
            port?.let { newContent.append("    Port $it\n") }
            identityFile?.let { newContent.append("    IdentityFile $it\n") }
        }

        configFile.writeText(newContent.toString().trim() + "\n")
        return updated
    }

    fun appendKnownHostLine(line: String): Boolean {
        val knownHostsFile = File(sshDir, "known_hosts")
        if (!knownHostsFile.exists() || !knownHostsFile.readText().contains(line)) {
            knownHostsFile.appendText("$line\n")
            return true
        }
        return false
    }
}
