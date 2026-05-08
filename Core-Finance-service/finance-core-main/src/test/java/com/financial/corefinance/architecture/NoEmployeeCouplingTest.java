package com.financial.corefinance.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class NoEmployeeCouplingTest {

    @Test
    void codebaseShouldNotReferenceEmployeeService() throws IOException {
        Path root = Path.of("src/main");
        try (Stream<Path> paths = Files.walk(root)) {
            List<String> violatingFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java") || path.toString().endsWith(".yml") || path.toString().endsWith(".yaml"))
                    .filter(this::containsEmployeeCoupling)
                    .map(Path::toString)
                    .toList();

            assertTrue(violatingFiles.isEmpty(), "Employee coupling detected in files: " + violatingFiles);
        }
    }

    private boolean containsEmployeeCoupling(Path path) {
        try {
            String content = Files.readString(path).toLowerCase();
            return content.contains("employee-service")
                    || content.contains("employeeserviceclient")
                    || content.contains("employee.service.url")
                    || content.contains("employeedto");
        } catch (IOException ex) {
            return false;
        }
    }
}
