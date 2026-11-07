package dev.detekt.gradle.invoke

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class FriendPathArgsSpec {
    @Test
    fun `passes the paths that exist`(@TempDir tempDir: File) {
        val existing = File(tempDir, "main.jar").apply { createNewFile() }
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()

        val actual = FriendPathArgs(project.files(existing)).toArgument()

        assertThat(actual).containsExactly("-Xfriend-paths", existing.absolutePath)
    }

    @Test
    fun `drops a path that does not exist`(@TempDir tempDir: File) {
        val existing = File(tempDir, "main.jar").apply { createNewFile() }
        val missing = File(tempDir, "disabled.jar")
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()

        val actual = FriendPathArgs(project.files(existing, missing)).toArgument()

        assertThat(actual).containsExactly("-Xfriend-paths", existing.absolutePath)
    }

    @Test
    fun `passes existing files and directories in order`(@TempDir tempDir: File) {
        val classes = File(tempDir, "classes").apply { mkdir() }
        val missing = File(tempDir, "disabled.jar")
        val jar = File(tempDir, "main.jar").apply { createNewFile() }
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()

        val actual = FriendPathArgs(project.files(classes, missing, jar)).toArgument()

        assertThat(actual).containsExactly("-Xfriend-paths", "${classes.absolutePath},${jar.absolutePath}")
    }

    @Test
    fun `checks friend paths when arguments are generated`(@TempDir tempDir: File) {
        val output = File(tempDir, "main.jar")
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        val subject = FriendPathArgs(project.files(output))

        assertThat(subject.toArgument()).isEmpty()

        output.createNewFile()

        assertThat(subject.toArgument()).containsExactly("-Xfriend-paths", output.absolutePath)

        output.delete()

        assertThat(subject.toArgument()).isEmpty()
    }

    @Test
    fun `passes nothing when no path exists`(@TempDir tempDir: File) {
        val missing = File(tempDir, "disabled.jar")
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()

        val actual = FriendPathArgs(project.files(missing)).toArgument()

        assertThat(actual).isEmpty()
    }

    @Test
    fun `passes nothing when the collection is empty`(@TempDir tempDir: File) {
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()

        val actual = FriendPathArgs(project.files()).toArgument()

        assertThat(actual).isEmpty()
    }
}
